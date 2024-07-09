package com.shoppingcart.demo.dao.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@Entity(name = "invoice_product")
public class InvoiceProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    private String name;

    private BigDecimal price;

    private String category;

    private BigDecimal quantity;

    private BigDecimal subtotal;

    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name="invoice_id")
    private InvoiceEntity invoiceEntity;
}
