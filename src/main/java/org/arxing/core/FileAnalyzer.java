package org.arxing.core;

import com.fasterxml.jackson.databind.JsonNode;

import org.arxing.util.MessagesUtil;

import java.io.IOException;

public class FileAnalyzer {
    public final SupportFileType fileType;
    public final String content;
    public final String filePath;

    public FileAnalyzer(SupportFileType fileType, String content, String filePath) {
        this.fileType = fileType;
        this.content = content;
        this.filePath = filePath;
    }

    public JsonNode analyze() {
        try {
            return fileType.serialize(content);
        } catch (IOException e) {
            MessagesUtil.println("Analyze failed: %s\n\tfilepath=%s", e.getMessage(), filePath);
        }
        return null;
    }
}
