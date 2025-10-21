package app.persistence;

import app.entities.User;
import app.exceptions.DatabaseException;
import java.sql.*;

public class UserMapper
{
    private ConnectionPool connectionPool;

    public UserMapper(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    public User createUser(User user) throws DatabaseException
    {
        User newUser = null;
        String sql = "INSERT INTO users (firstname, lastname, email, password, phonenumber, address, zipcode) values (?, ?, ?, ?, ?, ?, ?)";
        ensureZipExists(user.getZipCode(), user.getCity());

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setString(5,user.getPhoneNumber());
            ps.setString(6, user.getAddress());
            ps.setInt(7, user.getZipCode());

            ResultSet rs = ps.executeQuery();

            if (rs.next())
            {
                int userId = ps.getGeneratedKeys().getInt("user_id");
                newUser = user;
                newUser.setUserId(userId);
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Kunne ikke oprette bruger, email findes allerede i systemet");
        }
        return newUser;
    }

    public User getUserById(int userId) {
        return null;
    }

    public User updateUser(User user) {
        return null;
    }

    public boolean deleteUser(int userId) {
        return false;
    }

    public User login(String email, String password) throws DatabaseException
    {
        String sql = "SELECT u.user_id, u.firstname, u.lastname, u.email, u.password, u.admin, " +
                "u.phonenumber, u.address, u.zip_code, u.balance, z.city, " +
                "FROM users u " +
                "LEFT JOIN zip_codes z ON u.zip_code = z.zip_code " +
                "WHERE u.email = ? AND u.password = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("firstname"),
                        rs.getString("lastname"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("phonenumber"),
                        rs.getString("address"),
                        rs.getInt("zip_code"),
                        rs.getString("city"),
                        rs.getDouble("balance"),
                        rs.getBoolean("admin")
                );
            }
            else
            {
                throw new DatabaseException("Forkert email eller password");
            }
        }
        catch(SQLException e)
        {
            throw new DatabaseException("Login-fejl: " + e.getMessage());
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
            if (!rs.next()) {
                String insertSql = "INSERT INTO zip_codes (zip_code, city) VALUES (?, ?)";
                try (PreparedStatement insertPs = connection.prepareStatement(insertSql))
                {
                    insertPs.setInt(1, zipCode);
                    insertPs.setString(2, city);
                    insertPs.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Kunne ikke indsætte zip_code/city");
        }
    }
}
