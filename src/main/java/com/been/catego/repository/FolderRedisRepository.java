package com.been.catego.repository;

import com.been.catego.dto.response.FolderInfoWithChannelInfoResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
public class FolderRedisRepository {

    private static final Duration FOLDER_CACHE_TTL = Duration.ofDays(7);

    @Resource(name = "folderRedisTemplate")
    private ValueOperations<String, String> valueOperations;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void saveFolders(Long userId, List<FolderInfoWithChannelInfoResponse> folders) {
        if (folders == null || folders.isEmpty()) {
            log.error("[saveFolders] folders must not be null or empty");
            return;
        }

        try {
            valueOperations.set(getKey(userId), serializeFolderList(folders), FOLDER_CACHE_TTL);
            log.info("[saveFolders] success. userId = {}", userId);
        } catch (Exception e) {
            log.error("[saveFolders] error. userId = {}, {}", userId, e.getMessage());
        }
    }

    public List<FolderInfoWithChannelInfoResponse> getFolders(Long userId) {
        try {
            String s = valueOperations.get(getKey(userId));
            List<FolderInfoWithChannelInfoResponse> list = deserializeFolderList(s);
            log.info("[getFolders] success. userId = {}", userId);
            return list;
        } catch (Exception e) {
            log.error("[getFolders] error. userId = {}, {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public void deleteFolders(Long userId) {
        valueOperations.getAndDelete(getKey(userId));
        log.info("[deleteFolders] success. userId = {}", userId);
    }

    private String serializeFolderList(List<FolderInfoWithChannelInfoResponse> folders) throws JsonProcessingException {
        return objectMapper.writeValueAsString(folders);
    }

    private List<FolderInfoWithChannelInfoResponse> deserializeFolderList(String foldersStr) throws
            JsonProcessingException {
        return objectMapper.readValue(foldersStr, new TypeReference<>() {
        });
    }

    private String getKey(Long userId) {
        return "FOLDER:USER:" + userId;
    }
}
