package org.cloudbus.cloudsim.power.models;

import org.cloudbus.cloudsim.power.Resources;

public class PowerModelLinearIncludingNetwork implements PowerModel {

    private double maxCpuPower;
    private double maxNetworkPower;
    private double cpuConstant;
    private double networkConstant;
    private double staticPower;


    public PowerModelLinearIncludingNetwork(double maxCpuPower, double maxNetworkPower, double staticPower) {
        this.maxCpuPower = maxCpuPower;
        this.maxNetworkPower = maxNetworkPower;
        this.staticPower = staticPower;
        this.cpuConstant = (maxCpuPower - staticPower) / 100;
        this.networkConstant = (maxNetworkPower - staticPower) / 100;
    }

    @Override
    public double getPower(Resources environment) throws IllegalArgumentException {
        return getCpuPower(environment) + getNetworkPower(environment);
    }

    private double getNetworkPower(Resources resources) {
        double networkUtil = resources.getBwUsage();
        if (networkUtil < 0 || networkUtil > 1) {
            throw new IllegalArgumentException("Utilization value must be between 0 and 1");
        }

        return getStaticPower() + getNetworkConstant() * networkUtil * 100;
    }

    private double getCpuPower(Resources resources) {
        double cpuUtil = resources.getCpuUsage();
        if (cpuUtil < 0 || cpuUtil > 1) {
            throw new IllegalArgumentException("Utilization value must be between 0 and 1");
        }

        return getStaticPower() + getCpuConstant() * cpuUtil * 100;
    }

    public double getMaxCpuPower() {
        return maxCpuPower;
    }

    public void setMaxCpuPower(double maxCpuPower) {
        this.maxCpuPower = maxCpuPower;
    }

    public double getMaxNetworkPower() {
        return maxNetworkPower;
    }

    public void setMaxNetworkPower(double maxNetworkPower) {
        this.maxNetworkPower = maxNetworkPower;
    }

    public double getCpuConstant() {
        return cpuConstant;
    }

    public void setCpuConstant(double cpuConstant) {
        this.cpuConstant = cpuConstant;
    }

    public double getNetworkConstant() {
        return networkConstant;
    }

    public void setNetworkConstant(double networkConstant) {
        this.networkConstant = networkConstant;
    }

    public double getStaticPower() {
        return staticPower;
    }

    public void setStaticPower(double staticPower) {
        this.staticPower = staticPower;
    }
}
