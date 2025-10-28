package app.persistence;

import app.dto.UserDTO;
import app.entities.*;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest
{
    private final static String USER = "postgres";
    private final static String PASSWORD = "postgres";
    private final static String URL = "jdbc:postgresql://localhost:5432/cupcake?currentSchema=test";

    private static ConnectionPool connectionPool;
    private static OrderMapper orderMapper;
    private static OrderLineMapper orderLineMapper;

    Bottom bottomChocolate = new Bottom(1, "Chocolate", 5.0);
    Topping toppingChocolate = new Topping(1, "Chocolate", 5.0);
    Bottom bottomPistacio = new Bottom(4, "Pistacio", 6.0);
    Topping toppingStrawberry = new Topping(4, "Strawberry", 6.0);

    Cupcake cupcakeChocolate = new Cupcake(bottomChocolate, toppingChocolate);
    Cupcake cupcakePistacioStrawberry = new Cupcake(bottomPistacio, toppingStrawberry);

    @BeforeAll
    public static void setUpClass()
    {
        try
        {
            connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, "cupcake");
            orderLineMapper = new OrderLineMapper(connectionPool);
            orderMapper = new OrderMapper(connectionPool, orderLineMapper);

            try (Connection testConnection = connectionPool.getConnection())
            {
                try (Statement stmt = testConnection.createStatement())
                {
                    // DROP tables in correct order
                    stmt.execute("DROP TABLE IF EXISTS test.orderlines CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.orders CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.users CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.zip_codes CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.bottoms CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.toppings CASCADE");

                    // CREATE tables with EXPLICIT structure
                    stmt.execute("""
                        CREATE TABLE test.zip_codes (
                            zip_code integer PRIMARY KEY,
                            city varchar NOT NULL
                        )
                    """);

                    stmt.execute("""
                        CREATE TABLE test.users (
                            user_id serial PRIMARY KEY,
                            firstname varchar NOT NULL,
                            lastname varchar NOT NULL,
                            email varchar NOT NULL UNIQUE,
                            password varchar,
                            phonenumber integer NOT NULL,
                            street varchar,
                            zip_code integer,
                            balance double precision NOT NULL DEFAULT 0,
                            admin boolean NOT NULL DEFAULT false,
                            is_guest boolean NOT NULL DEFAULT false,
                            created_at timestamp with time zone NOT NULL DEFAULT now(),
                            FOREIGN KEY (zip_code) REFERENCES test.zip_codes(zip_code)
                                ON DELETE NO ACTION ON UPDATE CASCADE
                        )
                    """);

                    stmt.execute("""
                        CREATE TABLE test.orders (
                            order_id serial PRIMARY KEY,
                            user_id integer NOT NULL,
                            order_date timestamp with time zone NOT NULL DEFAULT now(),
                            pickup_date timestamp with time zone NOT NULL,
                            paid boolean NOT NULL DEFAULT false,
                            price_total double precision NOT NULL,
                            FOREIGN KEY (user_id) REFERENCES test.users(user_id)
                                ON DELETE CASCADE ON UPDATE CASCADE
                        )
                    """);

                    stmt.execute("""
                        CREATE TABLE test.bottoms (
                            bottom_id serial PRIMARY KEY,
                            bottom_flavour varchar NOT NULL UNIQUE,
                            bottom_price double precision NOT NULL
                        )
                    """);

                    stmt.execute("""
                        CREATE TABLE test.toppings (
                            topping_id serial PRIMARY KEY,
                            topping_flavour varchar NOT NULL UNIQUE,
                            topping_price double precision NOT NULL
                        )
                    """);

                    stmt.execute("""
                        CREATE TABLE test.orderlines (
                            orderline_id serial PRIMARY KEY,
                            order_id integer NOT NULL,
                            topping_id integer NOT NULL,
                            bottom_id integer NOT NULL,
                            quantity integer NOT NULL,
                            orderline_price double precision NOT NULL,
                            FOREIGN KEY (order_id) REFERENCES test.orders(order_id)
                                ON DELETE CASCADE ON UPDATE CASCADE,
                            FOREIGN KEY (topping_id) REFERENCES test.toppings(topping_id)
                                ON DELETE NO ACTION ON UPDATE CASCADE,
                            FOREIGN KEY (bottom_id) REFERENCES test.bottoms(bottom_id)
                                ON DELETE NO ACTION ON UPDATE CASCADE
                        )
                    """);
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
                fail("Database connection failed: " + e.getMessage());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            fail("Failed to set up test database");
        }
    }

    @BeforeEach
    void setUp()
    {
        try (Connection testConnection = connectionPool.getConnection())
        {
            try (Statement stmt = testConnection.createStatement())
            {
                // Clear all data in correct order
                stmt.execute("DELETE FROM test.orderlines");
                stmt.execute("DELETE FROM test.orders");
                stmt.execute("DELETE FROM test.users");
                stmt.execute("DELETE FROM test.zip_codes");
                stmt.execute("DELETE FROM test.bottoms");
                stmt.execute("DELETE FROM test.toppings");

                // Reset sequences
                stmt.execute("SELECT setval('test.users_user_id_seq', 1)");
                stmt.execute("SELECT setval('test.orders_order_id_seq', 1)");
                stmt.execute("SELECT setval('test.orderlines_orderline_id_seq', 1)");
                stmt.execute("SELECT setval('test.bottoms_bottom_id_seq', 1)");
                stmt.execute("SELECT setval('test.toppings_topping_id_seq', 1)");

                // Insert test data
                stmt.execute("INSERT INTO test.zip_codes (zip_code, city) VALUES " +
                        "(1000, 'Copenhagen'), " +
                        "(2000, 'Frederiksberg'), " +
                        "(2100, 'København Ø'), " +
                        "(8000, 'Aarhus C')");

                stmt.execute("INSERT INTO test.users (user_id, firstname, lastname, email, password, phonenumber, street, zip_code, balance, admin, is_guest) VALUES " +
                        "(1, 'Hans', 'Hansen', 'hans@test.dk', 'password123', 12345678, 'Testvej 1', 2000, 100.0, false, false), " +
                        "(2, 'Jens', 'Jensen', 'jens@test.dk', 'password456', 87654321, 'Prøvevej 2', 2100, 200.0, false, false), " +
                        "(3, 'Admin', 'Adminson', 'admin@test.dk', 'admin123', 11111111, 'Adminvej 3', 8000, 0.0, true, false)");

                stmt.execute("INSERT INTO test.bottoms (bottom_id, bottom_flavour, bottom_price) VALUES " +
                        "(1, 'Chocolate', 5.00), " +
                        "(2, 'Vanilla', 5.00), " +
                        "(3, 'Nutmeg', 5.00), " +
                        "(4, 'Pistacio', 6.00)");

                stmt.execute("INSERT INTO test.toppings (topping_id, topping_flavour, topping_price) VALUES " +
                        "(1, 'Chocolate', 5.00), " +
                        "(2, 'Blueberry', 5.00), " +
                        "(3, 'Raspberry', 5.00), " +
                        "(4, 'Strawberry', 6.00)");

                // Sync sequences after manual inserts
                stmt.execute("SELECT setval('test.users_user_id_seq', COALESCE((SELECT MAX(user_id)+1 FROM test.users), 1), false)");
                stmt.execute("SELECT setval('test.bottoms_bottom_id_seq', COALESCE((SELECT MAX(bottom_id)+1 FROM test.bottoms), 1), false)");
                stmt.execute("SELECT setval('test.toppings_topping_id_seq', COALESCE((SELECT MAX(topping_id)+1 FROM test.toppings), 1), false)");
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            fail("Database setup failed: " + e.getMessage());
        }
    }

    @Test
    void testConnection() throws SQLException
    {
        assertNotNull(connectionPool.getConnection());
    }

    @Test
    void testCreateOrder() throws DatabaseException
    {
        UserDTO user = new UserDTO(1, "Hans", "Hansen", "hans@test.dk", 12345678, "Testvej 1", 2000, "Frederiksberg", 100.0);

        List<OrderLine> orderLines = new ArrayList<>();
        orderLines.add(new OrderLine(cupcakeChocolate, 2));

        LocalDateTime orderDate = LocalDateTime.now();
        LocalDateTime pickupDate = LocalDateTime.now().plusDays(2);

        Order order = new Order(0, user, orderDate, pickupDate, false, orderLines, 20.0);

        Order createdOrder = orderMapper.createOrder(order);

        assertNotNull(createdOrder);
        assertTrue(createdOrder.getOrderId() > 0);
        assertEquals(20.0, createdOrder.getTotalPrice());
        assertFalse(createdOrder.isPaid());
        assertEquals(1, createdOrder.getOrderlines().size());
    }

    @Test
    void testCreateOrderWithMultipleOrderLines() throws DatabaseException
    {
        UserDTO user = new UserDTO(2, "Jens", "Jensen", "jens@test.dk", 87654321, "Prøvevej 2", 2100, "København Ø", 200.0);

        List<OrderLine> orderLines = new ArrayList<>();
        orderLines.add(new OrderLine(cupcakeChocolate, 2));
        orderLines.add(new OrderLine(cupcakePistacioStrawberry, 4));

        Order order = new Order(0, user, LocalDateTime.now(), LocalDateTime.now().plusDays(1), false, orderLines, 68.0);

        Order createdOrder = orderMapper.createOrder(order);

        assertNotNull(createdOrder);
        assertEquals(68.0, createdOrder.getTotalPrice());
        assertEquals(2, createdOrder.getOrderlines().size());
    }

    @Test
    void testGetOrderByOrderId() throws DatabaseException
    {
        UserDTO user = new UserDTO(1, "Hans", "Hansen", "hans@test.dk", 12345678, "Testvej 1", 2000, "Frederiksberg", 100.0);
        List<OrderLine> orderLines = new ArrayList<>();
        orderLines.add(new OrderLine(cupcakeChocolate, 2));
        orderLines.add(new OrderLine(cupcakePistacioStrawberry, 3));

        Order order = new Order(0, user, LocalDateTime.now(), LocalDateTime.now().plusDays(2), true, orderLines, 56.00);
        Order createdOrder = orderMapper.createOrder(order);

        Order retrievedOrder = orderMapper.getOrderByOrderId(createdOrder.getOrderId(), 1);

        assertNotNull(retrievedOrder);
        assertEquals(createdOrder.getOrderId(), retrievedOrder.getOrderId());
        assertEquals(56.0, retrievedOrder.getTotalPrice());
        assertTrue(retrievedOrder.isPaid());
        assertEquals("Hans", retrievedOrder.getUserDTO().getFirstName());
        assertEquals(2, retrievedOrder.getOrderlines().size());
    }

    @Test
    void testGetOrderByOrderIdNotFound()
    {
        assertThrows(DatabaseException.class,
                () -> orderMapper.getOrderByOrderId(999, 1));
    }

    @Test
    void testGetOrderByOrderIdWrongUser() throws DatabaseException
    {
        UserDTO user = new UserDTO(1, "Hans", "Hansen", "hans@test.dk", 12345678, "Testvej 1", 2000, "Frederiksberg", 100.0);
        List<OrderLine> orderLines = new ArrayList<>();
        orderLines.add(new OrderLine(cupcakeChocolate, 2));
        orderLines.add(new OrderLine(cupcakePistacioStrawberry, 3));

        Order order = new Order(0, user, LocalDateTime.now(), LocalDateTime.now().plusDays(2), false, orderLines, 54.00);
        Order createdOrder = orderMapper.createOrder(order);

        assertThrows(DatabaseException.class,
                () -> orderMapper.getOrderByOrderId(createdOrder.getOrderId(), 2));
    }

    @Test
    void testGetOrdersByUserId() throws DatabaseException
    {
        UserDTO user = new UserDTO(1, "Hans", "Hansen", "hans@test.dk", 12345678, "Testvej 1", 2000, "Frederiksberg", 100.0);

        List<OrderLine> orderLines1 = new ArrayList<>();
        List<OrderLine> orderLines2 = new ArrayList<>();
        orderLines1.add(new OrderLine(cupcakeChocolate, 2));
        orderLines1.add(new OrderLine(cupcakePistacioStrawberry, 3));
        orderLines2.add(new OrderLine(cupcakeChocolate, 1));
        orderLines2.add(new OrderLine(cupcakePistacioStrawberry, 1));

        Order order1 = new Order(0, user, LocalDateTime.now(), LocalDateTime.now().plusDays(1), false, orderLines1, 56.0);
        Order order2 = new Order(1, user, LocalDateTime.now(), LocalDateTime.now().plusDays(1), false, orderLines2, 22.0);

        orderMapper.createOrder(order1);
        orderMapper.createOrder(order2);

        List<Order> orders = orderMapper.getOrdersByUserId(user);

        assertNotNull(orders);
        assertEquals(2, orders.size());
        assertEquals("Hans", orders.get(0).getUserDTO().getFirstName());
    }

    @Test
    void testGetOrdersByUserIdNoOrders() throws DatabaseException
    {
        UserDTO user = new UserDTO(3, "Admin", "Adminson", "admin@test.dk", 11111111, "Adminvej 3", 8000, "Aarhus C", 0.0);

        List<Order> orders = orderMapper.getOrdersByUserId(user);

        assertNotNull(orders);
        assertEquals(0, orders.size());
    }

    @Test
    void testGetAllOrders() throws DatabaseException
    {
        UserDTO user = new UserDTO(1, "Hans", "Hansen", "hans@test.dk", 12345678, "Testvej 1", 2000, "Frederiksberg", 100.0);
        UserDTO user2 = new UserDTO(2, "Jens", "Jensen", "jens@test.dk", 87654321, "Prøvevej 2", 2100, "København Ø", 200.0);

        List<OrderLine> orderLines1 = new ArrayList<>();
        List<OrderLine> orderLines2 = new ArrayList<>();
        List<OrderLine> orderLines3 = new ArrayList<>();
        orderLines1.add(new OrderLine(cupcakeChocolate, 2));
        orderLines1.add(new OrderLine(cupcakePistacioStrawberry, 3));
        orderLines2.add(new OrderLine(cupcakeChocolate, 1));
        orderLines2.add(new OrderLine(cupcakePistacioStrawberry, 1));
        orderLines3.add(new OrderLine(cupcakeChocolate, 1));

        Order order1 = new Order(0, user, LocalDateTime.now(), LocalDateTime.now().plusDays(1), false, orderLines1, 56.0);
        Order order2 = new Order(1, user, LocalDateTime.now(), LocalDateTime.now().plusDays(1), false, orderLines2, 22.0);
        Order order3 = new Order(2, user2, LocalDateTime.now(), LocalDateTime.now().plusDays(1), false, orderLines3, 10.0);

        orderMapper.createOrder(order1);
        orderMapper.createOrder(order2);
        orderMapper.createOrder(order3);

        List<Order> allOrders = orderMapper.getAllOrders();

        assertNotNull(allOrders);
        assertEquals(3, allOrders.size());
    }

    @Test
    void testDeleteOrder() throws DatabaseException
    {
        UserDTO user = new UserDTO(1, "Hans", "Hansen", "hans@test.dk", 12345678, "Testvej 1", 2000, "Frederiksberg", 100.0);
        List<OrderLine> orderLines = new ArrayList<>();
        orderLines.add(new OrderLine(cupcakeChocolate, 2));
        orderLines.add(new OrderLine(cupcakePistacioStrawberry, 3));

        Order order = new Order(0, user, LocalDateTime.now(), LocalDateTime.now().plusDays(2), false, orderLines, 54.00);
        Order createdOrder = orderMapper.createOrder(order);

        boolean result = orderMapper.deleteOrder(createdOrder.getOrderId());

        assertTrue(result);
        assertThrows(DatabaseException.class,
                () -> orderMapper.getOrderByOrderId(createdOrder.getOrderId(), 1));
    }

    @Test
    void testDeleteOrderNotFound() throws DatabaseException
    {
        boolean result = orderMapper.deleteOrder(999);

        assertFalse(result);
    }

    @Test
    void testUpdateOrderStatus() throws DatabaseException
    {
        UserDTO user = new UserDTO(1, "Hans", "Hansen", "hans@test.dk", 12345678, "Testvej 1", 2000, "Frederiksberg", 100.0);
        List<OrderLine> orderLines = new ArrayList<>();
        orderLines.add(new OrderLine(cupcakeChocolate, 2));
        orderLines.add(new OrderLine(cupcakePistacioStrawberry, 3));

        Order order = new Order(0, user, LocalDateTime.now(), LocalDateTime.now().plusDays(2), false, orderLines, 54.00);
        Order createdOrder = orderMapper.createOrder(order);

        assertFalse(createdOrder.isPaid());

        boolean result = orderMapper.updateOrderStatus(createdOrder.getOrderId(), true);

        assertTrue(result);
        Order updatedOrder = orderMapper.getOrderByOrderId(createdOrder.getOrderId(), 1);
        assertTrue(updatedOrder.isPaid());
    }
}