package com.zcl.service;

import com.zcl.dto.AdminProductVO;
import com.zcl.dto.PageResult;
import com.zcl.dto.ProductCreateRequest;
import com.zcl.entity.Product;
import com.zcl.entity.ProductCategory;
import com.zcl.repository.ProductCategoryRepository;
import com.zcl.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AdminProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    public AdminProductService(ProductRepository productRepository, ProductCategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public PageResult<AdminProductVO> getProductList(int page, int size, Integer status, Long categoryId, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> productPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            productPage = productRepository.findByProductNameContaining(keyword.trim(), pageable);
        } else if (categoryId != null && status != null) {
            productPage = productRepository.findByCategoryIdAndStatus(categoryId, status, pageable);
        } else if (categoryId != null) {
            productPage = productRepository.findByCategoryId(categoryId, pageable);
        } else if (status != null) {
            productPage = productRepository.findByStatus(status, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        return PageResult.<AdminProductVO>builder()
                .content(productPage.getContent().stream().map(this::toVO).toList())
                .totalElements(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    public AdminProductVO getProductDetail(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        return toVO(product);
    }

    public Product createProduct(ProductCreateRequest request) {
        ProductCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("分类不存在"));

        Product product = new Product();
        product.setCategory(category);
        product.setProductName(request.getProductName());
        product.setProductImage(request.getProductImage());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock() != null ? request.getStock() : 0);
        product.setProductType(request.getProductType() != null ? request.getProductType() : 0);
        product.setHasOptions(request.getHasOptions() != null ? request.getHasOptions() : 0);
        product.setIsRecommend(request.getIsRecommend() != null ? request.getIsRecommend() : 0);
        product.setIsHot(request.getIsHot() != null ? request.getIsHot() : 0);
        product.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        product.setSalesCount(0);
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, ProductCreateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));

        if (request.getCategoryId() != null) {
            ProductCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("分类不存在"));
            product.setCategory(category);
        }
        if (request.getProductName() != null) product.setProductName(request.getProductName());
        if (request.getProductImage() != null) product.setProductImage(request.getProductImage());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getStock() != null) product.setStock(request.getStock());
        if (request.getProductType() != null) product.setProductType(request.getProductType());
        if (request.getHasOptions() != null) product.setHasOptions(request.getHasOptions());
        if (request.getIsRecommend() != null) product.setIsRecommend(request.getIsRecommend());
        if (request.getIsHot() != null) product.setIsHot(request.getIsHot());
        if (request.getStatus() != null) product.setStatus(request.getStatus());

        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        product.setStatus(0);
        productRepository.save(product);
    }

    public void toggleProductStatus(Long id, Integer status) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        product.setStatus(status);
        productRepository.save(product);
    }

    private AdminProductVO toVO(Product product) {
        return AdminProductVO.builder()
                .id(product.getId())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getCategoryName())
                .productName(product.getProductName())
                .productImage(product.getProductImage())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .salesCount(product.getSalesCount())
                .productType(product.getProductType())
                .hasOptions(product.getHasOptions())
                .isRecommend(product.getIsRecommend())
                .isHot(product.getIsHot())
                .status(product.getStatus())
                .build();
    }
}
