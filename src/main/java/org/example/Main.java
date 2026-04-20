package org.example;

import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.example.server.LogoLangServer;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) {
        try {
            LogoLangServer server = new LogoLangServer();

            InputStream in = System.in;
            OutputStream out = System.out;

            Launcher<LanguageClient> launcher = Launcher.createLauncher(
                    server,
                    LanguageClient.class,
                    in,
                    out
            );

            LanguageClient client = launcher.getRemoteProxy();
            server.connect(client);

            Future<Void> startListening = launcher.startListening();
            startListening.get();

        } catch (Exception e) {
            System.err.println("Starting LSP error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}