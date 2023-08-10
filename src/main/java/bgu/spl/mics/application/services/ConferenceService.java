package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConferenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.ConfrenceInformation;
import bgu.spl.mics.application.objects.Model;

import java.util.concurrent.CountDownLatch;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConferenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {
    private ConfrenceInformation conferenceInformation;
    CountDownLatch latch;
    public ConferenceService(String name,ConfrenceInformation conferenceInformation) {
        super(name);
        this.conferenceInformation=conferenceInformation;
    }

    public void setLatch(CountDownLatch latch){
        this.latch=latch;
    }

    @Override
    protected void initialize() {
        subscribeEvent(PublishResultsEvent.class,(PublishResultsEvent event)-> {
            Model model = event.getModel();
            conferenceInformation.addModel(model);
        });
        subscribeBroadcast(TickBroadcast.class,(TickBroadcast b)->{
            conferenceInformation.advanceTime();
            if(conferenceInformation.isFinished()){
                sendBroadcast(new PublishConferenceBroadcast(conferenceInformation.getModels()));
                terminate();
            }
        });

        subscribeBroadcast(TerminateBroadcast.class,(TerminateBroadcast b)->{
            terminate();
        });
        latch.countDown();
    }
}
