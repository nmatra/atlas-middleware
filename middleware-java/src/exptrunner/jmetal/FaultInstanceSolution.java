package exptrunner.jmetal;

import java.util.List;
import java.util.Map;

import org.uma.jmetal.solution.Solution;

public class FaultInstanceSolution implements Solution {

	private static final long serialVersionUID = 1L;

	// Represents a fault instance
	// The fault name
	// Variables for the time start and length
	// Intensity information (how to mutate?)
	
	@Override
	public void setObjective(int index, double value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double getObjective(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double[] getObjectives() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getVariable(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List getVariables() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVariable(int index, Object variable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public double[] getConstraints() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getConstraint(int index) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setConstraint(int index, double value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNumberOfVariables() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfObjectives() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getNumberOfConstraints() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Solution copy() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAttribute(Object id, Object value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getAttribute(Object id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasAttribute(Object id) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}
}