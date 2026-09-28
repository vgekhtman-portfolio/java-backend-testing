package com.vgekhtman.automation.tests.websocketpublic;

import com.vgekhtman.automation.tests.websocketpublic.client.EchoWebSocketClient;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("public-api")
class ReceiveTimeoutTest {

    @Test
    void waitingForAMessageThatWasNeverSentTimesOutRatherThanHanging() {
        try (EchoWebSocketClient client = EchoWebSocketClient.connect()) {
            client.nextMessage(Duration.ofSeconds(10)); // drain the connect-time greeting - nothing else is coming

            assertThatThrownBy(() -> client.nextMessage(Duration.ofSeconds(2)))
                    .isInstanceOf(NoSuchElementException.class);
        }
    }
}
