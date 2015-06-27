package com.IR;

//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;

import java.util.LinkedList;

public class Main {

    public static void main(String[] args) {

        ReadFiles readFiles = new ReadFiles(ConstValues.BASE_DIR);
        readFiles.getFileList(ConstValues.FILE_DIR);
        TermForm termForm = new TermForm();
        KeyRetrieval keyRetrieval = new KeyRetrieval();
//        readFiles.loadFiles(termForm);
//        termForm.printTable();
//
//        IndexCompressor.compress(termForm, ConstValues.INDEX_DIR);
        termForm = IndexCompressor.uncompress(ConstValues.INDEX_DIR);
        termForm.printTable();

        keyRetrieval.processRetrieval(termForm, readFiles);
    }
}
