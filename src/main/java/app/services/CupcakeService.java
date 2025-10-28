package app.services;


import app.entities.Bottom;
import app.entities.Topping;
import app.exceptions.DatabaseException;

import java.util.List;

public interface CupcakeService
{
    List<Bottom> getAllBottoms() throws DatabaseException;

    List<Topping> getAllToppings() throws DatabaseException;

    void createNewBottomFlavour(String bottomFlavour, double bottomPrice) throws DatabaseException;

    void createNewToppingFlavour(String toppingFlavour, double toppingPrice) throws DatabaseException;

    void setBottomPrice(Bottom bottom, double price) throws DatabaseException;

    void setToppingPrice(Topping topping, double price) throws DatabaseException;

    void renameBottomFlavour(Bottom bottom, String flavour) throws DatabaseException;

    void renameToppingFlavour(Topping topping, String flavour) throws DatabaseException;

    void deleteBottomFlavour(Bottom bottom) throws DatabaseException;

    void deleteToppingFlavour(Topping topping) throws DatabaseException;

    Bottom getBottomById(int id) throws DatabaseException;

    Topping getToppingById(int id) throws DatabaseException;
}
