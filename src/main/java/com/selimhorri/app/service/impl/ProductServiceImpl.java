package com.selimhorri.app.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.selimhorri.app.domain.Category;
import com.selimhorri.app.domain.Product;
import com.selimhorri.app.dto.ProductDto;
import com.selimhorri.app.exception.wrapper.CategoryNotFoundException;
import com.selimhorri.app.exception.wrapper.ProductNotFoundException;
import com.selimhorri.app.helper.ProductMappingHelper;
import com.selimhorri.app.repository.CategoryRepository;
import com.selimhorri.app.repository.ProductRepository;
import com.selimhorri.app.service.ProductService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.togglz.core.Feature;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.util.NamedFeature;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final FeatureManager manager;
    public static final Feature DISCOUNT_APPLIED = new NamedFeature("DISCOUNT_APPLIED");

    private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;

	@Override
	public List<ProductDto> findAll() {
		log.info("*** ProductDto List, service; fetch all products *");
        List<Product> products = this.productRepository.findAllWithoutDeleted();

        if (manager.isActive(DISCOUNT_APPLIED)) {
            products = applyDiscount(products);
        }

        return products.stream()
            .map(ProductMappingHelper::map)
            .distinct()
            .collect(Collectors.toUnmodifiableList());
	}

	@Override
	public ProductDto findById(final Integer productId) {
		log.info("*** ProductDto, service; fetch product by id *");
        Product product = this.productRepository.findByIdWithoutDeleted(productId)
            .orElseThrow(() -> new ProductNotFoundException(
                        String.format("Product with id: %d not found", productId)
            ));

        Product processed = manager.isActive(DISCOUNT_APPLIED)
                ? applyDiscount(product)
                : product;

		return ProductMappingHelper.map(processed);
	}

	@Override
	public ProductDto save(final ProductDto productDto) {
		log.info("*** ProductDto, service; save product *");

		// Validación de campos obligatorios
		if (productDto.getProductTitle() == null || productDto.getProductTitle().isEmpty()) {
			throw new IllegalArgumentException("El título del producto es requerido");
		}

		if (productDto.getImageUrl() == null || productDto.getImageUrl().isEmpty()) {
			throw new IllegalArgumentException("La URL de la imagen es requerida");
		}

		if (productDto.getSku() == null || productDto.getSku().isEmpty()) {
			throw new IllegalArgumentException("El SKU es requerido");
		}

		if (productDto.getPriceUnit() == null) {
			throw new IllegalArgumentException("El precio unitario es requerido");
		}

		if (productDto.getQuantity() == null) {
			throw new IllegalArgumentException("La cantidad es requerida");
		}

		if (productDto.getCategoryDto() == null || productDto.getCategoryDto().getCategoryId() == null) {
			throw new IllegalArgumentException("La categoría es requerida");
		}

		// Validar que la categoría exista (usando Integer como ID)
		Integer categoryId = productDto.getCategoryDto().getCategoryId();
		categoryRepository.findById(categoryId)
				.orElseThrow(() -> new CategoryNotFoundException("Categoría no encontrada con ID: " + categoryId));

		productDto.setProductId(null);
		return ProductMappingHelper.map(this.productRepository
				.save(ProductMappingHelper.map(productDto)));
	}

	@Override
	public ProductDto update(final ProductDto productDto) {
		log.info("*** ProductDto, service; update product *");

		// Validar que el producto exista
		if (productDto.getProductId() == null || !productRepository.existsById(productDto.getProductId())) {
			throw new ProductNotFoundException("Producto no encontrado con ID: " + productDto.getProductId());
		}

		return ProductMappingHelper.map(this.productRepository
				.save(ProductMappingHelper.map(productDto)));
	}

	@Override
	public ProductDto update(final Integer productId, final ProductDto productDto) {
		log.info("*** ProductDto, service; update product with productId *");

		// Verificar que el producto exista
		Product existingProduct = productRepository.findById(productId)
				.orElseThrow(() -> new ProductNotFoundException("Producto no encontrado con ID: " + productId));

		// Actualizar los campos del producto existente con los del DTO
		Product updatedProduct = ProductMappingHelper.map(productDto);
		updatedProduct.setProductId(existingProduct.getProductId()); // Asegurar que se mantiene el mismo ID

		return ProductMappingHelper.map(this.productRepository.save(updatedProduct));
	}

	@Override
	public void deleteById(final Integer productId) {
		log.info("*** Void, service; soft delete product by id *");

		// 1. Verificar si el producto existe
		Product product = this.productRepository.findByIdWithoutDeleted(productId)
				.orElseThrow(() -> new ProductNotFoundException("Product with id: " + productId + " not found"));

		// 2. Buscar la categoría "Deleted"
		Category deletedCategory = this.categoryRepository.findByCategoryTitle("Deleted")
				.orElseThrow(() -> new RuntimeException("Category 'Deleted' not found in database"));

		// 3. Actualizar la categoría del producto a "Deleted" (soft delete)
		product.setCategory(deletedCategory);
		this.productRepository.save(product);
	}
    
	private Product applyDiscount(Product product) {
		if (product == null) {
			return null;
		}

		Double price = product.getPriceUnit();
		if (price == null) {
			return product;
		}

		Double discounted = price * 0.8d;

		return Product.builder()
				.productId(product.getProductId())
				.productTitle(product.getProductTitle())
				.imageUrl(product.getImageUrl())
				.sku(product.getSku())
				.priceUnit(discounted)
				.quantity(product.getQuantity())
				.category(product.getCategory())
				.build();
	}

	private List<Product> applyDiscount(List<Product> products) {
		if (products == null) {
			return null;
		}

		return products.stream()
				.map(this::applyDiscount)
				.collect(Collectors.toUnmodifiableList());
	}
}
