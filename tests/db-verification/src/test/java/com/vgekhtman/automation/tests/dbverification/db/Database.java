package com.vgekhtman.automation.tests.dbverification.db;

import com.vgekhtman.automation.testutils.CustomSut;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/** Direct JDBC access to the SUT's Postgres - the independent view of what was actually persisted. */
public final class Database {

    /** A row of the {@code orders} table. */
    public record OrderRow(UUID id, String customerId, String productId, int quantity, String status,
                           OffsetDateTime createdAt) {
    }

    private static final String ORDER_BY_ID =
            "SELECT id, customer_id, product_id, quantity, status, created_at FROM orders WHERE id = ?";
    private static final String COUNT_ORDERS_BY_PRODUCT = "SELECT count(*) FROM orders WHERE product_id = ?";
    private static final String INVENTORY_QUANTITY = "SELECT quantity FROM inventory WHERE product_id = ?";

    private Database() {
    }

    public static Optional<OrderRow> findOrder(UUID id) {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(ORDER_BY_ID)) {
            statement.setObject(1, id);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(new OrderRow(
                        rs.getObject("id", UUID.class),
                        rs.getString("customer_id"),
                        rs.getString("product_id"),
                        rs.getInt("quantity"),
                        rs.getString("status"),
                        rs.getObject("created_at", OffsetDateTime.class)));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("failed to query orders by id", e);
        }
    }

    public static long countOrdersForProduct(String productId) {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(COUNT_ORDERS_BY_PRODUCT)) {
            statement.setString(1, productId);
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("failed to count orders by product", e);
        }
    }

    public static int inventoryQuantity(String productId) {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(INVENTORY_QUANTITY)) {
            statement.setString(1, productId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException("no inventory row for " + productId);
                }
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("failed to query inventory", e);
        }
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(CustomSut.jdbcUrl(), CustomSut.dbUser(), CustomSut.dbPassword());
    }
}
