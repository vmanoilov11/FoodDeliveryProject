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
    private final DatabaseConnection dbConnection = DatabaseConnection.getInstance();
    private final ProductRepository productRepository = new ProductRepository();

    public List<Order> getAllOrders() {
        List<OrderTemp> tempOrders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tempOrders.add(new OrderTemp(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("restaurant_id"),
                        rs.getString("status"),
                        rs.getTimestamp("order_date").toLocalDateTime(),
                        BigDecimal.ZERO
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        return mapTempOrdersToOrders(tempOrders);
    }

    public List<Order> getOrdersByUserId(int userId) {
        List<OrderTemp> tempOrders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tempOrders.add(new OrderTemp(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("restaurant_id"),
                        rs.getString("status"),
                        rs.getTimestamp("order_date").toLocalDateTime(),
                        BigDecimal.ZERO
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        List<Order> orders = new ArrayList<>();
        User user = new UserRepository().getUserById(userId);
        RestaurantRepository restaurantRepo = new RestaurantRepository();

        for (OrderTemp temp : tempOrders) {
            Restaurant restaurant = restaurantRepo.getRestaurantById(temp.restaurantId);
            Order order = new Order(temp.id, user, restaurant, new ArrayList<>(), temp.date, temp.status);
            loadOrderItems(order);
            orders.add(order);
        }

        return orders;
    }


    public List<Order> getOrdersByStatus(String status) {
        List<OrderTemp> tempOrders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status = ? ORDER BY order_date DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tempOrders.add(new OrderTemp(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("restaurant_id"),
                        status,
                        rs.getTimestamp("order_date").toLocalDateTime(),
                        BigDecimal.ZERO
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        return mapTempOrdersToOrders(tempOrders);
    }

    public List<Order> getDeliveriesByDelivererId(int delivererId) {
        List<OrderTemp> tempOrders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE deliverer_id = ? ORDER BY order_date DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, delivererId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tempOrders.add(new OrderTemp(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getInt("restaurant_id"),
                            rs.getString("status"),
                            rs.getTimestamp("order_date").toLocalDateTime(),
                            BigDecimal.ZERO
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        return mapTempOrdersToOrders(tempOrders);

    }
    public List<Order> getOrdersByDate(LocalDate date) {
        return getOrdersByDateQuery("SELECT * FROM orders WHERE DATE(order_date) = ? ORDER BY order_date DESC", date);
    }

    public List<Order> getOrdersByMonth(LocalDate date) {
        String sql = "SELECT * FROM orders WHERE YEAR(order_date) = ? AND MONTH(order_date) = ? ORDER BY order_date DESC";
        List<OrderTemp> tempOrders = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, date.getYear());
            stmt.setInt(2, date.getMonthValue());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tempOrders.add(new OrderTemp(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("restaurant_id"),
                        rs.getString("status"),
                        rs.getTimestamp("order_date").toLocalDateTime(),
                        BigDecimal.ZERO
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        return mapTempOrdersToOrders(tempOrders);
    }

    public boolean createOrder(Order order) {
        String sql = "INSERT INTO orders (user_id, restaurant_id, status, order_date) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, order.getUserId());
            stmt.setInt(2, order.getRestaurantId());
            stmt.setString(3, order.getStatus().toString());
            stmt.setTimestamp(4, Timestamp.valueOf(order.getOrderDate()));

            if (stmt.executeUpdate() > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    order.setId(keys.getInt(1));
                    return createOrderItems(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateOrderStatus(int orderId, String status, int delivererId) {
        String sql = "UPDATE orders SET status = ?, deliverer_id = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, delivererId);
            stmt.setInt(3, orderId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }



    public BigDecimal calculateDelivererEarnings(int delivererId) {
        String sql = """
            SELECT COALESCE(SUM(
                CASE WHEN o.status = 'DELIVERED' THEN
                    (SELECT SUM(p.price * oi.quantity) * 0.1
                     FROM order_items oi
                     JOIN products p ON oi.product_id = p.id
                     WHERE oi.order_id = o.id)
                ELSE 0 END), 0) AS total_earnings
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
        String sql = "SELECT COUNT(*) AS count FROM order_items WHERE product_id = ?";

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
            SELECT COALESCE(SUM(p.price * oi.quantity), 0) AS revenue
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
        UserRepository userRepo = new UserRepository();
        RestaurantRepository restaurantRepo = new RestaurantRepository();

        return new Order(
                rs.getInt("id"),
                userRepo.getUserById(rs.getInt("user_id")),
                restaurantRepo.getRestaurantById(rs.getInt("restaurant_id")),
                new ArrayList<>(),
                rs.getTimestamp("order_date").toLocalDateTime(),
                rs.getString("status")
        );
    }

    private void loadOrderItems(Order order) {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";
        List<OrderItemRow> tempItems = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, order.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tempItems.add(new OrderItemRow(
                        rs.getInt("id"),
                        rs.getInt("order_id"),
                        rs.getInt("product_id"),
                        rs.getInt("quantity")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        for (OrderItemRow row : tempItems) {
            Product product = productRepository.getProductById(row.productId);
            if (product == null) {
                System.err.printf("⚠ Product with ID %d not found (Order ID: %d). Skipping item.%n", row.productId, row.orderId);
                continue;
            }

            OrderItem item = new OrderItem(row.id, row.orderId, product, row.quantity);
            order.addOrderItem(item);
        }
    }


    private List<Order> getOrdersByDateQuery(String sql, LocalDate date) {
        List<OrderTemp> tempOrders = new ArrayList<>();

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                tempOrders.add(new OrderTemp(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("restaurant_id"),
                        rs.getString("status"),
                        rs.getTimestamp("order_date").toLocalDateTime(),
                        BigDecimal.ZERO
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        return mapTempOrdersToOrders(tempOrders);
    }

    private List<Order> mapTempOrdersToOrders(List<OrderTemp> tempOrders) {
        List<Order> orders = new ArrayList<>();
        UserRepository userRepo = new UserRepository();
        RestaurantRepository restaurantRepo = new RestaurantRepository();

        for (OrderTemp temp : tempOrders) {
            User user = userRepo.getUserById(temp.userId);
            Restaurant restaurant = restaurantRepo.getRestaurantById(temp.restaurantId);
            Order order = new Order(temp.id, user, restaurant, new ArrayList<>(), temp.date, temp.status);
            loadOrderItems(order);
            orders.add(order);
        }

        return orders;
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

    private static class OrderItemRow {
        int id, orderId, productId, quantity;

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
}
