package org.example.fooddelivery.repository;

import org.example.fooddelivery.model.Product;
import org.example.fooddelivery.model.Restaurant;
import org.example.fooddelivery.util.DatabaseConnection;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {
    private final DatabaseConnection dbConnection;

    public ProductRepository() {
        this.dbConnection = DatabaseConnection.getInstance();
    }

    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = """
        SELECT p.*, r.id as restaurant_id, r.name as restaurant_name
        FROM products p
        LEFT JOIN restaurants r ON p.restaurant_id = r.id
        """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public List<Product> getProductsByRestaurantId(int restaurantId) {
        List<Product> products = new ArrayList<>();
        String sql = """
    SELECT p.*, r.name as restaurant_name
    FROM products p
    LEFT JOIN restaurants r ON p.restaurant_id = r.id
    WHERE p.restaurant_id = ?
        """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, restaurantId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public Product getProductById(int id) {
        String sql = """
        SELECT p.*, r.id as restaurant_id, r.name as restaurant_name
        FROM products p
        LEFT JOIN restaurants r ON p.restaurant_id = r.id
        WHERE p.id = ?
    """;

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean createProduct(Product product) {
        String sql = "INSERT INTO products (name, description, price, restaurant_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getRestaurantId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getInt(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, description = ?, price = ?, restaurant_id = ? WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, product.getName());
            stmt.setString(2, product.getDescription());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getRestaurant().getId());
            stmt.setInt(5, product.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public List<Product> getPopularProducts() {
        List<Product> products = new ArrayList<>();
        String sql = """
        SELECT p.*, r.name AS restaurant_name
        FROM products p
        LEFT JOIN restaurants r ON p.restaurant_id = r.id
        LEFT JOIN order_items oi ON p.id = oi.product_id
        GROUP BY p.id
        ORDER BY COUNT(oi.product_id) DESC
        LIMIT 10
        """;


        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        int productId = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        BigDecimal price = rs.getBigDecimal("price");

        int restaurantId = rs.getInt("restaurant_id");
        String restaurantName = rs.getString("restaurant_name");
        Restaurant restaurant = new Restaurant(restaurantId, restaurantName, null, null);

        return new Product(productId, name, price, description, restaurant);
    }


}
