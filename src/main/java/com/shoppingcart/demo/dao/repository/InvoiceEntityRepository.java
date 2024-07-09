package com.shoppingcart.demo.dao.repository;

import com.shoppingcart.demo.dao.entity.InvoiceEntity;
import com.shoppingcart.demo.dao.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface InvoiceEntityRepository extends JpaRepository<InvoiceEntity, String> {

}