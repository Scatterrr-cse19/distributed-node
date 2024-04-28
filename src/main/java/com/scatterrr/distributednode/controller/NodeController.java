package com.scatterrr.distributednode.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import static com.scatterrr.distributednode.config.Config.CHUNK_DIRECTORY;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/node")
public class NodeController {

    private static final Logger log = LoggerFactory.getLogger(NodeController.class);
    @Value("${eureka.client.serviceUrl.defaultZone}")
    private String eurekaServerUrl;

    @GetMapping("/server-url")
    @ResponseStatus(HttpStatus.OK)
    public String getEurekaServerUrl() {
        return StringUtils.replace(eurekaServerUrl,
                "/eureka", "");
    }

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UploadResponse> uploadFile(
            @RequestPart("chunk") byte[] chunk,
            @RequestPart("chunkId") String chunkId,
            @RequestPart("fileName") String fileName,
            @RequestPart("merkleRootHash") String merkleRootHash,
            @RequestPart("nextNode") String nextNode,
            @RequestPart("prevHash") String prevHash
    // more request body parts
    ) throws Exception {
        log.info("""
                Received upload request with the data\s
                filename {}\s
                merkleRootHash {}\s
                nextNode {}\s
                prevHash {}""", fileName, merkleRootHash, nextNode, prevHash);
        
        // Save the chunk in fileSystem
        if (saveChunkToFileSystem(chunk, chunkId, fileName)) {
            // Save metadata of the chunk
            String filePath = CHUNK_DIRECTORY + chunkId + '_' + fileName;
            ChunkMetadataRecord metadataRecord = new ChunkMetadataRecord(
                    chunkId, fileName, merkleRootHash, nextNode, prevHash, filePath
            );
            // TODO: Save metadataRecord in the database

            // Return success message with hash
            String metadataRecordHash = sha256(metadataRecord.toString());
            return ResponseEntity.ok(new UploadResponse(HttpStatus.OK.value(), metadataRecordHash));
        } else {
            // Return error message
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Save chunks in the file system
    private boolean saveChunkToFileSystem(byte[] chunk, String chunkId, String fileName) {
        try {
            // The file path to save the chunk
            String filePath = CHUNK_DIRECTORY + chunkId + '_' + fileName;
            FileOutputStream fos = new FileOutputStream(filePath);
            fos.write(chunk);
            fos.close();
            return true;
        } catch (IOException e) {
            log.error("Failed to save chunk to file system: {}", e.getMessage());
            return false;
        }
    }

    // Hash the metadata record
    private String sha256(String original) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(original.getBytes());
        byte[] digest = md.digest();
        return Hex.encodeHexString(digest);
    }

    @PostMapping("retrieve")
    public ResponseEntity<RetrieveResponse> retrieveFile(
            @RequestPart("chunkId") String chunkId,
            @RequestPart("fileName") String fileName) {
        // search for the chunkID in db, get the chunk from fileSystem
        // send Dummy response for now
        byte[] dummy = new byte[10];
        return ResponseEntity.ok(new RetrieveResponse(
                HttpStatus.OK.value(),
                "SAMPLE_NEXT_NODE",
                "SAMPLE_PREV_HASH",
                dummy));
    }
}

// Class to hold upload response data
@Data
@AllArgsConstructor
class UploadResponse {
    private final int statusCode;
    private final String message;
}

@Data
@AllArgsConstructor
class RetrieveResponse {
    private final int statusCode;
    private final String nextNode;
    private final String prevHash;
    private final byte[] chunk;
}

@Data
@AllArgsConstructor
class ChunkMetadataRecord {
    private final String chunkId;
    private final String fileName;
    private final String merkleRootHash;
    private final String nextNode;
    private final String prevHash;
    private final String storedPath;
}
