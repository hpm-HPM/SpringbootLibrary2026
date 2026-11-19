package com.library.service;

import com.library.common.BusinessException;
import com.library.common.PageResult;
import com.library.dto.LoanDto;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.entity.Loan;
import com.library.entity.Reservation;
import com.library.entity.User;
import com.library.mapper.DtoMapper;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository copyRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    public LoanService(LoanRepository loanRepository,
                       BookRepository bookRepository,
                       BookCopyRepository copyRepository,
                       UserRepository userRepository,
                       ReservationRepository reservationRepository,
                       ReservationService reservationService) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.copyRepository = copyRepository;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
    }

    @Transactional
    public LoanDto borrow(String userId, String bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        long active = loanRepository.countActiveByUser(userId);
        if (active >= 5) {
            throw new BusinessException("每位读者最多同时借阅 5 本，请先归还部分图书");
        }
        Book book = bookRepository.findForUpdate(bookId)
                .orElseThrow(() -> new BusinessException("图书不存在"));
        if (!"正常".equals(book.getStatus())) {
            throw new BusinessException("该图书已废弃，不能借阅");
        }
        if (book.getStock() == null || book.getStock() <= 0) {
            throw new BusinessException("该图书暂无可借副本，可先预约");
        }
        BookCopy copy = copyRepository
                .findFirstByBook_IdAndIsBorrowedFalseOrderByCreateTimeAsc(bookId)
                .orElseThrow(() -> new BusinessException("该图书暂无可借副本，可先预约"));

        copy.setIsBorrowed(true);
        copyRepository.save(copy);
        book.setStock(book.getStock() - 1);
        bookRepository.save(book);

        LocalDateTime now = LocalDateTime.now();
        Loan loan = new Loan();
        loan.setBookCopy(copy);
        loan.setUser(user);
        loan.setBorrowDate(now);
        loan.setDueDate(now.plusDays(book.getDays() == null ? 30 : book.getDays()));
        loan.setStatus("未还");
        loanRepository.save(loan);

        reservationRepository.findFirstByUser_IdAndBook_IdAndStatusInOrderByCreateTimeDesc(
                        userId, bookId, List.of("等待中", "可借"))
                .ifPresent(reservation -> {
                    reservation.setStatus("已完成");
                    reservationRepository.save(reservation);
                });
        return DtoMapper.loan(loan);
    }

    @Transactional
    public LoanDto returnBook(String userId, String copyId) {
        BookCopy copy = copyRepository.findById(copyId)
                .orElseThrow(() -> new BusinessException("副本不存在"));
        Loan loan = loanRepository
                .findFirstByBookCopy_IdAndReturnDateIsNullAndStatusNotOrderByBorrowDateDesc(copyId, "已还")
                .orElseThrow(() -> new BusinessException("该副本没有未归还的借阅记录"));
        if (loan.getUser() == null || !userId.equals(loan.getUser().getId())) {
            throw new BusinessException("只能归还自己借阅的图书");
        }
        Book book = copy.getBook();
        if (book == null) {
            throw new BusinessException("副本关联的图书不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        loan.setReturnDate(now);
        loan.setStatus("已还");
        loanRepository.save(loan);
        copy.setIsBorrowed(false);
        copyRepository.save(copy);
        book.setStock((book.getStock() == null ? 0 : book.getStock()) + 1);
        bookRepository.save(book);
        reservationService.notifyNext(book);
        return DtoMapper.loan(loan);
    }

    @Transactional(readOnly = true)
    public PageResult<LoanDto> my(String userId, int page, int size, String filter,
                                  String start, String end) {
        int pageNo = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);
        LocalDate startDate = parseDate(start, "开始日期");
        LocalDate endDate = parseDate(end, "结束日期");
        checkRange(startDate, endDate);
        String filterValue = ServiceSupport.text(filter);

        Specification<Loan> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), userId));
            if ("unreturned".equalsIgnoreCase(filterValue)) {
                predicates.add(cb.isNull(root.get("returnDate")));
                predicates.add(cb.notEqual(root.get("status"), "已还"));
            }
            addDateRange(predicates, root, cb, startDate, endDate);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "borrowDate").and(Sort.by(Sort.Direction.DESC, "createTime")));
        Page<LoanDto> result = loanRepository.findAll(specification, pageable).map(DtoMapper::loan);
        return PageResult.of(result, pageNo, pageSize);
    }

    @Transactional(readOnly = true)
    public PageResult<LoanDto> search(int page, int size, String keyword, String status,
                                      boolean overdue, String start, String end) {
        int pageNo = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);
        String kw = ServiceSupport.text(keyword);
        String statusValue = ServiceSupport.text(status);
        LocalDate startDate = parseDate(start, "开始日期");
        LocalDate endDate = parseDate(end, "结束日期");
        checkRange(startDate, endDate);

        Specification<Loan> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (kw != null && !kw.isEmpty()) {
                String like = "%" + kw.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.join("user", JoinType.LEFT).get("name")), like),
                        cb.like(cb.lower(root.join("bookCopy", JoinType.LEFT)
                                .join("book", JoinType.LEFT).get("title")), like)
                ));
            }
            if (statusValue != null && !statusValue.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), statusValue));
            }
            if (overdue) {
                predicates.add(cb.isNull(root.get("returnDate")));
                predicates.add(cb.notEqual(root.get("status"), "已还"));
                predicates.add(cb.lessThan(root.get("dueDate"), LocalDateTime.now()));
            }
            addDateRange(predicates, root, cb, startDate, endDate);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "borrowDate").and(Sort.by(Sort.Direction.DESC, "createTime")));
        Page<LoanDto> result = loanRepository.findAll(specification, pageable).map(DtoMapper::loan);
        return PageResult.of(result, pageNo, pageSize);
    }

    private void addDateRange(List<Predicate> predicates, jakarta.persistence.criteria.Root<Loan> root,
                              jakarta.persistence.criteria.CriteriaBuilder cb,
                              LocalDate startDate, LocalDate endDate) {
        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("borrowDate"),
                    startDate.atStartOfDay()));
        }
        if (endDate != null) {
            predicates.add(cb.lessThan(root.get("borrowDate"),
                    endDate.plusDays(1).atStartOfDay()));
        }
    }

    private LocalDate parseDate(String value, String label) {
        String text = ServiceSupport.text(value);
        if (text == null || text.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text);
        } catch (Exception exception) {
            throw new BusinessException(label + "格式应为 yyyy-MM-dd");
        }
    }

    private void checkRange(LocalDate start, LocalDate end) {
        if (start != null && end != null && start.isAfter(end)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
    }
}