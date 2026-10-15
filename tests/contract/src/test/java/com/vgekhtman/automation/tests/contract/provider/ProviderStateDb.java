package com.vgekhtman.automation.tests.contract.provider;

import com.vgekhtman.automation.testutils.CustomSut;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.UUID;

/** Sets the provider's database into the state a pact interaction assumes, by writing rows directly. */
final class ProviderStateDb {

    private ProviderStateDb() {
    }

    /** Replaces any row with this id, so the state is the same however often it is applied. */
    static void putOrder(UUID id, String customerId, String productId, int quantity, String status,
                         OffsetDateTime createdAt) {
        deleteOrder(id);
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO orders (id, customer_id, product_id, quantity, status, created_at) "
                             + "VALUES (?, ?, ?, ?, ?, ?)")) {
            statement.setObject(1, id);
            statement.setString(2, customerId);
            statement.setString(3, productId);
            statement.setInt(4, quantity);
            statement.setString(5, status);
            statement.setObject(6, createdAt);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("failed to insert provider-state order " + id, e);
        }
    }

    static void deleteOrder(UUID id) {
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM orders WHERE id = ?")) {
            statement.setObject(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("failed to delete provider-state order " + id, e);
        }
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(CustomSut.jdbcUrl(), CustomSut.dbUser(), CustomSut.dbPassword());
    }
}
