package com.IR;

import java.io.*;
import java.util.LinkedList;

/**
 * Created by Rex on 15/6/11.
 */

public class ReadFiles {

    private String baseDir;
    public LinkedList<Integer> fileList = new LinkedList<Integer>();
    /**
     * Construct function
     * @param dir
     */
    public ReadFiles(String dir){
        baseDir = dir;
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
                fileList.addLast(i);
                InputStream inputStream = new FileInputStream(file);
                int next;
                StringBuilder article = new StringBuilder();
                while ((next = inputStream.read()) != -1) article.append((char) next);
                LinkedList<ParsedTermItem> parsedArticle = TermParser.parseArticle(article);
                termForm.addTerm(i, parsedArticle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
