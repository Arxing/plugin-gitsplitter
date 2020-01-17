import com.intellij.ide.IconProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import org.arxing.service.ConfigurationService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

public class IconsProvider extends IconProvider {

    public static final Icon Group = IconLoader.getIcon("/icons/spider_home.svg");

    public static final Icon Segment = IconLoader.getIcon("/icons/spider.svg");

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
