package com.zcl.service;

import com.zcl.dto.CategoryWithProductsVO;
import com.zcl.dto.ProductVO;
import com.zcl.entity.Product;
import com.zcl.entity.ProductCategory;
import com.zcl.repository.ProductCategoryRepository;
import com.zcl.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品展示服务类
 */
@Service
public class ProductDisplayService {

    @Autowired
    private ProductCategoryRepository productCategoryRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * 获取商品展示数据（按分类分组，每个分类下包含商品列表）
     * 只返回状态为启用/上架的数据
     *
     * @return 分类及商品列表（嵌套结构）
     */
    public List<CategoryWithProductsVO> getProductDisplayData() {
        // 1. 查询所有启用的商品分类（status = 1），按排序字段升序
        List<ProductCategory> categories = productCategoryRepository.findByStatusOrderBySortOrderAsc(1);

        // 2. 查询所有上架的商品（status = 1）
        List<Product> products = productRepository.findByStatus(1);

        // 3. 将商品按分类ID分组
        Map<Long, List<Product>> productsByCategory = products.stream()
                .collect(Collectors.groupingBy(product -> product.getCategory().getId()));

        // 4. 构建返回结果：每个分类下包含该分类的商品列表
        List<CategoryWithProductsVO> result = new ArrayList<>();
        
        for (ProductCategory category : categories) {
            CategoryWithProductsVO categoryVO = new CategoryWithProductsVO();
            categoryVO.setId(category.getId());
            categoryVO.setCategoryName(category.getCategoryName());
            categoryVO.setIcon(category.getIcon());
            
            // 获取该分类下的商品列表
            List<Product> categoryProducts = productsByCategory.getOrDefault(category.getId(), new ArrayList<>());
            
            // 转换为 ProductVO
            List<ProductVO> productVOList = categoryProducts.stream()
                    .map(this::convertToProductVO)
                    .collect(Collectors.toList());
            
            categoryVO.setProducts(productVOList);
            result.add(categoryVO);
        }

        return result;
    }

    /**
     * 将 Product 实体转换为 ProductVO
     */
    private ProductVO convertToProductVO(Product product) {
        ProductVO vo = new ProductVO();
        vo.setId(product.getId());
        vo.setProductName(product.getProductName());
        vo.setProductImage(product.getProductImage());
        vo.setDesc(product.getDescription());
        vo.setPrice(product.getPrice());
        vo.setSalesCount(product.getSalesCount() != null ? product.getSalesCount() : 0);
        return vo;
    }
}
