/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2011, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import org.cloudbus.cloudsim.power.Resources;

/**
 * The Class HostStateHistoryEntry.
 *
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.1.2
 */
public class HostStateHistoryEntry {

	/** The time. */
	private double time;

	/** The allocated resources */
	private Resources allocated;

	/** The requested resources */
	private Resources requested;

	/** The is active. */
	private boolean isActive;

	/**
	 * Instantiates a new vm mips allocation history entry.
	 *
	 * @param time the time
	 * @param allocated the allocated resources
	 * @param requested the requested resources
	 * @param isActive the is active
	 */
	public HostStateHistoryEntry(double time, Resources allocated, Resources requested, boolean isActive) {
		setTime(time);
		setAllocated(allocated);
		setRequested(requested);
		setActive(isActive);
	}

	/**
	 * Sets the time.
	 *
	 * @param time the new time
	 */
	protected void setTime(double time) {
		this.time = time;
	}

	/**
	 * Gets the time.
	 *
	 * @return the time
	 */
	public double getTime() {
		return time;
	}


	public Resources getAllocated() {
		return allocated;
	}

	public void setAllocated(Resources allocated) {
		this.allocated = allocated;
	}

	public Resources getRequested() {
		return requested;
	}

	public void setRequested(Resources requested) {
		this.requested = requested;
	}

	/**
	 * Sets the active.
	 *
	 * @param isActive the new active
	 */
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	/**
	 * Checks if is active.
	 *
	 * @return true, if is active
	 */
	public boolean isActive() {
		return isActive;
	}

}
