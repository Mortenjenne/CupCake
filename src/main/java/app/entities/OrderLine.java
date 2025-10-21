package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderLine
{
private int orderLineId;
private Cupcake cupcake;
private int quantity;
}
