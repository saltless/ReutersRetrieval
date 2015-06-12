package com.IR;

import java.util.LinkedList;

/**
 * Created by Rex on 15/6/12.
 */
public class TermParser {

    /**
     * Replace brackets with space.
     * @param raw
     */
    private static void removeBrackets(StringBuilder raw){

        LinkedList<Integer> parenthesisList = new LinkedList<Integer>();     //()
        LinkedList<Integer>bracketsList = new LinkedList<Integer>();        //[]
        LinkedList<Integer>bracesList = new LinkedList<Integer>();          //{}
        for (int i = 0; i < raw.length(); i++){
            switch (raw.charAt(i)){
                case '(':{
                    parenthesisList.addLast(i);
                    break;
                }
                case '[':{
                    bracketsList.addLast(i);
                    break;
                }
                case '{':{
                    bracesList.addLast(i);
                    break;
                }
                case ')':{
                    if (parenthesisList.size() > 0){
                        raw.setCharAt(parenthesisList.getLast(), ' ');
                        raw.setCharAt(i, ' ');
                        parenthesisList.removeLast();
                    }
                    break;
                }
                case ']':{
                    if (bracketsList.size() > 0){
                        raw.setCharAt(bracketsList.getLast(), ' ');
                        raw.setCharAt(i, ' ');
                        bracketsList.removeLast();
                    }
                    break;
                }
                case '}': {
                    if (bracesList.size() > 0){
                        raw.setCharAt(bracesList.getLast(), ' ');
                        raw.setCharAt(i, ' ');
                        bracesList.removeLast();
                    }
                    break;
                }
                default: break;
            }
        }
    }

    /**
     * Test whether a chatacter is a digit/letter
     * or not.
     * @param raw
     * @return
     */
    private static boolean isOthers(char raw){

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
    private static String removeOthers(String raw){

        StringBuilder str = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            if (!TermParser.isOthers(raw.charAt(i))) str.append(raw.charAt(i));
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
     * @param digitAppear
     * @param letteAppear
     * @param slashAppear
     * @param raw
     */
    private static LinkedList<String> parseTerm(boolean digitAppear, boolean letteAppear, boolean slashAppear, StringBuilder raw){

        String rawString = new String(raw);
        LinkedList<String> parsedTerm = new LinkedList<String>();
        if (slashAppear) {
            if (!letteAppear) {     //CASE: 08/07/1998
                parsedTerm.addLast(rawString);
            } else {                //CASE: man/woman, X5/X6
                String rawSplit[] = rawString.split("/");
                for (String itemStr : rawSplit) {
                    if (TermParser.removeOthers(itemStr).length() > 0)
                        parsedTerm.addLast(TermParser.removeOthers(itemStr).toLowerCase());
                }
            }
        } else if (digitAppear) {
            if (!letteAppear) {     //CASE: 123.234/22
                parsedTerm.addLast(rawString);
            } else {                //CASE: U.S.A
                parsedTerm.addLast(TermParser.removeOthers(rawString).toLowerCase());
            }
        } else if (letteAppear) {
            parsedTerm.addLast(TermParser.removeOthers(rawString).toLowerCase());
        }
        return parsedTerm;
    }

    /**
     * Parse a given article into terms
     * @param article article input from file
     * @return terms and their position in article
     */
    public static LinkedList<ParsedTermItem> parseArticle(StringBuilder article) {

        LinkedList<ParsedTermItem> parsedArticle = new LinkedList<ParsedTermItem>();
        TermParser.removeBrackets(article);

        boolean digitAppear = false;
        boolean letteAppear = false;
        boolean slashAppear = false;
        int docPos = 0;
        StringBuilder term = new StringBuilder();
        for (int cursor = 0; cursor < article.length(); cursor++) {
            char nextChar = article.charAt(cursor);
//                    System.out.println();
//                    System.out.print(nextChar + "(" + (int)nextChar + "):");
            if (nextChar == ' ' || nextChar == '\n'){
                if (term.length() > 0) {
//                            System.out.print("<" + docPos + ">" + term);
                    LinkedList<String> parsedTerm = TermParser.parseTerm(digitAppear, letteAppear, slashAppear, term);
                    for (String termItem : parsedTerm){
                        parsedArticle.addLast(new ParsedTermItem(termItem, ++docPos));
                    }
                }
                term = new StringBuilder();
                digitAppear = false;
                letteAppear = false;
                slashAppear = false;
            } else {
                if ((nextChar >= '0') && (nextChar <= '9')){
                    digitAppear = true;
                } else if (((nextChar >= 'a') && (nextChar <= 'z')) || ((nextChar >= 'A') && (nextChar <= 'Z'))){
                    letteAppear = true;
                } else if (nextChar == '/'){
                    slashAppear = true;
                }
                term.append(nextChar);
            }
        }
        return parsedArticle;
    }
}
