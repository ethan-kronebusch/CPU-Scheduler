package com.company;

import java.util.ArrayList;
import java.util.Formatter;

public class Process {

    private String name;
    private ProcessState state = ProcessState.NEW;
    private ProcessState lastState = ProcessState.NEW;
    // The time the process is created/added to the ready queue for the first time
    private int arrivalTime;
    // The time the process terminates/finishes
    private int finishTime = -1;
    // The total time the process has spent in the ready queue
    private int waitTime = 0;
    // The total time the process has spent in the wait queue
    private int ioWaitTime = 0;
    // The time it took from the process' arrival until the process' first response
    private int responseTime = -1;
    private int priority;
    // all bursts both CPU and IO
    public ArrayList<Integer> bursts = new ArrayList<Integer>();
    // Total number of bursts
    private int burstsCompleted;
    // This is how much time we have been working on the current burst
    private int burstProgress;

    // constructors
    public Process(String name, int arrivalTime, int priority, ArrayList<Integer> bursts)
    {
        this.name = name;
        this.arrivalTime = arrivalTime;
        this.priority = priority;
        this.bursts = bursts;
    }

    // getters/setters
    public int GetArrivalTime()
    {
        return arrivalTime;
    }
    public int GetBurstProgress()
    {
        return burstProgress;
    }
    public String GetName()
    {
        return name;
    }

    public void SetState(ProcessState state)
    {
        this.state = state;
    }
    
    public ProcessState getState() {
    	return state;
    }
    
    public void setFinishTime(int fTime) {
    	finishTime = fTime;
    }
    
    public int getFinishTime() {
    	return finishTime;
    }
    
    public void setResponseTime(int responseTime) {
    	this.responseTime = responseTime;
    }
    
    public int getResponseTime() {
    	return responseTime;
    }
    
    public int getReadyTime() {
    	return waitTime;
    }
    
    public int getWaitTime() {
    	return ioWaitTime;
    }

    public void SetBurstProgress(int burstProgress)
    {
        this.burstProgress = burstProgress;
    }

    public int getBurstTime()
    {
        return bursts.get(burstsCompleted);
    }
    public int getPriority()
    {
        return priority;
    }
    public int getBurstsCompleted() {
    	return burstsCompleted;
    }
    
    public ProcessState getLastState() {
    	return lastState;
    }
    
    public void setLastState(ProcessState state) {
    	lastState = state;
    }

    // other methods
    public boolean BurstFinished()
    {
        return burstProgress >= bursts.get(burstsCompleted);
    }
    public void IncrementBurstsCompleted()
    {
        burstsCompleted++;
    }
    public boolean processFinished()
    {
        return burstsCompleted >= bursts.size();
    }
    public String BurstInfo() {
    	Formatter f = new Formatter();
        String output = f.format("%02d/%02d", GetBurstProgress(), bursts.get(burstsCompleted)).toString();
        f.close();
    	return output;
    }

    public int getTurnAroundTime()
    {
        return  finishTime - arrivalTime + 1;
    }
    
    public void incrementReadyTime() {
    	waitTime++;
    }
    
    public void incrementWaitTime() {
    	ioWaitTime++;
    }

    @Override
    public String toString()
    {
    	String out = "Name: " + name + ", currentState:" + state + ", arrivalTime: " + arrivalTime + ", priority: " + priority;
    	if(state == ProcessState.TERMINATED) {
    		out += ", responseTime: " + responseTime + ", turnaround: " + getTurnAroundTime();
    	}
    	out += ", currentBurst: " +burstsCompleted+ ", bursts: "+bursts;
        return out;
    }
}
