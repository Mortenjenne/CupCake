package app.controllers;

import app.exceptions.DatabaseException;
import app.services.UserService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

public class UserController
{
    private UserService userService;

    public UserController(UserService userService)
    {
        this.userService = userService;
    }

    public void registerRoutes(Javalin app)
    {
        app.get("/login", ctx -> showLoginPage(ctx));
        app.get("/create-user", ctx -> showCreateUserPage(ctx));

        app.post("/create-user", ctx -> handleCreateUser(ctx));
        app.post("/login", ctx -> handleUserLogin(ctx));
    }

    private void handleUserLogin(Context ctx)
    {
    }

    private void handleCreateUser(Context ctx)
    {
        String email      = ctx.formParam("email");
        String password1  = ctx.formParam("password1");
        String password2  = ctx.formParam("password2");
        String firstName  = ctx.formParam("firstName");
        String lastName   = ctx.formParam("lastName");
        String street     = ctx.formParam("street");
        String city       = ctx.formParam("city");
        String zipStr     = ctx.formParam("zipCode");
        String phone      = ctx.formParam("phone");

        if (!password1.equals(password2))
        {
            ctx.attribute("errorMessage", "Passwords er ikke ens");
            keepFormValues(ctx, email, firstName, lastName, street, zipStr, city, phone);
            ctx.render("createuser.html");
            return;
        }

        try
        {

        } catch (DatabaseException | IllegalArgumentException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.render("create-user.html");
        }
    }

    private void showCreateUserPage(Context ctx)
    { ctx.render("createuser"); }

    private void showLoginPage(Context ctx)
    { ctx.render("/login"); }

    private void keepFormValues(Context ctx, String email, String firstName, String lastName, String street, String zipCode, String city, String phone)
    {
        ctx.attribute("email", email);
        ctx.attribute("firstName", firstName);
        ctx.attribute("lastName", lastName);
        ctx.attribute("street", street);
        ctx.attribute("zipCode", zipCode);
        ctx.attribute("city", city);
        ctx.attribute("phone", phone);
    }

}
