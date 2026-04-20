package org.example.server;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.services.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class LogoLangServer implements LanguageServer, LanguageClientAware {
    private final LogoTextDocService textDocumentService;
    private final WorkspaceService workspaceService;
    private LanguageClient client;

    public LogoLangServer() {
        this.textDocumentService = new LogoTextDocService();
        this.workspaceService = new LogoWorkspaceService();
    }

    @Override
    public void connect(LanguageClient languageClient) {

        this.client = languageClient;
        this.textDocumentService.setClient(client);
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(InitializeParams initializeParams) {
        ServerCapabilities capabilities = new ServerCapabilities();
        capabilities.setTextDocumentSync(TextDocumentSyncKind.Full);
        capabilities.setDefinitionProvider(true);

        SemanticTokensLegend legend = new SemanticTokensLegend(
                Arrays.asList("keyword", "string", "variable", "number", "operator"),
                Collections.emptyList()
        );
        SemanticTokensWithRegistrationOptions options = new SemanticTokensWithRegistrationOptions();
        options.setLegend(legend);
        options.setFull(true);

        capabilities.setSemanticTokensProvider(options);
        capabilities.setHoverProvider(true);

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
