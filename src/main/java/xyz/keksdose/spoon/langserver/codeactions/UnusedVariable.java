package xyz.keksdose.spoon.langserver.codeactions;


public class UnusedVariable {
/*
  private static final String COMMAND_NAME = "unused field";

  public boolean isApplicable(CtModel model, String uri, Range range, TextDocumentItem document) {
    List<Position> rangeList = PositionUtil.convertRange(range, document);
    List<CtField<?>> assignment = rangeList.stream()
        .map(v -> getExactMatch(model, uri, v))
        .flatMap(Optional::stream)
        .filter(v -> v instanceof CtField)
        .map(v -> (CtField<?>) v)
        .distinct()
        .filter(v->v.getReference() != null)
        .collect(Collectors.toList());
        if(assignment.size() != 1) {
          return false;
        }
        boolean unused = model.getElements(new TypeFilter<>(CtFieldAccess.class) {
          @Override
          public boolean matches(CtFieldAccess element) {
            return element.getVariable().equals(assignment.get(0).getReference());
          }
        }).isEmpty();
      return assignment.size() == 1 && unused ;
  }

  public CodeAction apply(CtModel model, String uri, Range range, TextDocumentItem document) {
    List<Position> rangeList = PositionUtil.convertRange(range, document);
    CtField<?> variable = rangeList.stream()
        .map(v -> getExactMatch(model, uri, v))
        .flatMap(Optional::stream)
        .filter(v -> v instanceof CtField)
        .map(v -> (CtField<?>) v)
        .distinct()
        .filter(v->v.getReference() != null)
        .findAny()
        .get();

        CodeAction action = new CodeAction(COMMAND_NAME);
    
        TextEdit remove = new TextEdit();
        int startLine = variable.getType().getPosition().getLine()-1;
        int startChar = variable.getType().getPosition().getColumn()-1;
        Position start = new Position(startLine, startChar);
    
        int endLine;
        int endChar; 
        if(variable.getDefaultExpression() != null) {
        endLine = variable.getDefaultExpression().getPosition().getEndLine()-1;
        //because statements end with ";"
        endChar = variable.getDefaultExpression().getPosition().getEndColumn() +1; 
        }
        else {
          endLine = variable.getPosition().getLine()-1;
          endChar = variable.getPosition().getEndColumn();
        }
        Position end = new Position(endLine, endChar);
        remove.setRange(new Range(start, end));
        remove.setNewText("");

        WorkspaceEdit documentEdit = new WorkspaceEdit();
        documentEdit.setChanges(Map.of(uri, List.of(remove)));
        action.setEdit(documentEdit);
    return action;
  }

  public List<Diagnostic> highlightRefactoring(CtModel model, String uri, Range range,
      TextDocumentItem document) {
    List<Position> rangeList = PositionUtil.convertRange(range, document);
    List<Diagnostic> result = new ArrayList<>();
    for (Position position : rangeList) {
      if (getExactMatch(model, uri, position).stream()
          .filter(v -> v instanceof CtField)
          .map(v -> (CtField<?>) v)
          .distinct()
          .filter(v -> v.getReference() != null)
          .count() > 0) {
        CtField<?> field = (CtField<?>) getExactMatch(model, uri, position).get();
        boolean unused = model.getElements(new TypeFilter<>(CtFieldAccess.class) {
          @Override
          public boolean matches(CtFieldAccess element) {
            return element.getVariable().equals(field.getReference());
          }
        }).isEmpty();
        if (unused) {
          Diagnostic diagnostic = new Diagnostic(new Range(position, position),
              "Remove unused field", DiagnosticSeverity.Warning, "spoon");
          result.add(diagnostic);
        }
      }
    }
    return result;
  }

*/

}