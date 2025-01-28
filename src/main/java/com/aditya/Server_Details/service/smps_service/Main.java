package com.aditya.Server_Details.service.smps_service;

import oshi.SystemInfo;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

public class Main {
    public static void main(String[] args) {
        SystemInfo systemInfo = new SystemInfo();
        OperatingSystem os = systemInfo.getOperatingSystem();
        long totalMemory = systemInfo.getHardware().getMemory().getTotal();

        for (OSProcess process : os.getProcesses()) {
            String processName = process.getName();
            double cpuUsage = process.getProcessCpuLoadCumulative() * 100; // %
            long memoryUsage = process.getResidentSetSize(); // bytes
            Integer processId = process.getProcessID();

                    System.out.println("Process name :" + " "+ processName +  " processid: " + processId + " CPU: " + cpuUsage + " Memory:"  + memoryUsage/1024/1024);
        }
    }
}

