package com.vgekhtman.automation.sut.orders;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Plain JDBC persistence for orders. No ORM by design. */
public class OrderRepository {

    private final DataSource dataSource;

    public OrderRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Order insert(Order order) {
        String sql = "INSERT INTO orders (id, customer_id, product_id, quantity, status, created_at) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, order.id());
            statement.setString(2, order.customerId());
            statement.setString(3, order.productId());
            statement.setInt(4, order.quantity());
            statement.setString(5, order.status().name());
            statement.setObject(6, OffsetDateTime.ofInstant(order.createdAt(), ZoneOffset.UTC));
            statement.executeUpdate();
            return order;
        } catch (SQLException e) {
            throw new RuntimeException("failed to insert order", e);
        }
    }

    public void updateStatus(UUID id, OrderStatus status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status.name());
            statement.setObject(2, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("failed to update order status", e);
        }
    }

    public Optional<Order> findById(UUID id) {
        String sql = "SELECT id, customer_id, product_id, quantity, status, created_at FROM orders WHERE id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("failed to load order", e);
        }
    }

    public List<Order> findAll() {
        String sql = "SELECT id, customer_id, product_id, quantity, status, created_at FROM orders";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<Order> orders = new ArrayList<>();
            while (rs.next()) {
                orders.add(map(rs));
            }
            return orders;
        } catch (SQLException e) {
            throw new RuntimeException("failed to list orders", e);
        }
    }

    private static Order map(ResultSet rs) throws SQLException {
        return new Order(
                rs.getObject("id", UUID.class),
                rs.getString("customer_id"),
                rs.getString("product_id"),
                rs.getInt("quantity"),
                OrderStatus.valueOf(rs.getString("status")),
                rs.getObject("created_at", OffsetDateTime.class).toInstant()
        );
    }
}
