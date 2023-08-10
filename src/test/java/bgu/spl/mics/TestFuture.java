package bgu.spl.mics;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class TestFuture {

    private Future<String> future;

    @Before
    public void setUp(){
        future=new Future<String>();
    }
    @Test
    public void testGet(){
        assertTrue("Expects to get false when Future first initialized", future.isDone()==false);
        String result="resolved";
        Thread t1= new Thread(()-> {
            try {
                future.get();
            } catch (Exception e){}
        });
        t1.start();
        future.resolve("result");
        assertTrue(future.isDone());
        assertEquals(future.get(),"result");
    }
    @Test
    public void testResolved(){
        assertFalse("Expect the future not to be done when it first initialized",future.isDone());
        String str="result";
        future.resolve(str);
        assertEquals("Expect the get method return the str value",future.get(),str);
    }
    @Test
    public void testisDone(){
        assertFalse("Expect the future not to be resolved when it first initialized",future.isDone());
        String str="result";
        future.resolve(str);
        assertTrue(future.isDone());
    }
    @Test
    public void testTimoutGet(){
        TimeUnit unit=TimeUnit.MILLISECONDS;
        long timeout=500;
        long timeout2=10000;
        assertTrue("Expects to get false when Future first initialized", future.isDone()==false);
        String result="resolved";
        Thread t1= new Thread(()->{
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            future.resolve(result);
        });
        t1.start();
        assertTrue("Expects to get null when Future first initialized", future.get(timeout,unit) == null);
        assertEquals("Expects to get resolved value when Future resolved", future.get(timeout2,unit), result);
    }
}
