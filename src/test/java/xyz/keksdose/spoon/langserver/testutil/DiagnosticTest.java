package xyz.keksdose.spoon.langserver.testutil;

import static org.junit.jupiter.api.Assertions.fail;
import static xyz.keksdose.spoon.langserver.testutil.DiagnosticAsserts.assertAbsentDiagnostic;
import static xyz.keksdose.spoon.langserver.testutil.DiagnosticAsserts.assertPresentDiagnostic;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.support.compiler.VirtualFile;
import xyz.keksdose.spoon.langserver.codeactions.PositionUtil;
import xyz.keksdose.spoon.langserver.diagnostics.DiagnosticVisitor;

/**
 * Defines util methods for testing diagnostics.
 * {@see #diagnosticAbsent(String, Diagnostic, DiagnosticVisitor)}
 * {@see #diagnosticExpected(String, Diagnostic, DiagnosticVisitor)}
 */
public class DiagnosticTest {

  private static TextDocumentItem createDocument(String className, String content) {
    return new TextDocumentItem(className, "java", 0, content);
  }


  private static Range generateSpanningRange(TextDocumentItem doc) {
    int lastChar = doc.getText().lines().reduce("", (o1, o2) -> o2).length();
    int lastLine = (int) doc.getText().lines().count();
    Position start = new Position(0, 0);
    Position end = new Position(lastLine, lastChar);
    Range range = new Range(start, end);
    return range;
  }

  public static void diagnosticExpected(String path, Diagnostic expected,
      DiagnosticVisitor visitor) {
    String className = Path.of(path).getFileName().toString().replace("\\.java", "");
    String content = readResource(path);
    String uri = Path.of(className).toUri().toString();
    TextDocumentItem document = createDocument(uri, content);
    Launcher launcher = new Launcher();
    Range range = generateSpanningRange(document);
    launcher.getEnvironment().setNoClasspath(true);
    launcher.getEnvironment().setAutoImports(true);
    launcher.addInputResource(new VirtualFile(content));
    CtModel model = launcher.buildModel();
    List<Position> positions = PositionUtil.convertRange(range, document);
    Set<CtElement> elements = new HashSet<>();
    for (Position position : positions) {
      PositionUtil.getExactMatch(model, uri, position).ifPresent(elements::add);
    }
    Set<Diagnostic> available = new HashSet<>();
    visitor.init(model, uri);
    for (CtElement ctElement : elements) {
      ctElement.accept(visitor);
      available.addAll(visitor.getResult());
    }
    assertPresentDiagnostic(available, expected);
  }

  public static void diagnosticAbsent(String path, Diagnostic absent, DiagnosticVisitor visitor) {
    String className = Path.of(path).getFileName().toString().replace("\\.java", "");
    String content = readResource(path);
    String uri = Path.of(className).toUri().toString();
    TextDocumentItem document = createDocument(uri, content);
    Launcher launcher = new Launcher();
    Range range = generateSpanningRange(document);
    launcher.getEnvironment().setNoClasspath(true);
    launcher.getEnvironment().setAutoImports(true);
    launcher.addInputResource(new VirtualFile(content));
    CtModel model = launcher.buildModel();
    List<Position> positions = PositionUtil.convertRange(range, document);
    Set<CtElement> elements = new HashSet<>();
    for (Position position : positions) {
      PositionUtil.getExactMatch(model, uri, position).ifPresent(elements::add);
    }
    Set<Diagnostic> available = new HashSet<>();
    visitor.init(model, uri);
    for (CtElement ctElement : elements) {
      ctElement.accept(visitor);
      available.addAll(visitor.getResult());
    }
    assertAbsentDiagnostic(available, absent);
  }

  private static String readResource(String fileName) {
    Path resourceDirectory = Paths.get("src", "test", "resources", fileName);
    File f = resourceDirectory.toFile();
    try (InputStreamReader isr = new InputStreamReader(new FileInputStream(f));
        BufferedReader reader = new BufferedReader(isr)) {
      return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
    catch(IOException e) {
      fail(e);
      return "";
    }
  }
  
}