package middleware.codegen;

import atlascarsgenerator.ConversionFailed;
import atlascarsgenerator.MOOSCodeGen;

import atlascollectiveintgenerator.*;

import atlasdsl.*;
import atlasdsl.loader.*;
import carsmapping.CARSSimulation;

public class GenMOOSCode {
	public static void testCodeGenerationMOOS(String code_dir) throws DSLLoadFailed {
		DSLLoader dslloader = new GeneratedDSLLoader();
		Mission mission;
		mission = dslloader.loadMission();

		
		MOOSCodeGen gen = new MOOSCodeGen(mission);
		CollectiveIntGen javaCI = new JavaCollectiveIntGen(mission);		
		System.out.println("Converting DSL to MOOS representation...");
		try {
			CARSSimulation moossim = gen.convertDSL(mission);
			System.out.println("DSL conversion completed");
			moossim.generateCARSInterface(code_dir);
			System.out.println("Code generation completed");
			javaCI.generateCollectiveIntFiles("src/atlascollectiveint/custom", CollectiveIntGenTypes.ALL_COMPUTERS);
			
		} catch (ConversionFailed cf) {
			
			System.out.println("ERROR: DSL conversion to MOOS representation failed: reason " + cf.getReason());
		}
	}
	
	public static void main(String [] args) {
		String outputCodeDir = System.getProperty("user.dir") + "/moos-sim/";
		System.out.println("Generating code from DSL in: " + outputCodeDir);
		try {
			testCodeGenerationMOOS(outputCodeDir);
		}
		catch (DSLLoadFailed e) {
			System.out.println("DSL loading configuration error");
			e.printStackTrace();
			System.exit(1);
		}
	}
}

