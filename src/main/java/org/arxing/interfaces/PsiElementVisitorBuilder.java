package org.arxing.interfaces;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;

import org.arxing.model.ConfigurationData;

public interface PsiElementVisitorBuilder {
    PsiElementVisitor createVisitor(ProblemsHolder holder,
                                    ConfigurationData.TraceTargetNode targetNode,
                                    ConfigurationData.TraceChildNode childNode);
}
