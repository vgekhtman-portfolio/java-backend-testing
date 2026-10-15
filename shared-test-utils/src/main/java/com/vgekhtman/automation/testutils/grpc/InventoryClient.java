package com.vgekhtman.automation.testutils.grpc;

import com.vgekhtman.automation.inventory.grpc.CheckStockRequest;
import com.vgekhtman.automation.inventory.grpc.CheckStockResponse;
import com.vgekhtman.automation.inventory.grpc.InventoryServiceGrpc;
import com.vgekhtman.automation.inventory.grpc.ReserveStockRequest;
import com.vgekhtman.automation.inventory.grpc.ReserveStockResponse;
import com.vgekhtman.automation.testutils.CustomSut;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Blocking client for the custom SUT's InventoryService; every call carries a deadline so a hang fails fast.
 * The stub and message classes are generated from {@code proto/.../inventory.proto} at build time (into
 * {@code proto/build/generated}), so they are not in the source tree.
 */
public final class InventoryClient implements AutoCloseable {

    private static final long DEADLINE_SECONDS = 10;

    private final ManagedChannel channel;
    private final InventoryServiceGrpc.InventoryServiceBlockingStub stub;

    private InventoryClient(ManagedChannel channel) {
        this.channel = channel;
        this.stub = InventoryServiceGrpc.newBlockingStub(channel);
    }

    public static InventoryClient connect() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(CustomSut.grpcHost(), CustomSut.grpcPort())
                .usePlaintext()
                .build();
        return new InventoryClient(channel);
    }

    public CheckStockResponse checkStock(CheckStockRequest request) {
        return stub.withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS).checkStock(request);
    }

    public ReserveStockResponse reserveStock(ReserveStockRequest request) {
        return stub.withDeadlineAfter(DEADLINE_SECONDS, TimeUnit.SECONDS).reserveStock(request);
    }

    @Override
    public void close() {
        channel.shutdownNow();
    }
}
