package org.arxing.manager;

public enum SupportFileExtension {
    json("json", SupportFileType.json),
    arb("arb", SupportFileType.json),
    yaml("yaml", SupportFileType.yaml),
    properties("properties", SupportFileType.json),
    xml("xml", SupportFileType.xml);

    private SupportFileType targetType;
    private String extension;

    SupportFileExtension(String extension, SupportFileType type) {
        this.targetType = type;
        this.extension = extension;
    }

    public SupportFileType getContentType() {
        return targetType;
    }

    public String getExtension() {
        return extension;
    }
}
