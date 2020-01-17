package org.arxing.core;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public enum SupportFileType {
    json("json"),
    yaml("yaml"),
    xml("xml"),
    properties("properties"),;

    public static SupportFileType parse(String extension) {
        if (extension == null)
            return null;
        return Stream.of(SupportFileExtension.values())
                     .filter(o -> o.getExtension().equalsIgnoreCase(extension))
                     .map(SupportFileExtension::getContentType)
                     .findFirst()
                     .orElse(null);
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
            return deserialize(node);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            String s = e.getMessage();
            s += Stream.of(e.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n"));
            return s;
        }
    }

    private ObjectMapper getMapper() {
        ObjectMapper mapper;
        switch (this) {
            case json:
                mapper = new ObjectMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                break;
            case yaml:
                mapper = new YAMLMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                break;
            case xml:
                mapper = new XmlMapper();
                break;
            case properties:
                mapper = new JavaPropsMapper();
                mapper.enable(SerializationFeature.INDENT_OUTPUT);
                break;
            default:
                throw new IllegalStateException();
        }
        return mapper;
    }

    public JsonNode serialize(String content) throws IOException {
        ObjectMapper mapper = getMapper();
        return mapper.readTree(content);
    }

    public String deserialize(JsonNode node) throws JsonProcessingException {
        ObjectMapper mapper = getMapper();
        String output = mapper.writeValueAsString(node);
        switch (this) {
            case json:
                break;
            case yaml:
                break;
            case xml:
                output = prettyXml(output);
                break;
            case properties:
                break;
        }
        return changeLineSeparators(output);
    }

    private String changeLineSeparators(String s) {
        return s.replaceAll("\r\n", "\n").replaceAll("\r", "\n");
    }

    private String prettyXml(String xml) {
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(new StringReader(xml)));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", Integer.toString(2));
            StringWriter stringWriter = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
            String pretty = stringWriter.toString();
            stringWriter.close();
            return pretty;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
