package xyz.keksdose.spoon.langserver.codeactions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.google.auto.service.AutoService;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.cu.SourcePosition;
import spoon.reflect.visitor.filter.VariableAccessFilter;

@AutoService(RefactorVisitor.class)
public class UnusedVariable extends RefactorVisitor {

  private static final String COMMAND_NAME = "Remove unused field with assignment";
  private CodeAction result;

  @Override
  public <T> void visitCtLocalVariable(CtLocalVariable<T> localVariable) {
    if (model.getElements(new VariableAccessFilter<>(localVariable.getReference())).isEmpty()) {
      CodeAction action = new CodeAction();
      action.setTitle(COMMAND_NAME);
      WorkspaceEdit edit = new WorkspaceEdit();
      TextEdit remove = new TextEdit();
      remove.setNewText("");

      SourcePosition sourcePosition = localVariable.getPosition();
      Position start = getStartPosition(localVariable);
      Position end = new Position(sourcePosition.getEndLine() - 1, sourcePosition.getEndColumn());
      remove.setRange(new Range(start, end));
      edit.setChanges(Map.of(uri, List.of(remove)));
      action.setEdit(edit);
      result = action;
    }
  }

  private Position getStartPosition(CtLocalVariable<?> localVariable) {
    SourcePosition position = localVariable.getType().getPosition();
    return new Position(position.getLine() - 1, position.getColumn() - 1);
  }

  @Override
  public Optional<CodeAction> getResult() {
    return Optional.ofNullable(result);
  }
}
