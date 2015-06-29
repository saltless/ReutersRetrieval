package com.IR;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Rex on 15/6/11.
 */

public class ReadFiles {

    private String baseDir;
    public LinkedList<Integer> fileList = new LinkedList<Integer>();
    /**
     * Construct function
     * @param dir Given base dir
     */
    public ReadFiles(String dir){
        baseDir = dir;
    }

    /**
     * Get pre-processed file list.
     * @param dir Directory of file list.
     */
    public void getFileList(String dir){
        File file = new File(String.valueOf(dir));
        if (file.exists()) try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String next;
            while ((next = reader.readLine()) != null)
                fileList.addLast(Integer.parseInt(next));
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        } else {
            System.out.println("File list not found.");
        }
    }
    /**
     * Load data from files, and check whether it
     * contains letter/digit/slash sign.
     * @param termForm info forms needed
     */
    public void loadFiles(TermForm termForm) {
        LinkedList<Integer> newFileList = new LinkedList<Integer>();
        for (int i : fileList){
            String dir = baseDir + i + ".html";
            File file = new File(String.valueOf(dir));
            if (file.exists()) try {
                System.out.println("[ Processing file " + i + ".html ]");
                newFileList.add(i);
                InputStream inputStream = new FileInputStream(file);
                StringBuilder article = new StringBuilder();
                int next;
                while ((next = inputStream.read()) != -1) article.append((char) next);
                LinkedList<ParsedTermItem> parsedArticle = TermParser.parseArticle(article);
                termForm.addTerm(i, parsedArticle);
                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        fileList = newFileList;
    }

    public void printFiles(int docID){

        String dir = baseDir + docID + ".html";
        File file = new File(String.valueOf(dir));
        if (file.exists()) try {
            System.out.println("\n================== FILE: " + docID + ".html ==================\n");
            InputStream inputStream = new FileInputStream(file);
            StringBuilder article = new StringBuilder();
            int next;
            while ((next = inputStream.read()) != -1) article.append((char) next);
            System.out.println(article);
            inputStream.close();
        } catch (Exception e){
            e.printStackTrace();
        } else {
            System.out.println("FILE: " + docID + ".html NOT FOUND!");
        }
    }
}
