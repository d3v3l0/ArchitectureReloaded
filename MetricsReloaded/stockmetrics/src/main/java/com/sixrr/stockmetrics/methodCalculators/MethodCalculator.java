/*
 * Copyright 2005-2016 Sixth and Red River Software, Bas Leijdekkers
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.sixrr.stockmetrics.methodCalculators;

import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiMethod;
import com.sixrr.stockmetrics.execution.BaseMetricsCalculator;

public abstract class MethodCalculator extends BaseMetricsCalculator {
    void postMetric(PsiMethod method, int numerator, int denominator) {
        resultsHolder.postMethodMetric(metric, method, (double) numerator, (double) denominator);
    }

    void postMetric(PsiMethod method, int value) {
        resultsHolder.postMethodMetric(metric, method, (double) value);
    }

    void postMetric(PsiMethod method, double value) {
        resultsHolder.postMethodMetric(metric, method, value);
    }

    public void processMethod(final PsiMethod method) {
        ProgressManager.getInstance().runProcess(new Runnable() {
            @Override
            public void run() {
                method.accept(visitor);
            }
        }, new EmptyProgressIndicator());
    }
}