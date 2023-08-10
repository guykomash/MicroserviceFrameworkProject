package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.objects.Model;

public class TestModelEvent implements Event<Model> {
    private Model m;
    public TestModelEvent(Model m){
        this.m=m;
    }
    public Model getModel(){
        return m;
    }
}
