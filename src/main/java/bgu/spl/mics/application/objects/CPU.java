package bgu.spl.mics.application.objects;
//import bgu.spl.mics.MicroService;
//import java.util.Collection;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class CPU {
    private int cores;
    private LinkedBlockingDeque<DataBatch> unprocessedData;
    private LinkedBlockingDeque<DataBatch> processedData;
    private Cluster cluster;
    private  volatile int timeLeftToProcess;
    private volatile int totalTime;
    private volatile int lastTimeUpdated;
    private int tempcheck;

    
    public CPU(int cores){
        timeLeftToProcess=0;
        this.cores=cores;
        this.cluster=Cluster.getInstance();
        unprocessedData=new LinkedBlockingDeque<DataBatch>();
        processedData=new LinkedBlockingDeque<DataBatch>();
        totalTime=0;
        lastTimeUpdated=0;
        tempcheck=0;
    }

    /**
     * @post trivial (simple getter)
     */
    public int getCores(){return this.cores;}

    /**
     * @post trivial (simple getter)
     */
    public int getUnprocessedDataSize(){return unprocessedData.size();}

    /**
     * @post trivial (simple getter)
     */
    public int getProcessedDataSize(){return processedData.size();}

    /**
     * @pre none
     * @inv
     * @post if cpu is the best,@pre unprocessedDataBatch.size()+1=@post unprocessedDataBatch.size()
     */
    public void askForDataBatch(){
            cluster.sendDataBatchToCpu();
    }
    /**
     * @pre dataBatch!=null
     * @inv
     * @post @pre unprocessedDataBatch.size()+1=@post unprocessedDataBatch.size() && @pre timeLeftToProcess+TimeToProcess(dataBatch)=@post timeLeftToProcess
     */
    public void addUnprocessedData(DataBatch dataBatch) {
       unprocessedData.add(dataBatch);
       setTimeLeftToProcess(TimeToProcess(dataBatch));
    }
    /**
     * @pre processedData.size>0
     * @post @pre(processedData.size())=processedData.size()-1;
     */
    public void sendDataBatchToCluster(DataBatch dataBatch) throws InterruptedException {//check for sync
        cluster.sendDataBatchToGpu(dataBatch);

    }
    /**
     * @param
     * @pre dataBatch!=null , dataBatch.status=UnProcessed
     * * @inv
     * @post @pre dataBatch.start_time+1=@post dataBatch.start_time
     */
    public void processDataBatch() throws InterruptedException {
        totalTime++;
        if(!unprocessedData.isEmpty()){
            //cpu stats
            DataBatch dataBatch=unprocessedData.peek();
            dataBatch.advanceTime();
            cluster.numOfCPUTimeUnitUsed.incrementAndGet(); //Statistics
            if(TimeToProcess(dataBatch)<=dataBatch.getTime().get()) {
                dataBatch.setStatusToProcessed();
                cluster.totalProcessedDataBatch.incrementAndGet(); //Statistics
                dataBatch.resetStartTime();
                sendDataBatchToCluster(dataBatch);
                cluster.setNewCpuTime(this,TimeToProcess(dataBatch)*(-1));
                unprocessedData.remove(dataBatch);
                dataBatch.getData().increaseProcessedData();
            }
        }
    }
    /**
     * @param dataBatch
     * @pre dataBatch!=null
     * * @inv
     * @post simple colculation
     */
    public int TimeToProcess(DataBatch dataBatch){
        if(dataBatch.getData().getType()== Data.Type.Images)
            return (32/cores)*4;
        if(dataBatch.getData().getType()== Data.Type.Text)
            return (32/cores)*2;
        return (32/cores);
    }
    /**
     * @param
     * @pre
     * * @inv
     * @post simple getter
     */
    public int getTimeLeftToProcess(){
        return timeLeftToProcess;
    }
    /**
     * @param
     * @pre
     * * @inv
     * @post simple setter && lastTimeUpdated=totalTime
     */
    public void setTimeLeftToProcess(int extraTime){
        timeLeftToProcess=timeLeftToProcess+extraTime+totalTime-lastTimeUpdated;
        setLastTimeUpdated();
    }
    /**
     * @param
     * @pre
     * * @inv
     * @post simple setter && lastTimeUpdated=totalTime
     */
    public void setLastTimeUpdated(){
        lastTimeUpdated=totalTime;
    }
    /**
     * @param
     * @pre
     * * @inv
     * @post simple getter
     */
    public Cluster getCluster(){
        return cluster;
    }
    /**
     * @param
     * @pre
     * * @inv
     * @post simple
     */
    public DataBatch removeDataBatch(){
       return unprocessedData.remove();
    }

}
