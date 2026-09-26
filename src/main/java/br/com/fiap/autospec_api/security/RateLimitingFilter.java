package br.com.fiap.autospec_api.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter implements Filter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket resolveBucket(String key) {
        return buckets.computeIfAbsent(key, k ->
                Bucket4j.builder()
                        .addLimit(Bandwidth.classic(
                                60, // capacity
                                Refill.intervally(60, Duration.ofMinutes(1)) // refill 60 tokens per minute
                        ))
                        .build()
        );
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Extrai IP do cliente, respeitando proxy headers quando presentes
        String ip = Optional.ofNullable(httpRequest.getHeader("X-Forwarded-For"))
                .map(h -> h.split(",")[0].trim())
                .orElse(httpRequest.getRemoteAddr());

        Bucket bucket = resolveBucket(ip);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            httpResponse.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(request, response);
        } else {
            long waitForRefillNanos = probe.getNanosToWaitForRefill();
            long waitSeconds = Math.max(1, waitForRefillNanos / 1_000_000_000L);

            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.setHeader("Retry-After", String.valueOf(waitSeconds));

            String body = String.format(
                    "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded. Retry after %d seconds.\"}",
                    waitSeconds
            );

            httpResponse.getWriter().write(body);
        }
    }
}
