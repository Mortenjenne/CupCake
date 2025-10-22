package app.services;

import app.entities.Bottom;
import app.entities.Cupcake;
import app.entities.ShoppingCart;
import app.entities.Topping;
import app.exceptions.DatabaseException;
import app.persistence.BottomMapper;
import app.persistence.ToppingMapper;

import java.util.List;

public class ShoppingServiceImpl implements ShoppingService
{
    private ToppingMapper toppingMapper;
    private BottomMapper bottomMapper;

    public ShoppingServiceImpl(BottomMapper bottomMapper, ToppingMapper toppingMapper)
    {
        this.bottomMapper = bottomMapper;
        this.toppingMapper = toppingMapper;
    }

    @Override
    public List<Bottom> getAllBottoms() throws DatabaseException
    {
        return bottomMapper.getAllBottoms();
    }

    @Override
    public List<Topping> getAllToppings() throws DatabaseException
    {
        return toppingMapper.getAllToppings();
    }

    @Override
    public Bottom getBottomById(int id) throws DatabaseException
    {
        return bottomMapper.getBottomById(id);
    }

    @Override
    public Topping getToppingById(int id) throws DatabaseException
    {
        return toppingMapper.getToppingById(id);
    }

    @Override
    public void addOrderLineToCart(ShoppingCart cart, Bottom bottom, Topping topping, int qty)
    {
        double price = bottom.getPrice() + topping.getPrice();
        Cupcake cupcake = new Cupcake(bottom, topping, price);
        cart.addOrderLineToCart(cupcake, qty);
    }

    @Override
    public void removeOrderLineFromCart(ShoppingCart cart, int index)
    {
        cart.removeOrderLineFromCart(cart.getShoppingCart().get(index));
    }

    @Override
    public void removeOneFromCupcakeQuantity(ShoppingCart cart, int index)
    {
        cart.removeOneFromCupcakeQuantity(cart.getShoppingCart().get(index));
    }

    @Override
    public void addOneToCupcakeQuantity(ShoppingCart cart, int index)
    {
        cart.addOneToCupcakeQuantity(cart.getShoppingCart().get(index));
    }

    @Override
    public double getTotalOrderPrice(ShoppingCart cart)
    {
        return cart.getTotalOrderPrice();
    }

    @Override
    public void clearCart(ShoppingCart cart)
    {
        cart.clearShoppingCart();
    }
}
