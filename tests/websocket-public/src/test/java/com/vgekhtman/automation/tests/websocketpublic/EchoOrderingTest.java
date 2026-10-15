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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("API Protocols")
@Feature("WebSocket")
@Story("Echo")
@Tag("public-api")
class EchoOrderingTest {

    @DisplayName("TC-WEBSOCKET-002: a sent message is echoed back exactly")
    @Description("Sends one text message and asserts the identical message returns.")
    @Test
    void sentMessageIsEchoedBackExactly() {
        try (EchoWebSocketClient client = EchoWebSocketClient.connect()) {
            client.nextMessage(Duration.ofSeconds(10)); // drain the connect-time greeting

            client.send("hello echo server");
            String echoed = client.nextMessage(Duration.ofSeconds(10));

            assertThat(echoed).isEqualTo("hello echo server");
        }
    }

    @DisplayName("TC-WEBSOCKET-003: several messages are echoed back in the order they were sent")
    @Description("Sends four messages in sequence and asserts they return in order.")
    @Test
    void severalMessagesAreEchoedBackInTheOrderTheyWereSent() {
        try (EchoWebSocketClient client = EchoWebSocketClient.connect()) {
            client.nextMessage(Duration.ofSeconds(10)); // drain the connect-time greeting

            List<String> sent = List.of("one", "two", "three", "four");
            sent.forEach(client::send);

            List<String> received = new ArrayList<>();
            for (int i = 0; i < sent.size(); i++) {
                received.add(client.nextMessage(Duration.ofSeconds(10)));
            }

            assertThat(received).containsExactlyElementsOf(sent);
        }
    }
}
