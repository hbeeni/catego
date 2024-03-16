CREATE TABLE users
(
    id          bigint NOT NULL PRIMARY KEY AUTO_INCREMENT,
    login_id    varchar(50)  NOT NULL UNIQUE,
    provider_id varchar(255) NOT NULL UNIQUE,
    nickname    varchar(20)  NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE folder (
    id      bigint NOT NULL PRIMARY KEY AUTO_INCREMENT,
    user_id bigint,
    name    varchar(100) NOT NULL COLLATE utf8mb4_unicode_ci,
    CONSTRAINT FK_folder_users
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE channels (
    id           varchar(255) NOT NULL PRIMARY KEY,
    name         varchar(100) NOT NULL COLLATE utf8mb4_unicode_ci,
    created_date datetime(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE folder_channel (
    id         bigint NOT NULL PRIMARY KEY AUTO_INCREMENT,
    folder_id  bigint,
    channel_id varchar(255),
    CONSTRAINT FK_folderchannels_channel
        FOREIGN KEY (channel_id) REFERENCES channels (id),
    CONSTRAINT FK_folderchannels_folder
        FOREIGN KEY (folder_id) REFERENCES folder (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
