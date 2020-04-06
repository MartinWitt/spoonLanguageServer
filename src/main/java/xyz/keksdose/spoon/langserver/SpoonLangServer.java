package xyz.keksdose.spoon.langserver;

import java.util.concurrent.CompletableFuture;
import org.eclipse.lsp4j.CompletionOptions;
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

  private LanguageClient client = null;
  private String workspaceRoot = null;

  private FullTextDocumentService fullTextDocumentService = new FullTextDocumentService();

  @Override
  public void connect(LanguageClient client) {
    this.client = client;
    client.showMessage(new MessageParams(MessageType.Info, "Spoon verbunden"));
    fullTextDocumentService.setClient(client);
  }

  @Override
  public CompletableFuture<InitializeResult> initialize(InitializeParams params) {
    //workspaceRoot = params.getRootUri();
    // var test = params.getCapabilities().getTextDocument();
    // System.out.println(String.valueOf(workspaceRoot));
    ServerCapabilities capabilities = new ServerCapabilities();
    capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
    capabilities.setCodeActionProvider(false);
    //capabilities.setCompletionProvider(new CompletionOptions(true, null));
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
    // TODO Auto-generated method stub
    return new WorkspaceService() {

      @Override
      public void didChangeConfiguration(DidChangeConfigurationParams params) {
        System.out.println(
            "SpoonLangServer.getWorkspaceService().new WorkspaceService() {...}.didChangeConfiguration()");
      }

      @Override
      public void didChangeWatchedFiles(DidChangeWatchedFilesParams params) {
        // TODO Auto-generated method stub
      }
    };
  }

  //private FullTextDocumentService fullTextDocumentService = new FullTextDocumentService(client) {
  //
  //
  //
  //  @Override
  //  public CompletableFuture<Either<List<CompletionItem>, CompletionList>> completion(
  //      CompletionParams position) {
  //    CompletionItem typescriptCompletionItem = new CompletionItem();
  //    typescriptCompletionItem.setLabel("TypeScript");
  //    typescriptCompletionItem.setKind(CompletionItemKind.Field);
  //    typescriptCompletionItem.setData(1.0);
  //    CompletionItem javascriptCompletionItem = new CompletionItem();
  //    javascriptCompletionItem.setLabel("JavaScript");
  //    javascriptCompletionItem.setKind(CompletionItemKind.Text);
  //    javascriptCompletionItem.setData(2.0);
  //
  //    List<CompletionItem> completions = new ArrayList<>();
  //    completions.add(typescriptCompletionItem);
  //    completions.add(javascriptCompletionItem);
  //
  //    return CompletableFuture
  //        .completedFuture(Either.forRight(new CompletionList(false, completions)));
  //  }
  //
  //  @Override
  //  public CompletableFuture<CompletionItem> resolveCompletionItem(CompletionItem item) {
  //    if (item.getData().equals(1.0)) {
  //      item.setDetail("TypeScript details");
  //      item.setDocumentation("TypeScript documentation");
  //    } else if (item.getData().equals(2.0)) {
  //      item.setDetail("JavaScript details");
  //      item.setDocumentation("JavaScript documentation");
  //    }
  //    return CompletableFuture.completedFuture(item);
  //  }
  //};

}
