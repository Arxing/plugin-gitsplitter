package org.arxing.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import org.arxing.core.SupportFileManager;
import org.arxing.core.SupportFileType;
import org.arxing.util.MessagesUtil;
import org.jetbrains.annotations.NotNull;

public class SplitAction extends CustomAction {

    protected void onActionPerformed(@NotNull AnActionEvent e) throws Exception {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null)
            return;
        if (!SupportFileManager.isFileSupport(file)) {
            MessagesUtil.showWarning("Unsupported file extension");
            return;
        }
        String tag = Messages.showInputDialog("Input your tag", MessagesUtil.TITLE, Messages.getInformationIcon());
        if (tag == null)
            return;
        SupportFileType targetFileType = SupportFileManager.parseContentType(file);


        InputValidator validator = new InputValidator() {
            @Override public boolean checkInput(String inputString) {
                return SupportFileType.getOptionsList().contains(inputString);
            }

            @Override public boolean canClose(String inputString) {
                return false;
            }
        };

        String chooseTypeName = Messages.showEditableChooseDialog("Choose content type",
                                                                  MessagesUtil.TITLE,
                                                                  Messages.getInformationIcon(),
                                                                  SupportFileType.getOptionsArray(),
                                                                  targetFileType.getTypeName(),
                                                                  validator);
        if (chooseTypeName == null)
            return;
        SupportFileType splitFileType = SupportFileType.parse(chooseTypeName);

        getConfigurationService().runSplitAction(file, tag, targetFileType, splitFileType);
    }
}
