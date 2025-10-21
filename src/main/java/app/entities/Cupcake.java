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

private double getPrice()
{
return cupcakePrice;
}

}
