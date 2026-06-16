package com.zcl.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opensymphony.xwork2.ActionSupport;
import com.zcl.dto.BatchGenerateResult;
import com.zcl.dto.DiningTableCreateRequest;
import com.zcl.dto.DiningTableDTO;
import com.zcl.dto.PageResult;
import com.zcl.dto.Result;
import com.zcl.service.DiningTableService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

/**
 * 桌台管理Action
 */
@Component
@Scope("prototype")
public class DiningTableAction extends ActionSupport {

    @Autowired
    private DiningTableService diningTableService;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private Long id;
    private int page = 0;
    private int size = 10;
    private String tableNo;
    private String tableName;
    private Integer status;

    private void writeJson(Result<?> result) {
        try {
            HttpServletResponse response = ServletActionContext.getResponse();
            response.setContentType("application/json;charset=UTF-8");
            objectMapper.writeValue(response.getOutputStream(), result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 分页查询桌台列表
     */
    public String list() {
        try {
            System.out.println("[DEBUG] DiningTableAction.list() called, page=" + page + ", size=" + size);
            PageResult<DiningTableDTO> pageResult = diningTableService.getTableList(page, size, tableNo, tableName, status);
            System.out.println("[DEBUG] getTableList returned " + pageResult.getContent().size() + " items");
            writeJson(Result.success("获取成功", pageResult));
        } catch (Exception e) {
            System.err.println("[ERROR] DiningTableAction.list() failed: " + e.getMessage());
            e.printStackTrace();
            writeJson(Result.error(500, "获取桌台列表失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 获取桌台详情
     */
    public String detail() {
        try {
            DiningTableDTO dto = diningTableService.getTableDetail(id);
            writeJson(Result.success("获取成功", dto));
        } catch (Exception e) {
            writeJson(Result.error(404, e.getMessage()));
        }
        return NONE;
    }

    /**
     * 新增桌台
     */
    public String add() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            DiningTableCreateRequest req = objectMapper.readValue(request.getInputStream(), DiningTableCreateRequest.class);
            diningTableService.createTable(req);
            writeJson(Result.success("创建成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "创建桌台失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 更新桌台
     */
    public String update() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            DiningTableCreateRequest req = objectMapper.readValue(request.getInputStream(), DiningTableCreateRequest.class);
            diningTableService.updateTable(id, req);
            writeJson(Result.success("更新成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "更新桌台失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 删除桌台
     */
    public String delete() {
        try {
            diningTableService.deleteTable(id);
            writeJson(Result.success("删除成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "删除桌台失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 生成单个桌台二维码
     * 参数：id（桌台ID）
     * 返回：base64 data URL
     */
    public String generateQr() {
        try {
            String dataUrl = diningTableService.generateQrCode(id);
            writeJson(Result.success("生成成功", dataUrl));
        } catch (Exception e) {
            writeJson(Result.error(500, "生成二维码失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 批量生成二维码（所有未生成的桌台）
     * 返回：成功/失败数量
     */
    public String generateAllQr() {
        try {
            BatchGenerateResult result = diningTableService.batchGenerateQrCode();
            writeJson(Result.success("批量生成完成", result));
        } catch (Exception e) {
            writeJson(Result.error(500, "批量生成二维码失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 下载二维码图片
     * 参数：id（桌台ID）
     */
    public String downloadQr() {
        try {
            DiningTableDTO dto = diningTableService.getTableDetail(id);
            if (dto.getQrCodeUrl() == null || dto.getQrCodeUrl().isEmpty()) {
                writeJson(Result.error(404, "二维码不存在，请先生成"));
                return NONE;
            }

            byte[] imageData = diningTableService.getQrCodeImage(id);

            HttpServletResponse response = ServletActionContext.getResponse();
            response.setContentType("image/png");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + dto.getTableNo() + ".png");
            response.setContentLength(imageData.length);

            try (OutputStream out = response.getOutputStream()) {
                out.write(imageData);
                out.flush();
            }
            // 阻止 Struts2 后续处理
            return null;
        } catch (Exception e) {
            try {
                writeJson(Result.error(500, "下载二维码失败: " + e.getMessage()));
            } catch (Exception ex) {
                e.printStackTrace();
            }
            return null;
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public String getTableNo() { return tableNo; }
    public void setTableNo(String tableNo) { this.tableNo = tableNo; }
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
