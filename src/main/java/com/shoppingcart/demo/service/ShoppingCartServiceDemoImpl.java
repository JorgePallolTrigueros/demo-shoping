package com.shoppingcart.demo.service;

import com.shoppingcart.demo.exception.DuplicatedProductIdsException;
import com.shoppingcart.model.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@Qualifier("ShoppingCartServiceDemo")
public class ShoppingCartServiceDemoImpl implements ShoppingCartService{

    private List<ShoppingCartItem> items = new ArrayList<>();

    @PostConstruct
    void init(){
        log.info("Se ha creado: ShoppingCartServiceDemoImpl");
    }

    @Override
    public List<ShoppingCartItem> getAllShoppingCarts() {
        return items;
    }

    @Override
    public Optional<ShoppingCartItem> getShoppingCartByUserId(String userId) {
        return items
                .stream()
                .filter(item->item.getId().equals(userId) )
                .findFirst();
    }

    @Override
    public ShoppingCartItem saveShoppingCart(String userId, ShoppingCartItemRequest shoppingCartItemRequest) {

        // Lista
        // 1
        // 1
        // 1
        // 2
        // 3

        final List<Long> ids = shoppingCartItemRequest
                .getProducts()
                .stream()
                .map(ProductRequest::getId)
                .collect(Collectors.toList());

        log.info("Ids: {}",ids);

        // Mapas
        // K llave - V valor
        // 1 -> 1,1,1 = agrupado 3
        // 2 -> 2     = agrupado 1
        // 3 -> 3     = agrupado 1

        Map<Long, List<Long> > groupedIds = ids
                .stream()
                .collect(Collectors.groupingBy(Function.identity(),Collectors.toList()));


        log.info("Map: {}",groupedIds);

        for ( List<Long> value : groupedIds.values()) {
            if(value.size()>1){
                throw new DuplicatedProductIdsException();
            }
        }

        AtomicReference<BigDecimal> subtotal = new AtomicReference<>(BigDecimal.ZERO);

        List<Product> products = shoppingCartItemRequest
                .getProducts()
                .stream()
                .map(productRequest -> {
                    Product product = new Product();
                    product.setId(productRequest.getId());
                    product.setName("DEMO_1");
                    product.setCategory("DEMO");
                    product.setDescription("DEMO");
                    product.setGalleries(Collections.emptyList());
                    product.setPrice(BigDecimal.valueOf(new Random().nextInt(50)+1));
                    product.setQuantity(productRequest.getQuantity());
                    product.setSubtotal(product.getPrice().multiply(product.getQuantity()));
                    subtotal.set(subtotal.get().add(product.getSubtotal()));
                    return product;
                })
                .toList();



        Optional<ShoppingCartItem> shoppingCartFound = getShoppingCartByUserId(userId);

        if(shoppingCartFound.isPresent()){
            int position = items.indexOf(shoppingCartFound.get());
            shoppingCartFound.get().setProducts(products);
            shoppingCartFound.get().setSubtotal(subtotal.get());
            items.set(position,shoppingCartFound.get() );//actualizar
            return shoppingCartFound.get();
        }

        ShoppingCartItem shoppingCartItem = new ShoppingCartItem();
        shoppingCartItem.setId(userId);
        shoppingCartItem.setProducts(products);
        shoppingCartItem.setSubtotal(subtotal.get());

        items.add(shoppingCartItem);//guardar

        return shoppingCartItem;
    }

    @Override
    public boolean deleteShoppingCartByUserId(String userId) {
        return items.removeIf( shoppingCartItem -> shoppingCartItem.getId().equals(userId) );
    }

    @Override
    public InvoiceShoppingCart buyShoppingCart(String userId) {
        //TODO guardar y restar los productos del inventario
        return null;
    }
}
