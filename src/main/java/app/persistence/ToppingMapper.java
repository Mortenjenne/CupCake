package app.persistence;

import app.entities.Topping;
import app.exceptions.DatabaseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ToppingMapper
{
    private final ConnectionPool connectionPool;

    public ToppingMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public List<Topping> getAllToppings() throws DatabaseException
    {
        List<Topping> toppingList = new ArrayList<>();
        String sql = "SELECT * FROM toppings";
        try(Connection connection = connectionPool.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery())
        {
            while(rs.next())
            {
                Topping t = new Topping(
                rs.getInt("topping_id"),
                rs.getString("flavour"),
                rs.getDouble("price")
                );
                toppingList.add(t);
            }
        } catch (SQLException e)
        {
            throw new DatabaseException("Database error while fetching toppings: " + e);
        }
        return toppingList;
    }

    public Topping getToppingById(int bottomId) throws DatabaseException
    {
        String sql = "SELECT * FROM toppings WHERE topping_id = ?";
        try(Connection connection = connectionPool.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, bottomId);
            try(ResultSet rs = ps.executeQuery())
            {
                if(rs.next())
                {
                    return new Topping(rs.getInt("topping_id"),
                            rs.getString("flavour"),
                            rs.getDouble("price"));
                }
                return null;
            }
        } catch (SQLException e)
        {
            throw new DatabaseException("Database error while fetching toppings: " + e);
        }
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