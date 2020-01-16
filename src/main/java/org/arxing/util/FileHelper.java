package org.arxing.util;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.Consumer;
import com.intellij.util.ThrowableRunnable;
import com.sun.jna.platform.win32.Guid;

import org.arxing.axutils_java.JParser;
import org.arxing.model.ConfigurationData;
import org.arxing.service.ConfigurationService;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("Duplicates")
public class FileHelper {
    private final static String CONFIGURATION_FILE_NAME = ".gitsplitter";
    private final static String ENCODING = "utf-8";
    private final static Application application = ApplicationManager.getApplication();

    public static VirtualFile findProjectRootFile(Project project) {
        return LocalFileSystem.getInstance().findFileByPath(Objects.requireNonNull(project.getBasePath()));
    }

    public static boolean isConfigurationFile(Project project, VirtualFile file) {
        VirtualFile conf = findProjectRootFile(project).findChild(CONFIGURATION_FILE_NAME);
        if (conf == null || file == null)
            return false;
        return file.getPath().equalsIgnoreCase(conf.getPath());
    }

    public static VirtualFile createConfigurationFile(Project project) {
        AtomicReference<VirtualFile> result = new AtomicReference<>();
        runWriteActionSync(() -> {
            VirtualFile configuration = findProjectRootFile(project).createChildData(null, CONFIGURATION_FILE_NAME);
            ConfigurationData data = ConfigurationService.initConfigurationData();
            writeSync(project, configuration, JParser.toPrettyJson(data));
            result.set(configuration);
        });
        return result.get();
    }

    public static VirtualFile findOrCreateConfigurationFile(Project project) {
        VirtualFile configuration = findProjectRootFile(project).findChild(CONFIGURATION_FILE_NAME);
        if (configuration == null) {
            return createConfigurationFile(project);
        } else {
            return configuration;
        }
    }

    public static String relativizeToConfigurationFile(Project project, String path) {
        VirtualFile projectRoot = findProjectRootFile(project);
        Path configurationPath = new File(projectRoot.getPath()).toPath();
        Path filePath = new File(path).toPath();
        return configurationPath.relativize(filePath).toFile().getPath().replace("\\", "/");
    }

    public static String relativizeToConfigurationFile(Project project, VirtualFile file) {
        return relativizeToConfigurationFile(project, file.getPath());
    }

    public static boolean exists(String path) {
        return findVirtualFile(path) != null;
    }

    public static VirtualFile findVirtualFile(String path) {
        return LocalFileSystem.getInstance().findFileByPath(path);
    }

    public static PsiFile findPsiFile(Project project, String path) {
        return virtualFileToPsiFile(project, findVirtualFile(path));
    }

    public static Document findDocument(Project project, String path) {
        return psiFileToDocument(findPsiFile(project, path));
    }

    public static String computeRelPath(Project project, String path) {
        return new File(Objects.requireNonNull(project.getBasePath())).toPath()
                                                                      .relativize(new File(path).toPath())
                                                                      .toString()
                                                                      .replace("\\", "/");
    }

    public static String computeFullPath(Project project, String path) {
        return new File(project.getBasePath(), path).getPath();
    }

    public static void runWriteAction(ThrowableRunnable action) {
        if (application.isWriteAccessAllowed()) {
            try {
                action.run();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } else {
            application.runWriteAction(() -> runWriteAction(action));
        }
    }

    public static void runWriteActionSync(ThrowableRunnable action) {
        runWriteAction(action);
    }

    public static void runWriteActionAsync(ThrowableRunnable action) {
        runWriteAction(action);
    }

    public static void runReadAction(ThrowableRunnable action) {
        if (application.isReadAccessAllowed()) {
            try {
                action.run();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        } else {
            application.runReadAction(() -> runReadAction(action));
        }
    }

    public static void runReadActionSync(ThrowableRunnable action) {
        runReadAction(action);
    }

    public static void runReadActionAsync(ThrowableRunnable action) {
        runReadAction(action);
    }

    private static void writeInternal(Document document, String content, boolean async) {
        runWriteAction(() -> document.setText(content));
    }

    private static void readInternal(Document document, Consumer<String> consumer, boolean async) {
        runReadAction(() -> consumer.consume(document.getText()));
    }

    public static Document psiFileToDocument(PsiFile psiFile) {
        return PsiDocumentManager.getInstance(psiFile.getProject()).getDocument(psiFile);
    }

    public static PsiFile virtualFileToPsiFile(Project project, VirtualFile virtualFile) {
        AtomicReference<PsiFile> result = new AtomicReference<>();
        runReadActionSync(() -> result.set(PsiManager.getInstance(project).findFile(virtualFile)));
        return result.get();
    }

    public static Document virtualFileToDocument(Project project, VirtualFile virtualFile) {
        return psiFileToDocument(virtualFileToPsiFile(project, virtualFile));
    }

    public static void writeSync(Document document, String content) {
        if (document == null)
            return;
        writeInternal(document, content, false);
    }

    public static void writeSync(PsiFile psiFile, String content) {
        if (psiFile == null)
            return;
        writeSync(psiFileToDocument(psiFile), content);
    }

    public static void writeSync(Project project, VirtualFile virtualFile, String content) {
        if (virtualFile == null)
            return;
        writeSync(virtualFileToPsiFile(project, virtualFile), content);
    }

    public static String readSync(Document document) {
        if (document == null)
            return null;
        AtomicReference<String> result = new AtomicReference<>();
        readInternal(document, result::set, false);
        return result.get();
    }

    public static String readSync(PsiFile psiFile) {
        return readSync(psiFileToDocument(psiFile));
    }

    public static String readSync(Project project, VirtualFile virtualFile) {
        return readSync(virtualFileToPsiFile(project, virtualFile));
    }
}
