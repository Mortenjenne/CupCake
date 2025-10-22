package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ShoppingCart
{
    private List<OrderLine> shoppingCart = new ArrayList<>();

    public ShoppingCart()
    {
    }

    public void addOrderLineToCart(Cupcake cupcake, int quantity)
    {
        if (cupcake == null || quantity < 0) {
            throw new IllegalArgumentException("Cupcake eller mængde kan ikke være tom eller 0");
        }
        boolean newQuantity = false;
        for (OrderLine o : shoppingCart) {
            if (o.getCupcake().equals(cupcake)) {
                o.setQuantity(o.getQuantity() + quantity);
                newQuantity = true;
            }
        }
        if (!newQuantity) {
            this.shoppingCart.add(new OrderLine(cupcake, quantity));
        }
    }

    public void removeOrderLineFromCart(OrderLine orderLine)
    {
        shoppingCart.remove(orderLine);
    }

    public void removeOneFromCupcakeQuantity(OrderLine orderLine)
    {
        if (orderLine.getQuantity() == 1) {
            removeOrderLineFromCart(orderLine);
        }
        orderLine.setQuantity((orderLine.getQuantity() - 1));
    }

    public void addOneToCupcakeQuantity(OrderLine orderLine)
    {
        orderLine.setQuantity((orderLine.getQuantity() + 1));
    }

    public double getTotalOrderPrice()
    {
        return this.shoppingCart.stream()
                .mapToDouble(orderLine -> (orderLine.getQuantity() * orderLine.getCupcake().getCupcakePrice()))
                .sum();
    }

    public void clearShoppingCart()
    {
        this.shoppingCart.clear();
    }

}
