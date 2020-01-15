package org.arxing.service.impl;

import org.arxing.axutils_java.JParser;
import org.arxing.manager.SupportFileManager;
import org.arxing.manager.SupportFileType;
import org.arxing.model.ConfigurationData;
import org.arxing.model.VirtualFileEx;
import org.arxing.service.ConfigurationService;
import org.arxing.util.CommandsWrap;
import org.arxing.util.FileHelper;

import com.annimon.stream.Stream;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.sun.istack.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ConfigurationServiceImpl implements ConfigurationService {
    public final static String SUFFIX = "gitsplit";
    private Project project;
    private ConfigurationData configurationData;
    private VirtualFile configurationFile;

    public ConfigurationServiceImpl(Project project) {
        this.project = project;
        FileHelper.findOrCreateConfigurationFile(project, file -> {
            configurationFile = file;
            configurationData = JParser.fromJsonOrNull(VfsUtil.loadText(configurationFile), ConfigurationData.class);
        });
    }

    private String relPath(String path) {
        return new File(Objects.requireNonNull(project.getBasePath())).toPath()
                                                                      .relativize(new File(path).toPath())
                                                                      .toString()
                                                                      .replace("\\", "/");
    }

    @Override public void syncConfiguration() {
        String content = JParser.toPrettyJson(configurationData);
        FileHelper.writeContent(configurationFile, content);
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
        if (isInTrace(targetPath)) {
            return Stream.of(findTraceTarget(targetPath).getChildren()).anyMatch(o -> o.getPath().equalsIgnoreCase(childPath));
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

    @Override public void runMergeAction(VirtualFileEx merged, List<VirtualFileEx> children, boolean needToCreateChildren) {
        List<VirtualFileEx> copiesChildren = new ArrayList<>();

        if (needToCreateChildren) {
            children.forEach(fileEx -> {
                VirtualFile file = fileEx.getFile();
                String copyFilename = String.format("%s.%s", file.getName(), SUFFIX);
                CommandsWrap.runWriteAction(() -> {
                    VirtualFile copyFile = file.copy(null, file.getParent(), copyFilename);
                    SupportFileType fileType = SupportFileManager.parseContentType(file);
                    copiesChildren.add(VirtualFileEx.of(copyFile, fileType));
                });
            });
        } else {
            copiesChildren.addAll(children);
        }

        ConfigurationData.TraceTargetNode target;
        if (isInTrace(merged.getFile().getPath())) {
            target = findTraceTarget(merged.getFile().getPath());
        } else {
            target = new ConfigurationData.TraceTargetNode(merged.getFile().getPath(), merged.getTypeName());
            addTraceTarget(merged.getFile().getPath(), merged.getType());
        }
        Stream.of(copiesChildren)
              .filterNot(o -> isChildOfTarget(target.getPath(), o.getPath()))
              .forEach(o -> addTraceChildInTarget(target.getPath(), o.getPath(), o.getType()));
    }

    @Override public void runSplitAction(VirtualFile targetFile, String tag, SupportFileType fileType) {
        String childFilename = String.format("%s_%s.%s.%s", targetFile.getNameWithoutExtension(), tag, targetFile.getExtension(), SUFFIX);
        CommandsWrap.runWriteAction(() -> {
            VirtualFile childFile = targetFile.getParent().createChildData(null, childFilename);
            FileHelper.writeContent(childFile, fileType.getInitContent());
            runMergeAction(VirtualFileEx.of(targetFile), Collections.singletonList(VirtualFileEx.of(childFile, fileType)), false);
        });
    }
}
