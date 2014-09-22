package meshi.optimizers;

import meshi.optimizers.exceptions.LineSearchException;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * This class provide a simple way to find the step length. It start with a certain length and check if
 * it brings to energy reduction. If it does not it is shortened by a reduction factor. This repeats until a
 * step size is found that produce energy reduction, or the step size is so small that an exception is thrown.
 * After the step size is returned, it is multiplied by an expansion factor, so that the next call to this
 * class evaluation is started with a longer guess. If the expension factor is set to 0, the step size initial
 * guess is always 1 (as should be tried for all quasi-newton techniques).
 *
 *How to use this class:
 *----------------------
 *a) Instantiate this class with the desired parameters.
 *b) In a position Xk: evaluate the energy gradients and coordinates to position Xk.
 *c) Run 'findStepLength(Vec[n][2])' where the first column in Vec is the position Xk, and the second
 *column is the direction Pk. This method returns the found step length. This method also changes the coordinates
 *in class 'energy' to the coordinates in the new (minimized) position: Xk+1 = Xk + (step_length)*Pk. Also the
 *gradients in the energy class are updated.
 *d) Check for thrown exceptions to make sure that the step length is correct.
 **/

public class SimpleStepLength extends LineSearch {

	private static final double TOO_SMALL = 1e-6;

	private double stepSize, stepSizeReduction, stepSizeExpansion;
	private double energyOld, energyNew;

	public SimpleStepLength(Energy energy, double stepSize1, double stepSizeReduction, double stepSizeExpansion) {
		super(energy);
		this.stepSizeExpansion = stepSizeExpansion;
		this.stepSizeReduction = stepSizeReduction;

		if (stepSizeExpansion > 0) stepSize = stepSize1 / stepSizeExpansion;
		else stepSize = 1;

		if (stepSizeReduction <= 0) throw new RuntimeException("\n\nIrrecoverable error in the line search method: SimpleStepLength.\n" + "The step size reduction parameter in the constructor is non-positive.\n");
	}

	/**
	 * Line search: Find step length
	 */
	@Override
	public double findStepLength() throws LineSearchException {
		System.err.println("\n");
		if (stepSize < TOO_SMALL) stepSize = 1.0 / stepSizeReduction;
		else stepSize *= stepSizeExpansion / stepSizeReduction;

		energy.setThetaBest();
		energyOld = energy.updateEnergy();
		energyNew = energyOld;

		// If no energy reduction is achieved for a specific step size it is reduced
		// until a step that produce reduction in energy is found.
		while (energyNew >= energyOld) {
			stepSize *= stepSizeReduction;

			if (stepSize < TOO_SMALL) return stepSize; // throw new LineSearchException(LineSearchException.NOT_A_DESCENT_DIRECTION, "\n\nThe search direction is apparently not a descent direction. \n" + "This problem might be caused by incorrect diffrentiation " + "of the energy function,\n" + "or by numerical instabilities of the minimizing techniques " + "(such as not fullfilling the Wolf condtions in BFGS).\n\tStep Size: " + stepSize);

			energy.addThetaBestGradient(-stepSize);
			energyNew = energy.updateEnergy(); // The energy at the new coordinates.

			if (debug) Gpr.debug("" //
					+ "\n\tStep size : " + stepSize //
					+ "\n\tenergy old: " + energyOld //
					+ "\n\tenerfy new: " + energyNew //
					+ "\n\tmodel     : " + Gpr.toString(energy.getThetaBest()) //
					+ "\n\tmodel     : " + Gpr.toString(energy.getTheta()) //
					+ "\n\tgradient  : " + Gpr.toString(energy.getGradient()) //
			);
		}

		return stepSize;
	}
}
