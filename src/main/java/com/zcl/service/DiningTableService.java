package com.zcl.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.zcl.dto.BatchGenerateResult;
import com.zcl.dto.DiningTableCreateRequest;
import com.zcl.dto.DiningTableDTO;
import com.zcl.dto.PageResult;
import com.zcl.entity.DiningTable;
import com.zcl.dao.DiningTableDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class DiningTableService {

    @Autowired
    private DiningTableDao diningTableDao;

    @Value("${upload.path:upload}")
    private String uploadPath;

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
        // 检查桌号唯一性
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
    }

    /**
     * 更新桌台
     */
    public void updateTable(Long id, DiningTableCreateRequest request) {
        DiningTable table = diningTableDao.findById(id);
        if (table == null) {
            throw new RuntimeException("桌台不存在");
        }

        // 如果修改了桌号，检查唯一性
        if (request.getTableNo() != null && !request.getTableNo().equals(table.getTableNo())) {
            DiningTable existing = diningTableDao.findByTableNo(request.getTableNo());
            if (existing != null) {
                throw new RuntimeException("桌号已存在");
            }
            table.setTableNo(request.getTableNo());
        }

        if (request.getTableName() != null) {
            table.setTableName(request.getTableName());
        }
        if (request.getSeatCount() != null) {
            table.setSeatCount(request.getSeatCount());
        }
        if (request.getStatus() != null) {
            table.setStatus(request.getStatus());
        }

        diningTableDao.save(table);
    }

    /**
     * 删除桌台
     */
    public void deleteTable(Long id) {
        DiningTable table = diningTableDao.findById(id);
        if (table == null) {
            throw new RuntimeException("桌台不存在");
        }
        // 删除二维码文件
        if (table.getQrCodeUrl() != null && !table.getQrCodeUrl().isEmpty()) {
            try {
                Path filePath = Paths.get(uploadPath, table.getQrCodeUrl());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // 忽略删除文件失败
            }
        }
        diningTableDao.deleteById(id);
    }

    /**
     * 生成二维码
     */
    public String generateQrCode(Long id, HttpServletRequest request) {
        DiningTable table = diningTableDao.findById(id);
        if (table == null) {
            throw new RuntimeException("桌台不存在");
        }

        try {
            // 二维码内容：小程序页面路径+桌号
            String content = "pages/index/index?tableNo=" + table.getTableNo();

            // 生成二维码
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 300, 300, hints);

            // 创建上传目录
            String qrDir = uploadPath + "/qrcode";
            Files.createDirectories(Paths.get(qrDir));

            // 保存文件
            String fileName = table.getTableNo() + ".png";
            String filePath = qrDir + "/" + fileName;
            Path path = Paths.get(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

            // 更新数据库
            String qrUrl = "/qrcode/" + fileName;
            table.setQrCodeUrl(qrUrl);
            diningTableDao.save(table);

            return qrUrl;
        } catch (WriterException | IOException e) {
            throw new RuntimeException("生成二维码失败: " + e.getMessage());
        }
    }

    /**
     * 批量生成二维码
     */
    public BatchGenerateResult batchGenerateQrCode(HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failCount = 0;

        List<DiningTable> tables = diningTableDao.findWithoutQrCode();
        for (DiningTable table : tables) {
            try {
                generateQrCode(table.getId(), request);
                successCount++;
            } catch (Exception e) {
                failCount++;
            }
        }

        BatchGenerateResult result = new BatchGenerateResult();
        result.setSuccessCount(successCount);
        result.setFailCount(failCount);
        result.setTimeCost(System.currentTimeMillis() - startTime);
        return result;
    }

    /**
     * 根据桌号查找桌台
     */
    public DiningTable findByTableNo(String tableNo) {
        return diningTableDao.findByTableNo(tableNo);
    }

    /**
     * 转换为DTO
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
