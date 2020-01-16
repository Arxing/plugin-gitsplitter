package org.arxing.core;

import com.annimon.stream.Stream;
import com.intellij.openapi.vfs.VirtualFile;

public class SupportFileManager {

    private SupportFileManager() {
    }

    public static boolean isFileSupport(VirtualFile file) {
        return isExtensionSupport(file.getExtension());
    }

    public static boolean isExtensionSupport(String extension) {
        return findSupportExtension(extension) != null;
    }

    public static SupportFileExtension findSupportExtension(String extension) {
        return Stream.of(SupportFileExtension.values()).filter(o -> o.getExtension().equalsIgnoreCase(extension)).findFirst().orElse(null);
    }

    public static SupportFileExtension findSupportExtension(VirtualFile file) {
        return findSupportExtension(file.getExtension());
    }

    public static SupportFileType parseContentType(String extension) {
        return findSupportExtension(extension).getContentType();
    }

    public static SupportFileType parseContentType(VirtualFile file) {
        return parseContentType(file.getExtension());
    }
}
