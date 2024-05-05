package org.kla;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class LogicalCouplingServiceTest {
    @Inject
    LogicalCouplingService sut;

    @Test
    void findCoupling() throws IOException {

        String result = sut.findCoupling("any", "test");

        assertEquals("Frank Elsinga Kordian Bruck number of commits 110", result);
    }
}