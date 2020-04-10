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
import spoon.reflect.code.CtComment.CommentType;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.declaration.CtElement;
import spoon.support.reflect.code.CtCommentImpl;

@AutoService(ICodeAction.class)
public class AddTypeOverVar implements ICodeAction {

  @Override
  public boolean isApplicable(CtModel model, String uri, Range range, TextDocumentItem document) {
    List<Position> rangeList = PositionUtil.convertRange(range, document);
    List<CtLocalVariable<?>> localVariables = rangeList.stream()
        .map(v -> getExactMatch(model, uri, v))
        .flatMap(Optional::stream)
        .filter(v -> v instanceof CtLocalVariable)
        .map(v -> (CtLocalVariable<?>) v)
        .filter(v -> v.getDefaultExpression() != null)
        .distinct()
        .collect(Collectors.toList());
    return localVariables.size() == 1
        && localVariables.get(0).getDefaultExpression().getType() != null;
  }

  @Override
  public CodeAction apply(CtModel model, String uri, Range range, TextDocumentItem document) {
    List<Position> rangeList = PositionUtil.convertRange(range, document);
    CtLocalVariable<?> localVariable = rangeList.stream()
        .map(v -> getExactMatch(model, uri, v))
        .flatMap(Optional::stream)
        .filter(v -> v instanceof CtLocalVariable)
        .map(v -> (CtLocalVariable<?>) v)
        .filter(v -> v.getDefaultExpression() != null)
        .distinct()
        .findAny()
        .get();
    var action = new CodeAction();
    action.setTitle("insert type as comment");
    WorkspaceEdit documentEdit = new WorkspaceEdit();
    TextEdit edit = new TextEdit();
    Position above = new Position(range.getStart().getLine() - 1, range.getStart().getCharacter());
    edit.setRange(new Range(above, above));
    if (localVariable.getDefaultExpression().getType() != null) {
      String content = "the type is: "
          + localVariable.getDefaultExpression().getType().getQualifiedName() + "\n";
      CtCommentImpl comment = new CtCommentImpl();
      comment.setCommentType(CommentType.INLINE);
      comment.setContent(content);
      String result = comment.toString();
      edit.setNewText(result);
    }
    Map<String, List<TextEdit>> changesByName = new HashMap<>();
    changesByName.put(uri, List.of(edit));
    documentEdit.setChanges(changesByName);
    action.setEdit(documentEdit);
    return action;
  }



}
