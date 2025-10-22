package app.controllers;

import app.entities.ShoppingCart;
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
        //TODO: post på remove line og clear cart?
    }

    private void showIndex(Context ctx) throws DatabaseException
    {
        var model = new HashMap<String, Object>();
        model.put("bottoms", shoppingService.getAllBottoms());
        model.put("toppings", shoppingService.getAllToppings());
        model.put("cart", getOrCreateCart(ctx));
        ctx.render("index.html");
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

    }

    private void removeFromCart()
    {

    }

}
