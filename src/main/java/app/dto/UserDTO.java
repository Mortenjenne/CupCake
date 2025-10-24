package app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTO
{
    private int userId;
    private String firstName;
    private String lastName;
    private String email;
    private int phoneNumber;
    private String street;
    private int zipCode;
    private String city;
    private double balance;
}
