package app.controllers;

import app.dto.UserDTO;
import app.entities.Order;
import app.entities.ShoppingCart;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.services.*;
import io.javalin.Javalin;
import io.javalin.http.Context;

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
        app.get("/checkout/delivery", ctx -> showDeliveryPage(ctx));
        app.post("/checkout/delivery", ctx -> saveDeliveryInfo(ctx));

        app.get("/checkout/contact-info", ctx -> showContactInfoPage(ctx));
        app.post("/checkout/contact-info", ctx -> saveContactInfo(ctx));

        app.get("/checkout/payment", ctx -> showPaymentPage(ctx));
        app.post("/checkout/payment", ctx -> handlePayment(ctx));

        app.get("/order/confirmation", ctx -> showConfirmationPage(ctx));
    }

    private void showDeliveryPage(Context ctx)
    {
        ShoppingCart cart = ctx.sessionAttribute("CART");

        if (cart == null || cart.getShoppingCart().isEmpty()) {
            ctx.redirect("/cart");
            return;
        }

        ctx.sessionAttribute("cart", cart);
        ctx.render("checkout-delivery");
    }

    private void saveDeliveryInfo(Context ctx)
    {
        String deliveryMethod = ctx.formParam("deliveryMethod");
        String pickupDate = ctx.formParam("pickupDate");
        String pickupTime = ctx.formParam("pickupTime");
        DeliveryStrategy deliveryStrategy = null;

        if(deliveryMethod.equals("delivery"))
        {
            deliveryStrategy = new StandardDelivery();
        }
        else
        {
            deliveryStrategy = new PickupDelivery();
        }

        try
        {
            LocalDate date = LocalDate.parse(pickupDate);
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            LocalTime time = LocalTime.parse(pickupTime);

            if (!validateDateAndTime(ctx, date, time, dayOfWeek, pickupTime))
            {
                return;
            }

            LocalDateTime pickupDateTime = LocalDateTime.of(date, time);

            ctx.sessionAttribute("deliveryPrice",deliveryStrategy.getDeliveryPrice());
            ctx.sessionAttribute("pickUp", pickupDateTime);
            ctx.sessionAttribute("deliveryMethod", deliveryMethod);
            ctx.redirect("/checkout/contact-info");

        } catch (Exception e) {
            ctx.attribute("errorMessage", "Der skete en fejl med dato/tid valg");
            ctx.render("checkout-delivery");
        }
    }

    private void showContactInfoPage(Context ctx)
    {
        String deliveryMethod = ctx.sessionAttribute("deliveryMethod");
        if (deliveryMethod == null)
        {
            ctx.redirect("/checkout/delivery");
            return;
        }

        User currentUser = ctx.sessionAttribute("currentUser");
        ShoppingCart cart = ctx.sessionAttribute("cart");

        ctx.attribute("currentUser", currentUser);
        ctx.attribute("deliveryMethod", deliveryMethod);
        ctx.render("checkout-contact");
    }

    private void saveContactInfo(Context ctx)
    {
        User currentUser = ctx.sessionAttribute("currentUser");
        String deliveryMethod = ctx.sessionAttribute("deliveryMethod");

        String firstName = ctx.formParam("firstName");
        String lastName = ctx.formParam("lastName");
        String email = ctx.formParam("email");
        String phoneNumberStr = ctx.formParam("phoneNumber");

        String street = "";
        String city = "";
        String zipCodeStr = "";

        try
        {
            int phoneNumber = Integer.parseInt(phoneNumberStr);
            int zipCode = 0;

            if (!deliveryMethod.equals("delivery"))
            {
                street = "Olsker Hovedgade 12";
                city = "Allinge";
                zipCode = 3770;
            }
            else
            {
                street = ctx.formParam("street");
                city = ctx.formParam("city");
                zipCodeStr = ctx.formParam("zipCode");
                zipCode = Integer.parseInt(zipCodeStr);
            }

            userService.validateInput(firstName, lastName, street, zipCode, city, phoneNumber, email);

            UserDTO userDTO;
            if(currentUser == null)
            {

                User guestUser = userService.registerGuestUser(
                        firstName,
                        lastName,
                        email,
                        phoneNumber,
                        city,
                        street,
                        zipCode
                );
                userDTO = userService.getUserById(guestUser.getUserId());
            } else
            {
                userDTO = userService.getUserById(currentUser.getUserId());
            }

            ctx.sessionAttribute("checkoutUser", userDTO);
            ctx.redirect("/checkout/payment");

        } catch (NumberFormatException e)
        {
            ctx.attribute("errorMessage","Kun heltal i postnummer og telefonnummer");
            ctx.attribute("currentUser", currentUser);
            ctx.attribute("deliveryMethod", deliveryMethod);
            ctx.render("checkout-contact");
        } catch (DatabaseException | IllegalArgumentException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.attribute("currentUser", currentUser);
            ctx.attribute("deliveryMethod", deliveryMethod);
            ctx.render("checkout-contact");
        }
    }

    private void showPaymentPage(Context ctx)
    {
        UserDTO checkoutUser = ctx.sessionAttribute("checkoutUser");
        if (checkoutUser == null)
        {
            ctx.redirect("/checkout/contact-info");
            return;
        }

        ShoppingCart cart = ctx.sessionAttribute("cart");
        if (cart == null || cart.getShoppingCart().isEmpty())
        {
            ctx.redirect("/cart");
            return;
        }

        if (checkoutUser.getUserId() != 0)
        {
            double balanceAfterPurchase = checkoutUser.getBalance() - cart.getTotalOrderPrice();
            ctx.attribute("userBalanceAfterPurchase", balanceAfterPurchase);
        }
        ctx.render("checkout-payment");
    }

    private void handlePayment(Context ctx)
    {
        String paymentMethod = ctx.formParam("paymentMethod");
        boolean payNow = "pay-now".equals(paymentMethod);

        UserDTO checkoutUser = ctx.sessionAttribute("checkoutUser");
        ShoppingCart cart = ctx.sessionAttribute("cart");
        LocalDateTime pickupDateTime = ctx.sessionAttribute("pickUp");
        String deliveryMethod = ctx.sessionAttribute("deliveryMethod");
        double deliveryTotal = ctx.sessionAttribute("deliveryPrice");

        if(!validateOrderDetails(ctx, checkoutUser, cart, pickupDateTime, deliveryMethod))
        {
            return;
        }

        if (payNow)
        {
            double totalPrice = cart.getTotalOrderPrice();

            if (checkoutUser.getUserId() == 0)
            {
                ctx.attribute("errorMessage", "Gæster kan kun betale ved afhentning");
                ctx.attribute("userBalanceAfterPurchase", 0);
                ctx.render("checkout-payment");
                return;
            }

            if (checkoutUser.getBalance() < totalPrice)
            {
                ctx.attribute("errorMessage",
                        String.format("Du har ikke penge nok på din konto. Din saldo: %.2f kr. Pris: %.2f kr. Vælg 'betal ved afhentning' eller kontakt os for at indsætte penge.",
                                checkoutUser.getBalance(), totalPrice));
                ctx.attribute("userBalanceAfterPurchase", checkoutUser.getBalance() - totalPrice);
                ctx.render("checkout-payment");
                return;
            }
        }

        try
        {
            Order order = orderService.createOrder(
                    checkoutUser,
                    cart.getShoppingCart(),
                    pickupDateTime,
                    payNow
            );
            cart.clearShoppingCart();

            ctx.sessionAttribute("cart", cart);
            ctx.sessionAttribute("CART", new ShoppingCart());
            ctx.sessionAttribute("completedOrder", order);
            ctx.redirect("/order/confirmation");

        } catch (DatabaseException e)
        {
            ctx.attribute("errorMessage", e.getMessage());
            ctx.attribute("userBalanceAfterPurchase", checkoutUser.getBalance() - cart.getTotalOrderPrice());
            ctx.render("checkout-payment");
        }
    }

    private void showConfirmationPage(Context ctx)
    {
        ctx.render("checkout-confirmed");
    }

    private boolean validateDateAndTime(Context ctx, LocalDate date, LocalTime time, DayOfWeek dayOfWeek, String pickupTime)
    {
        if (dayOfWeek == DayOfWeek.MONDAY)
        {
            ctx.attribute("errorMessage", "Butikken er lukket om mandagen");
            ctx.render("checkout-delivery");
            return false;
        }

        if(date.isBefore(LocalDate.now()))
        {
            ctx.attribute("errorMessage", "Du kan ikke lægge ordre tilbage i tiden");
            ctx.render("checkout-delivery");
            return false;
        }

        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY)
        {
            if (time.isBefore(LocalTime.parse("10:00")) || time.isAfter(LocalTime.parse("17:30")))
            {
                ctx.attribute("errorMessage", "I weekenden er butikken åben 10:00-17:30. Du valgte " + pickupTime);
                ctx.render("checkout-delivery");
                return false;
            }
        }
        else
        {
            if (time.isBefore(LocalTime.parse("12:30")) || time.isAfter(LocalTime.parse("17:00")))
            {
                ctx.attribute("errorMessage", "På hverdage er butikken åben 12:30-17:00. Du valgte " + pickupTime);
                ctx.render("checkout-delivery");
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
            ctx.redirect("/checkout/delivery");
            return false;
        }

        if(deliveryMethod == null || deliveryMethod.isEmpty())
        {
            ctx.redirect("/checkout/delivery");
            return false;
        }
        return true;
    }
}