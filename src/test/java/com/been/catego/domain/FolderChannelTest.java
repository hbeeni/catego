package com.been.catego.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class FolderChannelTest {

    @DisplayName("폴더 ID와 채널 ID가 같으면 ID가 달라도 동일한 인스턴스이다.")
    @Test
    void folderchannel_is_same() {
        FolderChannel folderChannel1 = makeFolderChannel(1L, 1L, "channel1");
        FolderChannel folderChannel2 = makeFolderChannel(2L, 1L, "channel1");

        assertEquals(folderChannel1, folderChannel2);
    }

    @DisplayName("폴더 ID가 다르면 다른 인스턴스이다.")
    @Test
    void when_folder_id_is_different_then_folderchannel_is_not_same() {
        FolderChannel folderChannel1 = FolderChannel.builder()
                .folder(makeFolder(1L))
                .channel(makeChannel("channel1"))
                .build();

        FolderChannel folderChannel2 = FolderChannel.builder()
                .folder(makeFolder(2L))
                .channel(makeChannel("channel1"))
                .build();

        assertNotEquals(folderChannel1, folderChannel2);
    }

    @DisplayName("채널 ID가 다르면 다른 인스턴스이다.")
    @Test
    void when_channel_id_is_different_then_folderchannel_is_not_same() {
        FolderChannel folderChannel1 = FolderChannel.builder()
                .folder(makeFolder(1L))
                .channel(makeChannel("channel1"))
                .build();

        FolderChannel folderChannel2 = FolderChannel.builder()
                .folder(makeFolder(1L))
                .channel(makeChannel("channel2"))
                .build();

        assertNotEquals(folderChannel1, folderChannel2);
    }

    private static FolderChannel makeFolderChannel(long id, long folderId, String channelId) {
        FolderChannel folderChannel = FolderChannel.builder()
                .folder(makeFolder(folderId))
                .channel(makeChannel(channelId))
                .build();
        ReflectionTestUtils.setField(folderChannel, "id", id);
        return folderChannel;
    }

    private static Folder makeFolder(long id) {
        Folder folder = Folder.builder().build();
        ReflectionTestUtils.setField(folder, "id", id);
        return folder;
    }

    private static Channel makeChannel(String id) {
        Channel channel = Channel.builder().build();
        ReflectionTestUtils.setField(channel, "id", id);
        return channel;
    }
}
