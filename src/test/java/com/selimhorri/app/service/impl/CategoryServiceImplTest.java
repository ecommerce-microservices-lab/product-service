package com.selimhorri.app.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
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

import com.selimhorri.app.domain.Category;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.exception.wrapper.CategoryNotFoundException;
import com.selimhorri.app.repository.CategoryRepository;
import com.selimhorri.app.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryServiceImpl Tests")
class CategoryServiceImplTest {

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private ProductRepository productRepository;

	@InjectMocks
	private CategoryServiceImpl categoryService;

	private Category category;
	private CategoryDto categoryDto;
	private Category reservedCategory;
	private Category noCategory;

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

		reservedCategory = Category.builder()
				.categoryId(2)
				.categoryTitle("Deleted")
				.imageUrl("https://example.com/deleted.jpg")
				.build();

		noCategory = Category.builder()
				.categoryId(3)
				.categoryTitle("No Category")
				.imageUrl("https://example.com/no-category.jpg")
				.build();
	}

	@Test
	@DisplayName("Should find all non-reserved categories successfully")
	void testFindAll_Success() {
		// Given
		Category category2 = Category.builder()
				.categoryId(2)
				.categoryTitle("Clothing")
				.imageUrl("https://example.com/clothing.jpg")
				.build();

		List<Category> categories = Arrays.asList(category, category2);
		when(categoryRepository.findAllNonReserved()).thenReturn(categories);

		// When
		List<CategoryDto> result = categoryService.findAll();

		// Then
		assertNotNull(result);
		assertEquals(2, result.size());
		verify(categoryRepository, times(1)).findAllNonReserved();
	}

	@Test
	@DisplayName("Should find category by id successfully")
	void testFindById_Success() {
		// Given
		when(categoryRepository.findNonReservedById(1)).thenReturn(Optional.of(category));

		// When
		CategoryDto result = categoryService.findById(1);

		// Then
		assertNotNull(result);
		assertEquals(1, result.getCategoryId());
		assertEquals("Electronics", result.getCategoryTitle());
		verify(categoryRepository, times(1)).findNonReservedById(1);
	}

	@Test
	@DisplayName("Should throw CategoryNotFoundException when category not found")
	void testFindById_NotFound() {
		// Given
		when(categoryRepository.findNonReservedById(999)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(CategoryNotFoundException.class, () -> categoryService.findById(999));
		verify(categoryRepository, times(1)).findNonReservedById(999);
	}

	@Test
	@DisplayName("Should save category successfully")
	void testSave_Success() {
		// Given
		CategoryDto newCategoryDto = CategoryDto.builder()
				.categoryTitle("Books")
				.imageUrl("https://example.com/books.jpg")
				.build();

		Category newCategory = Category.builder()
				.categoryTitle("Books")
				.imageUrl("https://example.com/books.jpg")
				.build();

		Category savedCategory = Category.builder()
				.categoryId(4)
				.categoryTitle("Books")
				.imageUrl("https://example.com/books.jpg")
				.build();

		when(categoryRepository.existsByCategoryTitleIgnoreCase("Books")).thenReturn(false);
		when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

		// When
		CategoryDto result = categoryService.save(newCategoryDto);

		// Then
		assertNotNull(result);
		assertEquals(4, result.getCategoryId());
		assertEquals("Books", result.getCategoryTitle());
		verify(categoryRepository, times(1)).existsByCategoryTitleIgnoreCase("Books");
		verify(categoryRepository, times(1)).save(any(Category.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when category title is null")
	void testSave_NullCategoryTitle() {
		// Given
		CategoryDto invalidCategoryDto = CategoryDto.builder()
				.categoryTitle(null)
				.imageUrl("https://example.com/books.jpg")
				.build();

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> categoryService.save(invalidCategoryDto));
		verify(categoryRepository, never()).save(any(Category.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when category title is empty")
	void testSave_EmptyCategoryTitle() {
		// Given
		CategoryDto invalidCategoryDto = CategoryDto.builder()
				.categoryTitle("")
				.imageUrl("https://example.com/books.jpg")
				.build();

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> categoryService.save(invalidCategoryDto));
		verify(categoryRepository, never()).save(any(Category.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when category title is only whitespace")
	void testSave_WhitespaceCategoryTitle() {
		// Given
		CategoryDto invalidCategoryDto = CategoryDto.builder()
				.categoryTitle("   ")
				.imageUrl("https://example.com/books.jpg")
				.build();

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> categoryService.save(invalidCategoryDto));
		verify(categoryRepository, never()).save(any(Category.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when category name already exists")
	void testSave_DuplicateCategoryTitle() {
		// Given
		CategoryDto newCategoryDto = CategoryDto.builder()
				.categoryTitle("Electronics")
				.imageUrl("https://example.com/electronics.jpg")
				.build();

		when(categoryRepository.existsByCategoryTitleIgnoreCase("Electronics")).thenReturn(true);

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> categoryService.save(newCategoryDto));
		verify(categoryRepository, times(1)).existsByCategoryTitleIgnoreCase("Electronics");
		verify(categoryRepository, never()).save(any(Category.class));
	}

	@Test
	@DisplayName("Should trim category title when saving")
	void testSave_TrimCategoryTitle() {
		// Given
		CategoryDto newCategoryDto = CategoryDto.builder()
				.categoryTitle("  Books  ")
				.imageUrl("https://example.com/books.jpg")
				.build();

		Category savedCategory = Category.builder()
				.categoryId(4)
				.categoryTitle("Books")
				.imageUrl("https://example.com/books.jpg")
				.build();

		when(categoryRepository.existsByCategoryTitleIgnoreCase("Books")).thenReturn(false);
		when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

		// When
		CategoryDto result = categoryService.save(newCategoryDto);

		// Then
		assertNotNull(result);
		assertEquals("Books", result.getCategoryTitle());
		verify(categoryRepository, times(1)).existsByCategoryTitleIgnoreCase("Books");
	}

	@Test
	@DisplayName("Should update category successfully")
	void testUpdate_Success() {
		// Given
		CategoryDto updatedCategoryDto = CategoryDto.builder()
				.categoryId(1)
				.categoryTitle("Updated Electronics")
				.imageUrl("https://example.com/updated-electronics.jpg")
				.build();

		Category updatedCategory = Category.builder()
				.categoryId(1)
				.categoryTitle("Updated Electronics")
				.imageUrl("https://example.com/updated-electronics.jpg")
				.build();

		when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
		when(categoryRepository.existsByCategoryTitleIgnoreCaseAndCategoryIdNot("Updated Electronics", 1))
				.thenReturn(false);
		when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

		// When
		CategoryDto result = categoryService.update(updatedCategoryDto);

		// Then
		assertNotNull(result);
		assertEquals(1, result.getCategoryId());
		assertEquals("Updated Electronics", result.getCategoryTitle());
		verify(categoryRepository, times(1)).findById(1);
		verify(categoryRepository, times(1))
				.existsByCategoryTitleIgnoreCaseAndCategoryIdNot("Updated Electronics", 1);
		verify(categoryRepository, times(1)).save(any(Category.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when category ID is null for update")
	void testUpdate_NullCategoryId() {
		// Given
		CategoryDto updatedCategoryDto = CategoryDto.builder()
				.categoryId(null)
				.categoryTitle("Updated Electronics")
				.imageUrl("https://example.com/updated-electronics.jpg")
				.build();

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> categoryService.update(updatedCategoryDto));
		verify(categoryRepository, never()).findById(anyInt());
		verify(categoryRepository, never()).save(any(Category.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when category title is null for update")
	void testUpdate_NullCategoryTitle() {
		// Given
		CategoryDto updatedCategoryDto = CategoryDto.builder()
				.categoryId(1)
				.categoryTitle(null)
				.imageUrl("https://example.com/updated-electronics.jpg")
				.build();

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> categoryService.update(updatedCategoryDto));
		verify(categoryRepository, never()).findById(anyInt());
		verify(categoryRepository, never()).save(any(Category.class));
	}

	@Test
	@DisplayName("Should throw CategoryNotFoundException when updating non-existent category")
	void testUpdate_CategoryNotFound() {
		// Given
		CategoryDto updatedCategoryDto = CategoryDto.builder()
				.categoryId(999)
				.categoryTitle("Updated Electronics")
				.imageUrl("https://example.com/updated-electronics.jpg")
				.build();

		when(categoryRepository.findById(999)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(CategoryNotFoundException.class, () -> categoryService.update(updatedCategoryDto));
		verify(categoryRepository, times(1)).findById(999);
		verify(categoryRepository, never()).save(any(Category.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when duplicate category name exists for update")
	void testUpdate_DuplicateCategoryTitle() {
		// Given
		CategoryDto updatedCategoryDto = CategoryDto.builder()
				.categoryId(1)
				.categoryTitle("Clothing")
				.imageUrl("https://example.com/clothing.jpg")
				.build();

		when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
		when(categoryRepository.existsByCategoryTitleIgnoreCaseAndCategoryIdNot("Clothing", 1))
				.thenReturn(true);

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> categoryService.update(updatedCategoryDto));
		verify(categoryRepository, times(1)).findById(1);
		verify(categoryRepository, times(1))
				.existsByCategoryTitleIgnoreCaseAndCategoryIdNot("Clothing", 1);
		verify(categoryRepository, never()).save(any(Category.class));
	}

	@Test
	@DisplayName("Should update category by id successfully")
	void testUpdateById_Success() {
		// Given
		CategoryDto updatedCategoryDto = CategoryDto.builder()
				.categoryTitle("Updated Electronics")
				.imageUrl("https://example.com/updated-electronics.jpg")
				.build();

		Category updatedCategory = Category.builder()
				.categoryId(1)
				.categoryTitle("Updated Electronics")
				.imageUrl("https://example.com/updated-electronics.jpg")
				.build();

		when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
		when(categoryRepository.existsByCategoryTitleIgnoreCaseAndCategoryIdNot("Updated Electronics", 1))
				.thenReturn(false);
		when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);

		// When
		CategoryDto result = categoryService.update(1, updatedCategoryDto);

		// Then
		assertNotNull(result);
		assertEquals(1, result.getCategoryId());
		assertEquals("Updated Electronics", result.getCategoryTitle());
		verify(categoryRepository, times(1)).findById(1);
		verify(categoryRepository, times(1))
				.existsByCategoryTitleIgnoreCaseAndCategoryIdNot("Updated Electronics", 1);
		verify(categoryRepository, times(1)).save(any(Category.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when category ID is null for update by id")
	void testUpdateById_NullCategoryId() {
		// Given
		CategoryDto updatedCategoryDto = CategoryDto.builder()
				.categoryTitle("Updated Electronics")
				.imageUrl("https://example.com/updated-electronics.jpg")
				.build();

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> categoryService.update(null, updatedCategoryDto));
		verify(categoryRepository, never()).findById(anyInt());
		verify(categoryRepository, never()).save(any(Category.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when category title is null for update by id")
	void testUpdateById_NullCategoryTitle() {
		// Given
		CategoryDto updatedCategoryDto = CategoryDto.builder()
				.categoryTitle(null)
				.imageUrl("https://example.com/updated-electronics.jpg")
				.build();

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> categoryService.update(1, updatedCategoryDto));
		verify(categoryRepository, never()).findById(anyInt());
		verify(categoryRepository, never()).save(any(Category.class));
	}

	@Test
	@DisplayName("Should throw CategoryNotFoundException when updating non-existent category by id")
	void testUpdateById_CategoryNotFound() {
		// Given
		CategoryDto updatedCategoryDto = CategoryDto.builder()
				.categoryTitle("Updated Electronics")
				.imageUrl("https://example.com/updated-electronics.jpg")
				.build();

		when(categoryRepository.findById(999)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(CategoryNotFoundException.class, () -> categoryService.update(999, updatedCategoryDto));
		verify(categoryRepository, times(1)).findById(999);
		verify(categoryRepository, never()).save(any(Category.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when duplicate category name exists for update by id")
	void testUpdateById_DuplicateCategoryTitle() {
		// Given
		CategoryDto updatedCategoryDto = CategoryDto.builder()
				.categoryTitle("Clothing")
				.imageUrl("https://example.com/clothing.jpg")
				.build();

		when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
		when(categoryRepository.existsByCategoryTitleIgnoreCaseAndCategoryIdNot("Clothing", 1))
				.thenReturn(true);

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> categoryService.update(1, updatedCategoryDto));
		verify(categoryRepository, times(1)).findById(1);
		verify(categoryRepository, times(1))
				.existsByCategoryTitleIgnoreCaseAndCategoryIdNot("Clothing", 1);
		verify(categoryRepository, never()).save(any(Category.class));
	}

	@Test
	@DisplayName("Should delete category by id successfully")
	void testDeleteById_Success() {
		// Given
		when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
		when(categoryRepository.findByCategoryTitleIgnoreCase("No Category"))
				.thenReturn(Optional.of(noCategory));

		// When
		categoryService.deleteById(1);

		// Then
		verify(categoryRepository, times(1)).findById(1);
		verify(categoryRepository, times(1)).findByCategoryTitleIgnoreCase("No Category");
		verify(productRepository, times(1)).updateCategoryForProducts(1, noCategory);
		verify(categoryRepository, times(1)).delete(category);
	}

	@Test
	@DisplayName("Should throw CategoryNotFoundException when deleting non-existent category")
	void testDeleteById_CategoryNotFound() {
		// Given
		when(categoryRepository.findById(999)).thenReturn(Optional.empty());

		// When & Then
		assertThrows(CategoryNotFoundException.class, () -> categoryService.deleteById(999));
		verify(categoryRepository, times(1)).findById(999);
		verify(categoryRepository, never()).findByCategoryTitleIgnoreCase(anyString());
		verify(productRepository, never()).updateCategoryForProducts(anyInt(), any(Category.class));
		verify(categoryRepository, never()).delete(any(Category.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when deleting reserved category 'Deleted'")
	void testDeleteById_ReservedCategoryDeleted() {
		// Given
		when(categoryRepository.findById(2)).thenReturn(Optional.of(reservedCategory));

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> categoryService.deleteById(2));
		verify(categoryRepository, times(1)).findById(2);
		verify(categoryRepository, never()).findByCategoryTitleIgnoreCase(anyString());
		verify(productRepository, never()).updateCategoryForProducts(anyInt(), any(Category.class));
		verify(categoryRepository, never()).delete(any(Category.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when deleting reserved category 'No Category'")
	void testDeleteById_ReservedCategoryNoCategory() {
		// Given
		when(categoryRepository.findById(3)).thenReturn(Optional.of(noCategory));

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> categoryService.deleteById(3));
		verify(categoryRepository, times(1)).findById(3);
		verify(categoryRepository, never()).findByCategoryTitleIgnoreCase(anyString());
		verify(productRepository, never()).updateCategoryForProducts(anyInt(), any(Category.class));
		verify(categoryRepository, never()).delete(any(Category.class));
	}

	@Test
	@DisplayName("Should throw IllegalArgumentException when deleting reserved category with case insensitive check")
	void testDeleteById_ReservedCategoryCaseInsensitive() {
		// Given
		Category deletedCategoryLower = Category.builder()
				.categoryId(2)
				.categoryTitle("deleted")
				.imageUrl("https://example.com/deleted.jpg")
				.build();

		when(categoryRepository.findById(2)).thenReturn(Optional.of(deletedCategoryLower));

		// When & Then
		assertThrows(IllegalArgumentException.class, () -> categoryService.deleteById(2));
		verify(categoryRepository, times(1)).findById(2);
		verify(categoryRepository, never()).findByCategoryTitleIgnoreCase(anyString());
		verify(productRepository, never()).updateCategoryForProducts(anyInt(), any(Category.class));
		verify(categoryRepository, never()).delete(any(Category.class));
	}

	@Test
	@DisplayName("Should throw IllegalStateException when 'No Category' category not found during deletion")
	void testDeleteById_NoCategoryNotFound() {
		// Given
		when(categoryRepository.findById(1)).thenReturn(Optional.of(category));
		when(categoryRepository.findByCategoryTitleIgnoreCase("No Category")).thenReturn(Optional.empty());

		// When & Then
		assertThrows(IllegalStateException.class, () -> categoryService.deleteById(1));
		verify(categoryRepository, times(1)).findById(1);
		verify(categoryRepository, times(1)).findByCategoryTitleIgnoreCase("No Category");
		verify(productRepository, never()).updateCategoryForProducts(anyInt(), any(Category.class));
		verify(categoryRepository, never()).delete(any(Category.class));
	}

}

