package com.shoppingcart.demo.dao.entity;

import com.shoppingcart.model.Product;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * shopping_cart -> - product1
 *                  - product2
 *                  - product3
 *
 *
 */

@Data
@Entity(name = "shopping_cart")
public class ShoppingCartItemEntity {

    @Id
    @NotNull
    private String id;

    @OneToMany(mappedBy = "shoppingCartEntity",cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<ProductShoppingCartEntity> products = new ArrayList<>();

}
