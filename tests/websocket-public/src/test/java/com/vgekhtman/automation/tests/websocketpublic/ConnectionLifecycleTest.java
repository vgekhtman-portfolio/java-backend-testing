package com.vgekhtman.automation.tests.websocketpublic;

import com.vgekhtman.automation.tests.websocketpublic.client.EchoWebSocketClient;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.http.WebSocket;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Protocols")
@Feature("WebSocket")
@Story("Connection lifecycle")
@Tag("public-api")
class ConnectionLifecycleTest {

    @DisplayName("TC-WEBSOCKET-001: a connection opens and closes with the expected code")
    @Description("Opens a connection, confirms it is live, closes it and asserts the close status code.")
    @Test
    void connectionOpensAndClosesWithTheExpectedCode() {
        try (EchoWebSocketClient client = EchoWebSocketClient.connect()) {
            assertThat(client.isOpen()).isTrue();

            // The server greets every new connection with a message - draining it here doubles as proof
            // the connection is genuinely live, not just that the handshake returned.
            String greeting = client.nextMessage(Duration.ofSeconds(10));
            assertThat(greeting).isNotBlank();

            EchoWebSocketClient.CloseEvent closeEvent =
                    client.closeAndAwait(WebSocket.NORMAL_CLOSURE, "test done", Duration.ofSeconds(10));

            assertThat(closeEvent.statusCode()).isEqualTo(WebSocket.NORMAL_CLOSURE);
            assertThat(client.isOpen()).isFalse();
        }
    }
}
