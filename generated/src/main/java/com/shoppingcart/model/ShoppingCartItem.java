package com.shoppingcart.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.shoppingcart.model.Product;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ShoppingCartItem
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen")
public class ShoppingCartItem {

  private String id;

  private BigDecimal subtotal;

  @Valid
  private List<@Valid Product> products;

  public ShoppingCartItem id(String id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
  */
  
  @Schema(name = "id", example = "d290f1ee-6c54-4b01-90e6-d701748f0851", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("id")
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public ShoppingCartItem subtotal(BigDecimal subtotal) {
    this.subtotal = subtotal;
    return this;
  }

  /**
   * Get subtotal
   * @return subtotal
  */
  @Valid 
  @Schema(name = "subtotal", example = "10", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("subtotal")
  public BigDecimal getSubtotal() {
    return subtotal;
  }

  public void setSubtotal(BigDecimal subtotal) {
    this.subtotal = subtotal;
  }

  public ShoppingCartItem products(List<@Valid Product> products) {
    this.products = products;
    return this;
  }

  public ShoppingCartItem addProductsItem(Product productsItem) {
    if (this.products == null) {
      this.products = new ArrayList<>();
    }
    this.products.add(productsItem);
    return this;
  }

  /**
   * Get products
   * @return products
  */
  @Valid 
  @Schema(name = "products", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("products")
  public List<@Valid Product> getProducts() {
    return products;
  }

  public void setProducts(List<@Valid Product> products) {
    this.products = products;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ShoppingCartItem shoppingCartItem = (ShoppingCartItem) o;
    return Objects.equals(this.id, shoppingCartItem.id) &&
        Objects.equals(this.subtotal, shoppingCartItem.subtotal) &&
        Objects.equals(this.products, shoppingCartItem.products);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, subtotal, products);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ShoppingCartItem {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    subtotal: ").append(toIndentedString(subtotal)).append("\n");
    sb.append("    products: ").append(toIndentedString(products)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

