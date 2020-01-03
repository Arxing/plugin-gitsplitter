package org.arxing.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import org.arxing.manager.SupportFileManager;
import org.arxing.manager.SupportFileType;
import org.arxing.util.MessagesWrap;
import org.jetbrains.annotations.NotNull;

public class SplitAction extends CustomAction {

    protected void onActionPerformed(@NotNull AnActionEvent e) throws Exception {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (file == null)
            return;
        if (!SupportFileManager.isFileSupport(file)) {
            MessagesWrap.showWarning("Unsupported file extension");
            return;
        }
        String tag = Messages.showInputDialog("Input your tag", MessagesWrap.TITLE, Messages.getInformationIcon());
        if (tag == null)
            return;
        SupportFileType supportFileType = SupportFileManager.parseContentType(file);


        InputValidator validator = new InputValidator() {
            @Override public boolean checkInput(String inputString) {
                return SupportFileType.getOptionsList().contains(inputString);
            }

            @Override public boolean canClose(String inputString) {
                return false;
            }
        };

        String chooseTypeName = Messages.showEditableChooseDialog("Choose content type",
                                                                  MessagesWrap.TITLE,
                                                                  Messages.getInformationIcon(),
                                                                  SupportFileType.getOptionsArray(),
                                                                  supportFileType.getTypeName(),
                                                                  validator);
        if (chooseTypeName == null)
            return;
        supportFileType = SupportFileType.parse(chooseTypeName);

        getConfigurationService().runSplitAction(file, tag, supportFileType);
    }
}
