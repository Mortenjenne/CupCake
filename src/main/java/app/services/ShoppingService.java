package app.services;

import app.entities.Bottom;
import app.entities.ShoppingCart;
import app.entities.Topping;
import app.exceptions.DatabaseException;

import java.util.List;

public interface ShoppingService
{
    List<Bottom> getAllBottoms() throws DatabaseException;

    List<Topping> getAllToppings() throws DatabaseException;

    Bottom getBottomById(int id) throws DatabaseException;

    Topping getToppingById(int id) throws DatabaseException;

    void addOrderLineToCart(ShoppingCart cart, Bottom bottom, Topping topping, int qty);

    void removeOrderLineFromCart(ShoppingCart cart, int index);

    void removeOneFromCupcakeQuantity(ShoppingCart cart, int index);

    void addOneToCupcakeQuantity(ShoppingCart cart, int index);

    double getTotalOrderPrice(ShoppingCart cart);

    void clearCart(ShoppingCart cart);
}
