package com.p2p.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemMonitor {

    private final MeterRegistry meterRegistry;

    @Scheduled(fixedRate = 60000) // 每分鐘執行一次
    public void monitorSystemMetrics() {
        recordMemoryMetrics();
        recordThreadMetrics();
        recordCpuMetrics();
        logSystemStatus();
    }

    private void recordMemoryMetrics() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();

        meterRegistry.gauge("system.memory.heap.used", heapUsed);
        meterRegistry.gauge("system.memory.heap.max", heapMax);
        meterRegistry.gauge("system.memory.nonheap.used", nonHeapUsed);
    }

    private void recordThreadMetrics() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        int threadCount = threadBean.getThreadCount();
        int peakThreadCount = threadBean.getPeakThreadCount();
        int daemonThreadCount = threadBean.getDaemonThreadCount();

        meterRegistry.gauge("system.threads.active", threadCount);
        meterRegistry.gauge("system.threads.peak", peakThreadCount);
        meterRegistry.gauge("system.threads.daemon", daemonThreadCount);
    }

    private void recordCpuMetrics() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double systemLoad = osBean.getSystemLoadAverage();
        int availableProcessors = osBean.getAvailableProcessors();

        meterRegistry.gauge("system.cpu.load", systemLoad);
        meterRegistry.gauge("system.cpu.processors", availableProcessors);
    }

    private void logSystemStatus() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        
        log.info("System Status - Memory: {}MB used, Threads: {} active",
            memoryBean.getHeapMemoryUsage().getUsed() / (1024 * 1024),
            threadBean.getThreadCount()
        );
    }
} 