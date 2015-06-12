package com.IR;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) {

        ReadFiles readFiles = new ReadFiles(ConstValues.BASE_DIR);
        TermForm termForm = new TermForm();
        readFiles.loadFiles(termForm);
        termForm.printTable();
    }
}
