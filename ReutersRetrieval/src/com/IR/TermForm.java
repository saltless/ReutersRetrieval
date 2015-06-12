package com.IR;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Rex on 15/6/11.
 */

public class TermForm {

    private HashMap<String, Integer> docuFrequency = new HashMap<String, Integer>();
    private HashMap<String, ArrayList<Integer>> termFrequency = new HashMap<String, ArrayList<Integer>>();

    /**
     * Add term into tf table.
     * If term does not exist,
     * create it.
     * @param docID
     * @param rawString
     */
    public void addTerm(int docID, String rawString){

        if (null == termFrequency.get(rawString)){
            docuFrequency.put(rawString, 1);
            termFrequency.put(rawString, new ArrayList<Integer>());
            termFrequency.get(rawString).set(docID, 1);
        } else {
            int currFrequency = termFrequency.get(rawString).get(docID);
            if (0 == currFrequency) {
                int currDocFrequency = docuFrequency.get(rawString);
                docuFrequency.put(rawString, currDocFrequency + 1);
            }
            termFrequency.get(rawString).set(docID, currFrequency + 1);
        }
    }

    /**
     * Test whether a chatacter is a digit/letter
     * or not.
     * @param raw
     * @return
     */
    private boolean isOthers(char raw){

        if (raw >= '0' && raw <= '9') return false;
        if (raw >= 'a' && raw <= 'z') return false;
        if (raw >= 'A' && raw <= 'Z') return false;
        return true;
    }

    /**
     * Remove all characters other than
     * digit/letter.
     * @param raw
     * @return
     */
    private String removeOthers(String raw){

        StringBuilder str = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            if (!this.isOthers(raw.charAt(i))) str.append(raw.charAt(i));
        }
        return new String(str);
    }

    /**
     * Parse term from given string.
     * Strategy:
     *  1, [digit]+'/'[digit]+                              =>      [digit]+/[digit]+
     *  2, [digit]*[chara]+[digit]*'/'[digit]*[chara]*      =>      front, end
     *  3, ([^'/'others]*[digit]+)*                         =>      as origin
     *  4, ([^'/'others]*[digit]*[chara]+)*                 =>      ([digit]*[chara]+)*
     * @param docID
     * @param digitAppear
     * @param letteAppear
     * @param slashAppear
     * @param raw
     */
    public void parseTerm(int docID, boolean digitAppear, boolean letteAppear, boolean slashAppear, StringBuilder raw){

        String rawString = new String(raw);
        if (slashAppear) {
            if (!letteAppear) {     //CASE: 08/07/1998
                addTerm(docID, rawString);
            } else {                //CASE: man/woman, X5/X6
                String rawSplit[] = rawString.split("/");
                for (String itemStr : rawSplit) {
                    addTerm(docID, itemStr);
                }
            }
        } else if (digitAppear) {
            if (!letteAppear) {     //CASE: 123.234/22
                addTerm(docID, rawString);
            } else {                //CASE: U.S.A
                addTerm(docID, this.removeOthers(rawString));
            }
        } else if (letteAppear) {
            addTerm(docID, this.removeOthers(rawString));
        }
    }

    public void printTable(){
        for (String term : termFrequency.keySet()){
            System.out.println(term);
        }
    }
}
