package app.controllers;

import app.entities.Bottom;
import app.entities.Topping;
import app.exceptions.DatabaseException;
import app.services.CupcakeService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

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
        app.post("/updateBottom", this::updateBottom);
        app.post("/updateTopping", this::updateTopping);
    }

    private void showCupcakes(Context ctx) throws DatabaseException
    {
        var model = new HashMap<String, Object>();

        model.put("bottoms", cupcakeService.getAllBottoms());
        model.put("toppings", cupcakeService.getAllToppings());

        ctx.render("cupcakes.html", model);
    }

    private void updateBottom(Context ctx) throws DatabaseException
    {
        int bottomId = Integer.parseInt(ctx.formParam("bottomId"));
        String inputText = ctx.formParam("bottomUpdateFlavour");
        String inputDouble = ctx.formParam("bottomUpdatePrice");
        Bottom bottom = cupcakeService.getBottomById(bottomId);

        if (!inputText.isEmpty())
        {
            cupcakeService.renameBottomFlavour(bottom, inputText.trim());
        }

        if (!inputDouble.isEmpty())
        {
            try
            {
                cupcakeService.setBottomPrice(bottom, Double.parseDouble(inputDouble.replace(",", ".").trim()));

            }
            catch (NumberFormatException e)
            {
                ctx.attribute("cupcakeErrorMessage","Indtast venligst gyldigt tal");
            }
        }
        ctx.redirect("/cupcakes");
    }

    private void updateTopping(Context ctx) throws DatabaseException
    {
        int toppingId = Integer.parseInt(ctx.formParam("toppingId"));
        String newToppingFlavour = ctx.formParam("toppingUpdateFlavour");
        String newToppingPrice = ctx.formParam("toppingUpdatePrice");
        Topping topping = cupcakeService.getToppingById(toppingId);

        if (!newToppingFlavour.isEmpty())
        {
            cupcakeService.renameToppingFlavour(topping, newToppingFlavour.trim());
        }

        if (!newToppingPrice.isEmpty())
        {
            try
            {
                cupcakeService.setToppingPrice(topping, Double.parseDouble(newToppingPrice.replace(",", ".").trim()));
            }
            catch (NumberFormatException e)
            {
                ctx.attribute("cupcakeErrorMessage","Indtast venligst gyldigt tal");
            }
        }


        ctx.redirect("/cupcakes");

    }


}
