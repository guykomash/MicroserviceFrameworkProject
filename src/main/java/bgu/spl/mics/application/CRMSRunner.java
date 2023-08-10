package bgu.spl.mics.application;
import bgu.spl.mics.MessageBusImpl;
import bgu.spl.mics.application.services.StudentService;
import bgu.spl.mics.application.services.GPUService;
import bgu.spl.mics.application.services.CPUService;
import bgu.spl.mics.application.services.ConferenceService;
import bgu.spl.mics.application.objects.*;
import bgu.spl.mics.application.services.*;
import com.google.gson.Gson;
import java.io.*;
import java.util.concurrent.CountDownLatch;


/**
 * This is the Main class of Compute Resources Management System application. You should parse the input file,
 * create the different instances of the objects, and run the system.
 * In the end, you should output a text file.
 */

public class CRMSRunner {
    public static void main(String[] args) throws InterruptedException, IOException {

        // Working on Ubuntu..

        System.out.println("Starting...");
        Gson gson = new Gson(); //Google Gson is a simple Java-based library to serialize Java objects to JSON and vice versa. It is an open-source library developed by Google.
        Reader reader = new FileReader("example_input.json"); // args[0]
        Input input = gson.fromJson(reader, Input.class);
        reader.close();


        MessageBusImpl bus = MessageBusImpl.getInstance();
        Cluster cluster = Cluster.getInstance();

        Student[] students = input.getStudents();
        GPU[] gpus = input.getGPUS();
        CPU[] cpus = input.getCPUS();
        ConfrenceInformation[] Conferences = input.getConferences();
        CountDownLatch latch = new CountDownLatch(gpus.length + cpus.length + Conferences.length);
        Thread[] StudentThreads = new Thread[students.length];
        Thread[] ServicesThreads = new Thread[gpus.length + cpus.length + Conferences.length];
        int thread_index = 0;

        //Get Gpus and Create GPUServices
        GPUService[] gpuServices = new GPUService[gpus.length];

        for (int i = 0; i < gpus.length; i++) {
            gpuServices[i] = new GPUService(gpus[i].getTypeString() + "GPUService" + i, gpus[i]);
            gpuServices[i].setLatch(latch);
            ServicesThreads[thread_index] = new Thread(gpuServices[i]);
            thread_index++;

        }
        System.out.println("Got " + gpus.length + " Gpus...");

        //Get Cpus and Create CPUServices

        CPUService[] cpuServices = new CPUService[cpus.length];
        for (int i = 0; i < cpus.length; i++) {
            cpuServices[i] = new CPUService("CPUService" + i, cpus[i]);
            cpuServices[i].setLatch(latch);
            ServicesThreads[thread_index] = new Thread(cpuServices[i]);
            thread_index++;
        }

        System.out.println("Got " + cpus.length + " Cpus...");

        ConferenceService[] ConferenceService = new ConferenceService[Conferences.length];
        for (int i = 0; i < Conferences.length; i++) {
            Conferences[i].initModels();
            ConferenceService[i] = new ConferenceService(Conferences[i].getName(), Conferences[i]);
            ConferenceService[i].setLatch(latch);
            ServicesThreads[thread_index] = new Thread(ConferenceService[i]);
            thread_index++;
        }

        System.out.println("Got " + Conferences.length + " Conferences (?)...");

        CountDownLatch latch2 = new CountDownLatch(students.length);

        //set Students Models and Create StudentServices
        StudentService[] studentServices = new StudentService[students.length];
        for (int i = 0; i < students.length; i++) {
            students[i].setVector();
            students[i].setModelsFromJson(students[i].models);
            studentServices[i] = new StudentService(students[i].getName() + "StudentService", students[i]);
            studentServices[i].setLatch(latch2);
            StudentThreads[i] = new Thread(studentServices[i]);
        }


        System.out.println("Got " + students.length + " Students...");


        for (int i = 0; i < ServicesThreads.length; i++) {
            ServicesThreads[i].start();
        }
        System.out.println("Started " + ServicesThreads.length + " Service Threads...");

        latch.await();

        for (int i = 0; i < StudentThreads.length; i++) {
            StudentThreads[i].start();
        }

        System.out.println("Started " + StudentThreads.length + " Student Threads...");

        latch2.await();

        //get TickTime and Duration
        int TickTime = input.getTickTime();
        int Duration = input.getDuration();

        TimeService TimeService = new TimeService(TickTime, Duration);
        Thread TimeThread = new Thread(TimeService);
        TimeThread.start();

        for (int i = 0; i < ServicesThreads.length; i++) {
            ServicesThreads[i].join();
        }
        for (int i = 0; i < StudentThreads.length; i++) {
            ServicesThreads[i].join();
        }
        TimeThread.join();


        //
        generateOutput(input);

    }


    public static void generateOutput(Input input ){
        //Output file

        try {
            System.out.println("Generating Output file... ");

            BufferedWriter writer = new BufferedWriter(new FileWriter("output.json"));
            writer.write("{ \n");
            //print students
            writer.write("\t\"students\": [");
            int i = 0;
            for (Student s : input.getStudents()) {
                i++;
                writer.write(" \n \t\t{");
                writer.write("\n \t\t\t \"name\": \"" + s.getName() + "\",");
                writer.write("\n \t\t\t \"department\": \"" + s.getDepartment() + "\",");
                writer.write("\n \t\t\t \"status\": \"" + s.getStatusString() + "\",");
                writer.write("\n \t\t\t \"publication:\": \"" + s.getPublications() + "\",");
                writer.write("\n \t\t\t \"papersRead:\": \"" + s.getPapersRead() + "\",");
                writer.write("\n \t\t\t \"trainedModels:\": [");
                int j = 0;
                if (s.getTrainedModels().size() == 0)
                    writer.write("]");
                else// case models empty
                {
                    for (Model m : s.getTrainedModels()) {
                        j++;
                        writer.write("\n\t\t\t\t{");
                        writer.write("\n\t\t\t\t\t\"name\": \"" + m.getName() + "\",");
                        writer.write("\n\t\t\t\t\t\"data\": {");
                        writer.write("\n\t\t\t\t\t\t\"type\": \"" + m.getData().getType() + "\",");
                        writer.write("\n\t\t\t\t\t\t\"size\": " + m.getData().getDataSize());
                        writer.write("\n\t\t\t\t\t},");
                        writer.write("\n\t\t\t\t\t\"status\": \"" + m.getStatus() + "\",");
                        writer.write("\n\t\t\t\t\t\"result\": \"" + m.getResult() + "\"");
                        writer.write("\n\t\t\t\t}");
                        if (j < s.getModels().size())
                            writer.write(",");

                    }//end of models
                    writer.write("\n \t\t\t]");
                }
                writer.write("\n \t\t}");
                if (i != input.getStudents().length)
                    writer.write(",");
            }//end print students
            writer.write("\n\t],");
            //print confrences
            writer.write("\n\t\"conferences\": [");
            i = 0;
            for (ConfrenceInformation c : input.getConferences()) {
                i++;
                writer.write("\n\t\t{");
                writer.write("\n\t\t\t\"name\":" + "\"" + c.getName() + "\",");
                writer.write("\n\t\t\t\"date\":" + "\"" + c.getDate() + "\",");
                writer.write("\n\t\t\t\"publications\": [");
                int j = 0;
                if (c.getSuccessfulModels().length == 0)//case publications empty
                    writer.write("]");
                else {
                    for (Model m : c.getSuccessfulModels())//publications
                    {
                        j++;
                        writer.write("\n\t\t\t\t{");
                        writer.write("\n\t\t\t\"name\": \"" + m.getName() + "\",");
                        writer.write("\n\t\t\t\"data\": {");
                        writer.write("\n\t\t\t\t\"type\": \"" + m.getData().getType() + "\",");
                        writer.write("\n\t\t\t\t\"size\": " + m.getData().getDataSize());
                        writer.write("\n\t\t\t\t},");
                        writer.write("\n\t\t\t\t\t\"status\": \"" + m.getStatus() + "\",");
                        writer.write("\n\t\t\t\t\t\"result\": \"" + m.getResult() + "\"");
                        writer.write("\n\t\t\t\t}");
                        if (j < c.getSuccessfulModels().length)
                            writer.write(",");
                    }//end of publicatoins
                    writer.write("\n\t\t\t]");
                }
                writer.write("\n\t\t}");
                if (i != input.getConferences().length)
                    writer.write(",");
            }
            writer.write("\n\t],");
            //CpuTimeUsed
            writer.write("\n\"cpuTimeUsed\": " + Cluster.getInstance().numOfCPUTimeUnitUsed.get() + ",");
            //GpuTimeUsed
            writer.write("\n\"gpuTimeUsed\": " + Cluster.getInstance().numOfGPUTimeUnitUsed.get() + ",");
            //BatchesProccessed
            writer.write("\n\"batchesProcessed\": " + Cluster.getInstance().totalProcessedDataBatch.get());
            writer.write("\n}");
            writer.close();
            System.out.println("Generating Output file...Finished ");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
