package xyz.keksdose.spoon.langserver.codeactions;

import static java.lang.Math.abs;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentItem;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.Filter;

public class PositionUtil {

  private PositionUtil() {
    //util class
  }

  public static Optional<CtElement> getExactMatch(CtModel model, String uri, Position position) {
    Optional<CtType<?>> searchedType = findMatchingType(model, convertURItoPath(uri));
    if (searchedType.isEmpty()) {
      return Optional.empty();
    }
    List<CtElement> result = searchedType.get().getElements(new Filter<CtElement>() {
      public boolean matches(CtElement elem) {
        if (!elem.getPosition().isValidPosition()) {
          return false;
        }
        if (elem.getPosition().getLine() <= position.getLine() + 1
            && elem.getPosition().getEndLine() >= position.getLine() + 1) {
          return true;
        }
        return false;
      }
    });
    for (CtElement ctElement : result) {
      if (ctElement.getPosition().getColumn() <= position.getCharacter() + 1
          && ctElement.getPosition().getEndColumn() >= position.getCharacter() + 1) {
        return Optional.of(ctElement);
      }
    }
    return Optional.empty();
  }

  public static Optional<CtElement> getClosestMatch(CtModel model, String uri, Position position) {
    // Search the type with the uri first.
    // if the type is unknown here, we can stop and return instant.
    Optional<CtType<?>> searchedType = findMatchingType(model, convertURItoPath(uri));
    if (searchedType.isEmpty()) {
      return Optional.empty();
    }
    /* 
    The searched Element should have the following attributes:
      1. Valid position.
      2. Range from start to end must include the searched line.
      3. The column should match.
    It's safe to assume every element without a valid position is not search.
    LSP4J indexes Position by zero and spoon by 1. so we need to translate it.
    */
    // First we compare lines and check attribute 1 and 2.
    List<CtElement> result = searchedType.get().getElements(new Filter<CtElement>() {
      public boolean matches(CtElement elem) {
        if (!elem.getPosition().isValidPosition()) {
          return false;
        }
        if (elem.getPosition().getLine() <= position.getLine() + 1
            && elem.getPosition().getEndLine() >= position.getLine() + 1) {
          return true;
        }
        return false;
      }
    });
    if (result.size() == 1) {
      // Only 1 match means we can stop here and return the result.
      return Optional.of(result.get(0));
    }
    // If we have multiple possible matches we need to check more.
    // Now we check attribute 3. The colum should match.
    if (result.size() > 1) {
      List<CtElement> matchWithColumn = new ArrayList<>();
      for (CtElement ctElement : result) {
        if (ctElement.getPosition().getColumn() <= position.getCharacter() + 1
            && ctElement.getPosition().getEndColumn() >= position.getCharacter() + 1) {
          matchWithColumn.add(ctElement);
        }
      }
      /* now we have a elements, that match the column. This can be multiple. 
      Now we remove all Elements, that have some child in the result. e.g.
      public class foo{
        int b;
      }
      assuming we search the position where b ist, class foo and int b are part of the result.
      Removing every element, that has a child in the result solves this. 
      */
      matchWithColumn
          .removeIf(parent -> matchWithColumn.stream().anyMatch(child -> child.hasParent(parent)));
      if (matchWithColumn.size() == 1) {
        return Optional.of(matchWithColumn.get(0));
      }
      // if we cant resolve it with this lets find the nearest element to the line with the elements left.
      if (!matchWithColumn.isEmpty()) {
        return matchWithColumn.stream()
            .min((o1, o2) -> Integer.compare(
                abs(o1.getPosition().getLine() - (position.getLine() + 1)),
                abs(o2.getPosition().getLine() - (position.getLine() + 1))));
      }
    }
    if (result.size() > 1) {
      // now search the object nearest the hovered line
      return result.stream()
          .min((o1, o2) -> Integer.compare(
              abs(o1.getPosition().getLine() - (position.getLine() + 1)),
              abs(o2.getPosition().getLine() - (position.getLine() + 1))));
    }
    return Optional.empty();
  }

  private static Optional<CtType<?>> findMatchingType(CtModel model, List<String> pathParts) {
    Map<List<String>, CtType<?>> typesByQName = createMap(model);
    for (String pathPart : pathParts) {
      typesByQName.entrySet().removeIf(v -> !v.getKey().contains(removeJavaFileEnding(pathPart)));
      if (typesByQName.size() == 1) {
        break;
      }
    }
    return typesByQName.isEmpty() ? Optional.empty()
        : Optional.of(typesByQName.values().iterator().next());
  }

  private static Map<List<String>, CtType<?>> createMap(CtModel model) {
    Map<List<String>, CtType<?>> result = new HashMap<>();
    model.getAllTypes().stream()
        .forEach(v -> result.put(Arrays.asList(v.getQualifiedName().split("\\.")), v));
    return result;
  }

  private static String removeJavaFileEnding(String input) {
    return input.endsWith(".java") ? input.substring(0, input.lastIndexOf(".java")) : input;
  }

  /**
   * creates a package name from uri in <b>reversed</b> order
   * @param uri
   * @return
   */
  private static List<String> convertURItoPath(String uri) {
    try {
      Path path = Path.of(new URI(uri));
      List<String> pathParts = new ArrayList<>();
      for (int i = 0; i < path.getNameCount(); i++) {
        pathParts.add(path.getName(i).toString());
      }
      Collections.reverse(pathParts);
      return pathParts;
    } catch (URISyntaxException e) {
      Logger.getAnonymousLogger().info(e.getMessage());
      return Collections.emptyList();
    }

  }

  public static List<Position> convertRange(Range range, TextDocumentItem document) {
    List<Position> result = new ArrayList<>();
    // if the range spans only 1 line we need no lookups in the document 
    Position start = range.getStart();
    Position end = range.getEnd();
    if (start.getLine() == end.getLine()) {
      for (int i = start.getCharacter(); i <= end.getCharacter(); i++) {
        result.add(new Position(start.getLine(), i));
      }
      return result;
    }
    Integer[] lineLength = document.getText().lines().map(String::length).toArray(Integer[]::new);
    // first add the positions  form start to line end.
    for (int i = start.getCharacter(); i <= lineLength[start.getLine()]; i++) {
      result.add(new Position(start.getLine(), i));
    }
    // add the lines between first and last line
    for (int i = start.getLine() + 1; i < end.getLine(); i++) {
      for (int charIndex = 0; charIndex < lineLength.length; charIndex++) {
        result.add(new Position(i, charIndex));
      }
    }
    // add a positions in the last line till last char 
    for (int i = 0; i <= end.getCharacter(); i++) {
      result.add(new Position(end.getLine(), i));
    }
    return result;
  }

}
