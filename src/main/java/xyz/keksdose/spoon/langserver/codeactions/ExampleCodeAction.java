package xyz.keksdose.spoon.langserver.codeactions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.auto.service.AutoService;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;

@AutoService(ICodeAction.class)
public class ExampleCodeAction implements ICodeAction {

  @Override
  public boolean isApplicable(CtModel model, String uri, Range range, TextDocumentItem document) {
    return getExactMatch(model, uri, range.getStart()).isPresent();
  }

  @Override
  public CodeAction apply(CtModel model, String uri, Range range, TextDocumentItem document) {
    CtElement element = getExactMatch(model, uri, range.getStart()).get();
    var action = new CodeAction();
    action.setTitle("insert spoon string");
    WorkspaceEdit documentEdit = new WorkspaceEdit();
    TextEdit edit = new TextEdit();
    edit.setRange(range);
    edit.setNewText("//spoon generates new text here\n" + element.toString());
    Map<String, List<TextEdit>> changesByName = new HashMap<>();
    changesByName.put(uri, List.of(edit));
    documentEdit.setChanges(changesByName);
    action.setEdit(documentEdit);
    return action;
  }



}
