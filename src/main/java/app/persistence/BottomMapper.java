package app.persistence;

import app.entities.Bottom;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BottomMapper
{
    private final ConnectionPool connectionPool;

    public BottomMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public List<Bottom> getAllBottoms() throws DatabaseException
    {
        List<Bottom> bottomList = new ArrayList<>();
        String sql = "SELECT * FROM bottoms";
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next())
            {
                Bottom b = new Bottom(
                        rs.getInt("bottom_id"),
                        rs.getString("bottom_flavour"),
                        rs.getDouble("bottom_price")
                );
                bottomList.add(b);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Database error while fetching toppings: " + e);
        }
        return bottomList;
    }

    public Bottom getBottomById(int bottomId) throws DatabaseException
    {
        String sql = "SELECT * FROM bottoms WHERE bottom_id = ?";
        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, bottomId);
            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    return new Bottom(
                            rs.getInt("bottom_id"),
                            rs.getString("bottom_flavour"),
                            rs.getDouble("bottom_price"));
                }
                return null;
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Database error while fetching toppings: " + e);
        }
    }

    public Bottom createBottom(String bottomFlavour, double bottomPrice) throws DatabaseException
    {
        String sql = "INSERT INTO bottoms (bottom_flavour, bottom_price)" +
                "VALUES(?,?)";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {

            ps.setString(1, bottomFlavour);
            ps.setDouble(2, bottomPrice);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected != 1)
            {
                throw new DatabaseException("Uventet fejl");
            }

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
            {
                int bottomId = rs.getInt(1);

                return new Bottom(bottomId, bottomFlavour, bottomPrice);
            }

        }
        catch (SQLException e)
        {
            if (e.getSQLState().equals("23505")) // error code is the standard for catching unique constraint errors in PostgresSQL
            {
                throw new DatabaseException("Bund Smag findes allerede");
            }
            else
            {
                throw new DatabaseException("Databasefejl ved oprettelse af Bund smag " + e.getMessage());
            }
        }
        return null;
    }

    public boolean updateBottom(Bottom bottom)
    {
        return false;
    }

    public boolean deleteBottom(int bottomId) throws DatabaseException
    {
        String sql = "DELETE FROM bottoms WHERE bottom_id = ?";


        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, bottomId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1)
            {
                return true;
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved sletning af bottom med id: " + bottomId);
        }
        return false;
    }
}
