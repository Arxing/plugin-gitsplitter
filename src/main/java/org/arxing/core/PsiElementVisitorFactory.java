package org.arxing.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.json.psi.JsonElementVisitor;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.json.psi.JsonValue;
import com.intellij.lang.properties.psi.impl.PropertyImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.XmlElementVisitor;
import com.intellij.psi.xml.XmlTag;

import org.arxing.util.MessagesUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;
import org.jetbrains.yaml.psi.YAMLValue;
import org.jetbrains.yaml.psi.YamlPsiElementVisitor;
import org.jetbrains.yaml.psi.YamlRecursivePsiElementVisitor;

import java.io.IOException;

@SuppressWarnings("all")
public class PsiElementVisitorFactory {
    private PsiElementScanner handler;

    public PsiElementVisitorFactory(PsiElementScanner handler) {
        this.handler = handler;
    }

    public PsiElementVisitor json() {
        return new JsonElementVisitor() {
            @Override public void visitProperty(@NotNull JsonProperty property) {
                String key = property.getName();
                JsonValue jsonValue = property.getValue();
                String val = jsonValue.getText();

                try {
                    JsonNode node = SupportFileType.json.serialize(val);
                    handler.onScan(key, node, property);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public PsiElementVisitor xml() {
        return new XmlElementVisitor() {
            @Override public void visitXmlTag(XmlTag tag) {
                String key = tag.getName();
                String val = tag.getValue().getText();

                try {
                    JsonNode node = SupportFileType.xml.serialize(val);
                    handler.onScan(key, node, tag);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public PsiElementVisitor yaml() {
        return new YamlPsiElementVisitor() {
            @Override public void visitKeyValue(@NotNull YAMLKeyValue keyValue) {
                PsiElement grandfather = keyValue.getParent().getParent();
                if (!(grandfather instanceof YAMLDocument))
                    return;
                String key = keyValue.getKeyText();
                // reformat for yaml
                String val = "  " + keyValue.getValue().getText();

                try {
                    JsonNode node = SupportFileType.yaml.serialize(val);
                    handler.onScan(key, node, keyValue);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public PsiElementVisitor properties() {
        return new PsiElementVisitor() {
            @Override public void visitElement(PsiElement element) {
                if (!(element instanceof PropertyImpl))
                    return;
                PropertyImpl property = (PropertyImpl) element;
                String key = property.getKey();
                String val = property.getValue();

                try {
                    JsonNode node = SupportFileType.properties.serialize(val);
                    handler.onScan(key, node, property);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public interface PsiElementScanner {
        void onScan(String key, JsonNode value, PsiElement element);
    }
}
