package co.empresa.vivaeventos.orders.domain.service;

import co.empresa.vivaeventos.orders.domain.model.Order;
import co.empresa.vivaeventos.orders.domain.model.PromoCode;
import co.empresa.vivaeventos.orders.domain.model.PromoCodeUsage;
import co.empresa.vivaeventos.orders.domain.model.dto.OrderRequestDto;
import co.empresa.vivaeventos.orders.domain.model.dto.OrderRequestDto.OrderItemRequest;
import co.empresa.vivaeventos.orders.domain.repository.IOrderRepository;
import co.empresa.vivaeventos.orders.domain.repository.IPromoCodeRepository;
import co.empresa.vivaeventos.orders.domain.repository.IPromoCodeUsageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.client.RestTemplate;

import co.empresa.vivaeventos.orders.config.AuditEventClient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplConcurrencyTest {

    @Mock
    private IOrderRepository orderRepository;
    @Mock
    private IPromoCodeRepository promoCodeRepository;
    @Mock
    private IPromoCodeUsageRepository promoCodeUsageRepository;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private AuditEventClient auditEventClient;

    private OrderServiceImpl service;
    private OrderRequestDto request;
    private AtomicInteger currentUses;

    @BeforeEach
    void setUp() {
        service = new OrderServiceImpl(orderRepository, promoCodeRepository, promoCodeUsageRepository, restTemplate, auditEventClient);

        OrderItemRequest item = new OrderItemRequest();
        item.setEventId(UUID.randomUUID());
        item.setEventName("Concierto");
        item.setTicketTypeId(UUID.randomUUID());
        item.setTicketTypeName("VIP");
        item.setQuantity(1);
        item.setUnitPrice(new BigDecimal("100000"));
        item.setHolderName("Juan Perez");
        item.setHolderEmail("juan@example.com");

        request = new OrderRequestDto();
        request.setUserId(UUID.randomUUID());
        request.setEventId(UUID.randomUUID());
        request.setPromoCode("CONCURSO");
        request.setItems(List.of(item));

        currentUses = new AtomicInteger(0);
    }

    @Test
    void createOrder_concurrentSamePromoCode_respectsMaxUses() throws InterruptedException {
        int threadCount = 15;
        int maxUses = 5;
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(UUID.randomUUID());
            o.setCreatedAt(LocalDateTime.now());
            return o;
        });

        when(promoCodeRepository.findByCode("CONCURSO")).thenAnswer(inv -> {
            PromoCode p = new PromoCode();
            p.setId(UUID.randomUUID());
            p.setCode("CONCURSO");
            p.setDiscountType("FIXED");
            p.setDiscountValue(new BigDecimal("5000"));
            p.setMaxUses(maxUses);
            p.setMinPurchase(new BigDecimal("10000"));
            p.setIsActive(true);
            p.setStartsAt(LocalDateTime.now().minusDays(1));
            p.setExpiresAt(LocalDateTime.now().plusDays(1));
            int current = currentUses.get();
            p.setUsesCount(current);
            p.setVersion((long) current);
            return Optional.of(p);
        });

        doAnswer(inv -> {
            PromoCode p = inv.getArgument(0);
            int expectedVersion = p.getVersion().intValue();
            if (!currentUses.compareAndSet(expectedVersion, expectedVersion + 1)) {
                throw new ObjectOptimisticLockingFailureException("PromoCode", p.getId());
            }
            return p;
        }).when(promoCodeRepository).save(any(PromoCode.class));

        when(promoCodeUsageRepository.save(any(PromoCodeUsage.class))).thenReturn(new PromoCodeUsage());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    service.createOrder(request, "Bearer test-token");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        assertEquals(threadCount, successCount.get() + failCount.get());
        assertTrue(successCount.get() <= maxUses,
                "Maximo " + maxUses + " ordenes exitosas, pero hubo " + successCount.get());
        assertEquals(currentUses.get(), successCount.get(),
                "El contador de usos debe coincidir con las ordenes exitosas");
    }
}
