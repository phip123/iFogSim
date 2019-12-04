package org.fog.utils;

import org.cloudbus.cloudsim.power.Resources;
import org.cloudbus.cloudsim.power.models.PowerModel;

public class FogLinearPowerModelOnlyNetwork implements PowerModel {

    private double maxNetworkPower;
    private double staticPower;
    private double networkConstant;

    public FogLinearPowerModelOnlyNetwork(double maxNetworkPower, double staticPower) {
        this.maxNetworkPower = maxNetworkPower;
        this.staticPower = staticPower;
        this.networkConstant = (maxNetworkPower - staticPower) / 100;
    }

    @Override
    public double getPower(Resources environment) throws IllegalArgumentException {
        return getNetworkPower(environment);
    }


    private double getNetworkPower(Resources resources) {
        double networkUtil = resources.getBwUsage();
        if (networkUtil < 0 || networkUtil > 1) {
            throw new IllegalArgumentException("Utilization value must be between 0 and 1");
        }

        return getStaticPower() + getNetworkConstant() * networkUtil * 100;
    }

    public double getMaxNetworkPower() {
        return maxNetworkPower;
    }

    public void setMaxNetworkPower(double maxNetworkPower) {
        this.maxNetworkPower = maxNetworkPower;
    }

    public double getStaticPower() {
        return staticPower;
    }

    public void setStaticPower(double staticPower) {
        this.staticPower = staticPower;
    }

    public double getNetworkConstant() {
        return networkConstant;
    }

    public void setNetworkConstant(double networkConstant) {
        this.networkConstant = networkConstant;
    }
}
