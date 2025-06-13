package com.kcbgroup.billingservice.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

    private String statusCode;
    private String statusDescription;
    private String messageCode;
    private String messageDescription;
    private PrimaryData primaryData;


    @Getter
    @Setter
    public static class PrimaryData {
        private String orderId;
        private String conversationId;
        private String customerName;
        private String customerAddress;
        private String customerPhone;

        private String productId;
        private String productName;
        private String productPrice;
        private int productQuantity;
        private double totalPrice;

    }
}
