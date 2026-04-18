package org.example;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageServer;
import org.example.server.LogoLangServer;
import org.eclipse.lsp4j.services.LanguageClient;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        LogoLangServer server = new LogoLangServer();

        InputStream input = System.in;
        OutputStream output = System.out;

        Launcher<LanguageClient> launcher = LSPLauncher.createServerLauncher(server, input, output);

        LanguageClient client = launcher.getRemoteProxy();
        server.connect(client);

        Future<Void> startListening = launcher.startListening();
        System.err.println("LOGO LSP Server started!");
        startListening.get();
    }
}