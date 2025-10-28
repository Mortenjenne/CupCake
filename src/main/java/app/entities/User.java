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
    private boolean isGuest;

    public User(int userId, String firstName, String lastName, String email, int phoneNumber, String street, int zipCode, String city, double balance, boolean isAdmin, boolean isGuest)
    {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.street = street;
        this.zipCode = zipCode;
        this.city = city;
        this.balance = balance;
        this.isAdmin = isAdmin;
        this.isGuest = isGuest;
    }

    public void addToBalance(double amount)
    {

    }

    public boolean subtractFromBalance(double amount)
    {
        return false;
    }
}
