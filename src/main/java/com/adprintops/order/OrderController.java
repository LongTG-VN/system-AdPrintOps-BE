package com.adprintops.order;

import com.adprintops.order.dto.CreateOrderItemRequest;
import com.adprintops.order.dto.CreateOrderRequest;
import com.adprintops.order.dto.OrderResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.createOrderFromPricing(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/code/{orderCode}")
    public ResponseEntity<OrderResponse> getOrderByCode(@PathVariable String orderCode) {
        return ResponseEntity.ok(orderService.getOrderByCode(orderCode));
    }

    @PutMapping("/{id}")
    @SuppressWarnings("unchecked")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        String status = payload.containsKey("status") ? (String) payload.get("status") : null;
        String note = payload.containsKey("note") ? (String) payload.get("note") : null;
        String recipientName = payload.containsKey("recipientName") ? (String) payload.get("recipientName") : null;
        String recipientPhone = payload.containsKey("recipientPhone") ? (String) payload.get("recipientPhone") : null;
        String recipientAddress = payload.containsKey("recipientAddress") ? (String) payload.get("recipientAddress") : null;

        BigDecimal totalAmount = payload.containsKey("totalAmount") && payload.get("totalAmount") != null
                ? new BigDecimal(payload.get("totalAmount").toString())
                : null;

        BigDecimal paidAmount = payload.containsKey("paidAmount") && payload.get("paidAmount") != null
                ? new BigDecimal(payload.get("paidAmount").toString())
                : null;

        String paymentStatus = payload.containsKey("paymentStatus") ? (String) payload.get("paymentStatus") : null;
        String paymentMethod = payload.containsKey("paymentMethod") ? (String) payload.get("paymentMethod") : null;

        List<CreateOrderItemRequest> items = null;
        if (payload.containsKey("items") && payload.get("items") instanceof List) {
            List<Map<String, Object>> itemMaps = (List<Map<String, Object>>) payload.get("items");
            items = new ArrayList<>();
            for (Map<String, Object> m : itemMaps) {
                BigDecimal w = m.get("widthCm") != null ? new BigDecimal(m.get("widthCm").toString()) : null;
                BigDecimal h = m.get("heightCm") != null ? new BigDecimal(m.get("heightCm").toString()) : null;
                Integer q = m.get("quantity") != null ? Integer.parseInt(m.get("quantity").toString()) : null;
                BigDecimal p = m.get("calculatedPrice") != null ? new BigDecimal(m.get("calculatedPrice").toString()) : null;

                items.add(new CreateOrderItemRequest(
                        (String) m.get("categoryCode"),
                        (String) m.get("productName"),
                        w,
                        h,
                        q,
                        (String) m.get("materialCode"),
                        p,
                        (String) m.get("specificationsJson")
                ));
            }
        }

        OrderResponse response = orderService.updateOrder(id, status, note, totalAmount, recipientName, recipientPhone, recipientAddress, paidAmount, paymentStatus, paymentMethod, items);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
