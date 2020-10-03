package exptrunner.jmetal;

import java.io.FileNotFoundException;
import java.util.Optional;

import org.uma.jmetal.util.JMetalException;

import atlasdsl.Mission;
import atlasdsl.loader.DSLLoadFailed;
import atlasdsl.loader.DSLLoader;
import atlasdsl.loader.GeneratedDSLLoader;
import exptrunner.jmetal.test.ATLASEvaluationProblemDummy.*;

public class JMetalMutationDummyTest_MinimalFaults {
	
	static int runCount = 100;
	
	public static void main(String[] args) throws JMetalException, FileNotFoundException {
		DSLLoader dslloader = new GeneratedDSLLoader();
		Mission mission;
		try {
			mission = dslloader.loadMission();
			for (int i = 0; i < runCount; i++) {
				JMetalMutationRunner.jMetalRun(mission, Optional.of(EvaluationProblemDummyChoices.MINIMAL_FAULT_INSTANCE_COUNT));
			}
		} catch (DSLLoadFailed e) {
			System.out.println("DSL loading failed - configuration problems");
			e.printStackTrace();
		}
	}
}
