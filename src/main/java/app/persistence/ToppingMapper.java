package app.persistence;

import app.entities.Topping;
import java.util.ArrayList;
import java.util.List;

public class ToppingMapper
{
    private ConnectionPool connectionPool;

    public ToppingMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public List<Topping> getAllToppings()
    {
        List<Topping> toppingList = new ArrayList<>();

        return toppingList;
    }

    public Topping getToppingById(int bottomId)
    {
        return null;
    }

    public Topping createTopping(Topping topping)
    {
        return null;
    }

    public boolean updateTopping(Topping topping)
    {
        return false;
    }

    public boolean delete(int bottomId)
    {
        return false;
    }

}