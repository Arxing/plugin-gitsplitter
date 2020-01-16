package org.arxing.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.VirtualFile;

import org.arxing.model.ConfigurationData;
import org.arxing.service.ConfigurationService;

public class StopTraceAction extends CustomAction {

    @Override protected void onActionPerformed(AnActionEvent e) throws Exception {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        ConfigurationService service = getConfigurationService();

        String fullPath = file.getPath();
        String relPath = service.computeRelPath(fullPath);
        if (service.isInTraceOrChildOfAnyTarget(relPath)) {
            if (service.isInTrace(relPath)) {
                service.removeTraceTarget(relPath);
            } else {
                ConfigurationData.TraceTargetNode targetNode = service.findParentTargetOfChild(relPath);
                service.removeTraceChildInTarget(targetNode.getPath(), relPath);
            }
        }
    }
}
