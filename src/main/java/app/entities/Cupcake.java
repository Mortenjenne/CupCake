package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Cupcake
{
    private Bottom bottom;
    private Topping topping;
    private double cupcakePrice;

    public Cupcake(Bottom bottom, Topping topping)
    {
        this.bottom = bottom;
        this.topping = topping;
        this.cupcakePrice = bottom.getPrice() + topping.getPrice();
    }
}



