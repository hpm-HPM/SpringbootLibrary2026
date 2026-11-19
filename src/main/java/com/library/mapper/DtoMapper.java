package com.library.mapper;

import com.library.dto.AttachmentDto;
import com.library.dto.BookCopyDto;
import com.library.dto.BookDto;
import com.library.dto.LoanDto;
import com.library.dto.NotificationDto;
import com.library.dto.RecommendDto;
import com.library.dto.ReservationDto;
import com.library.dto.UserDto;
import com.library.entity.Book;
import com.library.entity.BookAttachment;
import com.library.entity.BookCopy;
import com.library.entity.Loan;
import com.library.entity.Notification;
import com.library.entity.BookRecommend;
import com.library.entity.Reservation;
import com.library.entity.User;

import java.time.LocalDateTime;

public final class DtoMapper {

    private DtoMapper() {
    }

    public static UserDto user(User user) {
        return new UserDto(
                user.getId(),
                user.getName(),
                user.getPhone(),
                user.getRole(),
                user.getMajor(),
                user.getLoginTime(),
                user.getCreateTime(),
                user.getUpdateTime(),
                user.getPermissions(),
                user.getPermissionLevel(),
                user.getEntryTime(),
                user.getArea(),
                user.getDuty(),
                user.getGrade(),
                user.getCollege(),
                user.getTitle()
        );
    }

    public static BookDto book(Book book, String coverUrl, long attachmentCount,
                               long copyCount, long borrowedCount) {
        return new BookDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getType(),
                book.getPublisher(),
                book.getDays(),
                book.getPrice(),
                book.getStock(),
                book.getStatus(),
                book.getIntroduction(),
                book.getTags(),
                book.getCreateTime(),
                book.getUpdateTime(),
                coverUrl,
                attachmentCount,
                copyCount,
                borrowedCount
        );
    }

    public static BookCopyDto bookCopy(BookCopy copy) {
        return new BookCopyDto(
                copy.getId(),
                copy.getBook() == null ? null : copy.getBook().getId(),
                copy.isBorrowed(),
                copy.getCreateTime(),
                copy.getUpdateTime()
        );
    }

    public static AttachmentDto attachment(BookAttachment attachment) {
        return new AttachmentDto(
                attachment.getId(),
                attachment.getBookId(),
                attachment.getFileName(),
                attachment.getOriginalName(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getUrl(),
                attachment.getIsCover(),
                attachment.getSortOrder(),
                attachment.getCreateTime()
        );
    }

    public static LoanDto loan(Loan loan) {
        BookCopy copy = loan.getBookCopy();
        Book book = copy == null ? null : copy.getBook();
        User user = loan.getUser();
        boolean overdue = loan.getReturnDate() == null
                && loan.getDueDate() != null
                && loan.getDueDate().isBefore(LocalDateTime.now());
        return new LoanDto(
                loan.getId(),
                copy == null ? null : copy.getId(),
                book == null ? null : book.getId(),
                book == null ? null : book.getTitle(),
                book == null ? null : book.getAuthor(),
                book == null ? null : book.getType(),
                book == null ? null : book.getDays(),
                user == null ? null : user.getId(),
                user == null ? null : user.getName(),
                user == null ? null : user.getRole(),
                user == null ? null : user.getMajor(),
                loan.getBorrowDate(),
                loan.getReturnDate(),
                loan.getDueDate(),
                loan.getStatus(),
                overdue,
                loan.getCreateTime()
        );
    }

    public static RecommendDto recommend(BookRecommend recommend) {
        User user = recommend.getUser();
        return new RecommendDto(
                recommend.getId(),
                user == null ? null : user.getId(),
                user == null ? null : user.getName(),
                recommend.getBookName(),
                recommend.getAuthor(),
                recommend.getPublisher(),
                recommend.getPrice(),
                recommend.getStatus(),
                recommend.getReason(),
                recommend.getCreateTime(),
                recommend.getUpdateTime()
        );
    }

    public static ReservationDto reservation(Reservation reservation, String coverUrl) {
        Book book = reservation.getBook();
        User user = reservation.getUser();
        return new ReservationDto(
                reservation.getId(),
                book == null ? null : book.getId(),
                book == null ? null : book.getTitle(),
                coverUrl,
                user == null ? null : user.getId(),
                user == null ? null : user.getName(),
                reservation.getStatus(),
                reservation.getNotifyTime(),
                reservation.getExpireTime(),
                reservation.getCreateTime()
        );
    }

    public static NotificationDto notification(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getTitle(),
                notification.getContent(),
                notification.getType(),
                notification.getIsRead(),
                notification.getLink(),
                notification.getCreateTime()
        );
    }
}