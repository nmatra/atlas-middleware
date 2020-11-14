package exptrunner.jmetal;

// Breaks up variable-length chromosomes X and Y and splits it into two new ones
// Takes the ones before the cut point - X_left, Y_left  
// Takes the ones after the cut point - X_right, Y_right

// Produces two new ones X_left, Y_right
// and Y_left, X_right
import org.uma.jmetal.operator.crossover.CrossoverOperator;
import org.uma.jmetal.solution.integersolution.IntegerSolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.RandomGenerator;

import atlassharedclasses.FaultInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SimpleFaultMixingCrossover implements CrossoverOperator<FaultInstanceSetSolution> {

	private double crossoverProbability;
	private Random randomGenerator;
	private static final long serialVersionUID = 1L;

	public SimpleFaultMixingCrossover(double crossoverProbability, Random randomGenerator) {
		if (crossoverProbability < 0) {
			throw new JMetalException("Crossover probability is negative: " + crossoverProbability);
		}

		this.crossoverProbability = crossoverProbability;
		this.randomGenerator = randomGenerator;
	}

	public List<FaultInstanceSetSolution> doCrossover(FaultInstanceSetSolution cx, FaultInstanceSetSolution cy) {
		List<FaultInstanceSetSolution> output = new ArrayList<FaultInstanceSetSolution>();

		// We are creating a new empty chromosome here, using the existing one simply to set the parameters
		FaultInstanceSetSolution new1 = FaultInstanceSetSolution.empty(cx);
		FaultInstanceSetSolution new2 = FaultInstanceSetSolution.empty(cy);
		int new1_index = 0;
		int new2_index = 0;

		int xlimit = cx.getNumberOfVariables();
		int ylimit = cy.getNumberOfVariables();

		if (xlimit > 0 && ylimit > 0) {
			System.out.println("crossover input cx = " + cx.toString());
			System.out.println("crossover input cy = " + cy.toString());

			int xcut = randomGenerator.nextInt(xlimit);
			int ycut = randomGenerator.nextInt(ylimit);

			for (int x = 0; x < xlimit; x++) {
				if (x <= xcut) {
					// Create a new fault instance object in every case here
					new1.addContents(new1_index++, new FaultInstance(cx.getVariable(x)));
				} else {
					new2.addContents(new2_index++, new FaultInstance(cx.getVariable(x)));
				}
			}

			for (int y = 0; y < ylimit; y++) {
				if (y <= ycut) {
					new2.addContents(new2_index++, new FaultInstance(cy.getVariable(y)));
				} else {
					new1.addContents(new1_index++, new FaultInstance(cy.getVariable(y)));
				}
			}

			output.add(new1);
			output.add(new2);
			System.out.println("crossover output new1 = " + new1.toString());
			System.out.println("crossover output new2 = " + new2.toString());
		} else {
			System.out.println("Not performing crossover - one chromosome is empty");
		}
		return output;
	}

	public List<FaultInstanceSetSolution> execute(List<FaultInstanceSetSolution> solutions) {
		if (null == solutions) {
			throw new JMetalException("Null parameter");
		} else if (solutions.size() != 2) {
			throw new JMetalException("There must be two parents instead of " + solutions.size());
		}
		return doCrossover(solutions.get(0), solutions.get(1));
	}

	public double getCrossoverProbability() {
		return crossoverProbability;
	}

	public int getNumberOfRequiredParents() {
		return 2;
	}

	public int getNumberOfGeneratedChildren() {
		return 2;
	}

}
