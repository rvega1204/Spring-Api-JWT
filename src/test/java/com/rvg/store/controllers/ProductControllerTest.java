package com.rvg.store.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rvg.store.dtos.ProductDto;
import com.rvg.store.entities.Category;
import com.rvg.store.entities.Product;
import com.rvg.store.mappers.ProductMapper;
import com.rvg.store.repositories.CategoryRepository;
import com.rvg.store.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("ProductController Unit Tests")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private ProductMapper productMapper;

    @MockitoBean
    private CategoryRepository categoryRepository;

    private Product testProduct;
    private ProductDto testProductDto;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category((byte) 1);
        testCategory.setName("Electronics");

        testProduct = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("Gaming Laptop")
                .price(new BigDecimal("1500.00"))
                .category(testCategory)
                .build();

        testProductDto = new ProductDto();
        testProductDto.setId(1L);
        testProductDto.setName("Laptop");
        testProductDto.setDescription("Gaming Laptop");
        testProductDto.setPrice(new BigDecimal("1500.00"));
        testProductDto.setCategoryId((byte) 1);
    }

    @Test
    @DisplayName("GET /products - Should return all products")
    void testGetAllProducts() throws Exception {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAllWithCategory()).thenReturn(products);
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);

        // When & Then
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Laptop")));

        verify(productRepository, times(1)).findAllWithCategory();
        verify(productMapper, times(1)).toDto(testProduct);
    }

    @Test
    @DisplayName("GET /products?categoryId=1 - Should filter by category")
    void testGetAllProductsByCategory() throws Exception {
        // Given
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findByCategoryId(anyByte())).thenReturn(products);
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);

        // When & Then
        mockMvc.perform(get("/products")
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(productRepository, times(1)).findByCategoryId((byte) 1);
    }

    @Test
    @DisplayName("GET /products/{id} - Should return product")
    void testGetProduct_Success() throws Exception {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productMapper.toDto(testProduct)).thenReturn(testProductDto);

        // When & Then
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Laptop")));

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /products/{id} - Should return 404")
    void testGetProduct_NotFound() throws Exception {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /products - Should create product")
    void testCreateProduct_Success() throws Exception {
        // Given
        ProductDto newDto = new ProductDto();
        newDto.setName("Mouse");
        newDto.setPrice(new BigDecimal("50.00"));
        newDto.setCategoryId((byte) 1);

        Product newProduct = Product.builder()
                .name("Mouse")
                .price(new BigDecimal("50.00"))
                .build();

        when(categoryRepository.findById((byte) 1)).thenReturn(Optional.of(testCategory));
        when(productMapper.toEntity(any())).thenReturn(newProduct);
        when(productRepository.save(any())).thenAnswer(inv -> {
            Product p = inv.getArgument(0);
            p.setId(2L);
            return p;
        });

        // When & Then
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        verify(productRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("POST /products - Should return 400 when category not found")
    void testCreateProduct_CategoryNotFound() throws Exception {
        // Given
        ProductDto newDto = new ProductDto();
        newDto.setCategoryId((byte) 99);

        when(categoryRepository.findById((byte) 99)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDto)))
                .andExpect(status().isBadRequest());

        verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("PUT /products/{id} - Should update product")
    void testUpdateProduct_Success() throws Exception {
        // Given
        ProductDto updateDto = new ProductDto();
        updateDto.setName("Updated");
        updateDto.setPrice(new BigDecimal("2000"));
        updateDto.setCategoryId((byte) 1);

        when(categoryRepository.findById((byte) 1)).thenReturn(Optional.of(testCategory));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any())).thenReturn(testProduct);

        // When & Then
        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        verify(productRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("DELETE /products/{id} - Should delete product")
    void testDeleteProduct_Success() throws Exception {
        // Given
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // When & Then
        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isNoContent());

        verify(productRepository, times(1)).delete(testProduct);
    }

    @Test
    @DisplayName("DELETE /products/{id} - Should return 404")
    void testDeleteProduct_NotFound() throws Exception {
        // Given
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/products/999"))
                .andExpect(status().isNotFound());

        verify(productRepository, never()).delete(any());
    }
}
