package app.entities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingCartTest
{
    private ShoppingCart shoppingCart;
    private Cupcake cupcake;
    private Cupcake cupcakeTwo;
    private OrderLine orderLineOne;
    private OrderLine orderLineTwo;

    @BeforeEach
    void setUp()
    {
        shoppingCart = new ShoppingCart();

        Bottom vanilla = new Bottom(0, "vanilla", 6);
        Topping chocolate = new Topping(0, "chocolate", 6);
        double price = vanilla.getPrice() + chocolate.getPrice();
        cupcake = new Cupcake(vanilla, chocolate, price);

        Bottom choco = new Bottom(1, "chocolate", 5);
        Topping vanill = new Topping(1, "vanilla", 5);
        double priceTwo = choco.getPrice() + vanill.getPrice();
        cupcakeTwo = new Cupcake(choco, vanill, priceTwo);

        orderLineOne = new OrderLine(cupcake, 3);
        orderLineTwo = new OrderLine(cupcakeTwo, 5);
    }

    @AfterEach
    void tearDown()
    {
        shoppingCart = null;
    }

    @Test
    void addToCart()
    {
        // Act
        shoppingCart.addOrderLineToCart(cupcake, 2);
        shoppingCart.addOrderLineToCart(cupcakeTwo, 2);

        // Assert
        assertNotNull(shoppingCart.getShoppingCart());
    }

    @Test
    @DisplayName("AddMoreThanOneToCart")
    void addToCartTwo()
    {
        // Act
        shoppingCart.addOrderLineToCart(cupcake, 2);
        shoppingCart.addOrderLineToCart(cupcakeTwo, 2);
        shoppingCart.addOrderLineToCart(cupcake, 1);
        shoppingCart.addOrderLineToCart(cupcakeTwo, 3);

        // Assert
        assertEquals(2, shoppingCart.getShoppingCart().size());
    }

    @Test
    @DisplayName("AddMoreThanOneToCart_CheckQuantity")
    void addToCartThree()
    {
        // Act
        shoppingCart.addOrderLineToCart(cupcake, 2);
        shoppingCart.addOrderLineToCart(cupcakeTwo, 2);
        shoppingCart.addOrderLineToCart(cupcake, 1);
        shoppingCart.addOrderLineToCart(cupcakeTwo, 3);

        // Assert
        assertEquals(3, shoppingCart.getShoppingCart().get(0).getQuantity());
        assertEquals(5, shoppingCart.getShoppingCart().get(1).getQuantity());
        assertNotEquals(4, shoppingCart.getShoppingCart().get(1).getQuantity());
    }

    @Test
    @DisplayName("AddMoreThanOneToCart_CheckQuantityNotEquals")
    void addToCartFour()
    {
        // Act
        shoppingCart.addOrderLineToCart(cupcake, 2);
        shoppingCart.addOrderLineToCart(cupcakeTwo, 2);
        shoppingCart.addOrderLineToCart(cupcake, 1);
        shoppingCart.addOrderLineToCart(cupcakeTwo, 3);

        // Assert
        assertNotEquals(2, shoppingCart.getShoppingCart().get(0).getQuantity());
        assertNotEquals(4, shoppingCart.getShoppingCart().get(1).getQuantity());
    }

    @Test
    @DisplayName("Check removeFromCart equals")
    void removeFromCart()
    {
        // Arrange
        shoppingCart.addOrderLineToCart(cupcake, 2);
        shoppingCart.addOrderLineToCart(cupcakeTwo, 2);
        shoppingCart.addOrderLineToCart(cupcake, 1);
        shoppingCart.addOrderLineToCart(cupcakeTwo, 3);

        // Act
        shoppingCart.removeOrderLineFromCart(shoppingCart.getShoppingCart().get(1));

        // Assert
        assertEquals(orderLineOne, shoppingCart.getShoppingCart().get(0));
    }

    @Test
    @DisplayName("Check removeFromCart not equals")
    void removeFromCartTwo()
    {
        // Arrange
        shoppingCart.addOrderLineToCart(cupcake, 2);
        shoppingCart.addOrderLineToCart(cupcakeTwo, 2);
        shoppingCart.addOrderLineToCart(cupcake, 1);
        shoppingCart.addOrderLineToCart(cupcakeTwo, 3);

        // Act
        shoppingCart.removeOrderLineFromCart(shoppingCart.getShoppingCart().get(1));

        // Assert
        assertNotEquals(new OrderLine(cupcake, 2), shoppingCart.getShoppingCart().get(0));
    }

    @Test
    void getTotalPrice()
    {
        // Act
        shoppingCart.addOrderLineToCart(cupcake, 2);
        shoppingCart.addOrderLineToCart(cupcakeTwo, 2);

        // Assert
        assertEquals(44.00, shoppingCart.getTotalOrderPrice());
    }

    @DisplayName("Remove One quantity from cart")
    @Test
    void removeOneFromCart()
    {
        // Act
        shoppingCart.addOrderLineToCart(cupcake, 2);
        shoppingCart.removeOneFromCupcakeQuantity(shoppingCart.getShoppingCart().get(0));

        // Assert
        assertEquals(1, shoppingCart.getShoppingCart().get(0).getQuantity());
    }

    @DisplayName("Remove One quantity from quantity with only 1")
    @Test
    void removeOneFromCarTwo()
    {
        // Act
        shoppingCart.addOrderLineToCart(cupcake, 1);
        shoppingCart.removeOneFromCupcakeQuantity(shoppingCart.getShoppingCart().get(0));

        // Assert
        assertTrue(shoppingCart.getShoppingCart().isEmpty());
    }

    @DisplayName("add One quantity from cart")
    @Test
    void addOneToCart()
    {
        // Act
        shoppingCart.addOrderLineToCart(cupcake, 2);
        shoppingCart.addOneToCupcakeQuantity(shoppingCart.getShoppingCart().get(0));

        // Assert
        assertEquals(3, shoppingCart.getShoppingCart().get(0).getQuantity());
    }
}
