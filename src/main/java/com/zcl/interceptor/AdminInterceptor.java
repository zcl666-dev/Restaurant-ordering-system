package com.zcl.interceptor;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.zcl.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts2.ServletActionContext;

@Component
public class AdminInterceptor extends AbstractInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public String intercept(ActionInvocation invocation) throws Exception {
        HttpServletRequest request = ServletActionContext.getRequest();
        HttpServletResponse response = ServletActionContext.getResponse();

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            return null;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtil.parseToken(token);
            String type = claims.get("type", String.class);
            if (!"admin".equals(type)) {
                response.setStatus(403);
                return null;
            }
            Long adminId = claims.get("adminId", Long.class);
            request.setAttribute("adminId", adminId);
            return invocation.invoke();
        } catch (Exception e) {
            response.setStatus(401);
            return null;
        }
    }
}
