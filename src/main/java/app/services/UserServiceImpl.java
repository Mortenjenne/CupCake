package app.services;

import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.UserMapper;

public class UserServiceImpl implements UserService
{
    private UserMapper userMapper;

    public UserServiceImpl(UserMapper userMapper)
    {
        this.userMapper = userMapper;
    }

    @Override
    public User authenticate(String email, String password) throws DatabaseException
    {
        return null;
    }

    @Override
    public User registerUser(User user) throws DatabaseException
    {
        return null;
    }

    @Override
    public void addBalance(int userId, double amount) throws DatabaseException
    {

    }

    @Override
    public User getUserById(int userId) throws DatabaseException
    {
        return null;
    }
}
