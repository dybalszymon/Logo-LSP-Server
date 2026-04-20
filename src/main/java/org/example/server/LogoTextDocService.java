package org.example.server;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.example.model.Procedure;
import org.example.model.Variable;
import org.example.parser.Lexer;
import org.example.parser.LogoParser;
import org.example.parser.Token;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LogoTextDocService implements TextDocumentService {

    private final Map<String, String> openDocuments = new HashMap<>();
    private LanguageClient client;
    public void setClient(LanguageClient client) {
        this.client = client;
    }
    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        String text = params.getTextDocument().getText();
        openDocuments.put(uri, text);
        validateDocument(uri, text);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        String newText = params.getContentChanges().get(0).getText();
        openDocuments.put(uri, newText);
        validateDocument(uri, newText);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        openDocuments.remove(params.getTextDocument().getUri());
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {

    }

    private void validateDocument(String uri, String text){
        if(client == null) return;
        Lexer lexer = new Lexer(text);
        LogoParser parser = new LogoParser(lexer.tokenize());
        parser.parse();
        List<Diagnostic> diagnostic = parser.getDiagnostics();
        PublishDiagnosticsParams diagnosticParams = new PublishDiagnosticsParams(uri, diagnostic);
        client.publishDiagnostics(diagnosticParams);
    }

    @Override
    public CompletableFuture<Either<List<? extends Location>, List<? extends LocationLink>>> definition(DefinitionParams params){
        return CompletableFuture.supplyAsync(() -> {
            String uri = params.getTextDocument().getUri();
            String text = openDocuments.get(uri);
            if(text == null) return Either.forLeft(Collections.emptyList());

            int line = params.getPosition().getLine();
            int column = params.getPosition().getCharacter();

            String wordUnderCursor = extractWordAt(text, line, column).toUpperCase();
            if(wordUnderCursor.isEmpty()) return Either.forLeft(Collections.emptyList());

            String cleanWord = wordUnderCursor;
            if(cleanWord.startsWith(":") || cleanWord.startsWith("\"")) {
                cleanWord = cleanWord.substring(1);
            }

            Lexer lexer = new Lexer(text);
            LogoParser parser = new LogoParser(lexer.tokenize());
            parser.parse();

            Procedure targetProcedure = parser.getProcedures().get(cleanWord);
            if (targetProcedure != null) {
                return Either.forLeft(List.of(createLocation(uri, targetProcedure.startLine, targetProcedure.startColumn, targetProcedure.name.length())));
            }

            Variable targetVariable = parser.getVariables().get(cleanWord);
            if(targetVariable != null){
                return Either.forLeft(List.of(createLocation(uri, targetVariable.startLine, targetVariable.startColumn, targetVariable.name.length())));
            }
            return Either.forLeft(Collections.emptyList());
        });
    }

    private Location createLocation(String uri, int line, int column, int length){
        Location location = new Location();
        location.setUri(uri);
        location.setRange(new Range (new Position(line, column), new Position(line, column + length)));
        return location;
    }

    private String extractWordAt(String text, int lineIdx, int columnIdx){
        String[] lines = text.split("\n");
        if(lineIdx >= lines.length || lineIdx < 0 )return "";
        String line = lines[lineIdx];
        if(columnIdx >= line.length() || columnIdx < 0) return "";

        int start = columnIdx;
        while(start > 0 && isLogoIdentifierChar(line.charAt(start - 1))){
            start--;
        }
        int end = columnIdx;
        while(end < line.length() && isLogoIdentifierChar(line.charAt(end - 1))){
            end++;
        }
        if(start < end) return line.substring(start, end);
        return "";
    }

    private boolean isLogoIdentifierChar(char c) {
        return Character.isLetterOrDigit(c) || c == ':' || c == '"' || c == '_';
    }

    @Override
    public CompletableFuture<SemanticTokens> semanticTokensFull(SemanticTokensParams params){
        return CompletableFuture.supplyAsync(() -> {
            String uri = params.getTextDocument().getUri();
            String text = openDocuments.get(uri);

            if(text == null) return new SemanticTokens(Collections.emptyList());

            Lexer lexer = new Lexer(text);
            List<Token> tokens = lexer.tokenize();

            List<Integer> data = new ArrayList<>();
            int prevLine = 0;
            int prevChar = 0;

            for(Token token : tokens){
                int tokenTypeIndex = getTokenTypeIndex(token);
                if(tokenTypeIndex == -1) continue;
                int deltaLine = token.line - prevLine;
                int deltaStart;
                if(deltaLine == 0){
                    deltaStart = token.column - prevChar;
                }else{
                    deltaStart = token.column;
                }
                int length = token.text.length();
                data.add(deltaLine); //move down
                data.add(deltaStart); //move right;
                data.add(length); //word length
                data.add(tokenTypeIndex); //color type
                data.add(0); // modifiers

                prevLine = token.line;
                prevChar = token.column;
            }
            return new SemanticTokens(data);
        });
    }

    private int getTokenTypeIndex(Token token) {
        return switch (token.type) {
            case TO, END, REPEAT, IF, IFELSE, MAKE, FOR, WHILE, STOP, OUTPUT -> 0;

            case FORWARD, BACKWARD, RIGHT, LEFT, HOME, SETXY,
                    CLEARSCREEN, PENUP, PENDOWN, HIDETURTLE, SHOWTURTLE,
                    SETPENSIZE, SETCOLOR, SETBACKGROUND -> 1;

            case VARIABLE, WORD -> 2;

            case NUMBER -> 3;

            default -> -1;
        };
    }
}
