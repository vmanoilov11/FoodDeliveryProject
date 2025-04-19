package org.example.fooddelivery.repository;

import org.example.fooddelivery.model.*;
import org.example.fooddelivery.util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrderRepository {
    private final DatabaseConnection dbConnection;
    private final ProductRepository productRepository;

    public OrderRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
        this.productRepository = new ProductRepository();
    }

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        List<OrderTemp> tempOrders = new ArrayList<>();

        String sql = "SELECT * FROM orders ORDER BY order_date DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int userId = rs.getInt("user_id");
                int restaurantId = rs.getInt("restaurant_id");
                String status = rs.getString("status");
                LocalDateTime date = rs.getTimestamp("order_date").toLocalDateTime();
                BigDecimal total = BigDecimal.ZERO;

                tempOrders.add(new OrderTemp(id, userId, restaurantId, status, date, total));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return orders;
        }

        UserRepository userRepository = new UserRepository();
        RestaurantRepository restaurantRepository = new RestaurantRepository();

        for (OrderTemp temp : tempOrders) {
            User user = userRepository.getUserById(temp.userId);
            Restaurant restaurant = restaurantRepository.getRestaurantById(temp.restaurantId);

            Order order = new Order(temp.id, user, restaurant, new ArrayList<>(), temp.date, temp.status);
            loadOrderItems(order);
            orders.add(order);
        }

        return orders;
    }


    public List<Order> getOrdersByUserId(int userId) {
        List<Order> orders = new ArrayList<>();

        String sql = "SELECT * FROM orders WHERE user_id = ?";
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            List<Integer> restaurantIds = new ArrayList<>();
            List<Integer> orderIds = new ArrayList<>();
            List<LocalDateTime> orderDates = new ArrayList<>();
            List<String> statuses = new ArrayList<>();

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orderIds.add(rs.getInt("id"));
                    restaurantIds.add(rs.getInt("restaurant_id"));
                    orderDates.add(rs.getTimestamp("order_date").toLocalDateTime());
                    statuses.add(rs.getString("status"));
                }
            }

            User user = new UserRepository().getUserById(userId);
            RestaurantRepository restaurantRepo = new RestaurantRepository();

            for (int i = 0; i < orderIds.size(); i++) {
                Restaurant restaurant = restaurantRepo.getRestaurantById(restaurantIds.get(i));
                Order order = new Order(orderIds.get(i), user, restaurant, new ArrayList<>(), orderDates.get(i), statuses.get(i));
                orders.add(order);
            }

            for (Order order : orders) {
                loadOrderItems(order);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }


    public List<Order> getOrdersByStatus(String status) {
        List<Order> orders = new ArrayList<>();
        List<OrderTemp> tempOrders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status = ? ORDER BY order_date DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int userId = rs.getInt("user_id");
                    int restaurantId = rs.getInt("restaurant_id");
                    LocalDateTime orderDate = rs.getTimestamp("order_date").toLocalDateTime();

                    tempOrders.add(new OrderTemp(id, userId, restaurantId, status, orderDate, BigDecimal.ZERO));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return orders;
        }

        UserRepository userRepository = new UserRepository();
        RestaurantRepository restaurantRepository = new RestaurantRepository();

        for (OrderTemp temp : tempOrders) {
            User user = userRepository.getUserById(temp.userId);
            Restaurant restaurant = restaurantRepository.getRestaurantById(temp.restaurantId);

            Order order = new Order(temp.id, user, restaurant, new ArrayList<>(), temp.date, temp.status);
            loadOrderItems(order);
            orders.add(order);
        }

        return orders;
    }


    public List<Order> getDeliveriesByDelivererId(int delivererId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE deliverer_id = ? ORDER BY order_date DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, delivererId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                Order order = mapResultSetToOrder(rs);
                orders.add(order);
            }

            for (Order order : orders) {
                loadOrderItems(order);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getOrdersByDate(LocalDate date) {
        List<Order> orders = new ArrayList<>();
        List<OrderTemp> tempOrders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE DATE(order_date) = ? ORDER BY order_date DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    int userId = rs.getInt("user_id");
                    int restaurantId = rs.getInt("restaurant_id");
                    String status = rs.getString("status");
                    LocalDateTime orderDate = rs.getTimestamp("order_date").toLocalDateTime();

                    tempOrders.add(new OrderTemp(id, userId, restaurantId, status, orderDate, BigDecimal.ZERO));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return orders;
        }

        UserRepository userRepository = new UserRepository();
        RestaurantRepository restaurantRepository = new RestaurantRepository();

        for (OrderTemp temp : tempOrders) {
            User user = userRepository.getUserById(temp.userId);
            Restaurant restaurant = restaurantRepository.getRestaurantById(temp.restaurantId);

            Order order = new Order(temp.id, user, restaurant, new ArrayList<>(), temp.date, temp.status);
            loadOrderItems(order);
            orders.add(order);
        }

        return orders;
    }


    public List<Order> getOrdersByMonth(LocalDate date) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE YEAR(order_date) = ? AND MONTH(order_date) = ? ORDER BY order_date DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, date.getYear());
            stmt.setInt(2, date.getMonthValue());

            try (ResultSet rs = stmt.executeQuery()) {
                List<Order> tempOrders = new ArrayList<>();

                while (rs.next()) {
                    Order order = mapResultSetToOrder(rs);
                    tempOrders.add(order);
                }

                for (Order order : tempOrders) {
                    loadOrderItems(order);
                }

                orders.addAll(tempOrders);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }



    public boolean createOrder(Order order) {
        String sql = "INSERT INTO orders (user_id, restaurant_id, status, order_date) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, order.getUserId());
            stmt.setInt(2, order.getRestaurantId());
            stmt.setString(3, order.getStatus().toString());
            stmt.setTimestamp(4, Timestamp.valueOf(order.getOrderDate()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    int orderId = generatedKeys.getInt(1);
                    order.setId(orderId);
                    return createOrderItems(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "IN_PROGRESS");
            stmt.setInt(2, orderId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public BigDecimal calculateDelivererEarnings(int delivererId) {
        String sql = """
            SELECT COALESCE(SUM(
                CASE
                    WHEN o.status = 'DELIVERED' THEN
                        (SELECT SUM(p.price * oi.quantity) * 0.1
                         FROM order_items oi
                         JOIN products p ON oi.product_id = p.id
                         WHERE oi.order_id = o.id)
                    ELSE 0
                END
            ), 0) as total_earnings
            FROM orders o
            WHERE o.deliverer_id = ?
           """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, delivererId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("total_earnings");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    public int getProductOrderCount(int productId) {
        String sql = "SELECT COUNT(*) as count FROM order_items WHERE product_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public BigDecimal getProductRevenue(int productId) {
        String sql = """
            SELECT COALESCE(SUM(p.price * oi.quantity), 0) as revenue
            FROM order_items oi
            JOIN products p ON oi.product_id = p.id
            JOIN orders o ON oi.order_id = o.id
            WHERE oi.product_id = ? AND o.status = 'DELIVERED'
            """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getBigDecimal("revenue");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int userId = rs.getInt("user_id");
        int restaurantId = rs.getInt("restaurant_id");
        LocalDateTime orderDate = rs.getTimestamp("order_date").toLocalDateTime();
        String status = rs.getString("status");

        UserRepository userRepository = new UserRepository();
        RestaurantRepository restaurantRepository = new RestaurantRepository();

        User user = userRepository.getUserById(userId);
        Restaurant restaurant = restaurantRepository.getRestaurantById(restaurantId);

        return new Order(id, user, restaurant, new ArrayList<>(), orderDate, status);
    }

    private void loadOrderItems(Order order) {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";

        List<OrderItemRow> tempItems = new ArrayList<>();

        // Step 1: Load all rows from ResultSet into memory
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, order.getId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderItemRow row = new OrderItemRow(
                            rs.getInt("id"),
                            rs.getInt("order_id"),
                            rs.getInt("product_id"),
                            rs.getInt("quantity")
                    );
                    tempItems.add(row);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Step 2: Use the extracted data to create actual OrderItem objects
        for (OrderItemRow row : tempItems) {
            Product product = productRepository.getProductById(row.productId);
            OrderItem item = new OrderItem(row.id, row.orderId, product, row.quantity);
            order.addOrderItem(item);
        }
    }

    
    private static class OrderItemRow {
        int id;
        int orderId;
        int productId;
        int quantity;

        public OrderItemRow(int id, int orderId, int productId, int quantity) {
            this.id = id;
            this.orderId = orderId;
            this.productId = productId;
            this.quantity = quantity;
        }
    }

    private static class OrderTemp {
        int id, userId, restaurantId;
        String status;
        LocalDateTime date;
        BigDecimal total;

        public OrderTemp(int id, int userId, int restaurantId, String status, LocalDateTime date, BigDecimal total) {
            this.id = id;
            this.userId = userId;
            this.restaurantId = restaurantId;
            this.status = status;
            this.date = date;
            this.total = total;
        }
    }


    private boolean createOrderItems(Order order) {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (OrderItem item : order.getOrderItems()) {
                stmt.setInt(1, order.getId());
                stmt.setInt(2, item.getProductId());
                stmt.setInt(3, item.getQuantity());
                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            return results.length == order.getOrderItems().size();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
