package wreulicke.javassist.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtNewMethod;

public class GetterCreateTest {

  /**
   * 普通に元からあるクラスを拡張してみる（loadもwriteもしてないのでクラスファイルはそのまま）
   * 
   * @throws Exception
   */

  @Test
  public void test() throws Exception {
    ClassPool pool = ClassPool.getDefault();
    CtClass class1 = pool.get(ModifyTarget.class.getName());
    class1.addMethod(CtNewMethod.getter("getTest", class1.getField("test")));
    System.out.println(class1.getMethod("getTest", "()Ljava/lang/String;"));

  }

  /**
   * publicでも継承したら無理っぽい
   * 
   * @throws Exception
   */
  @Test
  public void publicFieldGetterCreation() throws Exception {
    ClassPool pool = ClassPool.getDefault();
    CtClass class1 = pool.get(ModifyTarget.class.getName());
    CtClass class2 = pool.makeClass(class1.getName() + "$case1");
    class2.setSuperclass(class1);
    CtField f = class2.getField("xxx");
    try {
      // next statement fails. created getter is not declared in class2, so in class1.
      class2.addMethod(CtNewMethod.getter("getTest", class2.getDeclaredField("test")));
      fail();
    } catch (Exception e) {
    }
    class2.addMethod(CtNewMethod.copy(CtNewMethod.getter("getTest", f), class2, null));
    Object o = class2.toClass().newInstance();
    System.out.println(o.getClass().getMethod("getTest").invoke(o));

  }

  /**
   * protected fieldの場合はCtNewMethod.getterでは作れないのでワークアラウンド
   * 
   * @throws Exception
   */
  @Test
  public void protectedFieldGetterCreation() throws Exception {
    ClassPool pool = ClassPool.getDefault();
    CtClass class1 = pool.get(ModifyTarget.class.getName());
    CtClass class2 = pool.makeClass(class1.getName() + "$case2");
    class2.setSuperclass(class1);
    class2.addMethod(CtNewMethod.make("public String getTest(){return test;}", class2));
    Object o = class2.toClass().newInstance();
    System.out.println(o.getClass().getMethod("getTest").invoke(o));

  }

  public static class ModifyTarget {
    protected String test = "test";
    public String xxx = "xxx";
  }
}
