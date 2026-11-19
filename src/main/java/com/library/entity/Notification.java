package com.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification", indexes = {
        @Index(name = "idx_notification_user", columnList = "userId,isRead")
})
public class Notification extends BaseEntity {

    @Column(name = "userId", length = 36, nullable = false)
    private String userId;

    @Column(name = "title", length = 80, nullable = false)
    private String title;

    @Column(name = "content", length = 500)
    private String content;

    /** RESERVATION / SYSTEM */
    @Column(name = "type", length = 20)
    private String type;

    @Column(name = "isRead")
    private Boolean isRead;

    @Column(name = "link", length = 200)
    private String link;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getIsRead() {
        return isRead;
    }

    public void setIsRead(Boolean read) {
        isRead = read;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}