package org.fog.utils;

import org.cloudbus.cloudsim.power.Resources;
import org.cloudbus.cloudsim.power.models.PowerModel;


public class FogLinearPowerModelUsingUtil implements PowerModel {

	/** The max power. */
	private double maxPower;

	/** The constant. */
	private double constant;

	/** The static power. */
	private double staticPower;

	/**
	 * Instantiates a new linear power model.
	 *
	 * @param maxPower the max power
	 * @param staticPower the static power
	 */
	public FogLinearPowerModelUsingUtil(double maxPower, double staticPower) {
		setMaxPower(maxPower);
		setStaticPower(staticPower);
		setConstant((maxPower - getStaticPower()) / 100);
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.power.PowerModel#getPower(double)
	 */
	@Override
	public double getPower(Resources utilization) throws IllegalArgumentException {
		double cpuUtil = utilization.getCpuUsage();
		if (cpuUtil < 0 || cpuUtil > 1) {
			throw new IllegalArgumentException("Utilization value must be between 0 and 1");
		}
		return getStaticPower() + getConstant() * cpuUtil * 100;
	}

	/**
	 * Gets the max power.
	 *
	 * @return the max power
	 */
	protected double getMaxPower() {
		return maxPower;
	}

	/**
	 * Sets the max power.
	 *
	 * @param maxPower the new max power
	 */
	protected void setMaxPower(double maxPower) {
		this.maxPower = maxPower;
	}

	/**
	 * Gets the constant.
	 *
	 * @return the constant
	 */
	protected double getConstant() {
		return constant;
	}

	/**
	 * Sets the constant.
	 *
	 * @param constant the new constant
	 */
	protected void setConstant(double constant) {
		this.constant = constant;
	}

	/**
	 * Gets the static power.
	 *
	 * @return the static power
	 */
	protected double getStaticPower() {
		return staticPower;
	}

	/**
	 * Sets the static power.
	 *
	 * @param staticPower the new static power
	 */
	protected void setStaticPower(double staticPower) {
		this.staticPower = staticPower;
	}

}
