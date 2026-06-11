package com.zcl.service;

import com.zcl.dao.OptionGroupDao;
import com.zcl.dao.ProductCategoryDao;
import com.zcl.dao.ProductDao;
import com.zcl.dao.ProductOptionRelationDao;
import com.zcl.dto.AdminProductVO;
import com.zcl.dto.PageResult;
import com.zcl.dto.ProductCreateRequest;
import com.zcl.entity.OptionGroup;
import com.zcl.entity.Product;
import com.zcl.entity.ProductCategory;
import com.zcl.entity.ProductOptionRelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminProductService {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ProductCategoryDao categoryDao;

    @Autowired
    private OptionGroupDao optionGroupDao;

    @Autowired
    private ProductOptionRelationDao productOptionRelationDao;

    /**
     * 获取所有启用的规格组（简单 Map 列表，避免 Hibernate 懒加载问题）
     */
    public List<Map<String, Object>> getAllEnabledOptionGroups() {
        List<OptionGroup> groups = optionGroupDao.findAll();
        return groups.stream()
                .filter(g -> g.getStatus() != null && g.getStatus() == 1)
                .map(g -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", g.getId());
                    map.put("groupName", g.getGroupName());
                    return map;
                })
                .collect(Collectors.toList());
    }

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
        product.setIsExchangeable(request.getIsExchangeable() != null ? request.getIsExchangeable() : 0);
        product.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        product.setSalesCount(0);
        productDao.save(product);

        // 保存规格组关联
        saveOptionGroupRelations(product, request.getOptionGroupIds());

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
        if (request.getIsExchangeable() != null) product.setIsExchangeable(request.getIsExchangeable());
        if (request.getStatus() != null) product.setStatus(request.getStatus());

        productDao.save(product);

        // 更新规格组关联（如果传了 optionGroupIds 则替换，否则不动）
        if (request.getOptionGroupIds() != null) {
            saveOptionGroupRelations(product, request.getOptionGroupIds());
        }

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
        // 查询该商品关联的规格组 ID 列表
        List<Long> optionGroupIds = new ArrayList<>();
        if (product.getHasOptions() != null && product.getHasOptions() == 1) {
            List<ProductOptionRelation> relations = productOptionRelationDao.findByProductId(product.getId());
            optionGroupIds = relations.stream()
                    .map(r -> r.getGroup().getId())
                    .collect(Collectors.toList());
        }

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
                .isExchangeable(product.getIsExchangeable())
                .status(product.getStatus())
                .optionGroupIds(optionGroupIds)
                .build();
    }

    /**
     * 保存商品与规格组的关联关系（先删后增）
     */
    private void saveOptionGroupRelations(Product product, List<Long> groupIds) {
        if (groupIds == null) groupIds = new ArrayList<>();

        // 删除旧关联
        List<ProductOptionRelation> oldRelations = productOptionRelationDao.findByProductId(product.getId());
        for (ProductOptionRelation old : oldRelations) {
            productOptionRelationDao.delete(old);
        }

        // 创建新关联
        for (int i = 0; i < groupIds.size(); i++) {
            OptionGroup group = optionGroupDao.findById(groupIds.get(i));
            if (group != null) {
                ProductOptionRelation relation = new ProductOptionRelation();
                relation.setProduct(product);
                relation.setGroup(group);
                relation.setSortOrder(i);
                relation.setIsVisible(1);
                productOptionRelationDao.save(relation);
            }
        }
    }
}
