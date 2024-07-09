package com.shoppingcart.demo.dao.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/*

carrito jon
    producto1:
        -id: 1
        -quantity: 3




carrito jorge
    producto1:
        -id: 1
        -quantity: 10


carrito fulano
    producto1:
        -id: 1
        -quantity: 50


 */

@Data
@Entity(name = "product")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @NotNull
    @NotEmpty
    private String name;

    @NotNull
    @PositiveOrZero
    private BigDecimal price;

    @NotNull
    @NotEmpty
    @Column(name = "category_id")
    private String category;

    @NotNull
    @PositiveOrZero
    private BigDecimal quantity;


    @ToString.Exclude
    @OneToMany(cascade = CascadeType.ALL,mappedBy = "productEntity")
    private List<GalleryEntity> galleriesEntity=new ArrayList<>();

    public List<String> getGalleries(){
        if(CollectionUtils.isEmpty(galleriesEntity)){
            return new ArrayList<>();
        }
        return galleriesEntity
                .stream()
                .filter(Objects::nonNull) // filtrar los objetos que no son nulos, osea me quedo con los objetos que no son nulos
                .map(GalleryEntity::getUrl)// me quedo con la url
                .filter(StringUtils::isNotBlank)// filtramos que la url obtenida no sea nula ni este en blanco
                .distinct()//nos quedamos con los valores distintos
                .toList();//devolvemos lista
    }

}
