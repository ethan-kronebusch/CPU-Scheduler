package com.company;

public class IODevice extends ProcessHandler {

    // constructor
    public IODevice(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    @Override
    protected void FinishBurst() {
        currentProcess.IncrementBurstsCompleted();
        
        scheduler.AddProcessToReadyQueue(currentProcess);
        scheduler.waitQueue.remove(currentProcess);
        currentProcess.SetBurstProgress(0);
        Logger.GetLogger().Log("process \"" + currentProcess.GetName() + "\" I/O completed, moving to Ready Queue at " + time);
        currentProcess = null;
    }
}
