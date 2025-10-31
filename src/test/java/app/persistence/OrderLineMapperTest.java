package app.persistence;

import app.entities.Bottom;
import app.entities.Cupcake;
import app.entities.OrderLine;
import app.entities.Topping;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderLineMapperTest
{
    private final static String USER = "postgres";
    private final static String PASSWORD = "postgres";
    private final static String URL = "jdbc:postgresql://localhost:5432/cupcake?currentSchema=test";

    private static ConnectionPool connectionPool;
    private static OrderLineMapper orderLineMapper;

    private Cupcake cupcakeChocolate;
    private Cupcake cupcakeVanilla;
    private Cupcake cupcakePistacioStrawberry;

    @BeforeAll
    public static void setUpClass()
    {
        try
        {
            connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, "cupcake");
            orderLineMapper = new OrderLineMapper(connectionPool);

            try (Connection testConnection = connectionPool.getConnection())
            {
                try (Statement stmt = testConnection.createStatement())
                {
                    stmt.execute("DROP TABLE IF EXISTS test.orderlines CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.orders CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.users CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.zip_codes CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.bottoms CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.toppings CASCADE");

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
                stmt.execute("DELETE FROM test.orderlines");
                stmt.execute("DELETE FROM test.orders");
                stmt.execute("DELETE FROM test.users");
                stmt.execute("DELETE FROM test.zip_codes");
                stmt.execute("DELETE FROM test.bottoms");
                stmt.execute("DELETE FROM test.toppings");

                stmt.execute("SELECT setval('test.users_user_id_seq', 1)");
                stmt.execute("SELECT setval('test.orders_order_id_seq', 1)");
                stmt.execute("SELECT setval('test.orderlines_orderline_id_seq', 1)");
                stmt.execute("SELECT setval('test.bottoms_bottom_id_seq', 1)");
                stmt.execute("SELECT setval('test.toppings_topping_id_seq', 1)");

                stmt.execute("INSERT INTO test.zip_codes (zip_code, city) VALUES " +
                        "(1000, 'Copenhagen'), " +
                        "(2000, 'Frederiksberg'), " +
                        "(2100, 'København Ø'), " +
                        "(8000, 'Aarhus C')");

                stmt.execute("INSERT INTO test.users (user_id, firstname, lastname, email, password, phonenumber, street, zip_code, balance, admin, is_guest) VALUES " +
                        "(1, 'Hans', 'Hansen', 'hans@test.dk', 'password123', 12345678, 'Testvej 1', 2000, 100.0, false, false), " +
                        "(2, 'Jens', 'Jensen', 'jens@test.dk', 'password456', 87654321, 'Prøvevej 2', 2100, 200.0, false, false)");

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

                stmt.execute("INSERT INTO test.orders (order_id, user_id, order_date, pickup_date, paid, price_total) VALUES " +
                        "(1, 1, '" + Timestamp.valueOf(LocalDateTime.now()) + "', '" +
                        Timestamp.valueOf(LocalDateTime.now().plusDays(2)) + "', false, 30.00)");

                stmt.execute("SELECT setval('test.users_user_id_seq', COALESCE((SELECT MAX(user_id)+1 FROM test.users), 1), false)");
                stmt.execute("SELECT setval('test.orders_order_id_seq', COALESCE((SELECT MAX(order_id)+1 FROM test.orders), 1), false)");
                stmt.execute("SELECT setval('test.bottoms_bottom_id_seq', COALESCE((SELECT MAX(bottom_id)+1 FROM test.bottoms), 1), false)");
                stmt.execute("SELECT setval('test.toppings_topping_id_seq', COALESCE((SELECT MAX(topping_id)+1 FROM test.toppings), 1), false)");
            }

            Bottom bottomChocolate = new Bottom(1, "Chocolate", 5.0);
            Topping toppingChocolate = new Topping(1, "Chocolate", 5.0);
            cupcakeChocolate = new Cupcake(bottomChocolate, toppingChocolate);

            Bottom bottomVanilla = new Bottom(2, "Vanilla", 5.0);
            Topping toppingBlueberry = new Topping(2, "Blueberry", 5.0);
            cupcakeVanilla = new Cupcake(bottomVanilla, toppingBlueberry);

            Bottom bottomPistacio = new Bottom(4, "Pistacio", 6.0);
            Topping toppingStrawberry = new Topping(4, "Strawberry", 6.0);
            cupcakePistacioStrawberry = new Cupcake(bottomPistacio, toppingStrawberry);
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
    void testInsertOrderLines() throws SQLException, DatabaseException
    {
        List<OrderLine> orderLines = new ArrayList<>();
        orderLines.add(new OrderLine(cupcakeChocolate, 2));
        orderLines.add(new OrderLine(cupcakeVanilla, 1));

        try (Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);
            orderLineMapper.insertOrderLines(connection, 1, orderLines);
            connection.commit();
        }

        List<OrderLine> retrievedLines = orderLineMapper.getOrderLinesByOrderId(1);

        assertNotNull(retrievedLines);
        assertEquals(2, retrievedLines.size());
    }

    @Test
    void testInsertSingleOrderLine() throws SQLException, DatabaseException
    {
        List<OrderLine> orderLines = new ArrayList<>();
        orderLines.add(new OrderLine(cupcakePistacioStrawberry, 3));

        try (Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);
            orderLineMapper.insertOrderLines(connection, 1, orderLines);
            connection.commit();
        }

        List<OrderLine> retrievedLines = orderLineMapper.getOrderLinesByOrderId(1);

        assertEquals(1, retrievedLines.size());
        assertEquals(3, retrievedLines.get(0).getQuantity());
        assertEquals("Pistacio", retrievedLines.get(0).getCupcake().getBottom().getName());
        assertEquals("Strawberry", retrievedLines.get(0).getCupcake().getTopping().getName());
    }

    @Test
    void testInsertMultipleOrderLines() throws SQLException, DatabaseException
    {
        List<OrderLine> orderLines = new ArrayList<>();
        orderLines.add(new OrderLine(cupcakeChocolate, 2));
        orderLines.add(new OrderLine(cupcakeVanilla, 1));
        orderLines.add(new OrderLine(cupcakePistacioStrawberry, 3));

        try (Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);
            orderLineMapper.insertOrderLines(connection, 1, orderLines);
            connection.commit();
        }

        List<OrderLine> retrievedLines = orderLineMapper.getOrderLinesByOrderId(1);

        assertEquals(3, retrievedLines.size());
    }

    @Test
    void testGetOrderLinesByOrderId() throws SQLException, DatabaseException
    {
        List<OrderLine> orderLines = new ArrayList<>();
        orderLines.add(new OrderLine(cupcakeChocolate, 2));
        orderLines.add(new OrderLine(cupcakeVanilla, 3));

        try (Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);
            orderLineMapper.insertOrderLines(connection, 1, orderLines);
            connection.commit();
        }

        List<OrderLine> retrievedLines = orderLineMapper.getOrderLinesByOrderId(1);

        assertNotNull(retrievedLines);
        assertEquals(2, retrievedLines.size());

        OrderLine line1 = retrievedLines.get(0);
        assertEquals(2, line1.getQuantity());
        assertEquals("Chocolate", line1.getCupcake().getBottom().getName());
        assertEquals("Chocolate", line1.getCupcake().getTopping().getName());
        assertEquals(5.0, line1.getCupcake().getBottom().getPrice());
        assertEquals(5.0, line1.getCupcake().getTopping().getPrice());

        OrderLine line2 = retrievedLines.get(1);
        assertEquals(3, line2.getQuantity());
        assertEquals("Vanilla", line2.getCupcake().getBottom().getName());
        assertEquals("Blueberry", line2.getCupcake().getTopping().getName());
    }

    @Test
    void testGetOrderLinesByOrderIdEmpty() throws DatabaseException
    {
        List<OrderLine> retrievedLines = orderLineMapper.getOrderLinesByOrderId(1);

        assertNotNull(retrievedLines);
        assertEquals(0, retrievedLines.size());
    }

    @Test
    void testGetOrderLinesByOrderIdNonExistent() throws DatabaseException
    {
        List<OrderLine> retrievedLines = orderLineMapper.getOrderLinesByOrderId(999);

        assertNotNull(retrievedLines);
        assertEquals(0, retrievedLines.size());
    }

    @Test
    void testDeleteOrderline() throws SQLException, DatabaseException
    {
        List<OrderLine> orderLines = new ArrayList<>();
        orderLines.add(new OrderLine(cupcakeChocolate, 2));
        orderLines.add(new OrderLine(cupcakeVanilla, 1));

        try (Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);
            orderLineMapper.insertOrderLines(connection, 1, orderLines);
            connection.commit();
        }

        List<OrderLine> retrievedLines = orderLineMapper.getOrderLinesByOrderId(1);
        assertEquals(2, retrievedLines.size());

        int orderlineIdToDelete = retrievedLines.get(0).getOrderLineId();

        boolean result = orderLineMapper.deleteOrderline(orderlineIdToDelete);

        assertTrue(result);

        List<OrderLine> remainingLines = orderLineMapper.getOrderLinesByOrderId(1);
        assertEquals(1, remainingLines.size());
    }

    @Test
    void testDeleteOrderlineNotFound() throws DatabaseException
    {
        boolean result = orderLineMapper.deleteOrderline(999);

        assertFalse(result);
    }

    @Test
    void testDeleteAllOrderlines() throws SQLException, DatabaseException
    {
        List<OrderLine> orderLines = new ArrayList<>();
        orderLines.add(new OrderLine(cupcakeChocolate, 2));
        orderLines.add(new OrderLine(cupcakeVanilla, 1));
        orderLines.add(new OrderLine(cupcakePistacioStrawberry, 3));

        try (Connection connection = connectionPool.getConnection())
        {
            connection.setAutoCommit(false);
            orderLineMapper.insertOrderLines(connection, 1, orderLines);
            connection.commit();
        }

        List<OrderLine> retrievedLines = orderLineMapper.getOrderLinesByOrderId(1);
        assertEquals(3, retrievedLines.size());

        for (OrderLine line : retrievedLines)
        {
            boolean result = orderLineMapper.deleteOrderline(line.getOrderLineId());
            assertTrue(result);
        }

        List<OrderLine> remainingLines = orderLineMapper.getOrderLinesByOrderId(1);
        assertEquals(0, remainingLines.size());
    }

    @Test
    void testInsertOrderLineWithInvalidToppingId()
    {
        Bottom validBottom = new Bottom(1, "Chocolate", 5.0);
        Topping invalidTopping = new Topping(999, "Invalid", 5.0);
        Cupcake invalidCupcake = new Cupcake(validBottom, invalidTopping);

        List<OrderLine> orderLines = new ArrayList<>();
        orderLines.add(new OrderLine(invalidCupcake, 1));

        assertThrows(SQLException.class, () ->
        {
            try (Connection connection = connectionPool.getConnection())
            {
                connection.setAutoCommit(false);
                orderLineMapper.insertOrderLines(connection, 1, orderLines);
                connection.commit();
            }
        });
    }
}