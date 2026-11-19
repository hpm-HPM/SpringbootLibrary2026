package com.library.service;

import com.library.common.BusinessException;
import com.library.dto.LoginRequest;
import com.library.dto.RegisterRequest;
import com.library.dto.UserDto;
import com.library.entity.User;
import com.library.mapper.DtoMapper;
import com.library.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern PHONE = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Map<String, String> ROLES = Map.of(
            "admin", "管理员",
            "operator", "操作员",
            "student", "学生",
            "teacher", "教师"
    );

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserDto login(LoginRequest request) {
        String name = ServiceSupport.requireText(request.name(), "用户名", 30);
        String password = request.password() == null ? "" : request.password();
        if (password.isEmpty()) {
            throw new BusinessException("密码不能为空");
        }
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        if (!password.equals(user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        user.setLoginTime(LocalDateTime.now());
        userRepository.save(user);
        return DtoMapper.user(user);
    }

    @Transactional
    public UserDto register(RegisterRequest request) {
        String name = ServiceSupport.requireText(request.name(), "姓名", 30);
        String password = request.password() == null ? "" : request.password();
        if (password.length() < 6 || password.length() > 20) {
            throw new BusinessException("密码长度必须为 6 到 20 个字符");
        }
        String phone = ServiceSupport.requireText(request.phone(), "手机号", 20);
        if (!PHONE.matcher(phone).matches()) {
            throw new BusinessException("手机号格式不正确");
        }
        String major = ServiceSupport.requireText(request.major(), "专业", 100);
        String role = normalizeRole(request.role());
        if (userRepository.existsByName(name)) {
            throw new BusinessException("该姓名已被注册");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new BusinessException("该手机号已被注册");
        }

        User user = new User();
        user.setName(name);
        user.setPassword(password);
        user.setPhone(phone);
        user.setRole(role);
        user.setMajor(major);

        if ("管理员".equals(role)) {
            int level = request.permissionLevel() == null ? 1 : request.permissionLevel();
            if (level < 1 || level > 9) {
                throw new BusinessException("权限等级必须在 1 到 9 之间");
            }
            user.setPermissionLevel(level);
            user.setEntryTime(request.entryTime() == null ? LocalDate.now() : request.entryTime());
        } else if ("操作员".equals(role)) {
            user.setArea(ServiceSupport.requireText(request.area(), "负责区域", 100));
            user.setDuty(ServiceSupport.requireText(request.duty(), "岗位职责", 100));
        } else if ("学生".equals(role)) {
            user.setGrade(ServiceSupport.requireText(request.grade(), "年级", 100));
            user.setCollege(ServiceSupport.requireText(request.college(), "学院", 100));
        } else {
            user.setCollege(ServiceSupport.requireText(request.college(), "学院", 100));
            user.setTitle(ServiceSupport.requireText(request.title(), "职称", 100));
        }

        userRepository.save(user);
        return DtoMapper.user(user);
    }

    private String normalizeRole(String role) {
        String value = ServiceSupport.text(role);
        if (value == null || value.isEmpty()) {
            throw new BusinessException("请选择角色");
        }
        String normalized = ROLES.getOrDefault(value.toLowerCase(Locale.ROOT), value);
        if (!"管理员".equals(normalized) && !"操作员".equals(normalized)
                && !"学生".equals(normalized) && !"教师".equals(normalized)) {
            throw new BusinessException("角色不合法");
        }
        return normalized;
    }
}