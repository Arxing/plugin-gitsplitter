package org.arxing.util;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.intellij.openapi.ui.Messages;

public class MessagesUtil {
    public final static String TITLE = "Git Splitter";

    private MessagesUtil() {
    }

    public static void showInfo(String format, Object... params) {
        Messages.showInfoMessage(String.format(format, params), TITLE);
    }

    public static void showError(String format, Object... params) {
        Messages.showErrorDialog(String.format(format, params), TITLE);
    }

    public static void showWarning(String format, Object... params) {
        Messages.showWarningDialog(String.format(format, params), TITLE);
    }

    public static void handleThrowable(Throwable throwable) {
        String s = throwable.getMessage() + "\n";
        s += Stream.of(throwable.getStackTrace()).map(StackTraceElement::toString).collect(Collectors.joining("\n"));
        throwable.printStackTrace();
        showError("%s", s);
    }

    public static void println(String format, Object... params) {
        System.out.println(String.format(format, params));
    }

    public static void highlightPrint(String format, Object... params) {
        println("\n===========================================================");
        println(format, params);
        println("===========================================================\n\n");
    }
}
