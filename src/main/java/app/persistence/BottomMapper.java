package app.persistence;

import app.entities.Bottom;
import app.exceptions.DatabaseException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        try(Connection connection = connectionPool.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery())
        {
            while(rs.next())
            {
                Bottom b = new Bottom(
                        rs.getInt("bottom_id"),
                        rs.getString("flavour"),
                        rs.getDouble("price")
                );
                bottomList.add(b);
            }
        } catch (SQLException e)
        {
            throw new DatabaseException("Database error while fetching toppings: " + e);
        }
        return bottomList;
    }

    public Bottom getBottomById(int bottomId) throws DatabaseException
    {
        String sql = "SELECT * FROM toppings WHERE bottom_id = ?";
        try(Connection connection = connectionPool.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, bottomId);
            try(ResultSet rs = ps.executeQuery())
            {
                if(rs.next())
                {
                    return new Bottom(rs.getInt("bottom_id"),
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

    public Bottom createBottom(Bottom bottom)
    {
        return null;
    }

    public boolean updateBottom(Bottom bottom)
    {
        return false;
    }

    public boolean delete(int bottomId)
    {
        return false;
    }
}
