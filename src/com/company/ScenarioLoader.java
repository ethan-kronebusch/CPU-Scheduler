/*
 * @author: Ethan Kronebusch
 * Date: 2022-02-05
 * Project: CPU_Scheduler
 * Program: ScenarioLoader.java
 * Description: Parses scenario files into a list of Process objects readable by the program.
 */
package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.InvalidPropertiesFormatException;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;

/**
 * @author krone
 *
 */
public class ScenarioLoader {

	/**
	 * @throws InvalidPropertiesFormatException, FileNotFoundException
	 * 
	 */
	// just pass string to a file, let the next method deal with it
	public static ArrayList<Process> loadScenario(String path) throws FileNotFoundException, InvalidPropertiesFormatException{
		File stream = new File(path);
		return loadScenario(stream);
	}
	
	//read and parse scenario file. If file doesn't exist, throws a FileNotFoundException.
	public static ArrayList<Process> loadScenario(File path) throws FileNotFoundException, InvalidPropertiesFormatException{
		if(!path.exists()) {
			throw new FileNotFoundException("File " + path.getAbsolutePath() + " does not exist. Please select a different file.");
		}
		
		//read the scenario file line by line and add processes to output list
		ArrayList<Process> processes = new ArrayList<>();
		Scanner reader = new Scanner(path);
		String[] line;
		while(reader.hasNextLine()) {
			line = reader.nextLine().split(" ");
			try {
				int[] nums = Arrays.stream(Arrays.copyOfRange(line, 1, line.length)).mapToInt(Integer::parseInt).toArray();
				ArrayList<Integer> bursts = new ArrayList<Integer>(Arrays.stream(Arrays.copyOfRange(nums, 2, nums.length)).boxed().collect(Collectors.toList()));
				if(bursts.size() % 2 == 0) {
					throw new Exception("Process must start and end with a CPU burst.");
				}
				processes.add(new Process(line[0], nums[0], nums[1], bursts));
			}catch(Exception e) {
				reader.close();
				throw new InvalidPropertiesFormatException("Invalid scenario file! Please check section 3.2.2 of the CPU Scheduling assignment for formatting details.\n"+e.getMessage());
			}
		}
		reader.close();
		return processes;
	}
	
	//show file choosing dialog, pass to the parsing method
	public static ArrayList<Process> loadScenario() throws FileNotFoundException, InvalidPropertiesFormatException{
		JFileChooser scenarioFinder = new JFileChooser(new File(System.getProperty("user.home")));
		int result = scenarioFinder.showOpenDialog(null);
		ArrayList<Process> output = new ArrayList<>();
		
		if(result == JFileChooser.APPROVE_OPTION) {
			File scenarioFile = scenarioFinder.getSelectedFile();
			output = loadScenario(scenarioFile);
		}
		return output;
	}
	
	//main method for testing purposes
	public static void main(String[] args) {
		ArrayList<Process> arr = new ArrayList<>();
		
		while(true) {
			try {
				arr = loadScenario();
			}catch(FileNotFoundException fe) {
				System.out.println("File not found! Try again.");
				continue;
			}catch(InvalidPropertiesFormatException ipe) {
				
			}
			break;
		}
		
		System.out.println(arr.get(0));
	}
}
