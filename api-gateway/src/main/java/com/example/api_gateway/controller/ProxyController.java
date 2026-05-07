package com.example.api_gateway.controller;

import java.util.Enumeration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@Tag(name = "Gateway", description = "Proxy ke semua service")
public class ProxyController {

    private final RestTemplate restTemplate;

    @Value("${auth.service.url}")
    private String authServiceUrl;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    @Value("${accounting.service.url}")
    private String accountingServiceUrl;

    public ProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // ─── AUTH ────────────────────────────────────────────────────────────────────

    @RequestMapping("/auth/**")
    @Operation(summary = "Proxy ke Auth Service")
    public ResponseEntity<Object> proxyAuth(HttpServletRequest request,
                                             @RequestBody(required = false) Object body) {
        return proxy(request, body, authServiceUrl);
    }

    // ─── INVENTORY ───────────────────────────────────────────────────────────────

    @RequestMapping("/api/inventory/**")
    @Operation(summary = "Proxy ke Inventory Service")
    public ResponseEntity<Object> proxyInventory(HttpServletRequest request,
                                                   @RequestBody(required = false) Object body) {
        return proxy(request, body, inventoryServiceUrl);
    }

    // ─── ACCOUNTING ──────────────────────────────────────────────────────────────

    @RequestMapping("/api/accounting/**")
    @Operation(summary = "Proxy ke Accounting Service")
    public ResponseEntity<Object> proxyAccounting(HttpServletRequest request,
                                                    @RequestBody(required = false) Object body) {
        return proxy(request, body, accountingServiceUrl);
    }

    // ─── PRIVATE HELPER ──────────────────────────────────────────────────────────

    private ResponseEntity<Object> proxy(HttpServletRequest request,
                                          Object body,
                                          String targetBaseUrl) {
        String path = request.getRequestURI();
        String query = request.getQueryString();
        String targetUrl = targetBaseUrl + path + (query != null ? "?" + query : "");

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            headers.set(name, request.getHeader(name));
        }

        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.exchange(targetUrl, method, entity, Object.class);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAs(Object.class));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(java.util.Map.of("message", "Service tidak tersedia: " + e.getMessage()));
        }
    }
}