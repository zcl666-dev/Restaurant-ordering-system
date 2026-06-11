package com.zcl.service;

import com.zcl.dao.DiningTableDao;
import com.zcl.dto.BatchGenerateResult;
import com.zcl.dto.DiningTableCreateRequest;
import com.zcl.dto.DiningTableDTO;
import com.zcl.dto.PageResult;
import com.zcl.entity.DiningTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 桌台管理服务
 */
@Service
@Transactional
public class DiningTableService {

    private static final Logger log = LoggerFactory.getLogger(DiningTableService.class);

    @Autowired
    private DiningTableDao diningTableDao;

    @Autowired
    private WechatService wechatService;

    /** 小程序码宽度 */
    private static final int QRCODE_WIDTH = 300;
    /** 小程序页面路径 */
    private static final String MINI_PROGRAM_PAGE = "pages/index/index";

    /**
     * 分页查询桌台
     */
    public PageResult<DiningTableDTO> getTableList(int page, int size, String tableNo, String tableName, Integer status) {
        List<DiningTable> tables = diningTableDao.findByPage(page, size, tableNo, tableName, status);
        long total = diningTableDao.countByCondition(tableNo, tableName, status);

        List<DiningTableDTO> dtoList = tables.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        PageResult<DiningTableDTO> result = new PageResult<>();
        result.setContent(dtoList);
        result.setTotalElements(total);
        result.setTotalPages((int) Math.ceil((double) total / size));
        result.setCurrentPage(page);
        result.setPageSize(size);
        return result;
    }

    /**
     * 获取桌台详情
     */
    public DiningTableDTO getTableDetail(Long id) {
        DiningTable table = diningTableDao.findById(id);
        if (table == null) {
            throw new RuntimeException("桌台不存在");
        }
        return toDTO(table);
    }

    /**
     * 新增桌台
     */
    public void createTable(DiningTableCreateRequest request) {
        DiningTable existing = diningTableDao.findByTableNo(request.getTableNo());
        if (existing != null) {
            throw new RuntimeException("桌号已存在");
        }

        DiningTable table = new DiningTable();
        table.setTableNo(request.getTableNo());
        table.setTableName(request.getTableName());
        table.setSeatCount(request.getSeatCount() != null ? request.getSeatCount() : 4);
        table.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        diningTableDao.save(table);

        log.info("新增桌台: tableNo={}", request.getTableNo());
    }

    /**
     * 更新桌台
     */
    public void updateTable(Long id, DiningTableCreateRequest request) {
        DiningTable table = diningTableDao.findById(id);
        if (table == null) {
            throw new RuntimeException("桌台不存在");
        }

        if (request.getTableNo() != null && !request.getTableNo().equals(table.getTableNo())) {
            DiningTable existing = diningTableDao.findByTableNo(request.getTableNo());
            if (existing != null) {
                throw new RuntimeException("桌号已存在");
            }
            table.setTableNo(request.getTableNo());
        }

        if (request.getTableName() != null) table.setTableName(request.getTableName());
        if (request.getSeatCount() != null) table.setSeatCount(request.getSeatCount());
        if (request.getStatus() != null) table.setStatus(request.getStatus());

        diningTableDao.save(table);
        log.info("更新桌台: id={}, tableNo={}", id, table.getTableNo());
    }

    /**
     * 删除桌台
     */
    public void deleteTable(Long id) {
        DiningTable table = diningTableDao.findById(id);
        if (table == null) {
            throw new RuntimeException("桌台不存在");
        }
        diningTableDao.deleteById(id);
        log.info("删除桌台: id={}, tableNo={}", id, table.getTableNo());
    }

    /**
     * 生成单个桌台的二维码（优先微信小程序码，失败自动降级ZXing）
     * @param id 桌台ID
     * @return base64 data URL
     */
    public String generateQrCode(Long id) {
        DiningTable table = diningTableDao.findById(id);
        if (table == null) {
            throw new RuntimeException("桌台不存在");
        }

        // 调用微信API生成小程序码
        String scene = "tableNo=" + java.net.URLEncoder.encode(table.getTableNo(), java.nio.charset.StandardCharsets.UTF_8);
        byte[] imageData = wechatService.generateQrCode(scene, MINI_PROGRAM_PAGE, QRCODE_WIDTH);

        // 转为 base64 data URL 存入数据库
        String dataUrl = wechatService.toDataUrl(imageData);

        table.setQrCodeUrl(dataUrl);
        diningTableDao.save(table);

        log.info("生成二维码成功: tableNo={}", table.getTableNo());
        return dataUrl;
    }

    /**
     * 批量生成二维码（所有未生成二维码的桌台）
     */
    public BatchGenerateResult batchGenerateQrCode() {
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failCount = 0;

        List<DiningTable> tables = diningTableDao.findWithoutQrCode();
        for (DiningTable table : tables) {
            try {
                generateQrCode(table.getId());
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("批量生成二维码失败: tableNo={}, error={}", table.getTableNo(), e.getMessage());
            }
        }

        BatchGenerateResult result = new BatchGenerateResult();
        result.setSuccessCount(successCount);
        result.setFailCount(failCount);
        result.setTimeCost(System.currentTimeMillis() - startTime);

        log.info("批量生成二维码完成: 成功={}, 失败={}, 耗时={}ms", successCount, failCount, result.getTimeCost());
        return result;
    }

    /**
     * 获取二维码图片字节数组（用于下载）
     */
    public byte[] getQrCodeImage(Long id) {
        DiningTableDTO dto = getTableDetail(id);
        if (dto.getQrCodeUrl() == null || dto.getQrCodeUrl().isEmpty()) {
            throw new RuntimeException("二维码不存在，请先生成");
        }
        return wechatService.fromDataUrl(dto.getQrCodeUrl());
    }

    /**
     * 根据桌号查找桌台
     */
    public DiningTable findByTableNo(String tableNo) {
        return diningTableDao.findByTableNo(tableNo);
    }

    /**
     * Entity -> DTO
     */
    private DiningTableDTO toDTO(DiningTable table) {
        DiningTableDTO dto = new DiningTableDTO();
        dto.setId(table.getId());
        dto.setTableNo(table.getTableNo());
        dto.setTableName(table.getTableName());
        dto.setSeatCount(table.getSeatCount());
        dto.setStatus(table.getStatus());
        dto.setQrCodeUrl(table.getQrCodeUrl());
        dto.setCreateTime(table.getCreateTime());
        return dto;
    }
}
