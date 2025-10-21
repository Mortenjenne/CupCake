package app.controllers;

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
    }

    private void showCreateUserPage(Context ctx)
    { ctx.render("createuser"); }

    private void showLoginPage(Context ctx)
    { ctx.render("/login"); }

}
