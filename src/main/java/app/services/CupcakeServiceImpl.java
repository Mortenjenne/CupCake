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
    public Topping getToppingById(int id) throws DatabaseException
    {
        return toppingMapper.getToppingById(id);
    }

    @Override
    public Bottom getBottomById(int id) throws DatabaseException
    {
        return bottomMapper.getBottomById(id);
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
    public void deleteBottomFlavour(Bottom bottom) throws DatabaseException
    {
        bottomMapper.deleteBottom(bottom.getBottomId());
    }

    @Override
    public void deleteToppingFlavour(Topping topping) throws DatabaseException
    {
        toppingMapper.deleteTopping(topping.getToppingId());
    }

    @Override
    public void updateTopping(Topping topping) throws DatabaseException
    {
        toppingMapper.updateTopping(topping);
    }

    @Override
    public void updateBottom(Bottom bottom) throws DatabaseException
    {
        bottomMapper.updateBottom(bottom);
    }
}

