package com.company;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class Scheduler implements Callable<Object>, TimeSynced {

    private int schedulerTime = 0;
    private int timeQuantum;
    private SchedulerAlgorithm algorithmToUse;

    // Processes that haven't "arrived" yet
    public ArrayList<Process> allProcesses = new ArrayList<>();
    public ArrayList<Process> newQueue = new ArrayList<>();
    public List<Process> readyQueue = Collections.synchronizedList(new ArrayList<>());
    public List<Process> waitQueue = Collections.synchronizedList(new ArrayList<>());
    public List<Process> terminatedQueue = Collections.synchronizedList(new ArrayList<>());

    public ArrayList<CPU> CPUs = new ArrayList<>();
    public ArrayList<IODevice> IODevices = new ArrayList<>();

    // Constructor
    public Scheduler(ArrayList<Process> newQueue, SchedulerAlgorithm algorithmToUse, int timeQuantum)
    {
        this.newQueue = newQueue;
        this.algorithmToUse = algorithmToUse;
        this.timeQuantum = timeQuantum;
        allProcesses.addAll(newQueue);
    }

    // getters/setters
    public int GetSchedulerTime()
    {
        return schedulerTime;
    }
    public SchedulerAlgorithm GetAlgorithmToUse()
    {
        return algorithmToUse;
    }
    public int getQuantum() {
    	return timeQuantum;
    }

    // moves one step forward in the simulation
    private void takeSimulationStep()
    {
        schedulerTime++;
        checkProcessArrival();
        fillEmptyCPUs();
        fillEmptyIODevices();
        addReadyStep();
        addWaitStep();
        // Let the main method continue
        Main.SimulationSync.release();
    }
    // Moves processes from the NewQueue to the ReadyQueue when the arrivalTime comes
    private void checkProcessArrival()
    {
        // go over all the processes from last to first so we can remove them
        for (int i = newQueue.size() - 1; i >= 0; i--) {

            Process process = newQueue.get(i);

            // If it is time for the process to arrive remove it from the newProcesses List and add it to the readyQueue
            if (process.GetArrivalTime() <= Main.GetSystemTime()){
                newQueue.remove(i);
                Logger.GetLogger().Log("New process \"" + process.GetName() + "\" created at " + schedulerTime);
                AddProcessToReadyQueue(process);
            }
        }
    }

    private void HandlePrioritySchedulingInterrupts()
    {
        while (true)
        {
            // Get highest priority process
            Process highPriorityProcess = SchedulerAlgorithms.GetNextProcess(readyQueue, algorithmToUse);

            if (highPriorityProcess == null)
                return;

            // Get cpu with lowest priority process
            CPU lowestPriorityCPU = CPUs.get(0);
            for (int i = 1; i < CPUs.size(); i++) {
                if (CPUs.get(i).currentProcess.getPriority() > lowestPriorityCPU.currentProcess.getPriority())
                {
                    lowestPriorityCPU = CPUs.get(i);
                }
            }

            // If the highest priority process in the ready queue is higher priority then the process in the cpu then replace it
            if (highPriorityProcess.getPriority() < lowestPriorityCPU.currentProcess.getPriority())
            {
                AddProcessToReadyQueue(lowestPriorityCPU.currentProcess);
                Logger.GetLogger().Log("process \"" +lowestPriorityCPU.currentProcess.GetName()+ "\" preempted and put back in READY Queue at "+ schedulerTime);
                addProcessToCPU(highPriorityProcess, lowestPriorityCPU);
            }
            else // Else break out of the loop
            {
                return;
            }
        }
    }

    // Moves processes from the ReadyQueue to a CPU
    private void fillEmptyCPUs()
    {
        // Go through every CPU and assign it a process if it doesn't have one
        for (CPU cpu: CPUs) {
            if (readyQueue.size() > 0 && cpu.GetCurrentProcess() == null)
            {
                // Pick the correct process to use based upon our scheduling algorithm
                Process process = SchedulerAlgorithms.GetNextProcess(readyQueue, algorithmToUse);
                addProcessToCPU(process, cpu);
                if (algorithmToUse == SchedulerAlgorithm.RR)
                {
                    ResetTimeQuantum(cpu);
                }
            }
        }

        if (algorithmToUse == SchedulerAlgorithm.PS)
        {
            HandlePrioritySchedulingInterrupts();
        }
    }

    public void AddProcessToReadyQueue(Process process)
    {
        process.setLastState(process.getState());
    	process.SetState(ProcessState.READY);
        readyQueue.add(process);

//        if (algorithmToUse == SchedulerAlgorithm.PS)
//        {
//            HandlePrioritySchedulingInterrupts(process);
//        }
    }

    private void addProcessToCPU(Process process, CPU cpu)
    {
        // Load the process into the cpu
        cpu.SetCurrentProcess(process);
        // Set the response time of the process
        if(process.getResponseTime() == -1) {process.setResponseTime(schedulerTime-process.GetArrivalTime());}
        
        // Record the process' current state
        process.setLastState(process.getState());
        // Set the process state to running
        process.SetState(ProcessState.RUNNING);
        // Log that the process was dispatched
        Logger.GetLogger().Log("process \"" + process.GetName() + "\" dispatched to CPU at " + schedulerTime);
        // Remove the process from the ready queue
        readyQueue.remove(process);
    }

    // Moves processes from the WaitQueue to a IODevice
    private void fillEmptyIODevices()
    {
        // Go through every I/O device and assign it a process if it doesn't have one
        for (IODevice ioDevice: IODevices) {
            if (waitQueue.size() > 0 && ioDevice.GetCurrentProcess() == null)
            {
                // This is FCFS(first come first serve) this is the only scheduling algorithm we need for IODevices
                ioDevice.SetCurrentProcess(waitQueue.get(0));
                waitQueue.get(0).setLastState(ProcessState.WAITING);
                waitQueue.get(0).SetState(ProcessState.RUNNING_IO);
                waitQueue.remove(0);
            }
        }
    }
    
    //check if the simulation is finished, that there are no more processes left in the scheduler
    public boolean isFinished() {
    	if(newQueue.isEmpty() && readyQueue.isEmpty() && waitQueue.isEmpty()) {
    		for(IODevice io : IODevices) {
    			if(io.GetCurrentProcess() != null) {
    				return false;
    			}
    		}
    		for(CPU cpu : CPUs) {
    			if(cpu.GetCurrentProcess() != null) {
    				return false;
    			}
    		}
    		return true;
    	}
    	return false;
    }
    
    //adds 1 to the wait time of all processes in the ready queue
    public synchronized void addReadyStep() {
    	for(Process readyP : readyQueue) {
    		if(readyP.getLastState() == ProcessState.READY) {
    			readyP.incrementReadyTime();
    		}else {
    			readyP.setLastState(ProcessState.READY);
    		}
    	}
    }
    
    //adds 1 to the wait time of all processes in the I/O waiting queue
    public synchronized void addWaitStep() {
    	for(Process waitingP : waitQueue) {
    		if(waitingP.getLastState() == ProcessState.WAITING) {
    			waitingP.incrementWaitTime();
    		}else {
    			waitingP.setLastState(ProcessState.WAITING);
    		}
    	}
    }

    public void ResetTimeQuantum(CPU cpu)
    {
        cpu.timeLeft = timeQuantum;
    }

    @Override
    public synchronized Object call() throws Exception {
        while (true)
        {
            // Wait until the main method calls TakeStep
            wait();

            // If the local time is behind the system time then run simulation
            if (Main.GetSystemTime() > schedulerTime)
                takeSimulationStep();
        }
    }

    @Override
    public int GetTime() {
        return schedulerTime;
    }

    public synchronized void TakeStep()
    {
        notifyAll();
    }
}
