package xyz.keksdose.spoon.langserver;

import static java.util.Collections.emptySet;
import java.io.File;
import java.util.Set;
import spoon.IncrementalLauncher;
import spoon.SpoonException;
import spoon.reflect.CtModel;
import spoon.support.compiler.VirtualFile;

public class Compiler {


  private IncrementalLauncher launcher;
  private String cachePath = ".spoonCache";
  private File cacheDir;

  public Compiler() {
    cacheDir = new File(cachePath);
    cacheDir.mkdir();
    createLauncher(emptySet());
  }

  private void createLauncher(Set<VirtualFile> files) {

    launcher = new IncrementalLauncher(emptySet(), emptySet(), cacheDir, false);
    files.forEach(launcher::addInputResource);
    launcher.getEnvironment().setNoClasspath(true);
    launcher.getEnvironment().setAutoImports(true);
    launcher.getEnvironment().setShouldCompile(false);
    launcher.getEnvironment().disableConsistencyChecks();
  }

  public void addFile(Set<VirtualFile> files) throws SpoonException {
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
