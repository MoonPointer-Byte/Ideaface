-- -----------------------------------------------------
-- 数据库: interview_agent_db
-- -----------------------------------------------------
CREATE DATABASE IF NOT EXISTS `interview_agent_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `interview_agent_db`;

-- -----------------------------------------------------
-- 表: users (存储用户信息)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `users` (
                                       `id` BIGINT NOT NULL AUTO_INCREMENT,
                                       `username` VARCHAR(255) NOT NULL,
    `email` VARCHAR(255) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `UK_username` (`username` ASC) VISIBLE,
    UNIQUE INDEX `UK_email` (`email` ASC) VISIBLE)
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- 表: questions (存储面试题目)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `questions` (
                                           `id` BIGINT NOT NULL AUTO_INCREMENT,
                                           `title` VARCHAR(255) NOT NULL,
    `content` TEXT NULL,
    `category` VARCHAR(255) NOT NULL,
    `difficulty` VARCHAR(255) NOT NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- 表: learning_paths (存储学习路线)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `learning_paths` (
                                                `id` BIGINT NOT NULL AUTO_INCREMENT,
                                                `title` VARCHAR(255) NOT NULL,
    `description` VARCHAR(1000) NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- 表: courses (存储推荐课程)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `courses` (
                                         `id` BIGINT NOT NULL AUTO_INCREMENT,
                                         `title` VARCHAR(255) NOT NULL,
    `description` VARCHAR(1000) NULL,
    `url` VARCHAR(255) NOT NULL,
    `platform` VARCHAR(255) NULL,
    `category` VARCHAR(255) NULL,
    PRIMARY KEY (`id`))
    ENGINE = InnoDB;


-- -----------------------------------------------------
-- 表: interview_report (存储面试报告)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `interview_report` (
                                                  `session_id` VARCHAR(255) NOT NULL,
    `position_id` VARCHAR(255) NOT NULL,
    `position_title` VARCHAR(255) NULL,
    `user_id` VARCHAR(255) NULL,
    `transcript` TEXT NULL,
    `llm_response_json` TEXT NULL,
    `video_file_path` VARCHAR(255) NULL,
    `audio_file_path` VARCHAR(255) NULL,
    `create_time` DATETIME NOT NULL,
    `status` VARCHAR(255) NULL,
    PRIMARY KEY (`session_id`))
    ENGINE = InnoDB;

-- -----------------------------------------------------
-- 表: comments (存储评论) - 未来任务3需要
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `comments` (
 `id` BIGINT NOT NULL AUTO_INCREMENT,
 `content` TEXT NOT NULL,
 `create_time` DATETIME NOT NULL,
 `user_id` BIGINT NOT NULL,
 `question_id` BIGINT NOT NULL,
 PRIMARY KEY (`id`))
ENGINE = InnoDB;