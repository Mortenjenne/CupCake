package app.controllers;

import app.entities.Bottom;
import app.entities.ShoppingCart;
import app.entities.Topping;
import app.exceptions.DatabaseException;
import app.services.ShoppingService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.HashMap;

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
        app.get("/checkout", this::showCheckout);
        app.post("/clearCart", this::clearCart);
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

        var model = new HashMap<String, Object>();
        model.put("cart", cart);
        model.put("basketTotalPrice", shoppingService.getTotalOrderPrice(cart));
        model.put("basketTotalQuantity", shoppingService.getTotalOrderQuantity(cart));

        ctx.render("basket.html", model);
    }

    private void showCheckout(Context ctx)
    {
        ShoppingCart cart = getOrCreateCart(ctx);
        if (cart == null || cart.isEmpty())
        {
            ctx.sessionAttribute("basketErrorLabel", "Din kurv er tom");
            ctx.redirect("/basket");
            return;
        }

        var model = new HashMap<String, Object>();
        model.put("cart", cart);
        model.put("basketTotalPrice", shoppingService.getTotalOrderPrice(cart));
        model.put("basketTotalQuantity", shoppingService.getTotalOrderQuantity(cart));

        ctx.render("checkout.html", model);
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
        ShoppingCart cart = getOrCreateCart(ctx);
        int index = Integer.parseInt(ctx.formParam("index"));
        shoppingService.removeOrderLineFromCart(cart, index);
        ctx.sessionAttribute("CART", cart);
        ctx.redirect("/basket");

    }

    private void increaseCupcakeQuantity(Context ctx)
    {
        ShoppingCart cart = getOrCreateCart(ctx);
        int index = Integer.parseInt(ctx.formParam("increaseQuantity"));
        shoppingService.addOneToCupcakeQuantity(cart, index);
        ctx.sessionAttribute("CART", cart);
        ctx.redirect("/basket");
    }

    private void decreaseCupcakeQuantity(Context ctx)
    {
        ShoppingCart cart = getOrCreateCart(ctx);
        int index = Integer.parseInt(ctx.formParam("decreaseQuantity"));
        shoppingService.removeOneFromCupcakeQuantity(cart, index);
        ctx.sessionAttribute("CART", cart);
        ctx.redirect("/basket");
    }

    private void clearCart(Context ctx)
    {
        ShoppingCart cart = getOrCreateCart(ctx);
        shoppingService.clearCart(cart);
        ctx.sessionAttribute("CART", cart);
        ctx.redirect("/basket");
    }
}
