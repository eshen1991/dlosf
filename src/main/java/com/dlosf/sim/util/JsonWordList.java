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

    public List<String> words = new ArrayList<String>();
    public List<String> availableWords = new ArrayList<String>();

    protected String baseWords[];
    protected List<LetterColor> baseColors;
    protected HashMap<Character, Float> letterProbabilities;

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

    public String[] getBaseWords() {
        return baseWords;
    }

    public void setBaseWords(String[] baseWords) {
        this.baseWords = baseWords;
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
