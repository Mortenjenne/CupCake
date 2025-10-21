package app.services;

import app.entities.User;
import app.exceptions.DatabaseException;

public interface UserService
{
public User authenticate(String email, String password) throws DatabaseException;
public User registerUser(User user) throws DatabaseException;
public void addBalance(int userId, double amount) throws DatabaseException;
public User getUserById(int userId) throws DatabaseException;
}
