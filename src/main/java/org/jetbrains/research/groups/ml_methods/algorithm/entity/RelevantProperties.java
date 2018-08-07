package org.jetbrains.research.groups.ml_methods.algorithm.entity;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;

import static org.jetbrains.research.groups.ml_methods.utils.PsiSearchUtil.getHumanReadableName;

/**
 * A set of properties of an {@link CodeEntity}. A property is a method, a class or a field which has
 * some relation to the {@link CodeEntity} that stores object of this class. Each property has a
 * weight which corresponds to importance of this property.
 */
public class RelevantProperties {
    private final @NotNull Map<MethodEntity, Integer> notOverrideMethods = new HashMap<>();

    private final @NotNull Map<ClassEntity, Integer> classes = new HashMap<>();

    private final @NotNull Map<FieldEntity, Integer> fields = new HashMap<>();

    private final @NotNull Map<MethodEntity, Integer> overrideMethods = new HashMap<>();

    private final Integer DEFAULT_PROPERTY_WEIGHT = 1;

    void addNotOverrideMethod(MethodEntity method) {
        addNotOverrideMethod(method, DEFAULT_PROPERTY_WEIGHT);
    }


    void addNotOverrideMethod(MethodEntity method, Integer weight) {
        if (notOverrideMethods.getOrDefault(method, 0) < weight) {
            notOverrideMethods.put(method, weight);
        }
    }

    void addClass(ClassEntity aClass) {
        addClass(aClass, DEFAULT_PROPERTY_WEIGHT);
    }

    void addClass(ClassEntity aClass, Integer weight) {
        if (classes.getOrDefault(aClass, 0) < weight) {
            classes.put(aClass, weight);
        }
    }

    void addField(FieldEntity field) {
        addField(field, DEFAULT_PROPERTY_WEIGHT);
    }

    void addField(FieldEntity field, Integer weight) {
        if (fields.getOrDefault(field, 0) < weight) {
            fields.put(field, weight);
        }
    }

    void addOverrideMethod(MethodEntity method) {
        addOverrideMethod(method, DEFAULT_PROPERTY_WEIGHT);
    }

    void addOverrideMethod(MethodEntity method, Integer weight) {
        if (overrideMethods.getOrDefault(method, 0) < weight) {
            overrideMethods.put(method, weight);
        }
    }

    int numberOfMethods() {
        return notOverrideMethods.size();
    }

    public Set<FieldEntity> getFields() {
        return Collections.unmodifiableSet(fields.keySet());
    }

    public Set<MethodEntity> getNotOverrideMethods() {
        return Collections.unmodifiableSet(notOverrideMethods.keySet());
    }
    
    public Set<MethodEntity> getOverrideMethods() {
        return Collections.unmodifiableSet(overrideMethods.keySet());
    }

    public Set<ClassEntity> getClasses() {
        return Collections.unmodifiableSet(classes.keySet());
    }

    public int size() {
        return getWeightedSize(classes) + getWeightedSize(fields) + getWeightedSize(notOverrideMethods);
    }

    public int getWeight(String name) {
        return classes.getOrDefault(name, 0)
                + notOverrideMethods.getOrDefault(name, 0)
                + fields.getOrDefault(name, 0);
    }

    private int getWeightedSize(Map<?, Integer> m) {
        return m.values().stream().mapToInt(Integer::valueOf).sum();
    }

    public int sizeOfIntersection(RelevantProperties properties) {
        int result = 0;

        final BinaryOperator<Integer> bop = Math::min;
        result += sizeOfIntersectWeighted(classes, properties.classes, bop);
        result += sizeOfIntersectWeighted(notOverrideMethods, properties.notOverrideMethods, bop);
        result += sizeOfIntersectWeighted(overrideMethods, properties.overrideMethods, bop);
        result += sizeOfIntersectWeighted(fields, properties.fields, bop);

        return result;
    }

    private static int sizeOfIntersectWeighted(Map<?, Integer> m1, Map<?, Integer> m2, BinaryOperator<Integer> f) {
        return m1.entrySet().stream()
                .filter(e -> m2.containsKey(e.getKey()))
                .mapToInt(e -> f.apply(e.getValue(), m2.get(e.getKey())))
                .sum();
    }

    public int sizeOfUnion(RelevantProperties other) {
        int result = 0;

        final BinaryOperator<Integer> bop = Math::max;
        result += size() + other.size();
        result -= sizeOfIntersectWeighted(classes, other.classes, bop);
        result -= sizeOfIntersectWeighted(notOverrideMethods, other.notOverrideMethods, bop);
        result -= sizeOfIntersectWeighted(overrideMethods, other.overrideMethods, bop);
        result -= sizeOfIntersectWeighted(fields, other.fields, bop);
        return result;
    }

    public RelevantProperties copy() {
        final RelevantProperties copy = new RelevantProperties();
        copy.classes.putAll(classes);
        copy.overrideMethods.putAll(overrideMethods);
        copy.notOverrideMethods.putAll(notOverrideMethods);
        copy.fields.putAll(fields);
        return copy;
    }
}