USE opus;

DROP TABLE IF EXISTS `contest`;
DROP TABLE IF EXISTS `contest_award`;
DROP TABLE IF EXISTS `contest_category`;
DROP TABLE IF EXISTS `contest_template`;
DROP TABLE IF EXISTS `contest_track`;
DROP TABLE IF EXISTS `contest_sort`;
DROP TABLE IF EXISTS `file`;
DROP TABLE IF EXISTS `member`;
DROP TABLE IF EXISTS `member_roles`;
DROP TABLE IF EXISTS `notice`;
DROP TABLE IF EXISTS `team`;
DROP TABLE IF EXISTS `team_member_roles`;
DROP TABLE IF EXISTS `team_vote`;
DROP TABLE IF EXISTS `team_like`;
DROP TABLE IF EXISTS `team_comment`;
DROP TABLE IF EXISTS `team_contest_award`;
DROP TABLE IF EXISTS `team_member`;
DROP TABLE IF EXISTS `member_roles`;
DROP TABLE IF EXISTS `team`;
DROP TABLE IF EXISTS `contest_sort`;
DROP TABLE IF EXISTS `contest_track`;
DROP TABLE IF EXISTS `contest_award`;
DROP TABLE IF EXISTS `file`;
DROP TABLE IF EXISTS `notice`;
DROP TABLE IF EXISTS `member`;
DROP TABLE IF EXISTS `contest_category`;
DROP TABLE IF EXISTS `contest`;

CREATE TABLE `contest` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `category_id` bigint NOT NULL,
  `contest_name` varchar(255) NOT NULL,
  `is_current` bit(1) NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `max_votes_limit` int NOT NULL,
  `vote_end_at` datetime(6) NOT NULL,
  `vote_start_at` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `contest_award` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `award_color` varchar(255) NOT NULL,
  `award_name` varchar(255) NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `contest_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `contest_category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `category_name` varchar(255) NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `contest_template` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `contest_id` bigint NOT NULL,
  `track_required` bit(1) NOT NULL,
  `project_name_required` bit(1) NOT NULL,
  `team_name_required` bit(1) NOT NULL,
  `leader_required` bit(1) NOT NULL,
  `team_members_required` bit(1) NOT NULL,
  `professor_required` bit(1) NOT NULL,
  `github_path_required` bit(1) NOT NULL,
  `you_tube_path_required` bit(1) NOT NULL,
  `production_path_required` bit(1) NOT NULL,
  `overview_required` bit(1) NOT NULL,
  `poster_required` bit(1) NOT NULL,
  `images_required` bit(1) NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_contest_team_template_contest_id` (`contest_id`)
);

CREATE TABLE `contest_track` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `track_name` varchar(255) NOT NULL,
  `contest_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `contest_sort` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `mode` enum('ASC','CUSTOM','RANDOM') NOT NULL,
  `contest_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_contest_id` (`contest_id`)
);

CREATE TABLE `file` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `file_path` varchar(255) NOT NULL,
  `image_type` enum('BANNER','PREVIEW','THUMBNAIL','POSTER') NOT NULL,
  `is_webp_converted` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `reference_id` bigint NOT NULL,
  `reference_type` enum('CONTEST','TEAM','TRACK') NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) NULL,
  `student_id` varchar(255) NULL,
  `social_type` varchar(20) NULL,
  `social_id` varchar(255) NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_email` (`email`),
  UNIQUE KEY `uk_member_student_id` (`student_id`),
    CONSTRAINT `ck_member_login_type` CHECK (
        (social_type IS NULL AND social_id IS NULL AND student_id IS NOT NULL AND password IS NOT NULL)
        OR
        (social_type IS NOT NULL AND social_id IS NOT NULL AND student_id IS NULL AND password IS NULL)
    )
);

CREATE TABLE `member_roles` (
  `member_id` bigint NOT NULL,
  `role` enum('ROLE_관리자','ROLE_회원') NOT NULL,
  PRIMARY KEY (`member_id`,`role`)
);

CREATE TABLE `notice` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `contest_id` bigint DEFAULT NULL,
  `description` varchar(255) NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `title` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `team` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `contest_id` bigint NOT NULL,
  `github_path` varchar(255) DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `is_submitted` bit(1) NOT NULL,
  `item_order` int NOT NULL,
  `overview` varchar(3000) DEFAULT NULL,
  `production_path` varchar(255) DEFAULT NULL,
  `professor_name` varchar(255) DEFAULT NULL,
  `project_name` varchar(255) DEFAULT NULL,
  `team_name` varchar(255) DEFAULT NULL,
  `track_id` bigint DEFAULT NULL,
  `you_tube_path` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `team_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `description` varchar(255) NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `member_id` bigint NOT NULL,
  `team_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `team_contest_award` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `contest_award_id` bigint NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `team_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `team_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `is_liked` bit(1) NOT NULL,
  `member_id` bigint NOT NULL,
  `team_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_team_like_member_team` (`member_id`, `team_id`)
);

CREATE TABLE `team_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `member_id` bigint NOT NULL,
  `team_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `team_member_roles` (
  `team_member_id` bigint NOT NULL,
  `role` enum('ROLE_팀원','ROLE_팀장') NOT NULL,
  PRIMARY KEY (`team_member_id`,`role`)
);

CREATE TABLE `team_vote` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `created_at` datetime(6) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    `is_voted` bit(1) NOT NULL,
    `member_id` bigint NOT NULL,
    `team_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_team_vote_member_team` (`member_id`, `team_id`)
);
