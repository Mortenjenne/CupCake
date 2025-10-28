package app.controllers;

import app.dto.UserDTO;
import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.services.OrderService;
import io.javalin.Javalin;
import io.javalin.http.Context;

import java.util.ArrayList;
import java.util.List;

public class OrderController
{
    private OrderService orderService;

    public OrderController(OrderService orderService)
    {
        this.orderService = orderService;
    }

    public void addRoutes(Javalin app)
    {
      app.get("/orders", ctx -> showOrderPage(ctx));
    }

    private void showOrderPage(Context ctx)
    {
        User currentUser = ctx.sessionAttribute("currentUser");
        validateCurrentUserIsAdmin(ctx, currentUser);

        try
        {
            List<Order> unpaidOrders = orderService.getAllOrdersByStatusNotPaid(currentUser.getUserId());
            List<Order> paidOrders = orderService.getAllOrdersByStatusPaid(currentUser.getUserId());
            loadErrorAndSuccesMessage(ctx);
            ctx.attribute("unpaidOrders", unpaidOrders);
            ctx.attribute("paidOrders", paidOrders);
            ctx.render("orders");
        }
        catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.attribute("unpaidOrders", new ArrayList<>());
            ctx.attribute("paidOrders", new ArrayList<>());
            ctx.redirect("/orders");
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

}
