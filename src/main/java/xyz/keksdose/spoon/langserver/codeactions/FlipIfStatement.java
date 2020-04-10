package xyz.keksdose.spoon.langserver.codeactions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.google.auto.service.AutoService;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtIf;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtUnaryOperator;
import spoon.reflect.code.UnaryOperatorKind;
import spoon.support.reflect.eval.VisitorPartialEvaluator;

@AutoService(ICodeAction.class)
public class FlipIfStatement implements ICodeAction {

  @Override
  public boolean isApplicable(CtModel model, String uri, Range range, TextDocumentItem document) {
    List<Position> rangeList = PositionUtil.convertRange(range, document);
    List<CtIf> localVariables = rangeList.stream()
        .map(v -> getExactMatch(model, uri, v))
        .flatMap(Optional::stream)
        .filter(v -> v instanceof CtIf)
        .map(v -> (CtIf) v)
        .filter(v -> v.getElseStatement() != null)
        .collect(Collectors.toList());
    return localVariables.size() == 1;
  }

  @Override
  public CodeAction apply(CtModel model, String uri, Range range, TextDocumentItem document) {
    List<Position> rangeList = PositionUtil.convertRange(range, document);
    CtIf ifStatement = rangeList.stream()
        .map(v -> getClosestMatch(model, uri, v))
        .flatMap(Optional::stream)
        .filter(v -> v instanceof CtIf)
        .map(v -> (CtIf) v)
        .filter(v -> v.getElseStatement() != null)
        .findAny()
        .get();

    CodeAction action = new CodeAction();
    action.setTitle("invert if statement");
    WorkspaceEdit documentEdit = new WorkspaceEdit();
    TextEdit edit = new TextEdit();

    invertCode(ifStatement);
    edit.setNewText(ifStatement.toString());
    setNewRange(ifStatement, edit);
    setChanges(uri, documentEdit, edit);
    action.setEdit(documentEdit);
    return action;
  }

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
}
