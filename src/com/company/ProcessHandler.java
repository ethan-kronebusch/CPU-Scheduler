package com.company;

import java.util.concurrent.Callable;

public abstract class ProcessHandler implements Callable<Object>, TimeSynced {
    protected int time;

    // The process this CPU is acting on
    protected Process currentProcess;
    // This is to allow more accurate output display
    protected Process prevProcess;

    protected Scheduler scheduler;

    // getters/setters
    public Process GetCurrentProcess()
    {
        return currentProcess;
    }
    public synchronized void SetCurrentProcess(Process process)
    {
        currentProcess = process;
    }

    // moves one step forward in the simulation
    protected void takeSimulationStep()
    {
        time++;

        // If no process is loaded then idle
        if (currentProcess == null)
        {
            prevProcess = null;
        }
        else {
            // Increment the processes completion and then check to see if the burst is finished
            currentProcess.SetBurstProgress(currentProcess.GetBurstProgress() + 1);
            if (currentProcess.BurstFinished())
            {
                prevProcess = currentProcess;
                FinishBurst();
            }
        }
        // Releases one of the locks on the main method continuing
        Main.SimulationSync.release();
    }

    protected  abstract  void FinishBurst();

    @Override
    public synchronized Object call() throws Exception {
        while (true)
        {
            // Wait until the main method calls TakeStep
            wait();

            // If the local time is behind the system time then run simulation
            if (Main.GetSystemTime() > time)
                takeSimulationStep();

        }
    }

    @Override
    public int GetTime() {
        return time;
    }

    @Override
    public synchronized void TakeStep() {
        notify();
    }
}
