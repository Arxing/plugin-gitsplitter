package org.arxing.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

import org.arxing.service.ConfigurationService;
import org.arxing.util.MessagesUtil;
import org.jetbrains.annotations.NotNull;

public abstract class CustomAction extends AnAction {
    private Project project;

    @Override public void actionPerformed(@NotNull AnActionEvent e) {
        project = e.getProject();
        try {
            onActionPerformed(e);
        } catch (Exception e1) {
            MessagesUtil.handleThrowable(e1);
        }
    }

    protected abstract void onActionPerformed(AnActionEvent e) throws Exception;

    protected Project getProject() {
        return project;
    }

    protected ConfigurationService getConfigurationService(){
        return ConfigurationService.getInstance(getProject());
    }
}
