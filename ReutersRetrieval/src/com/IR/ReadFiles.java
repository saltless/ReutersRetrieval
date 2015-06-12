package com.IR;

import java.io.*;
import java.util.LinkedList;

/**
 * Created by Rex on 15/6/11.
 */

public class ReadFiles {

    private String baseDir;

    /**
     * Construct function
     * @param dir
     */
    public ReadFiles(String dir){
        baseDir = dir;
    }

    /**
     * Replace brackets with space.
     * @param raw
     */
    private void removeBrackets(StringBuilder raw){

        LinkedList<Integer>parenthesisList = new LinkedList<Integer>();     //()
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
     * Load data from files, and check whether it
     * contains letter/digit/slash sign.
     * @param termForm
     */
    public void loadFiles(TermForm termForm) {

        for (int i = 1; i <= ConstValues.TOTAL_FILE_AMOUNT; i++){
            String dir = baseDir + i + ".html";
            System.out.println("[ Processing file " + i + ".html ]");
            File file = new File(String.valueOf(dir));
            if (file.exists()) try {
                InputStream inputStream = new FileInputStream(file);
                int next;
                StringBuilder article = new StringBuilder();
                while ((next = inputStream.read()) != -1) article.append((char) next);
                this.removeBrackets(article);

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
                            docPos++;
//                            System.out.print("<" + docPos + ">" + term);
                            termForm.parseTerm(i, docPos, digitAppear, letteAppear, slashAppear, term);
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
                        } else if (nextChar == '\\'){
                            slashAppear = true;
                        }
                        term.append(nextChar);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
