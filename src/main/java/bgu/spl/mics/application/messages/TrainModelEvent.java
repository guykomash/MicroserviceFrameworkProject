package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;

public class TrainModelEvent implements Event<Model> {
    private Model m;
    public TrainModelEvent(Model m){
        this.m=m;
    }
    public Model getModel(){
        return m;
    }
}
