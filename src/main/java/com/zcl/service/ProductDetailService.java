package com.zcl.service;

import com.zcl.dto.OptionGroupVO;
import com.zcl.dto.OptionValueVO;
import com.zcl.dto.ProductDetailVO;
import com.zcl.entity.OptionValue;
import com.zcl.entity.Product;
import com.zcl.entity.ProductOptionRelation;
import com.zcl.repository.OptionValueRepository;
import com.zcl.repository.ProductOptionRelationRepository;
import com.zcl.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品详情服务类
 */
@Service
public class ProductDetailService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductOptionRelationRepository productOptionRelationRepository;

    @Autowired
    private OptionValueRepository optionValueRepository;

    /**
     * 根据商品ID获取商品详情（包含规格信息）
     *
     * @param productId 商品ID
     * @return 商品详情对象，如果商品不存在则返回 null
     */
    public ProductDetailVO getProductDetail(Long productId) {
        // 1. 查询商品信息
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return null;
        }

        // 2. 构建商品详情 VO
        ProductDetailVO detailVO = new ProductDetailVO();
        detailVO.setId(product.getId());
        detailVO.setProductName(product.getProductName());
        detailVO.setProductImage(product.getProductImage());
        detailVO.setDescription(product.getDescription());
        detailVO.setPrice(product.getPrice());
        detailVO.setStock(product.getStock());
        detailVO.setSalesCount(product.getSalesCount());
        detailVO.setStatus(product.getStatus());
        detailVO.setProductType(product.getProductType());

        // 3. 查询商品关联的规格组（只查询可见的）
        List<ProductOptionRelation> relations = productOptionRelationRepository
                .findByProduct_IdAndIsVisibleOrderBySortOrderAsc(productId, 1);

        // 4. 组装规格组和规格值
        List<OptionGroupVO> optionGroupVOList = new ArrayList<>();
        for (ProductOptionRelation relation : relations) {
            OptionGroupVO groupVO = new OptionGroupVO();
            groupVO.setGroupId(relation.getGroup().getId());
            groupVO.setGroupName(relation.getGroup().getGroupName());

            // 5. 查询该规格组下的所有启用的规格值
            List<OptionValue> optionValues = optionValueRepository
                    .findByGroup_IdAndStatusOrderBySortOrderAsc(relation.getGroup().getId(), 1);

            // 6. 转换为 OptionValueVO
            List<OptionValueVO> optionValueVOList = optionValues.stream()
                    .map(this::convertToOptionValueVO)
                    .collect(Collectors.toList());

            groupVO.setOptions(optionValueVOList);
            optionGroupVOList.add(groupVO);
        }

        detailVO.setOptionGroups(optionGroupVOList);

        return detailVO;
    }

    /**
     * 将 OptionValue 实体转换为 OptionValueVO
     */
    private OptionValueVO convertToOptionValueVO(OptionValue optionValue) {
        OptionValueVO vo = new OptionValueVO();
        vo.setId(optionValue.getId());
        vo.setValueName(optionValue.getValueName());
        vo.setIsDefault(optionValue.getIsDefault() == 1);
        return vo;
    }
}
