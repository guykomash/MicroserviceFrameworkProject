package bgu.spl.mics.application.services;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TestModelEvent;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.objects.Data;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {

    private GPU gpu;
    private CountDownLatch latch;
    private boolean busy;
    private boolean trained;
    private LinkedBlockingQueue<Message> awaitingmessages;


    public GPUService(String name,GPU gpu) {
        super(name);
        this.gpu=gpu;
        busy=false;
        awaitingmessages=new LinkedBlockingQueue<Message>();
        trained=false;


    }
    public void setLatch(CountDownLatch latch){
        this.latch=latch;
    }

    @Override
    protected void initialize() {
        subscribeEvent(TrainModelEvent.class,(TrainModelEvent event) -> {
            if(busy==false) {
                Model model = event.getModel(); //get Model from Event
                model.setStatusToTraining(); //Change Model status to Training
                gpu.setModel(model); // set the model field in gpu to this model
                gpu.setEvent(event); // set the event field in gpu to this event
                Data data = model.getData(); // get data from model
                gpu.divideData(data); // Divide data
                gpu.makeMeBusy();
                busy=true;
                trained=false;
                gpu.sendDataBatchToCluster(); // sent dataBatch to cluster
            }
            else
                awaitingmessages.add(event);
        });
        subscribeBroadcast(TickBroadcast.class,(TickBroadcast tickBroadcast) -> {
            if (busy == true) {
                gpu.trainDataBach(); // Train DataBatch
                gpu.sendDataBatchToCluster(); // Send dataBatch to cluster
                if (trained == false && gpu.isAllDataTrained()) { //if all data is trained -
                    gpu.getModel().setStatusToTrained(); // change model status to trained
                    //Statistics
                    gpu.addTrainedModelName(gpu.getModel().getName());
                    complete(gpu.getEvent(), gpu.getModel());

                    gpu.cleanAllTrainedData();
                    gpu.makeMeUnbusy();
                    busy = false;
                    trained = true;
                }
            }
            else {
                if (!awaitingmessages.isEmpty()) {
                    Message msg = awaitingmessages.remove();
                    functionMap.get(msg.getClass()).call(msg);
                }
            }
        });
        subscribeEvent(TestModelEvent.class,(TestModelEvent event)-> {
            if (busy == false) {
                gpu.makeMeBusy();
                Model model = event.getModel();
                gpu.setModel(model);
                gpu.setEvent(event);
                if (model.getStudent().isPhd()) { //Check if Student is Phd
                    if (Math.random() <= 0.8)
                        model.setResultToGood();
                    else
                        model.setResultToBad();
                } else {
                    if (Math.random() <= 0.6)
                        model.setResultToGood();
                    else
                        model.setResultToBad();
                }
                model.setStatusToTested();
                gpu.makeMeUnbusy();
                complete(event, model);
                }
             else {
                awaitingmessages.add(event);
            }
        });

        subscribeBroadcast(TerminateBroadcast.class,(TerminateBroadcast b)->{
            terminate();
        });
        latch.countDown();
    }
}
