package com.scatterrr.distributednode.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChunkMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String chunkId;
    private String fileName;
    private String merkleRootHash;
    private String nextNode;
    private String prevHash;
    private String storedPath;

    public String toJSONString() {
        return "{"
                + "\"chunkId\":\"" + chunkId + "\","
                + "\"fileName\":\"" + fileName + "\","
                + "\"merkleRootHash\":\"" + merkleRootHash + "\","
                + "\"nextNode\":\"" + nextNode + "\","
                + "\"prevHash\":\"" + prevHash + "\","
                + "\"storedPath\":\"" + storedPath + "\""
                + "}";
    }
}
