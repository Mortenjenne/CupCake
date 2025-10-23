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
        //TODO: post på remove line og clear cart?
    }

    private void showIndex(Context ctx) throws DatabaseException
    {
        var model = new HashMap<String, Object>();

        model.put("bottoms", shoppingService.getAllBottoms());
        model.put("toppings", shoppingService.getAllToppings());
        model.put("cart", getOrCreateCart(ctx));

        String label = ctx.sessionAttribute("succesLabel");
        if (label != null)
        {
            model.put("succesLabel", label);
            ctx.sessionAttribute("succesLabel", null);
        }
        ctx.render("index.html", model);
    }

    private void showBasket(Context ctx)
    {
        var model = new HashMap<String, Object>();
        model.put("cart", getOrCreateCart(ctx).getShoppingCart());

        ctx.render("basket.html", model);
    }

    private ShoppingCart getOrCreateCart(Context ctx)
    {
        ShoppingCart cart = ctx.sessionAttribute("CART");
        if (cart == null)
        {
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

    private void removeFromCart(Context ctx)
    {

    }

}
