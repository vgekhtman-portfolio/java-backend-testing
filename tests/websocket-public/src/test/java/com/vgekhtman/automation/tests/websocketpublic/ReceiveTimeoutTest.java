package com.vgekhtman.automation.tests.websocketpublic;

import com.vgekhtman.automation.tests.websocketpublic.client.EchoWebSocketClient;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Epic("API Protocols")
@Feature("WebSocket")
@Story("Timeouts")
@Tag("public-api")
class ReceiveTimeoutTest {

    @DisplayName("TC-WEBSOCKET-004: waiting for a message that never comes times out instead of hanging")
    @Description("Handles the nothing-happened case deliberately, not just the happy path.")
    @Test
    void waitingForAMessageThatWasNeverSentTimesOutRatherThanHanging() {
        try (EchoWebSocketClient client = EchoWebSocketClient.connect()) {
            client.nextMessage(Duration.ofSeconds(10)); // drain the connect-time greeting - nothing else is coming

            assertThatThrownBy(() -> client.nextMessage(Duration.ofSeconds(2)))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }
}
