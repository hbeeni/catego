package com.been.catego.repository;

import com.been.catego.domain.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FolderRepository extends JpaRepository<Folder, Long> {

    List<Folder> findByUser_Id(Long userId);

    @Query("select f From Folder f " +
            "join fetch f.folderChannels fc " +
            "where f.id = :folderId and f.user.id = :userId")
    Optional<Folder> findFolderByIdAndUserId(@Param("folderId") Long folderId, @Param("userId") Long userId);
}
