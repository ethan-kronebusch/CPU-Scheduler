package com.company;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class Logger {

    // This contains the simulation logs
    private String outputText = "";
    //This contains temporary (per-tick) logs
    private String tempOutput = "";

    // Make static instance of logger that can only be accessed using method GetLogger, for easy access anywhere in project
    private static Logger logger;
    public static Logger GetLogger()
    {
        if (logger == null)
        {
            logger = new Logger();
        }
        return logger;
    }

    public void PromptUserToSaveLogs()
    {
        System.out.print("Would you like to save the simulation log? (y/n): ");
        String answer = "";
        answer = Main.sc.nextLine();
        //Wait for user to enter proper input
        while (isAnswerValid(answer) == false)
        {
            System.out.println("Incorrect input, please answer with y or n");
            answer = Main.sc.nextLine();
        }

        if (isAnswerAffirmative(answer))
        {
            SaveLogs();
        }
    }

    private boolean isAnswerValid(String answer)
    {
        if (answer == null)
            return false;

        switch(answer.toLowerCase(Locale.ROOT)) {
	        case "y":
	        case "yes":
	        case "n":
	        case "no":
	        	return true;
	        default:
	        	return false;
        }
    }

    private boolean isAnswerAffirmative(String answer)
    {
        if (answer.toLowerCase(Locale.ROOT).charAt(0) == 'y') {
            return true;
        }else {
        	return false;
        }
    }

    // Prints out logString and Adds it to outputText variable
    public synchronized void Log(String logString)
    {
        outputText += logString;
        outputText += "\n";
        tempOutput += logString;
        tempOutput += "\n";
    }
    
    public String drainLog() {
    	String out = tempOutput;
    	tempOutput = "";
    	return out;
    }

    public void SaveLogs()
    {
        System.out.println("Please enter file path/name (Note : if the entered file already exists it will be overwritten)");
        String fileName = Main.sc.nextLine();

        createFile(fileName);
        writeFile(fileName);
    }
    
    public void SaveLogs(String path)
    {
        createFile(path);
        writeFile(path);
    }

    private void createFile(String fileName)
    {
        try {
            File myObj = new File(fileName);
            if (myObj.createNewFile()) {
                System.out.println("File created: " + myObj.getName());
            } else {
                System.out.println("File already exists.");
            }
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    private void writeFile(String fileName)
    {
        try {
            FileWriter myWriter = new FileWriter(fileName);
            myWriter.write(outputText);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    //Use for testing this class
    public  static  void main(String[] args)
    {
        GetLogger().Log("This is a test");
        GetLogger().Log("This should be the second line");
        GetLogger().Log("");
        GetLogger().Log("This should be the third fourth line");
        GetLogger().SaveLogs();
    }

}
