package com.vgekhtman.automation.sut.inventory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.OptionalInt;

public class InventoryRepository {

    private final DataSource dataSource;

    public InventoryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public OptionalInt findQuantity(String productId) {
        String sql = "SELECT quantity FROM inventory WHERE product_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, productId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? OptionalInt.of(rs.getInt(1)) : OptionalInt.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("failed to read inventory", e);
        }
    }

    /**
     * Atomically reserves stock: locks the row, checks availability, and decrements only if there is
     * enough. Never lets quantity go negative.
     */
    public ReservationResult reserve(String productId, int quantity) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Integer current = lockAndRead(connection, productId);
                if (current == null) {
                    connection.rollback();
                    return ReservationResult.notFound();
                }
                if (quantity > current) {
                    connection.rollback();
                    return ReservationResult.insufficient(current);
                }
                int remaining = current - quantity;
                try (PreparedStatement statement =
                             connection.prepareStatement("UPDATE inventory SET quantity = ? WHERE product_id = ?")) {
                    statement.setInt(1, remaining);
                    statement.setString(2, productId);
                    statement.executeUpdate();
                }
                connection.commit();
                return ReservationResult.success(remaining);
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("failed to reserve inventory", e);
        }
    }

    private static Integer lockAndRead(Connection connection, String productId) throws SQLException {
        try (PreparedStatement statement =
                     connection.prepareStatement("SELECT quantity FROM inventory WHERE product_id = ? FOR UPDATE")) {
            statement.setString(1, productId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }
}
