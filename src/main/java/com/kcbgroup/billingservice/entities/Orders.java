package com.kcbgroup.billingservice.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ORDERS")
@RequiredArgsConstructor
@Getter
@Setter
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  long id;
    private String orderId;
    private String customerName;
    private String productId;
    private String productName;
    private Double productPrice;
    private int quantity;
    private String customerPhone;
    private String customerAddress;


}
