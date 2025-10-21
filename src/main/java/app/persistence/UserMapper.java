package app.persistence;

import app.entities.User;

public class UserMapper
{
    private ConnectionPool connectionPool;

    public UserMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public User createUser(User user)
    {
        return null;
    }

    public User getUserById(int userId)
    {
        return null;
    }

    public User updateUser(User user)
    {
        return null;
    }

    public boolean deleteUser(int userId)
    {
        return false;
    }
}
