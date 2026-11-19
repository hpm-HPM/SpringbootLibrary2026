package com.library.service;

import com.library.common.BusinessException;
import com.library.common.PageResult;
import com.library.dto.UserDto;
import com.library.entity.User;
import com.library.mapper.DtoMapper;
import com.library.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResult<UserDto> search(int page, int size, String keyword, String role, String major) {
        int pageNo = Math.max(page, 1);
        int pageSize = Math.min(Math.max(size, 1), 100);
        String kw = ServiceSupport.text(keyword);
        String roleValue = ServiceSupport.text(role);
        String majorValue = ServiceSupport.text(major);

        Specification<User> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (kw != null && !kw.isEmpty()) {
                String like = "%" + kw.toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("phone")), like),
                        cb.like(cb.lower(root.get("major")), like)
                ));
            }
            if (roleValue != null && !roleValue.isEmpty()) {
                predicates.add(cb.equal(root.get("role"), roleValue));
            }
            if (majorValue != null && !majorValue.isEmpty()) {
                predicates.add(cb.equal(root.get("major"), majorValue));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Pageable pageable = PageRequest.of(pageNo - 1, pageSize,
                Sort.by(Sort.Direction.DESC, "createTime").and(Sort.by(Sort.Direction.DESC, "id")));
        Page<UserDto> result = userRepository.findAll(specification, pageable).map(DtoMapper::user);
        return PageResult.of(result, pageNo, pageSize);
    }

    @Transactional(readOnly = true)
    public UserDto detail(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        return DtoMapper.user(user);
    }

    @Transactional(readOnly = true)
    public List<String> majors() {
        return userRepository.findDistinctMajors();
    }

    public User require(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }
}