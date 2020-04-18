package xyz.keksdose.spoon.langserver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import xyz.keksdose.spoon.langserver.codeactions.PositionUtil;
import xyz.keksdose.spoon.langserver.codeactions.RefactorVisitor;
import xyz.keksdose.spoon.langserver.diagnostics.DiagnosticVisitor;

public class CodeActionManager {

  private ServiceLoader<RefactorVisitor> refactorLoader;
  private ServiceLoader<DiagnosticVisitor> diagnosticLoader;

  public CodeActionManager() {
    refactorLoader = ServiceLoader.load(RefactorVisitor.class);
    diagnosticLoader = ServiceLoader.load(DiagnosticVisitor.class);
  }

  public Set<CodeAction> getAvailableCodeActions(CtModel model, String uri, Range range,
      TextDocumentItem document) {
    List<Position> positions = PositionUtil.convertRange(range, document);
    Set<CtElement> elements = new HashSet<>();
    for (Position position : positions) {
      PositionUtil.getExactMatch(model, uri, position).ifPresent(elements::add);;
    }

    Set<CodeAction> available = new HashSet<>();
    List<RefactorVisitor> visitor = new ArrayList<>();
    refactorLoader.iterator().forEachRemaining(v ->
      {
        v.init(model, uri);
        visitor.add(v);
      });
    for (CtElement ctElement : elements) {
      for (RefactorVisitor refactorVisitor : visitor) {
        ctElement.accept(refactorVisitor);
        refactorVisitor.getResult().ifPresent(available::add);
      }
    }
    return available;
  }

  public Set<Diagnostic> getAvailableHighlight(CtModel model, String uri, Range range,
      TextDocumentItem document) {
    List<Position> positions = PositionUtil.convertRange(range, document);
    Set<CtElement> elements = new HashSet<>();
    for (Position position : positions) {
      PositionUtil.getExactMatch(model, uri, position).ifPresent(elements::add);;
    }
    Set<Diagnostic> available = new HashSet<>();
    List<DiagnosticVisitor> visitor = new ArrayList<>();
    diagnosticLoader.iterator().forEachRemaining(v ->
      {
        v.init(model, uri);
        visitor.add(v);
      });
    for (CtElement ctElement : elements) {
      for (DiagnosticVisitor diagnosticVisitor : visitor) {
        ctElement.accept(diagnosticVisitor);
        available.addAll(diagnosticVisitor.getResult());
      }
    }
    return available;
  }
}
