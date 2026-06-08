package com.zcl.service;

import com.zcl.dao.ProductCategoryDao;
import com.zcl.dao.ProductDao;
import com.zcl.dto.AdminProductVO;
import com.zcl.dto.PageResult;
import com.zcl.dto.ProductCreateRequest;
import com.zcl.entity.Product;
import com.zcl.entity.ProductCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminProductService {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ProductCategoryDao categoryDao;

    public PageResult<AdminProductVO> getProductList(int page, int size, Integer status, Long categoryId, String keyword) {
        int offset = page * size;
        List<Product> products;
        long totalElements;

        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productDao.findByProductNameContaining(keyword.trim(), offset, size);
            totalElements = productDao.countByProductNameContaining(keyword.trim());
        } else if (categoryId != null && status != null) {
            products = productDao.findByCategoryIdAndStatus(categoryId, status, offset, size);
            totalElements = productDao.countByCategoryIdAndStatus(categoryId, status);
        } else if (categoryId != null) {
            products = productDao.findByCategoryId(categoryId, offset, size);
            totalElements = productDao.countByCategoryId(categoryId);
        } else if (status != null) {
            products = productDao.findByStatus(status, offset, size);
            totalElements = productDao.countByStatus(status);
        } else {
            products = productDao.findAllWithPaging(offset, size);
            totalElements = productDao.count();
        }

        List<AdminProductVO> content = products.stream().map(this::toVO).collect(Collectors.toList());

        return PageResult.<AdminProductVO>builder()
                .content(content)
                .totalElements(totalElements)
                .totalPages((int) Math.ceil((double) totalElements / size))
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    public AdminProductVO getProductDetail(Long id) {
        Product product = productDao.findById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        return toVO(product);
    }

    public Product createProduct(ProductCreateRequest request) {
        ProductCategory category = categoryDao.findById(request.getCategoryId());
        if (category == null) {
            throw new RuntimeException("分类不存在");
        }

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
        productDao.save(product);
        return product;
    }

    public Product updateProduct(Long id, ProductCreateRequest request) {
        Product product = productDao.findById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        if (request.getCategoryId() != null) {
            ProductCategory category = categoryDao.findById(request.getCategoryId());
            if (category == null) {
                throw new RuntimeException("分类不存在");
            }
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

        productDao.save(product);
        return product;
    }

    public void deleteProduct(Long id) {
        Product product = productDao.findById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        product.setStatus(0);
        productDao.save(product);
    }

    public void toggleProductStatus(Long id, Integer status) {
        Product product = productDao.findById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        product.setStatus(status);
        productDao.save(product);
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
