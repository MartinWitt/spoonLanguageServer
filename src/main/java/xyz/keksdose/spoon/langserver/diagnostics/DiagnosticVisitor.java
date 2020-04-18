package xyz.keksdose.spoon.langserver.diagnostics;

import java.util.List;
import org.eclipse.lsp4j.Diagnostic;
import spoon.reflect.CtModel;
import spoon.reflect.visitor.CtScanner;

public abstract class DiagnosticVisitor extends CtScanner {

  protected CtModel model;
  protected String uri;
  public void init(CtModel model, String uri){
    this.model = model;
    this.uri = uri;
  }
  public abstract List<Diagnostic> getResult();
}
