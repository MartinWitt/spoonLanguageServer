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
    Optional<CtType<?>> searchedType = findMatchingType(model, convertURItoPath(uri));
    if (searchedType.isEmpty()) {
      return Optional.empty();
    }
    //copied form @monperrus
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
      return Optional.of(result.get(0));
    }
    if (result.size() > 1) {
      List<CtElement> matchWithColumn = new ArrayList<>();
      // now we have multiple possible matches
      for (CtElement ctElement : result) {
        if (ctElement.getPosition().getColumn() <= position.getCharacter() + 1
            && ctElement.getPosition().getEndColumn() >= position.getCharacter() + 1) {
          matchWithColumn.add(ctElement);
        }
      }
      matchWithColumn
          .removeIf(parent -> matchWithColumn.stream().anyMatch(child -> child.hasParent(parent)));
      if (matchWithColumn.size() == 1) {
        return Optional.of(matchWithColumn.get(0));
      }
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

}
