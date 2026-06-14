package com.zcl.service;

import com.zcl.dao.OrderDao;
import com.zcl.entity.Orders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单定时任务
 * 每分钟检查超时的待制作订单，自动转为制作中
 */
@Service
public class OrderScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderScheduler.class);

    @Autowired
    private OrderDao orderDao;

    /**
     * 每分钟执行一次：将超过 cancelDeadline 的待制作订单自动转为制作中
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void autoStartProduction() {
        LocalDateTime now = LocalDateTime.now();
        List<Orders> expiredOrders = orderDao.findExpiredPendingOrders(now);

        if (expiredOrders.isEmpty()) {
            return;
        }

        for (Orders order : expiredOrders) {
            order.setOrderStatus(2); // 制作中
            order.setAutoProductionTime(now);
            order.setCancelDeadline(null);
            orderDao.save(order);
            log.info("订单自动转为制作中: orderNo={}", order.getOrderNo());
        }

        log.info("定时任务：自动处理 {} 个超时待制作订单", expiredOrders.size());
    }
}
