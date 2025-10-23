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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class OrderServiceImpl implements OrderService
{
    private OrderMapper orderMapper;
    private UserMapper userMapper;
    private OrderLineMapper orderLineMapper;

    public OrderServiceImpl(OrderMapper orderMapper, UserMapper userMapper, OrderLineMapper orderLineMapper)
    {
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.orderLineMapper = orderLineMapper;
    }

    @Override
    public Order createOrder(int userId, List<OrderLine> orderLines, LocalDateTime pickUpDate, boolean payNow) throws DatabaseException
    {
        User user = userMapper.getUserById(userId);
        UserDTO userDTO = buildUserDTO(user);
        double totalPrice = calculateTotalPrice(orderLines);

        validateUserBalance(payNow, user, totalPrice);

        Order order = new Order(
                0,
                userDTO,
                LocalDateTime.now(),
                pickUpDate,
                payNow,
                orderLines,
                totalPrice
        );

        if(payNow)
        {
            double newUserBalance = user.getBalance() - totalPrice;
            userMapper.updateUserBalance(userId, newUserBalance);
        }
        return orderMapper.createOrder(order);
        }


    @Override
    public List<Order> getAllUserOrders(UserDTO userDTO) throws DatabaseException
    {
        List<Order> userOrders = orderMapper.getOrdersByUserId(userDTO);
        if(userOrders == null || userOrders.isEmpty())
        {
            throw new DatabaseException("Ingen ordre oprette nu!");
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
            refundUser(orderId);
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
    public List<Order> getAllOrdersByStatusPaid() throws DatabaseException
    {
        return List.of();
    }

    @Override
    public List<Order> getAllOrdersByStatusNotPaid() throws DatabaseException
    {
        return List.of();
    }

    @Override
    public List<Order> getAllOrders(int adminId) throws DatabaseException
    {
        return List.of();
    }

    @Override
    public double getTotalRevenue(int adminId) throws DatabaseException
    {
        return 0;
    }

    private void refundUser(int orderId) throws DatabaseException
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
                user.getFirstName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStreet(),
                user.getZipCode(),
                user.getCity(),
                user.getBalance()
        );
    }
}
