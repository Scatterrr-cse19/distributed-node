package com.scatterrr.distributednode.controller;

import com.scatterrr.distributednode.repository.NodeRepository;
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
import com.scatterrr.distributednode.model.ChunkMetadata;
import com.scatterrr.distributednode.dto.RetrieveResponse;
import com.scatterrr.distributednode.dto.UploadResponse;

import java.io.FileInputStream;
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
    private final NodeRepository nodeRepository;

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
                chunkId {}\s
                merkleRootHash {}\s
                nextNode {}\s
                prevHash {}""", fileName, chunkId, merkleRootHash, nextNode, prevHash);
        
        // Save the chunk in fileSystem
        if (saveChunkToFileSystem(chunk, chunkId, fileName)) {
            // Save metadata of the chunk
            String filePath = CHUNK_DIRECTORY + chunkId + '_' + fileName;
            ChunkMetadata metadataRecord = ChunkMetadata.builder()
                    .chunkId(chunkId)
                    .fileName(fileName)
                    .merkleRootHash(merkleRootHash)
                    .nextNode(nextNode)
                    .prevHash(prevHash)
                    .storedPath(filePath)
                    .build();

            // Save metadataRecord in the database
            nodeRepository.save(metadataRecord);

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

    // Retrieve chunk from the file system given the path
    private byte[] retrieveChunkFromFileSystem(String path) {
        try {
            FileInputStream fis = new FileInputStream(path);
            byte[] chunk = fis.readAllBytes();
            fis.close();
            return chunk;
        } catch (IOException e) {
            log.error("Failed to retrieve chunk from file system: {}", e.getMessage());
            return null;
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

        log.info("Received retrieve request for chunkId: {} and fileName: {}", chunkId, fileName);
        // search for the entry with the given chunkId and fileName
        ChunkMetadata metadata = nodeRepository.findByFileNameAndChunkId(fileName, chunkId);
        if (metadata == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        log.info("Metadata record found: {}", metadata.toString());

        // get the chunk from fileSystem
        byte[] chunk = retrieveChunkFromFileSystem(metadata.getStoredPath() + chunkId + '_' + fileName);
        System.out.println(metadata.getStoredPath()); // REMOVE THIS - just added to check the path

        return ResponseEntity.ok(new RetrieveResponse(
                HttpStatus.OK.value(),
                metadata.getNextNode(),
                metadata.getPrevHash(),
                chunk));
    }
}
