package com.example.productservice.controller.user;

import com.example.productservice.dto.PagedResponse;
import com.example.productservice.dto.ProductDto;
import com.example.productservice.logic.ProductLogic;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api")
public class UserProductController {

	private final ProductLogic productLogic;

	@Autowired
	public UserProductController(ProductLogic productLogic) {
		this.productLogic = productLogic;
	}

	@Operation(summary = "Get product list", description = "Returns paginated list of products. Optional filters: category (slug) and keyword.")
	@ApiResponse(responseCode = "200", description = "Paged product list", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class)))
	@GetMapping("/products")
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
}
