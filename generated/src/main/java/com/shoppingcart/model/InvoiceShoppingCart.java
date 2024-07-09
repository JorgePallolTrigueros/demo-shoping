package com.shoppingcart.model;

import java.net.URI;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.shoppingcart.model.Product;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.openapitools.jackson.nullable.JsonNullable;
import java.time.OffsetDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * InvoiceShoppingCart
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen")
public class InvoiceShoppingCart {

  private String id;

  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private OffsetDateTime datetime;

  private String businessId;

  private String businessName;

  private BigDecimal subtotal;

  private BigDecimal total;

  private BigDecimal totalTax;

  private String taxDescription;

  private BigDecimal tax;

  @Valid
  private List<@Valid Product> products;

  public InvoiceShoppingCart id(String id) {
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

  public InvoiceShoppingCart datetime(OffsetDateTime datetime) {
    this.datetime = datetime;
    return this;
  }

  /**
   * Get datetime
   * @return datetime
  */
  @Valid 
  @Schema(name = "datetime", example = "2024-03-16T15:28:42.183Z", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("datetime")
  public OffsetDateTime getDatetime() {
    return datetime;
  }

  public void setDatetime(OffsetDateTime datetime) {
    this.datetime = datetime;
  }

  public InvoiceShoppingCart businessId(String businessId) {
    this.businessId = businessId;
    return this;
  }

  /**
   * Get businessId
   * @return businessId
  */
  
  @Schema(name = "businessId", example = "B123456", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("businessId")
  public String getBusinessId() {
    return businessId;
  }

  public void setBusinessId(String businessId) {
    this.businessId = businessId;
  }

  public InvoiceShoppingCart businessName(String businessName) {
    this.businessName = businessName;
    return this;
  }

  /**
   * Get businessName
   * @return businessName
  */
  
  @Schema(name = "businessName", example = "tienda s.l.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("businessName")
  public String getBusinessName() {
    return businessName;
  }

  public void setBusinessName(String businessName) {
    this.businessName = businessName;
  }

  public InvoiceShoppingCart subtotal(BigDecimal subtotal) {
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

  public InvoiceShoppingCart total(BigDecimal total) {
    this.total = total;
    return this;
  }

  /**
   * Get total
   * @return total
  */
  @Valid 
  @Schema(name = "total", example = "10", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("total")
  public BigDecimal getTotal() {
    return total;
  }

  public void setTotal(BigDecimal total) {
    this.total = total;
  }

  public InvoiceShoppingCart totalTax(BigDecimal totalTax) {
    this.totalTax = totalTax;
    return this;
  }

  /**
   * Get totalTax
   * @return totalTax
  */
  @Valid 
  @Schema(name = "totalTax", example = "10", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("totalTax")
  public BigDecimal getTotalTax() {
    return totalTax;
  }

  public void setTotalTax(BigDecimal totalTax) {
    this.totalTax = totalTax;
  }

  public InvoiceShoppingCart taxDescription(String taxDescription) {
    this.taxDescription = taxDescription;
    return this;
  }

  /**
   * Get taxDescription
   * @return taxDescription
  */
  
  @Schema(name = "taxDescription", example = "IVA", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("taxDescription")
  public String getTaxDescription() {
    return taxDescription;
  }

  public void setTaxDescription(String taxDescription) {
    this.taxDescription = taxDescription;
  }

  public InvoiceShoppingCart tax(BigDecimal tax) {
    this.tax = tax;
    return this;
  }

  /**
   * Get tax
   * @return tax
  */
  @Valid 
  @Schema(name = "tax", example = "0.21", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("tax")
  public BigDecimal getTax() {
    return tax;
  }

  public void setTax(BigDecimal tax) {
    this.tax = tax;
  }

  public InvoiceShoppingCart products(List<@Valid Product> products) {
    this.products = products;
    return this;
  }

  public InvoiceShoppingCart addProductsItem(Product productsItem) {
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
    InvoiceShoppingCart invoiceShoppingCart = (InvoiceShoppingCart) o;
    return Objects.equals(this.id, invoiceShoppingCart.id) &&
        Objects.equals(this.datetime, invoiceShoppingCart.datetime) &&
        Objects.equals(this.businessId, invoiceShoppingCart.businessId) &&
        Objects.equals(this.businessName, invoiceShoppingCart.businessName) &&
        Objects.equals(this.subtotal, invoiceShoppingCart.subtotal) &&
        Objects.equals(this.total, invoiceShoppingCart.total) &&
        Objects.equals(this.totalTax, invoiceShoppingCart.totalTax) &&
        Objects.equals(this.taxDescription, invoiceShoppingCart.taxDescription) &&
        Objects.equals(this.tax, invoiceShoppingCart.tax) &&
        Objects.equals(this.products, invoiceShoppingCart.products);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, datetime, businessId, businessName, subtotal, total, totalTax, taxDescription, tax, products);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class InvoiceShoppingCart {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    datetime: ").append(toIndentedString(datetime)).append("\n");
    sb.append("    businessId: ").append(toIndentedString(businessId)).append("\n");
    sb.append("    businessName: ").append(toIndentedString(businessName)).append("\n");
    sb.append("    subtotal: ").append(toIndentedString(subtotal)).append("\n");
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
    sb.append("    totalTax: ").append(toIndentedString(totalTax)).append("\n");
    sb.append("    taxDescription: ").append(toIndentedString(taxDescription)).append("\n");
    sb.append("    tax: ").append(toIndentedString(tax)).append("\n");
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

