package org.example.parser;

import org.example.model.Procedure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogoParser {
    private final List<Token> tokens;
    private int current  = 0;
    
    private final Map<String, Procedure> procedures = new HashMap<>();

    public LogoParser(List<Token> tokens) {
        this.tokens = tokens;
    }
    
    public Map<String, Procedure> parse(){
        while(peek().type != Token.Type.EOF){
            Token token = advance();

            if(token.type == Token.Type.TO){
                parseProcedureDeclaration(token);
            }
        }
        return procedures;
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
