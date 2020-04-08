package xyz.keksdose.spoon.langserver;

import static java.util.Collections.emptySet;
import java.io.File;
import java.util.List;
import java.util.Set;
import spoon.IncrementalLauncher;
import spoon.SpoonException;
import spoon.reflect.CtModel;

public class Compiler {


  private IncrementalLauncher launcher;
  private String cachePath = ".spoonCache";
  private File cacheDir;

  public Compiler() {
    cacheDir = new File(cachePath);
    cacheDir.mkdir();
    createLauncher(emptySet());
  }

  private void createLauncher(Set<File> files) {
    launcher = new IncrementalLauncher(files, emptySet(), cacheDir, false);
    launcher.getEnvironment().setNoClasspath(true);
    launcher.getEnvironment().setAutoImports(true);
    launcher.getEnvironment().disableConsistencyChecks();
  }

  public void addFile(Set<File> files) throws SpoonException {
    try {
      createLauncher(files);
      launcher.buildModel();
      launcher.saveCache();
    } catch (Exception e) {
      throw new SpoonException(e);
    }

  }

  /**
   * @return
   * @see spoon.Launcher#getModel()
   */

  public CtModel getModel() {
    return launcher.getModel();
  }

}
