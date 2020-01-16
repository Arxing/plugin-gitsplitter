package org.arxing.core;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

import org.arxing.model.ConfigurationData;
import org.arxing.service.ConfigurationService;
import org.arxing.util.MessagesUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GitsplitterInspection extends LocalInspectionTool implements PsiElementVisitorFactory.PsiElementScanner {
    private ConfigurationService service;
    private PsiElementVisitorFactory visitorFactory = new PsiElementVisitorFactory(this);
    private ProblemsHolder problemsHolder;
    private ConfigurationData.TraceTargetNode currentTargetNode;
    private ConfigurationData.TraceChildNode currentChildNode;

    public static boolean equalsNode(JsonNode node1, JsonNode node2) {
        if (node1.getNodeType() != node2.getNodeType()) {
            return false;
        }
        if (node1.isObject() && node2.isObject()) {
            List<String> sameKeys = Stream.of(node1.fieldNames()).filter(n1 -> Stream.of(node2.fieldNames()).anyMatch(n1::equals)).toList();
            return Stream.of(sameKeys).allMatch(key -> equalsNode(node1.get(key), node2.get(key)));
        }
        if (node1.isArray() && node2.isArray()) {
            return true;
        }
        return node1.equals(node2);
    }

    @NotNull @Override public HighlightDisplayLevel getDefaultLevel() {
        return HighlightDisplayLevel.WARNING;
    }

    @NotNull @Override public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        service = ConfigurationService.getInstance(holder.getProject());
        String currentFullPath = holder.getFile().getVirtualFile().getPath();
        String currentRelPath = service.computeRelPath(currentFullPath);
        if (!service.isChildOfAnyTarget(currentRelPath))
            return super.buildVisitor(holder, isOnTheFly);

        problemsHolder = holder;
        currentTargetNode = service.findParentTargetOfChild(currentRelPath);
        currentChildNode = service.findTraceChildInTarget(currentTargetNode.getPath(), currentRelPath);

        switch (currentChildNode.getAsSupportFileType()) {
            case json:
                return visitorFactory.json();
            case yaml:
                return visitorFactory.yaml();
            case xml:
                return visitorFactory.xml();
            case properties:
                return visitorFactory.properties();
        }
        return super.buildVisitor(holder, isOnTheFly);
    }

    @Nls @NotNull @Override public String getDisplayName() {
        return "";
    }

    @Override public void onScan(String scanKey, JsonNode scanNode, PsiElement element) {
        Map<String, String> duplicatePaths = new HashMap<>();
        boolean isValid = true;
        Map<String, List<Map.Entry<String, JsonNode>>> groupMembers = service.getGroupMembersOfTraceTarget(currentTargetNode.getPath());
        for (Map.Entry<String, List<Map.Entry<String, JsonNode>>> group : groupMembers.entrySet()) {
            String childPath = group.getKey();
            // ignore self
            if (childPath.equalsIgnoreCase(currentChildNode.getPath()))
                continue;
            for (Map.Entry<String, JsonNode> member : group.getValue()) {
                String memberKey = member.getKey();
                JsonNode memberNode = member.getValue();
                if (!memberKey.equals(scanKey))
                    continue;
                if (equalsNode(memberNode, scanNode))
                    continue;
                duplicatePaths.put(childPath, memberNode.toString());
                isValid = false;
            }
        }
        if (!isValid) {
            String extras = Stream.of(duplicatePaths)
                                  .map(o -> String.format("%s -> %s", o.getKey(), o.getValue()))
                                  .collect(Collectors.joining("<br>"));
            String description = String.format("This element defined in multiple files but has different value.<br>%s", extras);
            String nodeString = scanNode.toPrettyString();
            problemsHolder.registerProblem(element,
                                           description,
                                           new OverridesFix(scanKey, nodeString),
                                           new RemoveOthersFix(scanKey, nodeString));
        }
    }

    private class OverridesFix implements LocalQuickFix {
        private String key;
        private String val;

        public OverridesFix(String key, String val) {
            this.key = key;
            this.val = val;
        }

        @Nls @NotNull @Override public String getFamilyName() {
            return String.format("Overrides conflicting elements with value(\"%s\").", val);
        }

        @Override public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            MessagesUtil.showWarning("Coming Soon...");
        }
    }

    private class RemoveOthersFix implements LocalQuickFix {
        private String key;
        private String val;

        public RemoveOthersFix(String key, String val) {
            this.key = key;
            this.val = val;
        }

        @Nls @NotNull @Override public String getFamilyName() {
            return "Remove conflicting elements.";
        }

        @Override public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            MessagesUtil.showWarning("Coming Soon...");
        }
    }
}
