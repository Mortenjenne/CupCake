package app.persistence;

import app.dto.UserDTO;
import app.entities.Order;
import app.entities.OrderLine;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderMapper
{
    private ConnectionPool connectionPool;
    private OrderLineMapper orderLineMapper;

    public OrderMapper(ConnectionPool connectionPool, OrderLineMapper orderLineMapper)
    {
        this.connectionPool = connectionPool;
        this.orderLineMapper = orderLineMapper;
    }

    public Order createOrder(Order order) throws DatabaseException
    {
        Connection connection = null;
        try
        {
            connection = connectionPool.getConnection();
            connection.setAutoCommit(false);

            int orderId = insertOrder(connection, order);
            orderLineMapper.insertOrderLines(connection, orderId, order.getOrderlines());

            connection.commit();

            order.setOrderId(orderId);
            return order;
        }
        catch (SQLException e)
        {
            if (connection != null)
            {
                try
                {
                    connection.rollback();
                }
                catch (SQLException rollbackException)
                {
                }
            }
            throw new DatabaseException("Kunne ikke oprette ordren i databasen: " + e.getMessage());
        }
        finally
        {
            if (connection != null)
            {
                try
                {
                    connection.setAutoCommit(true);
                    connection.close();
                }
                catch (SQLException e)
                {
                }
            }
        }
    }

    public Order getOrderByOrderId(int orderId, int userId) throws DatabaseException
    {
        String sql = "SELECT o.order_id, o.user_id, o.order_date, o.pickup_date, o.paid, o.price_total, " +
                "u.firstname, u.lastname, u.email, u.phonenumber, u.street, u.zip_code, u.balance, " +
                "u.admin, u.is_guest, z.city " +
                "FROM orders o " +
                "JOIN users u ON o.user_id = u.user_id " +
                "JOIN zip_codes z ON u.zip_code = z.zip_code " +
                "WHERE o.order_id = ? AND o.user_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, orderId);
            ps.setInt(2, userId);

            try (ResultSet rs = ps.executeQuery())
            {
                if (rs.next())
                {
                    UserDTO user = buildUserDTOFromResultSet(rs);
                    List<OrderLine> orderLines = orderLineMapper.getOrderLinesByOrderId(orderId);

                    Order order = new Order(
                            rs.getInt("order_id"),
                            user,
                            rs.getTimestamp("order_date").toLocalDateTime(),
                            rs.getTimestamp("pickup_date").toLocalDateTime(),
                            rs.getBoolean("paid"),
                            orderLines,
                            rs.getDouble("price_total")
                    );
                    return order;
                }
                else
                {
                    throw new DatabaseException("Ordre ikke fundet");
                }
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Kunne ikke hente ordre: " + e.getMessage());
        }
    }

    public List<Order> getOrdersByUserId(UserDTO userDTO) throws DatabaseException
    {
        List<Order> orders = new ArrayList<>();

        String sql = "SELECT o.order_id, o.user_id, o.order_date, o.pickup_date, o.paid, o.price_total, " +
                "u.firstname, u.lastname, u.email, u.phonenumber, u.street, u.zip_code, u.balance, " +
                "u.admin, u.is_guest, z.city " +
                "FROM orders o " +
                "JOIN users u ON o.user_id = u.user_id " +
                "JOIN zip_codes z ON u.zip_code = z.zip_code " +
                "WHERE o.user_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, userDTO.getUserId());

            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    int orderId = rs.getInt("order_id");
                    List<OrderLine> orderLines = orderLineMapper.getOrderLinesByOrderId(orderId);

                    Order order = new Order(
                            orderId,
                            userDTO,
                            rs.getTimestamp("order_date").toLocalDateTime(),
                            rs.getTimestamp("pickup_date").toLocalDateTime(),
                            rs.getBoolean("paid"),
                            orderLines,
                            rs.getDouble("price_total")
                    );
                    orders.add(order);
                }
            }
            return orders;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Kunne ikke hente ordrer: " + e.getMessage());
        }
    }

    public List<Order> getAllOrders() throws DatabaseException
    {
        List<Order> orders = new ArrayList<>();

        String sql = "SELECT o.order_id, o.user_id, o.order_date, o.pickup_date, o.paid, o.price_total, " +
                "u.firstname, u.lastname, u.email, u.phonenumber, u.street, u.zip_code, u.balance, " +
                "u.admin, u.is_guest, z.city " +
                "FROM orders o " +
                "JOIN users u ON o.user_id = u.user_id " +
                "JOIN zip_codes z ON u.zip_code = z.zip_code";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            try (ResultSet rs = ps.executeQuery())
            {
                while (rs.next())
                {
                    int orderId = rs.getInt("order_id");
                    UserDTO userDTO = buildUserDTOFromResultSet(rs);
                    List<OrderLine> orderLines = orderLineMapper.getOrderLinesByOrderId(orderId);

                    Order order = new Order(
                            orderId,
                            userDTO,
                            rs.getTimestamp("order_date").toLocalDateTime(),
                            rs.getTimestamp("pickup_date").toLocalDateTime(),
                            rs.getBoolean("paid"),
                            orderLines,
                            rs.getDouble("price_total")
                    );
                    orders.add(order);
                }
            }
            return orders;
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Kunne ikke hente ordrer: " + e.getMessage());
        }
    }

    public boolean deleteOrder(int orderId) throws DatabaseException
    {
        boolean result = false;
        String sql = "DELETE FROM orders WHERE order_id = ?";

        try(Connection connection = connectionPool.getConnection();
        PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, orderId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1)
            {
                result = true;
            }
        } catch (SQLException e)
        {
            throw new DatabaseException("Kunne ikke slette en ordre");
        }
        return result;
    }

    public boolean updateOrderStatus(int orderId, boolean paid) throws DatabaseException
    {
        String sql = "UPDATE orders SET paid = ? WHERE order_id = ?";
        boolean result = false;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setBoolean(1, paid);
            ps.setInt(2, orderId);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1)
            {
                result = true;
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Kunne ikke opdatere ordre status" + e.getMessage());
        }
        return result;
    }

    private int insertOrder(Connection connection, Order order) throws SQLException
    {
        String sql = "INSERT INTO orders (user_id, order_date, pickup_date, paid, price_total) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setInt(1, order.getUserDTO().getUserId());
            ps.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
            ps.setTimestamp(3, Timestamp.valueOf(order.getPickUpDate()));
            ps.setBoolean(4, order.isPaid());
            ps.setDouble(5, order.getTotalPrice());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
            {
                return rs.getInt(1);
            }
            throw new SQLException("Ingen ordre id oprettet");
        }
    }

    private UserDTO buildUserDTOFromResultSet(ResultSet rs) throws SQLException
    {
        return new UserDTO(
                rs.getInt("user_id"),
                rs.getString("firstname"),
                rs.getString("lastname"),
                rs.getString("email"),
                rs.getInt("phonenumber"),
                rs.getString("street"),
                rs.getInt("zip_code"),
                rs.getString("city"),
                rs.getDouble("balance")
        );
    }
}
