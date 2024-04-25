package com.scatterrr.distributednode.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/node")
public class NodeController {

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
    public String uploadFile(@RequestPart("chunk") byte[] chunk,
                             @RequestPart("chunkId") String chunkId) throws Exception{
        // TODO: Save the chunk and save the metadata in db
        return "Chunk received successfully";
    }

}
