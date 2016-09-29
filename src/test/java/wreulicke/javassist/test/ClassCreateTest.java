package wreulicke.javassist.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.windup.decompiler.api.Decompiler;
import org.jboss.windup.decompiler.procyon.ProcyonDecompiler;
import org.junit.Test;

import com.google.auto.service.AutoService;
import com.google.common.base.CaseFormat;
import com.strobel.reflection.Type;
import com.strobel.reflection.Types;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.SignatureAttribute.ClassType;
import javassist.bytecode.SignatureAttribute.MethodSignature;
import javassist.bytecode.SignatureAttribute.ObjectType;
import javassist.bytecode.SignatureAttribute.TypeArgument;
import javassist.bytecode.SignatureAttribute.TypeParameter;
import javassist.bytecode.SignatureAttribute.TypeVariable;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.ClassMemberValue;

public class ClassCreateTest {
  Path expectedRoot = Paths.get("src", "test", "resources", "expected");

  /**
   * クラスファイル書き出しからデコンパイルして表示まで
   */
  @Test
  public void createSimpleSubClassTest() throws Exception {
    ClassPool pool = ClassPool.getDefault();
    CtClass ctClass = pool.makeClass("test.test.Test$case1");
    ctClass.setSuperclass(pool.getCtClass(AbstractProcessor.class.getName()));
    ctClass.writeFile();

    String clazzName = ctClass.getName().replace(".", File.separator);
    Decompiler decompiler = new ProcyonDecompiler();
    Path root = Paths.get("./").normalize().toAbsolutePath();
    Path clazzDest = root.resolve(clazzName + ".class");
    decompiler.decompileClassFile(root, clazzDest, root);

    Path srcDest = root.resolve(clazzName + ".java");
    checkDiff(srcDest);

    Files.deleteIfExists(clazzDest);
    Files.deleteIfExists(srcDest);
  }

  /**
   * アノテーション付けてファイル書き出し
   */
  @Test
  public void annotationAddTest() throws Exception {
    ClassPool pool = ClassPool.getDefault();
    CtClass ctClass = pool.makeClass("test.test.Test$case2");
    ctClass.setSuperclass(pool.getCtClass(AbstractProcessor.class.getName()));
    ConstPool constPool = ctClass.getClassFile().getConstPool();
    AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
    Annotation annotation = new Annotation(AutoService.class.getName(), constPool);
    ClassMemberValue memberValue = new ClassMemberValue(Processor.class.getName(), constPool);
    annotation.addMemberValue("value", memberValue);
    attr.addAnnotation(annotation);
    ctClass.getClassFile().addAttribute(attr);
    ctClass.writeFile();

    String clazzName = ctClass.getName().replace(".", File.separator);
    Decompiler decompiler = new ProcyonDecompiler();
    Path root = Paths.get("./").normalize().toAbsolutePath();

    Path clazzDest = root.resolve(clazzName + ".class");
    decompiler.decompileClassFile(root, clazzDest, root);
    Path srcDest = root.resolve(clazzName + ".java");
    checkDiff(srcDest);

    Files.deleteIfExists(clazzDest);
    Files.deleteIfExists(srcDest);
  }

  /**
   * フィールドとゲッターにアノテーション付与
   */
  @Test
  public void createMethodAndaddAnnotation() throws Exception {

    ClassPool pool = ClassPool.getDefault();
    CtClass ctClass = pool.makeClass("test.test.Test$case3");
    ctClass.setSuperclass(pool.getCtClass(AbstractProcessor.class.getName()));
    CtField field = CtField.make("int x;", ctClass);
    ConstPool constPool = ctClass.getClassFile().getConstPool();
    AnnotationsAttribute attr1 = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
    attr1.addAnnotation(new Annotation(Inject.class.getName(), constPool));
    field.getFieldInfo().addAttribute(attr1);
    ctClass.addField(field);
    CtMethod method = CtNewMethod.getter("get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, field.getName()), field);
    AnnotationsAttribute attr2 = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
    attr2.addAnnotation(new Annotation(Produces.class.getName(), constPool));
    method.getMethodInfo().addAttribute(attr2);
    ctClass.addMethod(method);
    ctClass.writeFile();

    String clazzName = ctClass.getName().replace(".", File.separator);
    Decompiler decompiler = new ProcyonDecompiler();
    Path root = Paths.get("./").normalize().toAbsolutePath();
    Path clazzDest = root.resolve(clazzName + ".class");
    decompiler.decompileClassFile(root, clazzDest, root);

    Path srcDest = root.resolve(clazzName + ".java");
    checkDiff(srcDest);


    Files.deleteIfExists(clazzDest);
    Files.deleteIfExists(srcDest);
  }

  /**
   * ジェネリクス
   * procyonのTypeを使ったgenericType生成からの型引数を持つ型の仮引数のクラス生成
   */
  @Test
  public void typeVariableArgumentTest1() throws Exception {
    final Type<Map> map = Type.of(Map.class);
    final Type<Map<String, Integer>> boundMap = map.makeGenericType(Types.String, Types.Integer);

    ClassPool pool = ClassPool.getDefault();
    CtClass ctClass = pool.makeClass("test.test.Test$case4");
    ctClass.setSuperclass(pool.getCtClass(AbstractProcessor.class.getName()));
    CtMethod method = CtNewMethod.make("public Object test(Object x, Object y){return null;}", ctClass);
    method.setGenericSignature("(" + boundMap.getSignature() + "" + boundMap.getSignature() + ")" + boundMap.getSignature());
    ctClass.addMethod(method);
    ctClass.writeFile();
    String clazzName = ctClass.getName().replace(".", File.separator);
    Decompiler decompiler = new ProcyonDecompiler();
    Path root = Paths.get("./").normalize().toAbsolutePath();
    Path clazzDest = root.resolve(clazzName + ".class");
    decompiler.decompileClassFile(root, clazzDest, root);

    Path srcDest = root.resolve(clazzName + ".java");
    checkDiff(srcDest);

    Files.deleteIfExists(clazzDest);
    Files.deleteIfExists(srcDest);
  }

  /**
   * ジェネリクス
   * 普通にjavassistの機能でもできたっぽい
   */
  @Test
  public void typeVariableArgumentTest2() throws Exception {
    ClassPool pool = ClassPool.getDefault();
    CtClass ctClass = pool.makeClass("test.test.Test$case5");
    ctClass.setSuperclass(pool.getCtClass(AbstractProcessor.class.getName()));
    CtMethod method = CtNewMethod.make("public Object test(Object x, Object y){return null;}", ctClass);

    SignatureAttribute.Type type = new ClassType(Map.class.getName(), new TypeArgument[] {
        new TypeArgument(new ClassType(String.class.getName())),
        new TypeArgument(new ClassType(Integer.class.getName()))});
    MethodSignature signature = new MethodSignature(null, new SignatureAttribute.Type[] {type, type}, type, null);
    method.setGenericSignature(signature.encode());
    ctClass.addMethod(method);
    ctClass.writeFile();
    String clazzName = ctClass.getName().replace(".", File.separator);
    Decompiler decompiler = new ProcyonDecompiler();
    Path root = Paths.get("./").normalize().toAbsolutePath();
    Path clazzDest = root.resolve(clazzName + ".class");
    decompiler.decompileClassFile(root, clazzDest, root);

    Path srcDest = root.resolve(clazzName + ".java");
    checkDiff(srcDest);

    Files.deleteIfExists(clazzDest);
    Files.deleteIfExists(srcDest);
  }

  /**
   * ジェネリックなメソッド生成
   */
  @Test
  public void genericMethodCreationTest() throws Exception {
    ClassPool pool = ClassPool.getDefault();
    CtClass ctClass = pool.makeClass("test.test.Test$case6");
    ctClass.setSuperclass(pool.getCtClass(AbstractProcessor.class.getName()));
    CtMethod method = CtNewMethod.make("public Object test(Object x, Object y){return null;}", ctClass);
    ClassType type = new ClassType(Map.class.getName(), new TypeArgument[] {
        new TypeArgument(new ClassType(String.class.getName())),
        new TypeArgument(new ClassType(Integer.class.getName()))});
    TypeParameter parameter = new TypeParameter("T", type, null);
    TypeVariable variable = new SignatureAttribute.TypeVariable("T");
    MethodSignature signature = new MethodSignature(new SignatureAttribute.TypeParameter[] {parameter}, new SignatureAttribute.Type[] {
        variable,
        type}, variable, null);
    method.setGenericSignature(signature.encode());
    ctClass.addMethod(method);
    ctClass.writeFile();
    String clazzName = ctClass.getName().replace(".", File.separator);
    Decompiler decompiler = new ProcyonDecompiler();
    Path root = Paths.get("./").normalize().toAbsolutePath();
    Path clazzDest = root.resolve(clazzName + ".class");
    decompiler.decompileClassFile(root, clazzDest, root);

    Path srcDest = root.resolve(clazzName + ".java");
    checkDiff(srcDest);


    Files.deleteIfExists(clazzDest);
    Files.deleteIfExists(srcDest);
  }

  /**
   * ジェネリックなメソッド生成2 こいつは無理
   * @throw AssersionError
   */
  @Test
  public void multiInterfaceGenericMethod() throws Exception {
    ClassPool pool = ClassPool.getDefault();
    CtClass ctClass = pool.makeClass("test.test.Test$case7");
    ctClass.setSuperclass(pool.getCtClass(AbstractProcessor.class.getName()));
    CtMethod method = CtNewMethod.make("public Object test(Object x, Object y){return null;}", ctClass);
    ClassType type = new ClassType(Map.class.getName(), new TypeArgument[] {
        new TypeArgument(new ClassType(String.class.getName())),
        new TypeArgument(new ClassType(Integer.class.getName()))});
    TypeParameter parameter = new TypeParameter("T", null, new ObjectType[]{type, new ClassType(Cloneable.class.getName())});
    TypeVariable variable = new SignatureAttribute.TypeVariable("T");
    MethodSignature signature = new MethodSignature(new SignatureAttribute.TypeParameter[] {parameter}, new SignatureAttribute.Type[] {
        variable,
        type}, variable, null);
    method.setGenericSignature(signature.encode());
    ctClass.addMethod(method);
    ctClass.writeFile();
    String clazzName = ctClass.getName().replace(".", File.separator);
    Decompiler decompiler = new ProcyonDecompiler();
    Path root = Paths.get("./").normalize().toAbsolutePath();
    Path clazzDest = root.resolve(clazzName + ".class");
    decompiler.decompileClassFile(root, clazzDest, root);
    Path srcDest = root.resolve(clazzName + ".java");
    checkDiff(srcDest);
    Files.deleteIfExists(clazzDest);
    Files.deleteIfExists(srcDest);
  }
  public void checkDiff(Path srcDest) throws IOException{
    List<String> decompiled = Files.readAllLines(srcDest);
    List<String> expected = Files.readAllLines(expectedRoot.resolve(srcDest.getFileName()));
    Patch patch = DiffUtils.diff(decompiled, expected);
    checkDiff(patch);
  }
  public void checkDiff(Patch patch){
    try{
      assertEquals("has not diff", 0, patch.getDeltas().size());
    }catch (Throwable e) {
      for (Delta delta : patch.getDeltas()) {
        delta.getOriginal().getLines().forEach(x->System.out.println("+"+x));
        delta.getRevised().getLines().forEach(x->System.out.println("-"+x));
      }
      throw e;
    }
  }
}
