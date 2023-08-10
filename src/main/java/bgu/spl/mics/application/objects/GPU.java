package bgu.spl.mics.application.objects;

import bgu.spl.mics.Event;
import bgu.spl.mics.Message;
import bgu.spl.mics.application.messages.TestModelEvent;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */

    private Object lockSendToGpu;

    public enum Type {RTX3090, RTX2080, GTX1080}
    private Type type;
    private Model model;
    private Cluster cluster;
    private int size_Vram;
    private int canSend;
    private boolean finishedtrain;
    public AtomicInteger dataBatchSize;
    private Event<Model> event=null;
    private boolean busy;
    private LinkedBlockingQueue<DataBatch> unprocessedDataBatches;
    private LinkedBlockingQueue<DataBatch> processedDataBatches;
    public AtomicInteger trainedDataBatches;
    private LinkedBlockingQueue<Message> eventsToWait;

    public GPU(String type){
        if(type.equals("RTX3090")){
            this.type=Type.RTX3090;
        }
        else if(type.equals("RTX2080"))
            this.type=Type.RTX2080;
        else this.type=Type.GTX1080;
        setVram(this.type);
        canSend=size_Vram;
        model=null;
        cluster=Cluster.getInstance();
        cluster.addGpuToCollection(this);
        unprocessedDataBatches=new LinkedBlockingQueue<DataBatch>();
        processedDataBatches=new LinkedBlockingQueue<DataBatch>();
        eventsToWait=new LinkedBlockingQueue<Message>();
        busy=false;
        lockSendToGpu=new Object();
        dataBatchSize=new AtomicInteger();
        trainedDataBatches=new AtomicInteger();
        finishedtrain=false;
    }
    /**
     *
     * @pre simply getter
     * @inv
     * @post
     */
    public String getTypeString(){
        if(this.type==Type.GTX1080)
            return "GTX1080";
        else if (type==Type.RTX2080)
            return "RTX2080";
        else return "RTX3090";
    }
    /**
     *
     * @pre simply setter
     * @inv
     * @post
     */
    private void setVram(Type type){
        if(type==Type.RTX2080)
            size_Vram=16;
        if(type==Type.GTX1080)
            size_Vram=8;
        if(type==Type.RTX3090)
            size_Vram=32;
    }
    /**
     *
     * @pre none
     * @inv
     * @post trainedDataBatches.get()==0 && dataBatchSize.get()==0 && model=null && event=null
     */
    public void cleanAllTrainedData(){
        this.trainedDataBatches.set(0);
        this.dataBatchSize.set(0);
        model=null;
        event=null;
    }
    /**
     *
     * @pre simple getter
     * @inv
     * @post
     */
    public boolean isBusy() {
        return busy;
    }
    /**
     *
     * @pre none
     * @inv
     * @post busy=true
     */
    public void makeMeUnbusy(){
        busy=false;
    }
    /**
     *
     * @pre none
     * @inv
     * @post busy=false
     */
    public void makeMeBusy(){
        busy=true;
    }
    /**
     * @param event<Model>
     * @pre none
     * @inv
     * @post this.event=event
     */
    public void setEvent(Event<Model> event) {
        this.event = event;
    }
    /**
     * @param
     * @pre simple getter
     * @inv
     * @post
     */
    public Event<Model> getEvent(){
        return event;
    }
    /**
     * @param  m
     * @pre none
     * @inv
     * @post this.model=m
     */
    public void setModel(Model m){
        model=m;
    }
    /**
     *
     * @pre none
     * @post trivial(simple getter)
     */
    public Model getModel(){return model;}

    /**
     *
     * @pre none
     * @inv getUnprocessedDataBatchesSize>=0
     * @post unprocessedDataBatches.size()==getUnprocessedDataBatchesSize
     */
    public int getUnprocessedDataBatchesSize(){return unprocessedDataBatches.size();}

    /**
     *
     * @pre none
     * @inv getProcessedDataBatchesSize>=0
     * @post processedDataBatches.size()==getProcessedDataBatchesSize
     */
    public int getProcessedDataBatchesSize(){return processedDataBatches.size(); }

    /**
     *
     * @pre UnprocessedDataBatches.size==0
     * @inv data!=null
     * @post getUnprocessedDataBatchesSize==@pre(getUnprocessedDataBatchesSize)+data.getSize/1000
     */
    public void divideData(Data data){
        int data_size=data.getDataSize()/1000;
        int data_batch_index=0;
        while(data_size>0){
            DataBatch dataBatch=new DataBatch(data,data_batch_index);
            dataBatchSize.incrementAndGet();
            dataBatch.setGpu(this);
            unprocessedDataBatches.add(dataBatch);
            data_size--;
            data_batch_index+=1000;
        }
    }

    /**
     * @param
     * @pre dataBatch!=null
     * @inv
     * @post @pre processedDataBatches.peek().TimeToProcess()-1=@post processedDataBatches.peek().TimeToProcess()
     */
    public void trainDataBach(){
        if(!processedDataBatches.isEmpty()){
            DataBatch dataBatch=processedDataBatches.peek();
            dataBatch.advanceTime();
            cluster.numOfGPUTimeUnitUsed.incrementAndGet();//Statistics
            if(TimeToTrain()<=dataBatch.getTime().get()) {
                dataBatch.setStatusToTrained();
                trainedDataBatches.incrementAndGet();
                processedDataBatches.remove(dataBatch);
                canSend++;
            }
        }
    }
    /**
     * @param
     * @pre none
     * @inv
     * @post simple colculation
     */
    public int TimeToTrain(){
        if(type==Type.RTX2080)
            return 2;
        if(type==Type.GTX1080)
            return 4;
        return 1;
    }

    /**
     * @pre none
     * @inv
     * @post  simple check
     */
    public boolean isAllDataTrained(){
        return  trainedDataBatches.get()==dataBatchSize.get()&&trainedDataBatches.get()!=0;}

    /**
     *
     * @pre getUnprocessedDataBatchesSize>0
     * @inv
     * @post getUnprocessedDataBatchesSize==@pre(getUnprocessedDataBatchesSize-canSend)
     */
    public void sendDataBatchToCluster(){
        while (canSend!=0 && unprocessedDataBatches.size()>0) {
            cluster.addUnprocessedDataBatch(unprocessedDataBatches.remove());
            canSend--;
        }
    }

    /**
     *
     * @pre isProcessedDataBatchesIsFull==false
     * @inv getProcessBatchSize <= size_Vram
     * @post getProcessBatchSize==@pre(getProcessBatchSize())+1
     */
    public void getProcessedBatchFromCluster(DataBatch dataBatch) throws InterruptedException {//check for sync
        synchronized (lockSendToGpu) {
            processedDataBatches.add(dataBatch);
        }
    }

    //Statistics
    public void addTrainedModelName(String name){
        cluster.TrainedModels.add(name);
    }

    //For Test
    public LinkedBlockingQueue<Message> getEventsToWait(){return this.eventsToWait;}

    public LinkedBlockingQueue<DataBatch> getProcessedDataBatch(){
        return processedDataBatches;
    }
    public LinkedBlockingQueue<DataBatch> getUnprocessedDataBatches(){
        return unprocessedDataBatches;
    }
    public int getCanSend(){
        return canSend;
    }
}

