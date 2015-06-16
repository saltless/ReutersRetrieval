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
            if (vec0.value < vec1.value) return 1;
            else return -1;
        }
    };

    /**
     * Check whether a given tf is nil in case
     * fault occurs in log function.
     * @param raw given tf
     * @return zero or (1 + log tf)
     */
    private double calcTermFrequency(int raw) {
        return raw == 0 ? 0 : (1 + Math.log(raw));
    }

    /**
     * Calculate cosine of two given vectors
     * @param queryVector query as an evaluation vector
     * @param docVector doc as an evaluation vector
     * @param docLength the length of doc, for normalizing
     * @return cosine of given vectors
     */
    private double vectorEvaluation(HashMap<String, Integer> queryVector, HashMap<String, Integer> docVector, double docLength) {
        double cosine = 0.0;
        double queryLength = 0.0;
        for (String term : queryVector.keySet()) {
//            System.out.print(queryVector.get(term) + ":" + docVector.get(term) + " ");
            cosine += calcTermFrequency(queryVector.get(term)) * calcTermFrequency(docVector.get(term));
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
            if (null == queryVector.get(term))
                queryVector.put(term.term, 1);
            else queryVector.put(term.term, queryVector.get(term) + 1);
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
            } else if (docCursor.get(term) + 1 < termForm.getTermFrequencyDocLength(term)){
                if (docID == termForm.getTermFrequencyDocID(term, docCursor.get(term) + 1)){
                    docCursor.put(term, docCursor.get(term) + 1);
                    docVector.put(term, termForm.getTermFrequency(term, docCursor.get(term)));
                }
            }
        }
        return  docVector;
    }

    /**
     * Processing of retrieval, including reading
     * from screen, calculation and print out on
     * screen
     * @param termForm term frequency on processing
     * @param readFiles file list
     */
    public void processRetrieval(TermForm termForm, ReadFiles readFiles) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("[Search Key]: ");
            StringBuilder searchKey = new StringBuilder(scanner.nextLine());
            if (searchKey.equals("exit()")) return;
            System.out.print("[Search Result]: ");
            docCursor = new HashMap<String, Integer>();
            LinkedList<ParsedTermItem> termSet = TermParser.parseArticle(searchKey.append("\n"));
            HashMap<String, Integer> queryVector = this.getQueryVector(termSet);
            ArrayList<VectorValueItem> evaluationList = new ArrayList<VectorValueItem>();
            for (int docID : readFiles.fileList) {
                HashMap<String, Integer> docVector = this.getDocVector(termForm, docID, queryVector);
//                System.out.print("Doc " + docID + " ");
                double vectorValue = this.vectorEvaluation(queryVector, docVector, termForm.getDocLength(docID));
//                System.out.println(vectorValue);
                if (ConstValues.DIVIDED_BY_ZERO != vectorValue) {
                    evaluationList.add(new VectorValueItem(docID, vectorValue));
                }
            }
            Collections.sort(evaluationList, vectorComp);
            Boolean isFound = false;
            for (int ans = 0; ans < (4 < evaluationList.size() ? 4 : evaluationList.size()); ans++){
//                System.out.println(ans + " " + evaluationList.get(ans).value);
                if (evaluationList.get(ans).value > 0) {
                    isFound = true;
                    System.out.println("FILE: " + evaluationList.get(ans).docID + " " + evaluationList.get(ans).value);
                }
            }
            if (!isFound) System.out.println("No answer found!");
        }
    }
}
