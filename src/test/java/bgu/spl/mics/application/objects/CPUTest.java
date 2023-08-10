package bgu.spl.mics.application.objects;

import junit.framework.TestCase;

public class CPUTest extends TestCase {
    private CPU cpu;
    private Data data;
    private DataBatch dataBatch;
    private Cluster cluster;
    private GPU gpu;
    public void setUp() {
        cpu=new CPU(32);
        data=new Data("Images",10000);
        dataBatch=new DataBatch(data,0);
        cluster=Cluster.getInstance();
        gpu=new GPU("RTX3090");


    }

    public void testAddUnprocessedData() {
        assertTrue("The CPU should be empty",cpu.getUnprocessedDataSize()==0);
        cpu.addUnprocessedData(dataBatch);
        assertTrue("The CPU size should be 1",cpu.getUnprocessedDataSize()==1);
        cpu.removeDataBatch();
    }

    public void testSendDataBatchToCluster() throws InterruptedException {
        gpu=new GPU("RTX3090");
        cluster=Cluster.getInstance();
        dataBatch.setGpu(gpu);
        cpu.addUnprocessedData(dataBatch);
        cpu.sendDataBatchToCluster(dataBatch);
        assertTrue("The processed queue should be increased by 1",gpu.getProcessedDataBatchesSize()==1);
    }

    public void testProcessDataBatch() throws InterruptedException {
        cpu.addUnprocessedData(dataBatch);
        cpu.processDataBatch();
        assertTrue("The data batch should be processed once",dataBatch.getStart_time().get()==1);
        dataBatch.resetStartTime();
    }

    public void testTimeToProcess() {
        assertTrue("The result should be 4",cpu.TimeToProcess(dataBatch)==4);
    }

    public void testGetTimeLeftToProcess() throws InterruptedException {
        assertTrue(cpu.getTimeLeftToProcess()==0);
        cpu.addUnprocessedData(dataBatch);
        assertTrue(cpu.getTimeLeftToProcess()==4);
    }
}