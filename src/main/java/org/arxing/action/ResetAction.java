package org.arxing.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;

import org.arxing.service.ConfigurationService;
import org.arxing.util.FileHelper;

public class ResetAction extends CustomAction {

    @Override protected void onActionPerformed(AnActionEvent e) throws Exception {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);

        if (file == null)
            return;
        if (FileHelper.isConfigurationFile(getProject(), file)) {
            ConfigurationService service = ConfigurationService.getInstance(getProject());
            service.reset();
        }
    }
}
