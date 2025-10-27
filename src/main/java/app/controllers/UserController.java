package app.controllers;

import app.dto.CreateUserRequestDTO;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.services.UserService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;

public class UserController
{
    private UserService userService;

    public UserController(UserService userService)
    {
        this.userService = userService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/login", ctx -> showLoginPage(ctx));
        app.get("/create-user", ctx -> showCreateUserPage(ctx));
        app.get("/logout", ctx -> logOut(ctx));

        app.post("/create-user", ctx -> handleCreateUser(ctx));
        app.post("/login", ctx -> handleUserLogin(ctx));
    }

    private void logOut(Context ctx)
    {
        ctx.req().getSession().invalidate();
        ctx.redirect("/");
    }

    private void handleUserLogin(Context ctx)
    {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        try
        {
            User user = userService.authenticate(email, password);
            ctx.sessionAttribute("currentUser",user);
            ctx.sessionAttribute("CART", new ArrayList()); // TODO Refactor this
            ctx.render("/index");
        } catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.render("login");
        }
    }

    private void handleCreateUser(Context ctx)
    {
        CreateUserRequestDTO createUserRequestDTO = new CreateUserRequestDTO(
                ctx.formParam("email"),
                ctx.formParam("password1"),
                ctx.formParam("password2"),
                ctx.formParam("firstName"),
                ctx.formParam("lastName"),
                ctx.formParam("street"),
                ctx.formParam("city"),
                ctx.formParam("zipCode"),
                ctx.formParam("phone")
        );

        try
        {
            userService.registerUser(createUserRequestDTO);
            ctx.sessionAttribute("succesMessage","Du har oprettet en bruger! Log på med email og password");
            ctx.redirect("login");
        } catch (DatabaseException | IllegalArgumentException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            keepFormValues(
                    ctx,
                    createUserRequestDTO.getEmail(),
                    createUserRequestDTO.getFirstName(),
                    createUserRequestDTO.getLastName(),
                    createUserRequestDTO.getStreet(),
                    createUserRequestDTO.getZipCode(),
                    createUserRequestDTO.getCity(),
                    createUserRequestDTO.getPhoneNumber()
            );
            ctx.render("createuser.html");
        }
    }

    private void showCreateUserPage(Context ctx)
    { ctx.render("createuser"); }

    private void showLoginPage(Context ctx)
    {ctx.render("login"); }

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
