package org.example.parser;

public class Token {
    public enum Type {
        TO, END, REPEAT, IF, IFELSE, MAKE, FOR, WHILE, STOP, OUTPUT,
        FORWARD, BACKWARD, RIGHT, LEFT, HOME, SETXY,
        CLEARSCREEN, PENUP, PENDOWN, HIDETURTLE, SHOWTURTLE, SETPENSIZE, SETCOLOR, SETBACKGROUND,

        NUMBER,
        IDENTIFIER,
        VARIABLE,
        WORD,
        BRACKET_OPEN, BRACKET_CLOSE,
        PAREN_OPEN, PAREN_CLOSE,
        PLUS, MINUS, MULTIPLY, DIVIDE,
        EQUALS, LESS_THAN, GREATER_THAN,

        EOF
    }
    public final Type type;
    public final String text;
    public final int line;
    public final int column;

    public Token(Type type, String text, int line, int column){
        this.type = type;
        this.text = text;
        this.line = line;
        this.column = column;
    }
    @Override
    public String toString() {
        return String.format("Token(%s, '%s', %d:%d)", type, text, line, column);
    }
}
