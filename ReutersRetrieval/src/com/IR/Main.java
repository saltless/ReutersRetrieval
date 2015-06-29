package com.IR;

//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
//import java.util.LinkedList;
import com.IR.util.*;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        ReadFiles readFiles = new ReadFiles(ConstValues.BASE_DIR);
        readFiles.getFileList(ConstValues.FILE_DIR);
        TermForm termForm = new TermForm();

        readFiles.loadFiles(termForm);
//        termForm.printTable();
//
        IndexCompressor.compress(termForm, ConstValues.INDEX_DIR);
        System.out.println("Loading index...\n\n");
        termForm = IndexCompressor.uncompress(ConstValues.INDEX_DIR);
//      termForm.printTable();

        System.out.println("************************************************");
        System.out.println("*                                              *");
        System.out.println("*       Welcome to the retrieval system!       *");
        System.out.println("*                                              *");
        System.out.println("*                BY:  AN LEI                   *");
        System.out.println("*                  CAO HONG                    *");
        System.out.println("*                  QIN ZHUO                    *");
        System.out.println("*                  WANG RU                     *");
        System.out.println("*                                              *");
        System.out.println("*             === INSTRUCTIONS ===             *");
        System.out.println("*                                              *");
        System.out.println("*         ~bool  :   BOOLEAN retrieval         *");
        System.out.println("*         ~fuzzy :   FUZZY retrieval           *");
        System.out.println("*         ~f num :   inspect file 'num.html'   *");
        System.out.println("*         ~exit  :   exit                      *");
        System.out.println("*                                              *");
        System.out.println("************************************************");

        KeyRetrieval keyRetrieval = new KeyRetrieval();
        BoolRetrieval boolRetrieval = new BoolRetrieval();
        RunningState processingState = RunningState.KEY_RETRIEVAL;

        while (processingState != RunningState.EXIT){
            if (processingState == RunningState.KEY_RETRIEVAL) {
                System.out.println("\n<FUZZY MODE>");
                processingState = keyRetrieval.processRetrieval(termForm, readFiles);
            } else if (processingState == RunningState.BOOL_RETRIEVAL){
                System.out.println("\n<BOOLEAN MODE>");
                processingState = boolRetrieval.processRetrieval(termForm, readFiles);
            }
        }
        System.out.println("\nGood Bye!\n");

//=============Test for stem algorithm
//        Scanner in = new Scanner(System.in);
//        System.out.print("[ in ] ");
//        String input = in.nextLine();
//        while (true) {
//            TermStem termstem = new TermStem(input);
//            System.out.println("[ ou ] " + termstem.stemmed);
//            System.out.print("[ in ] ");
//            input = in.nextLine();
//        }
    }
}
