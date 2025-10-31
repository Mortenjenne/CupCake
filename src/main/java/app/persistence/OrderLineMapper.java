package app.persistence;

import app.entities.Bottom;
import app.entities.Cupcake;
import app.entities.OrderLine;
import app.entities.Topping;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderLineMapper
{
    private ConnectionPool connectionPool;

    public OrderLineMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public void insertOrderLines(Connection connection, int orderId, List<OrderLine> orderLines) throws SQLException
    {
        String sql = "INSERT INTO orderlines (order_id, topping_id, bottom_id, quantity, orderline_price) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql))
        {
            for (OrderLine line : orderLines)
            {
                ps.setInt(1, orderId);
                ps.setInt(2, line.getCupcake().getTopping().getToppingId());
                ps.setInt(3, line.getCupcake().getBottom().getBottomId());
                ps.setInt(4, line.getQuantity());
                ps.setDouble(5, line.getOrderLinePrice());
                ps.executeUpdate();
            }
        }
    }

    public List<OrderLine> getOrderLinesByOrderId(int orderId) throws DatabaseException
    {
        List<OrderLine> orderLines = new ArrayList<>();
        String sql = "SELECT ol.orderline_id, ol.topping_id, ol.bottom_id, ol.quantity, ol.orderline_price, t.topping_flavour, t.topping_price, b.bottom_flavour, b.bottom_price FROM orderlines ol JOIN toppings t ON t.topping_id = ol.topping_id JOIN bottoms b ON b.bottom_id = ol.bottom_id WHERE ol.order_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, orderId);

            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                Topping topping = new Topping(
                        rs.getInt("topping_id"),
                        rs.getString("topping_flavour"),
                        rs.getDouble("topping_price")
                );

                Bottom bottom = new Bottom(
                        rs.getInt("bottom_id"),
                        rs.getString("bottom_flavour"),
                        rs.getDouble("bottom_price")
                );

                Cupcake cupcake = new Cupcake(bottom, topping);
                int orderLineId = rs.getInt("orderline_id");
                int quantity = rs.getInt("quantity");
                double orderLinePrice = rs.getDouble("orderline_price");

                orderLines.add(
                        new OrderLine(
                                orderLineId,
                                cupcake,
                                quantity,
                                orderLinePrice)
                );
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af ordrelinjer" + e.getMessage());
        }
        return orderLines;
    }

    public boolean deleteOrderline(int orderlineId) throws DatabaseException
    {
        String sql = "DELETE FROM orderlines WHERE orderline_id = ?";
        boolean result = false;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, orderlineId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1)
            {
                result = true;
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved sletning af ordrelinje med id: " + orderlineId);
        }
        return result;
    }
}
