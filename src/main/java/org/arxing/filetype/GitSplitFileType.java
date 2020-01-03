package org.arxing.filetype;

import com.intellij.icons.AllIcons;
import com.intellij.json.JsonLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class GitSplitFileType extends LanguageFileType {
    public final static GitSplitFileType INSTANCE = new GitSplitFileType();

    private GitSplitFileType() {
        super(Language.findInstance(JsonLanguage.class));
    }

    @NotNull @Override public String getName() {
        return "Split file";
    }

    @NotNull @Override public String getDescription() {
        return "Git-splitter based file";
    }

    @NotNull @Override public String getDefaultExtension() {
        return "gitsplit";
    }

    @Nullable @Override public Icon getIcon() {
        return AllIcons.Actions.Download;
    }
}
