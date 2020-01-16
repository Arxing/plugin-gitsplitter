package org.arxing.action;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import org.arxing.core.SupportFileManager;
import org.arxing.model.VirtualFileEx;
import org.arxing.util.MessagesUtil;

import java.util.List;

public class MergeAction extends CustomAction {

    protected void onActionPerformed(AnActionEvent e) throws Exception {
        VirtualFile projectRootFile = LocalFileSystem.getInstance().findFileByPath(e.getProject().getBasePath());
        VirtualFile selectFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        VirtualFile[] splitVirtualFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (splitVirtualFiles == null || splitVirtualFiles.length == 0)
            return;
        List<String> unsupportedFiles = Stream.ofNullable(splitVirtualFiles)
                                              .filterNot(o -> !o.isDirectory() && SupportFileManager.isFileSupport(o))
                                              .map(VirtualFile::getPath)
                                              .toList();
        if (unsupportedFiles.size() > 0) {
            String joins = Stream.of(unsupportedFiles).collect(Collectors.joining("\n"));
            MessagesUtil.showError("Unsupported:\n%s", joins);
            return;
        }
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
        descriptor.setRoots(projectRootFile);
        VirtualFile mergedVirtualFile = FileChooser.chooseFile(descriptor, getProject(), selectFile);
        if (mergedVirtualFile == null)
            return;
        if (!SupportFileManager.isFileSupport(mergedVirtualFile)) {
            MessagesUtil.showError("Unsupported file extension(%s)", mergedVirtualFile.getName());
            return;
        }
        if (Stream.of(splitVirtualFiles).map(VirtualFile::getPath).anyMatch(o -> o.equals(mergedVirtualFile.getPath()))) {
            MessagesUtil.showError("Split files could not include merged file.");
            return;
        }

        VirtualFileEx mergedVirtualFileEx = VirtualFileEx.of(mergedVirtualFile);
        List<VirtualFileEx> splitVirtualFileExList = Stream.of(splitVirtualFiles).map(VirtualFileEx::of).toList();

        getConfigurationService().runMergeAction(mergedVirtualFileEx, splitVirtualFileExList, true);
    }
}
