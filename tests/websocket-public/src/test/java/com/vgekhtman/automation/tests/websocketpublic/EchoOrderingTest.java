package com.vgekhtman.automation.tests.websocketpublic;

import com.vgekhtman.automation.tests.websocketpublic.client.EchoWebSocketClient;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("public-api")
class EchoOrderingTest {

    @Test
    void sentMessageIsEchoedBackExactly() {
        try (EchoWebSocketClient client = EchoWebSocketClient.connect()) {
            client.nextMessage(Duration.ofSeconds(10)); // drain the connect-time greeting

            client.send("hello echo server");
            String echoed = client.nextMessage(Duration.ofSeconds(10));

            assertThat(echoed).isEqualTo("hello echo server");
        }
    }

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
