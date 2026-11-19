-- ============================================================
-- SpringbootLibrary2026 新增表（MySQL 8.x）
-- 在已导入 librarydb.sql 的 librarydb 数据库中执行本脚本。
-- 现有 book / user 表的 id 为 char(36) COLLATE utf8mb4_general_ci，
-- 因此新增表的外键列必须保持相同字符集与排序规则，否则 MySQL 无法建立外键。
-- ============================================================
SET NAMES utf8mb4;

-- 图书封面 / 图集附件
CREATE TABLE IF NOT EXISTS `book_attachment` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `createTime` datetime(6) NULL DEFAULT NULL,
  `updateTime` datetime(6) NULL DEFAULT NULL,
  `bookId` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `fileName` varchar(120) NOT NULL,
  `originalName` varchar(255) NULL DEFAULT NULL,
  `contentType` varchar(80) NULL DEFAULT NULL,
  `fileSize` bigint NULL DEFAULT NULL,
  `url` varchar(500) NOT NULL,
  `isCover` bit(1) NULL DEFAULT b'0',
  `sortOrder` int NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_attachment_book` (`bookId`),
  KEY `idx_attachment_cover` (`bookId`, `isCover`),
  CONSTRAINT `fk_attachment_book` FOREIGN KEY (`bookId`) REFERENCES `book` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 预约记录
CREATE TABLE IF NOT EXISTS `reservation` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `createTime` datetime(6) NULL DEFAULT NULL,
  `updateTime` datetime(6) NULL DEFAULT NULL,
  `bookId` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `userId` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `status` varchar(10) NULL DEFAULT NULL,
  `notifyTime` datetime(6) NULL DEFAULT NULL,
  `expireTime` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_reservation_book_status` (`bookId`, `status`),
  KEY `idx_reservation_user_status` (`userId`, `status`),
  CONSTRAINT `fk_reservation_book` FOREIGN KEY (`bookId`) REFERENCES `book` (`id`),
  CONSTRAINT `fk_reservation_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 消息通知
CREATE TABLE IF NOT EXISTS `notification` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `createTime` datetime(6) NULL DEFAULT NULL,
  `updateTime` datetime(6) NULL DEFAULT NULL,
  `userId` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `title` varchar(80) NOT NULL,
  `content` varchar(500) NULL DEFAULT NULL,
  `type` varchar(20) NULL DEFAULT NULL,
  `isRead` bit(1) NULL DEFAULT b'0',
  `link` varchar(200) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notification_user` (`userId`, `isRead`),
  CONSTRAINT `fk_notification_user` FOREIGN KEY (`userId`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- AOP 操作日志（保留历史，不建用户外键）
CREATE TABLE IF NOT EXISTS `operation_log` (
  `id` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `createTime` datetime(6) NULL DEFAULT NULL,
  `updateTime` datetime(6) NULL DEFAULT NULL,
  `userId` char(36) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `userName` varchar(60) NULL DEFAULT NULL,
  `module` varchar(40) NULL DEFAULT NULL,
  `action` varchar(60) NULL DEFAULT NULL,
  `requestUri` varchar(200) NULL DEFAULT NULL,
  `httpMethod` varchar(10) NULL DEFAULT NULL,
  `params` varchar(2000) NULL DEFAULT NULL,
  `ip` varchar(60) NULL DEFAULT NULL,
  `success` bit(1) NULL DEFAULT NULL,
  `errorMessage` varchar(500) NULL DEFAULT NULL,
  `durationMs` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_log_user` (`userId`),
  KEY `idx_log_time` (`createTime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;