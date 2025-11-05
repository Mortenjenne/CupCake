package app;

import app.config.ThymeleafConfig;

import app.controllers.*;
import app.persistence.*;
import app.services.*;
import io.javalin.Javalin;//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
import io.javalin.rendering.template.JavalinThymeleaf;

import java.util.logging.Logger;

public class Main
{

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static final String USER = System.getenv("JDBC_USER") != null ?
            System.getenv("JDBC_USER") : "postgres";

    private static final String PASSWORD = System.getenv("JDBC_PASSWORD") != null ?
            System.getenv("JDBC_PASSWORD") : "postgres";

    private static final String URL = System.getenv("JDBC_CONNECTION_STRING") != null ?
            System.getenv("JDBC_CONNECTION_STRING") : "jdbc:postgresql://localhost:5432/%s?currentSchema=public";

    private static final String DB = System.getenv("JDBC_DB") != null ?
            System.getenv("JDBC_DB") : "cupcake";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);

    public static void main(String[] args)
    {
        // Initializing Javalin and Jetty webserver

        Javalin app = Javalin.create(config ->
        {
            config.staticFiles.add("/public");
            config.fileRenderer(new JavalinThymeleaf(ThymeleafConfig.templateEngine()));
            config.staticFiles.add("/templates");
        }).start(7070);

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
}
