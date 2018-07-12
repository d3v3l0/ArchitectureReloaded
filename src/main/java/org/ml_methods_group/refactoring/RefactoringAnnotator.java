package org.ml_methods_group.refactoring;

import com.intellij.analysis.AnalysisScope;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMember;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ml_methods_group.plugin.AutomaticRefactoringAction;
import org.ml_methods_group.utils.PsiSearchUtil;

import java.util.Map;

public class RefactoringAnnotator implements Annotator {
    @Override
    public synchronized void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        final Project project = psiElement.getProject();
        final AnalysisScope scope = new AnalysisScope(project);

        for (String algorithm : RefactoringExecutionContext.getAvailableAlgorithms()) {
            try {
                setAnnotations(psiElement,
                        algorithm,
                        AutomaticRefactoringAction.getInstance(project).getRefactoringsForName(algorithm),
                        annotationHolder, scope);
            } catch (IllegalArgumentException e) {
                //ignore
            }
        }
    }

    private static void setAnnotations(@NotNull PsiElement element,
                                       @NotNull String algorithmName,
                                       final Map<String, String> refactorings,
                                       @NotNull AnnotationHolder holder,
                                       @NotNull AnalysisScope scope) {
        if (refactorings == null || refactorings.isEmpty()) {
            return;
        }
        final String name = PsiSearchUtil.getHumanReadableName(element);
        if (refactorings.containsKey(name)) {
            final Annotation annotation = holder.createWarningAnnotation(
                    getAnnotationPart(element),
                    String.format("Can be moved to %s (%s)",
                            refactorings.get(name), algorithmName));

            annotation.registerFix(new RefactorIntentionAction(name, refactorings.get(name), scope));
        }
    }

    @NotNull
    private static TextRange getAnnotationPart(@Nullable PsiElement element) {
        if (element instanceof PsiMember) {
            final PsiIdentifier identifier = PsiTreeUtil.getChildOfType(element, PsiIdentifier.class);
            return identifier != null ? identifier.getTextRange() : TextRange.EMPTY_RANGE;
        }
        return TextRange.EMPTY_RANGE;
    }

}
