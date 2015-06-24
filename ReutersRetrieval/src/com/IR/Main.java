package com.IR;

public class Main {

    public static void main(String[] args) {

        ReadFiles readFiles = new ReadFiles(ConstValues.BASE_DIR);
        readFiles.getFileList(ConstValues.FILE_DIR);
        TermForm termForm = new TermForm();
        KeyRetrieval keyRetrieval = new KeyRetrieval();
//        readFiles.loadFiles(termForm);
        termForm = IndexCompressor.uncompress(ConstValues.INDEX_DIR);
        termForm.printTable();
//        keyRetrieval.processRetrieval(termForm, readFiles);
    }
}
