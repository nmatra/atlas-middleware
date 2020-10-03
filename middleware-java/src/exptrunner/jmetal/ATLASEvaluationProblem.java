package exptrunner.jmetal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Scanner;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.problem.Problem;

import atlasdsl.Mission;
import atlasdsl.faults.Fault;
import atlassharedclasses.FaultInstance;

public class ATLASEvaluationProblem implements Problem<FaultInstanceSetSolution> {

	private static final long serialVersionUID = 1L;
	private static final double INITIAL_MIN_SPEED_VALUE = 1.0;
	private static final double INITIAL_MAX_SPEED_VALUE = 5.0;
	private static final int DETECTIONS_PER_OBJECT_EXPECTED = 2;
	private int INITIAL_VARAIBLE_SIZE = 10;
	private int runCount = 0;
	private Random rng;
	
	private boolean realExperiment = true;
	
	private Mission mission;
	private boolean actuallyRun;
	private double exptRunTime;
	private String logFileDir;
	
	// This gives the weights for these different goals
	private Map<GoalsToCount, Integer> goalsToCount = new HashMap<GoalsToCount, Integer>();
	private Object algorithm;
	
	private FileWriter tempLog;
	private int variableFixedSize;
	
	public ATLASEvaluationProblem(Random rng, Mission mission, boolean actuallyRun, double exptRunTime, String logFileDir, Map<GoalsToCount, Integer> goalsToCount) throws IOException {
		this.rng = rng;
		this.mission = mission;
		this.exptRunTime = exptRunTime;
		this.logFileDir = logFileDir;
		this.actuallyRun = actuallyRun;
		this.goalsToCount = goalsToCount;
		this.variableFixedSize = mission.getFaultsAsList().size();
		
		tempLog = new FileWriter("tempLog.res");
	}
	
	public void setFakeExperiment() {
		realExperiment = false;
	}
	
	public int goalWeight(GoalsToCount g) {
		return goalsToCount.getOrDefault(g, 0);
	}

	public int getNumberOfVariables() {
		// TODO: this is fixed
		return variableFixedSize;
	}

	public int getNumberOfObjectives() {
		return 3;
	}

	public int getNumberOfConstraints() {
		return 0;
	}

	public String getName() {
		return "ATLASEvaluationProblem";
	}

	public void performATLASExperiment(FaultInstanceSetSolution solution) {
		String exptTag = "exptGA-" + (runCount++);
		try {
			RunExperiment.doExperiment(mission, exptTag, solution.getFaultInstances(), actuallyRun, exptRunTime);
			readLogFiles(logFileDir, solution);
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void fakeExperiment(FaultInstanceSetSolution solution) {
		int missedDetections = 0;
		int avoidanceViolations = 0;
		double timeProp = solution.faultCostProportion();
		
		//solution.setObjective(2, timeProp);
		//if (solution.hasFaultInstanceMatching((FaultInstance fi) ->
		//{ if fi.	})) {
			//missedDetections += 1;
		//}
		
		solution.setObjective(0, missedDetections);
		solution.setObjective(1, avoidanceViolations);
	}
	
	

	public void readLogFiles(String logFileDir, FaultInstanceSetSolution solution) {
		// Read the goal result file here - process the given goals
		// Write it out to a common result file - with the fault info
		File f = new File(logFileDir + "/goalLog.log");
		int detections = 0;
		int missedDetections = 0;
		int avoidanceViolations = 0;
		
		Scanner reader;
		try {
			reader = new Scanner(f);
			while (reader.hasNextLine()) {
				String line = reader.nextLine();
				String[] fields = line.split(",");
				String goalClass = fields[0];
				if (goalClass.equals("atlasdsl.DiscoverObjects")) {
					String time = fields[1];
					String robot = fields[2];
					String num = fields[3];
					// TODO: check the uniqueness of the detections
					
					detections += 1;
				}
				
				if (goalClass.equals("atlasdsl.AvoidOthers")) {
					avoidanceViolations += 1;
				}
			}
			
			
			missedDetections = (mission.getEnvironmentalObjects().size() * DETECTIONS_PER_OBJECT_EXPECTED) - detections;
			missedDetections = Math.max(missedDetections, 0);
			double timeProp = solution.faultCostProportion();
			
			String logRes = missedDetections + "," + avoidanceViolations + "," + timeProp + "\n";
			System.out.println(solution);
			System.out.println(logRes);
			tempLog.write(logRes);
			tempLog.flush();
			System.out.println(logRes);
			reader.close();
			
			
			
			solution.setObjective(0, -missedDetections);
			solution.setObjective(1, -avoidanceViolations);
			solution.setObjective(2, timeProp);
			
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void evaluate(FaultInstanceSetSolution solution) {
		if (realExperiment)
			performATLASExperiment(solution);
		else 
			fakeExperiment(solution);
	}
	
	private FaultInstance setupAdditionalInfo(FaultInstance input) {
		Fault f = input.getFault();
		FaultInstance output = input;
		if (f.getName().contains("SPEEDFAULT")) {
			double newSpeed = INITIAL_MIN_SPEED_VALUE + rng.nextDouble() * (INITIAL_MAX_SPEED_VALUE - INITIAL_MIN_SPEED_VALUE);
			output.setExtraData(Double.toString(newSpeed));	
		}
		
		if (f.getName().contains("HEADINGFAULT")) {
			double newHeading = rng.nextDouble() * 360.0;
			output.setExtraData(Double.toString(newHeading));	
		}
		return output;
	}
	
	private FaultInstance newFaultInstance(Fault f) {
		double maxRange = f.getLatestEndTime() - f.getEarliestStartTime();
		double timeStart = f.getEarliestStartTime() + rng.nextDouble() * maxRange;

		double rangeOfEnd = f.getLatestEndTime() - timeStart;
		double timeEnd = timeStart + rng.nextDouble() * rangeOfEnd;
		

		FaultInstance fi = new FaultInstance(timeStart, timeEnd, f, Optional.empty());
		return setupAdditionalInfo(fi);
	}

	private void setupInitialPopulation(FaultInstanceSetSolution fiss) {
		System.out.println("Setting up initial population...");
		List<Fault> allFaults = mission.getFaultsAsList();
		
		Collections.shuffle(allFaults, rng);

		int i = 0;
		int limit = rng.nextInt(allFaults.size()-1) + 1;
		
		for (Fault f : allFaults) {
			if (i < limit) {
				FaultInstance fi = newFaultInstance(f);
				fiss.setContents(i++, fi);
			}
		}
		System.out.println("Initial chromosome = " + fiss.toString());
	}

	public FaultInstanceSetSolution createSolution() {
		FaultInstanceSetSolution fiss = new FaultInstanceSetSolution(mission, "TAGTEST", actuallyRun, exptRunTime);
		setupInitialPopulation(fiss);
		return fiss;
	}
	
	public void setAlgorithm(Algorithm<List<FaultInstance>> algorithm) {
		this.algorithm = algorithm;
	}
}
