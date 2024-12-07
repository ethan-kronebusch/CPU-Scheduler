package com.company;

public class CPU extends ProcessHandler {

    // This is how much time this process has left in it's quantum(time slice)
    protected int timeLeft;
    // This is how many ticks the CPU has been running (non-idle) for.
    private int usage = 0;

    // constructor
    public CPU(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    @Override
    protected synchronized void FinishBurst() {
        currentProcess.IncrementBurstsCompleted();
        currentProcess.SetBurstProgress(0);

        // If the process is finished terminate it, otherwise move it to IO queue
        if (currentProcess.processFinished())
        {
        	currentProcess.setLastState(ProcessState.RUNNING);
        	currentProcess.SetState(ProcessState.TERMINATED);
            //scheduler.readyQueue.remove(currentProcess);
            scheduler.terminatedQueue.add(currentProcess);
            currentProcess.setFinishTime(time);
            logEndOfProcess();
            currentProcess = null;
        }
        else {
        	currentProcess.setLastState(ProcessState.RUNNING);
            currentProcess.SetState(ProcessState.WAITING);
            //scheduler.readyQueue.remove(currentProcess);
            scheduler.waitQueue.add(currentProcess);
            Logger.GetLogger().Log("process \"" +currentProcess.GetName()+ "\" moved to IO Queue at "+ time);
            currentProcess = null;
        }
    }

    // moves one step forward in the simulation
    @Override
    protected synchronized void takeSimulationStep()
    {
        // Code from super class, copied because I need to remove the releasing of the semaphore at the end
        time++;

        // If no process is loaded then idle
        if (currentProcess == null)
        {
            prevProcess = null;
        }
        else {
            // Increment the processes completion and then check to see if the burst is finished
            currentProcess.SetBurstProgress(currentProcess.GetBurstProgress() + 1);
            usage++;
            
            if (currentProcess.BurstFinished())
            {
                // If we finish running then we most stop the execution of this function and release the SimulationSync lock
                // If we let it continue it would through an
                prevProcess = currentProcess;
                FinishBurst();
                Main.SimulationSync.release();
                return;
            }
        }
        // End of code from super class

        if (currentProcess != null && scheduler.GetAlgorithmToUse() == SchedulerAlgorithm.RR)
            CheckForEndOfQuantum();



        // Releases one of the locks on the main method continuing
        Main.SimulationSync.release();
    }

    // This checks to see if the cpu's current quantum is finished, if so to removes the currently running process
    private void CheckForEndOfQuantum()
    {
        timeLeft--;
        if (timeLeft <= 0)
        {
            // If the ready queue is empty then reset quantum
            if (scheduler.readyQueue.size() == 0){
                scheduler.ResetTimeQuantum(this);
            }
            else {
                scheduler.AddProcessToReadyQueue(currentProcess);
                Logger.GetLogger().Log("process \"" +currentProcess.GetName()+ "\" ran out of time slice, it was preempted and put back in the READY queue at "+ time);
                currentProcess = null;
            }
        }
    }
    
    // I want usage to be read-only.
    public int getUsage() {
    	return usage;
    }

    private void logEndOfProcess()
    {
        String logString = "process terminated : {";
        logString += "Name : "+ currentProcess.GetName();
        logString += ", turnaround time : " + currentProcess.getTurnAroundTime();
        logString += ", response time : " + currentProcess.getResponseTime();
        logString += ", CPU wait time : " + currentProcess.getReadyTime();
        logString += ", I/O wait time : " + currentProcess.getWaitTime();
        logString += "} ";
        logString += " at " + time;
        Logger.GetLogger().Log(logString);
    }
}