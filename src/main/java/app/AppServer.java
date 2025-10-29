package app;

import app.config.ThymeleafConfig;
import app.controllers.*;
import app.persistence.*;
import app.services.*;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;


public class AppServer
{
    private final Javalin app;

    public AppServer(ConnectionPool connectionPool)
    {
        // Initializing Javalin and Jetty webserver
        app = Javalin.create(config ->
        {
            config.staticFiles.add("/public");
            config.fileRenderer(new JavalinThymeleaf(ThymeleafConfig.templateEngine()));
            config.staticFiles.add("/templates");
        });

        // Routing
        UserMapper userMapper = new UserMapper(connectionPool);
        UserService userService = new UserServiceImpl(userMapper);
        UserController userController = new UserController(userService);

        BottomMapper bottomMapper = new BottomMapper(connectionPool);
        ToppingMapper toppingMapper = new ToppingMapper(connectionPool);
        ShoppingService shoppingService = new ShoppingServiceImpl(bottomMapper, toppingMapper);
        ShoppingController shoppingController = new ShoppingController(shoppingService);

        CupcakeService cupcakeService = new CupcakeServiceImpl(bottomMapper, toppingMapper);
        CupcakeController cupcakeController = new CupcakeController(cupcakeService);

        OrderLineMapper orderLineMapper = new OrderLineMapper(connectionPool);
        OrderMapper orderMapper = new OrderMapper(connectionPool, orderLineMapper);
        OrderService orderService = new OrderServiceImpl(orderMapper, orderLineMapper, userMapper);

        OrderController orderController = new OrderController(orderService);
        AdminController adminController = new AdminController(userService, orderService);
        CheckoutController checkoutController = new CheckoutController(orderService, userService);

        shoppingController.addRoutes(app);
        userController.addRoutes(app);
        orderController.addRoutes(app);
        adminController.addRoutes(app);
        cupcakeController.addRoutes(app);
        checkoutController.addRoutes(app);

    }

    public void start(int port)
    {
        app.start(port);
    }
}
