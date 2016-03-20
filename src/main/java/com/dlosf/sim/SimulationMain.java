package com.dlosf.sim;

import alphabetsoup.base.SummaryReport;
import alphabetsoup.framework.SimulationWorld;
import alphabetsoup.userinterface.RenderWindow;
import com.dlosf.sim.graph.SimulationWorldGraph;
import com.dlosf.sim.greedy.SimulationWorldGreedy;
import com.dlosf.sim.simple.SimulationWorldSimple;
import com.dlosf.sim.util.SimulationWorldInitializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.concurrent.Executors;

/**
 * Created by eshen on 10/15/15.
 */
public class SimulationMain {

    public static final String PROMPT = ">> ";
    public static final String EOL = "#EOL#";
    public static final String DEFAULT_INITDATA_DIR = "initdata";
    private static SimulationWorldGraph simGraph;
    public static SimulationWorldGraph getSimulationWorld() {
        return simGraph;
    }


    public static void main(String arg[])
    {

        try {

            System.out.println("OFSG Simulator Command Line Tool");
            System.out.println("Enter the the following command at the prompt: init, runsimple, rungraph, or rungreedy");

            BufferedReader br =
                    new BufferedReader(new InputStreamReader(System.in));

            String command = "";


            while (!command.equals("exit")) {
                System.out.print(PROMPT);


                command = br.readLine().trim();


                if (command.equals("exit")) {
                    break;
                }

                handleCmd(command);


            }

            System.out.println("Simulation ended.");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private static void handleCmd(String cmd) {

        String[] cmdStr = cmd.split("\\s+");
        if (cmdStr.length > 0) {

            if (cmdStr[0].equalsIgnoreCase("init")) {
                System.out.println(String.format("Create init data set in %s directory. ", DEFAULT_INITDATA_DIR));
                System.setProperty("initdata", "true");
                SimulationWorldInitializer.generateInitData(SimulationWorldInitializer.SimWorldType.GRAPH, DEFAULT_INITDATA_DIR);

                System.setProperty("initdata", "");
                SimulationWorldGraph swGraph = (SimulationWorldGraph)SimulationWorldInitializer.loadGraphFromInitData(DEFAULT_INITDATA_DIR, SimulationWorldInitializer.SimWorldType.GRAPH);
                SimulationWorld.simulationWorld = swGraph;

                RenderWindow.renderInitData(swGraph, swGraph.getSimulationDuration());
                RenderWindow.destroyUserInterface();
                RenderWindow.resetRenderWindow();
                SimulationWorld.simulationWorld = null;
                swGraph = null;

                System.out.println("done");
                return;
            }

            if (cmdStr[0].equalsIgnoreCase("runsimple")) {

                System.out.println("Running simulation using init data set from initdata directory");

                SimulationWorldSimple swSimple = SimulationWorldInitializer.loadFromInitData(DEFAULT_INITDATA_DIR);
                SimulationWorld.simulationWorld = swSimple;

                RenderWindow.renderInitData(swSimple, swSimple.getSimulationDuration());
                SummaryReport.generateReport(swSimple);
                RenderWindow.destroyUserInterface();
                RenderWindow.resetRenderWindow();
                SimulationWorld.simulationWorld = null;
                swSimple = null;
                System.out.println("done");
                return;
            }

            if (cmdStr[0].equalsIgnoreCase("rungraph")) {

                System.out.println("Running simulation using init data set from initdata directory");

                SimulationWorldGraph swGraph = (SimulationWorldGraph)SimulationWorldInitializer.loadGraphFromInitData(DEFAULT_INITDATA_DIR, SimulationWorldInitializer.SimWorldType.GRAPH);
                SimulationWorld.simulationWorld = swGraph;

                RenderWindow.renderInitData(swGraph, swGraph.getSimulationDuration());
                SummaryReport.generateReport(swGraph);
                RenderWindow.destroyUserInterface();
                SimulationWorld.simulationWorld = null;
                RenderWindow.resetRenderWindow();
                swGraph = null;
                System.out.println("done");
                return;
            }
            if (cmdStr[0].equalsIgnoreCase("rungreedy")) {

                System.out.println("Running simulation using init data set from initdata directory");

                SimulationWorldGreedy swGreedy = (SimulationWorldGreedy)SimulationWorldInitializer.loadGraphFromInitData(DEFAULT_INITDATA_DIR, SimulationWorldInitializer.SimWorldType.GREEDY);
                SimulationWorld.simulationWorld = swGreedy;

                //RenderWindow.mainLoop(swGreedy, swGreedy.getSimulationWarmupTime());
                //swGreedy.resetStatistics();
                //RenderWindow.mainLoop(swGreedy, swGreedy.getSimulationDuration());
                RenderWindow.renderInitData(swGreedy, swGreedy.getSimulationDuration());
                SummaryReport.generateReport(swGreedy);
                RenderWindow.destroyUserInterface();
                SimulationWorld.simulationWorld = null;
                RenderWindow.resetRenderWindow();
                swGreedy = null;
                System.out.println("done");
                return;
            }

            System.out.println("invalid command. Valid commands are init and runsim");
        }

    }





}
