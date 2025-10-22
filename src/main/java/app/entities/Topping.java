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
                .replaceAll("[\\s/]+", "")   // fjern mellemrum og /
                .replaceAll("[^a-z0-9]", ""); // fjern alt andet sjovt
    }

}
