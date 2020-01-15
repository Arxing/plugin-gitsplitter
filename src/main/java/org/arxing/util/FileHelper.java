package org.arxing.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import org.arxing.axutils_java.JParser;
import org.arxing.interfaces.ThrowsConsumer;
import org.arxing.model.ConfigurationData;
import org.arxing.service.ConfigurationService;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

public class FileHelper {
    private final static String CONFIGURATION_FILE_NAME = ".gitsplitter";
    private final static String ENCODING = "utf-8";

    public static VirtualFile findProjectRootFile(Project project) {
        return LocalFileSystem.getInstance().findFileByPath(Objects.requireNonNull(project.getBasePath()));
    }

    public static void createConfigurationFile(Project project, ThrowsConsumer<VirtualFile> consumer) {
        CommandsWrap.runWriteAction(() -> {
            VirtualFile configuration = findProjectRootFile(project).createChildData(null, CONFIGURATION_FILE_NAME);
            ConfigurationData data = ConfigurationService.initConfigurationData();
            FileHelper.writeContent(configuration, JParser.toPrettyJson(data));
            consumer.apply(configuration);
        });
    }

    public static void findOrCreateConfigurationFile(Project project, ThrowsConsumer<VirtualFile> consumer) {
        VirtualFile configuration = findProjectRootFile(project).findChild(CONFIGURATION_FILE_NAME);
        if (configuration == null) {
            createConfigurationFile(project, consumer);
        } else {
            try {
                consumer.apply(configuration);
            } catch (Exception e) {
                MessagesWrap.handleThrowable(e);
            }
        }
    }

    public static void writeContent(VirtualFile file, String content) {
        CommandsWrap.runWriteAction(() -> {
            file.setBinaryContent(content.getBytes(ENCODING));
        });
        file.refresh(false, false);
    }

    public static String relativizeToConfigurationFile(Project project, String path) {
        VirtualFile projectRoot = findProjectRootFile(project);
        Path configurationPath = new File(projectRoot.getPath()).toPath();
        Path filePath = new File(path).toPath();
        MessagesWrap.println("config=%s file=%s", configurationPath.toString(), filePath.toString());
        return configurationPath.relativize(filePath).toFile().getPath().replace("\\", "/");
    }

    public static String relativizeToConfigurationFile(Project project, VirtualFile file) {
        return relativizeToConfigurationFile(project, file.getPath());
    }
}
