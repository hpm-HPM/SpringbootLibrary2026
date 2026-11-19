package com.library.service;

import com.library.common.BusinessException;
import com.library.dto.BookDto;
import com.library.dto.RecommendDto;
import com.library.dto.RecommendRequest;
import com.library.dto.ReviewRequest;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.entity.BookRecommend;
import com.library.entity.User;
import com.library.mapper.DtoMapper;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRecommendRepository;
import com.library.repository.BookRepository;
import com.library.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendService {

    private final BookRecommendRepository recommendRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository copyRepository;
    private final UserRepository userRepository;
    private final BookService bookService;

    public RecommendService(BookRecommendRepository recommendRepository,
                            BookRepository bookRepository,
                            BookCopyRepository copyRepository,
                            UserRepository userRepository,
                            BookService bookService) {
        this.recommendRepository = recommendRepository;
        this.bookRepository = bookRepository;
        this.copyRepository = copyRepository;
        this.userRepository = userRepository;
        this.bookService = bookService;
    }

    @Transactional
    public Map<String, Object> check(String userId, RecommendRequest request) {
        String bookName = ServiceSupport.requireText(request.bookName(), "书名", 20);
        String author = ServiceSupport.requireText(request.author(), "作者", 20);
        String publisher = ServiceSupport.optionalText(request.publisher(), "出版社", 20);
        double price = request.price() == null ? 0 : ServiceSupport.requireNonNegative(request.price(), "价格");

        List<BookDto> matches = bookService.titleMatches(bookName);
        Map<String, Object> result = new LinkedHashMap<>();
        if (!matches.isEmpty()) {
            result.put("exists", true);
            result.put("books", matches);
            result.put("recommend", null);
            result.put("message", "馆藏中已有相似图书，请确认是否需要推荐");
            return result;
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        BookRecommend recommend = new BookRecommend();
        recommend.setUser(user);
        recommend.setBookName(bookName);
        recommend.setAuthor(author);
        recommend.setPublisher(publisher);
        recommend.setPrice(price);
        recommend.setStatus("待审核");
        recommendRepository.save(recommend);

        result.put("exists", false);
        result.put("books", List.of());
        result.put("recommend", DtoMapper.recommend(recommend));
        result.put("message", "推荐已提交，等待管理员审核");
        return result;
    }

    @Transactional(readOnly = true)
    public List<RecommendDto> mine(String userId) {
        return recommendRepository.findByUser_IdOrderByCreateTimeDesc(userId)
                .stream().map(DtoMapper::recommend).toList();
    }

    @Transactional(readOnly = true)
    public List<RecommendDto> pending() {
        return recommendRepository.findByStatusOrderByCreateTimeDesc("待审核")
                .stream().map(DtoMapper::recommend).toList();
    }

    @Transactional
    public RecommendDto review(ReviewRequest request) {
        if (request.id() == null || request.id().isBlank() || request.approve() == null) {
            throw new BusinessException("审核参数不完整");
        }
        BookRecommend recommend = recommendRepository.findById(request.id())
                .orElseThrow(() -> new BusinessException("推荐记录不存在"));
        if (!"待审核".equals(recommend.getStatus())) {
            throw new BusinessException("该推荐已经处理过了");
        }
        if (Boolean.TRUE.equals(request.approve())) {
            String type = ServiceSupport.text(request.type());
            if (type == null || type.isEmpty()) {
                type = "综合";
            }
            type = ServiceSupport.maxLength(type, 10, "图书类型");
            int days = request.days() == null ? 7 : request.days();
            if (days < 1 || days > 3650) {
                throw new BusinessException("可借天数必须在 1 到 3650 之间");
            }
            int stock = request.stock() == null ? 1 : request.stock();
            if (stock < 1 || stock > 1000) {
                throw new BusinessException("库存必须在 1 到 1000 之间");
            }
            String publisher = ServiceSupport.optionalText(recommend.getPublisher(), "出版社", 20);

            Book book = new Book();
            book.setTitle(ServiceSupport.requireText(recommend.getBookName(), "书名", 30));
            book.setAuthor(ServiceSupport.requireText(recommend.getAuthor(), "作者", 30));
            book.setType(type);
            book.setPublisher(publisher);
            book.setDays(days);
            book.setPrice(recommend.getPrice() == null ? 0 : recommend.getPrice());
            book.setStock(stock);
            book.setStatus("正常");
            bookRepository.save(book);

            List<BookCopy> copies = new ArrayList<>();
            for (int i = 0; i < stock; i++) {
                BookCopy copy = new BookCopy();
                copy.setBook(book);
                copy.setIsBorrowed(false);
                copies.add(copy);
            }
            copyRepository.saveAll(copies);

            recommend.setStatus("已通过");
            recommend.setReason(null);
        } else {
            String reason = ServiceSupport.requireText(request.reason(), "驳回理由", 100);
            recommend.setStatus("已驳回");
            recommend.setReason(reason);
        }
        recommendRepository.save(recommend);
        return DtoMapper.recommend(recommend);
    }
}