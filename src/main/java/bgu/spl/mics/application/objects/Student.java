package bgu.spl.mics.application.objects;

import bgu.spl.mics.Future;
//import sun.text.normalizer.NormalizerBase;

import java.util.Vector;

/**
 * Passive object representing single student.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */

public class Student {
    /**
     * Enum representing the Degree the student is studying for.
     */
    public enum Degree {
        MSc, PhD
    }
    private String name;
    private String department;
    private Degree status;
    public ModelJson[] models;
    private volatile int publications;
    private volatile int papersRead;
    public Vector<Model> models_vector;
    private Future future;
    private Model currentModel;

    public Student(String name,String department,String status){
        this.name=name;
        this.department=department;
        if(status.equals("MSc"))
            this.status=Degree.MSc;
        else this.status=Degree.PhD;
        publications=0;
        papersRead=0;
        this.future=null;
        this.currentModel=null;
    }

    public void setCurrentModel(Model model){
        this.currentModel=model;
    }
    public void setCurrentFuture(Future future){
        this.future=future;
    }

    public Model getCurrentModel(){
        return currentModel;
    }

    public<T> Future getCurrentFuture(){
        return future;
    }

    public boolean isPhd(){
        if(status==Degree.PhD)
            return true;
        return false;
    }
    public void increasePapersRead(){
        papersRead++;
    }
    public void increasePublications(){publications++;}
    public Vector<Model> getModels(){
        return models_vector;
    }

    public void resetFuture(){
        this.future=null;
    }
    public String getName(){
        return this.name;
    }
    public int getPapersRead(){
        return papersRead;
    }
    public int getPublications(){
        return publications;
    }
    public void setModels(Vector<Model> models){
        this.models_vector=models;
    }
    public void setVector(){
        models_vector=new Vector<Model>();
    }
    public void setModelsFromJson(ModelJson[] models_json){

        for(int i=0;i<models_json.length;i++){
            models_vector.add(new Model(models_json[i].getName(),new Data(models_json[i].getType(),models_json[i].getSize()),this));
        }
    }
    public String getDepartment(){
        return this.department;
    }
    public String getStatusString(){
        if(this.status==Degree.MSc)
            return "MSc";
        else return "PhD";
    }
    public String toStringStudent(){
        return "name: "+this.getName()+"\n"+"department: "+this.getDepartment()+"\n"+"status: "+this.getStatusString()+"\n"
                +"publications: "+this.getPublications()+"\n"+"papersRead: "+this.getPapersRead()+"\n"
                +this.toStringTrainedModels()+"\n";
    }
    public String toStringTrainedModels(){
        String str="trainedModels: \n";

        for(Model model:this.models_vector){
            String currentModel="";
            if(model.getStatus()== Model.Status.Trained || model.getStatus()== Model.Status.Tested ){
                currentModel="name: "+model.getName()+"\n"+"data:{ \n" +model.getData().toStringData()+"\n } \n"+"status: "+model.getStatusString()+ "\n"
                        +"results: "+model.toStringResult();
            }
            str=str+"\n"+currentModel+"\n";
        }
        return str;
    }
    public Vector<Model> getTrainedModels(){
        Vector<Model> TrainedModel= new Vector<Model>();
        for(Model model:models_vector){
            if(model.getStatus()== Model.Status.Trained || model.getStatus()== Model.Status.Tested)
                TrainedModel.add(model);
        }
        return TrainedModel;
    }
}