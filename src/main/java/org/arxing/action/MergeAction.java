package org.arxing.action;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.vfs.VirtualFile;

import org.arxing.manager.SupportFileManager;
import org.arxing.model.VirtualFileEx;
import org.arxing.util.MessagesWrap;

import java.util.List;

public class MergeAction extends CustomAction {

    protected void onActionPerformed(AnActionEvent e) throws Exception {
        VirtualFile[] splitVirtualFiles = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (splitVirtualFiles == null || splitVirtualFiles.length == 0)
            return;
        List<String> unsupportedFiles = Stream.ofNullable(splitVirtualFiles)
                                              .filterNot(o -> !o.isDirectory() && SupportFileManager.isFileSupport(o))
                                              .map(VirtualFile::getPath)
                                              .toList();
        if (unsupportedFiles.size() > 0) {
            String joins = Stream.of(unsupportedFiles).collect(Collectors.joining("\n"));
            MessagesWrap.showError("Unsupported:\n%s", joins);
            return;
        }
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFileDescriptor();
        VirtualFile mergedVirtualFile = FileChooser.chooseFile(descriptor, getProject(), null);
        if (mergedVirtualFile == null)
            return;
        if (!SupportFileManager.isFileSupport(mergedVirtualFile)) {
            MessagesWrap.showError("Unsupported file extension(%s)", mergedVirtualFile.getName());
            return;
        }
        if (Stream.of(splitVirtualFiles).map(VirtualFile::getPath).anyMatch(o -> o.equals(mergedVirtualFile.getPath()))) {
            MessagesWrap.showError("Split files could not include merged file.");
            return;
        }

        VirtualFileEx mergedVirtualFileEx = VirtualFileEx.of(mergedVirtualFile);
        List<VirtualFileEx> splitVirtualFileExList = Stream.of(splitVirtualFiles).map(VirtualFileEx::of).toList();

        getConfigurationService().runMergeAction(mergedVirtualFileEx, splitVirtualFileExList, true);
    }
}
