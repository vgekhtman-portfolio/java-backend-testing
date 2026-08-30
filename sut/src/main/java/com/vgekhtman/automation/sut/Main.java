package com.vgekhtman.automation.sut;

import com.vgekhtman.automation.sut.db.Database;
import com.vgekhtman.automation.sut.inventory.InventoryRepository;
import com.vgekhtman.automation.sut.inventory.InventoryServiceImpl;
import com.vgekhtman.automation.sut.orders.OrderApi;
import com.vgekhtman.automation.sut.orders.OrderRepository;
import com.vgekhtman.automation.sut.orders.OrderService;
import com.vgekhtman.automation.sut.payment.PaymentClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.undertow.Undertow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        Config config = Config.fromEnv();

        Database database = new Database(config.jdbcUrl(), config.dbUser(), config.dbPassword());
        database.migrate();
        log.info("database migrated");

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        InventoryRepository inventoryRepository = new InventoryRepository(database.dataSource());
        Server grpcServer;
        try {
            grpcServer = ServerBuilder.forPort(config.grpcPort())
                    .addService(new InventoryServiceImpl(inventoryRepository))
                    .build()
                    .start();
        } catch (java.io.IOException e) {
            throw new RuntimeException("failed to start gRPC server", e);
        }
        log.info("gRPC InventoryService listening on {}", config.grpcPort());

        OrderRepository orderRepository = new OrderRepository(database.dataSource());
        PaymentClient paymentClient = new PaymentClient(config.paymentServiceUrl(), objectMapper);
        OrderService orderService = new OrderService(orderRepository, paymentClient, config.unitPrice());
        OrderApi orderApi = new OrderApi(orderService, orderRepository, objectMapper);

        Undertow httpServer = Undertow.builder()
                .addHttpListener(config.httpPort(), "0.0.0.0")
                .setHandler(orderApi.router())
                .build();
        httpServer.start();
        log.info("REST API listening on {}", config.httpPort());

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("shutting down");
            httpServer.stop();
            grpcServer.shutdownNow();
            database.close();
        }));

        grpcServer.awaitTermination();
    }
}
