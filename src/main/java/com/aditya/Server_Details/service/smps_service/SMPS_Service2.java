package com.aditya.Server_Details.service.smps_service;

import com.aditya.Server_Details.server_details.entities.ServerListDetail;
import com.aditya.Server_Details.server_details.repo.ServerListDetailRepo;
import com.aditya.Server_Details.server_master.repo.ServerMasterRepo;
import com.aditya.Server_Details.server_monitoring.entities.ServerMonitor;
import com.aditya.Server_Details.server_monitoring.repo.ServerMonitorRepo;
import com.aditya.Server_Details.service.email_service.EmailService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;
import oshi.util.GlobalConfig;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class SMPS_Service2 {
    Logger logger = LoggerFactory.getLogger(SMPS_Service2.class);





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

    @Value("${serverName}")
    String serverName;
    static Integer countOfServerDetailsList=0;





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
    
    SystemInfo systemInfo = new SystemInfo();
    CentralProcessor cp = systemInfo.getHardware().getProcessor();
    int logicalProcessorCount = cp.getLogicalProcessorCount();





    public void calculateDiskStorage(){
        SystemInfo systemInfo = new SystemInfo();
        FileSystem fileSystem = systemInfo.getOperatingSystem().getFileSystem();

        for (OSFileStore fileStore : fileSystem.getFileStores()) {
            String driveName = fileStore.getMount();
            Double totalSpace = fileStore.getTotalSpace()/(1024D*1024D*1024D);
            Double freeSpace = fileStore.getUsableSpace()/(1024D*1024D*1024D);

            if (driveName.startsWith("C") || driveName.startsWith("D") || driveName.startsWith("E")) {
                ServerListDetail server = new ServerListDetail();
                server.setProcessName(driveName);
                server.setSpecificDriveStorage(totalSpace);
                server.setSpecificDriveFreeStorage(freeSpace);
                sldRepo.save(server);
            }
        }
    }






    public void sendEmail(ArrayList<ServerListDetail> serverListDetails){
        String mailBody;
        Double cpuMin ;
        Double cpuMax;
        Long memMin;
        Long memMax;
        Double totalCpu;
        Long totalMem;

        for ( int i = 0 ; i<countOfServerDetailsList ; i++) {
            cpuMin = findCpuUtilMin().get(i);
            cpuMax = findCpuUtilMax().get(i);
            memMin = findMemUtilMin().get(i);
            memMax = findMemUtilMax().get(i);
            mailBody = findByEmailBodyModel().get(i);
            totalCpu = serverListDetails.get(i).getCpuUtilizationTotal();
            totalMem = serverListDetails.get(i).getMemoryUtilizationTotal();
            try{
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
            }
            catch (NullPointerException e){

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
        Double cpuUtil = 0.0;
        Long memUtil = 0L;
        int processId;

        ProcessBuilder pb = new ProcessBuilder("cmd.exe","/c",batchFilePath);
        pb.redirectErrorStream(true);
        Process process = pb.start();
        InputStream inputStream = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr);

        Thread.sleep(5000);

        File textFile = new File(batchFileText);
        List<String> batchFileTotalProcessesList = readBatchFile(textFile.getAbsolutePath());

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
        List<OSProcess> processList  = filterListOfOSProcess(batchFileTotalProcessesList);

        for (String targetProcess : findListOfSpecificProcessFromServerMasterDB()){

          /*  if (targetProcess != null && !targetProcess.contains("idea64")) {
                continue;
            }*/

            ServerListDetail sld = null;
            cpuUtil = 0.0; // Reset for each process
            memUtil = 0L;  // Reset for each process

            for (OSProcess totalProcessesList : processList){
                if (totalProcessesList.getCommandLine().toLowerCase().contains(targetProcess.toLowerCase()) || totalProcessesList.getName().toLowerCase().contains(targetProcess.toLowerCase())){
                    if (sld==null){
                        sld = new ServerListDetail();
                    }
//                    logger.error("Before: Target Process: {}, CPU Utilization: {}%, Memory Utilization: {} MB",, sld.getCpuUtilizationTotal(), sld.getMemoryUtilizationTotal());
//                    logger.error("Before: Target Process: {}, CPU Utilization: {}%, Memory Utilization: {} MB",sld.getProcessId(), sld.getCpuUtilizationTotal(), sld.getMemoryUtilizationTotal());

                    cpuUtil = cpuUtil + (totalProcessesList.getProcessCpuLoadBetweenTicks(totalProcessesList)/logicalProcessorCount);  //%
                    memUtil = memUtil + (totalProcessesList.getResidentSetSize()/1024/1024 );   //mb
                    processId = totalProcessesList.getProcessID();
                    sld.setCpuUtilizationTotal(cpuUtil);
                    sld.setMemoryUtilizationTotal(memUtil);
                    sld.setProcessId(processId);
                    sld.setProcessName(targetProcess);
//
//                    logger.error("After: Target Process: {}, CPU Utilization: {}%, Memory Utilization: {} MB", targetProcess, sld.getCpuUtilizationTotal(), sld.getMemoryUtilizationTotal());
//                    logger.error("After: Target Process: {}, CPU Utilization: {}%, Memory Utilization: {} MB",sld.getProcessId(), sld.getCpuUtilizationTotal(), sld.getMemoryUtilizationTotal());

                }

            }
                if (sld!=null){
                    serverListDetails.add(sld);
                    countOfServerDetailsList++;
                    sldRepo.save(sld);
                }
        }
        ServerListDetail server = new ServerListDetail();
        server.setProcessName("Server");
        server.setCpuUtilizationTotal(cpuUtilization);  //%
        server.setMemoryUtilizationTotal(usedMemoryMB); //mb
        serverListDetails.add(server);
        sldRepo.save(server);
        calculateDiskStorage();
        saveProcessMaster(serverListDetails);
        sendEmail(serverListDetails);
    }





    public ArrayList<OSProcess> filterListOfOSProcess(List<String> batchFileTotalProcessesList) {
        ArrayList<OSProcess> cleanedBatchText = new ArrayList<>();
        SystemInfo si = new SystemInfo();
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



