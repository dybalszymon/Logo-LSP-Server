package org.example.parser;

import org.example.model.Procedure;
import org.example.model.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogoParser {
    private final List<Token> tokens;
    private int current  = 0;
    
    private final Map<String, Procedure> procedures = new HashMap<>();
    private final Map<String, Variable> variables = new HashMap<>();

    public LogoParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public void parse() {
        while (peek().type != Token.Type.EOF) {
            Token token = advance();

            if (token.type == Token.Type.TO) {
                parseProcedureDeclaration(token);
            } else if (token.type == Token.Type.MAKE) {
                parseMakeDeclaration(token); // Wyłapujemy słowo MAKE
            }
        }
    }

    private void parseMakeDeclaration(Token makeToken) {
        if (peek().type == Token.Type.EOF) return;

        Token nameToken = advance();
        if (nameToken.type == Token.Type.WORD) {
            String cleanName = nameToken.text.substring(1).toUpperCase();
            variables.put(cleanName, new Variable(cleanName, nameToken.line, nameToken.column));
        }
    }

    private void parseProcedureDeclaration(Token toToken) {
        if (peek().type == Token.Type.EOF) return;


        Token nameToken = advance();

        if (nameToken.type == Token.Type.IDENTIFIER) {
            Procedure procedure = new Procedure(
                    nameToken.text,
                    toToken.line,
                    toToken.column
            );
            procedures.put(nameToken.text, procedure);

            while (peek().type == Token.Type.VARIABLE) {
                Token argToken = advance();
                String cleanName = argToken.text.substring(1).toUpperCase();
                variables.put(cleanName, new Variable(cleanName, argToken.line, argToken.column));
            }
        }

    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token advance() {
        if (peek().type != Token.Type.EOF) current++;
        return tokens.get(current - 1);
    }

}
