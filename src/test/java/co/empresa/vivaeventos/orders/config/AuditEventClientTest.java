package co.empresa.vivaeventos.orders.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditEventClientTest {

    @Mock
    private RestTemplate restTemplate;

    private AuditEventClient auditEventClient;

    @Captor
    private ArgumentCaptor<HttpEntity<Map<String, Object>>> entityCaptor;

    @BeforeEach
    void setUp() {
        String jwtSecret = "dGhpc0lzQVZlcnlTZWNyZXRLZXlGb3JWYWlhRXZlbnRvc1RoYXROZWVkczUw";
        auditEventClient = new AuditEventClient(restTemplate, "http://audit:8089", jwtSecret);
    }

    @Test
    void shouldSendAuditEvent() {
        auditEventClient.logEvent(new AuditEventRequest("orders", "550e8400-e29b-41d4-a716-446655440000", "CLIENT",
                "CREATE", "order", "550e8400-e29b-41d4-a716-446655440000",
                null, "{\"total\":50}"));

        verify(restTemplate).exchange(
                eq("http://audit:8089/api/v1/audit/log"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    @Test
    void shouldIncludeAuthorizationHeader() {
        auditEventClient.logEvent(new AuditEventRequest("orders", null, null,
                "HTTP_REQUEST", "GET", null, null, null));

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(Void.class)
        );

        HttpEntity<Map<String, Object>> entity = entityCaptor.getValue();
        assertNotNull(entity.getHeaders());
        assertTrue(entity.getHeaders().get("Authorization").get(0).startsWith("Bearer "));
    }

    @Test
    void shouldIncludeRequestBodyFields() {
        auditEventClient.logEvent(new AuditEventRequest("orders", "660e8400-e29b-41d4-a716-446655440001", "ADMIN",
                "UPDATE", "order", "660e8400-e29b-41d4-a716-446655440001",
                "{\"status\":\"PENDING\"}", "{\"status\":\"PAID\"}"));

        verify(restTemplate).exchange(
                anyString(),
                eq(HttpMethod.POST),
                entityCaptor.capture(),
                eq(Void.class)
        );

        Map<String, Object> body = entityCaptor.getValue().getBody();
        assertEquals("orders", body.get("serviceName"));
        assertEquals("ADMIN", body.get("userRole"));
        assertEquals("{\"status\":\"PAID\"}", body.get("newValues"));
    }

    @Test
    void shouldHandleRestTemplateExceptionGracefully() {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Void.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        assertDoesNotThrow(() ->
                auditEventClient.logEvent(new AuditEventRequest("orders", null, null,
                        "TEST", "test", null, null, null))
        );
    }
}
