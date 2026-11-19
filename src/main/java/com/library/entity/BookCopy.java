package com.library.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "bookcopy")
public class BookCopy extends BaseEntity {

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bookId", nullable = false)
    private Book book;

    /** 数据库是 bit(1)，JPA 的 Boolean 可以直接映射。 */
    @Column(name = "isBorrowed")
    private Boolean isBorrowed;

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Boolean getIsBorrowed() {
        return isBorrowed;
    }

    public void setIsBorrowed(Boolean borrowed) {
        isBorrowed = borrowed;
    }

    public boolean isBorrowed() {
        return Boolean.TRUE.equals(isBorrowed);
    }
}