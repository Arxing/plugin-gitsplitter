import com.intellij.ide.IconProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.IconManager;

import org.arxing.service.ConfigurationService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class IconsProvider extends IconProvider {

    public static final Icon Group = IconManager.getInstance().getIcon("/icons/spider_home.svg", IconsProvider.class);

    public static final Icon Segment = IconManager.getInstance().getIcon("/icons/spider.svg", IconsProvider.class);

    @Nullable @Override public Icon getIcon(@NotNull PsiElement element, int flags) {
        Project project = element.getProject();
        ConfigurationService service = ConfigurationService.getInstance(project);
        PsiFile file = element.getContainingFile();
        String filepath = file.getVirtualFile().getPath();
        String relPath = service.computeRelPath(filepath);
        if (service.isInTraceOrChildOfAnyTarget(relPath)) {
            if (service.isInTrace(relPath))
                return Group;
            else
                return Segment;
        }
        return null;
    }
}
