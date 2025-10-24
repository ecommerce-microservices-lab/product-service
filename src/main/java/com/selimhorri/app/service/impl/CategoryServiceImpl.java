package com.selimhorri.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.selimhorri.app.domain.Category;
import com.selimhorri.app.dto.CategoryDto;
import com.selimhorri.app.exception.wrapper.CategoryNotFoundException;
import com.selimhorri.app.helper.CategoryMappingHelper;
import com.selimhorri.app.repository.CategoryRepository;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.service.CategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository categoryRepository;
	private final ProductRepository productRepository;

	@Override
	public List<CategoryDto> findAll() {
		return this.categoryRepository.findAllNonReserved()
				.stream()
				.map(CategoryMappingHelper::map)
				.collect(Collectors.toUnmodifiableList());
	}

	@Override
	public CategoryDto findById(final Integer categoryId) {
		return this.categoryRepository.findNonReservedById(categoryId)
				.map(CategoryMappingHelper::map)
				.orElseThrow(() -> new CategoryNotFoundException(
						String.format("Category with id: %d not found or is reserved", categoryId)));
	}

	@Override
	@Transactional
	public CategoryDto save(final CategoryDto categoryDto) {
		log.info("*** CategoryDto, service; save category *");

		if (categoryDto.getCategoryTitle() == null || categoryDto.getCategoryTitle().trim().isEmpty()) {
			throw new IllegalArgumentException("Category title cannot be empty or null");
		}

		String normalizedTitle = categoryDto.getCategoryTitle().trim();

		boolean nameExists = this.categoryRepository.existsByCategoryTitleIgnoreCase(normalizedTitle);
		if (nameExists) {
			throw new IllegalArgumentException("A category with this name already exists");
		}

		// Resetear relaciones e ID para asegurar que es una nueva categoría
		categoryDto.setParentCategoryDto(null);
		categoryDto.setSubCategoriesDtos(null);
		categoryDto.setCategoryId(null);

		// Guardar y mapear a DTO
		return CategoryMappingHelper.map(
				this.categoryRepository.save(CategoryMappingHelper.map(categoryDto)));
	}

	@Override
	@Transactional
	public CategoryDto update(final CategoryDto categoryDto) {
		log.info("*** CategoryDto, service; update category *");

		// Validaciones básicas
		if (categoryDto.getCategoryId() == null) {
			throw new IllegalArgumentException("Category ID cannot be null for update");
		}

		if (categoryDto.getCategoryTitle() == null || categoryDto.getCategoryTitle().trim().isEmpty()) {
			throw new IllegalArgumentException("Category title cannot be empty or null");
		}

		String normalizedTitle = categoryDto.getCategoryTitle().trim();

		// Verificar existencia de la categoría a actualizar
		Category existingCategory = this.categoryRepository.findById(categoryDto.getCategoryId())
				.orElseThrow(() -> new CategoryNotFoundException(
						"Category not found with ID: " + categoryDto.getCategoryId()));

		// Verificar si existe otra categoría con el mismo nombre (ignorando la actual)
		boolean nameExists = this.categoryRepository.existsByCategoryTitleIgnoreCaseAndCategoryIdNot(
				normalizedTitle, categoryDto.getCategoryId());

		if (nameExists) {
			throw new IllegalArgumentException("Another category with this name already exists");
		}

		// Actualizar campos
		existingCategory.setCategoryTitle(normalizedTitle);

		// Resetear relaciones (si es necesario)
		existingCategory.setParentCategory(null);
		existingCategory.setSubCategories(null);

		return CategoryMappingHelper.map(this.categoryRepository.save(existingCategory));
	}

	@Override
	@Transactional
	public CategoryDto update(final Integer categoryId, final CategoryDto categoryDto) {
		log.info("*** CategoryDto, service; update category with categoryId *");

		// Validaciones básicas
		if (categoryId == null) {
			throw new IllegalArgumentException("Category ID cannot be null");
		}

		if (categoryDto.getCategoryTitle() == null || categoryDto.getCategoryTitle().trim().isEmpty()) {
			throw new IllegalArgumentException("Category title cannot be empty or null");
		}

		String normalizedTitle = categoryDto.getCategoryTitle().trim();

		// Verificar existencia de la categoría a actualizar
		Category existingCategory = this.categoryRepository.findById(categoryId)
				.orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + categoryId));

		// Verificar si existe otra categoría con el mismo nombre (ignorando la actual)
		boolean nameExists = this.categoryRepository.existsByCategoryTitleIgnoreCaseAndCategoryIdNot(
				normalizedTitle, categoryId);

		if (nameExists) {
			throw new IllegalArgumentException("Another category with this name already exists");
		}

		// Actualizar campos
		existingCategory.setCategoryTitle(normalizedTitle);

		// Resetear relaciones (si es necesario)
		existingCategory.setParentCategory(null);
		existingCategory.setSubCategories(null);

		// No necesitamos mapear el DTO a entidad porque trabajamos con la existente
		return CategoryMappingHelper.map(this.categoryRepository.save(existingCategory));
	}

	@Override
	@Transactional
	public void deleteById(final Integer categoryId) {
		log.info("*** Void, service; delete category by id *");

		// 1. Verificar que la categoría exista
		Category category = this.categoryRepository.findById(categoryId)
				.orElseThrow(() -> new CategoryNotFoundException(
						"Category not found with ID: " + categoryId));

		// 2. Verificar que no sea una categoría reservada
		String categoryName = category.getCategoryTitle().toLowerCase().trim();
		if ("deleted".equals(categoryName) || "no category".equals(categoryName)) {
			throw new IllegalArgumentException(
					"Cannot delete reserved categories: 'Deleted' or 'No Category'");
		}

		// 3. Buscar la categoría "No Category"
		Category noCategory = this.categoryRepository.findByCategoryTitleIgnoreCase("No Category")
				.orElseThrow(() -> new IllegalStateException(
						"The 'No Category' category is required but not found in database"));

		// 4. Migrar todos los productos a "No Category"
		this.productRepository.updateCategoryForProducts(categoryId, noCategory);

		// 5. Eliminar la categoría
		this.categoryRepository.delete(category);
	}

}
