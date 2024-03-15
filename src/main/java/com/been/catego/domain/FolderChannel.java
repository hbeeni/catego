package com.been.catego.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class FolderChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    private Folder folder;

    @ManyToOne(fetch = FetchType.LAZY)
    private Channel channel;

    @Builder
    private FolderChannel(Folder folder, Channel channel) {
        this.folder = folder;
        this.channel = channel;
    }

    private boolean isSameFolderAndChannel(Folder folder, Channel channel) {
        if (this.folder == null || this.channel == null || folder == null || channel == null) {
            return false;
        }
        return Objects.equals(this.folder.getId(), folder.getId()) &&
                Objects.equals(this.channel.getId(), channel.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FolderChannel that)) {
            return false;
        }
        return (this.getId() != null && Objects.equals(this.getId(), that.getId())) ||
                isSameFolderAndChannel(that.folder, that.channel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId());
    }
}
