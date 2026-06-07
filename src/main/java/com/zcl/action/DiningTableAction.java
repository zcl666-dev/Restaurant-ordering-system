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
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

@Controller
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

    // 用于接收JSON请求体
    private DiningTableCreateRequest tableRequest;

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
            PageResult<DiningTableDTO> pageResult = diningTableService.getTableList(page, size, tableNo, tableName, status);
            writeJson(Result.success("获取成功", pageResult));
        } catch (Exception e) {
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
     * 生成二维码
     */
    public String generateQr() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            String qrUrl = diningTableService.generateQrCode(id, request);
            writeJson(Result.success("生成成功", qrUrl));
        } catch (Exception e) {
            writeJson(Result.error(500, "生成二维码失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 批量生成二维码
     */
    public String generateAllQr() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            BatchGenerateResult result = diningTableService.batchGenerateQrCode(request);
            writeJson(Result.success("批量生成完成", result));
        } catch (Exception e) {
            writeJson(Result.error(500, "批量生成二维码失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 下载二维码
     */
    public String downloadQr() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            HttpServletResponse response = ServletActionContext.getResponse();

            DiningTableDTO dto = diningTableService.getTableDetail(id);
            if (dto.getQrCodeUrl() == null || dto.getQrCodeUrl().isEmpty()) {
                writeJson(Result.error(404, "二维码不存在"));
                return NONE;
            }

            String uploadPath = "upload";
            String filePath = uploadPath + dto.getQrCodeUrl();
            File file = new File(filePath);

            if (!file.exists()) {
                writeJson(Result.error(404, "二维码文件不存在"));
                return NONE;
            }

            response.setContentType("image/png");
            String fileName = URLEncoder.encode(dto.getTableNo() + ".png", "UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            try (InputStream in = new FileInputStream(file);
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
            }
        } catch (Exception e) {
            writeJson(Result.error(500, "下载二维码失败: " + e.getMessage()));
        }
        return NONE;
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
    public DiningTableCreateRequest getTableRequest() { return tableRequest; }
    public void setTableRequest(DiningTableCreateRequest tableRequest) { this.tableRequest = tableRequest; }
}
