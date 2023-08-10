package bgu.spl.mics.application.objects;

public class Input {
    public Student [] Students;
    public String [] GPUS;
    public int [] CPUS;
    public ConfrenceInformation [] Conferences;
    public int TickTime;
    public int Duration;

//    public Input(Student[] Students,String [] GPUS,int [] CPUS,ConfrenceInformation [] Conferences,int TickTime,int Duration){
//        this.Students=Students;
//        this.GPUS=GPUS;
//        this.CPUS=CPUS;
//        this.Conferences=Conferences;
//        this.TickTime=TickTime;
//        this.Duration=Duration;
//    }
    public GPU [] getGPUS(){
        GPU[] gpus = new GPU[GPUS.length];
        for(int i=0;i<GPUS.length;i++){
            gpus[i]= new GPU(GPUS[i]);
        }
        return gpus;
    }
    public CPU [] getCPUS(){
        CPU[] cpus = new CPU[CPUS.length];
        for(int i=0;i<CPUS.length;i++){
            cpus[i]= new CPU(CPUS[i]);
        }
        return cpus;
    }
    public int getTickTime(){
       return TickTime;
    }
    public int getDuration(){
        return Duration;
    }

    public ConfrenceInformation [] getConferences(){
        return this.Conferences;
    }

    public Student [] getStudents(){
        return this.Students;
    }
}
