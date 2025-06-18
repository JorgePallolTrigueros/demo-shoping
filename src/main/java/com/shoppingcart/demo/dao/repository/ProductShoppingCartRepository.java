package com.shoppingcart.demo.dao.repository;

import com.shoppingcart.demo.dao.entity.ProductShoppingCartEntity;
import com.shoppingcart.demo.dao.entity.ShoppingCartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductShoppingCartRepository extends JpaRepository<ProductShoppingCartEntity,String> {
}
