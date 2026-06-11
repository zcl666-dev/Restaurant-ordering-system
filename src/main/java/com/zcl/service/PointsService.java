package com.zcl.service;

import com.zcl.dao.PointLogDao;
import com.zcl.dao.PointsMallDao;
import com.zcl.dao.UserDao;
import com.zcl.dao.UserExchangeVoucherDao;
import com.zcl.dto.PointLogVO;
import com.zcl.dto.PointsDetailVO;
import com.zcl.dto.PointsMallItemVO;
import com.zcl.dto.VoucherItemVO;
import com.zcl.entity.PointLog;
import com.zcl.entity.PointsMall;
import com.zcl.entity.User;
import com.zcl.entity.UserExchangeVoucher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class PointsService {

    @Autowired
    private PointLogDao pointLogDao;

    @Autowired
    private PointsMallDao pointsMallDao;

    @Autowired
    private UserExchangeVoucherDao userExchangeVoucherDao;

    @Autowired
    private UserDao userDao;

    public PointsDetailVO getPointsDetail(Long userId) {
        if (userId == null) {
            throw new RuntimeException("用户未登录");
        }

        User user = userDao.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        List<PointLog> logs = pointLogDao.findByUserId(userId);

        // 按月分组
        Map<String, List<PointLogVO>> grouped = new LinkedHashMap<>();
        for (PointLog log : logs) {
            String monthKey = log.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy年M月"));
            PointLogVO vo = PointLogVO.builder()
                    .id(log.getId())
                    .type(log.getType())
                    .pointsChange(Math.abs(log.getPointsChange()))
                    .balanceAfter(log.getBalanceAfter())
                    .remark(log.getRemark() != null ? log.getRemark() : "")
                    .orderNo(log.getOrder() != null ? log.getOrder().getOrderNo() : "")
                    .createdAt(log.getCreatedAt())
                    .build();
            grouped.computeIfAbsent(monthKey, k -> new ArrayList<>()).add(vo);
        }

        return PointsDetailVO.builder()
                .pointsBalance(user.getPointsBalance())
                .records(grouped)
                .build();
    }

    /**
     * 获取积分商城可兑换商品列表
     */
    public Map<String, Object> getMallList(Long userId) {
        User user = userDao.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        List<PointsMall> mallItems = pointsMallDao.findByStatus(1); // 上架状态

        List<PointsMallItemVO> items = mallItems.stream()
                .filter(item -> item.getExchangeQuantity() == null
                        || item.getExchangeQuantity() == 0
                        || item.getRedeemedCount() < item.getExchangeQuantity())
                .map(item -> {
                    int exchangeQuantity = item.getExchangeQuantity() != null ? item.getExchangeQuantity() : 0;
                    int redeemedCount = item.getRedeemedCount() != null ? item.getRedeemedCount() : 0;
                    return PointsMallItemVO.builder()
                            .id(item.getId())
                            .productId(item.getProduct().getId())
                            .productName(item.getProduct().getProductName())
                            .productImage(item.getProduct().getProductImage())
                            .pointsRequired(item.getPointsRequired())
                            .exchangeQuantity(exchangeQuantity)
                            .remainCount(exchangeQuantity > 0 ? exchangeQuantity - redeemedCount : -1)
                            .build();
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("pointsBalance", user.getPointsBalance());
        result.put("items", items);
        return result;
    }

    /**
     * 用户兑换积分商品
     */
    public UserExchangeVoucher exchange(Long userId, Long pointsMallId) {
        User user = userDao.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        PointsMall mall = pointsMallDao.findById(pointsMallId);
        if (mall == null || mall.getStatus() != 1) {
            throw new RuntimeException("兑换商品不存在或已下架");
        }

        int requiredPoints = mall.getPointsRequired();
        if (user.getPointsBalance() < requiredPoints) {
            throw new RuntimeException("积分不足");
        }

        // 检查库存
        int exchangeQuantity = mall.getExchangeQuantity() != null ? mall.getExchangeQuantity() : 0;
        int redeemedCount = mall.getRedeemedCount() != null ? mall.getRedeemedCount() : 0;
        if (exchangeQuantity > 0 && redeemedCount >= exchangeQuantity) {
            throw new RuntimeException("该商品已兑完");
        }

        // 扣积分
        user.setPointsBalance(user.getPointsBalance() - requiredPoints);
        userDao.save(user);

        // 递增已兑换数量
        mall.setRedeemedCount(redeemedCount + 1);
        pointsMallDao.save(mall);

        // 创建积分流水记录
        PointLog pointLog = new PointLog();
        pointLog.setUser(user);
        pointLog.setType(2); // 扣除
        pointLog.setPointsChange(requiredPoints);
        pointLog.setBalanceAfter(user.getPointsBalance());
        pointLog.setRemark("积分兑换「" + mall.getProduct().getProductName() + "」");
        pointLogDao.save(pointLog);

        // 创建兑换券
        UserExchangeVoucher voucher = new UserExchangeVoucher();
        voucher.setUser(user);
        voucher.setPointsMall(mall);
        voucher.setVoucherCode(generateVoucherCode());
        voucher.setRequiredPoints(requiredPoints);
        voucher.setStatus(0); // 未使用
        voucher.setExpireTime(LocalDateTime.now().plusDays(mall.getExpireDays()));
        userExchangeVoucherDao.save(voucher);

        return voucher;
    }

    /**
     * 获取用户兑换券列表（按状态分组）
     */
    public Map<String, Object> getVoucherList(Long userId) {
        User user = userDao.findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 先检查并过期已超时的券
        List<UserExchangeVoucher> allVouchers = userExchangeVoucherDao.findByUserId(userId);
        LocalDateTime now = LocalDateTime.now();
        for (UserExchangeVoucher v : allVouchers) {
            if (v.getStatus() == 0 && v.getExpireTime().isBefore(now)) {
                v.setStatus(2); // 标记为已过期
                userExchangeVoucherDao.save(v);
            }
        }

        // 按状态分组统计
        long unusedCount = 0;
        long usedCount = 0;
        long expiredCount = 0;
        List<VoucherItemVO> unusedList = new ArrayList<>();
        List<VoucherItemVO> usedList = new ArrayList<>();
        List<VoucherItemVO> expiredList = new ArrayList<>();

        for (UserExchangeVoucher v : allVouchers) {
            VoucherItemVO vo = VoucherItemVO.builder()
                    .id(v.getId())
                    .productId(v.getPointsMall().getProduct().getId())
                    .productName(v.getPointsMall().getProduct().getProductName())
                    .productImage(v.getPointsMall().getProduct().getProductImage())
                    .requiredPoints(v.getRequiredPoints())
                    .status(v.getStatus())
                    .expireTime(v.getExpireTime())
                    .usedAt(v.getUsedAt())
                    .createdAt(v.getCreatedAt())
                    .build();

            switch (v.getStatus()) {
                case 0:
                    unusedCount++;
                    unusedList.add(vo);
                    break;
                case 1:
                    usedCount++;
                    usedList.add(vo);
                    break;
                case 2:
                    expiredCount++;
                    expiredList.add(vo);
                    break;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("unusedCount", unusedCount);
        result.put("usedCount", usedCount);
        result.put("expiredCount", expiredCount);
        result.put("unusedList", unusedList);
        result.put("usedList", usedList);
        result.put("expiredList", expiredList);
        return result;
    }

    private String generateVoucherCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
