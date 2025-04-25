package org.example.fooddelivery;

import org.example.fooddelivery.model.*;
import org.example.fooddelivery.repository.*;
import org.example.fooddelivery.util.*;
import org.example.fooddelivery.config.AppConfig;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

public class ConsoleApplication {
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private User currentUser;

    public ConsoleApplication() {
        this.userRepository = new UserRepository();
        this.restaurantRepository = new RestaurantRepository();
        this.productRepository = new ProductRepository();
        this.orderRepository = new OrderRepository();
    }

    public void start() {
        while (true) {
            ConsoleUtils.clearScreen();
            if (currentUser == null) {
                showLoginMenu();
            } else {
                showMainMenu();
            }
        }
    }

    private void showLoginMenu() {
        ConsoleUtils.printHeader("Food Delivery System");
        ConsoleUtils.printMenu(
                "Login",
                "Register",
                "Exit"
        );

        switch (ConsoleUtils.readChoice(1, 3)) {
            case 1:
                login();
                break;
            case 2:
                register();
                break;
            case 3:
                System.out.println("\nGoodbye!");
                System.exit(0);
        }
    }

    private void login() {
        ConsoleUtils.printHeader("Login");
        String username = ConsoleUtils.readLine("Username: ");
        String password = ConsoleUtils.readLine("Password: ");

        currentUser = userRepository.authenticate(username, password);
        if (currentUser != null) {
            ConsoleUtils.printSuccess("Login successful!");
        } else {
            ConsoleUtils.printError("Invalid username or password!");
            ConsoleUtils.waitForEnter();
        }
    }

    private void register() {
        ConsoleUtils.printHeader("Register New User");
        String username = ConsoleUtils.readLine("Username: ");
        String password = ConsoleUtils.readLine("Password: ");

        ConsoleUtils.printMenu(
                "Client",
                "Employee",
                "Deliverer"
        );

        int roleChoice = ConsoleUtils.readChoice(1, 3);
        User.UserRole role = User.UserRole.values()[roleChoice - 1];

        if (!PasswordUtils.isPasswordValid(password)) {
            ConsoleUtils.printError("Password is not valid!");
            System.out.println(PasswordUtils.getPasswordRequirements());
            ConsoleUtils.waitForEnter();
            return;
        }

        User newUser = new User(0, username, password, role);
        if (userRepository.registerUser(newUser)) {
            ConsoleUtils.printSuccess("Registration successful! Please login.");
        } else {
            ConsoleUtils.printError("Registration failed! Username might already exist.");
        }
        ConsoleUtils.waitForEnter();
    }

    private void showMainMenu() {
        while (currentUser != null) {
            ConsoleUtils.clearScreen();
            ConsoleUtils.printHeader("Main Menu - " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");

            switch (currentUser.getRole()) {
                case CLIENT:
                    showClientMenu();
                    break;
                case EMPLOYEE:
                    showEmployeeMenu();
                    break;
                case DELIVERER:
                    showDelivererMenu();
                    break;
            }
        }
    }

    private void showClientMenu() {
        ConsoleUtils.printMenu(
                "View Restaurants",
                "Place Order",
                "View My Orders",
                "Logout"
        );

        switch (ConsoleUtils.readChoice(1, 4)) {
            case 1:
                viewRestaurants();
                break;
            case 2:
                placeOrder();
                break;
            case 3:
                viewMyOrders();
                break;
            case 4:
                logout();
                break;
        }
    }

    private void showEmployeeMenu() {
        ConsoleUtils.printMenu(
                "Manage Restaurants",
                "Manage Products",
                "View Orders",
                "Generate Reports",
                "Logout"
        );

        switch (ConsoleUtils.readChoice(1, 5)) {
            case 1:
                manageRestaurants();
                break;
            case 2:
                manageProducts();
                break;
            case 3:
                viewAllOrders();
                break;
            case 4:
                generateReports();
                break;
            case 5:
                logout();
                break;
        }
    }

    private void showDelivererMenu() {
        ConsoleUtils.printMenu(
                "View Available Orders",
                "View My Deliveries",
                "View Earnings",
                "Logout"
        );

        switch (ConsoleUtils.readChoice(1, 4)) {
            case 1:
                viewAvailableOrders();
                break;
            case 2:
                viewMyDeliveries();
                break;
            case 3:
                viewEarnings();
                break;
            case 4:
                logout();
                break;
        }
    }

    private void viewRestaurants() {
        ConsoleUtils.printHeader("Restaurants");
        List<Restaurant> restaurants = restaurantRepository.getAllRestaurants();

        ConsoleUtils.printTableHeader("ID", "Name", "Address", "Phone");
        for (Restaurant restaurant : restaurants) {
            ConsoleUtils.printTableRow(
                    String.valueOf(restaurant.getId()),
                    restaurant.getName(),
                    restaurant.getAddress(),
                    restaurant.getPhone()
            );
        }
        ConsoleUtils.printTableFooter();
        ConsoleUtils.waitForEnter();
    }

    private void placeOrder() {
        ConsoleUtils.printHeader("Place Order");
        viewRestaurants();

        int restaurantId = ConsoleUtils.readChoice(1, restaurantRepository.getAllRestaurants().size());
        Restaurant restaurant = restaurantRepository.getRestaurantById(restaurantId);

        if (restaurant == null) {
            ConsoleUtils.printError("Restaurant not found!");
            return;
        }

        List<Product> products = productRepository.getProductsByRestaurantId(restaurantId);
        List<OrderItem> orderItems = new ArrayList<>();

        while (true) {
            ConsoleUtils.printTableHeader("ID", "Name", "Price", "Description");
            for (Product product : products) {
                ConsoleUtils.printTableRow(
                        String.valueOf(product.getId()),
                        product.getName(),
                        product.getPrice().toString(),
                        product.getDescription()
                );
            }
            ConsoleUtils.printTableFooter();

            if (!ConsoleUtils.readBoolean("Add product to order?")) {
                break;
            }

            int selectedIndex = ConsoleUtils.readChoice(1, products.size());
            int quantity = ConsoleUtils.readChoice(1, 10);

            Product product = products.get(selectedIndex - 1);
            orderItems.add(new OrderItem(0, 0, product, quantity));
        }


        if (orderItems.isEmpty()) {
            ConsoleUtils.printError("Order must contain at least one item!");
            return;
        }

        Order order = new Order(0, currentUser, restaurant, orderItems, LocalDateTime.now(), "PENDING");
        if (orderRepository.createOrder(order)) {
            ConsoleUtils.printSuccess("Order placed successfully!");
        } else {
            ConsoleUtils.printError("Failed to place order!");
        }
        ConsoleUtils.waitForEnter();
    }

    private void viewMyOrders() {
        ConsoleUtils.printHeader("My Orders");
        List<Order> orders = orderRepository.getOrdersByUserId(currentUser.getId());

        if (orders.isEmpty()) {
            System.out.println("No orders found.");
            ConsoleUtils.waitForEnter();
            return;
        }

        ConsoleUtils.printTableHeader("ID", "Restaurant", "Status", "Date", "Total");
        for (Order order : orders) {
            ConsoleUtils.printTableRow(
                    String.valueOf(order.getId()),
                    order.getRestaurant().getName(),
                    order.getStatus(),
                    order.getOrderDate().toString(),
                    calculateOrderTotal(order).toString()
            );
        }
        ConsoleUtils.printTableFooter();
        ConsoleUtils.waitForEnter();
    }

    private void manageRestaurants() {
        while (true) {
            ConsoleUtils.printHeader("Manage Restaurants");
            ConsoleUtils.printMenu(
                    "View All Restaurants",
                    "Add Restaurant",
                    "Edit Restaurant",
                    "Delete Restaurant",
                    "Back"
            );

            switch (ConsoleUtils.readChoice(1, 5)) {
                case 1:
                    viewRestaurants();
                    break;
                case 2:
                    addRestaurant();
                    break;
                case 3:
                    editRestaurant();
                    break;
                case 4:
                    deleteRestaurant();
                    break;
                case 5:
                    return;
            }
        }
    }

    private void manageProducts() {
        while (true) {
            ConsoleUtils.printHeader("Manage Products");
            ConsoleUtils.printMenu(
                    "View All Products",
                    "Add Product",
                    "Edit Product",
                    "Delete Product",
                    "Back"
            );

            switch (ConsoleUtils.readChoice(1, 5)) {
                case 1:
                    viewAllProducts();
                    break;
                case 2:
                    addProduct();
                    break;
                case 3:
                    editProduct();
                    break;
                case 4:
                    deleteProduct();
                    break;
                case 5:
                    return;
            }
        }
    }

    private void viewAllOrders() {
        ConsoleUtils.printHeader("All Orders");
        List<Order> orders = orderRepository.getAllOrders();

        if (orders.isEmpty()) {
            System.out.println("No orders found.");
            ConsoleUtils.waitForEnter();
            return;
        }

        ConsoleUtils.printTableHeader("ID", "User", "Restaurant", "Status", "Date", "Total");
        for (Order order : orders) {
            ConsoleUtils.printTableRow(
                    String.valueOf(order.getId()),
                    order.getUser().getUsername(),
                    order.getRestaurant().getName(),
                    order.getStatus(),
                    order.getOrderDate().toString(),
                    calculateOrderTotal(order).toString()
            );
        }
        ConsoleUtils.printTableFooter();
        ConsoleUtils.waitForEnter();
    }

    private void generateReports() {
        ConsoleUtils.printHeader("Generate Reports");
        ConsoleUtils.printMenu(
                "Daily Sales Report",
                "Monthly Sales Report",
                "Popular Products Report",
                "Back"
        );

        switch (ConsoleUtils.readChoice(1, 4)) {
            case 1:
                generateDailySalesReport();
                break;
            case 2:
                generateMonthlySalesReport();
                break;
            case 3:
                generatePopularProductsReport();
                break;
            case 4:
                return;
        }
    }

    private void viewAvailableOrders() {
        ConsoleUtils.printHeader("Available Orders");
        List<Order> orders = orderRepository.getOrdersByStatus("PENDING");

        if (orders.isEmpty()) {
            System.out.println("No available orders found.");
            ConsoleUtils.waitForEnter();
            return;
        }

        ConsoleUtils.printTableHeader("No", "ID", "Restaurant", "Items", "Total");
        int index = 1;
        for (Order order : orders) {
            ConsoleUtils.printTableRow(
                    String.valueOf(index++),
                    String.valueOf(order.getId()),
                    order.getRestaurant().getName(),
                    String.valueOf(order.getItems().size()),
                    calculateOrderTotal(order).toString()
            );
        }
        ConsoleUtils.printTableFooter();

        if (ConsoleUtils.readBoolean("Accept an order?")) {
            int selectedIndex = ConsoleUtils.readChoice(1, orders.size());
            Order selectedOrder = orders.get(selectedIndex - 1);

            if (orderRepository.updateOrderStatus(selectedOrder.getId(), "DELIVERED", currentUser.getId())) {
                ConsoleUtils.printSuccess("Order accepted successfully!");
            } else {
                ConsoleUtils.printError("Failed to accept order!");
            }
        }

        ConsoleUtils.waitForEnter();
    }



    private void viewMyDeliveries() {
        ConsoleUtils.printHeader("My Deliveries");
        List<Order> deliveries = orderRepository.getDeliveriesByDelivererId(currentUser.getId());

        if (deliveries.isEmpty()) {
            System.out.println("No deliveries found.");
            ConsoleUtils.waitForEnter();
            return;
        }

        ConsoleUtils.printTableHeader("ID", "Restaurant", "Status", "Date", "Total");
        for (Order delivery : deliveries) {
            ConsoleUtils.printTableRow(
                    String.valueOf(delivery.getId()),
                    delivery.getRestaurant().getName(),
                    delivery.getStatus(),
                    delivery.getOrderDate().toString(),
                    calculateOrderTotal(delivery).toString()
            );
        }
        ConsoleUtils.printTableFooter();
        ConsoleUtils.waitForEnter();
    }

    private void viewEarnings() {
        ConsoleUtils.printHeader("My Earnings");
        BigDecimal totalEarnings = orderRepository.calculateDelivererEarnings(currentUser.getId());
        System.out.println("Total earnings: $" + totalEarnings);
        ConsoleUtils.waitForEnter();
    }

    private void logout() {
        if (ConsoleUtils.readBoolean("Are you sure you want to logout?")) {
            currentUser = null;
            ConsoleUtils.printSuccess("Logged out successfully!");
            ConsoleUtils.waitForEnter();
        }
    }

    private BigDecimal calculateOrderTotal(Order order) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            total = total.add(item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return total;
    }

    private void addRestaurant() {
        ConsoleUtils.printHeader("Add Restaurant");
        String name = ConsoleUtils.readLine("Name: ");
        String address = ConsoleUtils.readLine("Address: ");
        String phone = ConsoleUtils.readLine("Phone: ");

        Restaurant restaurant = new Restaurant(0, name, address, phone);
        if (restaurantRepository.createRestaurant(restaurant)) {
            ConsoleUtils.printSuccess("Restaurant added successfully!");
        } else {
            ConsoleUtils.printError("Failed to add restaurant!");
        }
        ConsoleUtils.waitForEnter();
    }

    private void editRestaurant() {
        viewRestaurants();
        int id = ConsoleUtils.readChoice(1, restaurantRepository.getAllRestaurants().size());
        Restaurant restaurant = restaurantRepository.getRestaurantById(id);

        if (restaurant == null) {
            ConsoleUtils.printError("Restaurant not found!");
            return;
        }

        String name = ConsoleUtils.readLine("Name (" + restaurant.getName() + "): ");
        String address = ConsoleUtils.readLine("Address (" + restaurant.getAddress() + "): ");
        String phone = ConsoleUtils.readLine("Phone (" + restaurant.getPhone() + "): ");

        restaurant.setName(name.isEmpty() ? restaurant.getName() : name);
        restaurant.setAddress(address.isEmpty() ? restaurant.getAddress() : address);
        restaurant.setPhone(phone.isEmpty() ? restaurant.getPhone() : phone);

        if (restaurantRepository.updateRestaurant(restaurant)) {
            ConsoleUtils.printSuccess("Restaurant updated successfully!");
        } else {
            ConsoleUtils.printError("Failed to update restaurant!");
        }
        ConsoleUtils.waitForEnter();
    }

    private void deleteRestaurant() {
        viewRestaurants();
        int id = ConsoleUtils.readChoice(1, restaurantRepository.getAllRestaurants().size());

        if (ConsoleUtils.readBoolean("Are you sure you want to delete this restaurant?")) {
            if (restaurantRepository.deleteRestaurant(id)) {
                ConsoleUtils.printSuccess("Restaurant deleted successfully!");
            } else {
                ConsoleUtils.printError("Failed to delete restaurant!");
            }
        }
        ConsoleUtils.waitForEnter();
    }

    private void viewAllProducts() {
        ConsoleUtils.printHeader("All Products");
        List<Product> products = productRepository.getAllProducts();

        ConsoleUtils.printTableHeader("ID", "Name", "Price", "Restaurant", "Description");
        for (Product product : products) {
            if (product == null) {
                continue;
            }

            ConsoleUtils.printTableRow(
                    String.valueOf(product.getId()),
                    product.getName(),
                    product.getPrice().toString(),
                    product.getRestaurant() != null ? product.getRestaurant().getName() : "N/A",
                    product.getDescription()
            );
        }
        ConsoleUtils.printTableFooter();
        ConsoleUtils.waitForEnter();
    }

    private void addProduct() {
        ConsoleUtils.printHeader("Add Product");
        viewRestaurants();
        int restaurantId = ConsoleUtils.readChoice(1, restaurantRepository.getAllRestaurants().size());
        Restaurant restaurant = restaurantRepository.getRestaurantById(restaurantId);

        if (restaurant == null) {
            ConsoleUtils.printError("Restaurant not found!");
            return;
        }

        String name = ConsoleUtils.readLine("Name: ");
        BigDecimal price = new BigDecimal(ConsoleUtils.readLine("Price: "));
        String description = ConsoleUtils.readLine("Description: ");

        Product product = new Product(0, name, price, description, restaurant);
        if (productRepository.createProduct(product)) {
            ConsoleUtils.printSuccess("Product added successfully!");
        } else {
            ConsoleUtils.printError("Failed to add product!");
        }
        ConsoleUtils.waitForEnter();
    }

    private void editProduct() {
        viewAllProducts();
        int id = ConsoleUtils.readChoice(1, productRepository.getAllProducts().size());
        Product product = productRepository.getProductById(id);

        if (product == null) {
            ConsoleUtils.printError("Product not found!");
            return;
        }

        String name = ConsoleUtils.readLine("Name (" + product.getName() + "): ");
        String priceStr = ConsoleUtils.readLine("Price (" + product.getPrice() + "): ");
        String description = ConsoleUtils.readLine("Description (" + product.getDescription() + "): ");

        product.setName(name.isEmpty() ? product.getName() : name);
        product.setPrice(priceStr.isEmpty() ? product.getPrice() : new BigDecimal(priceStr));
        product.setDescription(description.isEmpty() ? product.getDescription() : description);

        if (productRepository.updateProduct(product)) {
            ConsoleUtils.printSuccess("Product updated successfully!");
        } else {
            ConsoleUtils.printError("Failed to update product!");
        }
        ConsoleUtils.waitForEnter();
    }

    private void deleteProduct() {
        viewAllProducts();
        int id = ConsoleUtils.readChoice(1, productRepository.getAllProducts().size());

        if (ConsoleUtils.readBoolean("Are you sure you want to delete this product?")) {
            if (productRepository.deleteProduct(id)) {
                ConsoleUtils.printSuccess("Product deleted successfully!");
            } else {
                ConsoleUtils.printError("Failed to delete product!");
            }
        }
        ConsoleUtils.waitForEnter();
    }

    private void generateDailySalesReport() {
        ConsoleUtils.printHeader("Daily Sales Report");
        LocalDate date = LocalDate.now();
        List<Order> orders = orderRepository.getOrdersByDate(date);
        printSalesReport(orders, "Daily Sales Report - " + date);
    }

    private void generateMonthlySalesReport() {
        ConsoleUtils.printHeader("Monthly Sales Report");
        LocalDate date = LocalDate.now().withDayOfMonth(1);
        List<Order> orders = orderRepository.getOrdersByMonth(date);
        printSalesReport(orders, "Monthly Sales Report - " + date.getMonth() + " " + date.getYear());
    }

    private void generatePopularProductsReport() {
        ConsoleUtils.printHeader("Popular Products Report");
        List<Product> products = productRepository.getPopularProducts();

        ConsoleUtils.printTableHeader("ID", "Name", "Total Orders", "Revenue");
        for (Product product : products) {
            ConsoleUtils.printTableRow(
                    String.valueOf(product.getId()),
                    product.getName(),
                    String.valueOf(orderRepository.getProductOrderCount(product.getId())),
                    orderRepository.getProductRevenue(product.getId()).toString()
            );
        }
        ConsoleUtils.printTableFooter();
        ConsoleUtils.waitForEnter();
    }

    private void printSalesReport(List<Order> orders, String title) {
        ConsoleUtils.printHeader(title);

        if (orders.isEmpty()) {
            System.out.println("No orders found for this period.");
            ConsoleUtils.waitForEnter();
            return;
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalOrders = orders.size();

        ConsoleUtils.printTableHeader("ID", "Restaurant", "Items", "Total");
        for (Order order : orders) {
            BigDecimal orderTotal = calculateOrderTotal(order);
            totalRevenue = totalRevenue.add(orderTotal);

            ConsoleUtils.printTableRow(
                    String.valueOf(order.getId()),
                    order.getRestaurant().getName(),
                    String.valueOf(order.getItems().size()),
                    orderTotal.toString()
            );
        }
        ConsoleUtils.printTableFooter();

        System.out.println("\nSummary:");
        System.out.println("Total Orders: " + totalOrders);
        System.out.println("Total Revenue: $" + totalRevenue);

        ConsoleUtils.waitForEnter();
    }

    public static void main(String[] args) {
        new ConsoleApplication().start();
    }
}
