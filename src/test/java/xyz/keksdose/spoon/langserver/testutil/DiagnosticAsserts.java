package xyz.keksdose.spoon.langserver.testutil;

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
/**
 * Util class contains various asserts for testing.
 * {@see #assertRange(Set, Range)}
 * {@see #assertPresentDiagnostic(Set, Diagnostic)}
 * {@see #assertAbsentDiagnostic(Set, Diagnostic)}
 */
public class DiagnosticAsserts {

  /**
   * checks if a given range is in the diagnostics. Fails if the set is empty or the range is not contained.
   * @param diagnostics created diagnostics a document.
   */
  public static void assertRange(Set<Diagnostic> diagnostics, Range range) {
    for (Diagnostic diagnostic : diagnostics) {
      if (diagnostic.getRange().equals(range)) {
        return;
      }
    }
    fail(String.format("Range %s is not included in  %s", range, convertToRangeString(diagnostics)));
  }

  private static String convertToRangeString(Set<Diagnostic> diagnostics) {
    return diagnostics.stream().map(v -> v.getRange().toString()).collect(Collectors.joining());
  }
  /**
   * checks if all set parameters of a diagnostic are set by any diagnostic.  
   * @param diagnostics create diagnostics for a document.
   * @param expected 
   */
  public static void assertPresentDiagnostic(Set<Diagnostic> diagnostics, Diagnostic expected) {
    assumeTrue(expected != null && !diagnostics.isEmpty());
    for (Diagnostic diagnostic : diagnostics) {
      boolean result = 
      checkMessage(expected, diagnostic) &&
      checkRange(expected, diagnostic) &&
      checkSeverity(expected, diagnostic) &&
      checkSource(expected, diagnostic);
      
      if(result) {
        // match found all correct
        return;
      }
    }
    fail(String.format("No diagnostic found that matches %s", expected.toString()));
  }

  /**
   * checks if all set parameters of a diagnostic are set by not any diagnostic.  
   * @param diagnostics create diagnostics for a document.
   * @param notExpected 
   */
  public static void assertAbsentDiagnostic(Set<Diagnostic> diagnostics, Diagnostic notExpected) {
    assumeTrue(notExpected != null && !diagnostics.isEmpty());
    for (Diagnostic diagnostic : diagnostics) {
      boolean result = 
      checkMessage(notExpected, diagnostic) &&
      checkRange(notExpected, diagnostic) &&
      checkSeverity(notExpected, diagnostic) &&
      checkSource(notExpected, diagnostic);
      
      if(result) {
        fail(String.format("Diagnostic found that matches %s", diagnostic.toString()));
      }
    }
  }
  private static boolean checkSource(Diagnostic shouldPresent, Diagnostic diagnostic) {
    if(shouldPresent.getSource() != null){
      return diagnostic.getSource().equals(shouldPresent.getSource());
    }
    return true;
  }

  private static boolean checkSeverity(Diagnostic shouldPresent, Diagnostic diagnostic) {
    if(shouldPresent.getSeverity() != null) {
      return diagnostic.getSeverity().equals(shouldPresent.getSeverity());
    }
    return true;
  }

  private static boolean checkRange(Diagnostic shouldPresent, Diagnostic diagnostic) {
    if(shouldPresent.getRange() != null) {
      return diagnostic.getRange().equals(shouldPresent.getRange());
    }
    return true;
  }

  private static boolean checkMessage(Diagnostic shouldPresent, Diagnostic diagnostic) {
    if(shouldPresent.getMessage()!= null){
      return diagnostic.getMessage().equals(shouldPresent.getMessage());
    }
    return true;
  }
}