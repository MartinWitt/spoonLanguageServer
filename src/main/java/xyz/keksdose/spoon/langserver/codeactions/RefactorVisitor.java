package xyz.keksdose.spoon.langserver.codeactions;

import java.util.Optional;
import org.eclipse.lsp4j.CodeAction;
import spoon.reflect.CtModel;
import spoon.reflect.visitor.CtScanner;

public abstract class RefactorVisitor extends CtScanner {

  protected CtModel model;
  protected String uri;
  public void init(CtModel model, String uri){
    this.model = model;
    this.uri = uri;
  }
  public abstract Optional<CodeAction> getResult();
}