package org.arxing.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;

import org.arxing.core.SupportFileManager;
import org.arxing.core.SupportFileType;
import org.arxing.service.ConfigurationService;
import org.arxing.util.MessagesUtil;

public class TraceAction extends CustomAction {

    @Override protected void onActionPerformed(AnActionEvent e) throws Exception {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);

        if (file == null)
            return;
        if (!SupportFileManager.isFileSupport(file)) {
            MessagesUtil.showWarning("Unsupported file extension");
            return;
        }

        ConfigurationService service = getConfigurationService();

        String fullPath = file.getPath();
        String relPath = service.computeRelPath(fullPath);
        if (service.isInTrace(relPath)) {
            MessagesUtil.showInfo("This file was already in trace.");
        } else {
            SupportFileType fileType = SupportFileType.parse(file.getExtension());
            service.addTraceTarget(relPath, fileType);
        }
    }
}
