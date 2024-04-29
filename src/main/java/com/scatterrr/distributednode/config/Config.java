package com.scatterrr.distributednode.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    public static final String CHUNK_DIRECTORY = System.getProperty("user.dir") + "/ChunkData/";
    public static final String ROOT = "src/main/resources/"; // TODO: TBD
}
