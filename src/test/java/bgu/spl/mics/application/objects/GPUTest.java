package bgu.spl.mics.application.objects;
import bgu.spl.mics.Event;

import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TrainModelEvent;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

public class GPUTest extends TestCase {
    private GPU gpu;
    private Data data;
    private Student student;
    private Model model;
    private Event<Model> testevent;
    private Event<Model> trainevent;
    private Cluster cluster;

    @Before
    public void setUp() {
        gpu = new GPU("RTX3090");
        data = new Data("Images", 5000);
        student = new Student("Simba", "Computer Science", "MSc");
        model = new Model("TestModel", data, student);
        testevent = new TestModelEvent(model);
        trainevent = new TrainModelEvent(model);
        cluster = Cluster.getInstance();
    }

    @Test
    public void testCleanAllTrainedData() {
        gpu.trainedDataBatches.set(1);
        gpu.dataBatchSize.set(1);
        gpu.setModel(model);
        gpu.setEvent(testevent);
        gpu.cleanAllTrainedData();
        assertEquals("after executing cleanAllTrainedData trainedDataBatches should be 0", gpu.trainedDataBatches.get(), 0);
        assertEquals("after executing dataBatchSize trainedDataBatches should be 0", gpu.dataBatchSize.get(), 0);
        assertEquals("model should be null", gpu.getModel(), null);
        assertEquals("event should be null", gpu.getEvent(), null);

    }

    @Test
    public void testMakeMeUnbusy() {
        assertEquals("at first gpu should be unbusy", gpu.isBusy(), false);
        gpu.makeMeBusy();
        assertEquals("gpu should be busy", gpu.isBusy(), true);
        gpu.makeMeUnbusy();
        assertEquals("now gpu should be unbusy", gpu.isBusy(), false);
    }

    @Test
    public void testMakeMeBusy() {
        assertEquals("at first gpu should be unbusy", gpu.isBusy(), false);
        gpu.makeMeBusy();
        assertEquals("gpu should be busy", gpu.isBusy(), true);
    }

    @Test
    public void testSetEvent() {
        gpu.setEvent(trainevent);
        assertEquals("gpu event should be the one that been set to", trainevent, gpu.getEvent());
    }
    @Test
    public void testSetModel() {
        Model newModel = new Model("NewModel", data, student);
        gpu.setModel(newModel);
        assertEquals("new model should be the gpu model now", gpu.getModel(), newModel);
    }
    @Test
    public void testDivideData() {
        gpu.divideData(data);
        int databatchsize = gpu.dataBatchSize.get();
        int unprocessed = gpu.getUnprocessedDataBatchesSize();
        assertEquals("Data Batch Size should be 5", 5, databatchsize);
        assertEquals("unprocessedatabatch size  should be 5", 5, unprocessed);
    }
    @Test
    public void testTrainDataBach() {
        gpu.divideData(data);
        for (DataBatch dataBatch : gpu.getUnprocessedDataBatches()) {
            gpu.getProcessedDataBatch().add(dataBatch);
        }
        int databatchsize = gpu.dataBatchSize.get();
        for (int i = 0; i < databatchsize; i++) {
            gpu.trainDataBach();
        }
        assertEquals("processedDataBatch size should be 0", 0, gpu.getProcessedDataBatchesSize());
        assertEquals("trained data batch number should be 5", 5, gpu.trainedDataBatches.get());
    }
    @Test
    public void testIsAllDataTrained() {
        gpu.divideData(data);
        for (DataBatch dataBatch : gpu.getUnprocessedDataBatches()) {
            gpu.getProcessedDataBatch().add(dataBatch);
        }
        int databatchsize = gpu.dataBatchSize.get();
        for (int i = 0; i < databatchsize; i++) {
            gpu.trainDataBach();
        }
        assertEquals("after 5 trainDataBatch all data should be trained", true, gpu.isAllDataTrained());
    }
    @Test
    public void testSendDataBatchToCluster() {
        gpu.divideData(data);
        int canSend = gpu.getCanSend();
        gpu.sendDataBatchToCluster();
        assertEquals("gpu canSend should decrease by databatchsize", canSend - gpu.dataBatchSize.get(), gpu.getCanSend());
    }
}