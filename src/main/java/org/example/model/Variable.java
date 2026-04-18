package org.example.model;

public class Variable {
    public final String name;
    public final int startLine;
    public final int startColumn;

    public Variable(String name, int startLine, int startColumn) {
        this.name = name;
        this.startLine = startLine;
        this.startColumn = startColumn;
    }
}
