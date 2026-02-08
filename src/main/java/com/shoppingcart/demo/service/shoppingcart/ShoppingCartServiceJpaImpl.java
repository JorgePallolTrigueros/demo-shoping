package com.shoppingcart.demo.service.shoppingcart;

import com.shoppingcart.demo.dao.entity.*;
import com.shoppingcart.demo.dao.repository.InvoiceEntityRepository;
import com.shoppingcart.demo.dao.repository.ProductShoppingCartRepository;
import com.shoppingcart.demo.dao.repository.ShoppingCartEntityRepository;
import com.shoppingcart.demo.dto.CampaignResponseDto;
import com.shoppingcart.demo.exception.ProductNotFoundException;
import com.shoppingcart.demo.exception.ShoppingCartInvalidProductsException;
import com.shoppingcart.demo.exception.ShoppingCartNotFoundException;

import com.shoppingcart.demo.service.campaign.CampaingApiService;
import com.shoppingcart.demo.service.product.ProductService;
import com.shoppingcart.model.*;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service

@Qualifier("ShoppingCartServiceJpa")
public class ShoppingCartServiceJpaImpl implements ShoppingCartService{

    private final ProductShoppingCartRepository productShoppingCartRepository;
    private final ShoppingCartEntityRepository shoppingCartEntityRepository;
    private final ProductService productService;
    private final InvoiceEntityRepository invoiceEntityRepository;
    private final JmsTemplate jmsTemplate;
    private final String destination;
    private final CampaingApiService campaingApiService;

    public ShoppingCartServiceJpaImpl(ProductShoppingCartRepository productShoppingCartRepository, ShoppingCartEntityRepository shoppingCartEntityRepository, ProductService productService, InvoiceEntityRepository invoiceEntityRepository, JmsTemplate jmsTemplate, CampaingApiService campaingApiService,@Value("${active-mq.queue}") String destination) {
        this.productShoppingCartRepository = productShoppingCartRepository;
        this.shoppingCartEntityRepository = shoppingCartEntityRepository;
        this.productService = productService;
        this.invoiceEntityRepository = invoiceEntityRepository;
        this.jmsTemplate = jmsTemplate;

        this.campaingApiService = campaingApiService;
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
                .map(shoppingCartItemEntity -> {

                    // Construcción básica del carrito de respuesta
                    final var item = new ShoppingCartItem();
                    item.setId(shoppingCartItemEntity.getId());

                    // Subtotal acumulado (mismo patrón que en getShoppingCartByUserId)
                    final AtomicReference<BigDecimal> subtotal = new AtomicReference<>(BigDecimal.ZERO);

                    // Obtener campañas por usuario (id del carrito == userId)
                    List<CampaignResponseDto> campaignResponse = new ArrayList<>();
                    if (StringUtils.isNotEmpty(item.getId())) {
                        campaignResponse = campaingApiService.getCampaignByUserId(item.getId());
                        log.info("Aplicando campanias: {} al usuario: {}", campaignResponse.size(), item.getId());
                    } else {
                        log.info("No se ha enviado usuario, no se aplican campanias");
                    }

                    final List<CampaignResponseDto> finalCampaignResponse = campaignResponse;

                    // 1) Convertir entidad -> Product
                    // 2) Aplicar campañas/descuentos (misma llamada que usas en el método individual)
                    List<Product> products = shoppingCartItemEntity
                            .getProducts()
                            .stream()
                            .map(this::getProductFromEntity)
                            .map(productWithoutCampaignApplied ->
                                    applyCampaignsDiscounts(
                                            item.getId(),                    // userId
                                            productWithoutCampaignApplied,   // producto base
                                            finalCampaignResponse           // campañas del usuario

                                    )
                            )
                            .toList();

                    // Setear productos y subtotal
                    item.setProducts(products);
                    item.setSubtotal(subtotal.get());

                    return item;
                })
                .toList();
    }
    @Override
    @Transactional // ✅ CRÍTICO: Asegurar que la transacción se complete
    public Optional<ShoppingCartItem> getShoppingCartByUserId(String userId) {
        log.info("🔍 Obteniendo carrito para usuario: {}", userId);

        Optional<ShoppingCartItemEntity> shoppingCartEntityOptional = shoppingCartEntityRepository.findById(userId);
        if (shoppingCartEntityOptional.isEmpty()) {
            log.info("❌ No existe carrito para usuario: {}", userId);
            return Optional.empty();
        }

        ShoppingCartItemEntity shoppingCartItemEntity = shoppingCartEntityOptional.get();

        // ✅ LOGGING: Ver qué hay en el carrito ANTES de filtrar
        log.info("📦 Productos en carrito ANTES de filtrar: {}", shoppingCartItemEntity.getProducts().size());
        shoppingCartItemEntity.getProducts().forEach(item -> {
            log.info("  - Producto ID: {}, Cantidad: {}", item.getProductId(), item.getQuantity());
        });

        // ✅ PASO 1: Identificar productos inválidos
        List<ProductShoppingCartEntity> invalidProducts = shoppingCartItemEntity.getProducts().stream()
                .filter(item -> {
                    boolean isInvalid = item.getQuantity() == null || item.getQuantity().compareTo(BigDecimal.ZERO) <= 0;
                    if (isInvalid) {
                        log.warn("⚠️ Producto INVÁLIDO detectado - ID: {}, Cantidad: {}",
                                item.getProductId(), item.getQuantity());
                    }
                    return isInvalid;
                })
                .toList();

        // ✅ PASO 2: Eliminar productos inválidos de la BD
        if (!invalidProducts.isEmpty()) {
            log.warn("🗑️ Eliminando {} productos inválidos del carrito de usuario {}",
                    invalidProducts.size(), userId);

            // IMPORTANTE: Eliminar de la colección en memoria
            shoppingCartItemEntity.getProducts().removeAll(invalidProducts);

            // IMPORTANTE: Hacer flush para asegurar que se persiste
            shoppingCartEntityRepository.saveAndFlush(shoppingCartItemEntity);

            log.info("✅ Productos inválidos eliminados y guardados en BD");

            // Verificar después de eliminar
            log.info("📦 Productos en carrito DESPUÉS de eliminar: {}",
                    shoppingCartItemEntity.getProducts().size());
        }

        // ✅ PASO 3: Obtener campañas
        List<CampaignResponseDto> campaignResponse = new ArrayList<>();
        if (StringUtils.isNotEmpty(userId)) {
            campaignResponse = campaingApiService.getCampaignByUserId(userId);
            log.info("🎯 Aplicando {} campañas al usuario: {}", campaignResponse.size(), userId);
        }

        // ✅ PASO 4: Procesar productos VÁLIDOS
        List<CampaignResponseDto> finalCampaignResponse = campaignResponse;

        List<Product> productList = shoppingCartItemEntity.getProducts().stream()
                // FILTRO 1: Solo productos con cantidad válida (> 0)
                .filter(item -> {
                    boolean isValid = item.getQuantity() != null && item.getQuantity().compareTo(BigDecimal.ZERO) > 0;
                    if (!isValid) {
                        log.error("❌ ESTO NO DEBERÍA PASAR: Producto {} con cantidad {} todavía en lista",
                                item.getProductId(), item.getQuantity());
                    }
                    return isValid;
                })
                // CONVERSIÓN: Obtener datos completos del producto
                .map(item -> {
                    log.debug("🔄 Convirtiendo producto ID: {} con cantidad: {}",
                            item.getProductId(), item.getQuantity());
                    var result = getProductFromEntity(item);

                    result.setQuantity(item.getQuantity());

                    return result;
                })
                // FILTRO 2: Solo productos que existen en BD
                .filter(product -> {
                    if (product == null) {
                        log.warn("⚠️ Producto no encontrado en BD, se omitirá del carrito");
                        return false;
                    }

                    // ✅ VALIDACIÓN ADICIONAL: Verificar overflow
                    if (product.getQuantity() != null && product.getQuantity().compareTo(new BigDecimal(Integer.MAX_VALUE)) > 0) {
                        log.error("❌ OVERFLOW detectado en producto {}: cantidad = {}",
                                product.getId(), product.getQuantity());
                        // Corregir overflow
                        product.setQuantity(BigDecimal.ONE);
                    }

                    return true;
                })
                // APLICAR CAMPAÑAS
                .map(productWithoutCampaignApplied -> {
                    Product result = applyCampaignsDiscounts(userId, productWithoutCampaignApplied, finalCampaignResponse);
                    log.debug("💰 Producto {} después de campañas - Precio: {}, Subtotal: {}",
                            result.getId(), result.getPrice(), result.getSubtotal());
                    return result;
                })
                .toList();

        // ✅ PASO 5: Calcular subtotal de forma segura
        BigDecimal subtotal = productList.stream()
                .map(Product::getSubtotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("💵 Subtotal calculado: {}", subtotal);

        // ✅ PASO 6: Construir respuesta
        ShoppingCartItem response = new ShoppingCartItem();
        response.setId(shoppingCartItemEntity.getId());
        response.setProducts(productList);
        response.setSubtotal(subtotal);

        log.info("✅ Carrito usuario {}: {} productos válidos, subtotal: {}",
                userId, productList.size(), subtotal);

        return Optional.of(response);
    }
    @Override
    @Transactional
    public ShoppingCartItem saveShoppingCart(String userId, ShoppingCartItemRequest request) {
        log.info("Sync carrito (replace-all) para usuario: {}", userId);

        // 1) Cargar o crear carrito vacío
        ShoppingCartItemEntity cart = shoppingCartEntityRepository.findById(userId)
                .orElseGet(() -> shoppingCartEntityRepository.save(newEmptyCart(userId)));

        // 2) Devolver stock del carrito anterior (solo cantidades válidas > 0)
        restoreStockFromOldCart(cart);

        // 3) Borrar productos antiguos del carrito (asociaciones)
        // Si tu mapping tiene orphanRemoval=true, esto es suficiente:
        cart.getProducts().clear();
        // Si NO tienes orphanRemoval, usa un repo hijo y descomenta:
        // productShoppingCartRepository.deleteByShoppingCartId(cart.getId());

        // 4) Normalizar request: merge duplicados + filtrar qty<=0
        Map<Long, Integer> desired = normalizeRequestedProducts(request);

        // 5) Reservar (restar) stock del nuevo carrito y construir items finales
        for (Map.Entry<Long, Integer> entry : desired.entrySet()) {
            Long productId = entry.getKey();
            int requestedQty = entry.getValue();

            int reservedQty = reserveStockSafely(productId, requestedQty); // tolerante a fallos

            if (reservedQty <= 0) {
                log.warn("No se pudo reservar stock para productId={}, solicitado={}", productId, requestedQty);
                continue;
            }

            ProductShoppingCartEntity item = new ProductShoppingCartEntity();
            item.setShoppingCartId(cart.getId());
            item.setProductId(productId);
            item.setQuantity(BigDecimal.valueOf(reservedQty));
            cart.getProducts().add(item);

            log.info("Stock productId={}. solicitado={}, reservado={}", productId, requestedQty, reservedQty);

            if (reservedQty != requestedQty) {
                log.warn("Stock insuficiente productId={}. solicitado={}, reservado={}", productId, requestedQty, reservedQty);
            }
        }

        // 6) Guardar y responder
        shoppingCartEntityRepository.saveAndFlush(cart);
        log.info("Carrito sincronizado. Total productos: {}", cart.getProducts().size());

        return buildResponse(cart, userId);
    }

    private ShoppingCartItemEntity newEmptyCart(String userId) {
        ShoppingCartItemEntity c = new ShoppingCartItemEntity();
        c.setId(userId);
        c.setProducts(new ArrayList<>());
        return c;
    }

    /** Devuelve stock del carrito anterior y limpia cantidades inválidas sin tocar stock */
    private void restoreStockFromOldCart(ShoppingCartItemEntity cart) {
        if (cart.getProducts() == null || cart.getProducts().isEmpty()) return;

        for (ProductShoppingCartEntity oldItem : cart.getProducts()) {
            int oldQty = oldItem.getQuantity() == null ? 0 : oldItem.getQuantity().intValue();
            Long productId = oldItem.getProductId();

            if (oldQty > 0) {
                productService.increaseStockProduct(productId, oldQty);
            } else {
                log.warn("Producto {} tenía qty inválida en carrito anterior ({}). Se ignora para stock.", productId, oldQty);
            }
        }
    }

    /** Agrupa IDs duplicados y elimina qty<=0 para no guardar “vacíos” */
    private Map<Long, Integer> normalizeRequestedProducts(ShoppingCartItemRequest request) {
        if (request == null || request.getProducts() == null) return Map.of();

        return request.getProducts().stream()
                .filter(p -> p != null && p.getId() != null && p.getQuantity() != null)
                .collect(Collectors.toMap(
                        ProductRequest::getId,
                        p -> p.getQuantity().intValue(),
                        Integer::sum
                ))
                .entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Resta stock de forma tolerante:
     * - reserva min(requested, stockDisponible)
     * - devuelve cuánta cantidad se ha reservado realmente
     */
    private int reserveStockSafely(Long productId, int requestedQty) {
        if (requestedQty <= 0) return 0;

        int stockActual = productService.findProductById(productId)
                .map(p -> p.getQuantity().intValue())
                .orElse(0);

        int toReserve = Math.min(requestedQty, Math.max(stockActual, 0));
        if (toReserve <= 0) return 0;

        productService.reduceStockProducts(Map.of(productId, toReserve));
        return toReserve;
    }

    private ShoppingCartItem buildResponse(ShoppingCartItemEntity cart, String userId) {
        List<CampaignResponseDto> campaigns = StringUtils.isNotEmpty(userId)
                ? campaingApiService.getCampaignByUserId(userId)
                : Collections.emptyList();

        AtomicReference<BigDecimal> subtotalTotal = new AtomicReference<>(BigDecimal.ZERO);

        List<Product> productDtos = cart.getProducts().stream()
                .map(p-> {
                            var result = getProductFromEntity(p);
                            result.setQuantity(p.getQuantity());
                            return result;
                        }
                )
                .map(p -> {
                    Product discounted = applyCampaignsDiscounts(userId, p, campaigns);
                    // Asumiendo que el subtotal es (precioConDescuento * cantidad)
                    BigDecimal itemTotal = discounted.getPrice().multiply(p.getQuantity());
                    subtotalTotal.set(subtotalTotal.get().add(itemTotal));
                    log.info("Carrito de compra: {} Stock de product {}, cantidad: {}",userId,p.getId(),p.getQuantity());
                    return discounted;
                })
                .toList();

        ShoppingCartItem response = new ShoppingCartItem();
        response.setId(userId);
        response.setProducts(productDtos);
        response.setSubtotal(subtotalTotal.get());
        return response;
    }

    @Override
    @Transactional
    public boolean deleteShoppingCartByUserId(String userId) {
        Optional<ShoppingCartItemEntity> shoppingCartEntityOptional = shoppingCartEntityRepository.findById(userId);

        if (shoppingCartEntityOptional.isPresent()) {
            ShoppingCartItemEntity shoppingCartEntity = shoppingCartEntityOptional.get();
            // Borra el carrito de compra de la base de datos
            //recorrer todos los productos y devolver la cantidad a producto

            for(var product:shoppingCartEntity.getProducts()){
                boolean resultIncreaseStock = productService.increaseStockProduct(product.getProductId(),product.getQuantity().intValue());
                System.out.println("Increase stock: "+product.getQuantity().intValue() + " ProductId: "+product.getProductId() + " Result: "+ resultIncreaseStock);
            }

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

        List<CampaignResponseDto> campaignResponse = campaingApiService.getCampaignByUserId(userId);


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
                    final Product productWithoutCampaign = getProductFromEntity(productRequest);


                    Product productFound = applyCampaignsDiscounts(userId, productWithoutCampaign, campaignResponse);


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
        invoiceItem.setEmail(shoppingCartItemEntity.getId());
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

    private Product getProductFromEntity(ProductShoppingCartEntity productShoppingCartEntity) {


        final Optional<Product> productResult = productService.findProductById(productShoppingCartEntity.getProductId());


        if(productResult.isEmpty()){
            throw new ProductNotFoundException("Product not found by id: "+ productShoppingCartEntity.getProductId());
        }

       return productResult.get();
    }



    private static Product applyCampaignsDiscounts(String userId, Product product, List<CampaignResponseDto> finalCampaignResponse) {
        if(finalCampaignResponse
                .stream()
                .noneMatch(c->c.getProducts().contains(product.getId()))){
            log.info("No se aplican descuentos para el producto: {}", product.getId());

            BigDecimal subtotal = product.getPrice().multiply(product.getQuantity());

            product.setOriginalPrice(product.getPrice());

            product.setSubtotal(subtotal);

            return product;
        }

        double totalDiscounts = finalCampaignResponse
                .stream()
                .filter(c->c.getProducts().contains(product.getId()))
                .mapToDouble(CampaignResponseDto::getDiscount).sum();
        final BigDecimal basePrice = product.getPrice();
        final BigDecimal discountPrice = basePrice.subtract (basePrice.multiply(BigDecimal.valueOf(totalDiscounts)));
        log.info("Producto: {} precio original: {} descuentos sumados: {} precio final: {} para usuario: {}", product.getId(),basePrice,totalDiscounts,discountPrice, userId);
        product.setPrice(discountPrice);
        product.setOriginalPrice(basePrice);


        BigDecimal subtotal = product.getPrice().multiply(product.getQuantity());

        product.setSubtotal(subtotal);

        return product;
    }

}
