package app.services;

import app.entities.Bottom;
import app.entities.Topping;
import app.exceptions.DatabaseException;
import app.persistence.BottomMapper;
import app.persistence.ToppingMapper;

import java.util.List;

public class CupcakeServiceImpl implements CupcakeService
{
    private final BottomMapper bottomMapper;
    private final ToppingMapper toppingMapper;

    public CupcakeServiceImpl(BottomMapper bottomMapper, ToppingMapper toppingMapper)
    {
        this.bottomMapper = bottomMapper;
        this.toppingMapper = toppingMapper;
    }

    @Override
    public List<Bottom> getAllBottoms() throws DatabaseException
    {
        return bottomMapper.getAllBottoms();
    }

    @Override
    public List<Topping> getAllToppings() throws DatabaseException
    {
        return toppingMapper.getAllToppings();
    }

    @Override
    public void createNewBottomFlavour(String bottomFlavour, double bottomPrice) throws DatabaseException
    {
        bottomMapper.createBottom(bottomFlavour, bottomPrice);
    }

    @Override
    public void createNewToppingFlavour(String toppingFlavour, double toppingPrice) throws DatabaseException
    {
        toppingMapper.createTopping(toppingFlavour, toppingPrice);
    }

    @Override
    public void setBottomPrice(Bottom bottom, double price) throws DatabaseException
    {
        Bottom updatedBottom = new Bottom(bottom.getBottomId(),bottom.getName(), price);

        bottomMapper.updateBottom(updatedBottom);
    }

    @Override
    public void setToppingPrice(Topping topping, double price) throws DatabaseException
    {
        Topping newTopping = new Topping(topping.getToppingId(), topping.getName(), price);
        toppingMapper.updateTopping(newTopping);
    }

    @Override
    public void renameBottomFlavour(Bottom bottom, String flavour) throws DatabaseException
    {
        Bottom updatedBottom = new Bottom(bottom.getBottomId(), flavour, bottom.getPrice());
        bottomMapper.updateBottom(updatedBottom);
    }

    @Override
    public void renameToppingFlavour(Topping topping, String flavour) throws DatabaseException
    {
        Topping newTopping = new Topping(topping.getToppingId(), flavour, topping.getPrice());
        toppingMapper.updateTopping(newTopping);
    }

    @Override
    public void deleteBottomFlavour(Bottom bottom) throws DatabaseException
    {
        bottomMapper.deleteBottom(bottom.getBottomId());
    }

    @Override
    public void deleteToppingFlavour(Topping topping) throws DatabaseException
    {
        toppingMapper.deleteTopping(topping.getToppingId());
    }
}

