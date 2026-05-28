package com.zcl.service;

import com.zcl.dto.AdminUserVO;
import com.zcl.dto.PageResult;
import com.zcl.entity.User;
import com.zcl.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class AdminUserService {

    private final UserRepository userRepository;

    public AdminUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public PageResult<AdminUserVO> getUserList(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            userPage = userRepository.findByNickNameContaining(keyword.trim(), pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        return PageResult.<AdminUserVO>builder()
                .content(userPage.getContent().stream().map(this::toVO).toList())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    public AdminUserVO getUserDetail(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return toVO(user);
    }

    public void updateUser(Long id, AdminUserVO vo) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (vo.getBalance() != null) user.setBalance(vo.getBalance());
        if (vo.getPointsBalance() != null) user.setPointsBalance(vo.getPointsBalance());
        if (vo.getStatus() != null) user.setStatus(vo.getStatus());
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setStatus(0);
        userRepository.save(user);
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
