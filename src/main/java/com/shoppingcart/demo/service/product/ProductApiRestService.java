package com.shoppingcart.demo.service.product;

import com.shoppingcart.model.Product;

import java.util.Map;
import java.util.Optional;

public class ProductApiRestService implements ProductService{



    @Override
    public Optional<Product> findProductById(Long id) {
        return Optional.empty();
    }

    @Override
    public boolean reduceStockProducts(Map<Long, Integer> productIds) {
        return false;
    }

    @Override
    public boolean increaseStockProduct(Long productId, Integer quantity) {
        return false;
    }
}
