/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/Repository.java to edit this template
 */
package com.paymentchain.customer.repository;

import com.paymentchain.customer.entities.CustomerProduct;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 *
 * @author Duoc
 */
public interface CustomerProductRepository extends JpaRepository<CustomerProduct, Long> {
    List<CustomerProduct> findByCustomerId(Long customerId);
    void deleteByCustomerId(Long customerId);
}
