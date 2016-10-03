package wreulicke.javassist.test;

import java.util.Map;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.junit.Test;

import com.google.auto.service.AutoService;
import com.google.common.base.CaseFormat;
import com.strobel.reflection.Type;
import com.strobel.reflection.Types;

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

  /**
   * クラスファイル書き出しからデコンパイルして表示まで
   */
  @Test
  public void createSimpleSubClassTest() throws Exception {
    ClassGenerator.test("test.test.Test$case1", (pool, clazz) -> {
      clazz.setSuperclass(pool.getCtClass(AbstractProcessor.class.getName()));
    });
  }

  /**
   * アノテーション付けてファイル書き出し
   */
  @Test
  public void annotationAddTest() throws Exception {

    ClassGenerator.test("test.test.Test$case2", (pool, clazz) -> {
      clazz.setSuperclass(pool.getCtClass(AbstractProcessor.class.getName()));
      ConstPool constPool = clazz.getClassFile()
        .getConstPool();
      AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
      Annotation annotation = new Annotation(AutoService.class.getName(), constPool);
      ClassMemberValue memberValue = new ClassMemberValue(Processor.class.getName(), constPool);
      annotation.addMemberValue("value", memberValue);
      attr.addAnnotation(annotation);
      clazz.getClassFile()
        .addAttribute(attr);
    });
  }

  /**
   * フィールドとゲッターにアノテーション付与
   */
  @Test
  public void createMethodAndaddAnnotation() throws Exception {
    ClassGenerator.test("test.test.Test$case3", (pool, clazz) -> {
      clazz.setSuperclass(pool.getCtClass(AbstractProcessor.class.getName()));
      CtField field = CtField.make("int x;", clazz);
      ConstPool constPool = clazz.getClassFile()
        .getConstPool();
      AnnotationsAttribute attr1 = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
      attr1.addAnnotation(new Annotation(Inject.class.getName(), constPool));
      field.getFieldInfo()
        .addAttribute(attr1);
      clazz.addField(field);
      CtMethod method = CtNewMethod.getter("get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, field.getName()), field);
      AnnotationsAttribute attr2 = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
      attr2.addAnnotation(new Annotation(Produces.class.getName(), constPool));
      method.getMethodInfo()
        .addAttribute(attr2);
      clazz.addMethod(method);
    });
  }

  /**
   * ジェネリクス
   * procyonのTypeを使ったgenericType生成からの型引数を持つ型の仮引数のクラス生成
   */
  @Test
  public void typeVariableArgumentTest1() throws Exception {
    ClassGenerator.test("test.test.Test$case4", (pool, clazz) -> {
      final Type<Map> map = Type.of(Map.class);
      final Type<Map<String, Integer>> boundMap = map.makeGenericType(Types.String, Types.Integer);

      clazz.setSuperclass(pool.getCtClass(AbstractProcessor.class.getName()));
      CtMethod method = CtNewMethod.make("public Object test(Object x, Object y){return null;}", clazz);
      method.setGenericSignature("(" + boundMap.getSignature() + "" + boundMap.getSignature() + ")" + boundMap.getSignature());
      clazz.addMethod(method);
    });
  }

  /**
   * ジェネリクス
   * 普通にjavassistの機能でもできたっぽい
   */
  @Test
  public void typeVariableArgumentTest2() throws Exception {
    ClassGenerator.test("test.test.Test$case5", (pool, clazz) -> {
      clazz.setSuperclass(pool.get(AbstractProcessor.class.getName()));
      CtMethod method = CtNewMethod.make("public Object test(Object x, Object y){return null;}", clazz);

      SignatureAttribute.Type type = new ClassType(Map.class.getName(), new TypeArgument[] {
        new TypeArgument(new ClassType(String.class.getName())),
        new TypeArgument(new ClassType(Integer.class.getName()))
      });
      MethodSignature signature = new MethodSignature(null, new SignatureAttribute.Type[] {
        type,
        type
      }, type, null);
      method.setGenericSignature(signature.encode());
      clazz.addMethod(method);
    });
  }

  /**
   * ジェネリックなメソッド生成
   */
  @Test
  public void genericMethodCreationTest() throws Exception {
    ClassGenerator.test("test.test.Test$case6", (pool, clazz) -> {
      clazz.setSuperclass(pool.get(AbstractProcessor.class.getName()));
      CtMethod method = CtNewMethod.make("public Object test(Object x, Object y){return null;}", clazz);
      ClassType type = new ClassType(Map.class.getName(), new TypeArgument[] {
        new TypeArgument(new ClassType(String.class.getName())),
        new TypeArgument(new ClassType(Integer.class.getName()))
      });
      TypeParameter parameter = new TypeParameter("T", type, null);
      TypeVariable variable = new SignatureAttribute.TypeVariable("T");
      MethodSignature signature = new MethodSignature(new SignatureAttribute.TypeParameter[] {
        parameter
      }, new SignatureAttribute.Type[] {
        variable,
        type
      }, variable, null);
      method.setGenericSignature(signature.encode());
      clazz.addMethod(method);
    });
  }

  /**
   * ジェネリックなメソッド生成2 こいつは無理
   * 
   * @throw AssersionError
   */
  @Test(expected = AssertionError.class)
  public void multiInterfaceGenericMethod() throws Exception {
    ClassGenerator.test("test.test.Test$case7", (pool, clazz) -> {
      clazz.setSuperclass(pool.get(AbstractProcessor.class.getName()));
      CtMethod method = CtNewMethod.make("public Object test(Object x, Object y){return null;}", clazz);
      ClassType type = new ClassType(Map.class.getName(), new TypeArgument[] {
        new TypeArgument(new ClassType(String.class.getName())),
        new TypeArgument(new ClassType(Integer.class.getName()))
      });
      TypeParameter parameter = new TypeParameter("T", null, new ObjectType[] {
        type,
        new ClassType(Cloneable.class.getName())
      });
      TypeVariable variable = new SignatureAttribute.TypeVariable("T");
      MethodSignature signature = new MethodSignature(new SignatureAttribute.TypeParameter[] {
        parameter
      }, new SignatureAttribute.Type[] {
        variable,
        type
      }, variable, null);
      method.setGenericSignature(signature.encode());
      clazz.addMethod(method);
    });
  }
}
