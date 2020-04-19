package diagnostic;

public class UnusedVariable {

  private int b = 5;
  public void foo() {
    int a = 3;
    int b = 5;
    b++;
  }
}