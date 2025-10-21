package app;

import app.config.ThymeleafConfig;

import app.controllers.UserController;
import app.persistence.ConnectionPool;
import app.persistence.UserMapper;
import app.services.UserService;
import app.services.UserServiceImpl;
import io.javalin.Javalin;//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
import io.javalin.rendering.template.JavalinThymeleaf;

import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String URL = "jdbc:postgresql://localhost:5432/%s?currentSchema=public";
    private static final String DB = "cupcake";

    private static final ConnectionPool connectionPool = ConnectionPool.getInstance(USER, PASSWORD, URL, DB);

    public static void main(String[] args) {
        // Initializing Javalin and Jetty webserver

        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            config.fileRenderer(new JavalinThymeleaf(ThymeleafConfig.templateEngine()));
            config.staticFiles.add("/templates");
        }).start(7070);

        // Routing

        app.get("/", ctx -> {
            ctx.render("index.html");
        });

        UserMapper userMapper = new UserMapper(connectionPool);
        UserService userService = new UserServiceImpl(userMapper);
        UserController userController = new UserController(userService);
        userController.registerRoutes(app);

    }
}