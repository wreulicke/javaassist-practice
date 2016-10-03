package test.test;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.annotation.processing.AbstractProcessor;

public class Test$case3 extends AbstractProcessor{
    @Inject
    int x;
    @Produces
    public int getX(){
        return this.x;
    }
}