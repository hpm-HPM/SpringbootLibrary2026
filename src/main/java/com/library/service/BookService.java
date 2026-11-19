package com.library.service;

import com.library.common.BusinessException;
import com.library.common.PageResult;
import com.library.dto.AttachmentDto;
import com.library.dto.BookCopyDto;
import com.library.dto.BookDetailDto;
import com.library.dto.BookDto;
import com.library.dto.BookRequest;
import com.library.entity.Book;
import com.library.entity.BookAttachment;
import com.library.entity.BookCopy;
import com.library.mapper.DtoMapper;
import com.library.repository.BookAttachmentRepository;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookCopyRepository copyRepository;
    private final BookAttachmentRepository attachmentRepository;
    private final LoanRepository loanRepository;
    private final FileStorageService fileStorageService;
    private final ReservationService reservationService;

    public BookService(BookRepository bookRepository,
                       BookCopyRepository copyRepository,
                       BookAttachmentRepository attachmentRepository,
                       LoanRepository loanRepository,
                       FileStorageService fileStorageService,
                       ReservationService reservationService) {
        this.bookRepository = bookRepository;
        this.copyRepository = copyRepository;
        this.attachmentRepository = attachmentRepository;
        this.loanRepository = loanRepository;
        this.fileStorageService = fileStorageService;
        this.reservationService = reservationService;
    }

    @Transactional(readOnly = true)
    public PageResult<BookDto> search(int page, int size, String keyword, String type,
                                      String status, String author, String publisher) {
        int pageNo = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);
        String kw = ServiceSupport.text(keyword);
        String typeValue = ServiceSupport.text(type);
        String statusValue = ServiceSupport.text(status);
        String authorValue = ServiceSupport.text(author);
        String publisherValue = ServiceSupport.text(publisher);

        Specification<Book> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (kw != null && !kw.isEmpty()) {
                String like = "%" + kw.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("author")), like),
                        cb.like(cb.lower(root.get("type")), like),
                        cb.like(cb.lower(root.get("publisher")), like),
                        cb.like(cb.lower(root.get("introduction")), like)
                ));
            }
            if (typeValue != null && !typeValue.isEmpty()) {
                predicates.add(cb.equal(root.get("type"), typeValue));
            }
            if (statusValue != null && !statusValue.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), statusValue));
            }
            if (authorValue != null && !authorValue.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("author")),
                        "%" + authorValue.toLowerCase(Locale.ROOT) + "%"));
            }
            if (publisherValue != null && !publisherValue.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("publisher")),
                        "%" + publisherValue.toLowerCase(Locale.ROOT) + "%"));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "createTime").and(Sort.by(Sort.Direction.DESC, "id")));
        Page<BookDto> result = bookRepository.findAll(specification, pageable).map(this::toDto);
        return PageResult.of(result, pageNo, pageSize);
    }

    @Transactional(readOnly = true)
    public List<BookDto> borrowable(String keyword) {
        String kw = ServiceSupport.text(keyword);
        Specification<Book> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), "正常"));
            predicates.add(cb.greaterThan(root.get("stock"), 0));
            if (kw != null && !kw.isEmpty()) {
                String like = "%" + kw.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("author")), like)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        return bookRepository.findAll(specification,
                        Sort.by(Sort.Direction.DESC, "createTime"))
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public BookDetailDto detail(String id) {
        Book book = requireBook(id);
        List<BookCopyDto> copies = copyRepository.findByBook_IdOrderByCreateTimeAsc(id)
                .stream().map(DtoMapper::bookCopy).toList();
        List<AttachmentDto> attachments = attachmentRepository
                .findByBookIdOrderByIsCoverDescSortOrderAscCreateTimeAsc(id)
                .stream().map(DtoMapper::attachment).toList();
        return new BookDetailDto(toDto(book), copies, attachments);
    }

    @Transactional
    public BookDto add(BookRequest request) {
        Book book = new Book();
        book.setTitle(ServiceSupport.requireText(request.title(), "书名", 30));
        book.setAuthor(ServiceSupport.requireText(request.author(), "作者", 30));
        book.setType(ServiceSupport.requireText(request.type(), "图书类型", 10));
        book.setPublisher(ServiceSupport.optionalText(request.publisher(), "出版社", 20));
        book.setDays(ServiceSupport.requireInt(request.days(), "可借天数", 1, 3650));
        book.setPrice(ServiceSupport.requireNonNegative(request.price(), "价格"));
        int stock = ServiceSupport.requireInt(request.stock(), "库存", 0, 1000);
        book.setStock(stock);
        book.setStatus("正常");
        book.setIntroduction(ServiceSupport.optionalText(request.introduction(), "简介", 512));
        book.setTags(ServiceSupport.optionalText(request.tags(), "标签", 500));
        bookRepository.save(book);

        List<BookCopy> copies = new ArrayList<>();
        for (int i = 0; i < stock; i++) {
            BookCopy copy = new BookCopy();
            copy.setBook(book);
            copy.setIsBorrowed(false);
            copies.add(copy);
        }
        copyRepository.saveAll(copies);
        return toDto(book);
    }

    @Transactional
    public BookDto update(String id, BookRequest request) {
        Book book = requireBook(id);
        if ("已废弃".equals(book.getStatus())) {
            throw new BusinessException("已废弃的图书不能编辑");
        }
        book.setTitle(ServiceSupport.requireText(request.title(), "书名", 30));
        book.setAuthor(ServiceSupport.requireText(request.author(), "作者", 30));
        book.setType(ServiceSupport.requireText(request.type(), "图书类型", 10));
        if (request.publisher() != null) {
            book.setPublisher(ServiceSupport.optionalText(request.publisher(), "出版社", 20));
        }
        book.setDays(ServiceSupport.requireInt(request.days(), "可借天数", 1, 3650));
        book.setPrice(ServiceSupport.requireNonNegative(request.price(), "价格"));
        book.setIntroduction(ServiceSupport.optionalText(request.introduction(), "简介", 512));
        book.setTags(ServiceSupport.optionalText(request.tags(), "标签", 500));
        bookRepository.save(book);
        return toDto(book);
    }

    @Transactional
    public void discard(String id) {
        Book book = bookRepository.findForUpdate(id)
                .orElseThrow(() -> new BusinessException("图书不存在"));
        if ("已废弃".equals(book.getStatus())) {
            throw new BusinessException("该图书已经废弃");
        }
        long borrowed = copyRepository.countByBook_IdAndIsBorrowedTrue(id);
        if (borrowed > 0) {
            throw new BusinessException("该图书还有 " + borrowed + " 个副本未归还，不能废弃");
        }
        long copies = copyRepository.countByBook_Id(id);
        if (book.getStock() == null || book.getStock() != copies) {
            throw new BusinessException("该图书存在不可借副本，暂不能废弃");
        }
        book.setStatus("已废弃");
        bookRepository.save(book);
    }

    @Transactional(readOnly = true)
    public List<BookCopyDto> copies(String bookId) {
        requireBook(bookId);
        return copyRepository.findByBook_IdOrderByCreateTimeAsc(bookId)
                .stream().map(DtoMapper::bookCopy).toList();
    }

    @Transactional
    public BookCopyDto addCopy(String bookId) {
        Book book = bookRepository.findForUpdate(bookId)
                .orElseThrow(() -> new BusinessException("图书不存在"));
        if ("已废弃".equals(book.getStatus())) {
            throw new BusinessException("已废弃的图书不能新增副本");
        }
        BookCopy copy = new BookCopy();
        copy.setBook(book);
        copy.setIsBorrowed(false);
        copyRepository.save(copy);
        book.setStock((book.getStock() == null ? 0 : book.getStock()) + 1);
        bookRepository.save(book);
        reservationService.notifyNext(book);
        return DtoMapper.bookCopy(copy);
    }

    @Transactional
    public void deleteCopy(String copyId) {
        BookCopy copy = copyRepository.findById(copyId)
                .orElseThrow(() -> new BusinessException("副本不存在"));
        if (Boolean.TRUE.equals(copy.isBorrowed())) {
            throw new BusinessException("该副本正在借阅中，不能删除");
        }
        if (loanRepository.existsByBookCopy_Id(copyId)) {
            throw new BusinessException("该副本存在借阅记录，为保证借阅数据完整不能删除");
        }
        Book book = copy.getBook();
        if (book == null) {
            throw new BusinessException("副本关联的图书不存在");
        }
        if (book.getStock() == null || book.getStock() <= 0) {
            throw new BusinessException("图书可借库存不足，不能删除副本");
        }
        copyRepository.delete(copy);
        book.setStock(book.getStock() - 1);
        bookRepository.save(book);
    }

    @Transactional
    public List<AttachmentDto> upload(String bookId, MultipartFile[] files, boolean asCover) {
        Book book = requireBook(bookId);
        if (files == null || files.length == 0) {
            throw new BusinessException("请选择要上传的图片");
        }
        long existing = attachmentRepository.countByBookId(bookId);
        if (existing + files.length > 20) {
            throw new BusinessException("每本图书最多上传 20 张图片");
        }
        boolean hasCover = attachmentRepository.findFirstByBookIdAndIsCoverTrue(bookId).isPresent();
        List<AttachmentDto> result = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            FileStorageService.StoredFile stored = fileStorageService.store(files[i]);
            BookAttachment attachment = new BookAttachment();
            attachment.setBookId(book.getId());
            attachment.setFileName(stored.fileName());
            attachment.setOriginalName(stored.originalName());
            attachment.setContentType(stored.contentType());
            attachment.setFileSize(stored.size());
            attachment.setUrl(stored.url());
            attachment.setIsCover(asCover && i == 0 && !hasCover);
            attachment.setSortOrder((int) existing + i);
            attachmentRepository.save(attachment);
            result.add(DtoMapper.attachment(attachment));
        }
        return result;
    }

    @Transactional
    public void deleteAttachment(String attachmentId) {
        BookAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BusinessException("附件不存在"));
        boolean wasCover = Boolean.TRUE.equals(attachment.getIsCover());
        String bookId = attachment.getBookId();
        attachmentRepository.delete(attachment);
        fileStorageService.delete(attachment.getFileName());
        if (wasCover) {
            attachmentRepository.findByBookIdOrderByIsCoverDescSortOrderAscCreateTimeAsc(bookId)
                    .stream().findFirst().ifPresent(first -> {
                        first.setIsCover(true);
                        attachmentRepository.save(first);
                    });
        }
    }

    @Transactional
    public void setCover(String attachmentId) {
        BookAttachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BusinessException("附件不存在"));
        List<BookAttachment> attachments = attachmentRepository
                .findByBookIdOrderByIsCoverDescSortOrderAscCreateTimeAsc(attachment.getBookId());
        for (BookAttachment item : attachments) {
            item.setIsCover(item.getId().equals(attachmentId));
        }
        attachmentRepository.saveAll(attachments);
    }

    @Transactional(readOnly = true)
    public List<String> types() {
        return bookRepository.findDistinctTypes();
    }

    @Transactional(readOnly = true)
    public List<BookDto> titleMatches(String title) {
        String text = ServiceSupport.text(title);
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        return bookRepository.findByTitleContainingIgnoreCaseOrderByCreateTimeDesc(text)
                .stream().map(this::toDto).toList();
    }

    public Book requireBook(String id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException("图书不存在"));
    }

    private BookDto toDto(Book book) {
        List<BookAttachment> attachments = attachmentRepository
                .findByBookIdOrderByIsCoverDescSortOrderAscCreateTimeAsc(book.getId());
        String coverUrl = attachments.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsCover()))
                .findFirst()
                .or(() -> attachments.stream().findFirst())
                .map(BookAttachment::getUrl)
                .orElse(null);
        long copies = copyRepository.countByBook_Id(book.getId());
        long borrowed = copyRepository.countByBook_IdAndIsBorrowedTrue(book.getId());
        return DtoMapper.book(book, coverUrl, attachments.size(), copies, borrowed);
    }
}