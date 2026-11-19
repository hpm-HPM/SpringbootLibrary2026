package com.library.service;

import com.library.common.BusinessException;
import com.library.common.PageResult;
import com.library.dto.NotificationDto;
import com.library.entity.Notification;
import com.library.mapper.DtoMapper;
import com.library.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification create(String userId, String title, String content, String type, String link) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(ServiceSupport.maxLength(title, 80, "通知标题"));
        notification.setContent(ServiceSupport.maxLength(content, 500, "通知内容"));
        notification.setType(type);
        notification.setIsRead(false);
        notification.setLink(link);
        return notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public PageResult<NotificationDto> list(String userId, int page, int size) {
        int pageNo = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "createTime"));
        Page<NotificationDto> result = notificationRepository
                .findByUserIdOrderByCreateTimeDesc(userId, pageable)
                .map(DtoMapper::notification);
        return PageResult.of(result, pageNo, pageSize);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> unread(String userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreateTimeDesc(userId)
                .stream().map(DtoMapper::notification).toList();
    }

    @Transactional(readOnly = true)
    public long unreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public void markRead(String userId, String id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("通知不存在"));
        if (!userId.equals(notification.getUserId())) {
            throw new BusinessException("无权操作该通知");
        }
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllRead(String userId) {
        List<Notification> notifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreateTimeDesc(userId);
        for (Notification notification : notifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(notifications);
    }
}