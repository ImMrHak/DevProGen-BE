package com.devprogen.application.system;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class SystemServiceImpl implements SystemService{
    @Override
    public Object systemInformation() {
        try{
            SystemInfo systemInfo = new SystemInfo();
            HardwareAbstractionLayer hal = systemInfo.getHardware();
            OperatingSystem os = systemInfo.getOperatingSystem();

            CentralProcessor processor = hal.getProcessor();
            double cpuSpeed = processor.getMaxFreq() / 1_000_000_000.0; // Convert Hz to GHz

            long[] prevTicks = processor.getSystemCpuLoadTicks();
            try {
                Thread.sleep(1000); // Sleep for 1 second to get the CPU load
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            double cpuLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks) * 100;

            GlobalMemory memory = hal.getMemory();
            double ramUsage = (memory.getTotal() - memory.getAvailable()) / (double) memory.getTotal() * 100;

            List<Map<String, Object>> diskMetrics = new ArrayList<>();
            for (OSFileStore fs : os.getFileSystem().getFileStores()) {
                Map<String, Object> diskInfo = new HashMap<>();
                diskInfo.put("name", fs.getName());
                diskInfo.put("totalSpace", fs.getTotalSpace() / (1024.0 * 1024 * 1024)); // Convert to GB
                diskInfo.put("freeSpace", fs.getUsableSpace() / (1024.0 * 1024 * 1024)); // Convert to GB
                diskInfo.put("usedSpace", (fs.getTotalSpace() - fs.getUsableSpace()) / (1024.0 * 1024 * 1024)); // Convert to GB
                diskMetrics.add(diskInfo);
            }

            Map<String, Object> metrics = new HashMap<>();
            metrics.put("cpuUsage", cpuLoad);
            metrics.put("ramUsage", ramUsage);
            metrics.put("cpuSpeed", cpuSpeed);
            metrics.put("diskMetrics", diskMetrics);

            /*if (ramUsage <= 30) {
                lSI.createLog(new Log(null, "Memory usage is low: " + ramUsage + "%", LogSeverityEnum.LOW.toString(), new Date(System.currentTimeMillis())));
            } else if (ramUsage <= 60) {
                lSI.createLog(new Log(null, "Memory usage is moderate: " + ramUsage + "%", LogSeverityEnum.MID.toString(), new Date(System.currentTimeMillis())));
            } else {
                lSI.createLog(new Log(null, "Memory usage is high: " + ramUsage + "%", LogSeverityEnum.HIGH.toString(), new Date(System.currentTimeMillis())));
            }*/

            return metrics;
        }
        catch(Exception e){
            //lSI.createLog(new Log(null, "An unexpected error occurred while retrieving system metrics. Error: " + e.getMessage(), LogSeverityEnum.LOW.toString(), new Date(System.currentTimeMillis())));
            return "An unexpected error occurred";
        }
    }
}
