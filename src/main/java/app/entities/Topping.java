package app.entities;

import lombok.Getter;

public class Topping
{
    @Getter
    private int toppingId;
    private String name;
    private double price;

    public Topping (int toppingId, String name, double price)
    {
        this.toppingId = toppingId;
        this.name = name;
        this.price = price;
    }

}
