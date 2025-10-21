package app.persistence;

import app.entities.Order;

import java.util.List;

public class OrderMapper
{
    private ConnectionPool connectionPool;

    public OrderMapper(ConnectionPool connectionPool)
    {
        this.connectionPool = connectionPool;
    }

    public Order createOrder(Order order)
    {
        return order;
    }

    public Order getOrderByOrderId(int orderId)
    {
        Order order = null;
        return order;
    }

    public List<Order> getOrderByUserId(int userId)
    {
        List<Order> orders = null;
        return orders;
    }

    public List<Order> getAllOrders()
    {
        List<Order> orders = null;
        return orders;
    }

    public boolean deleteOrder(int orderId)
    {
        boolean result = false;
        return result;
    }


}
