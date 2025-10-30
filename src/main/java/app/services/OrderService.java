package app.services;

import app.dto.UserDTO;
import app.entities.Order;
import app.entities.OrderLine;
import app.exceptions.DatabaseException;

import javax.xml.crypto.Data;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

public interface OrderService
{
    public Order createOrder(UserDTO userDTO, List<OrderLine> orderlines, LocalDateTime pickUpDate, boolean payNow, double deliveryPrice) throws DatabaseException;
    public List<Order> getAllUserOrders(UserDTO userDTO) throws DatabaseException;

    public boolean deleteOrder(int orderId, int adminId, boolean refundUser) throws DatabaseException;
    public boolean updateOrderPaymentStatus(int orderId, boolean paid, int adminId) throws DatabaseException;
    public List<Order> getAllOrdersByStatusPaid(int adminId) throws DatabaseException;
    public List<Order> getAllOrdersByStatusNotPaid(int adminId) throws DatabaseException;
    public List<Order> getAllOrders(int adminId) throws DatabaseException;
    public Order getOrderById(int orderId, int userId) throws DatabaseException;
    public List<OrderLine> getAllOrderLinesByOrderId(int orderId) throws DatabaseException;
    public List<Order> searchOrdersByOrderId(int orderId) throws DatabaseException;
    public List<Order> searchOrdersByName(String name) throws DatabaseException;
    public List<Order> searchOrdersByEmail(String email) throws DatabaseException;
    public List<Order> sortOrdersByPaymentStatus(List<Order> orders, boolean paid);
}
