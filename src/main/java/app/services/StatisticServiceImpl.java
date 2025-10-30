package app.services;

import app.entities.Order;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.OrderMapper;
import app.persistence.UserMapper;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

public class StatisticServiceImpl implements StatisticService
{
    private UserMapper userMapper;
    private OrderMapper orderMapper;

    public StatisticServiceImpl(UserMapper userMapper, OrderMapper orderMapper)
    {
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
    }

    @Override
    public double getTotalRevenue(int adminId) throws DatabaseException
    {
        validateUserIsAdmin(adminId);
        List<Order> orders = orderMapper.getAllOrders();

        if (orders == null)
        {
            return 0.0;
        }
        return orders.stream()
                .filter(Order::isPaid)
                .mapToDouble(Order::getTotalPrice)
                .sum();
    }

    @Override
    public double getMonthlyRevenue(int adminId, YearMonth month) throws DatabaseException
    {
        validateUserIsAdmin(adminId);
        List<Order> orders = orderMapper.getAllOrders();

        if (orders == null)
        {
            return 0.0;
        }

        return orders.stream()
                .filter(Order::isPaid)
                .filter(order -> YearMonth.from(order.getOrderDate()).equals(month))
                .mapToDouble(Order::getTotalPrice)
                .sum();
    }

    @Override
    public double getAverageOrderValue(int adminId) throws DatabaseException
    {
        validateUserIsAdmin(adminId);
        List<Order> orders = orderMapper.getAllOrders();

        if (orders == null || orders.isEmpty())
        {
            return 0.0;
        }

        List<Order> paidOrders = orders.stream()
                .filter(Order::isPaid)
                .collect(Collectors.toList());

        if (paidOrders.isEmpty())
        {
            return 0.0;
        }

        double totalRevenue = paidOrders.stream()
                .mapToDouble(Order::getTotalPrice)
                .sum();

        return totalRevenue / paidOrders.size();
    }

    private void validateUserIsAdmin(int adminId) throws DatabaseException
    {
        User admin = userMapper.getUserById(adminId);
        if(!admin.isAdmin())
        {
            throw new DatabaseException("Denne handling kan kun tilgås af en adminstrator. Log venligst ind med en adminstrator bruger");
        }

    }
}
