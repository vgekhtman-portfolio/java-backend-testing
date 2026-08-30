package com.vgekhtman.automation.sut.inventory;

import com.vgekhtman.automation.inventory.grpc.CheckStockRequest;
import com.vgekhtman.automation.inventory.grpc.CheckStockResponse;
import com.vgekhtman.automation.inventory.grpc.InventoryServiceGrpc;
import com.vgekhtman.automation.inventory.grpc.ReserveStockRequest;
import com.vgekhtman.automation.inventory.grpc.ReserveStockResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.OptionalInt;

public class InventoryServiceImpl extends InventoryServiceGrpc.InventoryServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryRepository repository;

    public InventoryServiceImpl(InventoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public void checkStock(CheckStockRequest request, StreamObserver<CheckStockResponse> responseObserver) {
        OptionalInt quantity = repository.findQuantity(request.getProductId());
        if (quantity.isEmpty()) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription("product not found: " + request.getProductId())
                    .asRuntimeException());
            return;
        }
        log.info("CheckStock({}) -> {}", request.getProductId(), quantity.getAsInt());
        responseObserver.onNext(CheckStockResponse.newBuilder()
                .setProductId(request.getProductId())
                .setAvailableQuantity(quantity.getAsInt())
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void reserveStock(ReserveStockRequest request, StreamObserver<ReserveStockResponse> responseObserver) {
        if (request.getQuantity() <= 0) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("quantity must be positive")
                    .asRuntimeException());
            return;
        }

        ReservationResult result = repository.reserve(request.getProductId(), request.getQuantity());
        switch (result.status()) {
            case NOT_FOUND -> responseObserver.onError(Status.NOT_FOUND
                    .withDescription("product not found: " + request.getProductId())
                    .asRuntimeException());
            case INSUFFICIENT -> {
                log.info("ReserveStock({}, {}) -> insufficient, {} available",
                        request.getProductId(), request.getQuantity(), result.remainingQuantity());
                responseObserver.onNext(ReserveStockResponse.newBuilder()
                        .setSuccess(false)
                        .setRemainingQuantity(result.remainingQuantity())
                        .setMessage("insufficient stock")
                        .build());
                responseObserver.onCompleted();
            }
            case SUCCESS -> {
                log.info("ReserveStock({}, {}) -> reserved, {} remaining",
                        request.getProductId(), request.getQuantity(), result.remainingQuantity());
                responseObserver.onNext(ReserveStockResponse.newBuilder()
                        .setSuccess(true)
                        .setRemainingQuantity(result.remainingQuantity())
                        .setMessage("reserved")
                        .build());
                responseObserver.onCompleted();
            }
        }
    }
}
