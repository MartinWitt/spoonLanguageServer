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
import spoon.support.reflect.code.CtCommentImpl;

/**
 * Provides a code action adding the type of a local type inferred variable as a comment above the object.
 * <pre>
 * var object = new ArrayList<HashMap<Object,Boolean>>>
 * </pre>
 * will be transformed to: 
 * <pre>
 * // type is: ArrayList<HashMap<Object,Boolean>>>
 * var object = new ArrayList<HashMap<Object,Boolean>>>
 * </pre>
 */
@AutoService(ICodeAction.class)
public class AddTypeOverVar implements ICodeAction {


  private static final String COMMAND_NAME = "insert type as comment";

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
    CtLocalVariable<?> localVariable = findLocalVariable(model, uri, rangeList);

    CodeAction action = new CodeAction(COMMAND_NAME);
    TextEdit edit = new TextEdit();
    Position above = new Position(range.getStart().getLine(), range.getStart().getCharacter());
    edit.setRange(new Range(above, above));

    edit.setNewText(generateComment(localVariable));
    WorkspaceEdit documentEdit = new WorkspaceEdit();
    documentEdit.setChanges(Map.of(uri, List.of(edit)));
    action.setEdit(documentEdit);
    return action;
  }

  private String generateComment(CtLocalVariable<?> localVariable) {
    if (localVariable.getDefaultExpression().getType() != null) {
      StringBuilder content = new StringBuilder("type is: ");
      String genericType = findGenericType(localVariable);
      if (!genericType.isBlank()) {
        content.append(genericType);
      } else {
        content.append(localVariable.getDefaultExpression().getType().getQualifiedName());
      }
      CtCommentImpl comment = new CtCommentImpl();
      comment.setCommentType(CommentType.INLINE);
      comment.setContent(content.toString());
      return comment.toString() + "\n";
    }
    return "";
  }

  private CtLocalVariable<?> findLocalVariable(CtModel model, String uri,
      List<Position> rangeList) {
    return rangeList.stream()
        .map(v -> getExactMatch(model, uri, v))
        .flatMap(Optional::stream)
        .filter(v -> v instanceof CtLocalVariable)
        .map(v -> (CtLocalVariable<?>) v)
        .filter(v -> v.getDefaultExpression() != null)
        .distinct()
        .findAny()
        .get();
  }

  private String findGenericType(CtLocalVariable<?> localVariable) {
    return localVariable.getDefaultExpression()
        .getType()
        .getReferencedTypes()
        .stream()
        .max((o1, o2) -> Integer.compare(o1.toString().length(), o2.toString().length()))
        .map(v -> v.toString())
        .orElse("");
  }



}
