package org.cloudbus.cloudsim.power.models;

public class Utilization {

    private double cpuUsage;
    private double diskUsage;

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


    public static final class UtilizationBuilder {
        private double cpuUsage;
        private double diskUsage;

        private UtilizationBuilder() {
        }


        public UtilizationBuilder cpuUsage(double cpuUsage) {
            this.cpuUsage = cpuUsage;
            return this;
        }

        public UtilizationBuilder diskUsage(double diskUsage) {
            this.diskUsage = diskUsage;
            return this;
        }

        public Utilization build() {
            Utilization utilization = new Utilization();
            utilization.setCpuUsage(cpuUsage);
            utilization.setDiskUsage(diskUsage);
            return utilization;
        }
    }

    @Override
    public String toString() {
        return "Utilization{" +
                "cpuUsage=" + cpuUsage +
                ", diskUsage=" + diskUsage +
                '}';
    }

    public static UtilizationBuilder anUtilizationBuilder() {
        return new UtilizationBuilder();
    }
}
