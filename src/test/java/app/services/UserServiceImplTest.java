package app.services;

import app.dto.CreateUserRequestDTO;
import app.exceptions.DatabaseException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserServiceImplTest
{
    private final UserServiceImpl service = new UserServiceImpl(null);

    @Test
    void testRegisterWithPasswordNotMatching()
    {
        CreateUserRequestDTO dto = new CreateUserRequestDTO(
                "john@admin.dk",
                "Password1",
                "Password2",
                "John",
                "Admin",
                "12345678",
                "Admin Street 1234",
                "2100",
                "Copenhagen"
        );

        assertThrows(IllegalArgumentException.class, () -> service.registerUser(dto));
    }

    @Test
    void testRegisterWithToShortPassword()
    {
        CreateUserRequestDTO dto = new CreateUserRequestDTO(
                "john@admin.dk",
                "Pass1",
                "Pass1",
                "John",
                "Admin",
                "12345678",
                "Admin Street 1234",
                "2100",
                "Copenhagen"
        );

        assertThrows(IllegalArgumentException.class, () -> service.registerUser(dto));
    }

    @Test
    void testRegisterWithInvalidEmailFormat()
    {
        CreateUserRequestDTO dto = new CreateUserRequestDTO(
                "john@admin.d",
                "Pass1",
                "Pass1",
                "John",
                "Admin",
                "12345678",
                "Admin Street 1234",
                "2100",
                "Copenhagen"
        );

        assertThrows(IllegalArgumentException.class, () -> service.registerUser(dto));
    }

    @Test
    void testRegisterWithInvalidZipCode()
    {
        CreateUserRequestDTO dto = new CreateUserRequestDTO(
                "john@admin.dk",
                "Pass1",
                "Pass1",
                "John",
                "Admin",
                "12345678",
                "Admin Street 1234",
                "210",
                "Copenhagen"
        );

        assertThrows(IllegalArgumentException.class, () -> service.registerUser(dto));

    }

    @Test
    void testRegisterUserWithFirstNameWithOneLetter()
    {
        CreateUserRequestDTO dto = new CreateUserRequestDTO(
                "john@admin.dk",
                "Password1.",
                "Password1.",
                "J",
                "Admin",
                "12345678",
                "Admin Street 1234",
                "2100",
                "Copenhagen"
        );

        assertThrows(IllegalArgumentException.class, () -> service.registerUser(dto));
    }

    @Test
    void testRegisterUserWithLastNameWithOneLetter()
    {
        CreateUserRequestDTO dto = new CreateUserRequestDTO(
                "john@admin.dk",
                "Password1.",
                "Password1.",
                "John",
                "A",
                "12345678",
                "Admin Street 1234",
                "2100",
                "Copenhagen"
        );

        assertThrows(IllegalArgumentException.class, () -> service.registerUser(dto));
    }

    @Test
    void testRegisterUserWithFirstNameEmpty()
    {
        CreateUserRequestDTO dto = new CreateUserRequestDTO(
                "john@admin.dk",
                "Password1.",
                "Password1.",
                "",
                "Admin",
                "12345678",
                "Admin Street 1234",
                "2100",
                "Copenhagen"
        );

        assertThrows(IllegalArgumentException.class, () -> service.registerUser(dto));
    }

    @Test
    void testRegisterUserWith7digitTelephoneNumber()
    {
        CreateUserRequestDTO dto = new CreateUserRequestDTO(
                "john@admin.dk",
                "Password1.",
                "Password1.",
                "John",
                "Admin",
                "01234567",
                "Admin Street 1234",
                "2100",
                "Copenhagen"
        );

        assertThrows(IllegalArgumentException.class, () -> service.registerUser(dto));
    }

    @Test
    void testRegisterUserWith9digitTelephoneNumber()
    {
        CreateUserRequestDTO dto = new CreateUserRequestDTO(
                "john@admin.dk",
                "Password1.",
                "Password1.",
                "John",
                "Admin",
                "0123456789",
                "Admin Street 1234",
                "2100",
                "Copenhagen"
        );

        assertThrows(IllegalArgumentException.class, () -> service.registerUser(dto));
    }

    @Test
    void testAddNegativeBalance() throws DatabaseException
    {
        double balance = -500.00;
        int userId = 1;
        assertThrows(IllegalArgumentException.class, () -> service.addBalance(userId, balance));
    }
}
