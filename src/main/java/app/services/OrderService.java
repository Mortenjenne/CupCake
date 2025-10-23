package app.services;

import app.dto.UserDTO;
import app.entities.Order;
import app.entities.OrderLine;
import app.exceptions.DatabaseException;

import javax.xml.crypto.Data;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderService
{
    public Order createOrder(int userId, List<OrderLine> orderlines, LocalDateTime pickUpDate, boolean payNow) throws DatabaseException;
    public List<Order> getAllUserOrders(UserDTO userDTO) throws DatabaseException;

    public boolean deleteOrder(int orderId, int adminId, boolean refundUser) throws DatabaseException;
    public boolean updateOrderPaymentStatus(int orderId, boolean paid, int adminId) throws DatabaseException;
    public List<Order> getAllOrdersByStatusPaid() throws DatabaseException;
    public List<Order> getAllOrdersByStatusNotPaid() throws DatabaseException;
    public List<Order> getAllOrders(int adminId) throws DatabaseException;
    public double getTotalRevenue(int adminId) throws DatabaseException;

}
