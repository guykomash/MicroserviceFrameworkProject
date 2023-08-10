package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;

import java.util.concurrent.CountDownLatch;

/**
 * CPU service is responsible for handling the {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {
    private CPU cpu;
    private CountDownLatch latch;
    public CPUService(String name,CPU cpu) {
        super(name);
        this.cpu=cpu;
    }
    public void setLatch(CountDownLatch latch){
        this.latch=latch;
    }

    protected void initialize() {
        cpu.getCluster().addCpuToCollection(cpu);
        cpu.getCluster().pushCpuToMap(cpu);
       subscribeBroadcast(TickBroadcast.class,(TickBroadcast b)->{
           cpu.askForDataBatch();
           cpu.processDataBatch();
       });
       subscribeBroadcast(TerminateBroadcast.class,(TerminateBroadcast b)->{
           terminate();
       });
        latch.countDown();
    }

}
