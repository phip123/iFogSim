/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package org.cloudbus.cloudsim;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.lists.PeList;
import org.cloudbus.cloudsim.power.Resources;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * The class of a host supporting dynamic workloads and performance degradation.
 *
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 2.0
 */
public class HostDynamicWorkload extends Host {

    /**
     * The utilization.
     */
    private Resources resourceAllocation;
    /**
     * The previous utilization.
     */
    private Resources previousAllocation;

    /**
     * The state history.
     */
    private final List<HostStateHistoryEntry> stateHistory = new LinkedList<HostStateHistoryEntry>();

    /**
     * Instantiates a new host.
     *
     * @param id             the id
     * @param ramProvisioner the ram provisioner
     * @param bwProvisioner  the bw provisioner
     * @param storage        the storage
     * @param peList         the pe list
     * @param vmScheduler    the VM scheduler
     */
    public HostDynamicWorkload(
            int id,
            RamProvisioner ramProvisioner,
            BwProvisioner bwProvisioner,
            long storage,
            List<? extends Pe> peList,
            VmScheduler vmScheduler) {
        super(id, ramProvisioner, bwProvisioner, storage, peList, vmScheduler);
        setResourceAllocation(Resources.empty());
        setPreviousAllocation(Resources.empty());
    }

    /*
     * (non-Javadoc)
     * @see cloudsim.Host#updateVmsProcessing(double)
     */
    @Override
    public double updateVmsProcessing(double currentTime) {
        double smallerTime = super.updateVmsProcessing(currentTime);
        setPreviousAllocation(getResourceAllocation());
        setResourceAllocation(Resources.empty());
        double hostTotalRequestedMips = 0;
        double hostTotalRequestedBw = 0;

        for (Vm vm : getVmList()) {
            getVmScheduler().deallocatePesForVm(vm);
        }

        for (Vm vm : getVmList()) {
            getVmScheduler().allocatePesForVm(vm, vm.getCurrentRequestedMips());
        }

        for (Vm vm : getVmList()) {
            double totalRequestedMips = vm.getCurrentRequestedTotalMips();
            double totalRequesteBw = vm.getCurrentRequestedBw();
            double totalAllocatedMips = getVmScheduler().getTotalAllocatedMipsForVm(vm);
            double totalAllocatedBw = getBwProvisioner().getAllocatedBwForVm(vm);
            if (!Log.isDisabled()) {
                Log.formatLine(
                        "%.2f: [Host #" + getId() + "] Total allocated MIPS for VM #" + vm.getId()
                                + " (Host #" + vm.getHost().getId()
                                + ") is %.2f, was requested %.2f out of total %.2f (%.2f%%)",
                        CloudSim.clock(),
                        totalAllocatedMips,
                        totalRequestedMips,
                        vm.getMips(),
                        totalRequestedMips / vm.getMips() * 100);

                List<Pe> pes = getVmScheduler().getPesAllocatedForVM(vm);
                StringBuilder pesString = new StringBuilder();
                for (Pe pe : pes) {
                    pesString.append(String.format(" PE #" + pe.getId() + ": %.2f.", pe.getPeProvisioner()
                            .getTotalAllocatedMipsForVm(vm)));
                }
                Log.formatLine(
                        "%.2f: [Host #" + getId() + "] MIPS for VM #" + vm.getId() + " by PEs ("
                                + getNumberOfPes() + " * " + getVmScheduler().getPeCapacity() + ")."
                                + pesString,
                        CloudSim.clock());
            }

            if (getVmsMigratingIn().contains(vm)) {
                Log.formatLine("%.2f: [Host #" + getId() + "] VM #" + vm.getId()
                        + " is being migrated to Host #" + getId(), CloudSim.clock());
            } else {
                if (totalAllocatedMips + 0.1 < totalRequestedMips) {
                    Log.formatLine("%.2f: [Host #" + getId() + "] Under allocated MIPS for VM #" + vm.getId()
                            + ": %.2f", CloudSim.clock(), totalRequestedMips - totalAllocatedMips);
                }

                vm.addStateHistoryEntry(
                        currentTime,
                        totalAllocatedMips,
                        totalRequestedMips,
                        (vm.isInMigration() && !getVmsMigratingIn().contains(vm)));

                if (vm.isInMigration()) {
                    Log.formatLine(
                            "%.2f: [Host #" + getId() + "] VM #" + vm.getId() + " is in migration",
                            CloudSim.clock());
                    totalAllocatedMips /= 0.9; // performance degradation due to migration - 10%
                }
            }

            Resources build = Resources.ResourcesBuilder.aResources()
                    .maxMips(getTotalMips())
                    .maxBw(getBwProvisioner().getAvailableBw() + getBwProvisioner().getUsedBw())
                    .mips(getUtilizationMips() + totalAllocatedMips)
                    .bw(getBwProvisioner().getUsedBw() + totalAllocatedBw)
                    .build();
            setResourceAllocation(build);
            hostTotalRequestedMips += totalRequestedMips;
            hostTotalRequestedBw += totalRequesteBw;
        }

        Resources requested = Resources.ResourcesBuilder.aResources()
                .maxMips(getTotalMips())
                .maxBw(getBwProvisioner().getAvailableBw() + getBwProvisioner().getUsedBw())
                .mips(hostTotalRequestedMips)
                .bw(hostTotalRequestedBw)
                .build();

        Resources allocated = Resources.ResourcesBuilder.aResources()
                .maxMips(getTotalMips())
                .maxBw(getBwProvisioner().getAvailableBw() + getBwProvisioner().getUsedBw())
                .mips(getUtilizationMips())
                .bw(getBwProvisioner().getUsedBw())
                .build();

        addStateHistoryEntry(
                currentTime,
                allocated,
                requested,
                (getUtilizationMips() > 0));

        return smallerTime;
    }

    /**
     * Gets the completed vms.
     *
     * @return the completed vms
     */
    public List<Vm> getCompletedVms() {
        List<Vm> vmsToRemove = new ArrayList<Vm>();
        for (Vm vm : getVmList()) {
            if (vm.isInMigration()) {
                continue;
            }
            if (vm.getCurrentRequestedTotalMips() == 0) {
                vmsToRemove.add(vm);
            }
        }
        return vmsToRemove;
    }

    /**
     * Gets the max utilization among by all PEs.
     *
     * @return the utilization
     */
    public double getMaxUtilization() {
        return PeList.getMaxUtilization(getPeList());
    }

    /**
     * Gets the max utilization among by all PEs allocated to the VM.
     *
     * @param vm the vm
     * @return the utilization
     */
    public double getMaxUtilizationAmongVmsPes(Vm vm) {
        return PeList.getMaxUtilizationAmongVmsPes(getPeList(), vm);
    }

    /**
     * Gets the utilization of memory.
     *
     * @return the utilization of memory
     */
    public double getUtilizationOfRam() {
        return getRamProvisioner().getUsedRam();
    }

    /**
     * Gets the utilization of bw.
     *
     * @return the utilization of bw
     */
    public double getUtilizationOfBw() {
//        return getBwProvisioner().getUsedBw();
        return resourceAllocation.getBwUsage();
    }

    /**
     * Get current utilization of CPU in percentage.
     *
     * @return current utilization of CPU in percents
     */
    public double getUtilizationOfCpu() {
        double utilization = getUtilizationMips() / getTotalMips();
        if (utilization > 1 && utilization < 1.01) {
            utilization = 1;
        }
        return utilization;
    }

    /**
     * Gets the previous utilization of CPU in percentage.
     *
     * @return the previous utilization of cpu
     */
    public double getPreviousUtilizationOfCpu() {
        return previousAllocation.getCpuUsage();
    }

    private double getPreviousUtilizationOfBw() {
        return previousAllocation.getBwUsage();
    }

    /**
     * Get current utilization of CPU in MIPS.
     *
     * @return current utilization of CPU in MIPS
     */
    public double getUtilizationOfCpuMips() {
        return getUtilizationMips();
    }

    /**
     * Gets the utilization mips.
     *
     * @return the utilization mips
     */
    public double getUtilizationMips() {
        return resourceAllocation.getMips();
    }


    /**
     * Gets the previous utilization mips.
     *
     * @return the previous utilization mips
     */
    public double getPreviousUtilizationMips() {
        return previousAllocation.getMips();
    }


    /**
     * Gets the state history.
     *
     * @return the state history
     */
    public List<HostStateHistoryEntry> getStateHistory() {
        return stateHistory;
    }

    /**
     * Adds the state history entry.
     *
     * @param time      the time
     * @param allocated the allocated resources
     * @param requested the requested resources
     * @param isActive  the is active
     */
    public void
    addStateHistoryEntry(double time, Resources allocated, Resources requested, boolean isActive) {

        HostStateHistoryEntry newState = new HostStateHistoryEntry(
                time,
                allocated,
                requested,
                isActive);
        if (!getStateHistory().isEmpty()) {
            HostStateHistoryEntry previousState = getStateHistory().get(getStateHistory().size() - 1);
            if (previousState.getTime() == time) {
                getStateHistory().set(getStateHistory().size() - 1, newState);
                return;
            }
        }
        getStateHistory().add(newState);
    }

    public double getDiskUtiliziation() {
        // TODO implement
        return 0;
    }

    public Resources getResourceAllocation() {
        return resourceAllocation;
    }

    public Resources getPreviousAllocation() {
        return previousAllocation;
    }

    public void setResourceAllocation(Resources resourceAllocation) {
        this.resourceAllocation = resourceAllocation;
    }

    public void setPreviousAllocation(Resources previousAllocation) {
        this.previousAllocation = previousAllocation;
    }
}
