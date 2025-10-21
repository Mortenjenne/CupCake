package app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateUserRequestDTO
{
    private String email;
    private String password1;
    private String password2;
    private String firstName;
    private String lastName;
    private String street;
    private String city;
    private String zipCode;
    private String phoneNumber;
}
