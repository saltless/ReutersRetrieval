package com.IR;

/**
 * Created by Rex on 15/6/12.
 */
public class ParsedTermItem {

    public String term;
    public int docPos = 0;

    public ParsedTermItem(String term, int docPos){

        this.term = term;
        this.docPos = docPos;
    }
}
