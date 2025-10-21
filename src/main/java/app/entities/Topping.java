package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Topping
{
    private int toppingId;
    private String name;
    private double price;

}
