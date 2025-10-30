package app.services;

public class StandardDelivery implements DeliveryStrategy
{
    @Override
    public double getDeliveryPrice()
    {
        return 29.00;
    }
}
