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
public User getUserById(int userId) throws DatabaseException;
public List<UserDTO> getAllUsers() throws DatabaseException;
}
