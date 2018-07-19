package org.jetbrains.research.groups.ml_methods.extraction.refactoring;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.sixrr.metrics.utils.MethodUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class TextFormRefactoring {
    private final String targetClassQualifiedName;
    private final String methodPackage;
    private final String methodName;
    private final List<String> paramsClasses;

    public TextFormRefactoring(String methodPackage, String methodName,
                               List<String> params, String destinationClassQualifiedName) {
        this.methodPackage = methodPackage;
        this.methodName = methodName;
        this.paramsClasses = params;
        this.targetClassQualifiedName = destinationClassQualifiedName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TextFormRefactoring that = (TextFormRefactoring) o;
        return Objects.equals(targetClassQualifiedName, that.targetClassQualifiedName) &&
                Objects.equals(methodPackage, that.methodPackage) &&
                Objects.equals(methodName, that.methodName) &&
                Objects.equals(paramsClasses, that.paramsClasses);
    }

    private String getMethodsSignature() {
        StringBuilder methodsSignature = new StringBuilder();
        methodsSignature.append(methodPackage);
        methodsSignature.append(".");
        methodsSignature.append(methodName);
        if (paramsClasses.isEmpty()) {
            return methodsSignature.toString();
        }
        methodsSignature.append("(");
        paramsClasses.forEach(s -> methodsSignature.append(s).append(","));
        methodsSignature.deleteCharAt(methodsSignature.length() - 1);
        methodsSignature.append(")");
        return methodsSignature.toString();
    }

    private String getClassQualifiedName() {
        return targetClassQualifiedName;
    }

    private boolean isOfGivenMethod(PsiMethod method) {
        String methodsSignature = MethodUtils.calculateSignature(method);
        String methodsSignatureWithoutParams = methodsSignature.split("\\(")[0];
        String refactoringSignature = getMethodsSignature();
        return refactoringSignature.equals(methodsSignature) ||
                refactoringSignature.equals(methodsSignatureWithoutParams);
    }

    private boolean isToGivenPsiClass(PsiClass aClass) {
        return getClassQualifiedName().equals(aClass.getQualifiedName());
    }

    static Optional<TextFormRefactoring> getRefactoringOfGivenMethod(List<TextFormRefactoring> textualRefactorings,
                                                                     PsiMethod method) {
        List<TextFormRefactoring> matchedRefactorings = textualRefactorings.stream().
                filter(textualRefactoring -> textualRefactoring.isOfGivenMethod(method)).collect(Collectors.toList());
        if (matchedRefactorings.size() > 1) {
            throw new IllegalStateException("Refactorings list is ambiguous");
        }
        return Optional.ofNullable(matchedRefactorings.isEmpty() ? null : matchedRefactorings.get(0));
    }

    static List<TextFormRefactoring> getRefactoringsToGivenClass(List<TextFormRefactoring> textualRefactorings,
                                                                 PsiClass aClass) {
        return textualRefactorings.stream().
                filter(textualRefactoring -> textualRefactoring.isToGivenPsiClass(aClass)).
                collect(Collectors.toList());
    }
}
