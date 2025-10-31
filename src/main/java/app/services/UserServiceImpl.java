package app.services;

import app.dto.CreateUserRequestDTO;
import app.dto.UserDTO;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.UserMapper;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.stream.Collectors;

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
        User user = userMapper.getUserByEmail(email);
        if (user != null && BCrypt.checkpw(password, user.getPassword()))
        {
            return user;
        }
        else
        {
            throw new DatabaseException("Forkert email eller password");
        }
    }

    @Override
    public User registerUser(CreateUserRequestDTO createUserRequestDTO) throws DatabaseException
    {
        if (!createUserRequestDTO.getPassword1().equals(createUserRequestDTO.getPassword2()))
        {
            throw new IllegalArgumentException("Passwords er ikke ens");
        }

        int zipCode;
        int phoneNumber;

        try
        {
            zipCode = Integer.parseInt(createUserRequestDTO.getZipCode().trim());
            phoneNumber = Integer.parseInt(createUserRequestDTO.getPhoneNumber().trim());
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Postnummer og telefon skal være gyldige tal");
        }

        validateFirstOrLastName(createUserRequestDTO.getFirstName());
        validateFirstOrLastName(createUserRequestDTO.getLastName());
        validatePassword(createUserRequestDTO.getPassword1());
        validateEmail(createUserRequestDTO.getEmail());
        validateZipCode(zipCode);
        validatePhone(phoneNumber);
        validateStreet(createUserRequestDTO.getStreet());
        validateCity(createUserRequestDTO.getCity());

        String hashedPassword = BCrypt.hashpw(createUserRequestDTO.getPassword1(), BCrypt.gensalt());

        return userMapper.createUser(createUserRequestDTO.getFirstName(), createUserRequestDTO.getLastName(), createUserRequestDTO.getEmail(), hashedPassword, phoneNumber, createUserRequestDTO.getStreet(), zipCode, createUserRequestDTO.getCity());
    }

    @Override
    public User registerGuestUser(String firstName, String lastName, String email,
                                  int phoneNumber, String city, String street, int zipCode) throws DatabaseException
    {
        validateInput(firstName, lastName, street, zipCode, city, phoneNumber, email);

        User guestUser = checkIfGuestExists(email);
        if (guestUser != null)
        {
            return guestUser;
        }
        else
        {
            return userMapper.createGuestUser(firstName, lastName, email,
                    phoneNumber, street, zipCode);
        }
    }

    @Override
    public void addBalance(int userId, double amount) throws DatabaseException
    {
        if (amount <= 0)
        {
            throw new IllegalArgumentException("Beløb skal være positivt");
        }
        userMapper.updateUserBalance(userId, amount);
    }

    @Override
    public UserDTO getUserById(int userId) throws DatabaseException
    {
        User user = userMapper.getUserById(userId);
        return buildUserDTO(user);
    }

    @Override
    public List<UserDTO> getAllUsers() throws DatabaseException
    {
        return userMapper.getAllUsers().stream()
                .filter(user -> user.isGuest() == false)
                .map(user -> buildUserDTO(user))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> searchUsersByName(String name) throws DatabaseException
    {
        return userMapper.getAllUsers().stream()
                .filter(user -> user.getFirstName().toLowerCase().contains(name.toLowerCase())
                        || user.getLastName().toLowerCase().contains(name.toLowerCase()))
                .map(user -> buildUserDTO(user))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> searchUsersByEmail(String email) throws DatabaseException
    {
        return userMapper.getAllUsers().stream()
                .filter(user -> user.getEmail().toLowerCase().contains(email.toLowerCase()))
                .map(user -> buildUserDTO(user))
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> searchUsersByUserId(int userId) throws DatabaseException
    {
        return userMapper.getAllUsers().stream()
                .filter(user -> user.getUserId() == userId)
                .map(user -> buildUserDTO(user))
                .collect(Collectors.toList());
    }

    public void validateInput(String firstName, String lastName, String street, int zipCode, String city, int phoneNumber, String email)
    {
        validateFirstOrLastName(firstName);
        validateFirstOrLastName(lastName);
        validateStreet(street);
        validateZipCode(zipCode);
        validateCity(city);
        validatePhone(phoneNumber);
        validateEmail(email);
    }

    private void validateFirstOrLastName(String name)
    {
        if (name == null || name.trim().isEmpty())
        {
            throw new IllegalArgumentException("Fornavn eller efternavn kan ikke være tomme");
        }

        if (name.length() < 2)
        {
            throw new IllegalArgumentException("Fornavn eller efternavn skal minimum være 2 tegn");
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
            throw new IllegalArgumentException("Ikke gyldig email format");
        }
    }

    private void validatePassword(String password)
    {
        if (password == null || password.length() < 8)
        {
            throw new IllegalArgumentException("Password skal være mindst 8 tegn");
        }

        if (!password.matches(".*[A-Z].*"))
        {
            throw new IllegalArgumentException("Password skal indeholde et stort bogstav");
        }

        if (!password.matches(".*[0-9].*"))
        {
            throw new IllegalArgumentException("Password skal indeholde et tal");
        }
    }

    private void validateZipCode(int zipCode)
    {
        if (zipCode < 1000 || zipCode > 9999)
        {
            throw new IllegalArgumentException("Postnummer skal være mellem 1000 og 9999");
        }
    }

    private void validatePhone(int phone)
    {
        if (phone < 10000000 || phone > 99999999)
        {
            throw new IllegalArgumentException("Telefonnummer skal være 8 cifre");
        }
    }

    private void validateCity(String city)
    {
        if (city == null || city.trim().isEmpty())
        {
            throw new IllegalArgumentException("By kan ikke være tom");
        }
        if (city.length() < 2)
        {
            throw new IllegalArgumentException("By skal være mindst 2 tegn");
        }
    }

    private void validateStreet(String street)
    {
        if (street == null || street.trim().isEmpty())
        {
            throw new IllegalArgumentException("Gade kan ikke være tom");
        }
    }

    private UserDTO buildUserDTO(User user)
    {
        return new UserDTO(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStreet(),
                user.getZipCode(),
                user.getCity(),
                user.getBalance()
        );
    }

    private User checkIfGuestExists(String email)
    {
        try
        {
            User guest = userMapper.getUserByEmail(email);
            return guest;
        }
        catch (DatabaseException e)
        {
            return null;
        }
    }
}
