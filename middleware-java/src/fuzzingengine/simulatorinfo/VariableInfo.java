package fuzzingengine.simulatorinfo;

public class VariableInfo {

}
enum VariableDirection {
	INBOUND = 1;
	OUTBOUND = 2;
}

enum VariableNature {
	BINARY = 1;
	CONFIG = 2;
}

class VariableSpecification {
	val Simulator[1] sim;
	attr String component;
	attr String variable;
	attr VariableDirection dir;
 	attr VariableNature nature;
}

class Simulator {
	attr String name;
	attr String baseDirectory;
	val VariableSpecification[*] vars;
}

class TopologySpecification {
	val Simulator[1] sim;
	val Atlas.Message[1] msg;
	// The variables that are involved
	val VariableSpecification[*] specs;
}

class VariableMappings {
	val VariableSpecification[*] vars; 
	val Simulator[*] sims;
}