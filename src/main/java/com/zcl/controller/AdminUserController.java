package com.zcl.controller;

import com.zcl.dto.AdminUserVO;
import com.zcl.dto.PageResult;
import com.zcl.dto.Result;
import com.zcl.service.AdminUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ResponseEntity<Result<PageResult<AdminUserVO>>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(Result.success("获取成功", adminUserService.getUserList(page, size, keyword)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Result<AdminUserVO>> getUserDetail(@PathVariable Long id) {
        return ResponseEntity.ok(Result.success("获取成功", adminUserService.getUserDetail(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Result<Void>> updateUser(@PathVariable Long id, @RequestBody AdminUserVO vo) {
        adminUserService.updateUser(id, vo);
        return ResponseEntity.ok(Result.success("更新成功", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Result<Void>> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok(Result.success("删除成功", null));
    }
}
