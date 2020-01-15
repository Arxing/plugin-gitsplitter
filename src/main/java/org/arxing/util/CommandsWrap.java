package org.arxing.util;

import com.intellij.openapi.application.ApplicationManager;

import org.arxing.interfaces.ThrowsAction;

public class CommandsWrap {
    private CommandsWrap() {
    }

    public static void runWriteAction(ThrowsAction runnable) {
        ApplicationManager.getApplication().runWriteAction(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                MessagesWrap.handleThrowable(e);
            }
        });
    }

    public static void runReadAction(ThrowsAction runnable) {
        ApplicationManager.getApplication().runReadAction(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                MessagesWrap.handleThrowable(e);
            }
        });
    }

}
