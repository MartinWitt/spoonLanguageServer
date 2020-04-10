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

@AutoService(ICodeAction.class)
public class FlipIfStatement implements ICodeAction {

  @Override
  public boolean isApplicable(CtModel model, String uri, Range range, TextDocumentItem document) {
    List<Position> rangeList = PositionUtil.convertRange(range, document);
    var foo = rangeList.stream().map(v -> getExactMatch(model, uri, v)).flatMap(Optional::stream)
        .collect(Collectors.toList());
    List<CtIf> localVariables = rangeList.stream().map(v -> getExactMatch(model, uri, v))
        .flatMap(Optional::stream).filter(v -> v instanceof CtIf).map(v -> (CtIf) v)
        .filter(v -> v.getElseStatement() != null).collect(Collectors.toList());
    return localVariables.size() == 1;
  }

  @Override
  public CodeAction apply(CtModel model, String uri, Range range, TextDocumentItem document) {
    List<Position> rangeList = PositionUtil.convertRange(range, document);
    CtIf ifStatement = rangeList.stream().map(v -> getClosestMatch(model, uri, v))
        .flatMap(Optional::stream).filter(v -> v instanceof CtIf).map(v -> (CtIf) v)
        .filter(v -> v.getElseStatement() != null).findAny().get();
    var action = new CodeAction();
    action.setTitle("swap if bodies");
    WorkspaceEdit documentEdit = new WorkspaceEdit();
    TextEdit edit = new TextEdit();
    CtStatement ifBody = ifStatement.getThenStatement();
    CtStatement elseBody = ifStatement.getElseStatement();
    CtExpression<Boolean> condition = ifStatement.getCondition();
    //TODO: swap condition
    ifStatement.setThenStatement(elseBody);
    ifStatement.setElseStatement(ifBody);
    edit.setNewText(ifStatement.toString());
    Position newStart = new Position(ifStatement.getPosition().getLine() - 1,
        ifStatement.getPosition().getColumn() - 1);
    Position newEnd = new Position(ifStatement.getPosition().getEndLine() - 1,
        ifStatement.getPosition().getEndColumn());
    Range newRange = new Range(newStart, newEnd);
    edit.setRange(newRange);
    Map<String, List<TextEdit>> changesByName = new HashMap<>();
    changesByName.put(uri, List.of(edit));
    documentEdit.setChanges(changesByName);
    action.setEdit(documentEdit);
    return action;
  }
}
