package com.library.service;

import com.library.common.BusinessException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final Set<String> EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    private Path root;

    @PostConstruct
    public void init() {
        root = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException exception) {
            throw new IllegalStateException("无法创建上传目录: " + root, exception);
        }
    }

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!IMAGE_TYPES.contains(contentType)) {
            throw new BusinessException("只支持 JPG、PNG、GIF、WEBP 图片");
        }
        String originalName = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        originalName = Path.of(originalName).getFileName().toString();
        if (originalName.length() > 200) {
            originalName = originalName.substring(originalName.length() - 200);
        }
        String extension = extension(originalName);
        if (!EXTENSIONS.contains(extension)) {
            throw new BusinessException("图片扩展名不合法");
        }
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path target = root.resolve(storedName).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException("文件路径不合法");
        }
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new BusinessException("图片保存失败，请稍后重试");
        }
        return new StoredFile(storedName, originalName, contentType, file.getSize(), "/uploads/" + storedName);
    }

    public void delete(String storedName) {
        if (storedName == null || storedName.isBlank()) {
            return;
        }
        Path target = root.resolve(storedName).normalize();
        if (!target.startsWith(root)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // 文件删除失败不影响数据库记录清理
        }
    }

    private String extension(String name) {
        int index = name.lastIndexOf('.');
        return index < 0 ? "" : name.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    public record StoredFile(String fileName, String originalName, String contentType, long size, String url) {
    }
}