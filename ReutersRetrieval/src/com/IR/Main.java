package com.IR;

public class Main {

    public static void main(String[] args) {
	// write your code here
        ReadFiles readFiles = new ReadFiles("/Users/Rex/Documents/ZJU/2015春夏/IR/Reuters/");
        TermForm termForm = new TermForm();
        readFiles.loadFiles(termForm);
        termForm.printTable();
    }
}
