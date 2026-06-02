/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/springframework/RestController.java to edit this template
 */
package com.paymentchain.product.controller;

import com.paymentchain.product.entities.Product;
import com.paymentchain.product.repository.ProductRepository;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

/**
 *
 * @author SSG
 */
@RestController
@RequestMapping("/product")
public class ProductRestController {
    
    @Autowired
    ProductRepository productRepository;
    
    @GetMapping()
    public List<Product> list() {
        return productRepository.findAll();
    }
    
    //public Object get(@PathVariable String id) {
    @GetMapping("/{id}")
    public Object get(@PathVariable("id") Long id) {
        Optional<Product> optionalCustomer = productRepository.findById(id);
        
        if(optionalCustomer.isPresent()) {
            Product retorno = optionalCustomer.get();
            return new ResponseEntity<>(retorno, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<>( HttpStatus.NOT_FOUND);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> put(@PathVariable Long id, @RequestBody Product input) {
        Optional<Product> optionalCustomer = productRepository.findById(id);
        
        if(optionalCustomer.isPresent())
        {
            Product newCustomer = optionalCustomer.get();
            newCustomer.setName(input.getName());
            newCustomer.setCode(input.getCode());
            Product retorno = productRepository.save(newCustomer);
            return new ResponseEntity<>(retorno, HttpStatus.OK);
        }
        else        
        {
            return new ResponseEntity<>( HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    public ResponseEntity<?> post(@RequestBody Product input) {
        Product retorno=  productRepository.save(input);
        return ResponseEntity.ok(retorno);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        productRepository.deleteById(id);
        return ResponseEntity.ok(HttpStatus.OK);
    }
    
}
