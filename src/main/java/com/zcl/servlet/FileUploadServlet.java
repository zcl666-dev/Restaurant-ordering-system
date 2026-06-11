package com.zcl.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zcl.dto.Result;
import com.zcl.service.OssService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件上传 Servlet — 接收前端图片，通过 OssService 上传到阿里云 OSS
 * 路径: /api/upload（在 web.xml 中注册）
 */
public class FileUploadServlet extends HttpServlet {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        // 获取 Spring 上下文中的 OssService
        WebApplicationContext ctx = WebApplicationContextUtils
                .getWebApplicationContext(getServletContext());
        OssService ossService = ctx.getBean(OssService.class);

        try {
            // 从 multipart 请求中获取文件
            Part filePart = request.getPart("file");
            if (filePart == null || filePart.getSize() == 0) {
                objectMapper.writeValue(response.getOutputStream(),
                        Result.error(400, "请选择要上传的文件"));
                return;
            }

            String fileName = filePart.getSubmittedFileName();
            InputStream inputStream = filePart.getInputStream();

            // 上传到 OSS
            String imageUrl = ossService.uploadFile(inputStream, fileName);
            inputStream.close();

            objectMapper.writeValue(response.getOutputStream(),
                    Result.success("上传成功", imageUrl));

        } catch (Exception e) {
            e.printStackTrace();
            objectMapper.writeValue(response.getOutputStream(),
                    Result.error(500, "上传失败: " + e.getMessage()));
        }
    }
}
