package app.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class Order
{
private int orderId;
private User user;
private LocalDateTime orderDate;
private LocalDateTime pickUpDate;
private boolean paid;
private List<OrderLine> orderlines;
private double totalPrice;
}
