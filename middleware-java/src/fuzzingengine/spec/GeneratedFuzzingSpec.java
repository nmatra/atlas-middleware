package fuzzingengine.spec;

import java.util.Optional;
import fuzzingengine.*;
import fuzzingengine.operations.*;

public class GeneratedFuzzingSpec {
	public static FuzzingEngine createFuzzingEngine() {
	FuzzingEngine fe = new FuzzingEngine();
	FuzzingSimMapping simMapping = new FuzzingSimMapping();
	
	simMapping.addRecord("uSimMarine", "DESIRED_THRUST", "DEZIRED_THRUST", 
	FuzzingSimMapping.VariableDirection.INBOUND, Optional.of("/home/atlas/atlas/cust/TESTBASE"));
	simMapping.addRecord("uSimMarine", "DESIRED_RUDDER", "DEZIRED_RUDDER", 
	FuzzingSimMapping.VariableDirection.INBOUND, Optional.of("/home/atlas/atlas/cust/TESTBASE"));
	
	// TODO: set up the EGL code to generate the keys
	// EGL: have EGL code for generating keys (not yet for the message setup)
	fe.setFuzzSelectionType(FuzzingSelectionType.INTERROBOT_COMMS);
	FuzzingOperation test1_OP = NumericVariableChangeFuzzingOperation.Random(0.0, 10.0);
	fe.addFuzzingKeyOperation("TEST1'", Optional.of("TEST1"), Optional.empty(), test1_OP);
	
	// need to set up reflection for this key
	fe.setSimMapping(simMapping); 
	return fe;
	}
}