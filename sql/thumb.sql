
       /*
 Navicat Premium Data Transfer

 Source Server         : mysql_3306
 Source Server Type    : MySQL
 Source Server Version : 80030
 Source Host           : localhost:3306
 Source Schema         : prodigal_thumb

 Target Server Type    : MySQL
 Target Server Version : 80030
 File Encoding         : 65001

 Date: 18/04/2025 19:28:50
*/
create database prodigal_thumb;
use prodigal_thumb;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `username` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                         `password` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '123456' COMMENT '密码',
                         PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'lang', '123456');
INSERT INTO `user` VALUES (2, 'admin', '123456');
INSERT INTO `user` VALUES (3, 'tset', '123456');

SET FOREIGN_KEY_CHECKS = 1;

    -- ----------------------------
-- Table structure for blog
-- ----------------------------
DROP TABLE IF EXISTS `blog`;
CREATE TABLE `blog`  (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `userId` bigint NOT NULL,
                         `title` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
                         `coverImg` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面',
                         `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '内容',
                         `thumbCount` int NOT NULL DEFAULT 0 COMMENT '点赞数',
                         `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                         `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                         PRIMARY KEY (`id`) USING BTREE,
                         INDEX `idx_userId`(`userId` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- 添加新列
ALTER TABLE blog
    ADD COLUMN isDelete tinyint  default 0   not null comment '是否删除';
-- ----------------------------
-- Records of blog
-- ----------------------------
INSERT INTO `blog` VALUES (1, 1, '测试', 'https://prodigal-1315479209.cos.ap-shanghai.myqcloud.com/%E7%B4%A2%E9%9A%86.jpg', '博客测试内容', 1, '2025-04-18 16:54:04', '2025-04-18 17:48:53');
INSERT INTO `blog` VALUES (2, 2, '开发', 'https://www.antdv.com/assets/logo.1ef800a8.svg', '亿万级点赞系统开发实战', 1, '2025-04-18 17:03:29', '2025-04-18 17:48:39');

-- ----------------------------
-- Table structure for thumb
-- ----------------------------
DROP TABLE IF EXISTS `thumb`;
CREATE TABLE `thumb`  (
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `userId` bigint NOT NULL,
                          `blogId` bigint NOT NULL,
                          `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          PRIMARY KEY (`id`) USING BTREE,
                          UNIQUE INDEX `idx_userId_blogId`(`userId` ASC, `blogId` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of thumb
-- ----------------------------
INSERT INTO `thumb` VALUES (1, 1, 2, '2025-04-18 17:48:39');
INSERT INTO `thumb` VALUES (2, 1, 1, '2025-04-18 17:48:53');