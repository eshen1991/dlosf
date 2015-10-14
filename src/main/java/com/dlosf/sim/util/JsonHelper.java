package com.dlosf.sim.util;

import alphabetsoup.base.BucketBase;
import alphabetsoup.base.LetterStationBase;
import alphabetsoup.base.WordListBase;
import alphabetsoup.base.WordStationBase;
import alphabetsoup.framework.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eshen on 10/13/15.
 */
public class JsonHelper {
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
}
