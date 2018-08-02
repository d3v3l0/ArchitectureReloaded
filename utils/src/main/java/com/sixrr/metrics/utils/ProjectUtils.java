package com.sixrr.metrics.utils;

import com.intellij.ide.impl.ProjectUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectRootManager;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class ProjectUtils {
    private ProjectUtils() {
    }

    @Nullable
    public static Project loadProjectWithAllDependencies(Path projectPath) {
        Project projectToClose = ProjectManager.getInstance().getOpenProjects().length != 0 ?
                ProjectManager.getInstance().getOpenProjects()[0] : null;
        final Project project = ProjectUtil.openOrImport(projectPath.toAbsolutePath().toString(),
                projectToClose, true);
        return project == null ||
                ProjectRootManager.getInstance(project).getProjectSdk() == null ||
                !project.isInitialized() ?
                null : project;
    }
}