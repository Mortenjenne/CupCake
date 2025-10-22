package app.persistence;

import app.entities.OrderLine;
import app.exceptions.DatabaseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class OrderLineMapper
{
    private ConnectionPool connectionPool;

    public OrderLineMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public void insertOrderLines(Connection connection, int orderId, List<OrderLine> orderLines) throws SQLException
    {
        String sql = "INSERT INTO orderlines (order_id, topping_id, bottom_id, quantity) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (OrderLine line : orderLines) {
                ps.setInt(1, orderId);
                ps.setInt(2, line.getCupcake().getTopping().getToppingId());
                ps.setInt(3, line.getCupcake().getBottom().getBottomId());
                ps.setInt(4, line.getQuantity());
                ps.executeUpdate();
            }
        }
    }

    public List<OrderLine> getOrderLinesByOrderId(int orderId) throws DatabaseException
    {
        return null;
    }
}
