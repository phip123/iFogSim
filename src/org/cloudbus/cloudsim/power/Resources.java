package org.cloudbus.cloudsim.power;

public class Resources {
    private double mips;
    private double maxMips;
    private double bw;
    private double maxBw;

    public static Resources empty() {
        return ResourcesBuilder.aResources()
                .bw(0)
                .mips(0)
                .maxBw(1)
                .maxMips(1)
                .build();
    }

    public double getCpuUsage() {
        if (getMaxMips() == 0) {
            return 0;
        }
        double utilization = (getMips() / getMaxMips());
        if (utilization > 1 && utilization < 1.01) {
            utilization = 1;
        }

        return Math.min(1, utilization);
    }

    public double getBwUsage() {
        double utilization = (getBw() / getMaxBw());
        if (utilization > 1 && utilization < 1.01) {
            utilization = 1;
        }

        return Math.min(1, utilization);
    }

    public Resources(double mips, double maxMips, double bw, double maxBw) {
        this.mips = mips;
        this.maxMips = maxMips;
        this.bw = bw;
        this.maxBw = maxBw;
    }

    public double getMips() {
        return mips;
    }

    public void setMips(double mips) {
        this.mips = mips;
    }

    public double getMaxMips() {
        return maxMips;
    }

    public void setMaxMips(double maxMips) {
        this.maxMips = maxMips;
    }

    public double getBw() {
        return bw;
    }

    public void setBw(double bw) {
        this.bw = bw;
    }

    public double getMaxBw() {
        return maxBw;
    }

    public void setMaxBw(double maxBw) {
        this.maxBw = maxBw;
    }

    @Override
    public String toString() {
        return "Resources{" +
                "mips=" + mips +
                ", maxMips=" + maxMips +
                ", bw=" + bw +
                ", maxBw=" + maxBw +
                '}';
    }

    public static ResourcesBuilder aResourcesBuilder() {
        return new ResourcesBuilder();
    }

    public static final class ResourcesBuilder {
        private double mips;
        private double maxMips;
        private double bw;
        private double maxBw;

        private ResourcesBuilder() {
        }

        public static ResourcesBuilder aResources() {
            return new ResourcesBuilder();
        }

        public ResourcesBuilder mips(double mips) {
            this.mips = mips;
            return this;
        }

        public ResourcesBuilder maxMips(double maxMips) {
            this.maxMips = maxMips;
            return this;
        }

        public ResourcesBuilder bw(double bw) {
            this.bw = bw;
            return this;
        }

        public ResourcesBuilder maxBw(double maxBw) {
            this.maxBw = maxBw;
            return this;
        }

        public Resources build() {
            return new Resources(mips, maxMips, bw, maxBw);
        }
    }
}
