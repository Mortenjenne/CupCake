package app.entities;

import lombok.Getter;

public class Bottom
{
    @Getter
    private int bottomId;
    private String name;
    private double price;

    public Bottom (int bottomId, String name, double price)
    {
        this.bottomId = bottomId;
        this.name = name;
        this.price = price;
    }
}
