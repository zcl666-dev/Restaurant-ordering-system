package com.zcl.service;

import com.zcl.dao.PointLogDao;
import com.zcl.dao.PointsMallDao;
import com.zcl.dao.ProductDao;
import com.zcl.dto.AdminPointLogVO;
import com.zcl.dto.AdminPointsMallVO;
import com.zcl.dto.PageResult;
import com.zcl.dto.PointsMallRequest;
import com.zcl.entity.PointLog;
import com.zcl.entity.PointsMall;
import com.zcl.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminPointsService {

    @Autowired
    private PointLogDao pointLogDao;

    @Autowired
    private PointsMallDao pointsMallDao;

    @Autowired
    private ProductDao productDao;

    /**
     * 分页查询积分流水
     */
    public PageResult<AdminPointLogVO> getPointLogList(String keyword, int page, int size) {
        int offset = page * size;
        List<PointLog> logs;
        long total;

        if (keyword != null && !keyword.trim().isEmpty()) {
            logs = pointLogDao.findWithPaging(keyword.trim(), offset, size);
            total = pointLogDao.countByKeyword(keyword.trim());
        } else {
            logs = pointLogDao.findAllWithPaging(offset, size);
            total = pointLogDao.count();
        }

        List<AdminPointLogVO> content = logs.stream()
                .map(this::toPointLogVO)
                .collect(Collectors.toList());

        return PageResult.<AdminPointLogVO>builder()
                .content(content)
                .totalElements(total)
                .totalPages((int) Math.ceil((double) total / size))
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    /**
     * 分页查询积分商城商品
     */
    public PageResult<AdminPointsMallVO> getPointsMallList(int page, int size) {
        int offset = page * size;
        List<PointsMall> items = pointsMallDao.findAllWithPaging(offset, size);
        long total = pointsMallDao.count();

        List<AdminPointsMallVO> content = items.stream()
                .map(this::toMallVO)
                .collect(Collectors.toList());

        return PageResult.<AdminPointsMallVO>builder()
                .content(content)
                .totalElements(total)
                .totalPages((int) Math.ceil((double) total / size))
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    /**
     * 创建积分商城商品
     */
    public void createPointsMallItem(PointsMallRequest request) {
        Product product = productDao.findById(request.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        // 检查是否已在积分商城中
        PointsMall existing = pointsMallDao.findByProductId(request.getProductId());
        if (existing != null) {
            throw new RuntimeException("该商品已在积分商城中");
        }

        PointsMall mall = new PointsMall();
        mall.setProduct(product);
        mall.setPointsRequired(request.getPointsRequired());
        mall.setExchangeQuantity(request.getExchangeQuantity() != null ? request.getExchangeQuantity() : 0);
        mall.setExpireDays(request.getExpireDays() != null ? request.getExpireDays() : 7);
        mall.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        pointsMallDao.save(mall);

        // 设置商品为可兑换
        product.setIsExchangeable(1);
        productDao.save(product);
    }

    /**
     * 更新积分商城商品
     */
    public void updatePointsMallItem(PointsMallRequest request) {
        PointsMall mall = pointsMallDao.findById(request.getId());
        if (mall == null) {
            throw new RuntimeException("积分商城商品不存在");
        }

        if (request.getProductId() != null && !request.getProductId().equals(mall.getProduct().getId())) {
            Product newProduct = productDao.findById(request.getProductId());
            if (newProduct == null) {
                throw new RuntimeException("商品不存在");
            }
            // 旧商品取消可兑换标记
            mall.getProduct().setIsExchangeable(0);
            productDao.save(mall.getProduct());

            mall.setProduct(newProduct);
            newProduct.setIsExchangeable(1);
            productDao.save(newProduct);
        }

        if (request.getPointsRequired() != null) mall.setPointsRequired(request.getPointsRequired());
        if (request.getExchangeQuantity() != null) mall.setExchangeQuantity(request.getExchangeQuantity());
        if (request.getExpireDays() != null) mall.setExpireDays(request.getExpireDays());
        if (request.getStatus() != null) mall.setStatus(request.getStatus());

        pointsMallDao.save(mall);
    }

    /**
     * 删除积分商城商品
     */
    public void deletePointsMallItem(Long id) {
        PointsMall mall = pointsMallDao.findById(id);
        if (mall == null) {
            throw new RuntimeException("积分商城商品不存在");
        }

        // 取消商品的可兑换标记
        Product product = mall.getProduct();
        product.setIsExchangeable(0);
        productDao.save(product);

        pointsMallDao.delete(mall);
    }

    private AdminPointLogVO toPointLogVO(PointLog log) {
        return AdminPointLogVO.builder()
                .id(log.getId())
                .userId(log.getUser().getId())
                .userNickname(log.getUser().getNickName())
                .orderId(log.getOrder() != null ? log.getOrder().getId() : null)
                .orderNo(log.getOrder() != null ? log.getOrder().getOrderNo() : null)
                .type(log.getType())
                .pointsChange(log.getPointsChange())
                .balanceAfter(log.getBalanceAfter())
                .remark(log.getRemark())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private AdminPointsMallVO toMallVO(PointsMall mall) {
        int exchangeQuantity = mall.getExchangeQuantity() != null ? mall.getExchangeQuantity() : 0;
        int redeemedCount = mall.getRedeemedCount() != null ? mall.getRedeemedCount() : 0;
        int remainCount = exchangeQuantity > 0 ? exchangeQuantity - redeemedCount : -1; // -1 表示不限量

        return AdminPointsMallVO.builder()
                .id(mall.getId())
                .productId(mall.getProduct().getId())
                .productName(mall.getProduct().getProductName())
                .productImage(mall.getProduct().getProductImage())
                .pointsRequired(mall.getPointsRequired())
                .exchangeQuantity(exchangeQuantity)
                .redeemedCount(redeemedCount)
                .remainCount(remainCount)
                .expireDays(mall.getExpireDays())
                .status(mall.getStatus())
                .createdAt(mall.getCreatedAt())
                .build();
    }
}
