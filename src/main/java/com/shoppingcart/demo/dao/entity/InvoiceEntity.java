package com.shoppingcart.demo.dao.entity;

import com.shoppingcart.model.Product;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity(name = "invoice")
public class InvoiceEntity {

    @Id
    @NotNull
    private String id;

    private OffsetDateTime datetime;

    private String businessId;

    private String businessName;

    private BigDecimal subtotal;

    private BigDecimal total;

    private BigDecimal totalTax;

    private String taxDescription;

    private BigDecimal tax;

    @OneToMany(mappedBy = "invoiceEntity",cascade = CascadeType.ALL)
    private List<InvoiceProductEntity> products = new ArrayList<>();

}
