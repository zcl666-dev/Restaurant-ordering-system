package com.zcl.action;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.opensymphony.xwork2.ActionSupport;
import com.zcl.dto.AdminPointLogVO;
import com.zcl.dto.AdminPointsMallVO;
import com.zcl.dto.PageResult;
import com.zcl.dto.PointsMallRequest;
import com.zcl.dto.Result;
import com.zcl.service.AdminPointsService;
import org.apache.struts2.ServletActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Scope("prototype")
public class AdminPointsAction extends ActionSupport {

    @Autowired
    private AdminPointsService adminPointsService;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private int page = 0;
    private int size = 10;
    private String keyword;

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
     * 积分流水列表
     */
    public String list() {
        try {
            PageResult<AdminPointLogVO> pageResult = adminPointsService.getPointLogList(keyword, page, size);
            writeJson(Result.success("获取成功", pageResult));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取积分流水失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 积分商城列表
     */
    public String mallList() {
        try {
            PageResult<AdminPointsMallVO> pageResult = adminPointsService.getPointsMallList(page, size);
            writeJson(Result.success("获取成功", pageResult));
        } catch (Exception e) {
            writeJson(Result.error(500, "获取积分商城失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 创建积分商城商品
     */
    public String mallCreate() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            PointsMallRequest mallRequest = objectMapper.readValue(request.getInputStream(), PointsMallRequest.class);
            adminPointsService.createPointsMallItem(mallRequest);
            writeJson(Result.success("添加成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "添加失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 更新积分商城商品
     */
    public String mallUpdate() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            PointsMallRequest mallRequest = objectMapper.readValue(request.getInputStream(), PointsMallRequest.class);
            adminPointsService.updatePointsMallItem(mallRequest);
            writeJson(Result.success("更新成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "更新失败: " + e.getMessage()));
        }
        return NONE;
    }

    /**
     * 删除积分商城商品
     */
    public String mallDelete() {
        try {
            HttpServletRequest request = ServletActionContext.getRequest();
            PointsMallRequest mallRequest = objectMapper.readValue(request.getInputStream(), PointsMallRequest.class);
            adminPointsService.deletePointsMallItem(mallRequest.getId());
            writeJson(Result.success("删除成功", null));
        } catch (Exception e) {
            writeJson(Result.error(500, "删除失败: " + e.getMessage()));
        }
        return NONE;
    }

    // Getters and Setters
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}
