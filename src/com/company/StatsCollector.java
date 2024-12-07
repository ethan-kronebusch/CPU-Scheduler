/*
 * @author: Ethan Kronebusch
 * Date: 2022-02-20
 * Project: CPU_Scheduler
 * Program: StatsCollector.java
 * Description: Collects statistics about currently running processes and system state.
 */
package com.company;

/**
 * @author krone
 *
 */
public class StatsCollector {

	Scheduler scheduler;
	int waitTime;
	double avgUsage, throughput, avgWait;
	
	public StatsCollector(Scheduler scheduler) {
		this.scheduler = scheduler;
		updateStats();
	}
	
	public void updateStats() {
		avgUsage = 0;
		
		for(CPU cpu : scheduler.CPUs) {
			avgUsage += cpu.getUsage();
		}
		avgUsage = (avgUsage*100)/(scheduler.CPUs.size()*scheduler.GetTime());
		
		throughput = scheduler.terminatedQueue.size() > 0 ? (double)scheduler.GetTime()/scheduler.terminatedQueue.size() : Double.NaN;
		
		waitTime = 0;
		for(Process proc : scheduler.allProcesses) {
			waitTime += proc.getReadyTime();
		}
		
		avgWait = (double)waitTime/scheduler.allProcesses.size();
	}
	
	public String getRunningStats() {
		return String.format("Average CPU usage: %.2f%%%nAverage throughput: %.2f ticks/process%nAverage wait time: %.2f ticks/process%nTotal wait time: %d ticks", avgUsage, throughput, avgWait, waitTime);
	}
	
	public String getEndStats() {
		double avgResponse = 0;
		for(Process proc : scheduler.allProcesses) {
			avgResponse += proc.getResponseTime();
		}
		avgResponse /= scheduler.allProcesses.size();
		
		return String.format("Final statistics:%nTotal Processes: %d%nAlgorithm used: %s%nQuantum length: %d ticks%nCPUs used: %d%nI/Os used: %d%nTotal time taken: %d ticks%nTotal wait time: %d ticks"+
								"%nAverage CPU usage: %.2f%%%nAverage throughput: %.2f ticks/process%nAverage wait time: %.2f ticks/process%nAverage response time: %.2f ticks",
								scheduler.allProcesses.size(), scheduler.GetAlgorithmToUse(), scheduler.getQuantum(), scheduler.CPUs.size(), scheduler.IODevices.size(), scheduler.GetTime(), 
								waitTime, avgUsage, throughput, avgWait, avgResponse);
	}
}
