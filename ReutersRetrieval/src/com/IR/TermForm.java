package com.IR;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Rex on 15/6/11.
 */
public class TermForm {

    private HashMap<String, Integer> docuFrequency = new HashMap<String, Integer>();
    private HashMap<String, ArrayList<Integer>> termFrequency = new HashMap<String, ArrayList<Integer>>();

    public void addTerm(int docID, String rawString){
        if (null == termFrequency.get(rawString)){
            termFrequency.put(rawString, new ArrayList<Integer>());
            termFrequency.get(rawString).set(docID, 1);
        } else {
            int currFrequency = termFrequency.get(rawString).get(docID);
            termFrequency.get(rawString).set(docID, currFrequency + 1);
        }
    }

    public void parseTerm(int docID, boolean digitAppear, boolean charaAppear, boolean slashAppear, StringBuilder raw){
        String rawString = new String(raw);
        if (slashAppear) {
            if (!charaAppear) {     //CASE: 08/07/1998
                addTerm(docID, rawString);
            } else {                //CASE: man/woman, X5/X6
                String rawSplit[] = rawString.split("/");
                for (int i = 0; i < rawSplit.length; i++) {
                    addTerm(docID, rawSplit[i]);
                }
            }
        }
    }
}
