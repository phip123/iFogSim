package org.fog.test;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.FogBroker;
import org.fog.entities.PhysicalTopology;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.utils.JsonToTopology;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Simulation setup for EEG Beam Tractor Game extracting physical topology
 *
 * @author Harshit Gupta
 */
public class CleanFromJsonSimpleTopSensors {

    public static void main(String[] args) {

        Log.printLine("Starting simple MXNet...");

        try {
            Log.disable();
            int num_user = 1; // number of cloud users
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // mean trace events

            CloudSim.init(num_user, calendar, trace_flag);

            String appId = "mx_net_simple";

            FogBroker broker = new FogBroker("broker");

            Application application = createApplication(appId, broker.getId());
            application.setUserId(broker.getId());

            /*
             * Creating the physical topology from specified JSON file
             */
            PhysicalTopology physicalTopology = JsonToTopology.getPhysicalTopology(broker.getId(), appId, "topologies/simple_top_sensors");

            Controller controller = new Controller("master-controller", physicalTopology.getFogDevices(), physicalTopology.getSensors(),
                    physicalTopology.getActuators());

            controller.submitApplication(application, 0, new ModulePlacementEdgewards(physicalTopology.getFogDevices(),
                    physicalTopology.getSensors(), physicalTopology.getActuators(),
                    application, ModuleMapping.createModuleMapping()));

            CloudSim.startSimulation();

            CloudSim.stopSimulation();

            Log.printLine("Simple MXNet finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
    }


    @SuppressWarnings({"serial"})
    private static Application createApplication(String appId, int userId) {
        final String client = "client";
        final String mxnetService = "cloud";
        final String imageTuple = "IMAGE";
        final String cameraSensor = "camera";
        final String displayActuator = "display";
        final String imageRequestTuple = "IMAGE_REQUEST";
        final String resultTuple = "RESULT";
        final String showResultTupel = "SHOW_RESULT";

        Application application = Application.createApplication(appId, userId);
        application.addAppModule(client, 10);
        application.addAppModule(mxnetService, 10);

        application.addTupleMapping(mxnetService, imageRequestTuple, resultTuple, new FractionalSelectivity(1.0));
        application.addTupleMapping(client, imageTuple, imageRequestTuple, new FractionalSelectivity(1.0));
        application.addTupleMapping(client, resultTuple, showResultTupel, new FractionalSelectivity(1.0));

        application.addAppEdge(cameraSensor, client, 500, 500, imageTuple, Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge(client, displayActuator, 100, 100, showResultTupel, Tuple.DOWN, AppEdge.ACTUATOR);
        application.addAppEdge(client, mxnetService, 200, 100, imageRequestTuple, Tuple.UP, AppEdge.MODULE); // adding periodic edge (period=800ms) from Client to MXNet service module carrying tuples of type IMAGE
        application.addAppEdge(mxnetService, client, 140, 200, resultTuple, Tuple.DOWN, AppEdge.MODULE);  // adding edge from Concentration Calculator to Client module carrying tuples of type CONCENTRATION


        final AppLoop loop1 = new AppLoop(new ArrayList<String>() {{
            add(cameraSensor);
            add(client);
            add(mxnetService);
            add(client);
            add(displayActuator);
        }});
        List<AppLoop> loops = new ArrayList<AppLoop>() {{
            add(loop1);
        }};

        application.setLoops(loops);

        return application;
    }
}