package xyz.keksdose.spoon.langserver;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.DidSaveTextDocumentParams;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.TextDocumentService;
import spoon.SpoonException;

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
      client.showMessage(new MessageParams(MessageType.Error, msg));
    }
    Logger.getAnonymousLogger().info(msg);
  }

  @Override
  public void didOpen(DidOpenTextDocumentParams params) {
    System.out.println("FullTextDocumentService.didOpen()");
    documents.put(params.getTextDocument().getUri(), params.getTextDocument());
    try {
      Set<File> paths = documents.keySet().stream().map(t -> {
        try {
          return new URI(t);
        } catch (URISyntaxException e) {
          reportError(e.getMessage());
          return null;
        }
      }).filter(v -> v != null).map(Path::of).map(Path::toFile).collect(Collectors.toSet());
      compiler.addFile(paths);
    } catch (SpoonException e) {
      reportError(e.getMessage());
    }
  }

  @Override
  public void didChange(DidChangeTextDocumentParams params) {
    System.out.println("FullTextDocumentService.didChange()");
    String uri = params.getTextDocument().getUri();
    for (TextDocumentContentChangeEvent changeEvent : params.getContentChanges()) {
      // Will be full update because we specified that is all we support
      if (changeEvent.getRange() != null) {
        throw new UnsupportedOperationException("Range should be null for full document update.");
      }
      if (changeEvent.getRangeLength() != null) {
        throw new UnsupportedOperationException(
            "RangeLength should be null for full document update.");
      }

      documents.get(uri).setText(changeEvent.getText());
      try {
        Set<File> paths = documents.keySet().stream().map(t -> {
          try {
            return new URI(t);
          } catch (URISyntaxException e) {
            reportError(e.getMessage());
            return null;
          }
        }).filter(v -> v != null).map(Path::of).map(Path::toFile).collect(Collectors.toSet());
        compiler.addFile(paths);
      } catch (SpoonException e) {
        reportError(e.getMessage());
      }
    }
  }

  @Override
  public void didClose(DidCloseTextDocumentParams params) {
    String uri = params.getTextDocument().getUri();
    documents.remove(uri);
  }

  @Override
  public void didSave(DidSaveTextDocumentParams params) {
    System.out.println("FullTextDocumentService.enclosing_method()");
  }

  public void setClient(LanguageClient client) {
    this.client = client;
  }

}
