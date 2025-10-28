package app.controllers;

import app.dto.UserDTO;
import app.entities.Order;
import app.entities.OrderLine;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.services.OrderService;
import app.services.UserService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class AdminController
{
    private UserService userService;
    private OrderService orderService;

    public AdminController(UserService userService, OrderService orderService)
    {
        this.userService = userService;
        this.orderService = orderService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/customers", ctx -> showCustomerPage(ctx));
        app.get("/customers/edit/{id}", ctx -> showEditCustomerPage(ctx));
        app.get("/customers/search", ctx -> handleSearchQuery(ctx));
        app.get("/orders/details/{id}", ctx -> showOrderDetails(ctx));

        app.post("/customers/update-balance", ctx -> handleEditCustomerBalance(ctx));
        app.post("/orders/delete/{id}", ctx -> deleteOrder(ctx));
        app.post("/orders/mark-paid/{id}", ctx -> markOrderAsPaid(ctx));

    }

    private void markOrderAsPaid(Context ctx)
    {
        String orderIdStr = ctx.pathParam("id");
        User currentUser = ctx.sessionAttribute("currentUser");

        try
        {
            int orderId = Integer.parseInt(orderIdStr);
            if (orderService.updateOrderPaymentStatus(orderId, true, currentUser.getUserId()))
            {
                ctx.redirect("/orders");
                ctx.attribute("successMessage", "Du har opdateret status på ordre id: " + orderId);
            }
            else
            {
                ctx.attribute("errorMessage", "Kunne ikke sætte status til betalt");
                ctx.redirect("/orders");
            }

        }
        catch (NumberFormatException e)
        {
            ctx.attribute("errorMessage", "Kunne ikke parse tallet");
            ctx.redirect("/orders");
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.redirect("/orders");
        }
    }

    private void deleteOrder(Context ctx)
    {
        String orderIdStr = ctx.pathParam("id");
        User currentUser = ctx.sessionAttribute("currentUser");

        try
        {
            int orderId = Integer.parseInt(orderIdStr);
            if (orderService.deleteOrder(orderId, currentUser.getUserId(), false))
            {
                ctx.redirect("/orders");
                ctx.attribute("successMesage", "Du har slette ordren med id " + orderId);
            }
            else
            {
                ctx.attribute("errorMessage", "Kunne ikke slette ordren");
                ctx.redirect("/orders");
            }

        }
        catch (NumberFormatException e)
        {
            ctx.attribute("errorMessage", "Kunne ikke parse tallet");
            ctx.redirect("/orders");
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.redirect("/orders");
        }
    }

    private void showOrderDetails(Context ctx)
    {
        String orderIdStr = ctx.pathParam("id");

        try
        {
            int orderId = Integer.parseInt(orderIdStr);
            List<OrderLine> orderLines = orderService.getAllOrderLinesByOrderId(orderId);

            ctx.attribute("id", orderId);
            ctx.attribute("orderLines", orderLines);
            ctx.render("order-details");
        }
        catch (NumberFormatException e)
        {
            ctx.attribute("errorMessage", "Kunne ikke parse tallet");
            ctx.redirect("/orders");
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.redirect("/orders");
        }
    }

    private void handleSearchQuery(Context ctx)
    {
        User currentUser = ctx.sessionAttribute("currentUser");
        validateCurrentUserIsAdmin(ctx, currentUser);

        String searchType = ctx.queryParam("searchType");
        String searchQuery = ctx.queryParam("searchQuery");

        validateSearchType(ctx, searchType, searchQuery);

        try
        {
            List<UserDTO> customers = null;
            switch (searchType)
            {
                case "id":
                    try
                    {
                        int userId = Integer.parseInt(searchQuery.trim());
                        customers = userService.searchUsersByUserId(userId);
                    }
                    catch (NumberFormatException e)
                    {
                        ctx.attribute("errorMessage", "Kunde ID skal være et tal");
                        customers = new ArrayList<>();
                    }
                    break;
                case "name":
                    customers = userService.searchUsersByName(searchQuery.trim());
                    break;
                case "email":
                    customers = userService.searchUsersByEmail(searchQuery.trim());
                    break;
                default:
                    ctx.attribute("errorMessage", "Ugyldig søgetype");
                    customers = new ArrayList<>();
            }

            ctx.attribute("searchType", searchType);
            ctx.attribute("searchQuery", searchQuery);
            ctx.attribute("customers", customers);

            if (customers.isEmpty())
            {
                ctx.attribute("errorMessage", "Ingen kunder fundet med søgning: " + searchQuery);
            }
            ctx.render("customers.html");

        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.attribute("customers", new ArrayList<>());
            ctx.render("customers.html");
        }
    }

    private void showEditCustomerPage(Context ctx)
    {
        User currentUser = ctx.sessionAttribute("currentUser");
        validateCurrentUserIsAdmin(ctx, currentUser);

        try
        {
            int userId = Integer.parseInt(ctx.pathParam("id"));
            UserDTO customer = userService.getUserById(userId);

            ctx.attribute("customer", customer);
            ctx.render("editcustomer.html");
        }
        catch (NumberFormatException e)
        {
            ctx.attribute("errorMessage", "Ugyldig kunde ID");
            ctx.redirect("/customers");
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.redirect("/customers");
        }
    }

    private void handleEditCustomerBalance(Context ctx)
    {
        User currentUser = ctx.sessionAttribute("currentUser");
        validateCurrentUserIsAdmin(ctx, currentUser);

        try
        {
            double newBalance = Double.parseDouble(ctx.formParam("newBalance"));
            int userId = Integer.parseInt(ctx.formParam("userId"));

            userService.addBalance(userId, newBalance);

            ctx.sessionAttribute("successMessage", "Balance opdateret!");
            ctx.redirect("/customers");
        }
        catch (NumberFormatException e)
        {
            ctx.sessionAttribute("errorMessage", "Ugyldig balance værdi");
            ctx.redirect("/customers");
        }
        catch (DatabaseException e)
        {
            ctx.sessionAttribute("errorMessage", "Fejl: " + e.getMessage());
            ctx.redirect("/customers");
        }
    }

    private void showCustomerPage(Context ctx)
    {
        User currentUser = ctx.sessionAttribute("currentUser");

        if (currentUser == null)
        {
            ctx.attribute("errorMessage", "Du skal være logget ind for at se denne side");
            ctx.redirect("/login");
            return;
        }

        validateCurrentUserIsAdmin(ctx, currentUser);

        try
        {
            List<UserDTO> users = userService.getAllUsers();
            loadErrorAndSuccesMessage(ctx);
            ctx.attribute("customers", users);
            ctx.render("customers.html");
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.attribute("customers", new ArrayList<>());
            ctx.render("customers.html");
        }
    }

    private void validateCurrentUserIsAdmin(Context ctx, User currentUser)
    {
        if (currentUser == null || !currentUser.isAdmin())
        {
            ctx.attribute("errorMessage", "Du har ikke adgang til denne side");
            ctx.redirect("/");
            return;
        }
    }

    private void loadErrorAndSuccesMessage(Context ctx)
    {
        String successMessage = ctx.sessionAttribute("successMessage");
        String errorMessage = ctx.sessionAttribute("errorMessage");

        if (successMessage != null)
        {
            ctx.attribute("successMessage", successMessage);
            ctx.consumeSessionAttribute("successMessage");
        }

        if (errorMessage != null)
        {
            ctx.attribute("errorMessage", errorMessage);
            ctx.consumeSessionAttribute("errorMessage");
        }
    }

    private void validateSearchType(Context ctx, String searchType, String searchQuery)
    {
        if (searchType == null || searchQuery == null || searchQuery.trim().isEmpty())
        {
            ctx.attribute("errorMessage", "Du skal vælge søgetype og indtaste søgeord");
            ctx.redirect("/customers");
            return;
        }
    }

}
