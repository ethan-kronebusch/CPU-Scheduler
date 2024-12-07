package com.company;

// This file is a work in progress, with potential ideas for each scheduling process contained in comments.

import java.util.List; // Needed to preform operations on an inputted list of processes.

public class SchedulerAlgorithms
{

//  private Scheduler scheduler;
//
//  public SchedulingAlgorithms(Scheduler scheduler)
//  {
//    this.scheduler = scheduler;
//  }

  public static Process GetNextProcess(List<Process> processes, SchedulerAlgorithm algorithm)
  {
    if (processes.size() <= 0)
        return null;

    switch (algorithm) {
      case FCFS -> {
        return fcfs(processes);
      }
      case RR -> {
        // The only difference between rr and fcfs is that in rr the cpu will give up it's process after a
        // certain amount of time(QuantumeTime).
        return fcfs(processes);
      }
      case SJF -> {
        return sjf(processes);
      }
      case PS -> {
        return ps(processes);
      }
    }
    // This should never run
    System.out.println("None of the scheduling algorithms were picked. Something is wrong");
    return null;
  }


  /**
    The fcfs method is used to preform first-come-first-serve scheduling on the set of processes that are inputted.
    (add the at symbol)param processList A list of processes needed to be executed (Need to find out what type of array list or create a general).
  */
  private static Process fcfs(List<Process> processes)
  {
    return processes.get(0);
  }
  
  /**
    The sjf method is used to preform shortest-job-first scheduling on the set of processes that are inputted.
    param processList A list of processes needed to be executed (Need to find out what type of array list or create a general.)
  */
  private static Process sjf(List<Process> processes)
  {
    Process shortest = processes.get(0);

    // store the process with the shortest next burst time in shortest
    for (int i = 1; i < processes.size(); i++) {
        if (processes.get(i).getBurstTime() < shortest.getBurstTime())
            shortest = processes.get(i);
    }

    return shortest;
  }
  
  /**
    The ps method does priority scheduling on a set of processes inputted.
    param processList A list of processes needed to be executed (Need to find out what type of array list or create a general.)
  */
  private static Process ps(List<Process> processes)
  {
    Process highestPriority = processes.get(0);

    // store the process with the highest priority(lowest integer value)  in highestPriority
    for (int i = 1; i < processes.size(); i++) {
      if (processes.get(i).getPriority() < highestPriority.getPriority())
          highestPriority = processes.get(i);
    }

    return highestPriority;
  }
}
