package app.services;

import app.dto.CreateUserRequestDTO;
import app.dto.UserDTO;
import app.entities.User;
import app.exceptions.DatabaseException;

import java.util.List;

public interface UserService
{
public User authenticate(String email, String password) throws DatabaseException;
public User registerUser(CreateUserRequestDTO createUserRequestDTO) throws DatabaseException;
public void addBalance(int userId, double amount) throws DatabaseException;
public UserDTO getUserById(int userId) throws DatabaseException;
public List<UserDTO> getAllUsers() throws DatabaseException;
public List<UserDTO> searchUsersByName(String name) throws DatabaseException;
public List<UserDTO> searchUsersByEmail(String email) throws DatabaseException;
public List<UserDTO> searchUsersByUserId(int userId) throws DatabaseException;
public void validateInput(String firstName, String lastName, String street, int zipCode, String city, int phoneNumber, String email);
}
