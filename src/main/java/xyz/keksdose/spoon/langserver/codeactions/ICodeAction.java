package xyz.keksdose.spoon.langserver.codeactions;

import java.util.Optional;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;

public interface ICodeAction {

  default Optional<CtElement> getExactMatch(CtModel model, String uri, Position position) {
    return PositionUtil.getExactMatch(model, uri, position);
  }

  default Optional<CtElement> getClosestMatch(CtModel model, String uri, Position position) {
    return PositionUtil.getClosestMatch(model, uri, position);
  }

  public boolean isApplicable(CtModel model, String uri, Range range, TextDocumentItem document);

  public CodeAction apply(CtModel model, String uri, Range range, TextDocumentItem document);
}
