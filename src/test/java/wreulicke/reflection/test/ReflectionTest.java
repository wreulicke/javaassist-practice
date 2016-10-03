package wreulicke.reflection.test;

import static java.util.Arrays.stream;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.Test;

import javassist.expr.ExprEditor;

public class ReflectionTest {
  Consumer<Object> println = System.out::println;
  Function<String, Consumer<Object>> prefix = prefix -> y -> System.out.println(prefix + "\t" + y);
  Function<Type, String> getTypeName = Type::getTypeName;



  @Test
  public void case1() {
    assertThat("test", Sample.class.getName(), is("wreulicke.reflection.test.Sample"));
  }

  @Test
  public void case2() {
    Function<Parameter, Annotation[]> getAnnotations = Parameter::getAnnotations;
    new ExprEditor(){
      
    };
    stream(Sample.class.getDeclaredMethods()).sorted((a, b) -> a.getName()
      .compareTo(b.getName()))
      .forEach(m -> {
        println.accept(m.getReturnType() + " " + m.getName());
        stream(m.getParameters()).flatMap(getAnnotations.andThen(Arrays::stream))
          .forEach(prefix.apply("annotations"));
        stream(m.getParameterTypes()).forEach(x -> {
          Optional<Type> type = Optional.ofNullable(x.getGenericSuperclass());
          type.filter(ParameterizedType.class::isInstance)
            .map(ParameterizedType.class::cast)
            .map(ParameterizedType::getActualTypeArguments)
            .ifPresent(y -> stream(y).forEach(prefix.apply("superclazz type parameter")));
          stream(x.getGenericInterfaces()).map(ParameterizedType.class::cast)
            .map(ParameterizedType::getActualTypeArguments)
            .flatMap(Arrays::stream)
            .forEach(prefix.apply("interface type parameter"));
        });
        stream(m.getGenericParameterTypes()).filter(ParameterizedType.class::isInstance)
          .map(ParameterizedType.class::cast)
          .map(ParameterizedType::getActualTypeArguments)
          .flatMap(Arrays::stream)
          .forEach(println);
        System.out.println();
      });
  }
}
