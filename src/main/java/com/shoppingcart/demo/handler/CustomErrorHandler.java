package com.shoppingcart.demo.handler;

import com.shoppingcart.demo.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@ControllerAdvice
public class CustomErrorHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { DuplicatedProductIdsException.class, ProductNotFoundException.class, ShoppingCartInvalidProductsException.class, ShoppingCartNotFoundException.class  })
    protected ResponseEntity<Object> handleBadRequest(
            RuntimeException ex, WebRequest request) {



        String bodyOfResponse = "Ocurrio un error en la entrada de datos";


        if(ex instanceof DuplicatedProductIdsException){
            bodyOfResponse = "Datos de productos duplicados";
        }
        if(ex instanceof ProductNotFoundException){
            bodyOfResponse = "Datos de productos no encontrados";
        }
        if(ex instanceof ShoppingCartInvalidProductsException){
            bodyOfResponse = "Carrito de compra con productos invalidos";
        }
        if(ex instanceof ShoppingCartNotFoundException){
            bodyOfResponse = "Carrito de compras no encontrado";
        }

        log.info("Error: {}",bodyOfResponse);

        return handleExceptionInternal(ex, new ErrorResponse(bodyOfResponse,HttpStatus.BAD_REQUEST.value()), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

}
