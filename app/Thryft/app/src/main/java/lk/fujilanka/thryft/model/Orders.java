package lk.fujilanka.thryft.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Orders {
    private String orderId;
    private String userId;
    private double totalAmount;
    private String status;
    private long orderDate;
    private List<OrderItem> orderItems;
    private Address shippingAddress;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderItem {
        private String productId;
        private double unitPrice;

    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Address {
        private String name;
        private String email;
        private String contact;
        private String address1;
        private String address2;
        private String city;
        private String postcode;
    }

}