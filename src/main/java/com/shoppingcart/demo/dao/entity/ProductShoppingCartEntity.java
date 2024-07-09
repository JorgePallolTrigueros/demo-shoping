package com.shoppingcart.demo.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@Entity(name = "product_shopping_cart")
public class ProductShoppingCartEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;//1,2,3,4,5,6,7,8,10

    @NotNull
    private Long productId;//1,1,1,1,1,1,1,1,1


    @ToString.Exclude
    @ManyToOne
    @JoinColumn(name="shopping_cart_id")
    private ShoppingCartItemEntity shoppingCartEntity;

    @NotNull
    @PositiveOrZero
    private BigDecimal quantity;//3,5,10,15,44,10,10,10


    public void setShoppingCartId(String shoppingCartId){
        this.shoppingCartEntity = new ShoppingCartItemEntity();
        this.shoppingCartEntity.setId(shoppingCartId);
    }
}
