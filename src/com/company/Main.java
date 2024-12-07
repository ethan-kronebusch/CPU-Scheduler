package com.company;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.InvalidPropertiesFormatException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Main {

    // This is the master time that controls the simulation
    private static int systemTime;

    // The number of simulation steps that should occur per second
    private static  double stepsPerSecond=-1;

    // When true simulation advances automatically based on StepsPerSecond, When false manual input advances simulation
    private static boolean autoSimulate;

    private static boolean started;
    private static boolean paused;
    
    public static Scanner sc = new Scanner(System.in);

    public static Semaphore SimulationSync = new Semaphore(0);
    
    // Set the number of cpu and io devices to use
    private final static int CPU_COUNT = 2;
    private final static int IO_COUNT = 2;


    public static void main(String[] args) throws InterruptedException {

    	//parse console args
    	try {
    		autoSimulate = Integer.parseInt(args[0]) == 0 ? true : false;
    	} catch(Exception e) {
    		boolean flag=true;
    		while(flag) {
    			System.out.print("Automate simulation? (y/n): ");
    			String choice = sc.next();
    			
    			switch(choice.toLowerCase()) {
    			case "0":
    			case "y":
    			case "yes":
    				autoSimulate = true;
    				flag=false;
    				break;
    			case "1":
    			case "n":
    			case "no":
    				autoSimulate = false;
    				flag=false;
    				break;
    			default:
    				System.out.println("Invalid input. Please enter y for auto simulation or n for manual simulation.\n");
    				continue;
    			}
    		}
    	}
    	try {
    		stepsPerSecond = Double.parseDouble(args[1]);
    	}catch(Exception e) {
    		while(true) {
    			System.out.print("Please enter the speed of the simulation in steps per second: ");
    			try {
    				stepsPerSecond = sc.nextDouble();
    			}catch(Exception e2) {
    				System.out.println("Please only enter a number!\n");
    				continue;
    			}
    			
    			if (stepsPerSecond <= 0)
    	        {
    	            System.out.println("The Simulation speed of the program must be greater than zero!\n");
    	            continue;
    	        }else if(stepsPerSecond >= 100000) {
    	        	System.out.println("Don't you think that speed is a little ridiculous?\n");
    	        	continue;
    	        }else {
    	        	break;
    	        }
    		}
    	}
    	int quantumLength;
    	try {
    		quantumLength = Integer.parseInt(args[2]);
    	}catch(Exception e) {
    		quantumLength = 0;
    	}

        ArrayList<Process> scenario = null;
        SchedulerAlgorithm algo = null;
        
        //prompt for scenario file and validate
        while(scenario == null) {
        	System.out.print("Select scenario file: ");
        	try {
        		scenario = ScenarioLoader.loadScenario();
        	}catch(InvalidPropertiesFormatException ipe) {
        		System.err.println("That scenario file is invalid.");
        		System.err.println("Error: " + ipe.getMessage() + "\n");
        		continue;
        	} catch (FileNotFoundException fe) {
				System.err.println("Invalid file specified!");
				System.err.println("Error: " + fe.getMessage() + "\n");
				continue;
			}
        	if(scenario != null) {
        		System.out.println("Selected!");
        	}
        }
        
        //prompt for algorithm and validate
        while(algo == null) {
        	System.out.print("Select scheduling algorithm:\n1 - First Come First Served\n2 - Shortest Job First\n3 - Round-Robin\n4 - Priority Scheduling\nEnter selection here: ");
        	
        	switch(sc.nextInt()) {
        	case 1:
        		algo = SchedulerAlgorithm.FCFS;
        		break;
        	case 2:
        		algo = SchedulerAlgorithm.SJF;
        		break;
        	case 3:
        		algo = SchedulerAlgorithm.RR;
        		break;
        	case 4:
        		algo = SchedulerAlgorithm.PS;
        		break;
        	default:
        		System.out.println("Error: invalid input. Please enter either 1, 2, 3, or 4.");
        		continue;
        	}
        }
        
        //prompt for quantum length and validate
        if(algo == SchedulerAlgorithm.RR) {
        	while(quantumLength <= 0) {
        		System.out.print("Please enter desired quantum length: ");
        		try {
        			quantumLength = sc.nextInt();
        		}catch(Exception e) {
        			System.out.println("Please enter a whole number.\n");
        			continue;
        		}
        		
        		if(quantumLength <= 0) {
        			System.out.println("Please enter a value greater than 0. How would a quantum be 0 or less?\n");
        		}
        	}
        } else {
        	quantumLength = 0;
        }

        System.out.println();
        ExecutorService executor = Executors.newFixedThreadPool(CPU_COUNT + IO_COUNT + 1);
        Scheduler scheduler = new Scheduler(scenario, algo, quantumLength);
        executor.submit(scheduler);


        // populate the CPUs and IODevices Lists
        scheduler.CPUs = createCPUs(CPU_COUNT, scheduler, executor);
        scheduler.IODevices = createIODevices(IO_COUNT, scheduler, executor);



        // Create SyncedObjects lists with all the objects on different threads that need to stay in sync
        ArrayList<TimeSynced> SyncedObjects = new ArrayList<>();
        SyncedObjects.addAll(scheduler.CPUs);
        SyncedObjects.addAll(scheduler.IODevices);
        
        StatsCollector stats = new StatsCollector(scheduler);
        DisplayManager displayManager = new DisplayManager(scheduler, stats);
        Thread pauserT = new Thread();
        
        // Create and start pauser thread for handling user input to start/pause program during auto simulate
        if (autoSimulate)
        {
            pauserT = new Thread(new SimulationPauser(scheduler));
            pauserT.start();
        }

        // Simulation update loop. Will run until the simulation ends
        do{
            // Wait for user input or time slice in auto simulate mode
            WaitForNextStep(sc);
            systemTime++;
            // Schedule/Dispatch processes, This most run separately from other threads for consistent output results
            scheduler.TakeStep();
            // Wait for the scheduler to finish
            SimulationSync.acquire();
            // Set the SimulationSync Semaphore so that all the ProcessHandlers will have to finish before it continues
            SimulationSync = new Semaphore((-1 * SyncedObjects.size()) + 1);
            //Go through each ProcessHandler object and advance it one step in the simulation
            for (TimeSynced synced: SyncedObjects) {
                synced.TakeStep();
            }
            // Wait for all the ProcessHandler objects to finish their steps
            SimulationSync.acquire();
            // update statistics every step
            stats.updateStats();
            // Display current state of simulation
            displayManager.DisplaySimulationStep();
        }while (scheduler.isFinished() == false);

        System.out.println("No processes remain. Simulation has finished at time " + systemTime);
        if (autoSimulate) { 
        	// This is necessary because during auto simulate the SimulationPauser will still be waiting on input
            System.out.println("Please press enter to continue");
            pauserT.join();
        }
        Logger.GetLogger().Log("\n" + stats.getEndStats());
        Logger.GetLogger().PromptUserToSaveLogs();
        System.exit(0);
        // After the process finishes the SimulationPauser object which is running on a separate thread
        // will prompt the user to see if they want to save their file
    }

    // Waits for user input and then processes it
    private static void WaitForNextStep(Scanner sc)
    {
        if (autoSimulate)
        {
            try{
                Thread.sleep((long)(1000/ stepsPerSecond));

                while (paused == true || started == false)
                {
                    Thread.sleep(100);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else {
            System.out.println("press enter to continue simulation");
            sc.nextLine();
        }
    }

    // creates a list of CPUs, starts each CPU on a separate thread and returns the list
    // size : the number of CPUs to create
    // scheduler : the Scheduler that the CPUs should reference
    // executor : the thread pool to run the CPUs
    private static ArrayList<CPU> createCPUs(int size, Scheduler scheduler, ExecutorService executor)
    {
        ArrayList<CPU> cpus = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            CPU cpu = new CPU(scheduler);
            cpus.add(cpu);
            executor.submit(cpu);
        }
        return cpus;
    }

    // creates a list of IODevices, starts each IODevice on a separate thread and returns the list
    // size : the number of IODevices to create
    // scheduler : the Scheduler that the IODevices should reference
    // executor : the thread pool to run the IODevices
    private static ArrayList<IODevice> createIODevices(int size, Scheduler scheduler, ExecutorService executor)
    {
        ArrayList<IODevice> ioDevices = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            IODevice ioDevice = new IODevice(scheduler);
            ioDevices.add(ioDevice);
            executor.submit(ioDevice);
        }

        return ioDevices;
    }

    // getters/setters
    public static void SetStarted(boolean started)
    {
        Main.started = started;
    }

    public static void SetPaused(boolean paused)
    {
        Main.paused = paused;
    }
    
    public static boolean isStarted() {
    	return Main.started;
    }

    public static int GetSystemTime()
    {
        return systemTime;
    }

}
