package com.murong.nets.util;

import com.murong.nets.vo.CpuUsageVo;
import com.murong.nets.vo.HardUsageVo;
import com.murong.nets.vo.MemoryUsageVo;
import com.murong.nets.vo.ProcessActiveVo;
import com.sun.management.OperatingSystemMXBean;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

/**
 * 获取系统信息
 *
 * @author yaochuang 2024/04/29 12:39
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RunTimeUtil {

    /**
     * 获取系统内存
     */
    public static MemoryUsageVo gainMemoryUsage() {
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalMemorySize = osBean.getTotalMemorySize();
        long freeMemorySize = osBean.getFreeMemorySize();
        long totalSwapSpaceSize = osBean.getTotalSwapSpaceSize();
        return new MemoryUsageVo(totalMemorySize, freeMemorySize, totalSwapSpaceSize);
    }

    /**
     * cpu使用率
     */
    @SneakyThrows
    public static CpuUsageVo gainCpuUsage() {
        SystemInfo systemInfo = new SystemInfo();
        HardwareAbstractionLayer hardware = systemInfo.getHardware();
        CentralProcessor processor = hardware.getProcessor();
        // 获取系统的 CPU 核心数
        int coreCount = processor.getLogicalProcessorCount();
        // 查询系统的 CPU 使用率
        long[] systemCpuLoadTicks = processor.getSystemCpuLoadTicks();
        Thread.sleep(800);
        double cpuUsage = processor.getSystemCpuLoadBetweenTicks(systemCpuLoadTicks) * 100;
        return new CpuUsageVo(coreCount, cpuUsage);
    }

    /**
     * 硬盘使用情况
     */
    @SneakyThrows
    public static List<HardUsageVo> gainHardUsage() {
        return Arrays.stream(File.listRoots()).map(t -> new HardUsageVo(t.getAbsolutePath(), t.getTotalSpace(), t.getFreeSpace(), t.getUsableSpace())).collect(Collectors.toList());
    }

    /**
     * 查询进程信息
     */
    public static List<ProcessActiveVo> gainProcessList(int number) {
        ToLongFunction<ProcessHandle> function = process -> {
            Optional<Duration> duration = process.info().totalCpuDuration();
            if (duration.isPresent()) {
                return duration.orElseThrow().toSeconds();
            }
            return 0L;
        };
        List<ProcessHandle> processes = ProcessHandle.allProcesses().sorted(Comparator.comparingLong(function)).limit(number).toList();
        return processes.stream().map(process -> {
            ProcessActiveVo activeVo = new ProcessActiveVo();
            activeVo.setPid(process.pid());
            Optional<ProcessHandle> parent = process.parent();
            parent.ifPresent(processHandle -> activeVo.setParentPid(processHandle.pid()));
            ProcessHandle.Info info = process.info();
            info.totalCpuDuration().ifPresent(activeVo::setTotalCpuDuration);
            info.totalCpuDuration().ifPresent(activeVo::setTotalCpuDuration);
            info.user().ifPresent(activeVo::setUser);
            info.command().ifPresent(activeVo::setCommand);
            info.commandLine().ifPresent(activeVo::setCommandLine);
            return activeVo;
        }).toList();
    }

}
