package app.controllers;

import app.dto.UserDTO;
import app.entities.Order;
import app.entities.ShoppingCart;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.services.OrderService;
import app.services.UserService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
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
        app.get("/checkout/delivery/info", ctx -> showDeliveryPage(ctx));
        app.get("/order/confirmation", ctx -> showConfirmationPage(ctx));

        app.post("/checkout/delivery/info", ctx -> saveCheckoutContactInfo(ctx));
        app.post("/checkout/delivery/action", ctx -> handleDeliveryAction(ctx));
        app.post("/checkout/payment/action", ctx -> handlePayment(ctx));
    }

    private void showConfirmationPage(Context ctx)
    {
        ctx.render("/purchase-confirmed");
    }

    private void handlePayment(Context ctx)
    {
        String paymentMethod = ctx.formParam("paymentMethod");
        boolean payNow = paymentMethod.equals("pay-now");

        UserDTO checkoutUser = ctx.sessionAttribute("checkoutUser");
        ShoppingCart cart = ctx.sessionAttribute("cart");
        LocalDateTime pickupDateTime = ctx.sessionAttribute("pickUp");
        String deliveryMethod = ctx.sessionAttribute("deliveryMethod");

        if(!validateOrderDetails(ctx, checkoutUser, cart, pickupDateTime, deliveryMethod))
        {
            return;
        }

        if (payNow) {
            double totalPrice = cart.getTotalOrderPrice();

            if (checkoutUser.getUserId() == 0)
            {
                ctx.attribute("errorMessage", "Gæster kan kun betale ved afhentning");
                ctx.attribute("userBalanceAfterPurchase", 0);
                ctx.render("checkout");
                return;
            }

            if (checkoutUser.getBalance() < totalPrice)
            {
                ctx.attribute("errorMessage",
                        String.format("Du har ikke penge nok på din konto. Din saldo: %.2f kr. Pris: %.2f kr. Vælg 'betal ved afhentning' eller kontakt os for at indsætte penge.",
                                checkoutUser.getBalance(), totalPrice));
                ctx.attribute("userBalanceAfterPurchase", checkoutUser.getBalance() - totalPrice);
                ctx.render("checkout");
                return;
            }
        }

        try
        {
            Order order = orderService.createOrder(
                    checkoutUser.getUserId(),
                    cart.getShoppingCart(),
                    pickupDateTime,
                    payNow
            );
            cart.clearShoppingCart();

            ctx.sessionAttribute("cart", cart);
            ctx.sessionAttribute("CART", new ShoppingCart());
            ctx.sessionAttribute("completedOrder", order);
            ctx.render("purchase-confirmed");
        } catch (DatabaseException e)
        {
            ctx.attribute("errorMessage",e.getMessage());
            ctx.attribute("userBalanceAfterPurchase", checkoutUser.getBalance() - cart.getTotalOrderPrice());
            ctx.render("checkout.html");
        }

    }

    private void handleDeliveryAction(Context ctx)
    {
        String deliveryMethod = ctx.formParam("deliveryMethod");
        String pickupDate = ctx.formParam("pickupDate");
        String pickupTime = ctx.formParam("pickupTime");

        LocalDate date = LocalDate.parse(pickupDate);
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        LocalTime time = LocalTime.parse(pickupTime);

        if (!validateDateAndTime(ctx, date, time, dayOfWeek, pickupTime))
        {
            return;
        }

        LocalDateTime pickupDateTime = LocalDateTime.of(date, time);
        UserDTO userDTO = ctx.sessionAttribute("checkoutUser");
        ShoppingCart cart = ctx.sessionAttribute("cart");
        double balanceAfterPurchase = userDTO.getBalance() - cart.getTotalOrderPrice();

        ctx.sessionAttribute("pickUp", pickupDateTime);
        ctx.sessionAttribute("deliveryMethod", deliveryMethod);
        ctx.attribute("userBalanceAfterPurchase", balanceAfterPurchase);
        ctx.render("checkout");
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
                    userDTO = userService.getUserById(currentUser.getUserId());
                }
                ctx.sessionAttribute("checkoutUser", userDTO);
                ctx.render("checkout-delivery");

            } catch (NumberFormatException e)
            {
                ctx.attribute("errorMessage","Kun hel tal i postnummer og telefon nummer");
                ctx.attribute("currentUser", currentUser);
                ctx.attribute("cart", ctx.sessionAttribute("cart"));
                ctx.render("checkout-contact.html");
            } catch (DatabaseException | IllegalArgumentException e)
            {
                ctx.attribute("errorMessage", e.getMessage());
                ctx.attribute("currentUser", currentUser);
                ctx.attribute("cart", ctx.sessionAttribute("cart"));
                ctx.render("checkout-contact.html");
            }
    }

    private void showCheckoutContactPage(Context ctx)
    {
        User currentUser = ctx.sessionAttribute("currentUser");
        ShoppingCart cart = ctx.sessionAttribute("CART");

        ctx.attribute("currentUser", currentUser);
        ctx.sessionAttribute("cart",cart);
        ctx.render("checkout-contact");
    }

    private boolean validateDateAndTime(Context ctx, LocalDate date, LocalTime time, DayOfWeek dayOfWeek, String pickupTime)
    {
        if (dayOfWeek == DayOfWeek.MONDAY)
        {
            ctx.attribute("errorMessage", "Butikken er lukket om mandagen");
            ctx.render("checkout-delivery.html");
            return false;
        }

        if(date.isBefore(LocalDate.now()))
        {
            ctx.attribute("errorMessage", "Du kan ikke lægge ordre tilbage i tiden");
            ctx.render("checkout-delivery.html");
            return false;
        }

        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {

            if (time.isBefore(LocalTime.parse("10:00")) || time.isAfter(LocalTime.parse("17:30"))) {
                ctx.attribute("errorMessage", "I weekenden er butikken åben 10:00-17:30. Du valgte " + pickupTime);
                ctx.render("checkout-delivery.html");
                return false;
            }
        } else
        {

            if (time.isBefore(LocalTime.parse("12:30")) || time.isAfter(LocalTime.parse("17:00"))) {
                ctx.attribute("errorMessage", "På hverdage er butikken åben 12:30-17:00. Du valgte " + pickupTime);
                ctx.render("checkout-delivery.html");
                return false;
            }
        }
        return true;
    }

    private boolean validateOrderDetails(Context ctx, UserDTO checkoutUser, ShoppingCart cart, LocalDateTime pickupDateTime, String deliveryMethod)
    {
        if (checkoutUser == null)
        {
            ctx.redirect("/checkout/contact-info");
            return false;
        }

        if (cart == null || cart.getShoppingCart().isEmpty())
        {
            ctx.redirect("/cart");
            return false;
        }

        if(pickupDateTime == null)
        {
            ctx.redirect("/checkout/delivery/info");
            return false;
        }

        if(deliveryMethod == null || deliveryMethod.isEmpty())
        {
            ctx.redirect("/checkout/delivery/info");
            return false;
        }
        return true;
    }
}
