package com.IR;

import java.util.*;

/**
 * Created by Rex on 15/6/12.
 */
public class KeyRetrieval {
    /**
     * As docID in termFrequency form is in
     * ascending order and we need to check over
     * the whole termFrequency from, docCursor
     * records docID on processing last time
     */
    private HashMap<String, Integer> docCursor;
    /**
     * Comparator for class VectorValueItem.
     * Result of sort will be in descending
     * order
     */
    Comparator vectorComp = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            VectorValueItem vec0 = (VectorValueItem)o1;
            VectorValueItem vec1 = (VectorValueItem)o2;
            if ((vec1.value - vec0.value) > 0.0000000001) return 1;
            else if ((vec0.value - vec1.value) > 0.0000000001) return -1;
            else return 0;
        }
    };

    /**
     * Transform log(e)(n) into log(base)(n)
     * @param raw n
     * @param base target base
     * @return answer
     */
    private static double changeLogarithm(double raw, double base) {
        return Math.log(raw) / Math.log(base);
    }

    /**
     * Calculate tf value. Check whether
     * a given tf is nil in case
     * fault occurs in log function.
     * @param raw given tf
     * @return zero or (1 + log tf)
     */
    private double calcTermFrequency(int raw) {
        return raw <= 0 ? 0 : (1 + changeLogarithm((double) raw, 10));
    }

    /**
     * Calculate idf value. Check whether
     * a given df is nil in case fault
     * occurs in log function.
     * @param docSetSize size of the whole doc set.
     * @param docFrequency doc frequency of a term.
     * @return zero or log(N/df)
     */
    private double calcInverseDocFrequency(int docSetSize, int docFrequency){
        return docFrequency <= 0 ? 0 : changeLogarithm((double)docSetSize / (double)docFrequency, 10);
    }

    /**
     * Calculate cosine of two given vectors
     * @param queryVector query as an evaluation vector
     * @param docVector doc as an evaluation vector
     * @param docLength the length of doc, for normalizing
     * @return cosine of given vectors
     */
    private double vectorEvaluation(HashMap<String, Integer> queryVector, HashMap<String, Integer> docVector, HashMap<String, Double> iDocFrequency,double docLength) {
        double cosine = 0.0;
        double queryLength = 0.0;
        for (String term : queryVector.keySet()) {
//            System.out.print(queryVector.get(term) + ":" + docVector.get(term) + " ");
            cosine += calcTermFrequency(queryVector.get(term)) * calcTermFrequency(docVector.get(term)) * iDocFrequency.get(term);
            queryLength += 1.0 * Math.pow(queryVector.get(term), 2);
        }
        if ((0 == queryLength) || (0 == docLength)) return ConstValues.DIVIDED_BY_ZERO;
        return cosine / Math.sqrt(queryLength * docLength);
    }

    /**
     * Calculate the corresponding evaluation vector
     * of given query term set
     * @param termSet raw term set
     * @return evaluation vector of given query term set
     */
    private HashMap<String, Integer> getQueryVector(LinkedList<ParsedTermItem> termSet) {
        HashMap<String, Integer> queryVector = new HashMap<String, Integer>();
        for (ParsedTermItem term : termSet) {
            if (null == queryVector.get(term.term))
                queryVector.put(term.term, 1);
            else queryVector.put(term.term, queryVector.get(term.term) + 1);
        }
        return queryVector;
    }

    /**
     * Calculate the corresponding evaluation vector
     * of given doc term set
     * @param termForm term frequency form
     * @param docID docID on processing
     * @param queryVector query vector on processing
     * @return evaluation vector of given doc term set
     */
    private HashMap<String, Integer> getDocVector(TermForm termForm, int docID, HashMap<String, Integer> queryVector) {
        HashMap<String, Integer> docVector = new HashMap<String, Integer>();
        for (String term : queryVector.keySet()) {
            docVector.put(term, 0);
            if (null == docCursor.get(term)) {
                if (docID == termForm.getTermFrequencyDocID(term, 0)){
                    docCursor.put(term, 0);
                    docVector.put(term, termForm.getTermFrequency(term, docCursor.get(term)));
                }
            } else if (docCursor.get(term) + 1 < termForm.getDocFrequency(term)){
                if (docID == termForm.getTermFrequencyDocID(term, docCursor.get(term) + 1)){
                    docCursor.put(term, docCursor.get(term) + 1);
                    docVector.put(term, termForm.getTermFrequency(term, docCursor.get(term)));
                }
            }
        }
        return  docVector;
    }

    /**
     * Get idf vector.
     * @param termForm termForm contains tf table,
     *                 in which we can get df.
     * @param queryVector query vector
     * @param docSetSize size of total doc set
     * @return idf vector
     */
    private HashMap<String, Double> getInverseDocFrequency(TermForm termForm, HashMap<String, Integer> queryVector, int docSetSize){

        HashMap<String, Double> iDocFrequency = new HashMap<String, Double>();
        for (String term : queryVector.keySet()){
            iDocFrequency.put(term, calcInverseDocFrequency(docSetSize, termForm.getDocFrequency(term)));
        }
        return iDocFrequency;
    }

    /**
     * Processing of retrieval, including reading
     * from screen, calculation and print out on
     * screen
     * @param termForm term frequency on processing
     * @param readFiles file list
     */
    public RunningState processRetrieval(TermForm termForm, ReadFiles readFiles) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("\n[Search Key]: ");
            StringBuilder searchKey = new StringBuilder(scanner.nextLine());
            if (searchKey.toString().equals(ConstValues.EXIT_CODE)) return RunningState.EXIT;
            else if (searchKey.toString().equals(ConstValues.BOOL_CODE)) {
                System.out.println("\nSwitch into BOOLEAN retrieval mode? [y/n]");
                String det = scanner.nextLine();
                if (det.toLowerCase().equals(ConstValues.CONFIRM_CODE)) return RunningState.BOOL_RETRIEVAL;
            } else if (searchKey.toString().equals(ConstValues.KEY_CODE)) {
                System.out.println("\nYou are now in FUZZY retrieval mode.");
            } else if (searchKey.toString().split(" ")[0].equals(ConstValues.FILE_CODE)){
                if (searchKey.toString().split(" ").length > 2) System.out.println("\nToo many arguments!");
                else try {
                    readFiles.printFiles(Integer.parseInt(searchKey.toString().split(" ")[1]));
                } catch (NumberFormatException e){
                    System.out.println("\nPlease input an integer as the argument ~f.");
                }
            } else {
                System.out.println("\n[Search Result]: ");
                docCursor = new HashMap<String, Integer>();
                LinkedList<ParsedTermItem> termSet = TermParser.parseArticle(searchKey.append("\n"));
                HashMap<String, Integer> queryVector = this.getQueryVector(termSet);
                ArrayList<VectorValueItem> evaluationList = new ArrayList<VectorValueItem>();
                HashMap<String, Double> iDocFrequency = this.getInverseDocFrequency(termForm, queryVector, readFiles.fileList.size());
//                for (String term : iDocFrequency.keySet()) {
//                    System.out.println(term + ": " + iDocFrequency.get(term));
//                }
                for (int docID : readFiles.fileList) {
                    HashMap<String, Integer> docVector = this.getDocVector(termForm, docID, queryVector);
                    double vectorValue = this.vectorEvaluation(queryVector, docVector, iDocFrequency, termForm.getDocLength(docID));
//                    System.out.println(docID + ": " + vectorValue);
                    if (ConstValues.DIVIDED_BY_ZERO != vectorValue) {
//                        System.out.println("*" + termForm.getAdditionalGrade(docID) + "*");
                        evaluationList.add(new VectorValueItem(docID, vectorValue + termForm.getAdditionalGrade(docID)));
//                        evaluationList.add(new VectorValueItem(docID, vectorValue));
                    }
                }
                Collections.sort(evaluationList, vectorComp);
                Boolean isFound = false;
                for (int ans = 0; ans < (ConstValues.TOTAL_SHOWING_NUM < evaluationList.size() ? ConstValues.TOTAL_SHOWING_NUM : evaluationList.size()); ans++) {
                    if (evaluationList.get(ans).value > 0) {
                        isFound = true;
//                        System.out.println(evaluationList.get(ans).docID + ": " + evaluationList.get(ans).value);
                        readFiles.printFiles(evaluationList.get(ans).docID);
                    }
                }
                if (!isFound) System.out.println("\nNo answer found!");
            }
        }
    }
}
