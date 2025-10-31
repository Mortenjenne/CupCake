package app.persistence;

import app.entities.User;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest
{
    private final static String USER = "postgres";
    private final static String PASSWORD = "postgres";
    private final static String URL = "jdbc:postgresql://localhost:5432/cupcake?currentSchema=test";

    private static ConnectionPool connectionPool;
    private static UserMapper userMapper;

    @BeforeAll
    public static void setUpClass()
    {
        try
        {
            connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, "cupcake");
            userMapper = new UserMapper(connectionPool);

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
                        "(2, 'Jens', 'Jensen', 'jens@test.dk', 'password456', 87654321, 'Prøvevej 2', 2100, 200.0, false, false), " +
                        "(3, 'Admin', 'Adminson', 'admin@test.dk', 'admin123', 11111111, 'Adminvej 3', 8000, 0.0, true, false)");

                stmt.execute("SELECT setval('test.users_user_id_seq', COALESCE((SELECT MAX(user_id)+1 FROM test.users), 1), false)");
            }
        }
        catch (SQLException e)
        {
            System.out.println(e.getMessage());
            fail("Database setup failed");
        }
    }

    @Test
    void testConnection() throws SQLException
    {
        assertNotNull(connectionPool.getConnection());
    }

    @Test
    void testCreateUser() throws DatabaseException
    {
        User newUser = userMapper.createUser(
                "Peter",
                "Petersen",
                "peter@test.dk",
                "password789",
                99999999,
                "Nyvej 4",
                2000,
                "Frederiksberg"
        );

        assertNotNull(newUser);
        assertEquals("Peter", newUser.getFirstName());
        assertEquals("peter@test.dk", newUser.getEmail());
        assertEquals(4, newUser.getUserId());
        assertEquals(0.0, newUser.getBalance());
        assertFalse(newUser.isGuest());
    }

    @Test
    void testCreateGuestUser() throws DatabaseException
    {
        User guestUser = userMapper.createGuestUser(
                "Guest",
                "User",
                "guest@test.dk",
                88888888,
                "Gæstevej 5",
                2000
        );

        assertNotNull(guestUser);
        assertEquals("Guest", guestUser.getFirstName());
        assertTrue(guestUser.isGuest());
        assertEquals(0.0, guestUser.getBalance());
    }

    @Test
    void testGetUserById() throws DatabaseException
    {
        User user = userMapper.getUserById(1);

        assertNotNull(user);
        assertEquals("Hans", user.getFirstName());
        assertEquals("Hansen", user.getLastName());
        assertEquals("hans@test.dk", user.getEmail());
        assertEquals(2000, user.getZipCode());
        assertEquals("Frederiksberg", user.getCity());
        assertEquals(100.0, user.getBalance());
        assertFalse(user.isGuest());
    }

    @Test
    void testGetUserByIdNotFound()
    {
        assertThrows(DatabaseException.class,
                () -> userMapper.getUserById(999));
    }

    @Test
    void testGetUserByEmail() throws DatabaseException
    {
        User user = userMapper.getUserByEmail("jens@test.dk");

        assertNotNull(user);
        assertEquals(2, user.getUserId());
        assertEquals("Jens", user.getFirstName());
        assertEquals("Jensen", user.getLastName());
        assertEquals(200.0, user.getBalance());
    }

    @Test
    void testGetUserByEmailWithUpperCase() throws DatabaseException
    {
        User user = userMapper.getUserByEmail("JENS@TEST.DK");

        assertNotNull(user);
        assertEquals(2, user.getUserId());
        assertEquals("Jens", user.getFirstName());
        assertEquals("Jensen", user.getLastName());
        assertEquals(200.0, user.getBalance());
    }

    @Test
    void testGetUserByEmailNotFound()
    {
        assertThrows(DatabaseException.class,
                () -> userMapper.getUserByEmail("doesnotexist@test.dk"));
    }

    @Test
    void testUpdateUser() throws DatabaseException
    {
        User user = userMapper.getUserById(1);
        user.setFirstName("Hans Updated");
        user.setBalance(500.0);

        boolean result = userMapper.updateUser(user);

        assertTrue(result);
        User updatedUser = userMapper.getUserById(1);
        assertEquals("Hans Updated", updatedUser.getFirstName());
        assertEquals(500.0, updatedUser.getBalance());
    }

    @Test
    void testDeleteUser() throws DatabaseException
    {
        boolean result = userMapper.deleteUser(2);

        assertTrue(result);
        assertThrows(DatabaseException.class, () -> userMapper.getUserById(2));
    }

    @Test
    void testDeleteUserNotFound() throws DatabaseException
    {
        boolean result = userMapper.deleteUser(999);

        assertFalse(result);
    }

    @Test
    void testUpdateUserBalance() throws DatabaseException
    {
        boolean result = userMapper.updateUserBalance(1, 999.99);

        assertTrue(result);
        User user = userMapper.getUserById(1);
        assertEquals(999.99, user.getBalance());
    }

    @Test
    void testUpdateGuestUserBalance() throws DatabaseException
    {
        User guest = userMapper.createGuestUser("Test", "Guest", "testguest@test.dk", 12341234, "Street", 2000);

        boolean result = userMapper.updateUserBalance(guest.getUserId(), 100.0);

        assertFalse(result);
        User retrieved = userMapper.getUserById(guest.getUserId());
        assertEquals(0.0, retrieved.getBalance());
    }

    @Test
    void testLogin() throws DatabaseException
    {
        User user = userMapper.login("admin@test.dk", "admin123");

        assertNotNull(user);
        assertEquals(3, user.getUserId());
        assertEquals("Admin", user.getFirstName());
        assertTrue(user.isAdmin());
        assertFalse(user.isGuest());
    }

    @Test
    void testLoginWithUpperCase() throws DatabaseException
    {
        User user = userMapper.login("ADMIN@TEST.DK", "admin123");

        assertNotNull(user);
        assertEquals(3, user.getUserId());
        assertEquals("Admin", user.getFirstName());
        assertTrue(user.isAdmin());
        assertFalse(user.isGuest());
    }

    @Test
    void testLoginWithWrongPassword()
    {
        assertThrows(DatabaseException.class,
                () -> userMapper.login("hans@test.dk", "wrongpassword"));
    }

    @Test
    void testLoginWithWrongEmail()
    {
        assertThrows(DatabaseException.class,
                () -> userMapper.login("doesnotexist@test.dk", "password123"));
    }

    @Test
    void testGuestCannotLogin() throws DatabaseException
    {
        userMapper.createGuestUser("Cannot", "Login", "nologin@test.dk", 11112222, "Street", 2000);

        assertThrows(DatabaseException.class,
                () -> userMapper.login("nologin@test.dk", null));
    }

    @Test
    void testCreateUserWithNewZipcode() throws DatabaseException
    {
        User newUser = userMapper.createUser(
                "New",
                "User",
                "new@test.dk",
                "pass",
                12121212,
                "Newvej 1",
                9000,
                "Aalborg"
        );

        assertNotNull(newUser);
        assertEquals(9000, newUser.getZipCode());
        assertEquals("Aalborg", newUser.getCity());

        User retrieved = userMapper.getUserById(newUser.getUserId());
        assertEquals("Aalborg", retrieved.getCity());
    }

    @Test
    void testCreateUserWithEmailThatAlreadyExist()
    {
        assertThrows(DatabaseException.class,
                () -> userMapper.createUser(
                        "Duplicate",
                        "User",
                        "hans@test.dk",
                        "pass",
                        12121212,
                        "Dupvej 1",
                        2000,
                        "Frederiksberg"
                ));
    }

    @Test
    void updateUserWithNewZipcode() throws DatabaseException
    {
        User user = userMapper.getUserById(1);
        user.setZipCode(5000);
        user.setCity("Odense");

        boolean result = userMapper.updateUser(user);

        assertTrue(result);
        User updated = userMapper.getUserById(1);
        assertEquals(5000, updated.getZipCode());
        assertEquals("Odense", updated.getCity());
    }
}