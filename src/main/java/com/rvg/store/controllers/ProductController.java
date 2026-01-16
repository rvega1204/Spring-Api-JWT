package com.rvg.store.controllers;

import com.rvg.store.dtos.ProductDto;
import com.rvg.store.entities.Product;
import com.rvg.store.mappers.ProductMapper;
import com.rvg.store.repositories.CategoryRepository;
import com.rvg.store.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * REST controller for managing products.
 * <p>
 * Provides endpoints for creating, retrieving, updating, and deleting products.
 * </p>
 */
@AllArgsConstructor
@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

    /**
     * Retrieves all products, optionally filtered by category.
     *
     * @param categoryId optional category ID to filter products
     * @return a list of {@link ProductDto} objects
     */
    @GetMapping
    public List<ProductDto> getAllProducts(@RequestParam(name = "categoryId", required = false) Byte categoryId) {
        List<Product> products;
        if (categoryId != null) {
            // Find products by category
            products = productRepository.findByCategoryId(categoryId);
        } else {
            // Find all products
            products = productRepository.findAllWithCategory();
        }

        return products
                .stream()
                .map(productMapper::toDto)
                .toList();
    }

    /**
     * Retrieves a specific product by its ID.
     *
     * @param id the ID of the product
     * @return a {@link ResponseEntity} containing the {@link ProductDto} if found,
     *         or 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {
        var product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(productMapper.toDto(product));
    }

    /**
     * Creates a new product.
     *
     * @param productDto the product data
     * @param builder    {@link UriComponentsBuilder} for constructing the location
     *                   URI
     * @return a {@link ResponseEntity} with the created product and HTTP 201
     *         Created status, or 400 Bad Request if category is invalid
     */
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto,
            UriComponentsBuilder builder) {
        // Validate category exists
        var category = categoryRepository.findById(productDto.getCategoryId()).orElse(null);
        if (category == null) {
            return ResponseEntity.badRequest().build();
        }

        var product = productMapper.toEntity(productDto);
        product.setCategory(category);
        productRepository.save(product);
        productDto.setId(product.getId());

        var uri = builder.path("/products/{id}").buildAndExpand(productDto.getId()).toUri();

        return ResponseEntity.created(uri).body(productDto);

    }

    /**
     * Updates an existing product.
     *
     * @param id         the ID of the product to update
     * @param productDto the updated product data
     * @return a {@link ResponseEntity} with the updated product, or 400/404 based
     *         on validation
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable(name = "id") Long id,
            @RequestBody ProductDto productDto) {
        // Validate category exists
        var category = categoryRepository.findById(productDto.getCategoryId()).orElse(null);
        if (category == null) {
            return ResponseEntity.badRequest().build();
        }

        var product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        productMapper.update(productDto, product);
        product.setCategory(category);
        productRepository.save(product);
        productDto.setId(product.getId());

        return ResponseEntity.ok(productDto);
    }

    /**
     * Deletes a product by its ID.
     *
     * @param id the ID of the product to delete
     * @return a {@link ResponseEntity} with 204 No Content if successful, or 404
     *         Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable(name = "id") Long id) {
        // Check if product exists
        var product = productRepository.findById(id).orElse(null);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }

        productRepository.delete(product);
        return ResponseEntity.noContent().build();
    }
}
