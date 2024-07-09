package com.shoppingcart.demo.exception;

public class ProductNotFoundException extends RuntimeException{
    public ProductNotFoundException(String message){super(message);}
}
