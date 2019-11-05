package org.cloudbus.cloudsim.power.models;

public class Utilization {

    private double cpuUsage;
    private double diskUsage;
    private double bandwithUsage;

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public double getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(double diskUsage) {
        this.diskUsage = diskUsage;
    }

    public double getBandwithUsage() {
        return bandwithUsage;
    }

    public void setBandwithUsage(double bandwithUsage) {
        this.bandwithUsage = bandwithUsage;
    }



    public static UtilizationBuilder anUtilizationBuilder() {
        return new UtilizationBuilder();
    }

    public static final class UtilizationBuilder {
        private double cpuUsage;
        private double diskUsage;
        private double bandwithUsage;

        private UtilizationBuilder() {
        }

        public static UtilizationBuilder anUtilization() {
            return new UtilizationBuilder();
        }

        public UtilizationBuilder cpuUsage(double cpuUsage) {
            this.cpuUsage = cpuUsage;
            return this;
        }

        public UtilizationBuilder diskUsage(double diskUsage) {
            this.diskUsage = diskUsage;
            return this;
        }

        public UtilizationBuilder bandwithUsage(double bandwithUsage) {
            this.bandwithUsage = bandwithUsage;
            return this;
        }

        public Utilization build() {
            Utilization utilization = new Utilization();
            utilization.setCpuUsage(cpuUsage);
            utilization.setDiskUsage(diskUsage);
            utilization.setBandwithUsage(bandwithUsage);
            return utilization;
        }
    }
}
