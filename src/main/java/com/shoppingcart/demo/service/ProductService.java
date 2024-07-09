package com.shoppingcart.demo.service;

import com.shoppingcart.model.Product;

import java.util.Map;
import java.util.Optional;

public interface ProductService {

    Optional<Product> findProductById(Long id);

    boolean reduceStockProducts(Map<Long,Integer> productIds);// productd: 1 , quantity: 10 , productId: 2 , quantity: 5 ....


}
