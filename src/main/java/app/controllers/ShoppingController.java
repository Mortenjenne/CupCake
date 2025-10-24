package app.controllers;

import app.entities.Bottom;
import app.entities.ShoppingCart;
import app.entities.Topping;
import app.exceptions.DatabaseException;
import app.services.ShoppingService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.HashMap;
import java.util.List;

public class ShoppingController
{
    private final ShoppingService shoppingService;

    public ShoppingController(ShoppingService shoppingService)
    {
        this.shoppingService = shoppingService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/", this::showIndex);
        app.post("/cart/add", this::addToCart);
        app.get("/basket", this::showBasket);
        app.post("/basket/action", this::basketActions);
        app.get("/checkout",  this::showCheckout);
        app.post("/clearCart", this::clearCart);
        //TODO: post på remove line og clear cart?
    }

    private void showIndex(Context ctx) throws DatabaseException
    {
        var model = new HashMap<String, Object>();

        model.put("bottoms", shoppingService.getAllBottoms());
        model.put("toppings", shoppingService.getAllToppings());
        model.put("cart", getOrCreateCart(ctx));

        String label = ctx.sessionAttribute("succesLabel");
        if (label != null) {
            model.put("succesLabel", label);
            ctx.sessionAttribute("succesLabel", null);
        }
        ctx.render("index.html", model);
    }

    private void showBasket(Context ctx) throws DatabaseException
    {

        ShoppingCart cart = getOrCreateCart(ctx);
        // FOR TESTING DESIGN
        if (cart.getShoppingCart().isEmpty()) {
            populateTestCart(ctx);
        }

        var model = new HashMap<String, Object>();
        model.put("cart", cart);
        model.put("basketTotalPrice", shoppingService.getTotalOrderPrice(cart));

        ctx.render("basket.html", model);
    }

    private ShoppingCart getOrCreateCart(Context ctx)
    {
        ShoppingCart cart = ctx.sessionAttribute("CART");
        if (cart == null) {
            cart = new ShoppingCart();
            ctx.sessionAttribute("CART", cart);
        }
        return cart;
    }

    private void addToCart(Context ctx)
    {
        try {
            int bottomId = Integer.parseInt(ctx.formParam("bottomId"));
            int toppingId = Integer.parseInt(ctx.formParam("toppingId"));

            Bottom bottom = shoppingService.getBottomById(bottomId);
            Topping topping = shoppingService.getToppingById(toppingId);

            ShoppingCart cart = getOrCreateCart(ctx);

            int quantity = Integer.parseInt(ctx.formParam("cupcakeQuantity"));

            shoppingService.addOrderLineToCart(cart, bottom, topping, quantity);

            ctx.sessionAttribute("CART", cart);
            ctx.sessionAttribute("succesLabel", "Din ordre er tilføjet til kurven!:)");
            ctx.redirect("/");
        }
        catch (NullPointerException e) {
            ctx.sessionAttribute("indexErrorLabel", "Du skal have udfyldt alle felterne");
            ctx.redirect("/");
        }
        catch (DatabaseException e) {
            ctx.sessionAttribute("indexErrorLabel", "Kunne ikke finde cupcake info");
            ctx.redirect("/");
        }
    }

    private void basketActions(Context ctx)
    {
        String deleteIndexParam = ctx.formParam("index");
        String increaseQuantity = ctx.formParam("increaseQuantity");
        String decreaseQuantity = ctx.formParam("decreaseQuantity");


        if (deleteIndexParam != null) {
            removeFromCart(ctx);
        }

        if (increaseQuantity != null) {
            increaseCupcakeQuantity(ctx);
        }

        if (decreaseQuantity != null) {
            decreaseCupcakeQuantity(ctx);
        }

    }


    private void removeFromCart(Context ctx)
    {
        int index = Integer.parseInt(ctx.formParam("index"));
        shoppingService.removeOrderLineFromCart(getOrCreateCart(ctx), index);
        ctx.sessionAttribute("CART", getOrCreateCart(ctx));
        ctx.redirect("/basket");
    }

    private void increaseCupcakeQuantity(Context ctx)
    {
        int index = Integer.parseInt(ctx.formParam("increaseQuantity"));
        shoppingService.addOneToCupcakeQuantity(getOrCreateCart(ctx),index);
        ctx.sessionAttribute("CART", getOrCreateCart(ctx));
        ctx.redirect("/basket");
    }

    private void decreaseCupcakeQuantity(Context ctx)
    {
        int index = Integer.parseInt(ctx.formParam("decreaseQuantity"));
        shoppingService.removeOneFromCupcakeQuantity(getOrCreateCart(ctx),index);
        ctx.sessionAttribute("CART", getOrCreateCart(ctx));
        ctx.redirect("/basket");
    }

    private void clearCart(Context ctx)
    {
        shoppingService.clearCart(getOrCreateCart(ctx));
        ctx.sessionAttribute("CART", getOrCreateCart(ctx));
        ctx.redirect("/basket");
    }

    private void showCheckout(Context ctx)
    {
        ctx.sessionAttribute("CART", getOrCreateCart(ctx));
        ctx.render("/checkout");
    }


    //FOR DESIGN TESTING IN BASKET.HTML
    private void populateTestCart(Context ctx) throws DatabaseException
    {
        ShoppingCart cart = getOrCreateCart(ctx);

        // Get all bottoms and toppings
        List<Bottom> bottoms = shoppingService.getAllBottoms();
        List<Topping> toppings = shoppingService.getAllToppings();

        // Add 12 different combinations
        for (int i = 0; i < 6; i++) {
            Bottom bottom = bottoms.get(i % bottoms.size());
            Topping topping = toppings.get(i % toppings.size());
            int quantity = (i % 5) + 1; // quantities from 1-5

            shoppingService.addOrderLineToCart(cart, bottom, topping, quantity);
        }

        ctx.sessionAttribute("CART", cart);
    }


}
