package app.controllers;

import app.exceptions.DatabaseException;
import app.services.CupcakeService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.HashMap;


public class CupcakeController
{
    private final CupcakeService cupcakeService;

    public CupcakeController(CupcakeService cupcakeService)
    {
        this.cupcakeService = cupcakeService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/cupcakes", this::showCupcakes);
    }

    private void showCupcakes(Context ctx) throws DatabaseException
    {
        var model = new HashMap<String, Object>();

        model.put("bottoms", cupcakeService.getAllBottoms());
        model.put("toppings", cupcakeService.getAllToppings());

        ctx.render("cupcakes.html", model);
    }


}
