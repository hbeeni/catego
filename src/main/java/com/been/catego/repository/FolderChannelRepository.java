package com.been.catego.repository;

import com.been.catego.domain.FolderChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FolderChannelRepository extends JpaRepository<FolderChannel, Long> {

    @Query("select fc from FolderChannel fc "
            + "join fetch fc.folder f "
            + "join fetch fc.channel c "
            + "where f.id in :folderIds "
            + "order by c.name")
    List<FolderChannel> findAllByFolderIds(@Param("folderIds") List<Long> folderIds);
}
