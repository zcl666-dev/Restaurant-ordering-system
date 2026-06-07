package com.zcl.service;

import com.zcl.dao.OptionValueDao;
import com.zcl.dao.ProductDao;
import com.zcl.dao.ProductOptionRelationDao;
import com.zcl.dto.OptionGroupVO;
import com.zcl.dto.OptionValueVO;
import com.zcl.dto.ProductDetailVO;
import com.zcl.entity.OptionValue;
import com.zcl.entity.Product;
import com.zcl.entity.ProductOptionRelation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品详情服务类
 */
@Service
@Transactional(readOnly = true)
public class ProductDetailService {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ProductOptionRelationDao productOptionRelationDao;

    @Autowired
    private OptionValueDao optionValueDao;

    /**
     * 根据商品ID获取商品详情（包含规格信息）
     */
    public ProductDetailVO getProductDetail(Long productId) {
        // 1. 查询商品信息
        Product product = productDao.findById(productId);
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

        // 3. 查询商品关联的规格组
        List<ProductOptionRelation> relations = productOptionRelationDao.findByProductId(productId);

        // 4. 组装规格组和规格值
        List<OptionGroupVO> optionGroupVOList = new ArrayList<>();
        for (ProductOptionRelation relation : relations) {
            if (relation.getIsVisible() != null && relation.getIsVisible() == 1) {
                OptionGroupVO groupVO = new OptionGroupVO();
                groupVO.setGroupId(relation.getGroup().getId());
                groupVO.setGroupName(relation.getGroup().getGroupName());

                // 5. 查询该规格组下的所有启用的规格值
                List<OptionValue> optionValues = optionValueDao.findByGroupId(relation.getGroup().getId());
                optionValues = optionValues.stream()
                        .filter(ov -> ov.getStatus() != null && ov.getStatus() == 1)
                        .collect(Collectors.toList());

                // 6. 转换为 OptionValueVO
                List<OptionValueVO> optionValueVOList = optionValues.stream()
                        .map(this::convertToOptionValueVO)
                        .collect(Collectors.toList());

                groupVO.setOptions(optionValueVOList);
                optionGroupVOList.add(groupVO);
            }
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
        vo.setIsDefault(optionValue.getIsDefault() != null && optionValue.getIsDefault() == 1);
        return vo;
    }
}
