package com.library.service;

import com.library.common.BusinessException;

final class ServiceSupport {

    private ServiceSupport() {
    }

    static String text(String value) {
        return value == null ? null : value.trim();
    }

    static String requireText(String value, String label, int maxLength) {
        String text = text(value);
        if (text == null || text.isEmpty()) {
            throw new BusinessException(label + "不能为空");
        }
        return maxLength(text, maxLength, label);
    }

    static String optionalText(String value, String label, int maxLength) {
        String text = text(value);
        if (text != null && text.length() > maxLength) {
            throw new BusinessException(label + "长度不能超过 " + maxLength + " 个字符");
        }
        return text;
    }

    static String maxLength(String value, int maxLength, String label) {
        if (value != null && value.length() > maxLength) {
            throw new BusinessException(label + "长度不能超过 " + maxLength + " 个字符");
        }
        return value;
    }

    static int requireInt(Integer value, String label, int min, int max) {
        if (value == null) {
            throw new BusinessException(label + "不能为空");
        }
        if (value < min || value > max) {
            throw new BusinessException(label + "必须在 " + min + " 到 " + max + " 之间");
        }
        return value;
    }

    static double requireNonNegative(Double value, String label) {
        if (value == null) {
            throw new BusinessException(label + "不能为空");
        }
        if (value < 0) {
            throw new BusinessException(label + "不能为负数");
        }
        return value;
    }
}