package xyz.keksdose.spoon.langserver.diagnostics;

import java.util.ArrayList;
import java.util.List;
import com.google.auto.service.AutoService;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.visitor.filter.VariableAccessFilter;

@AutoService(DiagnosticVisitor.class)
public class UnusedVariable extends DiagnosticVisitor {

  private List<Diagnostic> result = new ArrayList<>();

  @Override
  public List<Diagnostic> getResult() {
    return result;
  }

  @Override
  public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {
    if (model.getElements(new VariableAccessFilter<>(localVariable.getReference())).isEmpty()) {
      Diagnostic diagnostic = new Diagnostic();
      SourcePosition sourcePosition = localVariable.getPosition();
      Position start = new Position(sourcePosition.getLine() - 1, sourcePosition.getColumn() - 1);
      Position end =
          new Position(sourcePosition.getEndLine() - 1, sourcePosition.getEndColumn() - 1);
      diagnostic.setRange(new Range(start, end));
      diagnostic.setMessage("unused variable found");
      diagnostic.setSeverity(DiagnosticSeverity.Warning);
      result.add(diagnostic);
    }
  }


  
}