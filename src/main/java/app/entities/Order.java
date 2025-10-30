package app.entities;

import app.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class Order
{
private int orderId;
private UserDTO userDTO;
private LocalDateTime orderDate;
private LocalDateTime pickUpDate;
private boolean paid;
private List<OrderLine> orderlines;
private double totalPrice;

public String getFormattedPrice()
{
    return String.format("%.2f", totalPrice );
}
}


