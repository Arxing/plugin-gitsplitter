package org.arxing.manager;

import com.annimon.stream.Stream;

import java.util.List;

public enum SupportFileType {
    json("json"),
    yaml("yaml"),
    xml("xml");

    public static SupportFileType parse(String typeName) {
        return Stream.of(values()).filter(o -> o.typeName.equalsIgnoreCase(typeName)).findFirst().orElse(null);
    }

    private String typeName;

    SupportFileType(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public static List<String> getOptionsList() {
        return Stream.of(values()).map(SupportFileType::getTypeName).toList();
    }

    public static String[] getOptionsArray() {
        return getOptionsList().toArray(new String[0]);
    }

    public String getInitContent() {
        switch (this) {
            case json:
                return "{}";
            case yaml:
                return "";
            case xml:
                return "<root></root>";
        }
        throw new IllegalStateException();
    }
}
