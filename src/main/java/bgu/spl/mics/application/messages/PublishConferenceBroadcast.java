package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.application.objects.Model;

//import java.util.LinkedList;
import java.util.Vector;
//import java.util.concurrent.LinkedBlockingDeque;

public class PublishConferenceBroadcast implements Broadcast {
    private Vector<Model> vectorOfModel;
    public PublishConferenceBroadcast( Vector<Model> vectorOfModel){
       this.vectorOfModel=vectorOfModel;
    }
    public Vector<Model> getListOfModels(){
        return vectorOfModel;
    }
}
