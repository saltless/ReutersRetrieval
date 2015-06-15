package com.IR;

import java.util.*;

/**
 * Created by Rex on 15/6/12.
 */
public class KeyRetrieval {

    private HashMap<String, Integer> docCursor;
    Comparator vectorComp = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            VectorValueItem vec0 = (VectorValueItem)o1;
            VectorValueItem vec1 = (VectorValueItem)o2;
            if (vec0.value < vec1.value) return 1;
            else return -1;
        }
    };

    private double calcTermFrequency(int raw) {
        return raw == 0 ? 0 : (1 + Math.log(raw));
    }

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

    private HashMap<String, Integer> getQueryVector(LinkedList<ParsedTermItem> termSet) {
        HashMap<String, Integer> queryVector = new HashMap<String, Integer>();
        for (ParsedTermItem term : termSet) {
            if (null == queryVector.get(term))
                queryVector.put(term.term, 1);
            else queryVector.put(term.term, queryVector.get(term) + 1);
        }
        return queryVector;
    }

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
