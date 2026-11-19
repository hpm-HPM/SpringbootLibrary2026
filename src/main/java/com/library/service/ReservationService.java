package com.library.service;

import com.library.common.BusinessException;
import com.library.common.PageResult;
import com.library.dto.ReservationDto;
import com.library.entity.Book;
import com.library.entity.BookAttachment;
import com.library.entity.Reservation;
import com.library.entity.User;
import com.library.mapper.DtoMapper;
import com.library.repository.BookAttachmentRepository;
import com.library.repository.BookRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ReservationService {

    private static final List<String> ACTIVE_STATUSES = List.of("等待中", "可借");

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final BookAttachmentRepository attachmentRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              BookRepository bookRepository,
                              BookAttachmentRepository attachmentRepository,
                              NotificationService notificationService,
                              UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
        this.attachmentRepository = attachmentRepository;
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReservationDto reserve(String userId, String bookId) {
        Book book = bookRepository.findForUpdate(bookId)
                .orElseThrow(() -> new BusinessException("图书不存在"));
        if ("已废弃".equals(book.getStatus())) {
            throw new BusinessException("已废弃的图书不能预约");
        }
        if (book.getStock() != null && book.getStock() > 0) {
            throw new BusinessException("该图书当前有可借副本，可以直接借阅");
        }
        if (reservationRepository.existsByUser_IdAndBook_IdAndStatusIn(userId, bookId, ACTIVE_STATUSES)) {
            throw new BusinessException("您已经预约过该图书，请勿重复预约");
        }
        Reservation reservation = new Reservation();
        reservation.setBook(book);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        reservation.setUser(user);
        reservation.setStatus("等待中");
        reservationRepository.save(reservation);
        return toDto(reservation);
    }

    @Transactional
    public void cancel(String userId, String reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException("预约记录不存在"));
        if (reservation.getUser() == null || !userId.equals(reservation.getUser().getId())) {
            throw new BusinessException("无权取消该预约");
        }
        if (!ACTIVE_STATUSES.contains(reservation.getStatus())) {
            throw new BusinessException("当前预约状态不能取消");
        }
        reservation.setStatus("已取消");
        reservationRepository.save(reservation);
        if (reservation.getBook() != null && reservation.getBook().getStock() != null
                && reservation.getBook().getStock() > 0) {
            notifyNext(reservation.getBook());
        }
    }

    @Transactional(readOnly = true)
    public PageResult<ReservationDto> my(String userId, int page, int size, String status) {
        int pageNo = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);
        String statusValue = ServiceSupport.text(status);
        Specification<Reservation> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            if (statusValue != null && !statusValue.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), statusValue));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "createTime"));
        Page<ReservationDto> result = reservationRepository.findAll(specification, pageable)
                .map(this::toDto);
        return PageResult.of(result, pageNo, pageSize);
    }

    @Transactional(readOnly = true)
    public PageResult<ReservationDto> search(int page, int size, String status, String keyword) {
        int pageNo = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);
        String statusValue = ServiceSupport.text(status);
        String kw = ServiceSupport.text(keyword);
        Specification<Reservation> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (statusValue != null && !statusValue.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), statusValue));
            }
            if (kw != null && !kw.isEmpty()) {
                String like = "%" + kw.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.join("book", JoinType.LEFT).get("title")), like),
                        cb.like(cb.lower(root.join("user", JoinType.LEFT).get("name")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "createTime"));
        Page<ReservationDto> result = reservationRepository.findAll(specification, pageable)
                .map(this::toDto);
        return PageResult.of(result, pageNo, pageSize);
    }

    /**
     * 图书出现可借副本时，通知最早等待的读者。
     */
    @Transactional
    public void notifyNext(Book book) {
        if (book == null || book.getStock() == null || book.getStock() <= 0) {
            return;
        }
        List<Reservation> waiting = reservationRepository
                .findByBook_IdAndStatusOrderByCreateTimeAsc(book.getId(), "等待中");
        if (waiting.isEmpty()) {
            return;
        }
        Reservation reservation = waiting.get(0);
        reservation.setStatus("可借");
        reservation.setNotifyTime(LocalDateTime.now());
        reservation.setExpireTime(LocalDateTime.now().plusHours(24));
        reservationRepository.save(reservation);
        notificationService.create(
                reservation.getUser().getId(),
                "预约图书可借通知",
                "您预约的《" + book.getTitle() + "》已有可借副本，请在 24 小时内到馆借阅。",
                "RESERVATION",
                "/book.html?id=" + book.getId()
        );
    }

    @Scheduled(fixedDelay = 600_000)
    @Transactional
    public void expireReservations() {
        List<Reservation> expired = reservationRepository
                .findByStatusAndExpireTimeBefore("可借", LocalDateTime.now());
        for (Reservation reservation : expired) {
            reservation.setStatus("已过期");
            reservationRepository.save(reservation);
            if (reservation.getBook() != null) {
                notifyNext(reservation.getBook());
            }
        }
    }

    private ReservationDto toDto(Reservation reservation) {
        String cover = null;
        if (reservation.getBook() != null) {
            cover = attachmentRepository
                    .findByBookIdOrderByIsCoverDescSortOrderAscCreateTimeAsc(reservation.getBook().getId())
                    .stream()
                    .filter(a -> Boolean.TRUE.equals(a.getIsCover()))
                    .findFirst()
                    .map(BookAttachment::getUrl)
                    .orElse(null);
        }
        return DtoMapper.reservation(reservation, cover);
    }
}