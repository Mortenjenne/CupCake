package app.services;

import app.dto.UserDTO;
import app.entities.Order;
import app.entities.OrderLine;
import app.entities.User;
import app.exceptions.DatabaseException;
import app.persistence.OrderLineMapper;
import app.persistence.OrderMapper;
import app.persistence.UserMapper;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OrderServiceImpl implements OrderService
{
    private OrderMapper orderMapper;
    private UserMapper userMapper;

    public OrderServiceImpl(OrderMapper orderMapper, UserMapper userMapper)
    {
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
    }

    @Override
    public Order createOrder(UserDTO userDTO, List<OrderLine> orderLines, LocalDateTime pickUpDate, boolean payNow) throws DatabaseException
    {
        double totalPrice = calculateTotalPrice(orderLines);
        int userId = userDTO.getUserId();

        if (userId == 0)
        {
            if (payNow)
            {
                throw new DatabaseException("Gæster kan kun betale ved afhentning");
            }

        } else
        {
            User user = userMapper.getUserById(userId);

            if (payNow)
            {
                if (user.getBalance() < totalPrice) {
                    throw new DatabaseException("Utilstrækkelig saldo. Dit beløb: " + user.getBalance() +
                            " kr. Ordretotal: " + totalPrice + " kr.");
                }
                double newUserBalance = user.getBalance() - totalPrice;
                userMapper.updateUserBalance(userId, newUserBalance);
            }

            userDTO = buildUserDTO(user);
        }

        Order order = new Order(
                0,
                userDTO,
                LocalDateTime.now(),
                pickUpDate,
                payNow,
                orderLines,
                totalPrice
        );

        return orderMapper.createOrder(order);
    }


    @Override
    public List<Order> getAllUserOrders(UserDTO userDTO) throws DatabaseException
    {
        List<Order> userOrders = orderMapper.getOrdersByUserId(userDTO);
        if (userOrders == null)
        {
            return new ArrayList<>();
        }

        return userOrders.stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteOrder(int orderId, int adminId, boolean refundUser) throws DatabaseException
    {
        validateUserIsAdmin(adminId);

        if(refundUser)
        {
            refundUserTotalOrderPrice(orderId);
        }
        return orderMapper.deleteOrder(orderId);
    }

    @Override
    public boolean updateOrderPaymentStatus(int orderId, boolean paid, int adminId) throws DatabaseException
    {
        validateUserIsAdmin(adminId);
        return orderMapper.updateOrderStatus(orderId, paid);
    }

    @Override
    public List<Order> getAllOrdersByStatusPaid(int adminId) throws DatabaseException
    {
        validateUserIsAdmin(adminId);
        return getAllOrdersByStatus(true);
    }

    @Override
    public List<Order> getAllOrdersByStatusNotPaid(int adminId) throws DatabaseException
    {
        validateUserIsAdmin(adminId);
        return getAllOrdersByStatus(false);
    }

    @Override
    public List<Order> getAllOrders(int adminId) throws DatabaseException
    {
        validateUserIsAdmin(adminId);
        List<Order> orders = orderMapper.getAllOrders();

        if (orders == null)
        {
            return new ArrayList<>();
        }
        return orderMapper.getAllOrders().stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .collect(Collectors.toList());
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

    private List<Order> getAllOrdersByStatus(boolean paid) throws DatabaseException
    {
        return orderMapper.getAllOrders().stream()
                .filter(order -> order.isPaid() == paid)
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .collect(Collectors.toList());
    }

    private void refundUserTotalOrderPrice(int orderId) throws DatabaseException
    {
        List<Order> allOrders = orderMapper.getAllOrders();
        Order orderToCancel = allOrders.stream()
                .filter(order -> order.getOrderId() == orderId)
                .findFirst()
                .orElseThrow(() -> new DatabaseException("Ordre ikke fundet"));

        if(orderToCancel.isPaid())
        {
            int userId = orderToCancel.getUserDTO().getUserId();
            User user = userMapper.getUserById(userId);
            double newBalance = user.getBalance() + orderToCancel.getTotalPrice();
            userMapper.updateUserBalance(userId, newBalance);
        }
    }

    private void validateUserIsAdmin(int adminId) throws DatabaseException
    {
        User admin = userMapper.getUserById(adminId);
        if(!admin.isAdmin())
        {
            throw new DatabaseException("Denne handling kan kun tilgås af en adminstrator. Log venligst ind med en adminstrator bruger");
        }

    }

    private void validateUserBalance(boolean payNow, User user, double totalPrice) throws DatabaseException
    {
        if (payNow && user.getBalance() < totalPrice)
        {
            throw new DatabaseException("Utilstrækkelig saldo. Dit beløb: " + user.getBalance() +
                    " kr. Ordretotal: " + totalPrice + " kr.");
        }
    }

    private double calculateTotalPrice(List<OrderLine> orderLines)
    {
        return orderLines.stream()
                .mapToDouble(OrderLine::getOrderLinePrice)
                .sum();
    }

    private UserDTO buildUserDTO(User user)
    {
        return new UserDTO(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStreet(),
                user.getZipCode(),
                user.getCity(),
                user.getBalance()
        );
    }
}
