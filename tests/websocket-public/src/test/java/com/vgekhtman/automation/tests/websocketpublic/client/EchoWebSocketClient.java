package com.vgekhtman.automation.tests.websocketpublic.client;

import com.vgekhtman.automation.testsupport.AllureEvidence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Minimal wrapper around the JDK's built-in {@link WebSocket}: connect, send a text message, block for
 * the next complete message (or time out), and observe the close handshake. Just enough to exercise
 * echo-server mechanics - not a general-purpose WebSocket client.
 * Every message and the close handshake is recorded with a timestamp, and the record is attached to the
 * Allure report when the client is closed.
 */
public final class EchoWebSocketClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(EchoWebSocketClient.class);

    public static final String DEFAULT_URI = env("WEBSOCKET_PUBLIC_URI", "wss://echo.websocket.org/");

    public record CloseEvent(int statusCode, String reason) {
    }

    private final WebSocket webSocket;
    private final BlockingQueue<String> messages;
    private final CompletableFuture<CloseEvent> closeEvent;
    private final List<String> messageLog;
    private boolean logAttached;

    private EchoWebSocketClient(WebSocket webSocket, BlockingQueue<String> messages,
                                 CompletableFuture<CloseEvent> closeEvent, List<String> messageLog) {
        this.webSocket = webSocket;
        this.messages = messages;
        this.closeEvent = closeEvent;
        this.messageLog = messageLog;
    }

    public static EchoWebSocketClient connect() {
        return connect(DEFAULT_URI);
    }

    public static EchoWebSocketClient connect(String uri) {
        BlockingQueue<String> messages = new LinkedBlockingQueue<>();
        CompletableFuture<CloseEvent> closeEvent = new CompletableFuture<>();
        List<String> messageLog = new CopyOnWriteArrayList<>();

        log.info("WebSocket connecting to {}", uri);
        try {
            WebSocket webSocket = HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .buildAsync(URI.create(uri), getNewListener(messages, closeEvent, messageLog))
                    .get(10, TimeUnit.SECONDS);
            record(messageLog, "connected to " + uri);
            return new EchoWebSocketClient(webSocket, messages, closeEvent, messageLog);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("interrupted while connecting to " + uri, e);
        } catch (ExecutionException | TimeoutException e) {
            throw new RuntimeException("failed to connect to " + uri, e);
        }
    }

    /** Collects complete text messages into the queue and reports the close handshake or a failure. */
    private static WebSocket.Listener getNewListener(BlockingQueue<String> messages,
                                                     CompletableFuture<CloseEvent> closeEvent,
                                                     List<String> messageLog) {
        return new WebSocket.Listener() {
            private final StringBuilder buffer = new StringBuilder();

            @Override
            public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                buffer.append(data);
                if (last) {
                    record(messageLog, "received: " + buffer);
                    messages.add(buffer.toString());
                    buffer.setLength(0);
                }
                ws.request(1);
                return null;
            }

            @Override
            public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
                record(messageLog, "closed by server: code " + statusCode + ", reason \"" + reason + "\"");
                closeEvent.complete(new CloseEvent(statusCode, reason));
                return null;
            }

            @Override
            public void onError(WebSocket ws, Throwable error) {
                record(messageLog, "error: " + error);
                closeEvent.completeExceptionally(error);
            }
        };
    }

    public boolean isOpen() {
        return !webSocket.isOutputClosed() && !webSocket.isInputClosed();
    }

    public void send(String text) {
        record(messageLog, "sent: " + text);
        webSocket.sendText(text, true);
    }

    /** Blocks for the next complete text message. @throws NoSuchElementException if none arrives in time. */
    public String nextMessage(Duration timeout) {
        try {
            String message = messages.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (message == null) {
                throw new NoSuchElementException("no message received within " + timeout);
            }
            return message;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /** Sends a close frame and blocks for the server's close acknowledgement. */
    public CloseEvent closeAndAwait(int statusCode, String reason, Duration timeout) {
        record(messageLog, "closing: code " + statusCode + ", reason \"" + reason + "\"");
        webSocket.sendClose(statusCode, reason);
        try {
            return closeEvent.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException | TimeoutException e) {
            throw new RuntimeException("did not observe a close event within " + timeout, e);
        }
    }

    /** Best-effort cleanup for tests that didn't already close explicitly via {@link #closeAndAwait}. */
    @Override
    public void close() {
        if (!webSocket.isOutputClosed()) {
            record(messageLog, "closing: test cleanup");
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "test cleanup");
        }
        if (!logAttached) {
            logAttached = true;
            AllureEvidence.text("WebSocket message log", String.join(System.lineSeparator(), messageLog));
        }
    }

    private static void record(List<String> messageLog, String event) {
        log.info("WebSocket {}", event);
        messageLog.add(LocalTime.now() + " " + event);
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}
