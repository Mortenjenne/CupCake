package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class ShoppingCart
{
private List<OrderLine> orderLines;

public void addToCart(Cupcake cupcake, int quantity)
{
    if(cupcake != null && quantity > 0)
    {
        this.orderLines.add(new OrderLine(0, cupcake, quantity));
    }
}
public void removeFromCart(Cupcake cupcake, int quantity)
{

}
public double getTotalPrice()
{
    return 0;
}

}
