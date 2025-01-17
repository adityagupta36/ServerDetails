package com.aditya.Server_Details.service.smps_service;

import com.aditya.Server_Details.server_details.entities.ServerListDetail;
import com.aditya.Server_Details.server_details.repo.ServerListDetailRepo;
import com.aditya.Server_Details.server_master.repo.ServerMasterRepo;
import com.aditya.Server_Details.server_monitoring.entities.ServerMonitor;
import com.aditya.Server_Details.server_monitoring.repo.ServerMonitorRepo;
import com.aditya.Server_Details.service.email_service.EmailService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Service
@AllArgsConstructor
@NoArgsConstructor
public class SMPS_Service1 {

    @Autowired
    ServerMasterRepo smrRepo;

    @Autowired
    ServerListDetailRepo sldRepo;

    @Autowired
    ServerMonitorRepo smr;

    @Autowired
    EmailService emailService;

    @Value("${batchFilePath}")
    String batchFilePath;

    @Value("${batchFileText}")
    String batchFileText;

    @Value("${toEmail}")
    String toEmail;

//    String toEmail = "guptaaditya9728@gmail.com";




    List<String> findListOfSpecificProcessFromServerMasterDB(){
        List<String> targetProcesses = smrRepo.findListOfSpecificProcess();
        return targetProcesses;
    }
    List<Double> findCpuUtilMin(){
        List<Double> strings = smrRepo.findCpuUtilMin();
        return strings;
    }
    List<Double> findCpuUtilMax(){
        List<Double> strings = smrRepo.findCpuUtilMax();
        return strings;
    }
    List<Long> findMemUtilMin(){
        List<Long> strings = smrRepo.findMemUtilMin();
        return strings;
    }
    List<Long> findMemUtilMax(){
        List<Long> strings = smrRepo.findMemUtilMax();
        return strings;
    }
    List<String> findByEmailBodyModel(){
        return smrRepo.findByEmailBodyModel();
    }
//    String findServerName(){
//        return smrRepo.findServerName();
//    }




    public void sendEmail(ArrayList<ServerListDetail> serverListDetails){

        String mailBody;

        Double cpuMin ;
        Double cpuMax;
        Long memMin;
        Long memMax;
        Double totalCpu;
        Long totalMem;

        for ( int i = 0 ; i< serverListDetails.size() ; i++) {

            cpuMin = findCpuUtilMin().get(i);
            cpuMax = findCpuUtilMax().get(i);
            memMin = findMemUtilMin().get(i);
            memMax = findMemUtilMax().get(i);

            mailBody = findByEmailBodyModel().get(i);

            totalCpu = serverListDetails.get(i).getCpuUtilizationTotal();
            totalMem = serverListDetails.get(i).getMemoryUtilizationTotal();
            if(mailBody!=null){
                mailBody = findByEmailBodyModel().get(i)
                        .replace("<process_name>", serverListDetails.get(i).getProcessName())
                        .replace("<total_cpu_utilization>", String.valueOf(Double.parseDouble(String.valueOf(totalCpu))))
                        .replace("<cpu_util_min>", String.valueOf(Double.parseDouble(String.valueOf(cpuMin))))
                        .replace("<cpu_util_max>", String.valueOf(Double.parseDouble(String.valueOf(cpuMax))))
                        .replace("<total_mem_utilization>",String.valueOf(Long.parseLong(String.valueOf(totalMem))))
                        .replace("<mem_util_min>", String.valueOf(Long.parseLong(String.valueOf(memMin))))
                        .replace("<mem_util_max>", String.valueOf(Long.parseLong(String.valueOf(memMax))));
            }


            if (totalCpu != null && totalMem != null) {

                if ((totalCpu < cpuMin || totalCpu > cpuMax) || (totalMem < memMin || totalMem > memMax)) {
                    emailService.sendEmail(toEmail, "Error", mailBody);
                }

            }
        }
    }




    public void saveProcessMaster(List<ServerListDetail> serverListDetails){

        ServerMonitor serverMonitor = new ServerMonitor();
        serverMonitor.setLocalDateTime(LocalDateTime.now());

        for (ServerListDetail serverListDetail : serverListDetails){
            serverListDetail.setServerMonitor(serverMonitor);
        }

        serverMonitor.setServerListDetails(serverListDetails);
        smr.save(serverMonitor);

    }




    private List<String> readBatchFile(String batchFileTextPath) throws IOException {

        FileReader fr = new FileReader(batchFileTextPath);
        BufferedReader br = new BufferedReader(fr);

        List<String> listOfProcessesInBatchFileText = new ArrayList<>();
        String line;

        while ((line=br.readLine())!=null){
            listOfProcessesInBatchFileText.add(line);
        }

        return listOfProcessesInBatchFileText;
    }




    public void runBatchFile() throws IOException, InterruptedException {

        //3
        /*Double serverCpuUtil = 0.0;
        Long serverMemUtil = 0L;*/

        Double cpuUtil = 0.0;
        Long memUtil = 0L;
        int processId;

//        String batchFilePath = "C:\\Users\\adity\\OneDrive\\Desktop\\getProcesses.bat";

        ProcessBuilder pb = new ProcessBuilder("cmd.exe","/c",batchFilePath);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        InputStream inputStream = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr);


        Thread.sleep(5000);

        File textFile = new File(batchFileText);

        int retries = 5; // Retry up to 5 times to wait for the file
        while (!textFile.exists() && retries > 0) {
            Thread.sleep(2000); // Wait 2 seconds before retrying
            retries--;
        }

        if (!textFile.exists()) {
            throw new FileNotFoundException("The processes.txt file was not generated by the batch file.");
        }

        //        String batchFileText = "C:\\Users\\adity\\OneDrive\\Desktop\\processes.txt";
        List<String> batchFileTotalProcessesList = readBatchFile(textFile.getAbsolutePath());


        /*oshi.SystemInfo si = new oshi.SystemInfo();
        OperatingSystem os = si.getOperatingSystem();*/

        SystemInfo systemInfo = new SystemInfo();
        GlobalMemory memory = systemInfo.getHardware().getMemory();
        long totalMemoryMB = memory.getTotal()/(1024 * 1024);
        long availableMemoryMB = memory.getAvailable()/(1024*1024);
        Long usedMemoryMB = totalMemoryMB - availableMemoryMB;

        CentralProcessor processor = systemInfo.getHardware().getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        Thread.sleep(1000);  // 1 second delay
        long[] ticks = processor.getSystemCpuLoadTicks();
        long totalTicks = 0;
        long idleTicks = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        for (int i = 0; i < ticks.length; i++) {
            totalTicks += ticks[i] - prevTicks[i];
        }

        Double cpuUtilization = (1.0 - ((double) idleTicks / totalTicks)) * 100;

        ArrayList<ServerListDetail> serverListDetails = new ArrayList<>();

        List<OSProcess> processList  = filterListOfBatchText(batchFileTotalProcessesList);

        for (String targetProcess : findListOfSpecificProcessFromServerMasterDB()){

            ServerListDetail sld = new ServerListDetail();
            for (OSProcess totalProcessesList : processList){
                if (totalProcessesList.getCommandLine().contains(targetProcess)){
                    cpuUtil = cpuUtil + totalProcessesList.getProcessCpuLoadBetweenTicks(totalProcessesList)*100;  //%
                    memUtil = memUtil + (totalProcessesList.getResidentSetSize()/(1024L * 1024L) );   //mb
                    processId = totalProcessesList.getProcessID();

                    sld.setCpuUtilizationTotal(cpuUtil);
                    sld.setMemoryUtilizationTotal(memUtil);
                    sld.setProcessId(processId);
                    sld.setProcessName(totalProcessesList.getName());
                }
            }

            serverListDetails.add(sld);
            sldRepo.save(sld);


//            Thread.sleep(1000);
        }

        ServerListDetail server = new ServerListDetail();
        server.setProcessName("Server");
        server.setCpuUtilizationTotal(cpuUtilization);  //%
        server.setMemoryUtilizationTotal(usedMemoryMB); //mb

        serverListDetails.add(server);
        sldRepo.save(server);

        saveProcessMaster(serverListDetails);
        sendEmail(serverListDetails);
    }




    public ArrayList<OSProcess> filterListOfBatchText(List<String> batchFileTotalProcessesList) {

        ArrayList<OSProcess> cleanedBatchText = new ArrayList<>();

        oshi.SystemInfo si = new oshi.SystemInfo();
        OperatingSystem os = si.getOperatingSystem();

        for ( String cleaned : batchFileTotalProcessesList){

            cleaned=cleaned.replaceAll("[^\\x20-\\x7E]", "");
            cleaned=cleaned.trim();
            cleaned=cleaned.replaceAll("\\s", "");
            cleaned=cleaned.trim();

            String[] parts = cleaned.split("(?<=\\D)(?=\\d)");

            if (parts.length==2) {
                int i = Integer.parseInt(parts[parts.length - 1]);
                OSProcess osProcess = os.getProcess(i);
                if (osProcess == null) {
                    System.out.println("Process Object is Null: Skipping...");
                    continue;
                }
                cleanedBatchText.add(osProcess);
            }
        }
        return cleanedBatchText;
    }
}



