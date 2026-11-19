package com.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "book_attachment", indexes = {
        @Index(name = "idx_attachment_book", columnList = "bookId"),
        @Index(name = "idx_attachment_cover", columnList = "bookId,isCover")
})
public class BookAttachment extends BaseEntity {

    @Column(name = "bookId", length = 36, nullable = false)
    private String bookId;

    @Column(name = "fileName", length = 120, nullable = false)
    private String fileName;

    @Column(name = "originalName", length = 255)
    private String originalName;

    @Column(name = "contentType", length = 80)
    private String contentType;

    @Column(name = "fileSize")
    private Long fileSize;

    /** 浏览器可访问的相对地址，例如 /uploads/{bookId}/{uuid}.jpg */
    @Column(name = "url", length = 500, nullable = false)
    private String url;

    @Column(name = "isCover")
    private Boolean isCover;

    @Column(name = "sortOrder")
    private Integer sortOrder;

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getIsCover() {
        return isCover;
    }

    public void setIsCover(Boolean cover) {
        isCover = cover;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}