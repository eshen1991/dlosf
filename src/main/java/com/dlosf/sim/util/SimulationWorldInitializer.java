/**
 * 
 */
package com.dlosf.sim.util;

import alphabetsoup.base.BucketBase;
import alphabetsoup.base.LetterStationBase;
import alphabetsoup.base.WordListBase;
import alphabetsoup.base.WordStationBase;
import alphabetsoup.framework.*;
import alphabetsoup.waypointgraph.BucketbotDriver;
import alphabetsoup.waypointgraph.Waypoint;
import alphabetsoup.waypointgraph.WaypointGraph;
import com.dlosf.sim.graph.SimulationWorldGraph;
import com.dlosf.sim.greedy.SimulationWorldGreedy;
import com.dlosf.sim.simple.Bot;
import com.dlosf.sim.simple.SimulationWorldSimple;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map;

/**Example AlphabetSoup simulation file, which puts buckets in a grid, lays out bots randomly,
 * parameratizes everything based on "alphabetsoup.config", and starts everything running.
 *
 */
public class SimulationWorldInitializer {

	public enum SimWorldType {SIMPLE, GRAPH, GREEDY}

	public static void generateInitData(SimWorldType simType, String dir) {

		SimulationWorld simulationWorld = null;
		switch (simType) {
			case SIMPLE:
				simulationWorld = new SimulationWorldSimple();
				recordInitData((SimulationWorldSimple)simulationWorld, dir);
				break;
			case GRAPH:
				simulationWorld = new SimulationWorldGraph();
				recordInitData((SimulationWorldGraph)simulationWorld, dir);

				break;
			default:

		}

		if (simulationWorld == null) {
			System.out.println("Simulation type is not supported. No initial data has been generated") ;
			return;
		}



	}


	private static boolean isFileMissiong(File dir) {

		List<String> missingFiles = new ArrayList<String>();
		if (!(new File(dir, OSFConstant.JSON_FILE_WORDLIST).exists())) {
			missingFiles.add(OSFConstant.JSON_FILE_WORDLIST);
		}

		if (!(new File(dir, OSFConstant.JSON_FILE_UNUSED_LOCATIONS).exists())) {
			missingFiles.add(OSFConstant.JSON_FILE_UNUSED_LOCATIONS);
		}

		if (!(new File(dir, OSFConstant.JSON_FILE_PARAMS).exists())) {
			missingFiles.add(OSFConstant.JSON_FILE_PARAMS);
		}

		if (!(new File(dir, OSFConstant.JSON_FILE_BOTS).exists())) {
			missingFiles.add(OSFConstant.JSON_FILE_BOTS);
		}

		if (!(new File(dir, OSFConstant.JSON_FILE_BUCKETS).exists())) {
			missingFiles.add(OSFConstant.JSON_FILE_BUCKETS);
		}

		if (!(new File(dir, OSFConstant.JSON_FILE_INVENTORYSTATION).exists())) {
			missingFiles.add(OSFConstant.JSON_FILE_INVENTORYSTATION);
		}

		if (!(new File(dir, OSFConstant.JSON_FILE_PICKSTATION).exists())) {
			missingFiles.add(OSFConstant.JSON_FILE_PICKSTATION);
		}

		boolean missing =     !missingFiles.isEmpty()?true:false;

		if (missing) {
			System.out.println("files are missing: " + missingFiles.toString());
		}
		return  missing;
	}


	public static SimulationWorld loadGraphFromInitData(String dir, SimWorldType smType) {
		SimulationWorld simulationWorld = null;
		File outputDir = new File(dir);
		if (!outputDir.exists()) {
			System.out.println("directory " + dir + " doesn't exist and simulation world can't be created from initial data set ");
			return null;
		}

		if (isFileMissiong(outputDir)) {
			System.out.println("init files are missing");
			return null;
		}


		ObjectMapper mapper = new ObjectMapper();


		Bucketbot[] bots = null;
		Bucket[] buckets = null;

		LetterStation[] letterStations = null;
		WordStation[] wordStations = null;
		Properties params = null;
		List<Waypoint> unusedBucketStorageLocations = null;
		HashMap<Bucket,Waypoint> usedBucketStorageLocations = new HashMap<Bucket, Waypoint>();
		WordList wordList = null;
		WaypointGraph waypointGraph = null;




		try {


			params = mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_PARAMS), Properties.class);

			List<JsonBot> jsonBots =   mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_BOTS), new TypeReference<List<JsonBot>>(){});
			bots = new Bucketbot[jsonBots.size()];
			for (int i=0;i<jsonBots.size();i++) {
				JsonBot jBot = jsonBots.get(i);
				BucketbotDriver b  = new BucketbotDriver(jBot.getRadius(),jBot.getBucketPickupSetdownTime(), jBot.getMaxAcceleration(), jBot.getMaxVelocity(),jBot.getCollisionPenaltyTime());
				b.setInitialPosition(jBot.getX(),jBot.getY());
				bots[i] = b;
			}

			List<JsonBucket> jsonBuckets =  mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_BUCKETS), new TypeReference<List<JsonBucket>>(){});
			List<JsonLetterStation> jsonLetterStations = mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_INVENTORYSTATION),new TypeReference<List<JsonLetterStation>>(){});
			List<JsonWordStation> jsonWordStations =  mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_PICKSTATION),new TypeReference<List<JsonWordStation>>(){});
			List<JsonWaypoint> jsonUnusedLocations =  mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_UNUSED_LOCATIONS),new TypeReference<List<JsonWaypoint>>(){});
			List<JsonWaypoint> jsonRestWaypoints =  mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_WAYPOINT),new TypeReference<List<JsonWaypoint>>(){});
			Map<JsonWaypoint, Waypoint> waypointMap = new HashMap<JsonWaypoint, Waypoint>();
			Map<String, JsonWaypoint>   jsonWaypointMap  = new HashMap<String, JsonWaypoint>();

			//*** layout of letterstaions, wordstations, buckets, unusedStorage locations, and rest of waypoints
			//layout buckets
			buckets = new Bucket[jsonBuckets.size()];
			for (int i=0; i<jsonBuckets.size(); i++) {
				JsonBucket jb = jsonBuckets.get(i);
				BucketBase b = new BucketBase(jb.getRadius(), jb.getCapacity());
				for (Letter l: JsonHelper.converStringToLetters(jb.getLetters())) {
					b.addLetter(l);
				}
				b.setInitialPosition(jb.getX(), jb.getY());

				buckets[i] = b;

				Waypoint waypoint = new Waypoint(b);

				waypointMap.put(jb, waypoint);
				jsonWaypointMap.put(jb.getUuid(), jb);

				usedBucketStorageLocations.put(b, waypoint);

			}

			//layout letter stations
			letterStations = new LetterStation[jsonLetterStations.size()];
			for (int i=0;i<letterStations.length;i++) {
				JsonLetterStation jls = jsonLetterStations.get(i);
				LetterStationBase ls = new LetterStationBase(jls.getRadius(),jls.getLetterToBucketTime(), jls.getBundleSize(),jls.getCapacity());
				ls.setInitialPosition(jls.getX(), jls.getY());

				letterStations[i] = ls;

				Waypoint waypoint = new Waypoint(ls);

				waypointMap.put(jls, waypoint);
				jsonWaypointMap.put(jls.getUuid(), jls);


			}

			//layout wordstations
			wordStations = new WordStation[jsonWordStations.size()];
			for (int i=0;i<jsonWordStations.size();i++) {
				JsonWordStation jws = jsonWordStations.get(i);
				WordStationBase ws = new WordStationBase(jws.getRadius(),jws.getBucketToLetterTime(),jws.getWordCompletionTime(),jws.getCapacity());
				ws.setInitialPosition(jws.getX(), jws.getY());

				wordStations[i] = ws;

				Waypoint waypoint = new Waypoint(ws);

				waypointMap.put(jws, waypoint);
				jsonWaypointMap.put(jws.getUuid(), jws);

			}

			//layout unused bucket storage locations
			unusedBucketStorageLocations = new ArrayList<Waypoint>();
			for (JsonWaypoint sl : jsonUnusedLocations) {

				Waypoint waypoint = new Waypoint(sl.getX(), sl.getY(), true);
				unusedBucketStorageLocations.add(waypoint);
				waypointMap.put(sl, waypoint);
				jsonWaypointMap.put(sl.getUuid(), sl);


			}

			//layout rest of waypoints
			for (JsonWaypoint jwp : jsonRestWaypoints) {
				Waypoint waypoint = new Waypoint(jwp.getX(), jwp.getY(), false);
				waypointMap.put(jwp, waypoint);
				jsonWaypointMap.put(jwp.getUuid(), jwp);
			}


			//establish paths for all the waypoints
			float map_width = Float.parseFloat(params.getProperty("map_width"));
			float map_length = Float.parseFloat(params.getProperty("map_length"));
			waypointGraph = new WaypointGraph(map_width, map_length);
			for (JsonWaypoint jp : waypointMap.keySet()) {
				Waypoint p = waypointMap.get(jp);
				Map<String, Float> jPaths = jp.getPaths();
				for (String id : jPaths.keySet()) {
					p.addPath(waypointMap.get(jsonWaypointMap.get(id)), jPaths.get(id));
				}

				waypointGraph.addWaypoint(p);
			}

			//generate word list
			JsonWordList jsonWordList =  mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_WORDLIST), JsonWordList.class);
			wordList = JsonHelper.convertToWordList(jsonWordList);



		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		switch (smType) {
			case GRAPH: simulationWorld = new SimulationWorldGraph(buckets, bots, letterStations, wordStations, wordList, unusedBucketStorageLocations, usedBucketStorageLocations, waypointGraph, params);
				        break;
			case GREEDY: simulationWorld = new SimulationWorldGreedy(buckets, bots, letterStations, wordStations, wordList, unusedBucketStorageLocations, usedBucketStorageLocations, waypointGraph, params);
				        break;
			default:
		}
		return simulationWorld;
	}



	public static SimulationWorldSimple loadFromInitData(String dir) {
		SimulationWorldSimple simulationWorld = null;
		File outputDir = new File(dir);
		if (!outputDir.exists()) {
			System.out.println("directory " + dir + " doesn't exist and simulation world can't be created from initial data set ");
			return null;
		}

		if (isFileMissiong(outputDir)) {
			System.out.println("init files are missing");
			return null;
		}


		ObjectMapper mapper = new ObjectMapper();


		Bucketbot[] bots = null;
		Bucket[] buckets = null;

		LetterStation[] letterStations = null;
		WordStation[] wordStations = null;
		Properties params = null;
		List<Circle> unusedBucketStorageLocations = null;
		HashMap<Bucket,Circle> usedBucketStorageLocations = new HashMap<Bucket, Circle>();
		WordList wordList = null;




		try {
			List<JsonBot> jsonBots =   mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_BOTS), new TypeReference<List<JsonBot>>(){});
			bots = new Bucketbot[jsonBots.size()];
			for (int i=0;i<jsonBots.size();i++) {
				JsonBot jBot = jsonBots.get(i);
				Bot b = new Bot(jBot.getRadius(),jBot.getBucketPickupSetdownTime(), jBot.getMaxAcceleration(), jBot.getMaxVelocity(),jBot.getCollisionPenaltyTime());
				b.setInitialPosition(jBot.getX(),jBot.getY());
				bots[i] = b;


			}
			JsonBucket[] jsonBuckets =  mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_BUCKETS), JsonBucket[].class);
			buckets = new Bucket[jsonBuckets.length];
			for (int i=0; i<jsonBuckets.length; i++) {
				BucketBase b = new BucketBase(jsonBuckets[i].getRadius(), jsonBuckets[i].getCapacity());
				for (Letter l: JsonHelper.converStringToLetters(jsonBuckets[i].getLetters())) {
					b.addLetter(l);
				}
				b.setInitialPosition(jsonBuckets[i].getX(), jsonBuckets[i].getY());
				buckets[i] = b;


			}

			List<JsonLetterStation> jsonLetterStations = mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_INVENTORYSTATION),new TypeReference<List<JsonLetterStation>>(){});
			letterStations = JsonHelper.convertToInvStation(jsonLetterStations).toArray(new LetterStation[jsonLetterStations.size()]);

			List<JsonWordStation> jsonWordStations =  mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_PICKSTATION),new TypeReference<List<JsonWordStation>>(){});
			wordStations =  JsonHelper.convertToWordStation(jsonWordStations).toArray(new WordStation[jsonWordStations.size()]);

			params = mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_PARAMS), Properties.class);

			List<JsonWaypoint> jsonSls=  mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_UNUSED_LOCATIONS),new TypeReference<List<JsonWaypoint>>(){});
			unusedBucketStorageLocations = new ArrayList<Circle>();
			for (JsonWaypoint jsl : jsonSls) {
				unusedBucketStorageLocations.add(new Circle(0.0f, jsl.getX(), jsl.getY()));
			}

			for (Bucket b: buckets) {
				usedBucketStorageLocations.put(b, new Circle(0.0f, b.getX(), b.getY()));
			}



			JsonWordList jsonWordList =  mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_WORDLIST), JsonWordList.class);
			wordList = JsonHelper.convertToWordList(jsonWordList);



		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		simulationWorld = new SimulationWorldSimple(buckets,bots,letterStations,wordStations,wordList,unusedBucketStorageLocations, usedBucketStorageLocations,params);

		return simulationWorld;
	}


	public static void recordInitData(SimulationWorldGraph graphSim, String dir) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

		File outputDir = new File(dir);
		if (!outputDir.exists()) {
			outputDir.mkdir();

		}

		try {

			//build some lookup maps
			HashMap<LetterStation, Waypoint> letterStations = graphSim.waypointGraph.getLetterStations();
			HashMap<WordStation, Waypoint> wordStations = graphSim.waypointGraph.getWordStations();
			HashMap<Bucket, Waypoint> buckets = graphSim.waypointGraph.getBuckets();
			Set<Waypoint> waypoints = graphSim.waypointGraph.getWaypoints();
			Set<Waypoint> usedWaypoints = new HashSet<Waypoint>();
			HashMap<Waypoint, JsonWaypoint> waypointJsonObjMap = new HashMap<Waypoint, JsonWaypoint>();


			List<JsonWaypoint> unusedBucketStorageLocations = new LinkedList<JsonWaypoint>();
			List<JsonBucket> jsonBuckets = new ArrayList<JsonBucket>();
			List<JsonLetterStation> jsonLetterStations = new ArrayList<JsonLetterStation>();
			List<JsonWordStation> jsonWordStations = new ArrayList<JsonWordStation>();

			for (LetterStation ls : letterStations.keySet()) {
				Waypoint p = letterStations.get(ls);
				JsonLetterStation jsonLetterStation  = new JsonLetterStation(ls.getX(),
						ls.getY(),ls.getRadius(),ls.getCapacity(),
						ls.getBundleSize(),ls.getLetterToBucketTime());

				waypointJsonObjMap.put(p, jsonLetterStation);
				usedWaypoints.add(p);
				jsonLetterStations.add(jsonLetterStation);
			}


			for (WordStation ws : wordStations.keySet()){
				Waypoint p = wordStations.get(ws);
				JsonWordStation jsonWordStation = new JsonWordStation(ws.getX(), ws.getY(),
						ws.getRadius(),ws.getCapacity(),ws.getBucketToLetterTime(),ws.getWordCompletionTime());
				waypointJsonObjMap.put(p, jsonWordStation);
				usedWaypoints.add(p);
				jsonWordStations.add(jsonWordStation);

			}

			for (Bucket b: buckets.keySet()) {
				Waypoint p = buckets.get(b);
				JsonBucket jsonBucket = new JsonBucket(b);
				waypointJsonObjMap.put(p, jsonBucket);
				usedWaypoints.add(p);
				jsonBuckets.add(jsonBucket);

			}


			for (Waypoint p : graphSim.bucketbotManager.getUnusedBucketStorageLocations()) {

				JsonWaypoint unusedLocation = new JsonWaypoint(p.getX(), p.getY(), p.getRadius());
				waypointJsonObjMap.put(p, unusedLocation);
				usedWaypoints.add(p);
				unusedBucketStorageLocations.add(unusedLocation);

			}

			List<JsonWaypoint> waypointList = new ArrayList<JsonWaypoint>();
			for (Waypoint p : waypoints) {
				if(!usedWaypoints.contains(p)) {
					JsonWaypoint jp = new JsonWaypoint(p.getX(), p.getY(), p.getRadius());
					waypointJsonObjMap.put(p, jp);
					waypointList.add(jp);
				}
			}
			//populate paths for all the waypoints
			for (Waypoint p : waypointJsonObjMap.keySet()) {
				JsonWaypoint jsonWaypoint = waypointJsonObjMap.get(p);
				HashMap<String, Float> jsonPaths = new HashMap<String, Float>();


				for (Waypoint path: p.getPaths())  {
					jsonPaths.put(waypointJsonObjMap.get(path).getUuid(), p.getPathWeight(path)) ;

				}

				jsonWaypoint.setPaths(jsonPaths);
			}

			List<JsonBot> jsonBots = new ArrayList<JsonBot>();
			for (Bucketbot bot : graphSim.getRobots()) {
				JsonBot jsonBot = new JsonBot(bot.getX(),bot.getY(),bot.getRadius(),
						bot.getBucketPickupSetdownTime(), bot.getMaxAcceleration(),bot.getMaxVelocity(),((BucketbotDriver)bot).getCollisionPenaltyTime());
				jsonBots.add(jsonBot);
			}


			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_BUCKETS), jsonBuckets);
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_BOTS), jsonBots);
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_INVENTORYSTATION), jsonLetterStations);
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_PICKSTATION), jsonWordStations);
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, "params.json"), graphSim.getParams());


			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_UNUSED_LOCATIONS), unusedBucketStorageLocations);
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_WAYPOINT), waypointList);

			WordListBase wl = (WordListBase)graphSim.getWordList();
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_WORDLIST), JsonHelper.convertToJsonWordList(wl));



		} catch (Exception e) {
			e.printStackTrace();

		}


	}



	public static void recordInitData(SimulationWorldSimple simpleSim, String dir) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

		File outputDir = new File(dir);
		if (!outputDir.exists()) {
			outputDir.mkdir();

		}

		try {

			List<JsonBot> jsonBots = new ArrayList<JsonBot>();
			for (Bucketbot bot : simpleSim.getRobots()) {
				JsonBot jsonBot = new JsonBot(bot.getX(),bot.getY(),bot.getRadius(),
						bot.getBucketPickupSetdownTime(), bot.getMaxAcceleration(),bot.getMaxVelocity(),((Bot)bot).getCollisionPenaltyTime());
				jsonBots.add(jsonBot);
			}

			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_BUCKETS), JsonHelper.convertToJsonBucket(simpleSim.getBuckets()));

			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_BOTS), jsonBots);
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_INVENTORYSTATION), JsonHelper.convertToJsonInvStation(Arrays.asList(simpleSim.getLetterStations())));
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_PICKSTATION), JsonHelper.convertToJsonWordStation(Arrays.asList(simpleSim.getWordStations())));
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, "params.json"), simpleSim.getParams());

			List<Circle> unusedBucketStorageLocations = simpleSim.bucketbotManager.getUnusedBucketStorageLocations();
			List<JsonWaypoint> jsonSls = new ArrayList<JsonWaypoint>();
			for (Circle c : unusedBucketStorageLocations) {
				jsonSls.add(new JsonWaypoint(c.getX(),c.getY(),c.getRadius()));
			}

			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_UNUSED_LOCATIONS), jsonSls);

			WordListBase wl = (WordListBase) simpleSim.getWordList();
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_WORDLIST), JsonHelper.convertToJsonWordList(wl));



		} catch (Exception e) {
			e.printStackTrace();

		}


	}

/*	public static String[] getBaseWords(String filename) {

			//open file to get words
			String content;
			try {
				FileInputStream fis = new FileInputStream(filename);
				int x= fis.available();
				byte b[]= new byte[x];
				fis.read(b);
				content = new String(b);
			}
			catch (Throwable e) {
				System.out.println("Could not open file " + filename);
				return null;
			}

			//store info to build more new words
			return content.split("(\r\n)|\n|\r");	//split on any newline combo
	}*/

	/**Launches the Alphabet Soup simulation without user interface.
	 * @param args
	 */
	public static void main(String[] args) {

		//generateInitData(SimWorldType.GRAPH, "debuggraphoutput");


		//generateInitData(SimWorldType.SIMPLE, "initoutput");
		//loadFromInitData("initouput");

		SimulationWorld simulationWorld1 = new SimulationWorldGraph();
		recordInitData((SimulationWorldGraph)simulationWorld1, "initgraphoutput");

		//SimulationWorld simulationWorld12 = loadGraphFromInitData("initgraphoutput");





		System.out.println("done");

	}




}
