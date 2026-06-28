USE opus;

DROP TABLE IF EXISTS `contest_submission_feedback`;
DROP TABLE IF EXISTS `contest_submission`;
DROP TABLE IF EXISTS `contest_submission_item_file_formats`;
DROP TABLE IF EXISTS `contest_submission_item`;
DROP TABLE IF EXISTS `contest_member_team_ids`;
DROP TABLE IF EXISTS `contest_member`;
DROP TABLE IF EXISTS `team_member_roles`;
DROP TABLE IF EXISTS `team_vote`;
DROP TABLE IF EXISTS `team_like`;
DROP TABLE IF EXISTS `team_comment`;
DROP TABLE IF EXISTS `team_contest_award`;
DROP TABLE IF EXISTS `team_member`;
DROP TABLE IF EXISTS `member_roles`;
DROP TABLE IF EXISTS `staff_info`;
DROP TABLE IF EXISTS `team`;
DROP TABLE IF EXISTS `contest_sort`;
DROP TABLE IF EXISTS `contest_track`;
DROP TABLE IF EXISTS `contest_template`;
DROP TABLE IF EXISTS `contest_award`;
DROP TABLE IF EXISTS `file_document`;
DROP TABLE IF EXISTS `file_image`;
DROP TABLE IF EXISTS `file_feedback`;
DROP TABLE IF EXISTS `file`;
DROP TABLE IF EXISTS `notice`;
DROP TABLE IF EXISTS `notification`;
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

CREATE TABLE `contest_submission_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `allow_late_submission` bit(1) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `end_at` datetime(6) NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `max_file_count` int NOT NULL,
  `max_file_size_mb` int NOT NULL,
  `name` varchar(255) NOT NULL,
  `start_at` datetime(6) NOT NULL,
  `visibility` enum('PUBLIC','PRIVATE') NOT NULL,
  `contest_id` bigint NOT NULL,
  `contest_track_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `contest_submission_item_file_formats` (
  `contest_submission_item_id` bigint NOT NULL,
  `file_format` enum('PDF','ZIP','PNG','JPG','JPEG','GIF','MP4','PPT','PPTX','DOC','DOCX','HWP') NOT NULL,
  PRIMARY KEY (`contest_submission_item_id`,`file_format`)
);

CREATE TABLE `contest_submission` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `first_submitted_at` datetime(6) NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `team_id` bigint NOT NULL,
  `contest_submission_item_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `contest_member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `is_deleted` bit(1) NOT NULL,
  `member_id` bigint NOT NULL,
  `contest_id` bigint NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `contest_member_team_ids` (
  `contest_member_id` bigint NOT NULL,
  `team_id` bigint NOT NULL,
  PRIMARY KEY (`contest_member_id`,`team_id`)
);

CREATE TABLE `contest_submission_feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `description` varchar(3000) NOT NULL,
  `member_id` bigint NOT NULL,
  `contest_submission_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_feedback_member_submission` (`member_id`,`contest_submission_id`)
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
  `file_size` bigint NOT NULL,
  `mime_type` varchar(100) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
);

CREATE TABLE `file_image` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `file_id` bigint NOT NULL,
  `image_type` enum('BANNER','PREVIEW','THUMBNAIL','POSTER','PROFILE') NOT NULL,
  `is_webp_converted` bit(1) NOT NULL,
  `reference_id` bigint NOT NULL,
  `reference_type` enum('CONTEST','TEAM','TRACK','MEMBER') NOT NULL,
  `single_image_key` varchar(255) GENERATED ALWAYS AS (
    CASE WHEN image_type != 'PREVIEW'
         THEN CONCAT(reference_id, '_', reference_type, '_', image_type)
         ELSE NULL END
  ) VIRTUAL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_image_file_id` (`file_id`),
  UNIQUE KEY `uk_file_image_single_per_ref` (`single_image_key`),
  KEY `idx_file_image_ref` (`reference_id`, `reference_type`, `image_type`),
  KEY `idx_file_image_zombie` (`is_webp_converted`, `created_at`),
  CONSTRAINT `fk_file_image_file` FOREIGN KEY (`file_id`) REFERENCES `file` (`id`) ON DELETE CASCADE
);

CREATE TABLE `file_feedback` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `file_id` bigint NOT NULL,
  `feedback_id` bigint NOT NULL,
  `file_order` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_feedback_file_id` (`file_id`),
  KEY `idx_file_feedback_feedback_id` (`feedback_id`),
  CONSTRAINT `fk_file_feedback_file` FOREIGN KEY (`file_id`) REFERENCES `file` (`id`) ON DELETE CASCADE
);

CREATE TABLE `file_document` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `file_id` bigint NOT NULL,
  `submission_id` bigint NOT NULL,
  `file_order` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_file_document_file_id` (`file_id`),
  CONSTRAINT `fk_file_document_file` FOREIGN KEY (`file_id`) REFERENCES `file` (`id`) ON DELETE CASCADE
);

CREATE TABLE `member` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  `is_fake` bit(1) NOT NULL DEFAULT 0,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) NULL,
  `student_id` varchar(255) NULL,
  `social_type` varchar(20) NULL,
  `social_id` varchar(255) NULL,
  `github_url` varchar(255),
  `is_profile_public` bit(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_email` (`email`),
  UNIQUE KEY `uk_member_student_id` (`student_id`),
    CONSTRAINT `ck_member_login_type` CHECK (
        is_fake = 1
        OR
        (social_type IS NULL AND social_id IS NULL AND student_id IS NOT NULL AND password IS NOT NULL)
        OR
        (social_type IS NOT NULL AND social_id IS NOT NULL AND password IS NULL)
    )
);

CREATE TABLE `member_roles` (
  `member_id` bigint NOT NULL,
  `role` enum('ROLE_학생','ROLE_관리자','ROLE_교수','ROLE_직원','ROLE_외부멘토') NOT NULL,
  PRIMARY KEY (`member_id`,`role`)
);

CREATE TABLE `staff_info` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `role` enum('ROLE_교수','ROLE_직원') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_staff_info_email_name` (`email`,`name`)
);

CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `member_id` bigint NOT NULL,
  `title` varchar(255) NOT NULL,
  `content` varchar(255) NOT NULL,
  `type` enum('TEAM','TEAM_COMMENT','TEAM_AWARDS') NOT NULL,
  `target_id` bigint NOT NULL,
  `redirect_url` varchar(255) DEFAULT NULL,
  `is_read` bit(1) NOT NULL,
  `is_deleted` bit(1) NOT NULL,
  PRIMARY KEY (`id`)
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
    `member_id` bigint NOT NULL,
    `team_id` bigint NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_team_vote_member_team` (`member_id`, `team_id`)
);
