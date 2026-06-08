package com.zcl.service;

import com.zcl.dao.UserDao;
import com.zcl.dto.AdminUserVO;
import com.zcl.dto.PageResult;
import com.zcl.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminUserService {

    @Autowired
    private UserDao userDao;

    public PageResult<AdminUserVO> getUserList(int page, int size, String keyword) {
        int offset = page * size;
        List<User> users;
        long totalElements;

        if (keyword != null && !keyword.trim().isEmpty()) {
            users = userDao.findByNickNameContaining(keyword.trim(), offset, size);
            totalElements = userDao.countByNickNameContaining(keyword.trim());
        } else {
            users = userDao.findAllWithPaging(offset, size);
            totalElements = userDao.count();
        }

        List<AdminUserVO> content = users.stream().map(this::toVO).collect(Collectors.toList());

        return PageResult.<AdminUserVO>builder()
                .content(content)
                .totalElements(totalElements)
                .totalPages((int) Math.ceil((double) totalElements / size))
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    public AdminUserVO getUserDetail(Long id) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return toVO(user);
    }

    public void updateUser(Long id, AdminUserVO vo) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (vo.getBalance() != null) user.setBalance(vo.getBalance());
        if (vo.getPointsBalance() != null) user.setPointsBalance(vo.getPointsBalance());
        if (vo.getStatus() != null) user.setStatus(vo.getStatus());
        userDao.save(user);
    }

    public void deleteUser(Long id) {
        User user = userDao.findById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setStatus(0);
        userDao.save(user);
    }

    private AdminUserVO toVO(User user) {
        return AdminUserVO.builder()
                .id(user.getId())
                .nickName(user.getNickName())
                .avatarUrl(user.getAvatarUrl())
                .balance(user.getBalance())
                .pointsBalance(user.getPointsBalance())
                .totalSpentAmount(user.getTotalSpentAmount())
                .totalOrderCount(user.getTotalOrderCount())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
