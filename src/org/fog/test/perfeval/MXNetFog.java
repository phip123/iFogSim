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
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Simulation setup for case study 1 - EEG Beam Tractor Game
 *
 * @author Harshit Gupta
 */
public class MXNetFog {
    static List<FogDevice> fogDevices = new ArrayList<>();
    static List<Sensor> sensors = new ArrayList<>();
    static List<Actuator> actuators = new ArrayList<>();


    static int numOfServices = 1;
    static int numOfPisPerService = 4;

    public static void main(String[] args) {

        Log.printLine("Starting MXNet...");

        try {
            Log.disable();
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events

            CloudSim.init(num_user, calendar, trace_flag);

            String appId = "mx_net"; // identifier of the application

            FogBroker broker = new FogBroker("broker");

            Application application = createApplication(appId, broker.getId());
            application.setUserId(broker.getId());

            createFogDevices(broker.getId(), appId);

            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // initializing a module mapping

            final String client = "client";
            final String mxnetService = "mxnet_service";

            for (FogDevice device: fogDevices) {
                if (device.getName().startsWith("pi")) {
                    moduleMapping.addModuleToDevice(client, device.getName());
                }
            }

            for (FogDevice device: fogDevices) {
                if (device.getName().startsWith("s")) {
                    moduleMapping.addModuleToDevice(mxnetService, device.getName());
                }
            }

            Controller controller = new Controller("master-controller", fogDevices, sensors,
                    actuators);

            controller.submitApplication(application, 0,
                    (new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, moduleMapping)));

            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

            CloudSim.startSimulation();

            CloudSim.stopSimulation();

            Log.printLine("MXNet finished!");
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
        // cloud is necessary, otherwise NPE is thrown
        FogDevice cloud = createFogDevice("cloud", 1000, 40000, 100, 10000, 0, 0.01, 16*103, 16*83.25); // creates the fog device Cloud at the apex of the hierarchy with level=0
        cloud.setParentId(-1);
        FogDevice proxy = createFogDevice("proxy-server", 1000, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333); // creates the fog device Proxy Server (level=1)
        proxy.setParentId(cloud.getId()); // setting Cloud as parent of the Proxy Server
        proxy.setUplinkLatency(100); // latency of connection from Proxy Server to the Cloud is 100 ms

        fogDevices.add(cloud);
        fogDevices.add(proxy);


        for (int i = 0; i < numOfServices; i++) {
            addService(i + "", userId, appId, proxy.getId()); // adding a fog device for every Gateway in physical topology. The parent of each gateway is the Proxy Server
        }

    }

    private static FogDevice addService(String id, int userId, String appId, int proxyId) {
        FogDevice service = createFogDevice("s-" + id, FogConfig.serviceConfig(id));
        fogDevices.add(service);
        service.setParentId(proxyId);
        service.setUplinkLatency(4);
        for (int i = 0; i < numOfPisPerService; i++) {
            String piId = id + "-" + i;
            FogDevice pi = addPi(piId, userId, appId, service.getId()); //adding pis to the physical topology
            fogDevices.add(pi);
        }
        return service;
    }

    private static FogDevice addPi(String id, int userId, String appId, int parentId) {
        FogDevice pi = createFogDevice("pi-" + id, FogConfig.piConfig(id));
        pi.setParentId(parentId);
        return pi;
    }



    private static FogDevice createFogDevice(String nodeName, FogConfig config) {
        return createFogDevice(nodeName, config.getMips(),config.getRam(),config.getUpBw(),config.getDownBw(),config.getLevel(),
                config.getRaterPerMips(),config.getBusyPower(),config.getIdlePower());
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
     * Function to create the MXNet application in the DDF model.
     *
     * @param appId  unique identifier of the application
     * @param userId identifier of the user of the application
     * @return
     */
    @SuppressWarnings({"serial"})
    private static Application createApplication(String appId, int userId) {

        Application application = Application.createApplication(appId, userId); // creates an empty application model (empty directed graph)

        /*
         * Adding modules (vertices) to the application model (directed graph)
         */
        final String client = "client";
        final String mxnetService = "mxnet_service";
        final String imageTuple = "IMAGE";
        final String resultTuple = "RESULT";
        application.addAppModule(client, 1); // adding module Client to the application model
        application.addAppModule(mxnetService, 4); // adding module MXNet Service to the application model

        /*
         * Connecting the application modules (vertices) in the application model (directed graph) with edges
         */
        application.addAppEdge(client, mxnetService,800,35000, 5000, imageTuple, Tuple.UP, AppEdge.MODULE); // adding periodic edge (period=800ms) from Client to MXNet service module carrying tuples of type IMAGE
        application.addAppEdge(mxnetService, client, 14, 5000, resultTuple, Tuple.DOWN, AppEdge.MODULE);  // adding edge from Concentration Calculator to Client module carrying tuples of type CONCENTRATION

        /*
         * Defining the input-output relationships (represented by selectivity) of the application modules.
         */
        application.addTupleMapping(mxnetService, imageTuple, resultTuple, new FractionalSelectivity(1.0)); // 1.0 tuples of type RESULT are emitted by mxnet module per incoming tuple of type IMAGE

        /*
         * Defining application loops to monitor the latency of.
         * Here, we add only one loop for monitoring : Client -> Mxnet Service -> Client
         */
        final AppLoop loop1 = new AppLoop(new ArrayList<String>() {{
            add(client);
            add(mxnetService);
            add(client);
        }});
        List<AppLoop> loops = new ArrayList<AppLoop>() {{
            add(loop1);
        }};
        application.setLoops(loops);

        return application;
    }


}
