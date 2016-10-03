package wreulicke.reflection.test;

import java.util.ArrayList;
import java.util.Comparator;

public class Sample extends Exception {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  public <T> T sample1(@TestAnnotation T x) {
    return null;
  }

  public <T extends Sample> T sample2(@TestAnnotation @Deprecated T x) {
    return null;
  }

  public <T extends Sample> void sample3(SampleArrayList x1, SampleGenerics1<Sample> x2,SampleGenerics2<Sample> x3, SampleGenericsSubtype<Sample> x4) throws T {}

  public <T extends SampleComparator> void sample4(@Deprecated T x) throws Sample {}

  public static class SampleComparator implements Comparator<Sample> {
    @Override
    public int compare(Sample o1, Sample o2) {
      return 0;
    }
  }
  public static class SampleArrayList extends ArrayList<Sample> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

  }

  public static class SampleGenerics1<T> {

  }

  public static class SampleGenerics2<T> extends ArrayList<T> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

  }

  public static class SampleGenericsSubtype<T extends Sample> extends ArrayList<T> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

  }
}
