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
    public User registerUser(String email, String password, String firstName, String lastName, String street, String city, int zipCode, int phoneNumber) throws DatabaseException
    {
        validateFirstOrLastName(firstName);
        validateFirstOrLastName(lastName);
        validatePassword(password);
        validateEmail(email);
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

    private void validateFirstOrLastName(String name)
    {
        if (name == null || name.trim().isEmpty())
        {
            throw new IllegalArgumentException("Fornavn eller efternavn kan ikke være tomme");
        }

        if (name.length() < 2)
        {
            throw new IllegalArgumentException("Fornavn eller efternavn skal være over 2 tegn");
        }
    }

    private void validateEmail(String email)
    {
        if (email == null || email.trim().isEmpty())
        {
            throw new IllegalArgumentException("Email kan ikke være tomt");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))
        {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
    private void validatePassword(String password)
    {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain uppercase letter");
        }

        if (!password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException("Password must contain a number");
        }
    }
}
