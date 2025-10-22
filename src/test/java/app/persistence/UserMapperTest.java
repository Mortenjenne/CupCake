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

                    stmt.execute("DROP TABLE IF EXISTS test.users_orders CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.orderlines CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.orders CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.users CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.zip_codes CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.bottoms CASCADE");
                    stmt.execute("DROP TABLE IF EXISTS test.toppings CASCADE");

                    stmt.execute("DROP SEQUENCE IF EXISTS test.users_user_id_seq CASCADE");
                    stmt.execute("DROP SEQUENCE IF EXISTS test.orders_order_id_seq CASCADE");
                    stmt.execute("DROP SEQUENCE IF EXISTS test.orderlines_order_line_id_seq CASCADE");
                    stmt.execute("DROP SEQUENCE IF EXISTS test.bottoms_bottom_id_seq CASCADE");
                    stmt.execute("DROP SEQUENCE IF EXISTS test.toppings_topping_id_seq CASCADE");

                    stmt.execute("CREATE TABLE test.zip_codes AS (SELECT * FROM public.zip_codes) WITH NO DATA");
                    stmt.execute("CREATE TABLE test.users AS (SELECT * FROM public.users) WITH NO DATA");
                    stmt.execute("CREATE TABLE test.orders AS (SELECT * FROM public.orders) WITH NO DATA");
                    stmt.execute("CREATE TABLE test.bottoms AS (SELECT * FROM public.bottoms) WITH NO DATA");
                    stmt.execute("CREATE TABLE test.toppings AS (SELECT * FROM public.toppings) WITH NO DATA");
                    stmt.execute("CREATE TABLE test.orderlines AS (SELECT * FROM public.orderlines) WITH NO DATA");
                    stmt.execute("CREATE TABLE test.users_orders AS (SELECT * FROM public.users_orders) WITH NO DATA");

                    stmt.execute("CREATE SEQUENCE test.users_user_id_seq");
                    stmt.execute("ALTER TABLE test.users ALTER COLUMN user_id SET DEFAULT nextval('test.users_user_id_seq')");

                    stmt.execute("CREATE SEQUENCE test.orders_order_id_seq");
                    stmt.execute("ALTER TABLE test.orders ALTER COLUMN order_id SET DEFAULT nextval('test.orders_order_id_seq')");

                    stmt.execute("CREATE SEQUENCE test.orderlines_order_line_id_seq");
                    stmt.execute("ALTER TABLE test.orderlines ALTER COLUMN order_line_id SET DEFAULT nextval('test.orderlines_order_line_id_seq')");

                    stmt.execute("CREATE SEQUENCE test.bottoms_bottom_id_seq");
                    stmt.execute("ALTER TABLE test.bottoms ALTER COLUMN bottom_id SET DEFAULT nextval('test.bottoms_bottom_id_seq')");

                    stmt.execute("CREATE SEQUENCE test.toppings_topping_id_seq");
                    stmt.execute("ALTER TABLE test.toppings ALTER COLUMN topping_id SET DEFAULT nextval('test.toppings_topping_id_seq')");
                }
            }
            catch (SQLException e)
            {
                System.out.println(e.getMessage());
                fail("Database connection failed");
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

                stmt.execute("DELETE FROM test.users_orders");
                stmt.execute("DELETE FROM test.orderlines");
                stmt.execute("DELETE FROM test.orders");
                stmt.execute("DELETE FROM test.users");
                stmt.execute("DELETE FROM test.zip_codes");
                stmt.execute("DELETE FROM test.bottoms");
                stmt.execute("DELETE FROM test.toppings");

                stmt.execute("SELECT setval('test.users_user_id_seq', 1)");

                stmt.execute("INSERT INTO test.zip_codes (zip_code, city) VALUES " +
                        "(1000, 'Copenhagen'), " +
                        "(2000, 'Frederiksberg'), " +
                        "(2100, 'København Ø'), " +
                        "(8000, 'Aarhus C')");


                stmt.execute("INSERT INTO test.users (user_id, firstname, lastname, email, password, phonenumber, street, zip_code, balance, admin) VALUES " +
                        "(1, 'Hans', 'Hansen', 'hans@test.dk', 'password123', 12345678, 'Testvej 1', 2000, 100.0, false), " +
                        "(2, 'Jens', 'Jensen', 'jens@test.dk', 'password456', 87654321, 'Prøvevej 2', 2100, 200.0, false), " +
                        "(3, 'Admin', 'Adminson', 'admin@test.dk', 'admin123', 11111111, 'Adminvej 3', 8000, 0.0, true)");

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
    void testLogin() throws DatabaseException
    {
        User user = userMapper.login("admin@test.dk", "admin123");

        assertNotNull(user);
        assertEquals(3, user.getUserId());
        assertEquals("Admin", user.getFirstName());
        assertTrue(user.isAdmin());
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

        // Verify zipcode was created
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
                        "hans@test.dk",  // Email already exists
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
