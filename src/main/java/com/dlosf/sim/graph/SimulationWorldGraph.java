/**
 * 
 */
package com.dlosf.sim.graph;

import alphabetsoup.base.*;
import alphabetsoup.framework.*;
import alphabetsoup.userinterface.*;
import alphabetsoup.waypointgraph.BucketbotDriver;
import alphabetsoup.waypointgraph.GenerateWaypointGraph;
import alphabetsoup.waypointgraph.Waypoint;
import alphabetsoup.waypointgraph.WaypointGraph;
import com.dlosf.sim.util.SimulationWorldInitializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;


/**Example AlphabetSoup simulation file, which puts buckets in a grid, lays out bots randomly,
 * parameratizes everything based on "alphabetsoup.config", and starts everything running.
 *
 */
public class SimulationWorldGraph extends SimulationWorld {

	private double simulationDuration = 0.0;
	public LetterManager letterManager = null;
	public Updateable wordManager = null;
	public BotManager bucketbotManager = null;
	public WaypointGraph waypointGraph = null;

	private static SimulationWorldGraph simulationWorldGraph;
	public static SimulationWorldGraph getSimulationWorld() {
		return simulationWorldGraph;
	}


	public SimulationWorldGraph(Bucket[] buckets,
								Bucketbot[] robots,
								LetterStation[] letterStations,
								WordStation[] wordStations,
								WordList wordList,
								List<Waypoint> unusedBucketStorageLocations,
								HashMap<Bucket,Waypoint> usedBucketStorageLocations,
								WaypointGraph waypointGraph,
								Properties params)
	{
		super();
		simulationWorldGraph=this;

		float map_width = Float.parseFloat(params.getProperty("map_width"));
		float map_length = Float.parseFloat(params.getProperty("map_length"));
		float tolerance = Float.parseFloat(params.getProperty("tolerance"));
		float max_acceleration = Float.parseFloat( params.getProperty("max_acceleration"));
		float max_velocity = Float.parseFloat( params.getProperty("max_velocity"));
		map = new Map(map_width, map_length, tolerance, max_acceleration, max_velocity);
		this.waypointGraph = waypointGraph;
		usingGUI = (Integer.parseInt(params.getProperty("useGUI")) == 1);

		simulationDuration = Double.parseDouble(params.getProperty("simulation_duration"));

		//Set up base map to add things to
		String window_size[] = params.getProperty("window_size").split("x");
		if(usingGUI)
			RenderWindow.initializeUserInterface(Integer.parseInt(window_size[0]), Integer.parseInt(window_size[1]), this);


		BucketbotDriver.waypointGraph = waypointGraph;
		BucketbotDriver.map = map;

		super.bucketbots = robots;
		super.buckets = buckets;
		super.letterStations = letterStations;
		super.wordStations = wordStations;


		bucketbotManager = new BotManager(buckets);
		letterManager = new LetterManager();
		wordManager = new OrderManager();

		for(Bucketbot r : bucketbots)
			((BucketbotDriver)r).manager = bucketbotManager;


		bucketbotManager.unusedBucketStorageLocations = unusedBucketStorageLocations;
		bucketbotManager.usedBucketStorageLocations = usedBucketStorageLocations;

		//layout on the map
		for (LetterStation ls : letterStations) {
			map.addLetterStation(ls);
		}

		for (WordStation ws : wordStations) {
			map.addWordStation(ws);
		}

		for(Bucket b : super.buckets) {
			map.addBucket(b);
		}
		for(Bucketbot r: super.bucketbots) {
			map.addRobot(r);
		}



		super.wordList = wordList;
		letterColors = ((WordListBase)wordList).getBaseColors();

		//populate update list
		updateables = new ArrayList<Updateable>();
		for(Bucketbot r : bucketbots)
			updateables.add((Updateable)r);
		updateables.add((Updateable)map);
		updateables.add((Updateable)bucketbotManager);
		updateables.add((Updateable)wordManager);
		updateables.add((Updateable)letterManager);
		for(WordStation s : wordStations)
			updateables.add((Updateable)s);
		for(LetterStation s : letterStations)
			updateables.add((Updateable)s);

		//finish adding things to be rendered
		if(usingGUI) {
			RenderWindow.addAdditionalDetailRender(new WordListRender((WordListBase) wordList));

			RenderWindow.addLineRender(new MapRender(map));

			for(LetterStation s : letterStations)
				RenderWindow.addSolidRender(new LetterStationRender((LetterStationBase) s));
			for(WordStation s : wordStations)
				RenderWindow.addSolidRender(new WordStationRender((WordStationBase) s));

			for(Bucket b : buckets)
				RenderWindow.addLineRender(new BucketRender((BucketBase) b));
			for(Bucketbot r : bucketbots)
				RenderWindow.addLineRender(new BucketbotRender((BucketbotBase) r));

			//RenderWindow.addSolidRender(resourceManager);

			//RenderWindow.addSolidRender(new WaypointGraphRender(waypointGraph));
		}

	}

	public SimulationWorldGraph() {
		super("alphabetsoup.config");
		simulationWorldGraph = this;

		float bucketbot_size = Float.parseFloat(params.getProperty("bucketbot_size"));
		float bucket_size = Float.parseFloat(params.getProperty("bucket_size"));
		float station_size = Float.parseFloat(params.getProperty("station_size"));
		int bucket_capacity = Integer.parseInt(params.getProperty("bucket_capacity"));
		int bundle_size = Integer.parseInt(params.getProperty("bundle_size"));
		int letter_station_capacity = Integer.parseInt(params.getProperty("letter_station_capacity"));
		int word_station_capacity = Integer.parseInt(params.getProperty("word_station_capacity"));
		float bucket_pickup_setdown_time = Float.parseFloat( params.getProperty("bucket_pickup_setdown_time"));
		float letter_to_bucket_time = Float.parseFloat( params.getProperty("letter_to_bucket_time"));
		float bucket_to_letter_time = Float.parseFloat( params.getProperty("bucket_to_letter_time"));
		float word_completion_time = Float.parseFloat( params.getProperty("word_completion_time"));
		float collision_penalty_time = Float.parseFloat( params.getProperty("collision_penalty_time"));
		usingGUI = (Integer.parseInt(params.getProperty("useGUI")) == 1);
		String window_size[] = params.getProperty("window_size").split("x");
		simulationDuration = Double.parseDouble(params.getProperty("simulation_duration"));
		
		waypointGraph = new WaypointGraph(map.getWidth(), map.getHeight());
		
		//Set up base map to add things to
		if(usingGUI)
			RenderWindow.initializeUserInterface(Integer.parseInt(window_size[0]), Integer.parseInt(window_size[1]), this);
		
		BucketbotDriver.waypointGraph = waypointGraph;
		BucketbotDriver.map = map;

		//Create classes, and add them to the map accordingly
		for(int i = 0; i < bucketbots.length; i++)
			bucketbots[i] = (Bucketbot) new BucketbotDriver(bucketbot_size,
					bucket_pickup_setdown_time, map.getMaxAcceleration(), map.getMaxVelocity(), collision_penalty_time);
		
		for(int i = 0; i < letterStations.length; i++)
			letterStations[i] = (LetterStation) new LetterStationBase(
															station_size, letter_to_bucket_time, bundle_size, letter_station_capacity);
		
		for(int i = 0; i < wordStations.length; i++)
			wordStations[i] = (WordStation) new WordStationBase(
														station_size, bucket_to_letter_time, word_completion_time, word_station_capacity);
		
		for(int i = 0; i < buckets.length; i++)
			buckets[i] = (Bucket) new BucketBase(bucket_size, bucket_capacity);
		
		bucketbotManager	= new BotManager(buckets);
		letterManager	= new LetterManager();
		wordManager		= (Updateable) new OrderManager();
		
		for(Bucketbot r : bucketbots)
			((BucketbotDriver)r).manager = bucketbotManager;

		//generate waypoint graph
		HashMap<Waypoint, Bucket> storage = GenerateWaypointGraph.initializeCompactRandomLayout(this, waypointGraph);
		for(Waypoint w : storage.keySet())
			if(storage.get(w) == null)
				bucketbotManager.addNewValidBucketStorageLocation(w);
			else
				bucketbotManager.addNewUsedBucketStorageLocation(storage.get(w), w);
		
		//generate words
		wordList.generateWordsFromFile(params.getProperty("dictionary"), letterColors,
				Integer.parseInt(params.getProperty("number_of_words")) );
		
		//populate buckets
		initializeBucketContentsRandom(Float.parseFloat(params.getProperty("initial_inventory")), bundle_size);
		
		//populate update list
		updateables = new ArrayList<Updateable>();
		for(Bucketbot r : bucketbots)
			updateables.add((Updateable)r);
		updateables.add((Updateable)map);
		updateables.add((Updateable)bucketbotManager);
		updateables.add((Updateable)wordManager);
		updateables.add((Updateable)letterManager);
		for(WordStation s : wordStations)
			updateables.add((Updateable)s);
		for(LetterStation s : letterStations)
			updateables.add((Updateable)s);
		
		//finish adding things to be rendered
		if(usingGUI) {
			RenderWindow.addAdditionalDetailRender(new WordListRender((WordListBase) wordList));
			
			RenderWindow.addLineRender(new MapRender(map));
			
			for(LetterStation s : letterStations)
				RenderWindow.addSolidRender(new LetterStationRender((LetterStationBase) s));
			for(WordStation s : wordStations)
				RenderWindow.addSolidRender(new WordStationRender((WordStationBase) s));
			
			for(Bucket b : buckets)
				RenderWindow.addLineRender(new BucketRender((BucketBase) b));
			for(Bucketbot r : bucketbots)
				RenderWindow.addLineRender(new BucketbotRender((BucketbotBase) r));
			
			//RenderWindow.addSolidRender(resourceManager);
			
			//RenderWindow.addSolidRender(new WaypointGraphRender(waypointGraph));
		}
	}
	
	/**Launches the Alphabet Soup simulation without user interface.
	 * @param args
	 */
	public static void main(String[] args) {
		String initDir = System.getProperty("initDir");
		System.out.println("init directory is " + initDir);

		if (initDir!=null) {
			simulationWorld = SimulationWorldInitializer.loadGraphFromInitData(initDir);

		}  else {

			simulationWorld = new SimulationWorldGraph();
			SimulationWorldInitializer.recordInitData(simulationWorldGraph, "graph-"+System.currentTimeMillis());
		}

		if (simulationWorld == null) {
			System.out.println("simulation initialization failed");
			return;
		}

		if(simulationWorld.isUsingGUI())
		{
			/*RenderWindow.mainLoop(simulationWorld,
					((SimulationWorldGraph) simulationWorld).simulationDuration);
			RenderWindow.destroyUserInterface();*/
			RenderWindow.renderInitData(simulationWorld, ((SimulationWorldGraph) simulationWorld).simulationDuration);
		}
		else
			simulationWorld.update( ((SimulationWorldGraph)simulationWorld).simulationDuration);

		SummaryReport.generateReport(simulationWorld);
	}
}
