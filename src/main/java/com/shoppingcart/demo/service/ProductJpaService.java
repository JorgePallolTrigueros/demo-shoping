package com.shoppingcart.demo.service;

import com.shoppingcart.demo.dao.entity.ProductEntity;
import com.shoppingcart.demo.dao.repository.ProductEntityRepository;
import com.shoppingcart.model.Product;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class ProductJpaService implements ProductService {

    private final ProductEntityRepository productEntityRepository;

    @Override
    public Optional<Product> findProductById(Long id) {
        return productEntityRepository.findById(id).map(
                productEntity -> {

                    final Product product = new Product();

                    product.setId(productEntity.getId());
                    product.setName(productEntity.getName());
                    product.setDescription(productEntity.getDescription());
                    product.setPrice(productEntity.getPrice());
                    product.setCategory(productEntity.getCategory());
                    product.setQuantity(productEntity.getQuantity());
                    product.setGalleries(productEntity.getGalleries());

                    return product;
                }
        );
    }

    @Override
    public boolean reduceStockProducts(Map<Long,Integer> productIds) {
        try {

            List<ProductEntity> products = productEntityRepository.findAllById(productIds.keySet());

            products.forEach(productEntity -> {
                final Integer quantity = productIds.get(productEntity.getId());

                // es por que la cantidad no se encontro en el mapa de request
                if(Objects.isNull(quantity)){
                    throw new IllegalArgumentException("Quantity not found for product id: "+productEntity.getId());
                }

                //
                if( productEntity.getQuantity().compareTo( BigDecimal.valueOf(quantity) ) < 0 ){
                    throw new IllegalArgumentException("Insuficent products ["+productEntity.getQuantity()+"] for quantity requested ["+quantity+"] for product: "+productEntity.getId());
                }
                productEntity.setQuantity( productEntity.getQuantity().subtract(BigDecimal.valueOf(quantity)) );
            });

            productEntityRepository.saveAll(products);

            return true;

        }catch (Exception exception){
            return false;
        }
    }
}
