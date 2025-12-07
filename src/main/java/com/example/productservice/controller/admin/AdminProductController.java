package com.example.productservice.controller.admin;

import com.example.productservice.dto.CategoryDto;
import com.example.productservice.dto.CreateCategoryRequest;
import com.example.productservice.dto.CreateProductRequest;
import com.example.productservice.dto.PagedResponse;
import com.example.productservice.dto.ProductDto;
import com.example.productservice.logic.ProductLogic;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import com.example.productservice.dto.UpdateCategoryRequest;
import com.example.productservice.dto.UpdateProductRequest;

@RestController
@RequestMapping("/admin")
@Validated
public class AdminProductController {

	private final ProductLogic productLogic;

	public AdminProductController(ProductLogic productLogic) {
		this.productLogic = productLogic;
	}

   @Operation(summary = "Get product list", description = "Returns paginated list of products. Optional filters: category (slug) and keyword.")
	@ApiResponse(responseCode = "200", description = "Paged product list", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class)))
	@GetMapping("/products")
   @PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<PagedResponse<ProductDto>> listProducts(
		@Parameter(description = "Category slug to filter") @RequestParam(required = false) String category,
		@Parameter(description = "Keyword to search in name, description or sku") @RequestParam(required = false) String keyword,
		@Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
		@Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size
	) {
		if (page < 0) {
			return ResponseEntity.badRequest().build();
		}
		if (size <= 0 || size > 200) {
			return ResponseEntity.badRequest().build();
		}
		PagedResponse<ProductDto> resp = productLogic.getProducts(category, keyword, page, size);
		return ResponseEntity.ok(resp);
	}

	@Operation(summary = "Create a category", description = "Create a new product category")
	@ApiResponse(responseCode = "201", description = "Category created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDto.class)))
	@PostMapping("/categories")
	public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CreateCategoryRequest req) {
		CategoryDto dto = productLogic.createCategory(req.getName(), req.getSlug());
		return ResponseEntity.status(201).body(dto);
	}

	@Operation(summary = "Update a category", description = "Update an existing product category")
	@ApiResponse(responseCode = "200", description = "Category updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CategoryDto.class)))
	@PutMapping("/categories/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest req) {
		CategoryDto dto = productLogic.updateCategory(id, req.getName(), req.getSlug());
		if (dto == null) return ResponseEntity.notFound().build();
		return ResponseEntity.ok(dto);
	}

	@Operation(summary = "Create a product", description = "Create a new product. images (urls) and categoryIds are optional")
	@ApiResponse(responseCode = "201", description = "Product created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class)))
	@PostMapping("/products")
	public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody CreateProductRequest req) {
		// basic status validation
		String st = req.getStatus();
		if (st != null && !st.equals("ACTIVE") && !st.equals("INACTIVE")) {
			return ResponseEntity.badRequest().build();
		}
		ProductDto dto = productLogic.createProduct(req);
		return ResponseEntity.status(201).body(dto);
	}

	@Operation(summary = "Update a product", description = "Update an existing product. Provide fields to change. images/categoryIds when provided replace existing relations.")
	@ApiResponse(responseCode = "200", description = "Product updated", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProductDto.class)))
	@PutMapping("/products/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest req) {
		// ensure id consistency
		if (req.getId() == null) req.setId(id);
		else if (!req.getId().equals(id)) return ResponseEntity.badRequest().build();
		String st = req.getStatus();
		if (st != null && !st.equals("ACTIVE") && !st.equals("INACTIVE")) {
			return ResponseEntity.badRequest().build();
		}
		ProductDto dto = productLogic.updateProduct(req);
		if (dto == null) return ResponseEntity.notFound().build();
		return ResponseEntity.ok(dto);
	}
}
