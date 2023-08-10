package bgu.spl.mics.application.objects;

import bgu.spl.mics.MicroService;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a data used by a model.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class DataBatch {
    enum Status {UnProcessed, Processed, Trained}

    private Data data;
    private int start_index;
    private Status status;
    private AtomicInteger start_time;
    private GPU gpu=null;


    public DataBatch(Data data,int start_index){
        this.data=data;
        this.start_index=start_index;
        start_time=new AtomicInteger(0);
    }
    public Status getStatus(){return status;}

    public void setStatusToTrained(){
        status=Status.Trained;
    }
    public void setStatusToProcessed(){
        status=Status.Processed;
    }
    public AtomicInteger getStart_time(){return start_time;}
    public void advanceTime(){
       start_time.incrementAndGet();
    }
    public AtomicInteger getTime(){
        return start_time;
    }
    public void resetStartTime(){
        start_time.set(0);
    }
    public Data getData(){return data;}

    public void setGpu(GPU m){
        gpu=m;
    }
    public GPU getGpu(){
        return gpu;
    }
}
