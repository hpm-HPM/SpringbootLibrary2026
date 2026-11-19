package com.library.common;

import com.library.dto.UserDto;
import com.library.entity.User;

import java.io.Serializable;
import java.util.Arrays;

public record LoginUser(
        String id,
        String name,
        String role,
        String major,
        String phone,
        String[] permissions
) implements Serializable {

    public static LoginUser from(User user) {
        return new LoginUser(
                user.getId(),
                user.getName(),
                user.getRole(),
                user.getMajor(),
                user.getPhone(),
                user.getPermissions()
        );
    }

    public static LoginUser from(UserDto user) {
        return new LoginUser(
                user.id(),
                user.name(),
                user.role(),
                user.major(),
                user.phone(),
                user.permissions()
        );
    }

    public boolean hasPermission(String permission) {
        return permissions != null && Arrays.asList(permissions).contains(permission);
    }

    public boolean isAdmin() {
        return "管理员".equals(role);
    }

    public boolean isOperator() {
        return "操作员".equals(role);
    }

    public boolean isTeacher() {
        return "教师".equals(role);
    }

    public boolean isStudent() {
        return "学生".equals(role);
    }
}