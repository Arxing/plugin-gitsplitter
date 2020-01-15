package org.arxing.manager;

import com.annimon.stream.Stream;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsFactory;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.util.List;

public enum SupportFileType {
    json("json"),
    yaml("yaml"),
    xml("xml"),
    properties("properties"),;

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

    public String getInitContent(JsonNode node) {
        try {
            return getMapper().writeValueAsString(node);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getInitContent() {
        switch (this) {
            case json:
                return "{}";
            case yaml:
                return "";
            case xml:
                return "<root></root>";
            case properties:
                return "";
        }
        throw new IllegalStateException();
    }

    public JsonFactory getFactory() {
        switch (this) {
            case json:
                return new JsonFactory();
            case yaml:
                return new YAMLFactory();
            case xml:
                return new XmlFactory();
            case properties:
                return new JavaPropsFactory();
        }
        throw new IllegalStateException();
    }

    public ObjectMapper getMapper() {
        ObjectMapper mapper;
        switch (this) {
            case json:
                mapper = new ObjectMapper();
                break;
            case yaml:
                mapper = new YAMLMapper();
                break;
            case xml:
                mapper = new XmlMapper();
                break;
            case properties:
                mapper = new JavaPropsMapper();
                break;
            default:
                throw new IllegalStateException();
        }
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }
}
