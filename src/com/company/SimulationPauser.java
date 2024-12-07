package com.company;

import java.util.Locale;

public class SimulationPauser implements Runnable{

    private Scheduler scheduler;
    //scheduler.isFinished()

    public SimulationPauser(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        // prompt user for input
        inputPrompt();
        Main.sc.nextLine();

        // Run continuously accepting user input to pause/resume simulation
        while (true)
        {
            String input = Main.sc.nextLine();
            // If process is finished prompt user to save log file
            if (scheduler.isFinished())
            {
            	break;
            }
            // pause when the user enters p
            // resume when the user enters r
            // start when the user enters s
            // If the input was not understood ask user for more input
            switch(input.toLowerCase(Locale.ROOT)) {
            case "p":
            	Main.SetPaused(true);
                inputPrompt();
                break;
            case "r":
            	Main.SetPaused(false);
            	break;
            case "s":
            	Main.SetStarted(true);
            	break;
            case "xyzzy":
            	System.out.println("A hollow voice says \"Fool.\"");
            	inputPrompt();
            	break;
            default:
            	System.out.println("Sorry, we did not understand your input");
                inputPrompt();
            }
        }
    }

    private void inputPrompt()
    {
    	if(!Main.isStarted()) {
    		System.out.println("Please enter: \ns to start execution \np to pause execution \nr to resume execution");
    	}else {
    		System.out.println("Please enter: \np to pause execution \nr to resume execution");
    	}
    }
}
