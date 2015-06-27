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
            if (!isOthers(raw.charAt(i))) str.append(raw.charAt(i));
        }
        return new String(str);
    }

    private static LinkedList<String> separateTermsByNonChaOrNum(String rawString){
        LinkedList<String> result = new LinkedList<String>();
        int counter = 0;
        StringBuilder str = new StringBuilder();
        for (int cursor = 0; cursor < rawString.length(); cursor++){
            if (isOthers(rawString.charAt(cursor))){
                if (str.length() > 0) {
                    counter++;
                    result.addLast(str.toString().toLowerCase());
                    str.delete(0, str.length());
                }
            } else str.append(rawString.charAt(cursor));
        }
        if (str.length() > 0) {
            counter++;
            result.addLast(str.toString().toLowerCase());
        }
        if (counter > 1) result.addLast(removeOthers(rawString).toLowerCase());
        return result;
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
                    if (removeOthers(itemStr).length() > 0)
                        for (String str: separateTermsByNonChaOrNum(itemStr)) parsedTerm.addLast(str);
                }
            }
        } else if (digitAppear) {
            if (!letteAppear) {     //CASE: 123.234/22
                parsedTerm.addLast(rawString);
            } else {                //CASE: U.S.A
                for (String str: separateTermsByNonChaOrNum(rawString)) parsedTerm.addLast(str);
            }
        } else if (letteAppear) {  //CASE: U.S.A
            for (String str: separateTermsByNonChaOrNum(rawString)) parsedTerm.addLast(str);
        }
        return parsedTerm;
    }

    /**
     * Parse a given article into terms. Bi-word used.
     * e,g. 'a b c' => 'a' 'b' 'c' 'a b' 'b c'
     * @param article article input from file
     * @return terms and their position in article
     */
    public static LinkedList<ParsedTermItem> parseArticle(StringBuilder article) {

        LinkedList<ParsedTermItem> parsedArticle = new LinkedList<ParsedTermItem>();
        StringBuilder prevTerm = new StringBuilder("");
        removeBrackets(article);

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
                    LinkedList<String> parsedTerm = parseTerm(digitAppear, letteAppear, slashAppear, term);
                    for (String termItem : parsedTerm){
                        parsedArticle.addLast(new ParsedTermItem(termItem, ++docPos));
                        if (prevTerm.length() > 0) {
                            prevTerm.append(" ").append(termItem);
                            parsedArticle.addLast(new ParsedTermItem(prevTerm.toString(), docPos - 1));
                        }
                        prevTerm.delete(0, prevTerm.length()).append(termItem);
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
