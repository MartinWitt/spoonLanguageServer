package xyz.keksdose.spoon.langserver.codeactions;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Range;
import spoon.reflect.CtModel;

public class CodeActionManager {

  private ServiceLoader<ICodeAction> loader;

  public CodeActionManager() {
    loader = ServiceLoader.load(ICodeAction.class);
  }

  public List<CodeAction> getAvailable(CtModel model, String uri, Range range) {
    Iterable<ICodeAction> iterable = () -> loader.iterator();
    return StreamSupport.stream(iterable.spliterator(), false)
        .filter(v -> v.isApplicable(model, uri, range)).map(v -> v.apply(model, uri, range))
        .collect(Collectors.toList());
  }

}