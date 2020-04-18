package xyz.keksdose.spoon.langserver;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.DidChangeConfigurationParams;
import org.eclipse.lsp4j.DidChangeWatchedFilesParams;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

public class SpoonLangServer implements LanguageServer, LanguageClientAware {

  private LanguageClient client;
  private String workspaceRoot;

  private FullTextDocumentService fullTextDocumentService = new FullTextDocumentService();

  @Override
  public void connect(LanguageClient client) {
    this.client = client;
    client.showMessage(new MessageParams(MessageType.Info, "Spoon connected"));
    fullTextDocumentService.setClient(client);
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    ServerCapabilities capabilities = new ServerCapabilities();
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
    capabilities.setCodeActionProvider(true);
    capabilities.setHoverProvider(true);
    //capabilities.setCodeLensProvider(new CodeLensOptions(true));
    return CompletableFuture.completedFuture(new InitializeResult(capabilities));
  }

  @Override
  public CompletableFuture<Object> shutdown() {
    return CompletableFuture.completedFuture(null);
  }

  @Override
  public void exit() {
  }

  @Override
  public TextDocumentService getTextDocumentService() {
    return fullTextDocumentService;

  }

  @Override
  public WorkspaceService getWorkspaceService() {
    return new WorkspaceService() {

      @Override
      public void didChangeConfiguration(DidChangeConfigurationParams params) {

      }

      @Override
      public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
      }
    };
  }


}
