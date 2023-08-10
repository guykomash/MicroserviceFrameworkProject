package bgu.spl.mics.application.objects;


//import bgu.spl.mics.MessageBusImpl;
//import bgu.spl.mics.MicroService;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;
//import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {
		private LinkedBlockingDeque<GPU> GPUS;
		private LinkedBlockingDeque<CPU> CPUS;
		private LinkedBlockingDeque<DataBatch> unProcessedDataBatches;
		private HashMap<Integer,PriorityQueue<CPU>> cpuPriorityMap;
		private  Object lockAdd;
		private Object lockSendToCpu;
		private Object lockSendToCpu2;

		//Statistics - check if need to be public
		public LinkedBlockingDeque<String> TrainedModels;
		public AtomicInteger totalProcessedDataBatch;
		public AtomicInteger numOfCPUTimeUnitUsed;
		public AtomicInteger numOfGPUTimeUnitUsed;

		private static class ClusterHolder {
			private static Cluster instance = new Cluster();
		}

		private Cluster(){
		GPUS=new LinkedBlockingDeque<GPU>();
		CPUS=new LinkedBlockingDeque<CPU>();
		unProcessedDataBatches=new LinkedBlockingDeque<DataBatch>();
		TrainedModels=new LinkedBlockingDeque<String>();
		cpuPriorityMap=new HashMap<Integer,PriorityQueue<CPU>>();
		lockAdd=new Object();
		lockSendToCpu=new Object();
		totalProcessedDataBatch= new AtomicInteger();
		numOfCPUTimeUnitUsed = new AtomicInteger();
		numOfGPUTimeUnitUsed = new AtomicInteger();
		lockSendToCpu2=new Object();
		}

	/**
     * Retrieves the single instance of this class.
     */
	public void addGpuToCollection(GPU gpu){
		GPUS.push(gpu);
	}
	public void addCpuToCollection(CPU cpu){
		CPUS.push(cpu);
	}
	public void addUnprocessedDataBatch(DataBatch dataBatch) { //check for sync
		synchronized (lockAdd) {
			unProcessedDataBatches.add(dataBatch);
		}
	}
//	}
	public void sendDataBatchToCpu() { //check for sync
		synchronized (lockSendToCpu) {
			if (!unProcessedDataBatches.isEmpty()) {
				DataBatch dataBatch = unProcessedDataBatches.remove();
				PriorityQueue<CPU> cpuBest = new PriorityQueue(new ComperatorCpuWithDataBatch(dataBatch));
				for (Map.Entry<Integer,PriorityQueue<CPU>> set :
					cpuPriorityMap.entrySet()){
					CPU cpu=set.getValue().peek();
					cpuBest.add(cpu);
				}
				CPU best = cpuBest.remove();

					best.setTimeLeftToProcess(best.TimeToProcess(dataBatch));//sets new time after adding the new
					best.addUnprocessedData(dataBatch);
					cpuPriorityMap.remove(best.getCores(), best);
					pushCpuToMap(best);
				}
			}

	}
	public void sendDataBatchToGpu(DataBatch dataBatch) throws InterruptedException {//check for sync
		GPU gpu=dataBatch.getGpu();
		gpu.getProcessedBatchFromCluster(dataBatch);

	}

	public static Cluster getInstance() {
		return ClusterHolder.instance;
	}


	public class ComperatorCpu implements Comparator<CPU>{
		public int compare(CPU c1,CPU c2){
			return c1.getTimeLeftToProcess()-c2.getTimeLeftToProcess();
		}
	}
	public class ComperatorCpuWithDataBatch implements Comparator<CPU>{
		private DataBatch extraData;
		public ComperatorCpuWithDataBatch(DataBatch extraData){
			this.extraData=extraData;
		}
		public int compare(CPU c1,CPU c2){
			return c1.getTimeLeftToProcess()+c1.TimeToProcess(extraData)-c2.getTimeLeftToProcess()-c2.TimeToProcess(extraData);
		}
	}
	public void pushCpuToMap(CPU cpu){
		int cores=cpu.getCores();
		if(cpuPriorityMap.containsKey(cores))//check for sync
			cpuPriorityMap.get(cores).add(cpu);
		else {
			PriorityQueue<CPU> newCpuQueue=new PriorityQueue<CPU>(new ComperatorCpu());
			newCpuQueue.add(cpu);
			cpuPriorityMap.put(cores,newCpuQueue);
		}
	}

	public void setNewCpuTime(CPU cpu,int newTime){
		cpuPriorityMap.remove(cpu.getCores(),cpu);
		cpu.setTimeLeftToProcess(newTime);
		pushCpuToMap(cpu);
	}

	public String toStringforOutput(){
		return "cpuTimeUsed"+numOfCPUTimeUnitUsed+"\n"+"gpuTimeUsed: "+numOfGPUTimeUnitUsed+"\n"+"batchesProcessed: "+totalProcessedDataBatch+"\n";
	}

}
