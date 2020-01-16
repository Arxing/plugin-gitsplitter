package org.arxing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import org.arxing.axutils_java.FileUtils;
import org.arxing.util.MessagesUtil;

import java.io.IOException;

public class Main {

    static String jsonSource = "一個字串";
    static String xmlSource = "<ObjectNode><name>John</name></ObjectNode>";
    static String yamlSource = "  git: aaaaaaaa\n  po:\n    C: 123";

    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new YAMLMapper();

        System.out.println(yamlSource);
        JsonNode node = mapper.readTree(yamlSource);

    }
}
