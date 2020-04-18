package xyz.keksdose.spoon.langserver.codeactions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.google.auto.service.AutoService;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.support.reflect.eval.VisitorPartialEvaluator;

/**
 * Provides a code refactoring for inverting an if statement. This includes negating the condition.
 * <pre> 
 * if(condition) {
 *  doSomething();
 * } else {
 *  doElse();
 * }
 * </pre>
 * will be transformed to:
 * <pre> 
 * if(!condition) {
 *  doElse();
 * } else {
 *  doSomething();
 * }
 * </pre>
 */
@AutoService(RefactorVisitor.class)
public class InvertIfStatement extends RefactorVisitor {

  private CodeAction result;


  private void setChanges(String uri, WorkspaceEdit documentEdit, TextEdit edit) {
    Map<String, List<TextEdit>> changesByName = new HashMap<>();
    changesByName.put(uri, List.of(edit));
    documentEdit.setChanges(changesByName);
  }

  private void invertCode(CtIf ifStatement) {
    CtStatement ifBody = ifStatement.getThenStatement();
    CtStatement elseBody = ifStatement.getElseStatement();
    CtExpression<Boolean> condition = ifStatement.getCondition();

    CtUnaryOperator<Boolean> newCondition = condition.getFactory().createUnaryOperator();
    newCondition.setKind(UnaryOperatorKind.NOT);
    newCondition.setOperand(condition);
    newCondition.accept(new VisitorPartialEvaluator());

    ifStatement.setThenStatement(elseBody);
    ifStatement.setElseStatement(ifBody);
    ifStatement.setCondition(newCondition);
  }

  private void setNewRange(CtIf ifStatement, TextEdit edit) {
    Position newStart = new Position(ifStatement.getPosition().getLine() - 1,
        ifStatement.getPosition().getColumn() - 1);
    Position newEnd = new Position(ifStatement.getPosition().getEndLine() - 1,
        ifStatement.getPosition().getEndColumn());
    Range newRange = new Range(newStart, newEnd);
    edit.setRange(newRange);
  }

  @Override
  public Optional<CodeAction> getResult() {
    return Optional.ofNullable(result);
  }

  @Override
  public void visitCtIf(CtIf ifElement) {
    ifElement = ifElement.clone();
    CodeAction action = new CodeAction();
    action.setTitle("invert if statement");
    WorkspaceEdit documentEdit = new WorkspaceEdit();
    TextEdit edit = new TextEdit();

    invertCode(ifElement);
    String newText = ifElement.toString();
    edit.setNewText(newText);
    setNewRange(ifElement, edit);
    setChanges(uri, documentEdit, edit);
    action.setEdit(documentEdit);
    this.result = action;
  }
}
