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

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.HoverParams;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;

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

    private String extractWordAt(String text, int lineIndex, int column) {
        if (text == null || text.isEmpty()) return "";
        String[] lines = text.split("\\r?\\n");
        if (lineIndex < 0 || lineIndex >= lines.length) return "";

        String line = lines[lineIndex];

        if (column < 0 || column >= line.length()) {
            column = line.length() - 1;
        }
        if (column < 0) return "";

        int left = column;
        while (left >= 0 && isLogoIdentifierChar(line.charAt(left))) {
            left--;
        }

        int right = column;
        while (right < line.length() && isLogoIdentifierChar(line.charAt(right))) {
            right++;
        }

        int start = left + 1;
        if (start >= right) return "";

        return line.substring(start, right);
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

    public CompletableFuture<Hover> hover(HoverParams params){
        return CompletableFuture.supplyAsync(() -> {
            String uri = params.getTextDocument().getUri();
            String text = openDocuments.get(uri);
            if(text == null) return null;

            int line  = params.getPosition().getLine();
            int column = params.getPosition().getCharacter();

            String wordUnderCursor = extractWordAt(text, line, column).toUpperCase();
            if(wordUnderCursor.isEmpty()) return null;

            String cleanWord = wordUnderCursor;
            if (cleanWord.startsWith(":") || cleanWord.startsWith("\"")) {
                cleanWord = cleanWord.substring(1);
            }

            String description = getCommandDescription(cleanWord);

            if(description == null){
                Lexer lexer = new Lexer(text);
                LogoParser parser = new LogoParser(lexer.tokenize());
                parser.parse();

                if (parser.getProcedures().containsKey(cleanWord)) {
                    description = "**User Procedure:** `" + cleanWord + "`\n\nPress F12 to go to definition.";
                } else if (parser.getVariables().containsKey(cleanWord)) {
                    description = "**Variable:** `" + cleanWord + "`";
                }
            }

            if (description != null) {
                MarkupContent markupContent = new MarkupContent(MarkupKind.MARKDOWN, description);
                return new Hover(markupContent);
            }
            return null;
        });
    }

    private String getCommandDescription(String command) {
        return switch (command) {
            case "TO" -> "**TO**\n\nBegins the definition of a new procedure.";
            case "END" -> "**END**\n\nEnds the definition of a procedure.";
            case "REPEAT" -> "**REPEAT**\n\nRepeats a block of commands a specified number of times.\n\n*Example:* `REPEAT 4 [FD 100 RT 90]`";
            case "IF" -> "**IF**\n\nExecutes a block of commands if the condition is true.";
            case "IFELSE" -> "**IFELSE**\n\nExecutes the first block if true, or the second block if false.";
            case "MAKE" -> "**MAKE**\n\nAssigns a value to a variable.\n\n*Example:* `MAKE \"SIZE 50`";
            case "FOR" -> "**FOR**\n\nA standard counting loop.";
            case "WHILE" -> "**WHILE**\n\nRepeats a block of commands as long as the condition is true.";
            case "STOP" -> "**STOP**\n\nHalts the execution of the current procedure and returns to the caller.";
            case "OUTPUT", "OP" -> "**OUTPUT (OP)**\n\nReturns a value from a procedure.";

            case "FD", "FORWARD" -> "**FORWARD (FD)**\n\nMoves the turtle forward by the specified number of steps.\n\n*Example:* `FD 100`";
            case "BK", "BACK", "BACKWARD" -> "**BACKWARD (BK)**\n\nMoves the turtle backward without changing its heading.";
            case "RT", "RIGHT" -> "**RIGHT (RT)**\n\nTurns the turtle right (clockwise) by the specified angle in degrees.";
            case "LT", "LEFT" -> "**LEFT (LT)**\n\nTurns the turtle left by the specified angle.";
            case "HOME" -> "**HOME**\n\nMoves the turtle to the center of the screen (0,0) and points it straight up.";
            case "SETXY" -> "**SETXY**\n\nMoves the turtle to the specified X and Y coordinates.";

            case "CS", "CLEARSCREEN" -> "**CLEARSCREEN (CS)**\n\nClears the screen and returns the turtle to the home position.";
            case "PU", "PENUP" -> "**PENUP (PU)**\n\nLifts the pen. The turtle will move without drawing a line.";
            case "PD", "PENDOWN" -> "**PENDOWN (PD)**\n\nLowers the pen. The turtle will leave a trail when moving.";
            case "HT", "HIDETURTLE" -> "**HIDETURTLE (HT)**\n\nHides the turtle cursor to speed up drawing.";
            case "ST", "SHOWTURTLE" -> "**SHOWTURTLE (ST)**\n\nShows the turtle cursor.";
            case "SETPENSIZE", "SETWIDTH" -> "**SETPENSIZE**\n\nSets the thickness of the pen.";
            case "SETCOLOR", "SETPC" -> "**SETCOLOR**\n\nSets the pen color.";
            case "SETBG", "SETBACKGROUND" -> "**SETBACKGROUND (SETBG)**\n\nSets the background color of the screen.";

            default -> null;
        };
    }
}
