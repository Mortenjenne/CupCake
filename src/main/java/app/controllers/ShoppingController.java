package app.controllers;

import app.entities.ShoppingCart;
import app.entities.Topping;
import app.services.ShoppingService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingController
{
    private final ShoppingService shoppingService;

    public ShoppingController(ShoppingService shoppingService)
    {
        this.shoppingService = shoppingService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/", ctx -> showIndex());
    }

    private void showIndex() {
        List<Topping> toppingList = shoppingService.getAllBottoms();
    }

}
