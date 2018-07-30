package org.jetbrains.research.groups.ml_methods.extraction.refactoring;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class RefactoringTextRepresentation {
    private final String targetClassQualifiedName;
    private final String sourceClassQualifiedName;
    private final String methodName;
    private final List<String> paramsClasses;

    public RefactoringTextRepresentation(String methodPackage, String methodName,
                                         List<String> params, String destinationClassQualifiedName) {
        this.sourceClassQualifiedName = methodPackage;
        this.methodName = methodName;
        this.paramsClasses = params;
        this.targetClassQualifiedName = destinationClassQualifiedName;
    }

    public RefactoringTextRepresentation(Refactoring refactoring) {
        PsiMethod method = refactoring.getMethod();
        PsiClass targetClass = refactoring.getTargetClass();
        if (method.getContainingClass() == null || method.getContainingClass().getQualifiedName() == null) {
            String errorMessage = "Refactorings without source qualified names are not supported. " +
                    "Problem during creating refactoring for method " + method.getName() + ".";
            throw new IllegalArgumentException(errorMessage);
        }
        sourceClassQualifiedName = method.getContainingClass().getQualifiedName();
        methodName = method.getName();
        paramsClasses = new ArrayList<>();
        targetClassQualifiedName = targetClass.getQualifiedName();
        final PsiParameter[] parameters = method.getParameterList().getParameters();
        for (PsiParameter psiParameter : parameters) {
            paramsClasses.add(psiParameter.getType().getCanonicalText());
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(targetClassQualifiedName, sourceClassQualifiedName, methodName, paramsClasses);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefactoringTextRepresentation that = (RefactoringTextRepresentation) o;
        return Objects.equals(targetClassQualifiedName, that.targetClassQualifiedName) &&
                Objects.equals(sourceClassQualifiedName, that.sourceClassQualifiedName) &&
                Objects.equals(methodName, that.methodName) &&
                Objects.equals(paramsClasses, that.paramsClasses);
    }

    String getMethodsSignature() {
        StringBuilder methodsSignature = new StringBuilder();
        methodsSignature.append(sourceClassQualifiedName);
        methodsSignature.append(".");
        methodsSignature.append(methodName);
        if (paramsClasses.isEmpty()) {
            return methodsSignature.toString();
        }
        if (paramsClasses.size() == 1 && paramsClasses.get(0).equals("void")) {
            methodsSignature.append("()");
        } else {
            methodsSignature.append("(");
            paramsClasses.forEach(s -> methodsSignature.append(s).append(","));
            methodsSignature.deleteCharAt(methodsSignature.length() - 1);
            methodsSignature.append(")");
        }
        return methodsSignature.toString();
    }

    private boolean isOfGivenMethod(PsiMethod method) {
        /*
        List<String> methodsParams = Arrays.stream(method.getParameterList().getParameters()).
                map(psiParameter -> psiParameter.getType().getCanonicalText()).
                collect(Collectors.toList());
        return method.getContainingClass() != null &&
                methodName.equals(method.getName()) &&
                sourceClassQualifiedName.equals(method.getContainingClass().getQualifiedName()) &&
                paramsClasses.equals(methodsParams);
        */
        String methodsSignature = MethodUtils.calculateSignature(method);
        String methodsSignatureWithoutParams = methodsSignature.split("\\(")[0];
        String refactoringSignature = getMethodsSignature();
        return refactoringSignature.equals(methodsSignature) ||
                refactoringSignature.equals(methodsSignatureWithoutParams);
    }

    private boolean isToGivenPsiClass(PsiClass aClass) {
        return targetClassQualifiedName.equals(aClass.getQualifiedName());
    }

    static List<RefactoringTextRepresentation> getRefactoringOfGivenMethod(List<RefactoringTextRepresentation> textualRefactorings,
                                                                          PsiMethod method) {
        return textualRefactorings.stream().
                filter(textualRefactoring -> textualRefactoring.isOfGivenMethod(method)).collect(Collectors.toList());
    }

    static List<RefactoringTextRepresentation> getRefactoringsToGivenClass(List<RefactoringTextRepresentation> textualRefactorings,
                                                                          PsiClass aClass) {
        return textualRefactorings.stream().
                filter(textualRefactoring -> textualRefactoring.isToGivenPsiClass(aClass)).
                collect(Collectors.toList());
    }

    public String getTargetClassQualifiedName() {
        return targetClassQualifiedName;
    }

    public String getSourceClassQualifiedName() {
        return sourceClassQualifiedName;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<String> getParamsClasses() {
        return paramsClasses;
    }
}
