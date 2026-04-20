package org.example.parser;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    private final String input;
    private int pos = 0;
    private int line = 0;
    private int col = 0;

    public Lexer(String input) {
        this.input = input;
    }
    public List<Token> tokenize(){
        List<Token> tokens = new ArrayList<>();
        while(pos < input.length()){
            char ch = input.charAt(pos);
            if (Character.isWhitespace(ch)) {
                next();
            } else if (Character.isLetter(ch) || ch == '_') {
                tokens.add(readIdentifierOrKeyword());
            } else if (Character.isDigit(ch) || (ch == '-' && isNextDigit())) {
                tokens.add(readNumber());
            } else if (ch == ':') {
                tokens.add(readVariable());
            } else if (ch == '"') {
                tokens.add(readWord());
            } else {
                Token.Type type = switch (ch) {
                    case '[' -> Token.Type.BRACKET_OPEN;
                    case ']' -> Token.Type.BRACKET_CLOSE;
                    case '(' -> Token.Type.PAREN_OPEN;
                    case ')' -> Token.Type.PAREN_CLOSE;
                    case '+' -> Token.Type.PLUS;
                    case '-' -> Token.Type.MINUS;
                    case '*' -> Token.Type.MULTIPLY;
                    case '/' -> Token.Type.DIVIDE;
                    case '=' -> Token.Type.EQUALS;
                    case '<' -> Token.Type.LESS_THAN;
                    case '>' -> Token.Type.GREATER_THAN;
                    default -> null;
                };

                if (type != null) {
                    tokens.add(new Token(type, String.valueOf(ch), line, col));
                }
                next();
            }
        }
        tokens.add(new Token(Token.Type.EOF, "", line, col));
        return tokens;
    }
    private void next(){
        if(input.charAt(pos) == '\n'){
            line++;
            col = 0;
        }else{
            col++;
        }
        pos++;
    }

    private boolean isNextDigit() {
        return pos + 1 < input.length() && Character.isDigit(input.charAt(pos + 1));
    }

    private Token readVariable() {
        int startCol = col;
        StringBuilder sb = new StringBuilder();
        sb.append(input.charAt(pos)); // Dodaj znak ':'
        next();
        while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '_') {
            sb.append(input.charAt(pos));
            next();
        }
        return new Token(Token.Type.VARIABLE, sb.toString(), line, startCol);
    }
    private Token readWord() {
        int startCol = col;
        StringBuilder sb = new StringBuilder();
        sb.append(input.charAt(pos)); // Dodaj znak '"'
        next();
        while (pos < input.length() && (Character.isLetterOrDigit(input.charAt(pos))) || input.charAt(pos) == '_') {
            sb.append(input.charAt(pos));
            next();
        }
        return new Token(Token.Type.WORD, sb.toString(), line, startCol);
    }
    private Token readIdentifierOrKeyword() {
        StringBuilder sb = new StringBuilder();
        int startCol = col;
        while (pos < input.length() && Character.isLetter(input.charAt(pos)) || input.charAt(pos) == '_') {
            sb.append(input.charAt(pos));
            next();
        }
        String text = sb.toString().toUpperCase();

        Token.Type type = switch (text) {
            case "TO" -> Token.Type.TO;
            case "END" -> Token.Type.END;
            case "REPEAT" -> Token.Type.REPEAT;
            case "IF" -> Token.Type.IF;
            case "IFELSE" -> Token.Type.IFELSE;
            case "MAKE" -> Token.Type.MAKE;
            case "FOR" -> Token.Type.FOR;
            case "WHILE" -> Token.Type.WHILE;
            case "STOP" -> Token.Type.STOP;
            case "OUTPUT", "OP" -> Token.Type.OUTPUT;

            case "FD", "FORWARD" -> Token.Type.FORWARD;
            case "BK", "BACK", "BACKWARD" -> Token.Type.BACKWARD;
            case "RT", "RIGHT" -> Token.Type.RIGHT;
            case "LT", "LEFT" -> Token.Type.LEFT;
            case "HOME" -> Token.Type.HOME;
            case "SETXY" -> Token.Type.SETXY;

            case "CS", "CLEARSCREEN" -> Token.Type.CLEARSCREEN;
            case "PU", "PENUP" -> Token.Type.PENUP;
            case "PD", "PENDOWN" -> Token.Type.PENDOWN;
            case "HT", "HIDETURTLE" -> Token.Type.HIDETURTLE;
            case "ST", "SHOWTURTLE" -> Token.Type.SHOWTURTLE;
            case "SETPENSIZE", "SETWIDTH" -> Token.Type.SETPENSIZE;
            case "SETCOLOR", "SETPC" -> Token.Type.SETCOLOR;
            case "SETBG", "SETBACKGROUND" -> Token.Type.SETBACKGROUND;

            default -> Token.Type.IDENTIFIER;
        };
        return new Token(type, text, line, startCol);
    }


    private Token readNumber() {
        StringBuilder sb = new StringBuilder();
        int startCol = col;
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            sb.append(input.charAt(pos));
            next();
        }
        return new Token(Token.Type.NUMBER, sb.toString(), line, startCol);
    }
}
