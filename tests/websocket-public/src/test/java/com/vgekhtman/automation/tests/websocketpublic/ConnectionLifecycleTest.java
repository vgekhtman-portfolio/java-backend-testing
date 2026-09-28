package com.vgekhtman.automation.tests.websocketpublic;

import com.vgekhtman.automation.tests.websocketpublic.client.EchoWebSocketClient;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.net.http.WebSocket;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("public-api")
class ConnectionLifecycleTest {

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
