package app.persistence;

import app.entities.Order;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class OrderMapper
{
    private ConnectionPool connectionPool;

    public OrderMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public Order createOrder(LocalDateTime orderDate, LocalDateTime pickUpDate, boolean paid, double totalPrice) throws DatabaseException
    {
        Order order = null;
        String sql = "INSERT INTO orders (order_date, pickup_date, paid, price_total) VALUES (?, ?, ?, ?)";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setTimestamp(1, Timestamp.valueOf(orderDate));
            ps.setTimestamp(2, Timestamp.valueOf(pickUpDate));
            ps.setBoolean(3, paid);
            ps.setDouble(4, totalPrice);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1)
            {
                try (ResultSet rs = ps.getGeneratedKeys())
                {
                    if (rs.next())
                    {
                        int orderId = rs.getInt(1);
                        order = new Order(orderId, null, orderDate, pickUpDate, paid, null, totalPrice);
                    }
                }
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Kunne ikke oprette ordren i databasen: " + e.getMessage());
        }

        return order;
    }

    public Order getOrderByOrderId(int orderId)
    {
        Order order = null;
        return order;
    }

    public List<Order> getOrderByUserId(int userId)
    {
        List<Order> orders = null;
        return orders;
    }

    public List<Order> getAllOrders()
    {
        List<Order> orders = null;
        return orders;
    }

    public boolean deleteOrder(int orderId)
    {
        boolean result = false;
        return result;
    }


}
