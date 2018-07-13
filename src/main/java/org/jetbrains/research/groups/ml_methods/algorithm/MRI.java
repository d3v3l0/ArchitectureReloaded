package org.jetbrains.research.groups.ml_methods.algorithm;

import com.sixrr.metrics.MetricCategory;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.ClassEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.Entity;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.EntitySearchResult;
import org.jetbrains.research.groups.ml_methods.algorithm.entity.FieldEntity;
import org.jetbrains.research.groups.ml_methods.algorithm.refactoring.Refactoring;
import org.jetbrains.research.groups.ml_methods.config.Logging;
import org.jetbrains.research.groups.ml_methods.utils.AlgorithmsUtil;

import java.util.*;
import java.util.stream.Stream;

public class MRI extends Algorithm {
    private static final Logger LOGGER = Logging.getLogger(MRI.class);
    private static final double ACCURACY = 1;

    private final List<Entity> units = new ArrayList<>();
    private final Map<String, ClassEntity> classesByName = new HashMap<>();
    private final List<ClassEntity> classes = new ArrayList<>();

    public MRI() {
        super("MRI", true);
    }

    @Override
    protected List<Refactoring> calculateRefactorings(ExecutionContext context, boolean enableFieldRefactorings) {
        final EntitySearchResult searchResult = context.getEntities();
        units.clear();
        classes.clear();
        List<FieldEntity> fields = enableFieldRefactorings ? searchResult.getFields() : Collections.emptyList();
        Stream.of(searchResult.getMethods(), fields)
                .flatMap(List::stream)
                .filter(Entity::isMovable)
                .forEach(units::add);

        searchResult.getClasses()
                .stream()
                .map(ClassEntity::copy) // create local copies
                .peek(entity -> classesByName.put(entity.getName(), entity))
                .forEach(classes::add);

        final List<Refactoring> refactorings = new ArrayList<>();

        int progress = 0;
        for (Entity currentEntity : units) {
            context.checkCanceled();
            final Holder minHolder = runParallel(classes, context, Holder::new,
                    (candidate, holder) -> getNearestClass(currentEntity, candidate, holder), this::min);
            progress++;
            reportProgress((double) progress / units.size(), context);
            if (minHolder.candidate == null) {
                LOGGER.warn(currentEntity.getName() + " has no nearest class");
                continue;
            }
            final ClassEntity nearestClass = minHolder.candidate;
            if (nearestClass.getName().equals(currentEntity.getClassName())) {
                continue;
            }

            final double accuracyRating =
                    AlgorithmsUtil.getGapBasedAccuracyRating(minHolder.distance, minHolder.difference) * ACCURACY;
            if (currentEntity.getCategory() == MetricCategory.Method) {
                processMethod(refactorings, currentEntity, nearestClass, accuracyRating, context);
            } else {
                refactorings.add(Refactoring.createRefactoring(currentEntity.getName(), nearestClass.getName(), accuracyRating,
                        currentEntity.isField(), context.getScope()));
            }
        }

        return refactorings;
    }

    @Nullable
    private Holder getNearestClass(Entity entity, ClassEntity targetClass, Holder holder) {
        final double distance = entity.distance(targetClass);
        if (holder.distance > distance) {
            holder.difference = holder.distance - distance;
            holder.distance = distance;
            holder.candidate = targetClass;
        } else if (distance - holder.distance < holder.difference) {
            holder.difference = distance - holder.distance;
        }
        return holder;
    }

    private class Holder {
        private double distance = Double.POSITIVE_INFINITY;
        private double difference = Double.POSITIVE_INFINITY;
        private ClassEntity candidate;
    }

    private Holder min(Holder first, Holder second) {
        if (first.distance > second.distance) {
            return min(second, first);
        }
        first.difference = Math.min(second.distance - first.distance, first.difference);
        return first;
    }

    private void processMethod(List<Refactoring> refactorings, Entity method, ClassEntity target, double accuracy, ExecutionContext context) {
        if (method.isMovable()) {
            final ClassEntity containingClass = classesByName.get(method.getClassName());
            refactorings.add(Refactoring.createRefactoring(method.getName(), target.getName(), accuracy, false, context.getScope()));
            containingClass.removeFromClass(method.getName());
            target.addToClass(method.getName());
        }
    }
}