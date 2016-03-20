package com.dlosf.sim.util;

import alphabetsoup.framework.LetterColor;
import alphabetsoup.framework.Word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by eshen on 10/5/15.
 */
public class JsonWordList {

    private List<String> words = new ArrayList<String>();
    private List<String> availableWords = new ArrayList<String>();


    private List<LetterColor> baseColors;
    private HashMap<Character, Float> letterProbabilities;

    public JsonWordList() {
    }

    public List<String> getWords() {
        return words;
    }

    public void setWords(List<String> words) {
        this.words = words;
    }

    public List<String> getAvailableWords() {
        return availableWords;
    }

    public void setAvailableWords(List<String> availableWords) {
        this.availableWords = availableWords;
    }



    public List<LetterColor> getBaseColors() {
        return baseColors;
    }

    public void setBaseColors(List<LetterColor> baseColors) {
        this.baseColors = baseColors;
    }

    public HashMap<Character, Float> getLetterProbabilities() {
        return letterProbabilities;
    }

    public void setLetterProbabilities(HashMap<Character, Float> letterProbabilities) {
        this.letterProbabilities = letterProbabilities;
    }
}
