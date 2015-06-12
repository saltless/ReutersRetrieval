package com.IR;

import java.io.*;

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
     * Load data from files
     * @param termForm
     */
    public void loadFiles(TermForm termForm) {

        String dir = baseDir;
        for (int i = 1; i <= ConstValues.TOTAL_FILE_AMOUNT; i++){
            dir += i + ".html";
            File file = new File(String.valueOf(dir));
            if (file.exists()) try {
                InputStream inputStream = new FileInputStream(file);
                int next;
                boolean digitAppear = false;
                boolean charaAppear = false;
                boolean slashAppear = false;
                StringBuilder term = new StringBuilder();
                while ((next = inputStream.read()) != -1) {
                    char nextChar = (char)next;
                    if (nextChar == ' '){
                        termForm.parseTerm(i, digitAppear, charaAppear, slashAppear, term);
                        term = new StringBuilder();
                        digitAppear = false;
                        charaAppear = false;
                    } else {
                        if ((nextChar >= '0') && (nextChar <= '9')){
                            digitAppear = true;
                        } else if (((nextChar >= 'a') && (nextChar <= 'z')) || ((nextChar >= 'A') && (nextChar <= 'Z'))){
                            charaAppear = true;
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
