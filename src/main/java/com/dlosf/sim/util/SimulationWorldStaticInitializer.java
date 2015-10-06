/**
 * 
 */
package com.dlosf.sim.util;

import alphabetsoup.base.BucketBase;
import alphabetsoup.base.WordListBase;
import alphabetsoup.framework.*;
import com.dlosf.sim.simple.Bot;
import com.dlosf.sim.simple.SimulationWorldSimple;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**Example AlphabetSoup simulation file, which puts buckets in a grid, lays out bots randomly,
 * parameratizes everything based on "alphabetsoup.config", and starts everything running.
 * @author Chris Hazard
 */
public class SimulationWorldStaticInitializer {

	public enum SimWorldType {SIMPLE, GRAPH, GREEDY}

	public static void generateInitData(SimWorldType simType, String dir) {

		SimulationWorld simulationWorld = null;
		switch (simType) {
			case SIMPLE:
				simulationWorld = new SimulationWorldSimple();
				recordInitData((SimulationWorldSimple)simulationWorld, dir);
				break;

		}

		if (simulationWorld == null) {
			System.out.println("Simulation type is not supported. No initial data has been generated") ;
			return;
		}



	}

	public static SimulationWorldSimple loadFromInitData(String dir) {
		SimulationWorldSimple simulationWorld = null;
		File outputDir = new File(dir);
		if (!outputDir.exists()) {
			System.out.println("directory " + dir + " doesn't exist and simulation world can't be created from initial data set ");
			return null;
		}


		ObjectMapper mapper = new ObjectMapper();

		//mapper.setDeserializationConfig(DeserializationConfig.Feature.WRAP_EXCEPTIONS);

		JsonNode node = null;
		//Object obj = null;

		Bucketbot[] bots = null;
		JsonBucket[] jsonBuckets = null;
		Bucket[] buckets = null;

		try {
			//bots = mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_BOTS), Bot[].class);
			//jsonBuckets =  mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_BOTS), new TypeReference<List<JsonBucket>>(){});
			jsonBuckets =  mapper.readValue(new File(outputDir, OSFConstant.JSON_FILE_BUCKETS), JsonBucket[].class);
			buckets = new Bucket[jsonBuckets.length];
			for (int i=0; i<jsonBuckets.length; i++) {
				BucketBase b = new BucketBase(jsonBuckets[i].getRadius(), jsonBuckets[i].getCapacity());
				for (Letter l: converStringToLetters(jsonBuckets[i].getLetters())) {
					b.addLetter(l);
				}
				b.setInitialPosition(jsonBuckets[i].getX(), jsonBuckets[i].getY());
				buckets[i] = b;


			}
		/*	node = mapper.readTree(new File(outputDir, OSFConstant.JSON_FILE_BUCKETS));
			ArrayNode arrayNode = (ArrayNode)node;

			for (JsonNode objectNode : arrayNode) {
				JsonBucket obj =  mapper.readValue(objectNode, JsonBucket.class);

			}*/

		} catch (IOException e) {
			e.printStackTrace();
		}

		//mapper
		//User user = mapper.readValue(new File("c:\\user.json"), User.class);


		return simulationWorld;
	}
	public static void recordInitData(SimulationWorldSimple simpleSim, String dir) {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

		File outputDir = new File(dir);
		if (!outputDir.exists()) {
			outputDir.mkdir();

		}

		try {
			//Bot bot = (Bot)simpleSim.bucketbots[0];
			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_BUCKETS), convertToJsonBucket(simpleSim.getBuckets()));
//			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_BOTS), simpleSim.getRobots());
//			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_INVENTORY), simpleSim.getLetterStations());
//			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_PICKSTATION), simpleSim.getWordStations());
//			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, "params.json"), simpleSim.getParams());
//
//			List<Circle> unusedBucketStorageLocations = simpleSim.bucketbotManager.getUnusedBucketStorageLocations();
//			HashMap<Bucket, Circle> usedBucketStorageLocations = simpleSim.bucketbotManager.getUsedBucketStorageLocations();
//
//			List<JsonBucketCirclePair>  jsonUsedBucketStorageLocations = new ArrayList<JsonBucketCirclePair>();
//			for (Bucket b: usedBucketStorageLocations.keySet()) {
//				jsonUsedBucketStorageLocations.add(new JsonBucketCirclePair(new JsonBucket(b), usedBucketStorageLocations.get(b)));
//
//			}
//
//			LinkedHashSet<Bucket> unusedBuckets = simpleSim.bucketbotManager.getUnusedBuckets();
//			LinkedHashSet<Bucket> usedBuckets = simpleSim.bucketbotManager.getUsedBuckets();
//			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_UNUSED_LOCATIONS), unusedBucketStorageLocations);
//			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_USED_LOCATIONS), jsonUsedBucketStorageLocations);
//
//
//			WordListBase wl = (WordListBase) simpleSim.getWordList();
//			mapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputDir, OSFConstant.JSON_FILE_WORDLIST), convertToJsonWord(wl));



		} catch (Exception e) {
			e.printStackTrace();

		}


	}

	/**Launches the Alphabet Soup simulation without user interface.
	 * @param args
	 */
	public static void main(String[] args) {

		//generateInitData(SimWorldType.SIMPLE, "initouput");
		loadFromInitData("initouput");




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


	public static List<JsonBucket> convertToJsonBucket(Bucket[] buckets) {
		List<JsonBucket> jsonBuckets = new ArrayList<JsonBucket>();
		for (Bucket bucket: buckets) {
			jsonBuckets.add(new JsonBucket(bucket));
		}

		return jsonBuckets;

	}


	public static List<String> convertWords(List<Word> words) {
		List<String> simpleWords = new ArrayList<String>();
		for (Word w: words) {
			simpleWords.add(convertLettersToString(w.getOriginalLetters()));

		}

		return simpleWords;


	}

	public static JsonWordList convertToJsonWord(WordListBase word) {
		JsonWordList jsonWord = new JsonWordList();
		jsonWord.setAvailableWords(convertWords(word.getAvailableWords()));
		jsonWord.setBaseColors(word.getBaseColors());
		jsonWord.setBaseWords(word.getBaseWords());
		jsonWord.setWords(convertWords(word.getWords()));
		jsonWord.setLetterProbabilities(word.getLetterProbabilities());
		return jsonWord;
	}


}
