package app.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class User
{
    private int userId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private int phoneNumber;
    private String street;
    private int zipCode;
    private String city;
    private double balance;
    private boolean isAdmin;

    public void addToBalance(double amount)
    {

    }

    public boolean subtractFromBalance(double amount)
    {
        return false;
    }
}
