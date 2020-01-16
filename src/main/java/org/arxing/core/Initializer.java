package org.arxing.core;

import com.annimon.stream.Stream;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileCopyEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;
import com.intellij.util.messages.MessageBusConnection;

import org.arxing.interfaces.VirtualFileWatcher;
import org.arxing.model.ConfigurationData;
import org.arxing.service.ConfigurationService;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Initializer implements StartupActivity, BulkFileListener {
    private ConfigurationService service;

    private void listenAllTrace() {
        MessageBusConnection connection = ApplicationManager.getApplication().getMessageBus().connect();
        connection.subscribe(VirtualFileManager.VFS_CHANGES, this);
    }

    @Override public void runActivity(@NotNull Project project) {
        listenAllTrace();
        service = ConfigurationService.getInstance(project);
        service.mergeAll();
    }

    @Override public void after(@NotNull List<? extends VFileEvent> events) {
        handleFileWatched(events, fileAfterWatcher);
    }

    private String getFullPathWithEvent(VFileEvent e) {
        if (e instanceof VFileMoveEvent)
            return ((VFileMoveEvent) e).getOldPath();
        if (e instanceof VFilePropertyChangeEvent) {
            if (((VFilePropertyChangeEvent) e).isRename())
                return ((VFilePropertyChangeEvent) e).getOldPath();
        }
        return e.getPath();
    }

    private void handleFileWatched(@NotNull List<? extends VFileEvent> events, VirtualFileWatcher fileWatcher) {
        VFileEvent event = Stream.of(events).findFirst().orElse(null);
        if (event == null)
            return;
        String fullPath = getFullPathWithEvent(event);
        String relPath = service.computeRelPath(fullPath);
        if (!service.isInTraceOrChildOfAnyTarget(relPath))
            return;
        boolean isTarget = service.isInTrace(relPath);
        ConfigurationData.TraceTargetNode targetNode;
        ConfigurationData.TraceChildNode childNode;

        if (isTarget) {
            targetNode = service.findTraceTarget(relPath);
            childNode = null;
        } else {
            targetNode = service.findParentTargetOfChild(relPath);
            childNode = service.findFirstTraceChild(relPath);
        }

        if (event instanceof VFileDeleteEvent)
            fileWatcher.onDelete((VFileDeleteEvent) event, isTarget, targetNode, childNode);
        if (event instanceof VFileContentChangeEvent)
            fileWatcher.onContentChange((VFileContentChangeEvent) event, isTarget, targetNode, childNode);
        if (event instanceof VFileMoveEvent)
            fileWatcher.onMove((VFileMoveEvent) event, isTarget, targetNode, childNode);
        if (event instanceof VFilePropertyChangeEvent)
            fileWatcher.onPropertyChange((VFilePropertyChangeEvent) event, isTarget, targetNode, childNode);
        if (event instanceof VFileCreateEvent)
            fileWatcher.onCreate((VFileCreateEvent) event, isTarget, targetNode, childNode);
        if (event instanceof VFileCopyEvent)
            fileWatcher.onCopy((VFileCopyEvent) event, isTarget, targetNode, childNode);
    }

    private VirtualFileWatcher fileAfterWatcher = new VirtualFileWatcher.VirtualFileWatcherAdapter() {

        @Override public void onMove(VFileMoveEvent e,
                                     boolean isTarget,
                                     ConfigurationData.TraceTargetNode targetNode,
                                     ConfigurationData.TraceChildNode childNode) {
            String relOldPath = service.computeRelPath(e.getOldPath());
            String relNewPath = service.computeRelPath(e.getPath());
            if (isTarget) {
                service.moveTraceTarget(relOldPath, relNewPath);
            } else {
                service.moveTraceChildInTarget(targetNode.getPath(), relOldPath, relNewPath);
            }
        }

        @Override public void onDelete(VFileDeleteEvent e,
                                       boolean isTarget,
                                       ConfigurationData.TraceTargetNode targetNode,
                                       ConfigurationData.TraceChildNode childNode) {
            if (isTarget) {
                service.removeTraceTarget(targetNode.getPath());
            } else {
                service.removeTraceChildInTarget(targetNode.getPath(), childNode.getPath());
            }
        }

        @Override public void onContentChange(VFileContentChangeEvent e,
                                              boolean isTarget,
                                              ConfigurationData.TraceTargetNode targetNode,
                                              ConfigurationData.TraceChildNode childNode) {
            if (isTarget)
                return;
            String targetPath = targetNode.getPath();
            service.merge(targetPath);
        }

        @Override public void onPropertyChange(VFilePropertyChangeEvent e,
                                               boolean isTarget,
                                               ConfigurationData.TraceTargetNode targetNode,
                                               ConfigurationData.TraceChildNode childNode) {
            if (!e.isRename())
                return;
            String relOldPath = service.computeRelPath(e.getOldPath());
            String relNewPath = service.computeRelPath(e.getPath());
            if (isTarget) {
                service.moveTraceTarget(relOldPath, relNewPath);
            } else {
                service.moveTraceChildInTarget(targetNode.getPath(), relOldPath, relNewPath);
            }
        }
    };
}
