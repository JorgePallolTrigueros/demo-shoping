package com.shoppingcart.demo.service.shoppingcart;

import com.shoppingcart.demo.dao.entity.*;
import com.shoppingcart.demo.dao.repository.InvoiceEntityRepository;
import com.shoppingcart.demo.dao.repository.ShoppingCartEntityRepository;
import com.shoppingcart.demo.exception.ProductNotFoundException;
import com.shoppingcart.demo.exception.ShoppingCartInvalidProductsException;
import com.shoppingcart.demo.exception.ShoppingCartNotFoundException;

import com.shoppingcart.demo.service.product.ProductService;
import com.shoppingcart.model.InvoiceShoppingCart;
import com.shoppingcart.model.Product;
import com.shoppingcart.model.ShoppingCartItem;
import com.shoppingcart.model.ShoppingCartItemRequest;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service

@Qualifier("ShoppingCartServiceJpa")
public class ShoppingCartServiceJpaImpl implements ShoppingCartService{

    private final ShoppingCartEntityRepository shoppingCartEntityRepository;
    private final ProductService productService;
    private final InvoiceEntityRepository invoiceEntityRepository;
    private final JmsTemplate jmsTemplate;
    private final String destination;

    public ShoppingCartServiceJpaImpl(ShoppingCartEntityRepository shoppingCartEntityRepository, ProductService productService, InvoiceEntityRepository invoiceEntityRepository, JmsTemplate jmsTemplate,@Value("${active-mq.queue}") String destination) {
        this.shoppingCartEntityRepository = shoppingCartEntityRepository;
        this.productService = productService;
        this.invoiceEntityRepository = invoiceEntityRepository;
        this.jmsTemplate = jmsTemplate;
        this.destination = destination;
    }


    @PostConstruct
    void init(){
        log.info("Se ha creado: ShoppingCartServiceJpaImpl");
    }

    @Override
    public List<ShoppingCartItem> getAllShoppingCarts() {
        return shoppingCartEntityRepository
                .findAll()
                .stream()
                .map(
                shoppingCartItemEntity -> {
                    final var item  = new ShoppingCartItem();
                    item.setId(shoppingCartItemEntity.getId());
                    AtomicReference<BigDecimal> subtotal = new AtomicReference<>(BigDecimal.ZERO);
                    /*
                    id          - carrito1
                    subtotal    - 40
                    productos   -1  Producto1(1,2,3)
                                -2  Producto2(1,2,3)
                                -3  Producto3(1,2,3)

                     */
                    List<Product> products =  shoppingCartItemEntity
                            .getProducts()
                            .stream()
                            .map(productShoppingCartEntity -> getProductFromEntity(productShoppingCartEntity, subtotal))
                            .toList();

                    item.products(products);
                    item.subtotal(subtotal.get());

                    return item;
                })
                .toList();
    }

    @Override
    public Optional<ShoppingCartItem> getShoppingCartByUserId(String userId) {
        Optional<ShoppingCartItemEntity> shoppingCartEntityOptional = shoppingCartEntityRepository.findById(userId);

        if(shoppingCartEntityOptional.isEmpty()){
            return Optional.empty();
        }

        // existe el dato
        ShoppingCartItemEntity shoppingCartItemEntity = shoppingCartEntityOptional.get();
        ShoppingCartItem response = new ShoppingCartItem();

        AtomicReference<BigDecimal> subtotal =  new AtomicReference<>();
        subtotal.set(BigDecimal.ZERO);

        response.setId(shoppingCartItemEntity.getId());
        List<Product> productList = shoppingCartItemEntity
                .getProducts()
                .stream()
                .map(productShoppingCartEntity -> getProductFromEntity(productShoppingCartEntity, subtotal))
                .toList();

        response.setProducts(productList);
        response.setSubtotal(subtotal.get());
        return Optional.of(response);
    }


    @Override
    @Transactional
    public ShoppingCartItem saveShoppingCart(String userId, ShoppingCartItemRequest shoppingCartItemRequest) {

        /*
        userId - carrito1
        shoppingCartRequest:{
            - products: [
                    ProductRequest1(id,quantity), -> buscar product entity por id ProductEntity, calcular el subtotal
                    ProductRequest2(id,quantity),
                    ProductRequest3(id,quantity),
              ]
        }


        ----------------------------------
        id 1 - zumos melocoton 2 EUR - 2 CANT = 4 EUR
        id 2 - jamon serrano   3 EUR - 1 CANT = 3 EUR


        ------------ subtotal:                  7 EUR
         */


        Optional<ShoppingCartItemEntity> shoppingCartItemResult = shoppingCartEntityRepository.findById(userId);

        ShoppingCartItemEntity shoppingCartItemEntity;
        if(shoppingCartItemResult.isEmpty()){
            shoppingCartItemEntity = new ShoppingCartItemEntity();
            shoppingCartItemEntity.setId(userId);
            shoppingCartItemEntity.setProducts(new ArrayList<>());
        }else{
            shoppingCartItemEntity = shoppingCartItemResult.get();
        }


        //shoppingCartItem o vacio pero creado o con valores de que se obtuvo de la consulta a la base de datos


        // se busca los productos que he enviado como request en la base de datos
        // se guarda unicamente lo necesario en productEntity
        List<ProductShoppingCartEntity> productEntitiesFound = shoppingCartItemRequest
                .getProducts()
                .stream()
                .map(productRequest -> {
                    final Optional<Product> productResult = productService.findProductById(productRequest.getId());

                    if(productResult.isEmpty()){
                        throw new ProductNotFoundException("Product not found by id:" + productRequest.getId());
                    }

                    ProductShoppingCartEntity productShoppingCartEntity = new ProductShoppingCartEntity();

                    Product product = productResult.get();

                    productShoppingCartEntity.setProductId(product.getId());
                    productShoppingCartEntity.setQuantity(productRequest.getQuantity());
                    productShoppingCartEntity.setShoppingCartId(shoppingCartItemEntity.getId());

                    return productShoppingCartEntity;
                }).toList();



        shoppingCartItemEntity.getProducts().clear();
        shoppingCartItemEntity.getProducts().addAll(productEntitiesFound);

        // ahora viene el procesado de los subtotales y devolver la respuesta al usuario de todos los calculos realizados
        ShoppingCartItem response = new ShoppingCartItem();

        AtomicReference<BigDecimal> subtotal =  new AtomicReference<>();
        subtotal.set(BigDecimal.ZERO);

        response.setId(userId);
        response.setProducts(shoppingCartItemEntity
                .getProducts()
                .stream()
                .map(productShoppingCartEntity -> getProductFromEntity(productShoppingCartEntity, subtotal))
                .toList()
        );
        response.setSubtotal(subtotal.get());
        shoppingCartEntityRepository.saveAndFlush(shoppingCartItemEntity);

        return response;
    }

    @Override
    @Transactional
    public boolean deleteShoppingCartByUserId(String userId) {
        Optional<ShoppingCartItemEntity> shoppingCartEntityOptional = shoppingCartEntityRepository.findById(userId);

        if (shoppingCartEntityOptional.isPresent()) {
            ShoppingCartItemEntity shoppingCartEntity = shoppingCartEntityOptional.get();
            // Borra el carrito de compra de la base de datos
            shoppingCartEntityRepository.delete(shoppingCartEntity);
            return true;
        } else {
            return false; // El carrito de compra no se encontró para el usuario especificado
        }
    }

    @Override
    public InvoiceShoppingCart buyShoppingCart(String userId) {

        //aplicar validaciones

        Optional<ShoppingCartItemEntity> shoppingCartItemEntityOptional = this.shoppingCartEntityRepository.findById(userId);

        // 1. Que el carrito exista
        if(shoppingCartItemEntityOptional.isEmpty()){
            throw new ShoppingCartNotFoundException();
        }
        ShoppingCartItemEntity shoppingCartItemEntity = shoppingCartItemEntityOptional.get();


        /**
         * ----------------------------------------------------
         *           Temporal S.L.
         *           B11223344
         *
         *     Fecha: 16/04/2024 20:06:56
         *
         * ---------------------------------------------------
         *  DESC               |Cantidad| Precion Unit. | Subtotal
         *
         * Teclado Gamer       |   1    |     60        |  60 EUR
         * Mouse Gamer         |   2    |     30        |  60 EUR
         * Pantalla 29"        |   1    |    300        | 300 EUR
         *
         * ----------------------------------------------------
         *                    Resumen
         * Subtotal:      420  EUR
         * IVA:             21 %
         * Total IVA:   88,20  EUR
         * Total Pagar: 508,20 EUR
         *
         */

        InvoiceEntity invoiceEntity = new InvoiceEntity();
        invoiceEntity.setDatetime(OffsetDateTime.now());
        invoiceEntity.setId(userId+invoiceEntity.getDatetime().toEpochSecond());
        invoiceEntity.setBusinessName("Temporal S.L.");
        invoiceEntity.setBusinessId("B11223344");


        AtomicReference<BigDecimal> subtotal = new AtomicReference<>();
        subtotal.set(BigDecimal.ZERO);

        InvoiceEntity finalInvoiceEntity = invoiceEntity;
        List<InvoiceProductEntity> invoiceProductEntities = shoppingCartItemEntity
                .getProducts()
                .stream()
                .map(productRequest-> {
                    if(Objects.isNull(productRequest.getQuantity()) || productRequest.getQuantity().compareTo(BigDecimal.ZERO) <= 0){
                        throw new ShoppingCartInvalidProductsException();
                    }
                    final Product productFound = getProductFromEntity(productRequest,subtotal);
                    if(productFound.getQuantity().compareTo(productRequest.getQuantity()) < 0){
                        throw new IllegalArgumentException("Insufficient product to process shopping cart, product id: "+productRequest.getId());
                    }
                    final InvoiceProductEntity invoiceProductEntity = new InvoiceProductEntity();
                    invoiceProductEntity.setId(productFound.getId());
                    invoiceProductEntity.setName(productFound.getName());
                    invoiceProductEntity.setCategory(productFound.getCategory());
                    invoiceProductEntity.setDescription(productFound.getDescription());
                    invoiceProductEntity.setPrice(productFound.getPrice());
                    invoiceProductEntity.setSubtotal(productFound.getPrice().multiply(productRequest.getQuantity()));
                    invoiceProductEntity.setQuantity(productRequest.getQuantity());
                    invoiceProductEntity.setInvoiceEntity(finalInvoiceEntity);

                    return invoiceProductEntity;
                }).toList();

        invoiceEntity.setProducts(invoiceProductEntities);

        invoiceEntity.setSubtotal(subtotal.get());
        invoiceEntity.setTaxDescription("IVA");
        invoiceEntity.setTax(BigDecimal.valueOf(0.21));


        invoiceEntity.setTotalTax(invoiceEntity.getSubtotal().multiply( invoiceEntity.getTax()));
        invoiceEntity.setTotal( invoiceEntity.getTotalTax().add(invoiceEntity.getTotalTax()));


        invoiceEntity = invoiceEntityRepository.saveAndFlush(invoiceEntity);

        InvoiceShoppingCart invoiceItem = new InvoiceShoppingCart();
        invoiceItem.setId(invoiceEntity.getId());
        invoiceItem.setBusinessId(invoiceEntity.getBusinessId());
        invoiceItem.setBusinessName(invoiceEntity.getBusinessName());
        invoiceItem.setDatetime(invoiceEntity.getDatetime());
        invoiceItem.setSubtotal(invoiceEntity.getSubtotal());
        invoiceItem.setTax(invoiceEntity.getTax());
        invoiceItem.setTaxDescription(invoiceEntity.getTaxDescription());
        invoiceItem.setTotalTax(invoiceEntity.getTotalTax());
        invoiceItem.setTotal(invoiceEntity.getTotal());

        invoiceItem.setProducts(invoiceEntity.getProducts().stream().map(
                invoiceProductEntity -> {
                    Product product = new Product();

                    product.setId(invoiceProductEntity.getId());
                    product.setName(invoiceProductEntity.getName());
                    product.setDescription(invoiceProductEntity.getDescription());
                    product.setCategory(invoiceProductEntity.getCategory());
                    product.setPrice(invoiceProductEntity.getPrice());
                    product.setQuantity(invoiceProductEntity.getQuantity());
                    product.setSubtotal(invoiceProductEntity.getSubtotal());

                    return product;
                }

        ).toList());

        jmsTemplate.convertAndSend(destination,invoiceItem);


        return invoiceItem;
    }

    private Product getProductFromEntity(ProductShoppingCartEntity productShoppingCartEntity, AtomicReference<BigDecimal> subtotal) {
        BigDecimal subtotalProduct = BigDecimal.ZERO;

        final Optional<Product> productResult = productService.findProductById(productShoppingCartEntity.getProductId());


        if(productResult.isEmpty()){
            throw new ProductNotFoundException("Product not found by id: "+ productShoppingCartEntity.getProductId());
        }

        Product product = productResult.get();

        if( Objects.nonNull( product.getPrice()) && Objects.nonNull(product.getQuantity()) ){
            subtotalProduct = product.getPrice().multiply(product.getQuantity());
        }

        product.setSubtotal(subtotalProduct);

        subtotal.set( subtotal.get().add( subtotalProduct  ));

        return product;
    }

}
