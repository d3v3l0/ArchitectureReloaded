package org.jetbrains.research.groups.ml_methods.extraction.refactoring.writers;

import org.jetbrains.research.groups.ml_methods.extraction.refactoring.JBRefactoringTextRepresentation;
import org.jetbrains.research.groups.ml_methods.extraction.refactoring.RefactoringTextRepresentation;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class JBWriterTest {
    private final static Path REFACTORINGS_PATH_TO_COMPARE =  Paths.get("./src/test/resources/JBRefactorings");
    private final static List<RefactoringTextRepresentation> REFACTORINGS_TO_WRITE = Arrays.asList(
            new JBRefactoringTextRepresentation(
                    "org.jhotdraw.samples.svg.gui.ViewToolBar", "setEditor",
                    Collections.singletonList("int"), "org.jhotdraw.samples.svg.SVGDrawingPanel"
            ),
            new JBRefactoringTextRepresentation(
                    "org.jhotdraw.samples.svg.io.SVGInputFormat", "readTransformAttribute",
                    Collections.emptyList(), "org.jhotdraw.draw.AttributeKeys"
            ),
            new JBRefactoringTextRepresentation(
                    "net.n3.nanoxml.XMLElement", "print",
                    Arrays.asList("java.lang.Integer", "java.util.List<java.lang.Integer>"), "org.jhotdraw.xml.NanoXMLDOMOutput"
            )
    );

    @Test
    public void writeToFile() throws IOException {
        Path fileToWrite = Files.createTempFile(null, null);
        fileToWrite.toFile().deleteOnExit();
        RefactoringsWriters.getJBWriter().writeRefactoringsInTextForm(REFACTORINGS_TO_WRITE, fileToWrite);
        checkWrittenRefactorings(fileToWrite.toFile());
    }

    @Test
    public void writeToOutputStream() throws IOException {
        Path fileToWrite = Files.createTempFile(null, null);
        fileToWrite.toFile().deleteOnExit();
        RefactoringsWriters.getJBWriter().writeRefactoringsInTextForm(REFACTORINGS_TO_WRITE,
                new FileOutputStream(fileToWrite.toFile()));
        checkWrittenRefactorings(fileToWrite.toFile());
    }

    private void checkWrittenRefactorings(File writtenFile) throws IOException {
        assertEquals(Files.readAllLines(writtenFile.toPath()), Files.readAllLines(REFACTORINGS_PATH_TO_COMPARE));
    }
}