package org.example.server;

import org.eclipse.lsp4j.*;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.example.model.Procedure;
import org.example.model.Variable;
import org.example.parser.Lexer;
import org.example.parser.LogoParser;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class LogoTextDocService implements TextDocumentService {

    private final Map<String, String> openDocuments = new HashMap<>();


    @Override
    public void didOpen(DidOpenTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        String text = params.getTextDocument().getText();
        openDocuments.put(uri, text);
    }

    @Override
    public void didChange(DidChangeTextDocumentParams params) {
        String uri = params.getTextDocument().getUri();
        String newText = params.getContentChanges().get(0).getText();
        openDocuments.put(uri, newText);
    }

    @Override
    public void didClose(DidCloseTextDocumentParams params) {
        openDocuments.remove(params.getTextDocument().getUri());
    }

    @Override
    public void didSave(DidSaveTextDocumentParams params) {

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
}
