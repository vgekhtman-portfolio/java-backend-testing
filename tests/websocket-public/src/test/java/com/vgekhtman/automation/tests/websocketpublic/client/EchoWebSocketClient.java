package com.vgekhtman.automation.tests.websocketpublic.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
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
 */
public final class EchoWebSocketClient implements AutoCloseable {

    public static final String DEFAULT_URI = env("WEBSOCKET_PUBLIC_URI", "wss://echo.websocket.org/");

    public record CloseEvent(int statusCode, String reason) {
    }

    private final WebSocket webSocket;
    private final BlockingQueue<String> messages;
    private final CompletableFuture<CloseEvent> closeEvent;

    private EchoWebSocketClient(WebSocket webSocket, BlockingQueue<String> messages,
                                 CompletableFuture<CloseEvent> closeEvent) {
        this.webSocket = webSocket;
        this.messages = messages;
        this.closeEvent = closeEvent;
    }

    public static EchoWebSocketClient connect() {
        return connect(DEFAULT_URI);
    }

    public static EchoWebSocketClient connect(String uri) {
        BlockingQueue<String> messages = new LinkedBlockingQueue<>();
        CompletableFuture<CloseEvent> closeEvent = new CompletableFuture<>();

        try {
            WebSocket webSocket = HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .buildAsync(URI.create(uri), getNewListener(messages, closeEvent))
                    .get(10, TimeUnit.SECONDS);
            return new EchoWebSocketClient(webSocket, messages, closeEvent);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("interrupted while connecting to " + uri, e);
        } catch (ExecutionException | TimeoutException e) {
            throw new RuntimeException("failed to connect to " + uri, e);
        }
    }

    /** Collects complete text messages into the queue and reports the close handshake or a failure. */
    private static WebSocket.Listener getNewListener(BlockingQueue<String> messages,
                                                     CompletableFuture<CloseEvent> closeEvent) {
        return new WebSocket.Listener() {
            private final StringBuilder buffer = new StringBuilder();

            @Override
            public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                buffer.append(data);
                if (last) {
                    messages.add(buffer.toString());
                    buffer.setLength(0);
                }
                ws.request(1);
                return null;
            }

            @Override
            public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
                closeEvent.complete(new CloseEvent(statusCode, reason));
                return null;
            }

            @Override
            public void onError(WebSocket ws, Throwable error) {
                closeEvent.completeExceptionally(error);
            }
        };
    }

    public boolean isOpen() {
        return !webSocket.isOutputClosed() && !webSocket.isInputClosed();
    }

    public void send(String text) {
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
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "test cleanup");
        }
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}
