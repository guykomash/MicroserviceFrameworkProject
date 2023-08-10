package bgu.spl.mics.application.objects;

import java.util.Vector;

/**
 * Passive object representing information on a conference.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class ConfrenceInformation {

    private String name;
    private int date;
    public Vector<Model> models;
    private int time;


    public ConfrenceInformation(String name,int date){
        models=new Vector<Model>();
        this.date=date;
        this.name=name;
        time=0;
    }
    public void addModel(Model model){
        models.add(model);
    }
    public void advanceTime(){
        time++;
    }
    public boolean isFinished(){
        return time==date;
    }
    public Vector<Model> getModels(){
        return models;
    }
    public String getName(){
        return  this.name;
    }
    public int getDate(){return this.date;}

    public void initModels(){
        this.models=new Vector<Model>();
    }
    public Model[] getSuccessfulModels(){
        Model[] SuccessModels= new Model[models.size()];
        int index=0;
        for(Model model:models){
            SuccessModels[index]=model;
            index++;
        }
        return SuccessModels;
    }


}
