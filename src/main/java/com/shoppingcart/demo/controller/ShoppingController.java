package com.shoppingcart.demo.controller;

import com.shoppingcart.api.ShoppingCartApi;
import com.shoppingcart.demo.service.ShoppingCartService;
import com.shoppingcart.model.InvoiceShoppingCart;
import com.shoppingcart.model.ShoppingCartItem;
import com.shoppingcart.model.ShoppingCartItemRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1/shopping-cart")
public class ShoppingController implements ShoppingCartApi {

    /*
    se necesita implementar los metodos de la interfaz, para poder hacer uso de ellos y que no nos devuelva un 501
    no hace anotar con ningun valor como @Get o @Value etc los parametros por que la interfaz ya se encargo de anotarlos
    de esta manera nos queda un codigo mucho mas limpio
     */


    private final ShoppingCartService shoppingCartService;

    public ShoppingController(@Qualifier("ShoppingCartServiceJpa") ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }


    @Override
    public ResponseEntity<List<ShoppingCartItem>> getAllShoppingCarts() {
        return ResponseEntity.ok(shoppingCartService.getAllShoppingCarts()) ;
    }

    @Override
    public ResponseEntity<ShoppingCartItem> getShoppingCartByUserId(String userId) {

        Optional<ShoppingCartItem> result = shoppingCartService.getShoppingCartByUserId(userId);
        if(result.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result.get());
    }

    @Override
    public ResponseEntity<ShoppingCartItem> saveShoppingCart(String userId, ShoppingCartItemRequest shoppingCartItemRequest) {
        return ResponseEntity.ok(shoppingCartService.saveShoppingCart(userId,shoppingCartItemRequest));
    }

    @Override
    public ResponseEntity<InvoiceShoppingCart> buyShoppingCart(String userId) {
        return ResponseEntity.ok(shoppingCartService.buyShoppingCart(userId));
    }

    @Override
    public ResponseEntity<Void> deleteShoppingCartByUserId(String userId) {
        final boolean result  = shoppingCartService.deleteShoppingCartByUserId(userId);
        if(result){
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }




}
