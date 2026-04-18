package org.example.parser;

public class Token {
    public enum Type {
        // --- Słowa kluczowe: Sterowanie i Procedury ---
        TO, END, REPEAT, IF, IFELSE, MAKE, FOR, WHILE, STOP, OUTPUT,

        // --- Słowa kluczowe: Ruch i Pozycja ---
        FORWARD, BACKWARD, RIGHT, LEFT, HOME, SETXY,

        // --- Słowa kluczowe: Ekran i Pióro ---
        CLEARSCREEN, PENUP, PENDOWN, HIDETURTLE, SHOWTURTLE, SETPENSIZE, SETCOLOR, SETBACKGROUND,

        // --- Zmienne, Wartości, Identyfikatory ---
        NUMBER,       // np. 100
        IDENTIFIER,   // np. MOJ_KWADRAT
        VARIABLE,     // np. :BOK
        WORD,         // np. "CZERWONY

        // --- Symbole i Operatory ---
        BRACKET_OPEN, BRACKET_CLOSE, // [ ]
        PAREN_OPEN, PAREN_CLOSE,     // ( )
        PLUS, MINUS, MULTIPLY, DIVIDE, // + - * /
        EQUALS, LESS_THAN, GREATER_THAN, // = < >

        EOF // Koniec pliku
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
