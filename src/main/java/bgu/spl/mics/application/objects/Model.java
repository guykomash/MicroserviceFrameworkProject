package bgu.spl.mics.application.objects;

/**
 * Passive object representing a Deep Learning model.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Model {

    public enum Status {PreTrained, Training, Trained,Tested}
    enum Results {None,Good, Bad}

    private String name;
    private Data data;
    private Student student;
    private Status status;
    private Results result;

    public Model(String name,Data data,Student student){
        this.name=name;
        this.data=data;
        this.student=student;
        status=Status.PreTrained;
        result=Results.None;
    }

    public Data getData(){return data;}

    public String getName(){return name;}

    public Status getStatus(){return status;}

    public Results getResult(){return result;}

    public void setStatusToTrained() {
        this.status = Status.Trained;
    }

    public void setStatusToTested() {
        this.status = Status.Tested;
    }

    public void setStatusToTraining() {
        this.status = Status.Training;
    }

    public Student getStudent(){
        return this.student;
    }

    public void setResultToGood(){
        result=Results.Good;
    }

    public void setResultToBad(){
        result=Results.Bad;
    }

    public boolean isResultGood(){
        return result==Results.Good;
    }
    public String toStringResult(){
        if (result==Results.Bad)
            return "Bad";
        else if(result==Results.Good)
            return "Good";
        else return "None";
    }

    public String getStatusString(){
        if(this.status==Status.PreTrained)
            return "PreTrained";
        if(this.status==Status.Trained)
            return "Trained";
        if(status==Status.Training)
            return "Training";
        return "Tested";
    }
}