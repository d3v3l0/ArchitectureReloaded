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

package org.ml_methods_group.algorithm.refactoring;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class MoveToClassRefactoring extends Refactoring {
    public MoveToClassRefactoring(
        final @NotNull PsiElement entity,
        final @NotNull PsiElement target,
        final double accuracy
    ) {
        super(entity, target, accuracy);
    }

    /**
     * Returns class which contains moved entity.
     */
    public abstract @Nullable PsiClass getContainingClass();

    /**
     * Returns class in which entity is placed in this refactoring
     */
    public abstract @NotNull PsiClass getTargetClass();
}
