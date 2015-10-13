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
import com.dlosf.sim.simple.Bot;
import com.dlosf.sim.simple.BotManager;
import com.dlosf.sim.simple.SimulationWorldSimple;
import org.codehaus.jackson.JsonNode;
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


	public static SimulationWorldGraph loadGraphFromInitData(String dir) {
		SimulationWorldGraph simulationWorld = null;
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
				bots[i] = new BucketbotDriver(jBot.getRadius(),jBot.getBucketPickupSetdownTime(), jBot.getMaxAcceleration(), jBot.getMaxVelocity(),jBot.getCollisionPenaltyTime());

			}

			List<JsonBucket> jsonBuckets =  mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_BUCKETS), new TypeReference<List<JsonBucket>>(){});
			List<JsonLetterStation> jsonLetterStations = mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_INVENTORYSTATION),new TypeReference<List<JsonLetterStation>>(){});
			List<JsonWordStation> jsonWordStations =  mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_PICKSTATION),new TypeReference<List<JsonWordStation>>(){});
			List<JsonWaypoint> jsonUnusedLocations =  mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_UNUSED_LOCATIONS),new TypeReference<List<JsonWaypoint>>(){});
			Map<JsonWaypoint, Waypoint> waypointMap = new HashMap<JsonWaypoint, Waypoint>();
			Map<String, JsonWaypoint>   jsonWaypointMap  = new HashMap<String, JsonWaypoint>();

			//layout of letterstaions, wordstations, and buckets through waypoint
			buckets = new Bucket[jsonBuckets.size()];
			for (int i=0; i<jsonBuckets.size(); i++) {
				JsonBucket jb = jsonBuckets.get(i);
				BucketBase b = new BucketBase(jb.getRadius(), jb.getCapacity());
				for (Letter l: converStringToLetters(jb.getLetters())) {
					b.addLetter(l);
				}
				b.setInitialPosition(jb.getX(), jb.getY());

				buckets[i] = b;

				Waypoint waypoint = new Waypoint(b);

				waypointMap.put(jb, waypoint);
				jsonWaypointMap.put(jb.getUuid(), jb);

				usedBucketStorageLocations.put(b, waypoint);

			}


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

			for (JsonWaypoint sl : jsonUnusedLocations) {
				//float x, float y, boolean bucket_storage_location
				Waypoint waypoint = new Waypoint(sl.getX(), sl.getY(), true);
				unusedBucketStorageLocations.add(waypoint);
				waypointMap.put(sl, waypoint);
				jsonWaypointMap.put(sl.getUuid(), sl);


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











			JsonWordList jsonWordList =  mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_WORDLIST), JsonWordList.class);
			wordList = convertToWordList(jsonWordList);



		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		simulationWorld = new SimulationWorldGraph(buckets,bots,letterStations,wordStations,wordList,unusedBucketStorageLocations, usedBucketStorageLocations, waypointGraph, params);

		return simulationWorld;
	}


	private static Waypoint converToWaypoint(JsonWaypoint jsonWaypoint) {
		return null;
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
			//bots = mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_BOTS), Bot[].class);
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
				for (Letter l: converStringToLetters(jsonBuckets[i].getLetters())) {
					b.addLetter(l);
				}
				b.setInitialPosition(jsonBuckets[i].getX(), jsonBuckets[i].getY());
				buckets[i] = b;


			}

			List<JsonLetterStation> jsonLetterStations = mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_INVENTORYSTATION),new TypeReference<List<JsonLetterStation>>(){});
			letterStations = convertToInvStation(jsonLetterStations).toArray(new LetterStation[jsonLetterStations.size()]);

			List<JsonWordStation> jsonWordStations =  mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_PICKSTATION),new TypeReference<List<JsonWordStation>>(){});
			wordStations =  convertToWordStation(jsonWordStations).toArray(new WordStation[jsonWordStations.size()]);

			params = mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_PARAMS), Properties.class);

			//unusedBucketStorageLocations = mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_UNUSED_LOCATIONS),new TypeReference<List<Circle>>(){});
			List<JsonWaypoint> jsonSls=  mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_UNUSED_LOCATIONS),new TypeReference<List<JsonWaypoint>>(){});
			unusedBucketStorageLocations = new ArrayList<Circle>();
			for (JsonWaypoint jsl : jsonSls) {
				unusedBucketStorageLocations.add(new Circle(0.0f, jsl.getX(), jsl.getY()));
			}

		/*	List<JsonBucketCirclePair> pairs = mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_USED_LOCATIONS),new TypeReference<List<JsonBucketCirclePair>>(){});
			usedBucketStorageLocations = new HashMap<Bucket, Circle>();
			for (JsonBucketCirclePair p: pairs) {
				usedBucketStorageLocations.put(convertToBucket(p.getBucket()), p.getCircle());



			}

			Circle c = new Circle(0.0f, b.getX(), b.getY());
		usedBucketStorageLocations.put(b, c);
*/

			for (Bucket b: buckets) {
				usedBucketStorageLocations.put(b, new Circle(0.0f, b.getX(), b.getY()));
			}



			JsonWordList jsonWordList =  mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_WORDLIST), JsonWordList.class);
			wordList = convertToWordList(jsonWordList);



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

			waypoints.removeAll(usedWaypoints);

			if (!waypoints.isEmpty()) {
				for (Waypoint p : waypoints) {
					JsonWaypoint unusedLocation = new JsonWaypoint(p.getX(), p.getY(),p.getRadius());
					waypointJsonObjMap.put(p, unusedLocation);
					unusedBucketStorageLocations.add(unusedLocation);
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

			WordListBase wl = (WordListBase)graphSim.getWordList();
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_WORDLIST), convertToJsonWordList(wl));



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

			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_BUCKETS), convertToJsonBucket(simpleSim.getBuckets()));
			//mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_BOTS), simpleSim.getRobots());
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_BOTS), jsonBots);
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_INVENTORYSTATION), convertToJsonInvStation(Arrays.asList(simpleSim.getLetterStations())));
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_PICKSTATION), convertToJsonWordStation(Arrays.asList(simpleSim.getWordStations())));
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, "params.json"), simpleSim.getParams());

			List<Circle> unusedBucketStorageLocations = simpleSim.bucketbotManager.getUnusedBucketStorageLocations();
			List<JsonWaypoint> jsonSls = new ArrayList<JsonWaypoint>();
			for (Circle c : unusedBucketStorageLocations) {
				jsonSls.add(new JsonWaypoint(c.getX(),c.getY(),c.getRadius()));
			}

			//mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_UNUSED_LOCATIONS), unusedBucketStorageLocations);
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_UNUSED_LOCATIONS), jsonSls);

			WordListBase wl = (WordListBase) simpleSim.getWordList();
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_WORDLIST), convertToJsonWordList(wl));



		} catch (Exception e) {
			e.printStackTrace();

		}


	}

	/**Launches the Alphabet Soup simulation without user interface.
	 * @param args
	 */
	public static void main(String[] args) {

		//generateInitData(SimWorldType.GRAPH, "graphoutput");
		generateInitData(SimWorldType.SIMPLE, "initoutput");
		//loadFromInitData("initouput");

	/*	SimulationWorld simulationWorld1 = new SimulationWorldSimple();
		recordInitData((SimulationWorldSimple)simulationWorld1, "initoutput");*/

		//SimulationWorld simulationWorld12 = loadFromInitData("initoutput");





		System.out.println("done");

	}

	public static String convertLettersToString(Letter[] letters) {



		StringBuffer buf = new StringBuffer();
		for (Letter l: letters) {
			buf.append(l.toString());
			buf.append("#");
		}

		return buf.toString();

	}

	public static List<Letter> converStringToLetters(String str) {
		List<Letter> letters = new ArrayList<Letter>();

		String[] charColorPairs = str.split("#");
		for (String pair: charColorPairs) {
			 Letter l = convertCCpair(pair);
			if (l!=null) {
				letters.add(l);
			}
		}
		return letters;

	}

	private static Letter convertCCpair(String str) {
		int i = str.indexOf('(');
		int j = str.indexOf(')');
		int colorId = 0;
		try {
			colorId =  Integer.parseInt(str.substring(i+1,j));
		} catch (NumberFormatException e) {
			return null;
		}
		return new Letter(str.charAt(0), colorId);

	}

	public static List<JsonLetterStation> convertToJsonInvStation(List<LetterStation> invStation) {
		List<JsonLetterStation> jsonLetterStations = new ArrayList<JsonLetterStation>();
		for (LetterStation s: invStation) {
			JsonLetterStation jsonStation = new JsonLetterStation();
			jsonStation.setBundleSize(s.getBundleSize());
			jsonStation.setCapacity(s.getCapacity());
			jsonStation.setLetterToBucketTime(s.getLetterToBucketTime());
			jsonStation.setX(s.getX());
			jsonStation.setY(s.getY());
			jsonStation.setRadius(s.getRadius());
			jsonLetterStations.add(jsonStation);

		}
		return jsonLetterStations;
	}

	public static List<LetterStation> convertToInvStation(List<JsonLetterStation> jsonStations) {
		List<LetterStation> stations = new ArrayList<LetterStation>();
		for (JsonLetterStation jsonStation: jsonStations) {

			LetterStationBase station = new LetterStationBase(jsonStation.getRadius(),jsonStation.getLetterToBucketTime(), jsonStation.getBundleSize(),jsonStation.getCapacity());
			station.setInitialPosition(jsonStation.getX(), jsonStation.getY());
			stations.add(station);
		}

		return stations;
	}


	public static List<JsonWordStation> convertToJsonWordStation(List<WordStation> wordStation) {
		List<JsonWordStation> jsonWordStations = new ArrayList<JsonWordStation>();
		for (WordStation s: wordStation) {
			JsonWordStation jsonStation = new JsonWordStation();
			jsonStation.setCapacity(s.getCapacity());
			jsonStation.setBucketToLetterTime(s.getBucketToLetterTime());
			jsonStation.setWordCompletionTime(s.getWordCompletionTime());
			jsonStation.setX(s.getX());
			jsonStation.setY(s.getY());
			jsonStation.setRadius(s.getRadius());
			jsonWordStations.add(jsonStation);

		}
		return jsonWordStations;
	}

	public static List<WordStation> convertToWordStation(List<JsonWordStation> jsonStations) {
		List<WordStation> stations = new ArrayList<WordStation>();
		for (JsonWordStation jsonStation: jsonStations) {

			WordStationBase station = new WordStationBase(jsonStation.getRadius(),jsonStation.getBucketToLetterTime(),jsonStation.getWordCompletionTime(),jsonStation.getCapacity());
			station.setInitialPosition(jsonStation.getX(), jsonStation.getY());
			stations.add(station);
		}

		return stations;
	}

	public static List<JsonBucket> convertToJsonBucket(Bucket[] buckets) {
		List<JsonBucket> jsonBuckets = new ArrayList<JsonBucket>();
		for (Bucket bucket: buckets) {
			jsonBuckets.add(new JsonBucket(bucket));
		}

		return jsonBuckets;

	}

	public static Bucket convertToBucket(JsonBucket jsonBucket) {
		BucketBase b = new BucketBase(jsonBucket.getRadius(), jsonBucket.getCapacity());
		for (Letter l : converStringToLetters(jsonBucket.getLetters())) {
			b.addLetter(l);
		}
		b.setInitialPosition(jsonBucket.getX(), jsonBucket.getY());

		return b;

	}



	public static List<String> convertWords(List<Word> words) {
		List<String> simpleWords = new ArrayList<String>();
		for (Word w: words) {
			simpleWords.add(convertLettersToString(w.getOriginalLetters()));

		}

		return simpleWords;


	}

	//public static Word convertStringToWord()

	public static JsonWordList convertToJsonWordList(WordListBase wordList) {
		JsonWordList jsonWord = new JsonWordList();
		jsonWord.setAvailableWords(convertWords(wordList.getAvailableWords()));
		jsonWord.setBaseColors(wordList.getBaseColors());
		jsonWord.setBaseWords(wordList.getBaseWords());
		jsonWord.setWords(convertWords(wordList.getWords()));
		jsonWord.setLetterProbabilities(wordList.getLetterProbabilities());
		return jsonWord;
	}

	public static WordList convertToWordList(JsonWordList jsonWordList) {

		WordListBase wordList = new WordListBase();

		List<Word> words = new ArrayList<Word>();
		for (String s: jsonWordList.getAvailableWords()){
			List<Letter>  letters = converStringToLetters(s) ;
			Word w = new Word(letters.toArray(new Letter[letters.size()]));
			words.add(w);
		}
		wordList.setAvailableWords(words);

		words = new ArrayList<Word>();
		for (String s: jsonWordList.getWords()){
			List<Letter>  letters = converStringToLetters(s) ;
			Word w = new Word(letters.toArray(new Letter[letters.size()]));
			words.add(w);
		}
		wordList.setWords(words);




		wordList.setBaseWords(jsonWordList.getBaseWords());
		wordList.setBaseColors(jsonWordList.getBaseColors());
		wordList.setLetterProbabilities(jsonWordList.getLetterProbabilities());

		return wordList;

	}


/*	public static void generateWaypointGraph(List<JsonBucket> buckets, WaypointGraph waypointGraph) {

		//create all the waypoint nodes for the grid
		float init_y = buckets.get(0).getY();

		List<List<JsonBucket>> bucketGrids = new ArrayList<List<JsonBucket>>();
		List<JsonBucket> rowBuckets = new ArrayList<JsonBucket>();
		for (JsonBucket b : buckets) {  //works because buckets already sorted
			if (b.getY() != init_y) {
				init_y = b.getY();
				bucketGrids.add(rowBuckets);
				rowBuckets = new ArrayList<JsonBucket>();
			} else {
				rowBuckets.add(b);
			}

		}

		int row = bucketGrids.size();    //height_count
		int col = bucketGrids.get(0).size();   //width_count
		Waypoint[][] grid = new Waypoint[row][col];

		int bucket_block_length = 5;

		for(int i = 0; i < col; i++) {
			for(int j = 0; j < row; j++) {

				waypointGraph.addWaypoint(grid[i][j]);

				//connect horizontally
				if(j > 0) { //don't connect if first node
					//connect based on the row
					switch(i % 10) {
						case 0:	case 4:
							grid[i][j].addPath(grid[i][j-1]);	break;
						case 1:	case 7:
							grid[i][j-1].addPath(grid[i][j]);	break;
						default:
							grid[i][j].addBidirectionalPath(grid[i][j-1]);
							//make sure it's not on the edge, and leave gaps
							*//*if(j > 1 && j < col - 2
									&& j % bucket_block_length != 0) {
								grid[i][j].setBucketStorageLocation();
								bucket_storage_locations.put(grid[i][j], null);
							}*//*
							break;
					}


				}

				//connect vertically
				if(i > 0) { //don't connect if first node
					//grid[i][j].addBidirectionalPath(grid[i-1][j]);

					if(j == 0)
						grid[i-1][j].addPath(grid[i][j]);
					else if(j == 1)
						grid[i][j].addPath(grid[i-1][j]);
					else if(j == col - 2)
						grid[i-1][j].addPath(grid[i][j]);
					else if(j == col - 1)
						grid[i][j].addPath(grid[i-1][j]);
					else { //need to check if open isle
						int column_remainder = j % (2 * bucket_block_length);
						if(column_remainder == 0)
							grid[i][j].addPath(grid[i-1][j]);
						else if(column_remainder == bucket_block_length)
							grid[i-1][j].addPath(grid[i][j]);
						else
							grid[i][j].addBidirectionalPath(grid[i-1][j]);
					}

				}
			}
		}

		//put buckets on storage locations
		ArrayList<Waypoint> storage_locations = new ArrayList<Waypoint>();
		storage_locations.addAll(bucket_storage_locations.keySet());
		for(int i = 0; i < sw.buckets.length; i++) {
			Bucket b = sw.buckets[i];
			Waypoint w = storage_locations.get(i);

			((Circle)b).setInitialPosition(w.getX(), w.getY());
			circles.add((Circle)b);

			waypointGraph.bucketSetdown(b, w);
			bucket_storage_locations.put(w, b);
		}



	}*/
}
