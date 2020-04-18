package xyz.keksdose.spoon.langserver.diagnostic;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xyz.keksdose.spoon.langserver.diagnostics.UnusedVariable;
import xyz.keksdose.spoon.langserver.testutil.DiagnosticTest;

public class UnusedVariableTest {

  @DisplayName("Check the hardcoded information")
  @Nested
  class DiagnosticInformation {
    @DisplayName("Check source")
    @Test
    public void checkSource() {
      Diagnostic diagnostic = new Diagnostic();
      diagnostic.setSource("Spoon");
      DiagnosticTest.diagnosticExpected("diagnostic/UnusedVariable.java", diagnostic,
          new UnusedVariable());
    }

    @DisplayName("Check notification string")
    @Test
    public void checkString() {
      //contract: a unused variable has the message: The variable is not used and can be removed
      Diagnostic diagnostic = new Diagnostic();
      diagnostic.setMessage("The variable is not used and can be removed");
      DiagnosticTest.diagnosticExpected("diagnostic/UnusedVariable.java", diagnostic,
          new UnusedVariable());
    }

    @DisplayName("Check severity level")
    @Test
    public void checkSeverity() {
      //contract: a unused variable has the message: The variable is not used and can be removed
      Diagnostic diagnostic = new Diagnostic();
      diagnostic.setSeverity(DiagnosticSeverity.Warning);
      DiagnosticTest.diagnosticExpected("diagnostic/UnusedVariable.java", diagnostic,
          new UnusedVariable());
    }
  }
  @DisplayName("Checks functionality")
  @Nested
  class Functionality {
    @Test
    public void checkRangeValid() {
      //contract: the variable is unused and must be included.
      Diagnostic diagnostic = new Diagnostic();
      Position start = new Position(6,8);
      Position end = new Position(6,13);
      diagnostic.setRange(new Range(start,end));
      DiagnosticTest.diagnosticExpected("diagnostic/UnusedVariable.java", diagnostic, new UnusedVariable());
    }
    @Test
    public void checkRangeInvalid() {
      //contract: the variable is used and must not be included. (b++ uses it)
      Diagnostic diagnostic = new Diagnostic();
      Position start = new Position(7,8);
      Position end = new Position(7,13);
      diagnostic.setRange(new Range(start,end));
      DiagnosticTest.diagnosticAbsent("diagnostic/UnusedVariable.java", diagnostic, new UnusedVariable());
    }
  }
}
