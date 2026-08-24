package com.example.uberbookingservice;

import com.example.uberbookingservice.repositories.DriverRepository;
import com.example.uberentityservice.models.Driver;
import com.example.uberentityservice.models.DriverState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@ActiveProfiles("test")
public class BookingConcurrencyTest {

    @Autowired
    private DriverRepository driverRepository;

    @Test
    @DisplayName("Simulate 100 simultaneous booking requests for 1 driver - exactly 1 must succeed")
    public void testConcurrentDriverReservation() throws InterruptedException {
        // Create an available driver
        Driver driver = Driver.builder()
                .name("Speedy Driver")
                .licenseNumber("DL-CONCURRENCY-100")
                .phoneNumber("9998887776")
                .isAvailable(true)
                .driverState(DriverState.AVAILABLE)
                .build();

        Driver savedDriver = driverRepository.save(driver);
        Long driverId = savedDriver.getId();

        int numberOfThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    latch.await(); // wait for start signal to execute simultaneously
                    int updatedRows = driverRepository.reserveDriverIfAvailable(driverId);
                    if (updatedRows > 0) {
                        successCount.incrementAndGet();
                    } else {
                        failureCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
            });
        }

        // Trigger all 100 threads simultaneously
        latch.countDown();
        executorService.shutdown();
        while (!executorService.isTerminated()) {
            Thread.sleep(10);
        }

        // Assert exactly 1 successful reservation and 99 controlled rejections
        Assertions.assertEquals(1, successCount.get(), "Exactly 1 request must reserve the driver");
        Assertions.assertEquals(99, failureCount.get(), "99 requests must be rejected safely");
    }
}
