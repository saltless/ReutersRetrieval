package com.IR;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {

        ReadFiles readFiles = new ReadFiles("/Users/Rex/Documents/ZJU/2015春夏/IR/Reuters/test/");
        TermForm termForm = new TermForm();
        readFiles.loadFiles(termForm);
        termForm.printTable();
    }
}
