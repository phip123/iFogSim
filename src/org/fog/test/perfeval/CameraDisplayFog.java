package org.fog.test.perfeval;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.*;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class CameraDisplayFog {
    static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
    static List<Sensor> sensors = new ArrayList<Sensor>();
    static List<Actuator> actuators = new ArrayList<Actuator>();

    static boolean CLOUD = false;

    static int numOfDepts = 1;
    static int numOfMobilesPerDept = 4;
    static double IMAGE_TRANSMISSION_TIME = 5.1;
    //static double IMAGE_TRANSMISSION_TIME = 10;

    public static void main(String[] args) {

        Log.printLine("Starting CameraDisplayFog...");

        try {
            Log.disable();
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events

            CloudSim.init(num_user, calendar, trace_flag);

            String appId = "cam_display"; // identifier of the application

            FogBroker broker = new FogBroker("broker");

            Application application = createApplication(appId, broker.getId());
            application.setUserId(broker.getId());

            createFogDevices(broker.getId(), appId);

            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // initializing a module mapping

            if (CLOUD) {
                moduleMapping.addModuleToDevice("classifier", "cloud"); // fixing all instances of the classifier module to the Cloud
                for (FogDevice device : fogDevices) {
                    if (device.getName().startsWith("m")) {
                        moduleMapping.addModuleToDevice("client", device.getName());  // fixing all instances of the Client module to the Smartphones
                    }
                }
            } else {
                moduleMapping.addModuleToDevice("classifier", "d-0"); // fixing all instances of classifier to edge computer
            }

            for (FogDevice device : fogDevices) {
                if (device.getName().startsWith("m")) {
                    moduleMapping.addModuleToDevice("client", device.getName());  // fixing all instances of the Client module to the Smartphones
                }
            }


            Controller controller = new Controller("master-controller", fogDevices, sensors,
                    actuators);

            controller.submitApplication(application, 0,
                    (CLOUD) ? (new ModulePlacementMapping(fogDevices, application, moduleMapping))
                            : (new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, moduleMapping)));

            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

            CloudSim.startSimulation();

            CloudSim.stopSimulation();

            Log.printLine("VRGame finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }

    /**
     * Creates the fog devices in the physical topology of the simulation.
     *
     * @param userId
     * @param appId
     */
    private static void createFogDevices(int userId, String appId) {
        FogDevice cloud = createFogDevice("cloud", 1000, 40000, 100, 10000, 0, 0.01, 16 * 103, 16 * 83.25); // creates the fog device Cloud at the apex of the hierarchy with level=0
        cloud.setParentId(-1);
        FogDevice proxy = createFogDevice("proxy-server", 1000, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333); // creates the fog device Proxy Server (level=1)
        proxy.setParentId(cloud.getId()); // setting Cloud as parent of the Proxy Server
        proxy.setUplinkLatency(100); // latency of connection from Proxy Server to the Cloud is 100 ms

        fogDevices.add(cloud);
        fogDevices.add(proxy);

        for (int i = 0; i < numOfDepts; i++) {
            addGw(i + "", userId, appId, proxy.getId()); // adding a fog device for every Gateway in physical topology. The parent of each gateway is the Proxy Server
        }

    }

    private static FogDevice addGw(String id, int userId, String appId, int parentId) {
        FogDevice dept = createEdgeComputer(id);
        fogDevices.add(dept);
        dept.setParentId(parentId);
        dept.setUplinkLatency(4); // latency of connection between gateways and proxy server is 4 ms
        for (int i = 0; i < numOfMobilesPerDept; i++) {
            String mobileId = id + "-" + i;
            FogDevice mobile = addMobile(mobileId, userId, appId, dept.getId()); // adding mobiles to the physical topology. Smartphones have been modeled as fog devices as well.
            mobile.setUplinkLatency(2); // latency of connection between the smartphone and proxy server is 4 ms
            fogDevices.add(mobile);
        }
        return dept;
    }

    private static FogDevice createEdgeComputer(String id) {
        return createFogDevice("d-" + id, 7314, 4000, 10000, 10000, 1, 0.0, 80, 8);
    }

    private static FogDevice addMobile(String id, int userId, String appId, int parentId) {
        FogDevice mobile = createMobileDevice(id);
        mobile.setParentId(parentId);
        Sensor cameraSensor = new Sensor("s-" + id, "CAMERA", userId, appId, new DeterministicDistribution(IMAGE_TRANSMISSION_TIME)); // inter-transmission time of EEG sensor follows a deterministic distribution
//        Sensor cameraSensor = new Sensor("s-" + id, "CAMERA", userId, appId, new DeterministicDistribution(IMAGE_TRANSMISSION_TIME)); // inter-transmission time of EEG sensor follows a deterministic distribution
        sensors.add(cameraSensor);
        Actuator display = new Actuator("a-" + id, userId, appId, "DISPLAY");
        actuators.add(display);
        cameraSensor.setGatewayDeviceId(mobile.getId());
        cameraSensor.setLatency(6.0);  // latency of connection between EEG sensors and the parent Smartphone is 6 ms
        display.setGatewayDeviceId(mobile.getId());
        display.setLatency(1.0);  // latency of connection between Display actuator and the parent Smartphone is 1 ms
        return mobile;
    }

    private static FogDevice createMobileDevice(String id) {
        return createFogDevice("m-" + id, 10000, 4096, 10000, 270, 3, 0, 10, 4);
    }

    /**
     * Creates a vanilla fog device
     *
     * @param nodeName    name of the device to be used in simulation
     * @param mips        MIPS
     * @param ram         RAM
     * @param upBw        uplink bandwidth
     * @param downBw      downlink bandwidth
     * @param level       hierarchy level of the device
     * @param ratePerMips cost rate per MIPS used
     * @param busyPower
     * @param idlePower
     * @return
     */
    private static FogDevice createFogDevice(String nodeName, long mips,
                                             int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower) {

        List<Pe> peList = new ArrayList<Pe>();

        // 3. Create PEs and add these into a list.
        peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

        int hostId = FogUtils.generateEntityId();
        long storage = 1000000; // host storage
        int bw = 10000;

        PowerHost host = new PowerHost(
                hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerOverbooking(bw),
                storage,
                peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(busyPower, idlePower)
        );

        List<Host> hostList = new ArrayList<Host>();
        hostList.add(host);

        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 10.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this
        // resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
        // devices by now

        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                arch, os, vmm, host, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        FogDevice fogdevice = null;
        try {
            fogdevice = new FogDevice(nodeName, characteristics,
                    new AppModuleAllocationPolicy(hostList), storageList, 10, upBw, downBw, 0, ratePerMips);
        } catch (Exception e) {
            e.printStackTrace();
        }

        fogdevice.setLevel(level);
        return fogdevice;
    }

    /**
     * Function to create the EEG Tractor Beam game application in the DDF model.
     *
     * @param appId  unique identifier of the application
     * @param userId identifier of the user of the application
     * @return
     */
    @SuppressWarnings({"serial"})
    private static Application createApplication(String appId, int userId) {

        Application application = Application.createApplication(appId, userId);
        application.addAppModule("client", 10);
        application.addAppModule("classifier", 10);

        application.addTupleMapping("client", "CAMERA", "IMAGE_REQUEST", new FractionalSelectivity(1.0));
        application.addTupleMapping("client", "CLASSIFICATION", "SHOW_RESULT", new FractionalSelectivity(1.0));
        application.addTupleMapping("classifier", "IMAGE_REQUEST", "CLASSIFICATION", new FractionalSelectivity(1.0));

        application.addAppEdge("CAMERA", "client", 100, 100, "CAMERA", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("client", "classifier", 5470, 1000, "IMAGE_REQUEST", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge("classifier", "client", 100, 100, "CLASSIFICATION", Tuple.DOWN, AppEdge.MODULE);
        application.addAppEdge("client", "DISPLAY", 10, 100, "SHOW_RESULT", Tuple.DOWN, AppEdge.ACTUATOR);


        final AppLoop loop1 = new AppLoop(new ArrayList<String>() {{
            add("CAMERA");
            add("client");
            add("classifier");
            add("client");
            add("DISPLAY");
        }});
        List<AppLoop> loops = new ArrayList<AppLoop>() {{
            add(loop1);
        }};

        application.setLoops(loops);

        //GeoCoverage geoCoverage = new GeoCoverage(-100, 100, -100, 100);
        return application;
    }
}