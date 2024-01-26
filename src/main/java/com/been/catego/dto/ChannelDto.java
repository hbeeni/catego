package com.been.catego.dto;

import com.been.catego.domain.Channel;

public record ChannelDto(
        String id,
        String name
) {
    public Channel toEntity() {
        return Channel.builder()
                .id(id)
                .name(name)
                .build();
    }
}
