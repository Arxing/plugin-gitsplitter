package org.arxing.worker;

import com.annimon.stream.Stream;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileManagerListener;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent;

import org.arxing.model.ConfigurationData;
import org.arxing.service.ConfigurationService;
import org.arxing.util.MessagesWrap;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Initializer implements StartupActivity, BulkFileListener {
    private ConfigurationService configurationService;

    @Override public void runActivity(@NotNull Project project) {
        configurationService = ConfigurationService.getInstance(project);
        listenAllTrace();
    }

    private void listenAllTrace() {
        ApplicationManager.getApplication().getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, this);
    }

    @Override public void after(@NotNull List<? extends VFileEvent> events) {
        VFileEvent event = Stream.of(events).findFirst().orElse(null);
        if (event == null)
            return;
        String path = event.getPath();
        if(!configurationService.isInTraceOrChildOfAnyTarget(path))
            return;

        if (event instanceof VFileDeleteEvent)
            handleDeleteEvent((VFileDeleteEvent) event);
        if (event instanceof VFileContentChangeEvent)
            handleDeleteEvent((VFileDeleteEvent) event);
        if (event instanceof VFileMoveEvent)
            handleDeleteEvent((VFileDeleteEvent) event);
    }

    private void handleDeleteEvent(VFileDeleteEvent e) {
e.
    }
}
