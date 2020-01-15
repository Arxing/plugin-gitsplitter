package org.arxing.interfaces;

import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileCopyEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent;
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent;

import org.arxing.model.ConfigurationData;

public interface VirtualFileWatcher {

    void onDelete(VFileDeleteEvent e,
                  boolean isTarget,
                  ConfigurationData.TraceTargetNode targetNode,
                  ConfigurationData.TraceChildNode childNode);

    void onContentChange(VFileContentChangeEvent e,
                         boolean isTarget,
                         ConfigurationData.TraceTargetNode targetNode,
                         ConfigurationData.TraceChildNode childNode);

    void onMove(VFileMoveEvent e,
                boolean isTarget,
                ConfigurationData.TraceTargetNode targetNode,
                ConfigurationData.TraceChildNode childNode);

    void onCreate(VFileCreateEvent e,
                  boolean isTarget,
                  ConfigurationData.TraceTargetNode targetNode,
                  ConfigurationData.TraceChildNode childNode);

    void onCopy(VFileCopyEvent e,
                boolean isTarget,
                ConfigurationData.TraceTargetNode targetNode,
                ConfigurationData.TraceChildNode childNode);

    void onPropertyChange(VFilePropertyChangeEvent e,
                          boolean isTarget,
                          ConfigurationData.TraceTargetNode targetNode,
                          ConfigurationData.TraceChildNode childNode);

    class VirtualFileWatcherAdapter implements VirtualFileWatcher {

        @Override public void onDelete(VFileDeleteEvent e,
                                       boolean isTarget,
                                       ConfigurationData.TraceTargetNode targetNode,
                                       ConfigurationData.TraceChildNode childNode) {

        }

        @Override public void onContentChange(VFileContentChangeEvent e,
                                              boolean isTarget,
                                              ConfigurationData.TraceTargetNode targetNode,
                                              ConfigurationData.TraceChildNode childNode) {

        }

        @Override public void onMove(VFileMoveEvent e,
                                     boolean isTarget,
                                     ConfigurationData.TraceTargetNode targetNode,
                                     ConfigurationData.TraceChildNode childNode) {

        }

        @Override public void onCreate(VFileCreateEvent e,
                                       boolean isTarget,
                                       ConfigurationData.TraceTargetNode targetNode,
                                       ConfigurationData.TraceChildNode childNode) {

        }

        @Override public void onCopy(VFileCopyEvent e,
                                     boolean isTarget,
                                     ConfigurationData.TraceTargetNode targetNode,
                                     ConfigurationData.TraceChildNode childNode) {

        }

        @Override public void onPropertyChange(VFilePropertyChangeEvent e,
                                               boolean isTarget,
                                               ConfigurationData.TraceTargetNode targetNode,
                                               ConfigurationData.TraceChildNode childNode) {

        }
    }
}
