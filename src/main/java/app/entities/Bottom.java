package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Bottom
{
    private int bottomId;
    private String name;
    private double price;

    public String getSlug() {
        if (name == null) return "";
        return name
                .toLowerCase()
                .replaceAll("[\\s/]+", "")
                .replaceAll("[^a-z0-9]", "");
    }
}
