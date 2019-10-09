package org.fog.test.perfeval;

public class FogConfig {
    private String id;
    private int mips;
    private int ram;
    private int upBw;
    private int downBw;
    private int level;
    private int raterPerMips;
    private double busyPower;
    private double idlePower;

    public FogConfig(String id, int mips, int ram, int upBw, int downBw, int level, int raterPerMips, double busyPower, double idlePower) {
        this.id = id;
        this.mips = mips;
        this.ram = ram;
        this.upBw = upBw;
        this.downBw = downBw;
        this.level = level;
        this.raterPerMips = raterPerMips;
        this.busyPower = busyPower;
        this.idlePower = idlePower;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMips() {
        return mips;
    }

    public int getRam() {
        return ram;
    }

    public int getUpBw() {
        return upBw;
    }

    public int getDownBw() {
        return downBw;
    }

    public int getLevel() {
        return level;
    }

    public int getRaterPerMips() {
        return raterPerMips;
    }

    public double getBusyPower() {
        return busyPower;
    }

    public double getIdlePower() {
        return idlePower;
    }

    public void setMips(int mips) {
        this.mips = mips;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public void setUpBw(int upBw) {
        this.upBw = upBw;
    }

    public void setDownBw(int downBw) {
        this.downBw = downBw;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setRaterPerMips(int raterPerMips) {
        this.raterPerMips = raterPerMips;
    }

    public void setBusyPower(double busyPower) {
        this.busyPower = busyPower;
    }

    public void setIdlePower(double idlePower) {
        this.idlePower = idlePower;
    }


    public static final class FogConfigBuilder {
        private String id;
        private int mips;
        private int ram;
        private int upBw;
        private int downBw;
        private int level;
        private int raterPerMips;
        private double busyPower;
        private double idlePower;

        private FogConfigBuilder() {
        }

        public static FogConfigBuilder aFogConfig() {
            return new FogConfigBuilder();
        }

        public FogConfigBuilder id(String id) {
            this.id = id;
            return this;
        }

        public FogConfigBuilder mips(int mips) {
            this.mips = mips;
            return this;
        }

        public FogConfigBuilder ram(int ram) {
            this.ram = ram;
            return this;
        }

        public FogConfigBuilder upBw(int upBw) {
            this.upBw = upBw;
            return this;
        }

        public FogConfigBuilder downBw(int downBw) {
            this.downBw = downBw;
            return this;
        }

        public FogConfigBuilder level(int level) {
            this.level = level;
            return this;
        }

        public FogConfigBuilder raterPerMips(int raterPerMips) {
            this.raterPerMips = raterPerMips;
            return this;
        }

        public FogConfigBuilder busyPower(double busyPower) {
            this.busyPower = busyPower;
            return this;
        }

        public FogConfigBuilder idlePower(double idlePower) {
            this.idlePower = idlePower;
            return this;
        }

        public FogConfig build() {
            return new FogConfig(id, mips, ram, upBw, downBw, level, raterPerMips, busyPower, idlePower);
        }
    }

    public static FogConfig serviceConfig(String id) {
        return FogConfig.FogConfigBuilder.aFogConfig()
                .id(id)
                .mips(30000)
                .ram(16000)
                .upBw(100)
                .downBw(1000)
                .level(1)
                .raterPerMips(0)
                .busyPower(20)
                .idlePower(8)
                .build();
    }

    public static FogConfig piConfig(String id) {
        return FogConfig.FogConfigBuilder.aFogConfig()
                .id(id)
                .mips(1000)
                .ram(1000)
                .upBw(100)
                .downBw(1000)
                .level(2)
                .raterPerMips(0)
                .busyPower(8)
                .idlePower(1)
                .build();
    }
}
