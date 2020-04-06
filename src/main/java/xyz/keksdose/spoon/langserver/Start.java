package xyz.keksdose.spoon.langserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;

public class Start {


  public static void main(String[] args) throws IOException {
    int port = 6009;
    System.out.println(port);
    Socket socket = new Socket("localhost", port);

    InputStream in = socket.getInputStream();
    OutputStream out = socket.getOutputStream();
    SpoonLangServer server = new SpoonLangServer();
    Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, in, out);
    LanguageClient client = launcher.getRemoteProxy();
    server.connect(client);
    launcher.startListening();
  }
}
