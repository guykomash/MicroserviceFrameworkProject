package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class StudentService extends MicroService {
        private Student student;
        CountDownLatch latch;
        public StudentService(String name,Student student) {
            super(name);
            this.student=student;
            latch=null;
        }

    public void setLatch(CountDownLatch latch){
        this.latch=latch;
    }

    @Override
    protected void initialize() {
            subscribeBroadcast(PublishConferenceBroadcast.class,(PublishConferenceBroadcast b)->{
                Vector<Model> models=b.getListOfModels();
                for(int i=0;i<models.size();i++){
                    if(student!=models.get(i).getStudent())
                        student.increasePapersRead();
                }
            });

            subscribeBroadcast(TerminateBroadcast.class,(TerminateBroadcast b)->{
               if(student.getCurrentFuture()!=null) {
                   student.getCurrentFuture().resolve(student.getCurrentModel());
                   student.setCurrentModel(null);
               }
                terminate();
            });

            Thread sendEvent=new Thread() {
                public void run(){

                    Vector<Model> models=student.getModels();
                    AtomicInteger integer=new AtomicInteger(0);
                    while(!isTerminated() && integer.get()<models.size()){
                        student.setCurrentModel(models.elementAt(integer.get()));
                        if(student.getCurrentModel()!=null) {
                            Future<Model> futureTrain = sendEvent(new TrainModelEvent(models.elementAt(integer.get())));
                            latch.countDown();
                            if(futureTrain!=null) {
                                student.setCurrentFuture(futureTrain);
                                futureTrain.get();
                            }
                        }
                        if(student.getCurrentModel()!=null) {
                            Future<Model> futureTest = sendEvent(new TestModelEvent(models.elementAt(integer.get())));
                            if(futureTest!=null) {
                                student.setCurrentFuture(futureTest);
                                futureTest.get();
                            }
                        }
                        if(student.getCurrentModel()!=null &&student.getCurrentModel().isResultGood()) {
                                sendEvent(new PublishResultsEvent(student.getCurrentModel()));
                                student.increasePublications();
                        }
                        integer.incrementAndGet();
                        student.resetFuture();
                        student.setCurrentModel(null);

                    }
            }
        };
            sendEvent.start();


        // TODO Implement this

    }
}
