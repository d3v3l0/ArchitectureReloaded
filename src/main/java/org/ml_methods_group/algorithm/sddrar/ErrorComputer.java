/*
 * Copyright 2017 Machine Learning Methods in Software Engineering Group of JetBrains Research
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

package org.ml_methods_group.algorithm.sddrar;

import org.ml_methods_group.algorithm.sddrar.rules.Rule;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ErrorComputer {

    public static List<Integer> getFaultyEntities(DataSet dataSet, Set<Rule> rules, double percentageOfErrorsThreshold) {

        List<Integer> errors = getNumberOfErrors(dataSet, rules);
        List<Double> pe = getPercentageOfErrors(errors, rules.size());

        Set<Integer> potentiallyFaulty = IntStream
                .range(0, pe.size())
                .filter(e -> pe.get(e) > percentageOfErrorsThreshold)
                .boxed()
                .collect(Collectors.toSet());

        List<Integer> faulty;

        if (!potentiallyFaulty.isEmpty()) {
            double avg = new Mean().evaluate(potentiallyFaulty.stream().mapToDouble(errors::get).toArray());
            faulty = potentiallyFaulty.stream().filter(e -> errors.get(e) > avg).collect(Collectors.toList());

        } else {
            double[] nonZeroErrors = errors.stream().mapToDouble(e -> e).filter(e -> e > 0).toArray();
            double avg = new Mean().evaluate(nonZeroErrors);
            double dev = new StandardDeviation().evaluate(nonZeroErrors);
            double eps = avg + dev;
            faulty = IntStream.range(0, pe.size()).filter(e -> errors.get(e) > eps).boxed().collect(Collectors.toList());
        }

        return faulty;
    }

    static List<Double> getPercentageOfErrors(List<Integer> errors, int numberOfRules) {
        List<Double> pe = new ArrayList<>();
        for (int error : errors) {
            pe.add((double) error / numberOfRules);
        }
        return pe;
    }



    static List<Integer> getNumberOfErrors(DataSet dataSet, Set<Rule> rules) {

        List<Integer> errors = new ArrayList<>();

        for (double[] entity: dataSet.getMatrix().getData()) {
            int errorsForEntity = 0;
            for (Rule rule : rules) {
                if (!rule.check(entity)) {
                    errorsForEntity++;
                }
            }
            errors.add(errorsForEntity);
        }

        return errors;
    }

    public static Map<String, Set<Rule>> getFailedRulesForEachEntity(DataSet dataSet, Set<Rule> rules) {
        Map<String, Set<Rule>> result = new HashMap<>();

        for (int i = 0; i < dataSet.getMatrix().getRowDimension(); i++) {
            result.put(dataSet.getEntityNames().get(i), new HashSet<>());
        }

        for (int i = 0; i < dataSet.getMatrix().getRowDimension(); i++) {
            for (Rule rule : rules) {
                if (!rule.check(dataSet.getMatrix().getRow(i))) {
                    result.get(dataSet.getEntityNames().get(i)).add(rule);
                }
            }
        }

        return result;
    }
}
