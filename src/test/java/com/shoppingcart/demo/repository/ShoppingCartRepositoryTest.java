package com.shoppingcart.demo.repository;

import com.shoppingcart.demo.dao.entity.ProductShoppingCartEntity;
import com.shoppingcart.demo.dao.entity.ShoppingCartItemEntity;
import com.shoppingcart.demo.dao.repository.ShoppingCartEntityRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class ShoppingCartRepositoryTest {
    
    @Autowired
    private ShoppingCartEntityRepository shoppingCartEntityRepository;

    @Test
    @Transactional
    public void testSaveProductEntity(){
        var shoppingCart = new ShoppingCartItemEntity();
        shoppingCart.setId("jorge");

        shoppingCart = shoppingCartEntityRepository.saveAndFlush(shoppingCart);



        var productShoppingCart = new ProductShoppingCartEntity();
        productShoppingCart.setProductId(1L);
        productShoppingCart.setQuantity(BigDecimal.ONE);
        productShoppingCart.setShoppingCartId(shoppingCart.getId());

        shoppingCart.setProducts(new ArrayList<>(List.of(productShoppingCart)));

        System.out.println("Shoppping cart: "+shoppingCart);
        shoppingCart = shoppingCartEntityRepository.saveAndFlush(shoppingCart);
        System.out.println("Shoppping cart: "+shoppingCart);
    }

}
