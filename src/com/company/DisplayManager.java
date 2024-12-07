package com.company;

import java.util.List;

public class DisplayManager {

    private Scheduler scheduler;
    private StatsCollector stats;

    public DisplayManager(Scheduler scheduler, StatsCollector stats)
    {
        this.scheduler = scheduler;
        this.stats = stats;
    }

    public void DisplaySimulationStep()
    {
        System.out.println("**************** TIME : "+scheduler.GetTime()+" ****************");
        displayQueue(scheduler.readyQueue, "Ready Queue");
        displayQueue(scheduler.waitQueue, "Wait Queue");
        displayProcesses();
        displayEvents();
        displayStats();
        displayCPUs();
        displayIODevices();
    }

    private void displayCPUs()
    {

        /*for (int i = 0; i < scheduler.CPUs.size(); i++) {
            System.out.print("╭―――――――――――╮   ");
        }*/

        System.out.print("\n");

        for (int i = 0; i < scheduler.CPUs.size(); i++) {
            String state = "idle  ";
            Process currentProcess = scheduler.CPUs.get(i).currentProcess;
            if (currentProcess != null)
            {
                state = currentProcess.BurstInfo() + " ";
            }else if(scheduler.CPUs.get(i).prevProcess != null) {
            	Process prev = scheduler.CPUs.get(i).prevProcess;
            	int lastLength = prev.bursts.get(prev.getBurstsCompleted()-1);
            	state = String.format("%02d/%02d", lastLength, lastLength);
            }

            System.out.printf("CPU %02d: %s	", i,state);
        }

        System.out.print("\n");
        
        for (CPU cpu : scheduler.CPUs) {
            System.out.printf("%d%% used   	",cpu.getUsage()*100/scheduler.GetTime());
        }

        /*for (int i = 0; i < scheduler.CPUs.size(); i++) {
            System.out.print("╰―――――――――――╯   ");
        }*/

        //System.out.print("\n");
        //System.out.print("\n");
    }
    
    private void displayIODevices()
    {
        /*for (int i = 0; i < scheduler.IODevices.size(); i++) {
            System.out.print("┌────────────────�   ");
        }*/

        System.out.println("\n");

        for (int i = 0; i < scheduler.IODevices.size(); i++) {
            String state = "idle  ";
            Process currentProcess = scheduler.IODevices.get(i).currentProcess;
            if (currentProcess != null)
            {
                state = currentProcess.BurstInfo() + " ";
            }else if(scheduler.IODevices.get(i).prevProcess != null) {
            	Process prev = scheduler.IODevices.get(i).prevProcess;
            	int lastLength = prev.bursts.get(prev.getBurstsCompleted()-1);
            	state = String.format("%02d/%02d", lastLength, lastLength);
            }

            System.out.printf("I/O %02d: %s	", i, state);
        }

        //System.out.print("\n");

        /*for (int i = 0; i < scheduler.IODevices.size(); i++) {
            System.out.print("└────────────────┘   ");
        }*/

        System.out.println("\n");
    }
    
    private void displayQueue(List<Process> queue, String name)
    {
        System.out.print(name + "  {");
        for (int i = 0; i < queue.size(); i++) {
            if (i > 0)
                System.out.print(", ");

            System.out.print(queue.get(i).GetName());
        }
        System.out.print("}");
        System.out.print("\n");
    }
    
    private void displayProcesses()
    {
        for (int i = 0; i < scheduler.allProcesses.size(); i++) {
            System.out.println(scheduler.allProcesses.get(i));
        }
    }
    
    private void displayStats()
    {
        System.out.println(stats.getRunningStats());
    }
    
    private void displayEvents() {
    	String tickLogs = Logger.GetLogger().drainLog();
    	if(tickLogs == "") {
    		System.out.println("\nEvents: None\n");
    		return;
    	}
    	System.out.println("\nEvents: \n"+tickLogs);
    }

}
