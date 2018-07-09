/*
 * Copyright 2018 Machine Learning Methods in Software Engineering Group of JetBrains Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ml_methods_group.refactoring.logging;

import com.sixrr.metrics.MetricCategory;
import com.sixrr.metrics.metricModel.MetricsResult;
import com.sixrr.metrics.metricModel.MetricsRun;
import com.sixrr.metrics.utils.MethodUtils;
import org.jetbrains.annotations.NotNull;
import org.ml_methods_group.algorithm.refactoring.MoveMethodRefactoring;

import java.util.ArrayList;
import java.util.List;

/**
 * Subclass of {@link RefactoringFeatures} that contains features of a
 * {@link MoveMethodRefactoring}.
 */
public class MoveMethodRefactoringFeatures extends RefactoringFeatures {
    private final @NotNull List<MetricCalculationResult> targetClassMetricsValues;

    private final @NotNull List<MetricCalculationResult> methodMetricsValues;

    /**
     * Extracts features from a given {@link MoveMethodRefactoring}.
     *
     * @param refactoring a {@link MoveMethodRefactoring} to extract features from.
     * @param metricsRun a result of metrics calculations. Some of metrics values calculated for
     *                   objects given refactoring operates on can be used to extract refactoring
     *                   features.
     */
    public MoveMethodRefactoringFeatures(
        final @NotNull MoveMethodRefactoring refactoring,
        final @NotNull MetricsRun metricsRun
    ) {
        MetricsResult resultsForClasses = metricsRun.getResultsForCategory(MetricCategory.Class);
        MetricsResult resultsForMethods = metricsRun.getResultsForCategory(MetricCategory.Method);

        targetClassMetricsValues = extractMetricsResultsFor(
            refactoring.getTargetClass().getQualifiedName(),
            resultsForClasses
        );

        methodMetricsValues = extractMetricsResultsFor(
            MethodUtils.calculateSignature(refactoring.getMethod()),
            resultsForMethods
        );
    }

    /**
     * Returns {@link List} of {@link MetricCalculationResult} with all extracted metrics values for
     * a target class of initial {@link MoveMethodRefactoring}.
     */
    public @NotNull List<MetricCalculationResult> getTargetClassMetricsValues() {
        return new ArrayList<>(targetClassMetricsValues);
    }

    /**
     * Returns {@link List} of {@link MetricCalculationResult} with all extracted metrics values for
     * a method that is moved in initial {@link MoveMethodRefactoring}.
     */
    public @NotNull List<MetricCalculationResult> getMethodMetricsValues() {
        return new ArrayList<>(methodMetricsValues);
    }

    @NotNull
    @Override
    public <R> R accept(@NotNull RefactoringFeaturesVisitor<R> visitor) {
        return visitor.visit(this);
    }
}