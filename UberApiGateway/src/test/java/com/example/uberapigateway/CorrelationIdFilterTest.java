package com.example.uberapigateway;

import com.example.uberapigateway.filters.CorrelationIdFilter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class CorrelationIdFilterTest {

    @Test
    @DisplayName("CorrelationIdFilter generates X-Correlation-ID header if not present")
    public void testCorrelationIdGeneratedWhenMissing() {
        CorrelationIdFilter filter = new CorrelationIdFilter();
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/bookings").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, (ex) -> Mono.empty()).block();

        String correlationId = exchange.getResponse().getHeaders().getFirst("X-Correlation-ID");
        Assertions.assertNotNull(correlationId, "X-Correlation-ID response header must be generated");
        Assertions.assertFalse(correlationId.isBlank());
    }

    @Test
    @DisplayName("CorrelationIdFilter preserves existing X-Correlation-ID header")
    public void testCorrelationIdPreservedWhenPresent() {
        CorrelationIdFilter filter = new CorrelationIdFilter();
        String customCorrelationId = "custom-trace-id-99999";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/bookings")
                .header("X-Correlation-ID", customCorrelationId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        filter.filter(exchange, (ex) -> Mono.empty()).block();

        String correlationId = exchange.getResponse().getHeaders().getFirst("X-Correlation-ID");
        Assertions.assertEquals(customCorrelationId, correlationId, "Existing X-Correlation-ID header must be preserved");
    }
}
