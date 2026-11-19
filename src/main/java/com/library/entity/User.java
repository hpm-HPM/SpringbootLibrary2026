package com.library.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户表使用 MySQL 的 user 表名（保留字风险），表名加反引号。
 * 数据库用 role 字段区分四种角色，角色专有字段允许为空。
 */
@Entity
@Table(name = "`user`")
public class User extends BaseEntity {

    @Column(name = "name")
    private String name;

    @JsonIgnore
    @Column(name = "password")
    private String password;

    @Column(name = "phone")
    private String phone;

    @Column(name = "role")
    private String role;

    @Column(name = "major")
    private String major;

    @Column(name = "loginTime")
    private LocalDateTime loginTime;

    @Column(name = "permissionLevel")
    private Integer permissionLevel;

    @Column(name = "entryTime")
    private LocalDate entryTime;

    @Column(name = "duty")
    private String duty;

    @Column(name = "area")
    private String area;

    @Column(name = "grade")
    private String grade;

    @Column(name = "college")
    private String college;

    @Column(name = "title")
    private String title;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }

    public Integer getPermissionLevel() {
        return permissionLevel;
    }

    public void setPermissionLevel(Integer permissionLevel) {
        this.permissionLevel = permissionLevel;
    }

    public LocalDate getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(LocalDate entryTime) {
        this.entryTime = entryTime;
    }

    public String getDuty() {
        return duty;
    }

    public void setDuty(String duty) {
        this.duty = duty;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getCollege() {
        return college;
    }

    public void setCollege(String college) {
        this.college = college;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    /**
     * 与作业 2 保持一致的权限字符串，前端据此显示功能卡和菜单。
     */
    public String[] getPermissions() {
        if (isAdmin()) {
            return new String[]{"用户信息查询", "借阅记录查询", "借阅数据统计", "查看所有书籍",
                    "添加书籍", "编辑图书", "废弃图书", "图书副本编辑",
                    "借书", "还书", "审阅推荐图书", "预约图书", "预约管理", "操作日志查询"};
        }
        if (isOperator()) {
            return new String[]{"添加书籍", "查看所有书籍", "废弃图书", "编辑图书", "图书副本编辑",
                    "借书", "还书", "个人借阅查询", "预约图书", "预约管理"};
        }
        if (isTeacher()) {
            return new String[]{"借书", "还书", "个人借阅查询", "推荐图书", "预约图书"};
        }
        return new String[]{"借书", "还书", "个人借阅查询", "预约图书"};
    }
}