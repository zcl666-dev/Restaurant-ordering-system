package com.zcl.service;

import com.zcl.dao.ProductCategoryDao;
import com.zcl.dao.ProductDao;
import com.zcl.dto.CategoryCreateRequest;
import com.zcl.entity.ProductCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AdminCategoryService {

    @Autowired
    private ProductCategoryDao categoryDao;

    @Autowired
    private ProductDao productDao;

    public List<ProductCategory> getCategoryList() {
        return categoryDao.findAllOrderBySortOrder();
    }

    public ProductCategory createCategory(CategoryCreateRequest request) {
        ProductCategory category = new ProductCategory();
        category.setCategoryName(request.getCategoryName());
        category.setIcon(request.getIcon());
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        categoryDao.save(category);
        return category;
    }

    public ProductCategory updateCategory(Long id, CategoryCreateRequest request) {
        ProductCategory category = categoryDao.findById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        if (request.getCategoryName() != null) category.setCategoryName(request.getCategoryName());
        if (request.getIcon() != null) category.setIcon(request.getIcon());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());
        if (request.getStatus() != null) category.setStatus(request.getStatus());
        categoryDao.save(category);
        return category;
    }

    public void deleteCategory(Long id) {
        ProductCategory category = categoryDao.findById(id);
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }
        long productCount = productDao.countByCategoryId(id);
        if (productCount > 0) {
            throw new RuntimeException("该分类下还有 " + productCount + " 个商品，无法删除");
        }
        categoryDao.delete(category);
    }
}
