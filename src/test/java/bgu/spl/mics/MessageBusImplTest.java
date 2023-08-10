package bgu.spl.mics;

import bgu.spl.mics.application.services.StudentService;
import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import bgu.spl.mics.example.services.ExampleBroadcastListenerService;
import bgu.spl.mics.example.services.ExampleEventHandlerService;
import bgu.spl.mics.example.services.ExampleMessageSenderService;
//import com.sun.tools.javac.code.Attribute;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageBusImplTest {
    private static MessageBusImpl mb;
    private Event event;
    private Broadcast broadcast;
    private MicroService m1;

    @Before
    public void setup(){

        mb=MessageBusImpl.getInstance();
        event=new ExampleEvent("name");
        broadcast=new ExampleBroadcast("name");
        String[]arr={"1"};
        m1= new ExampleEventHandlerService("name",arr);
    }

    @Test
    public void testsubscribeEvent() throws Exception {
        try {
            mb.subscribeEvent(ExampleEvent.class, m1);
            System.out.println("The micro service isn't registered");
        }
        catch (Exception e) {
            mb.register(m1);
            mb.subscribeEvent(ExampleEvent.class,m1);
            mb.unregister(m1);
        }
    }
    @Test
    public void testsubscribeBroadcast() {
        try {
            mb.subscribeBroadcast(broadcast.getClass(), m1);
            System.out.println("The micro service isn't registered");
        }
        catch (Exception e) {
            mb.register(m1);
            mb.subscribeBroadcast(broadcast.getClass(), m1);
            mb.unregister(m1);
        }
    }

    @Test
    public void testcomplete() {
        mb.register(m1);
        mb.subscribeEvent(ExampleEvent.class, m1);
        Future<Boolean> result=mb.sendEvent(event);
        assertEquals(result.isDone(),false);
        mb.complete(event,true);
        assertEquals(result.get(),true);
        mb.unregister(m1);
    }

    @Test
    public void testsendBroadcast() throws Exception {
        mb.register(m1);
        mb.subscribeBroadcast(broadcast.getClass(),m1);
        mb.sendBroadcast(broadcast);
        Message message=mb.awaitMessage(m1);
        assertEquals(message,broadcast);
        mb.unregister(m1);
    }

    @Test
    public void testsendEvent() throws Exception {
        mb.register(m1);
        mb.subscribeEvent(ExampleEvent.class,m1);
        mb.sendEvent(event);
        Message message=mb.awaitMessage(m1);
        assertEquals(message,event);
        mb.unregister(m1);
    }

    @Test
    public void testregister() {
        try {
            mb.subscribeEvent(ExampleEvent.class,m1);
        }
        catch (Exception e) {
            mb.register(m1);
        }
        try {
            mb.subscribeEvent(ExampleEvent.class, m1);
        }
        catch (Exception a){
            fail("after the micro service registered the action should succeed");
        }
        mb.unregister(m1);
    }

    @Test
    public void testunregister() {
        try {
            mb.unregister(m1);
        }
        catch (Exception e){
            mb.register(m1);
            mb.unregister(m1);
        }
    }

    @Test
    public void testawaitMessage() throws InterruptedException {
        mb.register(m1);
        mb.subscribeEvent(ExampleEvent.class,m1);
        mb.sendEvent(event);
        Message message=mb.awaitMessage(m1);
        assertEquals(message,event);
        mb.unregister(m1);
    }
}