package org.example.model;

public class Procedure {
    public final String name;
    public final int startLine;
    public final int startColumn;

    public Procedure(String name, int startLine, int startColumn) {
        this.name = name;
        this.startLine = startLine;
        this.startColumn = startColumn;
    }

    @Override
    public String toString() {
        return "Procedure{" + "name='" + name + '\'' + ", line=" + startLine + '}';
    }
}
