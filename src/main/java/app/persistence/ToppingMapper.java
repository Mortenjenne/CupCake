package app.persistence;

import app.entities.Topping;
import app.exceptions.DatabaseException;

import java.sql.*;
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
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                Topping t = new Topping(
                        rs.getInt("topping_id"),
                        rs.getString("topping_flavour"),
                        rs.getDouble("topping_price")
                );
                toppingList.add(t);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Database error while fetching toppings: " + e);
        }
        return toppingList;
    }

    public Topping getToppingById(int toppingId) throws DatabaseException
    {
        String sql = "SELECT * FROM toppings WHERE topping_id = ?";
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, toppingId);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return new Topping(rs.getInt("topping_id"),
                            rs.getString("topping_flavour"),
                            rs.getDouble("topping_price"));
                }
                return null;
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Database error while fetching toppings: " + e);
        }
    }

    public Topping createTopping(String toppingFlavour, double toppingPrice) throws DatabaseException
    {
        String sql = "INSERT INTO toppings (topping_flavour, topping_price)" +
                "VALUES(?,?)";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {

            ps.setString(1, toppingFlavour);
            ps.setDouble(2, toppingPrice);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected != 1)
            {
                throw new DatabaseException("Uventet fejl");
            }

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
            {
                int toppingId = rs.getInt(1);

                return new Topping(toppingId, toppingFlavour, toppingPrice);
            }

        }
        catch (SQLException e)
        {
            if (e.getSQLState().equals("23505")) // error code is the standard for catching unique constraint errors in PostgresSQL
            {
                throw new DatabaseException("Topping Smag findes allerede");
            }
            else
            {
                throw new DatabaseException("Databasefejl ved oprettelse af Topping smag " + e.getMessage());
            }
        }
        return null;
    }

    public boolean updateTopping(Topping topping) throws DatabaseException
    {
        String sql = "UPDATE toppings SET topping_flavour = ?, topping_price = ? WHERE topping_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, topping.getName());
            ps.setDouble(2, topping.getPrice());
            ps.setInt(3, topping.getToppingId());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1)
            {
                return true;
            }
            else
            {
                throw new DatabaseException("Topping blev ikke opdateret - id: " + topping.getToppingId());
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved opdatering af topping: " + e.getMessage());
        }
    }

    public boolean deleteTopping(int toppingId) throws DatabaseException
    {
        String sql = "DELETE FROM bottoms WHERE topping_id = ?";


        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, toppingId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1)
            {
                return true;
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved sletning af topping med id: " + toppingId);
        }
        return false;
    }
}
