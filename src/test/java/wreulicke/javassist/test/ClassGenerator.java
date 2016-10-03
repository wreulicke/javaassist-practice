package wreulicke.javassist.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.BiConsumer;

import org.jboss.windup.decompiler.api.Decompiler;
import org.jboss.windup.decompiler.procyon.ProcyonDecompiler;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import javassist.ClassPool;
import javassist.CtClass;

public class ClassGenerator {
  private ClassPool pool;
  private static ClassGenerator instance = new ClassGenerator();

  public ClassGenerator() {
    pool = ClassPool.getDefault();
  }

  public CtClass create(String name) {
    return pool.makeClass(name);
  }

  public static CtClass createTestClass(String name) {
    return instance.create(name);
  }

  public static void test(String name, UnsafeBiConsumer<ClassPool, CtClass> consumer) throws Exception {
    CtClass clazz = createTestClass(name);
    consumer.accept(instance.pool, clazz);
    clazz.writeFile();
    String clazzName = clazz.getName().replace(".", File.separator);
    Decompiler decompiler = new ProcyonDecompiler();
    Path root = Paths.get("./").normalize().toAbsolutePath();
    Path clazzDest = root.resolve(clazzName + ".class");
    decompiler.decompileClassFile(root, clazzDest, root);
    Path srcDest = root.resolve(clazzName + ".java");
    
    try {
      checkDiff(srcDest);
    } finally {
      Files.deleteIfExists(clazzDest);
      Files.deleteIfExists(srcDest);
    }
  }

  public static void checkDiff(Path srcDest) throws IOException {
    List<String> decompiled = Files.readAllLines(srcDest);
    List<String> expected = Files.readAllLines(Paths.get("src", "test", "resources", "expected", srcDest.getFileName().toString()));
    Patch patch = DiffUtils.diff(decompiled, expected);
    checkDiff(patch);
  }

  public static void checkDiff(Patch patch) {
    try {
      assertEquals("has not diff", 0, patch.getDeltas().size());
    } catch (Throwable e) {
      for (Delta delta : patch.getDeltas()) {
        delta.getOriginal().getLines().forEach(x -> System.out.println("+" + x));
        delta.getRevised().getLines().forEach(x -> System.out.println("-" + x));
      }
      throw e;
    }
  }

  public static interface UnsafeBiConsumer<T1,T2> extends BiConsumer<T1,T2>{
    public void apply(T1 t1, T2 t2) throws Exception;
    @Override
    default void accept(T1 t1,T2 t2) {
      try{
        apply(t1,t2);
      }catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
