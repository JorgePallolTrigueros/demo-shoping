package com.shoppingcart.demo.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "gallery")
public class GalleryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotEmpty
    private String url;

    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name="product_id", nullable=false, updatable=false)
    private ProductEntity productEntity;

    public void setProductEntityById(){
    }
}
