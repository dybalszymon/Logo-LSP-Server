
import org.example.parser.Lexer;
import org.example.parser.Token;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LexerTest {

    @Test
    public void testBasicMovementTokens() {
        String code = "FD 100 RT 90";
        Lexer lexer = new Lexer(code);

        List<Token> tokens = lexer.tokenize();

        assertEquals(5, tokens.size(), "should be 4 tokens + 1 EOF");

        assertEquals(Token.Type.FORWARD, tokens.get(0).type);
        assertEquals("FD", tokens.get(0).text);

        assertEquals(Token.Type.NUMBER, tokens.get(1).type);
        assertEquals("100", tokens.get(1).text);

        assertEquals(Token.Type.RIGHT, tokens.get(2).type);
        assertEquals(Token.Type.NUMBER, tokens.get(3).type);
        assertEquals(Token.Type.EOF, tokens.get(4).type);
    }

    @Test
    public void testVariablesAndStrings() {
        String code = "MAKE \"SIZE 50\nFD :SIZE";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.tokenize();

        assertEquals(Token.Type.MAKE, tokens.get(0).type);
        assertEquals(Token.Type.WORD, tokens.get(1).type);
        assertEquals("\"SIZE", tokens.get(1).text);

        assertEquals(Token.Type.FORWARD, tokens.get(3).type);
        assertEquals(Token.Type.VARIABLE, tokens.get(4).type);
        assertEquals(":SIZE", tokens.get(4).text);
    }
}