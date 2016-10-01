package wreulicke.javassist.test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiFunction;

import org.junit.Test;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.NotFoundException;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

public class ProxyTest {

  /**
   * final classはもちろん例外吐く
   * 
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  @Test(expected = RuntimeException.class)
  public void testFinalClass() throws InstantiationException, IllegalAccessException {
    ProxyFactory factory = new ProxyFactory();
    factory.setSuperclass(String.class);
    Class<?> c = factory.createClass();
  }

  @Test
  public void testExampleClass() throws InstantiationException, IllegalAccessException, IOException, NotFoundException, CannotCompileException {
    ProxyFactory factory = new ProxyFactory();
    factory.setSuperclass(Example.class);
    factory.setFilter(new MethodFilter() {
      public boolean isHandled(Method m) {
        return true;
      }
    });
    Class<?> c = factory.createClass();
    MethodHandler methodHandler = new MethodHandler() {
      int nest = 0;

      public Object invoke(Object self, Method m, Method proceed, Object[] args) throws Throwable {
        nest++;
        System.out.println(repeat.apply(" ", nest) + "start:" + m.getName());
        Object result = proceed.invoke(self, args); // execute the original method.
        System.out.println(repeat.apply(" ", nest) + "end:" + m.getName());
        nest--;
        return result;
      }
    };
    Example foo = (Example) c.newInstance();
    ((ProxyObject) foo).setHandler(methodHandler);
    /**
     * # below comments are printed when next statement invoked
     * # In private method, method handler is not invoked;
     * start:test
     * private method called
     * start:protectedMethod
     * protected method called
     * end:protectedMethod
     * test called
     * end:test
     */
    foo.test();
  }

  /**
   * めちゃシンプルなケース
   * 
   * @throws NotFoundException
   * @throws ClassNotFoundException
   */
  @Test
  public void testSimpleCreate() throws NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException,
      InvocationTargetException, NotFoundException, ClassNotFoundException {
    ClassPool.getDefault();
    ProxyFactory factory = new ProxyFactory();
    factory.setSuperclass(Example.class);
    Example example = (Example) factory.create(new Class[0], new Object[0], (self, m, p, args) -> {
      System.out.println(m);
      return p.invoke(self, args);
    });
    System.out.println(Example.class.getClassLoader());
    System.out.println(example.getClass().getClassLoader());
    System.out.println(ClassPool.getDefault().getClassLoader());
    example.test();
  }

  private final BiFunction<String, Integer, String> repeat = (s, n) -> String.format(String.format("%%%ds", n), " ").replace(" ", s);

  public static class Example {
    public void test() {
      privateMethod();
      protectedMethod();
      System.out.println("test called");
    }

    private void privateMethod() {
      System.out.println("private method called");
    }

    protected void protectedMethod() {
      System.out.println("protected method called");
    }
  }
}
