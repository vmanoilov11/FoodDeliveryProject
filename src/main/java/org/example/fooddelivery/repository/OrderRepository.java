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
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                loadOrderItems(order);
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getOrdersByUserId(int userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                loadOrderItems(order);
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getOrdersByStatus(String status) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE status = ? ORDER BY order_date DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                loadOrderItems(order);
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
                loadOrderItems(order);
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public List<Order> getOrdersByDate(LocalDate date) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE DATE(order_date) = ? ORDER BY order_date DESC";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                loadOrderItems(order);
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                loadOrderItems(order);
                orders.add(order);
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

            stmt.setString(1, status);
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
                CASE\s
                    WHEN o.status = 'DELIVERED' THEN\s
                        (SELECT SUM(p.price * oi.quantity) * 0.1\s
                         FROM order_items oi\s
                         JOIN products p ON oi.product_id = p.id\s
                         WHERE oi.order_id = o.id)
                    ELSE 0\s
                END
            ), 0) as total_earnings
            FROM orders o
            WHERE o.deliverer_id = ?
           \s""";

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
        // Create instances of repositories to fetch user and restaurant
        UserRepository userRepository = new UserRepository();
        RestaurantRepository restaurantRepository = new RestaurantRepository();

        int userId = rs.getInt("user_id");
        User user = userRepository.getUserById(userId);

        int restaurantId = rs.getInt("restaurant_id");
        Restaurant restaurant = restaurantRepository.getRestaurantById(restaurantId);

        Order order = new Order(
                rs.getInt("id"),
                user,
                restaurant,
                new ArrayList<>(),
                rs.getTimestamp("order_date").toLocalDateTime(),
                rs.getString("status")
        );

        loadOrderItems(order);

        return order;
    }

    private void loadOrderItems(Order order) {
        String sql = "SELECT * FROM order_items WHERE order_id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, order.getId());
            ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    int productId = rs.getInt("product_id");
                    Product product = productRepository.getProductById(productId);

                    OrderItem item = new OrderItem(
                            rs.getInt("id"),
                            rs.getInt("order_id"),
                            product,
                            rs.getInt("quantity")
                    );
                    order.addOrderItem(item);
                }
            } catch (SQLException e) {
            e.printStackTrace();
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