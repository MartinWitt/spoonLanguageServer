package xyz.keksdose.spoon.langserver;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;
import spoon.SpoonException;
import spoon.reflect.declaration.CtElement;
import spoon.support.compiler.VirtualFile;
import xyz.keksdose.spoon.langserver.codeactions.PositionUtil;

/**
 * `TextDocumentService` that only supports `TextDocumentSyncKind.Full` updates.
 *  Override members to add functionality.
 */
public class FullTextDocumentService implements TextDocumentService {

  protected HashMap<String, TextDocumentItem> documents = new HashMap<>();
  protected Compiler compiler;
  protected LanguageClient client;

  public FullTextDocumentService() {
    compiler = new Compiler();
  }

  public void reportError(String msg) {
    if (client != null) {
      // client.showMessage(new MessageParams(MessageType.Error, msg));
    }
    Logger.getAnonymousLogger().info(msg);
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    documents.put(params.getTextDocument().getUri(), params.getTextDocument());
    try {
      Set<VirtualFile> files = documents.entrySet().stream().map(t ->
        {
          try {
            URI uri = new URI(t.getKey());
            String name = Path.of(uri).getFileName().toString();
            return new VirtualFile(t.getValue().getText(), name);
          } catch (URISyntaxException e) {
            reportError(e.getMessage());
            return null;
          }
        }).filter(v -> v != null).collect(Collectors.toSet());
      compiler.addFile(files);
    } catch (SpoonException e) {
      reportError(e.getMessage());
    }
    createDiagnostics(params.getTextDocument().getUri());

  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    // lets try a save only because first goal is a refactor language server.
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {
    String uri = params.getTextDocument().getUri();
    documents.remove(uri);
  }

  @Override
  public void didSave(DidSaveTextDocumentParams params) {

    documents.get(params.getTextDocument().getUri()).setText(params.getText());
    try {
      Set<VirtualFile> files = documents.entrySet().stream().map(t ->
        {
          try {
            URI uri = new URI(t.getKey());
            String name = Path.of(uri).getFileName().toString();
            return new VirtualFile(t.getValue().getText(), name);
          } catch (URISyntaxException e) {
            reportError(e.getMessage());
            return null;
          }
        }).filter(v -> v != null).collect(Collectors.toSet());
      compiler.addFile(files);
    } catch (SpoonException e) {
      reportError(e.getMessage());
    }
    createDiagnostics(params.getTextDocument().getUri());
  }

  public void setClient(LanguageClient client) {
    this.client = client;
  }

  @Override
  public CompletableFuture<Hover> hover(HoverParams params) {
    Optional<CtElement> element = PositionUtil.getClosestMatch(compiler.getModel(),
        params.getTextDocument().getUri(), params.getPosition());
    if (element.isEmpty()) {
      return CompletableFuture
          .completedFuture(new Hover(new MarkupContent(MarkupKind.PLAINTEXT, "hover failed")));
    }
    Hover hover = new Hover(new MarkupContent(MarkupKind.MARKDOWN,
        "**spoon sees** " + element.get().getClass().toString()));
    return CompletableFuture.completedFuture(hover);
  }



  @Override
  public CompletableFuture<List<Either<Command, CodeAction>>> codeAction(CodeActionParams params) {
    String uri = params.getTextDocument().getUri();
    List<CodeAction> actions = new ArrayList<>(new CodeActionManager().getAvailableCodeActions(compiler.getModel(), uri,
        params.getRange(), documents.get(uri)));
    List<Either<Command, CodeAction>> resultList = new ArrayList<>();
    for (CodeAction codeAction : actions) {
      resultList.add(Either.forRight(codeAction));
    }
    return CompletableFuture.completedFuture(resultList);
  }

  private void createDiagnostics(String uri) {
    TextDocumentItem doc = documents.get(uri);
    int lastChar = doc.getText().lines().reduce("", (o1, o2) -> o2).length();
    int lastLine = (int) doc.getText().lines().count();
    Position start = new Position(0, 0);
    Position end = new Position(lastLine, lastChar);
    Range range = new Range(start, end);
    List<Diagnostic> result = new ArrayList<>(new CodeActionManager().getAvailableHighlight(compiler.getModel(),
        uri, range, documents.get(uri)));
    client.publishDiagnostics(new PublishDiagnosticsParams(uri, result));
  }


}
