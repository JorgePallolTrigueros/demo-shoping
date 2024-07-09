package com.shoppingcart.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
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
 * Product
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen")
public class Product {

  private Long id;

  private BigDecimal quantity;

  private String name;

  private String category;

  private String description;

  @Valid
  private List<String> galleries;

  private BigDecimal price;

  private BigDecimal subtotal;

  public Product id(Long id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
  */
  
  @Schema(name = "id", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("id")
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Product quantity(BigDecimal quantity) {
    this.quantity = quantity;
    return this;
  }

  /**
   * Get quantity
   * @return quantity
  */
  @Valid 
  @Schema(name = "quantity", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("quantity")
  public BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  public Product name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
  */
  
  @Schema(name = "name", example = "producto 1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Product category(String category) {
    this.category = category;
    return this;
  }

  /**
   * Get category
   * @return category
  */
  
  @Schema(name = "category", example = "category 1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("category")
  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public Product description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description
   * @return description
  */
  
  @Schema(name = "description", example = "description 1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Product galleries(List<String> galleries) {
    this.galleries = galleries;
    return this;
  }

  public Product addGalleriesItem(String galleriesItem) {
    if (this.galleries == null) {
      this.galleries = new ArrayList<>();
    }
    this.galleries.add(galleriesItem);
    return this;
  }

  /**
   * Get galleries
   * @return galleries
  */
  
  @Schema(name = "galleries", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("galleries")
  public List<String> getGalleries() {
    return galleries;
  }

  public void setGalleries(List<String> galleries) {
    this.galleries = galleries;
  }

  public Product price(BigDecimal price) {
    this.price = price;
    return this;
  }

  /**
   * Get price
   * @return price
  */
  @Valid 
  @Schema(name = "price", example = "10", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("price")
  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public Product subtotal(BigDecimal subtotal) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Product product = (Product) o;
    return Objects.equals(this.id, product.id) &&
        Objects.equals(this.quantity, product.quantity) &&
        Objects.equals(this.name, product.name) &&
        Objects.equals(this.category, product.category) &&
        Objects.equals(this.description, product.description) &&
        Objects.equals(this.galleries, product.galleries) &&
        Objects.equals(this.price, product.price) &&
        Objects.equals(this.subtotal, product.subtotal);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, quantity, name, category, description, galleries, price, subtotal);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Product {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    quantity: ").append(toIndentedString(quantity)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    galleries: ").append(toIndentedString(galleries)).append("\n");
    sb.append("    price: ").append(toIndentedString(price)).append("\n");
    sb.append("    subtotal: ").append(toIndentedString(subtotal)).append("\n");
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

