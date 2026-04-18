

package org.example.server;


import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.services.*;
import java.util.concurrent.CompletableFuture;

public class LogoLangServer implements LanguageServer, LanguageClientAware {
    private final TextDocumentService textDocumentService;
    private final WorkspaceService workspaceService;
    private LanguageClient client;

    public LogoLangServer() {
        this.textDocumentService = new LogoTextDocService();
        this.workspaceService = new LogoWorkspaceService();
    }

    @Override
    public void connect(LanguageClient languageClient) {
        this.client = languageClient;
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams initializeParams) {
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
        capabilities.setDefinitionProvider(true);

        return CompletableFuture.completedFuture(new InitializeResult(capabilities));
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void exit() {
        System.exit(0);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }
}
