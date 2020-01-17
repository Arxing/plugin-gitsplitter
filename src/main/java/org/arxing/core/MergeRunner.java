package org.arxing.core;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;

import org.arxing.service.ConfigurationService;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;


public class MergeRunner implements StartupActivity {

    @Override public void runActivity(@NotNull Project project) {
        Timer timer = new Timer();
        timer.schedule(new Task(project), 0, 1000);
    }

    private class Task extends TimerTask {
        private Project project;

        Task(Project project) {
            this.project = project;
        }

        @Override public void run() {
            ApplicationManager.getApplication().invokeAndWait(() -> ConfigurationService.getInstance(project).mergeAll());
        }
    }
}
