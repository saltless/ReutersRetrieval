package com.IR;

import java.util.LinkedList;
import java.util.Scanner;

/**
 * Created by Rex on 15/6/12.
 */
public class KeyRetrieval {

    public void processRetrieval(TermForm termForm) {

        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.print("[Search Key]: ");
            StringBuilder searchKey = new StringBuilder(scanner.nextLine());
            System.out.print("[Search Result]: ");
            LinkedList<ParsedTermItem> termSet = TermParser.parseArticle(searchKey.append("\n"));
            for (ParsedTermItem item : termSet){
                System.out.println(item.term);
            }
        }
    }
}
