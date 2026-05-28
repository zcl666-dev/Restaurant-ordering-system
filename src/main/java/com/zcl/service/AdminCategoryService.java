package com.zcl.service;

import com.zcl.dto.CategoryCreateRequest;
import com.zcl.entity.ProductCategory;
import com.zcl.repository.ProductCategoryRepository;
import com.zcl.repository.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminCategoryService {

    private final ProductCategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public AdminCategoryService(ProductCategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<ProductCategory> getCategoryList() {
        return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "sortOrder"));
    }

    public ProductCategory createCategory(CategoryCreateRequest request) {
        ProductCategory category = new ProductCategory();
        category.setCategoryName(request.getCategoryName());
        category.setIcon(request.getIcon());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        return categoryRepository.save(category);
    }

    public ProductCategory updateCategory(Long id, CategoryCreateRequest request) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        if (request.getCategoryName() != null) category.setCategoryName(request.getCategoryName());
        if (request.getIcon() != null) category.setIcon(request.getIcon());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());
        if (request.getStatus() != null) category.setStatus(request.getStatus());
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在"));
        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new RuntimeException("该分类下还有 " + productCount + " 个商品，无法删除");
        }
        categoryRepository.delete(category);
    }
}
