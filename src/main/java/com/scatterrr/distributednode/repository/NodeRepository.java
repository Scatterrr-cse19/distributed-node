package com.scatterrr.distributednode.repository;

import com.scatterrr.distributednode.model.ChunkMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NodeRepository extends JpaRepository<ChunkMetadata, String> {
    ChunkMetadata findByFileNameAndChunkId(String fileName, String chunkId);

    void deleteByFileName(String fileName);
}
