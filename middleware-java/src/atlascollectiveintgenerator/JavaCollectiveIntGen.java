package atlascollectiveintgenerator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.lang.model.element.Modifier;

import com.squareup.javapoet.*;

import atlasdsl.*;
import atlassharedclasses.*;

public class JavaCollectiveIntGen extends CollectiveIntGen {
	private static final boolean GENERATE_INDIVIDUAL_ROBOT_CI = false;

	public JavaCollectiveIntGen(Mission m) {
		super(m);
	}
	
	//find all the messages relevant for this match this code generation type/component
	private List<Message> findRelevantMessages(CollectiveIntGenTypes cg) {
		if (cg == CollectiveIntGenTypes.ALL_ROBOTS) {
			List<Robot> rbTargets = mission.getAllRobots();
			// TODO: look at this and see if we can do something better
			List<Component> targets = rbTargets.stream().map(rb -> (Component)rb).collect(Collectors.toList());
			System.out.println(targets);
			return mission.messagesToAnyComponent(targets);
		} else {
			// TODO: handle computers in findRelevantMessages
			//targets = mission.getAllComputers();
			System.out.println("cg not handled");
			return new ArrayList<Message>();
		}
	}
	
	private void generateStandardHooks(TypeSpec.Builder ciClass) {
		MethodSpec hook = MethodSpec.methodBuilder("init")
				.addModifiers(Modifier.PUBLIC)
				.addModifiers(Modifier.STATIC)
				.returns(void.class).build();
		ciClass.addMethod(hook);
	}

	private void generalMessageReceptionHooks(TypeSpec.Builder ciClass, CollectiveIntGenTypes cgt) {
		List<Message> msgs = findRelevantMessages(cgt);
		System.out.println("generateMessageReceptionHooks - msg count = " + msgs.size());
		for (Message m : msgs) {
			String msgMethodName = m.getName() + "Hook";
			System.out.println("adding method - " + msgMethodName);
			MethodSpec hook = MethodSpec.methodBuilder(msgMethodName)
						.addModifiers(Modifier.PRIVATE)
						.returns(void.class)
						.addParameter(Message.class, m.getName())
						.build();
			ciClass.addMethod(hook);
		}
	}
	
	// Generates a general set of hooks for all robots
	// Message arrivals
	// Sensors, with a second parameter indicating the identity
	public void generateRobotCI(MethodSpec.Builder activeMQHooks, CIFiles cif, Robot robot) {
		String className = "RobotCI";
		if (robot != null) {
			className = className + robot.getName();
		}
			
		TypeSpec.Builder ciClass = TypeSpec.classBuilder(className);
		generateStandardHooks(ciClass);
		if (robot != null) {
			// TODO: store the parent class generated
			//ciClass.superclass(cif.getClass("robotCollectiveIntelligence").class);
		}
		
		// Need to find all the potential hooks for ANY robot
		// and need to know which robot the hooks came from?
		
		List<Message> msgs = findRelevantMessages(CollectiveIntGenTypes.ALL_ROBOTS);
		for (Message m : msgs) {
			String hookName = m.getName() + "MessageHook";
			System.out.println("adding method - " + hookName);
			MethodSpec hook = MethodSpec.methodBuilder(hookName)
						.addModifiers(Modifier.PUBLIC)
						.returns(void.class)
						.addParameter(Message.class, m.getName())
						.addParameter(Component.class, "from")
						.addParameter(Component.class, "to")
						.build();
			ciClass.addMethod(hook);
		}
		
		try {
			FileWriter robotOut = cif.getOpenFile(className + ".java");
			JavaFile javaFile = JavaFile.builder("collectiveint", ciClass.build()).build();
			javaFile.writeTo(robotOut);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String activeMQcodeForSensorType(SensorType st) {
		switch (st) {
		case SONAR:		
			return "if (a.getContentsClass() == SensorDetection.class) {\n" + 
	    	"	  Optional<SensorDetection> d_o = a.getSensorDetection();\n"+
	    	"	  if (d_o.isPresent()) {\n" +
	    	"		  SensorDetection d = d_o.get();\n" +
	    	"		  if (d.getSensorType() == SensorType.SONAR) {\n" +
	    	"		  	  ComputerCIshoreside.SONARDetectionHook(d_o.get(), (String)d.getField(\"robotName\"));\n" +
	    	"		  }\n" +
	    	"	  }\n" +
	    	"  };\n";
			
		case CAMERA:
			return "if (a.getContentsClass() == SensorDetection.class) {\n" + 
	    	"	  Optional<SensorDetection> d_o = a.getSensorDetection();\n"+
	    	"	  if (d_o.isPresent()) {\n" +
	    	"		  SensorDetection d = d_o.get();\n" +
	    	"		  if (d.getSensorType() == SensorType.CAMERA) {\n" +
	    	"		  	  ComputerCIshoreside.CAMERADetectionHook(d_o.get(), (String)d.getField(\"robotName\"));\n" +
	    	"		  }\n" +
	    	"	  }\n" +
	    	"  };\n";
			
		case GPS_POSITION:
			return "if (a.getContentsClass() == GPSPositionReading.class) {\n" + 
					"		  Optional<GPSPositionReading> r_o = a.getGPSPositionReading();\n" + 
					"		  if (r_o.isPresent()) {\n" + 
					"			  GPSPositionReading r = r_o.get();\n" + 
					"			  ComputerCIshoreside.GPS_POSITIONDetectionHook(r.getX(),r.getY(), r.getRobotName());\n" + 
					"		  }\n" + 
					"	  }";
		default:
			return "";
		}
	}
	
	private void addMethodHookForSensorType(TypeSpec.Builder ciClass, SensorType st, String hookName) {
		MethodSpec.Builder m = null;
		switch (st) {
			case SONAR:
				m = MethodSpec.methodBuilder(hookName)
					.addModifiers(Modifier.PUBLIC)
					.addModifiers(Modifier.STATIC)
					.returns(void.class)
					.addParameter(SensorDetection.class, "detection")
					.addParameter(String.class, "robotName");
				ciClass.addMethod(m.build());
				break;
				
			case CAMERA:
				m = MethodSpec.methodBuilder(hookName)
					.addModifiers(Modifier.PUBLIC)
					.addModifiers(Modifier.STATIC)
					.returns(void.class)
					.addParameter(SensorDetection.class, "detection")
					.addParameter(String.class, "robotName");
				ciClass.addMethod(m.build());
				break;
			
			case GPS_POSITION:
				m = MethodSpec.methodBuilder(hookName)
					.addModifiers(Modifier.PUBLIC)
					.addModifiers(Modifier.STATIC)
					.returns(void.class)
					.addParameter(Double.class, "x")
					.addParameter(Double.class, "y")
					.addParameter(String.class, "robotName");
				    
				ciClass.addMethod(m.build());
				break;
		}
	}
	
	public void generateComputerCI(MethodSpec.Builder activeMQHooks, MethodSpec.Builder initHooks, CIFiles cif, Computer c) {
		String className = "ComputerCI";
		if (c != null) {
			className = className + c.getName();
		}
		
		TypeSpec.Builder ciClass = TypeSpec.classBuilder(className);
		generateStandardHooks(ciClass);
		activeMQHooks.addCode("super.handleMessage(a);\n");
		
		// Need to find all the potential hooks for ANY robot
		// and need to know which robot the hooks came from?
		List<Message> msgs = findRelevantMessages(CollectiveIntGenTypes.ALL_COMPUTERS);
		for (Message m : msgs) {
			String hookName = m.getName() + "MessageHook";
			System.out.println("DEBUG: adding method - " + hookName);
			MethodSpec hook = MethodSpec.methodBuilder(hookName)
						.addModifiers(Modifier.PUBLIC)
						.addModifiers(Modifier.STATIC)
						.returns(void.class)
						.addParameter(Message.class, m.getName())
						.addParameter(Component.class, "from")
						.addParameter(Component.class, "to")
						.build();
			
			ciClass.addMethod(hook);

        };
	
		// Generate a hook for any possible sensor notification 
		// on any robot in the system - since the computers process
        // all the notifications via pShare, this is done at the 
        // shoreside
		for (Map.Entry<SensorType, Robot> sr : mission.getAllSensorTypesOnVehicles().entrySet()) {
			System.out.println("sr_map = " + sr.toString());
			SensorType st = sr.getKey();
			Robot r = sr.getValue();
			String sensorTypeName = Sensor.sensorTypeToString(st).toUpperCase();
			
			System.out.println("DEBUG: adding method stub for sensor " + sensorTypeName);
			String hookName = st.toString() + "DetectionHook";
			
			addMethodHookForSensorType(ciClass, st, hookName);
			activeMQHooks.addCode(activeMQcodeForSensorType(st) + "\n");
		}
		
		initHooks.addCode("super.init();\n");
		initHooks.addCode(className + ".init();");
		
		try {
			FileWriter robotOut = cif.getOpenFile(className + ".java");
			List<String> customImports = new ArrayList<String>();
			customImports.add("atlascollectiveint.api.*");

			System.out.println("Adding custom imports");
			JavaFile javaFile = JavaFile.builder("atlascollectiveint.custom", ciClass.build()).build();
			String output = injectImports(javaFile, customImports);
			robotOut.write(output);
			robotOut.flush();
			robotOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// fix for https://github.com/square/javapoet/issues/512
	public String injectImports(JavaFile javaFile, List<String> imports) {
		    String rawSource = javaFile.toString();

		    List<String> result = new ArrayList<>();
		    for (String s : rawSource.split("\n", -1)) {
		      result.add(s);
		      if (s.startsWith("package ")) {
		        result.add("");
		        for (String i : imports) {
		          result.add("import " + i + ";");
		        }
		      }
		    }
		    return String.join("\n", result);
	}
	
	
	public void generateCollectiveIntFiles(String baseDir, CollectiveIntGenTypes cgt) {
			CIFiles cif = new CIFiles(baseDir);
			
			TypeSpec.Builder activeMQLink = TypeSpec.classBuilder("CustomCollectiveInt");
			activeMQLink.superclass(CollectiveInt.class);
			activeMQLink.addModifiers(Modifier.PUBLIC);
			
			MethodSpec.Builder initHooks = MethodSpec.methodBuilder("init");
			initHooks.addModifiers(Modifier.PUBLIC);
			
			MethodSpec.Builder addMQHooks = MethodSpec.methodBuilder("handleMessage");
			addMQHooks.addModifiers(Modifier.PROTECTED);
			addMQHooks.addParameter(ATLASSharedResult.class, "a");
						
			for (Robot r : mission.getAllRobots()) {
				if (GENERATE_INDIVIDUAL_ROBOT_CI) {
					generateRobotCI(addMQHooks, cif, r);
				}
			}
			
			for (Computer c : mission.getAllComputers()) {
				generateComputerCI(addMQHooks, initHooks, cif, c);
			}
			
			activeMQLink.addMethod(addMQHooks.build());
			activeMQLink.addMethod(initHooks.build());
			
			// Use additional imports: https://github.com/square/javapoet/issues/512
			List<String> customImports = new ArrayList<String>();
			customImports.add("atlassharedclasses.*");
			customImports.add("atlasdsl.SensorType");
			customImports.add("java.util.Optional");
			
			try {
				FileWriter mqFileOut = cif.getOpenFile("CustomCollectiveInt.java");
				JavaFile mqFile = JavaFile.builder("atlascollectiveint.custom", activeMQLink.build()).build();
				String modified = injectImports(mqFile, customImports);
				mqFileOut.write(modified);
				mqFileOut.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("\nCI Generation Completed");
	}
}
