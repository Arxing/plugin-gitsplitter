package org.arxing.model;

import com.intellij.openapi.vfs.VirtualFile;

import org.arxing.core.SupportFileManager;
import org.arxing.core.SupportFileType;

public class VirtualFileEx {
    private VirtualFile file;
    private SupportFileType type;

    private VirtualFileEx(VirtualFile file, SupportFileType type) {
        this.type = type;
        this.file = file;
    }

    public static VirtualFileEx of(VirtualFile file, SupportFileType fileType) {
        return new VirtualFileEx(file, fileType);
    }

    public static VirtualFileEx of(VirtualFile file) {
        return new VirtualFileEx(file, SupportFileManager.parseContentType(file));
    }

    public String getPath() {
        return file.getPath();
    }

    public SupportFileType getType() {
        return type;
    }

    public String getTypeName() {
        return type.getTypeName();
    }

    public void setType(SupportFileType type) {
        this.type = type;
    }

    public VirtualFile getFile() {
        return file;
    }

    public void setFile(VirtualFile file) {
        this.file = file;
    }
}
