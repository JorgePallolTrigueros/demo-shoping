package com.shoppingcart.demo.service;


import com.shoppingcart.model.InvoiceShoppingCart;
import com.shoppingcart.model.ShoppingCartItem;
import com.shoppingcart.model.ShoppingCartItemRequest;

import java.util.List;
import java.util.Optional;

public interface ShoppingCartService {

    List<ShoppingCartItem> getAllShoppingCarts();

    Optional<ShoppingCartItem> getShoppingCartByUserId(String userId);

    ShoppingCartItem saveShoppingCart(String userId, ShoppingCartItemRequest shoppingCartItemRequest);

    boolean deleteShoppingCartByUserId(String userId);


    InvoiceShoppingCart buyShoppingCart(String userId);
}
