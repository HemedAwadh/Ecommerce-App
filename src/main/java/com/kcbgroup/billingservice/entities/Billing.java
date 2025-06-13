package com.kcbgroup.billingservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "BILLING")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Billing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long billingId;

    private String orderId;
    private String productId;
    private int quantity;
    private double unitPrice;
    private Double totalAmount;
    private String status;
    private String CheckoutRequestID;
    private String message;
    private String mpesaReceiptNumber;
    private String transactionDate;
    private String PhoneNumber;

}
