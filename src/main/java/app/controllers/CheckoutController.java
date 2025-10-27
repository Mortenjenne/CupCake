package app.controllers;

import app.dto.UserDTO;
import app.entities.ShoppingCart;
import app.entities.User;
import app.services.OrderService;
import app.services.UserService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CheckoutController
{
    private OrderService orderService;
    private UserService userService;

    public CheckoutController(OrderService orderService, UserService userService)
    {
        this.orderService = orderService;
        this.userService = userService;
    }

    public void addRoutes(Javalin app)
    {
        app.get("/checkout/contact-info", ctx -> showCheckoutContactPage(ctx));
        app.get("checkout/delivery/info", ctx -> showDeliveryPage(ctx));

        app.post("/checkout/contact-info", ctx -> saveCheckoutContactInfo(ctx));
        app.post("/checkout/delivery/action", ctx -> handleDeliveryAction(ctx));

    }

    private void handleDeliveryAction(Context ctx)
    {
        String deliveryMethod = ctx.formParam("deliveryMethod");
        String pickupDate = ctx.formParam("pickupDate");
        String pickupTime = ctx.formParam("pickupTime");

        LocalDate date = LocalDate.parse(pickupDate);
        LocalTime time = LocalTime.parse(pickupTime);
        LocalDateTime pickupDateTime = LocalDateTime.of(date, time);

        UserDTO userDTO = ctx.sessionAttribute("checkoutUser");
        ShoppingCart cart = ctx.sessionAttribute("cart");
    }

    private void showDeliveryPage(Context ctx)
    {
        UserDTO checkoutUser = ctx.sessionAttribute("checkoutUser");
        if (checkoutUser == null) {
            ctx.redirect("/checkout/contact-info");
            return;
        }

        ShoppingCart cart = ctx.sessionAttribute("cart");
        if (cart == null || cart.getShoppingCart().isEmpty()) {
            ctx.redirect("/cart");
            return;
        }
        ctx.render("checkout-delivery.html");
    }

    private void saveCheckoutContactInfo(Context ctx)
    {
            User currentUser = ctx.sessionAttribute("currentUser");
            UserDTO userDTO = null;

            String firstName = ctx.formParam("firstName");
            String lastName = ctx.formParam("lastName");
            String email = ctx.formParam("email");
            String phoneNumberStr = ctx.formParam("phoneNumber");
            String street = ctx.formParam("street");
            String city = ctx.formParam("city");
            String zipCodeStr = ctx.formParam("zipCode");

            try
            {
                int phoneNumber = Integer.parseInt(phoneNumberStr);
                int zipCode = Integer.parseInt(zipCodeStr);
                userService.validateInput(firstName, lastName, street, zipCode, city, phoneNumber, email);

                if(currentUser == null)
                {
                    userDTO = new UserDTO(
                            0,
                            firstName,
                            lastName,
                            email,
                            phoneNumber,
                            street,
                            zipCode,
                            city,
                            0
                    );
                } else
                {
                    userDTO = new UserDTO(
                            currentUser.getUserId(),
                            firstName,
                            lastName,
                            email,
                            phoneNumber,
                            street,
                            zipCode,
                            city,
                            currentUser.getBalance()
                    );
                }
                ctx.sessionAttribute("checkoutUser", userDTO);
                ctx.redirect("/checkout/delivery");

            } catch (NumberFormatException e)
            {
                ctx.attribute("errorMessage","Kun hel tal i postnummer og telefon nummer");
                ctx.attribute("currentUser", currentUser);
                ctx.attribute("cart", ctx.sessionAttribute("cart"));
                ctx.render("checkout-contact.html");
            } catch (IllegalArgumentException e)
            {
                ctx.attribute("errorMessage", e.getMessage());
                ctx.attribute("currentUser", currentUser);
                ctx.attribute("cart", ctx.sessionAttribute("cart"));
                ctx.render("checkout-contact.html");
            }

        ctx.sessionAttribute("checkoutUser", userDTO);
        ctx.redirect("/checkout/delivery");
    }

    private void showCheckoutContactPage(Context ctx)
    {
        User currentUser = ctx.sessionAttribute("currentUser");
        ShoppingCart cart = ctx.sessionAttribute("cart");

        ctx.attribute("currentUser", currentUser);
        ctx.attribute("cart", cart);
        ctx.render("checkout-contact.html");
    }
}
