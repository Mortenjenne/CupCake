package app.persistence;

import app.entities.User;
import app.exceptions.DatabaseException;
import java.sql.*;

public class UserMapper
{
    private ConnectionPool connectionPool;

    public UserMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public User createUser(String firstname, String lastname, String email, String password, int phonenumber, String street, int zipcode, String city) throws DatabaseException
    {
        User newUser = null;
        String sql = "INSERT INTO users (firstname, lastname, email, password, phonenumber, street, zip_code) values (?, ?, ?, ?, ?, ?, ?)";
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

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 1)
            {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next())
                {
                    int userId = rs.getInt(1);
                    newUser = new User(
                            userId,
                            firstname,
                            lastname,
                            email,
                            password,
                            phonenumber,
                            street,
                            zipcode,
                            city,
                            0,
                            false
                    );
                }
            }
        }
        catch (SQLException e)
        {
            throw new DatabaseException("Kunne ikke oprette bruger, email findes allerede i systemet");
        }
        return newUser;
    }

    public User getUserById(int userId) throws DatabaseException
    {
        String sql = "SELECT * FROM users u JOIN zipcodes z ON u.zip_code = z.zip_code WHERE user_id = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setInt(1, userId);
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
                        rs.getBoolean("admin")
                );
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
        String sql = "SELECT * FROM users u JOIN zipcodes z ON u.zip_code = z.zip_code WHERE email = ?";

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
                        rs.getBoolean("admin")
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
        String sql = "UPDATE users SET balance = ? WHERE user_id = ?";
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

    public User login(String email, String password) throws DatabaseException
    {
        String sql = "SELECT u.user_id, u.firstname, u.lastname, u.email, u.password, u.admin, " +
                "u.phonenumber, u.street, u.zip_code, u.balance, z.city " +
                "FROM users u " +
                "LEFT JOIN zip_codes z ON u.zip_code = z.zip_code " +
                "WHERE u.email = ? AND u.password = ?";

        try (Connection connection = connectionPool.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql))
        {
            ps.setString(1, email);
            ps.setString(2, password);

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
                        rs.getBoolean("admin")
                );
            }
            else
            {
                throw new DatabaseException("Forkert email eller password");
            }
        }
        catch (SQLException e)
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
}
