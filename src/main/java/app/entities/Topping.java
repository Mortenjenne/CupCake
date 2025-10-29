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

    public String getSlug() {
        if (name == null) return "";
        return name
                .toLowerCase()
                .replaceAll("å", "aa")
                .replaceAll("æ", "ae")
                .replaceAll("ø", "oe")
                .replaceAll("[\\s/]+", "")
                .replaceAll("[^a-z0-9]", "");
    }

}
