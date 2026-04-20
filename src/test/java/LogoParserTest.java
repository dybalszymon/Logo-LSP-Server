
import org.eclipse.lsp4j.Diagnostic;
import org.example.model.Procedure;
import org.example.model.Variable;
import org.example.parser.Lexer;
import org.example.parser.LogoParser;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LogoParserTest {

    @Test
    public void testFindingProceduresAndVariables() {
        String code = "TO SQUARE :EDGE\n  FD :EDGE\nEND";
        Lexer lexer = new Lexer(code);
        LogoParser parser = new LogoParser(lexer.tokenize());

        // When
        parser.parse();
        Map<String, Procedure> procedures = parser.getProcedures();
        Map<String, Variable> variables = parser.getVariables();

        // Then
        assertTrue(procedures.containsKey("SQUARE"), "Parser should find square");
        assertEquals(0, procedures.get("SQUARE").startLine, "Procedure should be in line 0");

        assertTrue(variables.containsKey("EDGE"), "Parser should find edge");
    }

    @Test
    public void testDiagnosticsForUnknownCommands() {
        String code = "VIOD 100";
        Lexer lexer = new Lexer(code);
        LogoParser parser = new LogoParser(lexer.tokenize());

        // When
        parser.parse();
        List<Diagnostic> diagnostics = parser.getDiagnostics();

        // Then
        assertEquals(1, diagnostics.size(), "Parser should find 1 error");
        assertTrue(diagnostics.get(0).getMessage().contains("VIOD"), "message should contains name");
    }
}