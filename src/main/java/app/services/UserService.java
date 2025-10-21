package app.services;

import app.entities.User;
import app.exceptions.DatabaseException;

public interface UserService
{
public User authenticate(String email, String password) throws DatabaseException;
public User registerUser(String email, String password, String firstName, String lastName, String street, String city, int zipCode, int phoneNumber) throws DatabaseException;
public void addBalance(int userId, double amount) throws DatabaseException;
public User getUserById(int userId) throws DatabaseException;
}
