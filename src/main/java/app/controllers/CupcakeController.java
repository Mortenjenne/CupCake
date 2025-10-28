package app.controllers;

import app.entities.Bottom;
import app.entities.Topping;
import app.exceptions.DatabaseException;
import app.services.CupcakeService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
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
        app.post("/createBottom", this::createBottom);
        app.post("/createTopping", this::createTopping);
        app.post("/updateBottom", this::updateBottom);
        app.post("/updateTopping", this::updateTopping);
        app.post("/deleteBottom", this::deleteBottom);
        app.post("/deleteTopping", this::deleteTopping);
    }

    private void showCupcakes(Context ctx) throws DatabaseException
    {
        var model = new HashMap<String, Object>();

        model.put("bottoms", cupcakeService.getAllBottoms());
        model.put("toppings", cupcakeService.getAllToppings());

        ctx.render("cupcakes.html", model);
    }

    private void createBottom(Context ctx) throws DatabaseException
    {
        String bottomFlavour = ctx.formParam("bottomCreateFlavour");
        String bottomPrice = ctx.formParam("bottomCreatePrice");

        if (bottomFlavour != null && !bottomFlavour.isEmpty() &&
                bottomPrice != null && !bottomPrice.isEmpty())
        {
            try
            {
                cupcakeService.createNewBottomFlavour(bottomFlavour.trim(), Double.parseDouble(bottomPrice.replace(",", ".").trim()));
            }
            catch (NumberFormatException e)
            {
                ctx.attribute("cupcakeErrorMessage", "Indtast venligst gyldigt tal");
                return;
            }
        }
        ctx.redirect("/cupcakes");
    }

    private void createTopping(Context ctx) throws DatabaseException
    {
        String toppingFlavour = ctx.formParam("toppingCreateFlavour");
        String toppingCreatePrice = ctx.formParam("toppingCreatePrice");

        if (toppingFlavour != null && toppingCreatePrice != null &&
                !toppingFlavour.isEmpty() && !toppingCreatePrice.isEmpty())
        {
            try
            {
                cupcakeService.createNewToppingFlavour(toppingFlavour.trim(), Double.parseDouble(toppingCreatePrice.replace(",", ".").trim()));
            }
            catch (NumberFormatException e)
            {
                ctx.attribute("cupcakeErrorMessage", "Indtast venligst gyldigt tal");
                return;
            }
        }
        ctx.redirect("/cupcakes");
    }

    private void updateBottom(Context ctx) throws DatabaseException
    {
        int bottomId = Integer.parseInt(ctx.formParam("bottomId"));
        String newBottomFlavour = ctx.formParam("bottomUpdateFlavour");
        String newBottomPrice = ctx.formParam("bottomUpdatePrice");
        Bottom bottom = cupcakeService.getBottomById(bottomId);

        if (newBottomFlavour != null && !newBottomFlavour.isEmpty())
        {
            bottom.setName(newBottomFlavour.trim());
        }

        if (newBottomPrice != null && !newBottomPrice.isEmpty())
        {
            try
            {
                bottom.setPrice(Double.parseDouble(newBottomPrice.replace(",", ".").trim()));
            }
            catch (NumberFormatException e)
            {
                ctx.attribute("cupcakeErrorMessage", "Indtast venligst gyldigt tal");
                return;
            }
        }
        cupcakeService.updateBottom(bottom);
        ctx.redirect("/cupcakes");
    }

    private void updateTopping(Context ctx) throws DatabaseException
    {
        int toppingId = Integer.parseInt(ctx.formParam("toppingId"));
        String newToppingFlavour = ctx.formParam("toppingUpdateFlavour");
        String newToppingPrice = ctx.formParam("toppingUpdatePrice");
        Topping topping = cupcakeService.getToppingById(toppingId);

        if (newToppingFlavour != null && !newToppingFlavour.isEmpty())
        {
            topping.setName(newToppingFlavour.trim());
        }

        if (newToppingFlavour != null && !newToppingPrice.isEmpty())
        {
            try
            {
                topping.setPrice(Double.parseDouble(newToppingPrice.replace(",", ".").trim()));
            }
            catch (NumberFormatException e)
            {
                ctx.attribute("cupcakeErrorMessage", "Indtast venligst gyldigt tal");
                return;
            }
        }
        cupcakeService.updateTopping(topping);
        ctx.redirect("/cupcakes");
    }

    private void deleteBottom(Context ctx) throws DatabaseException
    {
        int bottomId = Integer.parseInt(ctx.formParam("bottomId"));
        cupcakeService.deleteBottomFlavour(cupcakeService.getBottomById(bottomId));

        ctx.redirect("/cupcakes");
    }

    private void deleteTopping(Context ctx) throws DatabaseException
    {
        int toppingId = Integer.parseInt(ctx.formParam("toppingId"));
        cupcakeService.deleteToppingFlavour(cupcakeService.getToppingById(toppingId));

        ctx.redirect("/cupcakes");
    }
}