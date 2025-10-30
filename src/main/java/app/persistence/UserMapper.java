package app.persistence;

import app.dto.UserDTO;
import app.entities.User;
import app.exceptions.DatabaseException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserMapper
{
    private ConnectionPool connectionPool;

    public UserMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public User createUser(String firstname, String lastname, String email, String password, int phonenumber, String street, int zipcode, String city) throws DatabaseException
    {
        User user = null;
        String sql = "INSERT INTO users (firstname, lastname, email, password, phonenumber, street, zip_code, balance, admin, is_guest) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING user_id";
        ensureZipExists(zipcode, city);

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setString(1, firstname);
            ps.setString(2, lastname);
            ps.setString(3, email);
            ps.setString(4, password);
            ps.setInt(5, phonenumber);
            ps.setString(6, street);
            ps.setInt(7, zipcode);
            ps.setDouble(8, 0.0);       // default balance
            ps.setBoolean(9, false); // default admin-status
            ps.setBoolean(10, false); // default guest-status

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected != 1)
            {
                throw new DatabaseException("Uventet fejl");
            }

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next())
            {
                int userId = rs.getInt(1);
                return getUserById(userId);
            }
        }
        catch (SQLException e)
        {
            if (e.getMessage().toLowerCase().contains("duplicate") || e.getMessage().toLowerCase().contains("email_unique"))
            {
                throw new DatabaseException("Email findes allerede, vælg en anden eller log ind");
            }
            else
            {
                throw new DatabaseException("Databasefejl ved oprettelse af bruger: " + e.getMessage());
            }
        }
        return null;
    }

    public User createGuestUser(String firstName, String lastName, String email,
                                int phoneNumber, String street, int zipCode) throws DatabaseException
    {
        String sql = "INSERT INTO users (firstname, lastname, email, password, phonenumber, street, zip_code, balance, admin, is_guest) " +
                "VALUES (?, ?, ?, NULL, ?, ?, ?, 0, false, true) RETURNING user_id";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            ps.setInt(4, phoneNumber);
            ps.setString(5, street);
            ps.setInt(6, zipCode);

            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                int userId = rs.getInt("user_id");
                return getUserById(userId);
            }
            throw new DatabaseException("Kunne ikke oprette guest bruger");
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved oprettelse af guest bruger: " + e.getMessage());
        }
    }

    public User getUserById(int userId) throws DatabaseException
    {
        String sql = "SELECT u.user_id, u.firstname, u.lastname, u.email, u.phonenumber, " +
                "u.street, u.zip_code, u.balance, u.admin, u.is_guest, z.city " +
                "FROM users u " +
                "JOIN zip_codes z ON u.zip_code = z.zip_code " +
                "WHERE u.user_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                return buildUserFromResultSet(rs);
            }
            else
            {
                throw new DatabaseException("Der blev ikke fundet en bruger med id: " + userId);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af bruger: " + e.getMessage());
        }
    }

    public User getUserByEmail(String email) throws DatabaseException
    {
        User user = null;
        String sql = "SELECT * FROM users u JOIN zip_codes z ON u.zip_code = z.zip_code WHERE email = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                 return new User(
                    rs.getInt("user_id"),
                    rs.getString("firstname"),
                    rs.getString("lastname"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getInt("phonenumber"),
                    rs.getString("street"),
                    rs.getInt("zip_code"),
                    rs.getString("city"),
                    rs.getDouble("balance"),
                    rs.getBoolean("admin"),
                    rs.getBoolean("is_guest")
            );
            }
            else
            {
                throw new DatabaseException("Der blev ikke fundet en bruger med email: " + email);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved hentning af bruger: " + e.getMessage());
        }
    }

    public boolean updateUser(User user) throws DatabaseException
    {
        String sql = "UPDATE users SET firstname = ?, lastname = ?, email = ?, password = ?, phonenumber = ?, street = ?, admin = ?, zip_code = ?, balance = ? WHERE user_id = ?";
        ensureZipExists(user.getZipCode(), user.getCity());
        boolean result = false;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setInt(5, user.getPhoneNumber());
            ps.setString(6, user.getStreet());
            ps.setBoolean(7, user.isAdmin());
            ps.setInt(8, user.getZipCode());
            ps.setDouble(9, user.getBalance());
            ps.setInt(10, user.getUserId());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1)
            {
                result = true;
            }
            else
            {
                throw new DatabaseException("Brugeren blev ikke opdateret - id: " + user.getUserId());
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved opdatering af bruger: " + e.getMessage());
        }
        return result;
    }

    public boolean deleteUser(int userId) throws DatabaseException
    {
        String sql = "DELETE FROM users WHERE user_id = ?";
        boolean result = false;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, userId);
            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1)
            {
                result = true;
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl ved sletning af bruger med id: " + userId);
        }
        return result;
    }

    public boolean updateUserBalance(int userId, double amount) throws DatabaseException
    {
        String sql = "UPDATE users SET balance = ? WHERE user_id = ? AND is_guest = false";
        boolean result = false;

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setDouble(1, amount);
            ps.setInt(2, userId);

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1)
            {
                result = true;
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Beløbet " + amount + ",- blev ikke tilføjet til bruger med id: " + userId);
        }
        return result;
    }

    public List<User> getAllUsers() throws DatabaseException
    {
        String sql = "SELECT users.*, zip_codes.city FROM users JOIN zip_codes ON users.zip_code = zip_codes.zip_code";
        List<User> users = new ArrayList<>();

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("email"),
                        rs.getInt("phonenumber"),
                        rs.getString("street"),
                        rs.getInt("zip_code"),
                        rs.getString("city"),
                        rs.getDouble("balance"),
                        rs.getBoolean("admin"),
                        rs.getBoolean("is_guest")
                ));
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Fejl under hentning af alle brugere");
        }
        return users;
    }

    public User login(String email, String password) throws DatabaseException
    {
        String sql = "SELECT u.user_id, u.firstname, u.lastname, u.email, u.phonenumber, " +
                "u.street, u.zip_code, u.balance, u.admin, u.is_guest, z.city " +
                "FROM users u " +
                "JOIN zip_codes z ON u.zip_code = z.zip_code " +
                "WHERE u.email = ? AND u.password = ? AND u.is_guest = false";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            if (rs.next())
            {
                return buildUserFromResultSet(rs);
            }
            throw new DatabaseException("Forkert email eller password");
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Login fejlede: " + e.getMessage());
        }
    }

    private void ensureZipExists(int zipCode, String city) throws DatabaseException
    {
        String checkSql = "SELECT zip_code FROM zip_codes WHERE zip_code = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(checkSql))
        {
            ps.setInt(1, zipCode);
            ResultSet rs = ps.executeQuery();
            if (!rs.next())
            {
                String insertSql = "INSERT INTO zip_codes (zip_code, city) VALUES (?, ?)";
                try (PreparedStatement insertPs = connection.prepareStatement(insertSql))
                {
                    insertPs.setInt(1, zipCode);
                    insertPs.setString(2, city);
                    insertPs.executeUpdate();
                }
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Kunne ikke indsætte postnummer og by");
        }
    }

    private User buildUserFromResultSet(ResultSet rs) throws SQLException
    {
        return new User(
                rs.getInt("user_id"),
                rs.getString("firstname"),
                rs.getString("lastname"),
                rs.getString("email"),
                rs.getInt("phonenumber"),
                rs.getString("street"),
                rs.getInt("zip_code"),
                rs.getString("city"),
                rs.getDouble("balance"),
                rs.getBoolean("admin"),
                rs.getBoolean("is_guest")
        );
    }
}
