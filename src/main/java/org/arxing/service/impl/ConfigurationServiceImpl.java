package org.arxing.service.impl;

import com.annimon.stream.Stream;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xml.DomElementsNavigationManager;

import org.arxing.axutils_java.JParser;
import org.arxing.axutils_java.StringUtils;
import org.arxing.core.FileAnalyzer;
import org.arxing.core.GitsplitterInspection;
import org.arxing.core.SupportFileManager;
import org.arxing.core.SupportFileType;
import org.arxing.model.ConfigurationData;
import org.arxing.model.VirtualFileEx;
import org.arxing.service.ConfigurationService;
import org.arxing.util.FileHelper;
import org.arxing.util.MessagesUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationServiceImpl implements ConfigurationService {
    public final static String SPLIT_FILE_SUFFIX = "gs";
    private Project project;
    private ConfigurationData configurationData;
    private VirtualFile configurationFile;

    public ConfigurationServiceImpl(Project project) {
        this.project = project;
        configurationFile = FileHelper.findOrCreateConfigurationFile(project);
        String configurationContent = FileHelper.readSync(project, configurationFile);
        configurationData = getConfigurationData(configurationContent);
    }

    private int retries = 0;

    private ConfigurationData getConfigurationData(String content) {
        if (configurationData != null)
            return configurationData;
        configurationData = JParser.fromJsonOrNull(content, ConfigurationData.class);
        if (configurationData != null) {
            return configurationData;
        } else {
            if (++retries >= 5) {
                throw new IllegalStateException("Can not init configuration data, please reload project.");
            }
            try {
                Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return getConfigurationData(content);
        }
    }

    private boolean isRelPathExist(String relPath) {
        return relPath != null && FileHelper.exists(computeFullPath(relPath));
    }

    @Override public String computeRelPath(String path) {
        return FileHelper.computeRelPath(project, path);
    }

    @Override public String computeFullPath(String path) {
        return FileHelper.computeFullPath(project, path);
    }

    @Override public void syncConfiguration() {
        String content = JParser.toPrettyJson(configurationData);
        FileHelper.writeSync(project, configurationFile, content);
    }

    @Override public List<ConfigurationData.TraceTargetNode> getAllTraceTarget() {
        return configurationData.getTrace();
    }

    @Override public ConfigurationData.TraceTargetNode findTraceTarget(String targetPath) {
        return Stream.of(getAllTraceTarget()).filter(o -> o.getPath().equalsIgnoreCase(targetPath)).findFirst().orElse(null);
    }

    @Override public ConfigurationData.TraceTargetNode findParentTargetOfChild(String childPath) {
        for (ConfigurationData.TraceTargetNode targetNode : configurationData.getTrace()) {
            for (ConfigurationData.TraceChildNode childNode : targetNode.getChildren()) {
                if (childNode.getPath().equalsIgnoreCase(childPath))
                    return targetNode;
            }
        }
        return null;
    }

    @Override public ConfigurationData.TraceChildNode findFirstTraceChild(String childPath) {
        for (ConfigurationData.TraceTargetNode targetNode : configurationData.getTrace()) {
            for (ConfigurationData.TraceChildNode childNode : targetNode.getChildren()) {
                if (childNode.getPath().equalsIgnoreCase(childPath))
                    return childNode;
            }
        }
        return null;
    }

    @Override public ConfigurationData.TraceChildNode findTraceChildInTarget(String targetPath, String childPath) {
        for (ConfigurationData.TraceTargetNode targetNode : configurationData.getTrace()) {
            if (targetNode.getPath().equalsIgnoreCase(targetPath)) {
                for (ConfigurationData.TraceChildNode childNode : targetNode.getChildren()) {
                    if (childNode.getPath().equalsIgnoreCase(childPath))
                        return childNode;
                }
            }
        }
        return null;
    }

    @Override public void addTraceTarget(String targetPath, SupportFileType fileType) {
        if (!isInTrace(targetPath)) {
            configurationData.addNode(new ConfigurationData.TraceTargetNode(targetPath, fileType.getTypeName()));
            syncConfiguration();
        }
    }

    @Override public void removeTraceTarget(String targetPath) {
        if (isInTrace(targetPath)) {
            configurationData.getTrace().removeIf(o -> o.getPath().equalsIgnoreCase(targetPath));
            syncConfiguration();
        }
    }

    @Override public void addTraceChildInTarget(String targetPath, String childPath, SupportFileType childFileType) {
        if (isInTrace(targetPath) && !isChildOfTarget(targetPath, childPath)) {
            ConfigurationData.TraceChildNode childNode = new ConfigurationData.TraceChildNode(childPath, childFileType.getTypeName());
            findTraceTarget(targetPath).addChild(childNode);
            syncConfiguration();
        }
    }

    @Override public void removeTraceChildInTarget(String targetPath, String childPath) {
        if (isInTrace(targetPath) && isChildOfTarget(targetPath, childPath)) {
            findTraceTarget(targetPath).getChildren().removeIf(o -> o.getPath().equalsIgnoreCase(childPath));
            syncConfiguration();
        }
    }

    @Override public void moveTraceTarget(String targetPath, String newPath) {
        if (isInTrace(targetPath)) {
            ConfigurationData.TraceTargetNode targetNode = findTraceTarget(targetPath);
            targetNode.setPath(newPath);
            syncConfiguration();
        }
    }

    @Override public void moveTraceChildInTarget(String targetPath, String childPath, String newChildPath) {
        if (isInTrace(targetPath) && isChildOfTarget(targetPath, childPath)) {
            ConfigurationData.TraceChildNode childNode = findTraceChildInTarget(targetPath, childPath);
            childNode.setPath(newChildPath);
            syncConfiguration();
        }
    }

    @Override public boolean isInTrace(String targetPath) {
        return Stream.of(getAllTraceTarget()).anyMatch(o -> o.getPath().equalsIgnoreCase(targetPath));
    }

    @Override public boolean isChildOfTarget(String targetPath, String childPath) {
        if (!isInTrace(targetPath))
            return false;
        return Stream.of(findTraceTarget(targetPath).getChildren()).anyMatch(o -> o.getPath().equalsIgnoreCase(childPath));
    }

    @Override public boolean isChildOfAnyTarget(String childPath) {
        for (ConfigurationData.TraceTargetNode targetNode : configurationData.getTrace()) {
            for (ConfigurationData.TraceChildNode childNode : targetNode.getChildren()) {
                if (childNode.getPath().equalsIgnoreCase(childPath))
                    return true;
            }
        }
        return false;
    }

    @Override public boolean isInTraceOrChildOfAnyTarget(String path) {
        for (ConfigurationData.TraceTargetNode targetNode : configurationData.getTrace()) {
            if (targetNode.getPath().equalsIgnoreCase(path))
                return true;
            for (ConfigurationData.TraceChildNode childNode : targetNode.getChildren()) {
                if (childNode.getPath().equalsIgnoreCase(path))
                    return true;
            }
        }
        return false;
    }

    @Override public List<FileAnalyzer> analyzeTraceTarget(String targetPath) {
        List<FileAnalyzer> analyzers = new ArrayList<>();
        if (isInTrace(targetPath)) {
            ConfigurationData.TraceTargetNode targetNode = findTraceTarget(targetPath);
            for (ConfigurationData.TraceChildNode childNode : targetNode.getChildren()) {
                if (!isChildOfTarget(targetPath, childNode.getPath()))
                    continue;
                String fullChildPath = FileHelper.computeFullPath(project, childNode.getPath());
                VirtualFile childFile = FileHelper.findVirtualFile(fullChildPath);
                if (childFile == null)
                    continue;
                SupportFileType childFileType = SupportFileType.parse(childNode.getType());

                String childContent = FileHelper.readSync(project, childFile);
                if (StringUtils.isEmpty(childContent))
                    continue;
                FileAnalyzer analyzer = new FileAnalyzer(childFileType, childContent, childNode.getPath());
                analyzers.add(analyzer);
            }
        }
        return analyzers;
    }

    @Override public void mergeAll() {
        for (ConfigurationData.TraceTargetNode targetNode : getAllTraceTarget()) {
            String targetPath = targetNode.getPath();
            merge(targetPath);
        }
    }

    @Override public ObjectNode merge(String targetPath) {
        if (!isInTrace(targetPath))
            return null;
        ConfigurationData.TraceTargetNode targetNode = findTraceTarget(targetPath);
        if (targetNode.getChildren().isEmpty()) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode mergeNode = objectMapper.createObjectNode();

        for (Map.Entry<String, JsonNode> entry : getAllMembersOfTraceTarget(targetPath)) {
            String key = entry.getKey();
            JsonNode wrapNode;
            JsonNode oldNode = mergeNode.get(key);
            JsonNode newNode = entry.getValue();

            if (mergeNode.hasNonNull(key)) {
                // key was defined
                if (!GitsplitterInspection.equalsNode(oldNode, newNode)) {
                    // Ignore element if they has same key but different value
                    continue;
                }
                if (oldNode.isObject() && newNode.isObject()) {
                    // merge object
                    wrapNode = objectMapper.createObjectNode();
                    Stream.concat(oldNode.fields(), newNode.fields()).forEach(allFieldEntry -> {
                        String wrapKey = allFieldEntry.getKey();
                        JsonNode wrapValue = allFieldEntry.getValue();
                        ((ObjectNode) wrapNode).set(wrapKey, wrapValue);
                    });
                } else if (oldNode.isArray() && newNode.isArray()) {
                    // merge array
                    wrapNode = objectMapper.createArrayNode();
                    Stream.concat(oldNode.elements(), newNode.elements()).forEach(((ArrayNode) wrapNode)::add);
                } else {
                    wrapNode = newNode.deepCopy();
                }
            } else {
                // key is not defined
                wrapNode = newNode.deepCopy();
            }
            mergeNode.set(key, wrapNode);
        }
        outputNode(targetPath, mergeNode);
        return mergeNode;
    }

    @Override public void reset() {
        configurationData.getTrace().clear();
        syncConfiguration();
    }

    @Override public void repair() {
        configurationData.getTrace().removeIf(node -> !isRelPathExist(node.getPath()));
        for (ConfigurationData.TraceTargetNode targetNode : configurationData.getTrace()) {
            targetNode.getChildren().removeIf(child -> !isRelPathExist(child.getPath()));
        }
        syncConfiguration();
    }

    @Override public void outputNode(String targetPath, ObjectNode node) {
        ConfigurationData.TraceTargetNode targetNode = findTraceTarget(targetPath);
        SupportFileType fileType = SupportFileType.parse(targetNode.getType());
        try {
            String content = fileType.deserialize(node);
            String fullPath = FileHelper.computeFullPath(project, targetNode.getPath());
            Document document = FileHelper.findDocument(project, fullPath);
            FileHelper.writeSync(document, content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public void runMergeAction(VirtualFileEx merged, List<VirtualFileEx> children, boolean needToCreateChildren) {
        List<VirtualFileEx> copiesChildren = new ArrayList<>();

        if (needToCreateChildren) {
            children.forEach(fileEx -> {
                VirtualFile file = fileEx.getFile();
                String copyFilename = String.format("%s.%s.%s", file.getNameWithoutExtension(), SPLIT_FILE_SUFFIX, file.getExtension());

                FileHelper.runWriteActionSync(() -> {
                    VirtualFile copyFile = file.copy(null, file.getParent(), copyFilename);
                    SupportFileType fileType = SupportFileManager.parseContentType(file);
                    copiesChildren.add(VirtualFileEx.of(copyFile, fileType));
                });
            });
        } else {
            copiesChildren.addAll(children);
        }

        ConfigurationData.TraceTargetNode target;
        String mergedPath = FileHelper.computeRelPath(project, merged.getFile().getPath());
        if (isInTrace(mergedPath)) {
            target = findTraceTarget(mergedPath);
        } else {
            target = new ConfigurationData.TraceTargetNode(mergedPath, merged.getTypeName());
            addTraceTarget(mergedPath, merged.getType());
        }
        String targetPath = target.getPath();
        Stream.of(copiesChildren)
              .filterNot(o -> isChildOfTarget(targetPath, FileHelper.computeRelPath(project, o.getPath())))
              .forEach(o -> addTraceChildInTarget(targetPath, FileHelper.computeRelPath(project, o.getPath()), o.getType()));
    }

    @Override public void runSplitAction(VirtualFile targetFile,
                                         String tag,
                                         SupportFileType targetFileType,
                                         SupportFileType splitFileType) {
        String childFilename = String.format("%s_%s.%s.%s",
                                             targetFile.getNameWithoutExtension(),
                                             tag,
                                             SPLIT_FILE_SUFFIX,
                                             splitFileType.getTypeName());

        try {
            String targetContent = FileHelper.readSync(project, targetFile);
            JsonNode node = targetFileType.serialize(targetContent);
            FileHelper.runWriteActionSync(() -> {
                VirtualFile childFile = targetFile.getParent().createChildData(null, childFilename);
                String initContent = splitFileType.getInitContent(node);
                FileHelper.writeSync(project, childFile, initContent);
                runMergeAction(VirtualFileEx.of(targetFile), Collections.singletonList(VirtualFileEx.of(childFile, splitFileType)), false);
                FileEditorManager.getInstance(project).openFile(childFile, true);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override public Map<String, List<Map.Entry<String, JsonNode>>> getGroupMembersOfTraceTarget(String targetPath) {
        Map<String, List<Map.Entry<String, JsonNode>>> result = new HashMap<>();
        List<FileAnalyzer> analyzers = analyzeTraceTarget(targetPath);
        for (FileAnalyzer analyzer : analyzers) {
            String childPath = analyzer.filePath;
            JsonNode node = analyzer.analyze();
            if (node != null) {
                if (!node.isObject()) {
                    continue;
                }
                ObjectNode objectNode = (ObjectNode) node;
                List<Map.Entry<String, JsonNode>> fields = Stream.of(objectNode.fields()).toList();
                result.put(childPath, fields);
            }
        }
        return result;
    }

    private List<Map.Entry<String, JsonNode>> getAllMembersOfTraceTarget(String targetPath) {
        return Stream.of(getGroupMembersOfTraceTarget(targetPath)).flatMap(entry -> Stream.of(entry.getValue())).toList();
    }
}
