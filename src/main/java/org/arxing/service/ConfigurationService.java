package org.arxing.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import org.arxing.core.FileAnalyzer;
import org.arxing.core.SupportFileType;
import org.arxing.model.ConfigurationData;
import org.arxing.model.VirtualFileEx;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface ConfigurationService {
    static ConfigurationService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, ConfigurationService.class);
    }

    String computeRelPath(String path);

    String computeFullPath(String path);

    void syncConfiguration();

    List<ConfigurationData.TraceTargetNode> getAllTraceTarget();

    ConfigurationData.TraceTargetNode findTraceTarget(String targetPath);

    ConfigurationData.TraceTargetNode findParentTargetOfChild(String childPath);

    ConfigurationData.TraceChildNode findFirstTraceChild(String childPath);

    ConfigurationData.TraceChildNode findTraceChildInTarget(String targetPath, String childPath);

    void addTraceTarget(String targetPath, SupportFileType fileType);

    void removeTraceTarget(String targetPath);

    void addTraceChildInTarget(String targetPath, String childPath, SupportFileType childFileType);

    void removeTraceChildInTarget(String targetPath, String childPath);

    void moveTraceTarget(String targetPath, String newPath);

    void moveTraceChildInTarget(String targetPath, String childPath, String newChildPath);

    boolean isInTrace(String targetPath);

    boolean isChildOfTarget(String targetPath, String childPath);

    boolean isChildOfAnyTarget(String childPath);

    boolean isInTraceOrChildOfAnyTarget(String path);

    List<FileAnalyzer> analyzeTraceTarget(String targetPath);

    void mergeAll();

    ObjectNode merge(String targetPath);

    void reset();

    void repair();

    void outputNode(String targetPath, ObjectNode node);

    void runMergeAction(VirtualFileEx merged, List<VirtualFileEx> children, boolean needToCreateChildren);

    void runSplitAction(VirtualFile file, String tag, SupportFileType targetFileType, SupportFileType splitFileType);

    Map<String, List<Map.Entry<String, JsonNode>>> getGroupMembersOfTraceTarget(String targetPath);

    static ConfigurationData initConfigurationData() {
        ConfigurationData data = new ConfigurationData();
        return data;
    }
}
