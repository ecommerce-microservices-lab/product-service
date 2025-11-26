package com.selimhorri.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;

import com.selimhorri.app.domain.Category;
import com.selimhorri.app.domain.Product;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.exception.wrapper.CategoryNotFoundException;
import com.selimhorri.app.exception.wrapper.ProductNotFoundException;
import com.selimhorri.app.repository.CategoryRepository;
import com.selimhorri.app.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Tests")
class ProductServiceImplTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private FeatureManager featureManager;

	@InjectMocks
	private ProductServiceImpl productService;

	private Product product;
	private ProductDto productDto;
	private Category category;
	private CategoryDto categoryDto;

	@BeforeEach
	void setUp() {
		category = Category.builder()
				.categoryId(1)
				.categoryTitle("Electronics")
				.imageUrl("https://example.com/electronics.jpg")
				.build();

		categoryDto = CategoryDto.builder()
				.categoryId(1)
				.categoryTitle("Electronics")
				.imageUrl("https://example.com/electronics.jpg")
				.build();

		product = Product.builder()
				.productId(1)
				.productTitle("Laptop")
				.imageUrl("https://example.com/laptop.jpg")
				.sku("LAP-001")
				.priceUnit(999.99)
				.quantity(10)
				.category(category)
				.build();

		productDto = ProductDto.builder()
				.productId(1)
				.productTitle("Laptop")
				.imageUrl("https://example.com/laptop.jpg")
				.sku("LAP-001")
				.priceUnit(999.99)
				.quantity(10)
				.categoryDto(categoryDto)
				.build();

		// Mock FeatureManager to return false for isActive() by default
		lenient().when(featureManager.isActive(any(Feature.class))).thenReturn(false);
	}

	@Test
	@DisplayName("Should find all products successfully")
	void testFindAll_Success() {
		// Given
		Product product2 = Product.builder()
				.productId(2)
				.productTitle("Phone")
				.imageUrl("https://example.com/phone.jpg")
				.sku("PHN-001")
				.priceUnit(599.99)
				.quantity(5)
				.category(category)
				.build();

		List<Product> products = Arrays.asList(product, product2);
		when(productRepository.findAllWithoutDeleted()).thenReturn(products);

		// When
		List<ProductDto> result = productService.findAll();

		// Then
		assertNotNull(result);
		assertEquals(2, result.size());
		verify(productRepository, times(1)).findAllWithoutDeleted();
	}

	@Test
	@DisplayName("Should not apply discount when feature is disabled in findAll")
	void testFindAll_DiscountFeatureDisabled() {
		// Given
		when(featureManager.isActive(any(Feature.class))).thenReturn(false);

		Product product2 = Product.builder()
				.productId(2)
				.productTitle("Phone")
				.imageUrl("https://example.com/phone.jpg")
				.sku("PHN-001")
				.priceUnit(500.00)
				.quantity(5)
				.category(category)
				.build();

		List<Product> products = Arrays.asList(product, product2);
		when(productRepository.findAllWithoutDeleted()).thenReturn(products);

		// When
		List<ProductDto> result = productService.findAll();

		// Then
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(999.99, result.get(0).getPriceUnit());
		assertEquals(500.00, result.get(1).getPriceUnit());
		verify(featureManager, times(1)).isActive(any(Feature.class));
	}

	@Test
	@DisplayName("Should apply discount when feature is enabled in findAll")
	void testFindAll_DiscountFeatureEnabled() {
		// Given
		when(featureManager.isActive(any(Feature.class))).thenReturn(true);

		Product product2 = Product.builder()
				.productId(2)
				.productTitle("Phone")
				.imageUrl("https://example.com/phone.jpg")
				.sku("PHN-001")
				.priceUnit(500.00)
				.quantity(5)
				.category(category)
				.build();

		List<Product> products = Arrays.asList(product, product2);
		when(productRepository.findAllWithoutDeleted()).thenReturn(products);

		// When
		List<ProductDto> result = productService.findAll();

		// Then
		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(999.99 * 0.8, result.get(0).getPriceUnit());
		assertEquals(500.00 * 0.8, result.get(1).getPriceUnit());
		verify(featureManager, times(1)).isActive(any(Feature.class));
	}

	@Test
	@DisplayName("Should find product by id successfully")
	void testFindById_Success() {
		// Given
		when(productRepository.findByIdWithoutDeleted(1)).thenReturn(Optional.of(product));

		// When
		ProductDto result = productService.findById(1);

		// Then
		assertNotNull(result);
		assertEquals(1, result.getProductId());
		assertEquals("Laptop", result.getProductTitle());
		assertEquals("LAP-001", result.getSku());
		assertEquals(999.99, result.getPriceUnit());
		verify(productRepository, times(1)).findByIdWithoutDeleted(1);
	}

	@Test
	@DisplayName("Should apply discount when feature is enabled in findById")
	void testFindById_DiscountFeatureEnabled() {
		// Given
		when(featureManager.isActive(any(Feature.class))).thenReturn(true);
		when(productRepository.findByIdWithoutDeleted(1)).thenReturn(Optional.of(product));

		// When
		ProductDto result = productService.findById(1);

		// Then
		assertNotNull(result);
		assertEquals(1, result.getProductId());
		assertEquals(999.99 * 0.8, result.getPriceUnit());
		verify(featureManager, times(1)).isActive(any(Feature.class));
		verify(productRepository, times(1)).findByIdWithoutDeleted(1);
	}

	@Test
	@DisplayName("Should throw ProductNotFoundException when product not found")
	void testFindById_NotFound() {
		// Given
		when(productRepository.findByIdWithoutDeleted(999)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(ProductNotFoundException.class, () -> productService.findById(999));
		verify(productRepository, times(1)).findByIdWithoutDeleted(999);
	}

	@Test
	@DisplayName("Should save product successfully")
	void testSave_Success() {
		// Given
		ProductDto newProductDto = ProductDto.builder()
				.productTitle("Tablet")
				.imageUrl("https://example.com/tablet.jpg")
				.sku("TAB-001")
				.priceUnit(299.99)
				.quantity(15)
				.categoryDto(categoryDto)
				.build();

		Product newProduct = Product.builder()
				.productTitle("Tablet")
				.imageUrl("https://example.com/tablet.jpg")
				.sku("TAB-001")
				.priceUnit(299.99)
				.quantity(15)
				.category(category)
				.build();

		Product savedProduct = Product.builder()
				.productId(2)
				.productTitle("Tablet")
				.imageUrl("https://example.com/tablet.jpg")
				.sku("TAB-001")
				.priceUnit(299.99)
				.quantity(15)
				.category(category)
				.build();

		when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
		when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

		// When
		ProductDto result = productService.save(newProductDto);

		// Then
		assertNotNull(result);
		assertEquals(2, result.getProductId());
		assertEquals("Tablet", result.getProductTitle());
		verify(categoryRepository, times(1)).findById(1);
		verify(productRepository, times(1)).save(any(Product.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when product title is null")
	void testSave_NullProductTitle() {
		// Given
		ProductDto invalidProductDto = ProductDto.builder()
				.productTitle(null)
				.imageUrl("https://example.com/tablet.jpg")
				.sku("TAB-001")
				.priceUnit(299.99)
				.quantity(15)
				.categoryDto(categoryDto)
				.build();

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> productService.save(invalidProductDto));
		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when product title is empty")
	void testSave_EmptyProductTitle() {
		// Given
		ProductDto invalidProductDto = ProductDto.builder()
				.productTitle("")
				.imageUrl("https://example.com/tablet.jpg")
				.sku("TAB-001")
				.priceUnit(299.99)
				.quantity(15)
				.categoryDto(categoryDto)
				.build();

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> productService.save(invalidProductDto));
		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when image URL is null")
	void testSave_NullImageUrl() {
		// Given
		ProductDto invalidProductDto = ProductDto.builder()
				.productTitle("Tablet")
				.imageUrl(null)
				.sku("TAB-001")
				.priceUnit(299.99)
				.quantity(15)
				.categoryDto(categoryDto)
				.build();

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> productService.save(invalidProductDto));
		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when SKU is null")
	void testSave_NullSku() {
		// Given
		ProductDto invalidProductDto = ProductDto.builder()
				.productTitle("Tablet")
				.imageUrl("https://example.com/tablet.jpg")
				.sku(null)
				.priceUnit(299.99)
				.quantity(15)
				.categoryDto(categoryDto)
				.build();

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> productService.save(invalidProductDto));
		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when price unit is null")
	void testSave_NullPriceUnit() {
		// Given
		ProductDto invalidProductDto = ProductDto.builder()
				.productTitle("Tablet")
				.imageUrl("https://example.com/tablet.jpg")
				.sku("TAB-001")
				.priceUnit(null)
				.quantity(15)
				.categoryDto(categoryDto)
				.build();

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> productService.save(invalidProductDto));
		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when quantity is null")
	void testSave_NullQuantity() {
		// Given
		ProductDto invalidProductDto = ProductDto.builder()
				.productTitle("Tablet")
				.imageUrl("https://example.com/tablet.jpg")
				.sku("TAB-001")
				.priceUnit(299.99)
				.quantity(null)
				.categoryDto(categoryDto)
				.build();

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> productService.save(invalidProductDto));
		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when category is null")
	void testSave_NullCategory() {
		// Given
		ProductDto invalidProductDto = ProductDto.builder()
				.productTitle("Tablet")
				.imageUrl("https://example.com/tablet.jpg")
				.sku("TAB-001")
				.priceUnit(299.99)
				.quantity(15)
				.categoryDto(null)
				.build();

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> productService.save(invalidProductDto));
		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when category ID is null")
	void testSave_NullCategoryId() {
		// Given
		CategoryDto invalidCategoryDto = CategoryDto.builder()
				.categoryId(null)
				.categoryTitle("Electronics")
				.build();

		ProductDto invalidProductDto = ProductDto.builder()
				.productTitle("Tablet")
				.imageUrl("https://example.com/tablet.jpg")
				.sku("TAB-001")
				.priceUnit(299.99)
				.quantity(15)
				.categoryDto(invalidCategoryDto)
				.build();

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> productService.save(invalidProductDto));
		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	@DisplayName("Should throw CategoryNotFoundException when category does not exist")
	void testSave_CategoryNotFound() {
		// Given
		ProductDto newProductDto = ProductDto.builder()
				.productTitle("Tablet")
				.imageUrl("https://example.com/tablet.jpg")
				.sku("TAB-001")
				.priceUnit(299.99)
				.quantity(15)
				.categoryDto(categoryDto)
				.build();

		when(categoryRepository.findById(1)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(CategoryNotFoundException.class, () -> productService.save(newProductDto));
		verify(categoryRepository, times(1)).findById(1);
		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	@DisplayName("Should update product successfully")
	void testUpdate_Success() {
		// Given
		ProductDto updatedProductDto = ProductDto.builder()
				.productId(1)
				.productTitle("Updated Laptop")
				.imageUrl("https://example.com/updated-laptop.jpg")
				.sku("LAP-001")
				.priceUnit(1099.99)
				.quantity(8)
				.categoryDto(categoryDto)
				.build();

		Product updatedProduct = Product.builder()
				.productId(1)
				.productTitle("Updated Laptop")
				.imageUrl("https://example.com/updated-laptop.jpg")
				.sku("LAP-001")
				.priceUnit(1099.99)
				.quantity(8)
				.category(category)
				.build();

		when(productRepository.existsById(1)).thenReturn(true);
		when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

		// When
		ProductDto result = productService.update(updatedProductDto);

		// Then
		assertNotNull(result);
		assertEquals(1, result.getProductId());
		assertEquals("Updated Laptop", result.getProductTitle());
		verify(productRepository, times(1)).existsById(1);
		verify(productRepository, times(1)).save(any(Product.class));
	}

	@Test
	@DisplayName("Should throw ProductNotFoundException when updating non-existent product")
	void testUpdate_ProductNotFound() {
		// Given
		ProductDto updatedProductDto = ProductDto.builder()
				.productId(999)
				.productTitle("Updated Laptop")
				.imageUrl("https://example.com/updated-laptop.jpg")
				.sku("LAP-001")
				.priceUnit(1099.99)
				.quantity(8)
				.categoryDto(categoryDto)
				.build();

		when(productRepository.existsById(999)).thenReturn(false);

		// When & Then
		assertThrows(ProductNotFoundException.class, () -> productService.update(updatedProductDto));
		verify(productRepository, times(1)).existsById(999);
		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	@DisplayName("Should throw ProductNotFoundException when product ID is null")
	void testUpdate_NullProductId() {
		// Given
		ProductDto updatedProductDto = ProductDto.builder()
				.productId(null)
				.productTitle("Updated Laptop")
				.imageUrl("https://example.com/updated-laptop.jpg")
				.sku("LAP-001")
				.priceUnit(1099.99)
				.quantity(8)
				.categoryDto(categoryDto)
				.build();

		// When & Then
		assertThrows(ProductNotFoundException.class, () -> productService.update(updatedProductDto));
		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	@DisplayName("Should update product by id successfully")
	void testUpdateById_Success() {
		// Given
		ProductDto updatedProductDto = ProductDto.builder()
				.productTitle("Updated Laptop")
				.imageUrl("https://example.com/updated-laptop.jpg")
				.sku("LAP-001")
				.priceUnit(1099.99)
				.quantity(8)
				.categoryDto(categoryDto)
				.build();

		Product updatedProduct = Product.builder()
				.productId(1)
				.productTitle("Updated Laptop")
				.imageUrl("https://example.com/updated-laptop.jpg")
				.sku("LAP-001")
				.priceUnit(1099.99)
				.quantity(8)
				.category(category)
				.build();

		when(productRepository.findById(1)).thenReturn(Optional.of(product));
		when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

		// When
		ProductDto result = productService.update(1, updatedProductDto);

		// Then
		assertNotNull(result);
		assertEquals(1, result.getProductId());
		assertEquals("Updated Laptop", result.getProductTitle());
		verify(productRepository, times(1)).findById(1);
		verify(productRepository, times(1)).save(any(Product.class));
	}

	@Test
	@DisplayName("Should throw ProductNotFoundException when updating by non-existent id")
	void testUpdateById_ProductNotFound() {
		// Given
		ProductDto updatedProductDto = ProductDto.builder()
				.productTitle("Updated Laptop")
				.imageUrl("https://example.com/updated-laptop.jpg")
				.sku("LAP-001")
				.priceUnit(1099.99)
				.quantity(8)
				.categoryDto(categoryDto)
				.build();

		when(productRepository.findById(999)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(ProductNotFoundException.class, () -> productService.update(999, updatedProductDto));
		verify(productRepository, times(1)).findById(999);
		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	@DisplayName("Should delete product by id successfully (soft delete)")
	void testDeleteById_Success() {
		// Given
		Category deletedCategory = Category.builder()
				.categoryId(2)
				.categoryTitle("Deleted")
				.imageUrl("https://example.com/deleted.jpg")
				.build();

		when(productRepository.findByIdWithoutDeleted(1)).thenReturn(Optional.of(product));
		when(categoryRepository.findByCategoryTitle("Deleted")).thenReturn(Optional.of(deletedCategory));
		when(productRepository.save(any(Product.class))).thenReturn(product);

		// When
		productService.deleteById(1);

		// Then
		verify(productRepository, times(1)).findByIdWithoutDeleted(1);
		verify(categoryRepository, times(1)).findByCategoryTitle("Deleted");
		verify(productRepository, times(1)).save(any(Product.class));
	}

	@Test
	@DisplayName("Should throw ProductNotFoundException when deleting non-existent product")
	void testDeleteById_ProductNotFound() {
		// Given
		when(productRepository.findByIdWithoutDeleted(999)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(ProductNotFoundException.class, () -> productService.deleteById(999));
		verify(productRepository, times(1)).findByIdWithoutDeleted(999);
		verify(categoryRepository, never()).findByCategoryTitle(any());
		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	@DisplayName("Should throw RuntimeException when Deleted category not found")
	void testDeleteById_DeletedCategoryNotFound() {
		// Given
		when(productRepository.findByIdWithoutDeleted(1)).thenReturn(Optional.of(product));
		when(categoryRepository.findByCategoryTitle("Deleted")).thenReturn(Optional.empty());

		// When & Then
		assertThrows(RuntimeException.class, () -> productService.deleteById(1));
		verify(productRepository, times(1)).findByIdWithoutDeleted(1);
		verify(categoryRepository, times(1)).findByCategoryTitle("Deleted");
		verify(productRepository, never()).save(any(Product.class));
	}

}


