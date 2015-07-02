package com.IR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class BoolRetrieval {
	int index;
	HashMap<String, ArrayList<TermFreqItem>> termDoc;
	
	public RunningState processRetrieval(TermForm termForm, ReadFiles readFiles) {
		Scanner scanner = new Scanner(System.in);
		termDoc = termForm.getTermFrequency();
        while (true) {
            System.out.print("\n[Search Key]: ");
            StringBuilder searchKey = new StringBuilder(scanner.nextLine());
			if (searchKey.toString().equals(ConstValues.EXIT_CODE)) return RunningState.EXIT;
			else if (searchKey.toString().equals(ConstValues.KEY_CODE)) {
				System.out.println("\nSwitch into FUZZY retrieval mode? [y/n]");
				String det = scanner.nextLine();
				if (det.toLowerCase().equals(ConstValues.CONFIRM_CODE)) return RunningState.KEY_RETRIEVAL;
			} else if (searchKey.toString().equals(ConstValues.BOOL_CODE)) {
				System.out.println("\nYou are now in BOOLEAN retrieval mode.");
			} else if (searchKey.toString().split(" ")[0].equals(ConstValues.FILE_CODE)){
				if (searchKey.toString().split(" ").length > 2) System.out.println("\nToo many arguments!");
				else try {
					readFiles.printFiles(Integer.parseInt(searchKey.toString().split(" ")[1]));
				} catch (NumberFormatException e){
					System.out.println("\nPlease input an integer as the argument ~f.");
				}
			} else {
				System.out.println("\n[Search Result]: ");
				ArrayList<String> keys = ParseKeys(searchKey);
				index = 0;
				ArrayList<TermFreqItem> answer = doSearch(keys);
				if (answer != null && answer.size() > 0) {
					for (int ans = 0; ans < (ConstValues.TOTAL_SHOWING_NUM < answer.size() ? ConstValues.TOTAL_SHOWING_NUM : answer.size()); ans++) {
						if (answer.get(ans).docID > 0) {
							readFiles.printFiles(answer.get(ans).docID);
						}
					}
					System.out.print("\nAll Files Found: ");
					for (TermFreqItem ans : answer) System.out.print(ans.docID + " ");
					System.out.println("\n");
				} else System.out.println("\nNo answer found!");
			}
        }
	}

	ArrayList<TermFreqItem>  doSearch(ArrayList<String> keys) {
		ArrayList<TermFreqItem> nowResult = new ArrayList<TermFreqItem>();
		int op = -1;
		while (true) {
			if (index >= keys.size())	break;
			if (keys.get(index).equals("(")) {
				index++;
				nowResult = merge(nowResult,op,doSearch(keys));
			}
			if (index >= keys.size())	break;
			if (keys.get(index).equals(")")) {
				index++;
				return nowResult;
			}
			if (index >= keys.size())	break;
			if (keys.get(index).equals("AND") || keys.get(index).equals("OR")) {
				if (keys.get(index).equals("AND"))
					op = 0;
				else
					op = 1;
				index++;
				if (index < keys.size()  && keys.get(index).equals("NOT")) {
					index ++;
					op += 2;
				}
			}
			else {
				if (op == -1) {
					nowResult = termDoc.get(keys.get(index++));//keys.get(index++);
				}
				else {
					nowResult = merge(nowResult,op,termDoc.get(keys.get(index++)));
				}
			}
		}
		return nowResult;
	}

	private ArrayList<TermFreqItem> merge(ArrayList<TermFreqItem> nowResult, int op, ArrayList<TermFreqItem> key) {
		if (op == -1)
			return key;
		ArrayList<TermFreqItem> tmp = new ArrayList<TermFreqItem>();
		int p = 0,q = 0;
		int sizep = 0,sizeq = 0;
		if (nowResult != null)
			sizep = nowResult.size();
		if (key != null)
			sizeq = key.size();
		switch (op) {
		case 0://AND
			while (true) {
				if (p >= sizep || q >= sizeq)
					break;
				TermFreqItem docp = nowResult.get(p);
				TermFreqItem docq = key.get(q);
				if (docp.docID == docq.docID) {
					tmp.add(new TermFreqItem(docp.docID,docp.freq+docq.freq));
					p++;
					q++;
				}
				else if (docp.docID < docq.docID) {
					p++;
				}
				else {
					q++;
				}
			}
			break;
		case 1://OR
			while (true) {
				if (p >= sizep) {
					while (q < sizeq) 
						tmp.add(key.get(q++));
					break;
				}
				else if (q >= sizeq) {
					while (p < sizep) 
						tmp.add(nowResult.get(p++));
					break;
				}
				TermFreqItem docp = nowResult.get(p);
				TermFreqItem docq = key.get(q);
				if (docp.docID == docq.docID) {
					tmp.add(new TermFreqItem(docp.docID,docp.freq+docq.freq));
					p++;
					q++;
				}
				else if (docp.docID < docq.docID) {
					tmp.add(docp);
					p++;
				}
				else {
					tmp.add(docq);
					q++;
				}
			}
		case 2://AND NOT
			while (true) {
				if (p >= sizep) {
					break;
				}
				else if (q >= sizeq) {
					while (p < sizep) 
						tmp.add(nowResult.get(p++));
					break;
				}
				TermFreqItem docp = nowResult.get(p);
				TermFreqItem docq = key.get(q);
				if (docp.docID == docq.docID) {
					p++;
					q++;
				}
				else if (docp.docID < docq.docID) {
					tmp.add(docp);
					p++;
				}
				else {
					q++;
				}
			}
		}
		return tmp;
	}

	private ArrayList<String> ParseKeys(StringBuilder searchKey) {
		String[] token = searchKey.toString().split(" ");
		ArrayList<String> tmp = new ArrayList<String>();
		int i = 0;
		while (true) {
			if (i >= token.length)
				break;
			String a = token[i];
			while (a.charAt(0) == '(') {
				tmp.add("(");
				a = a.substring(1);
			}
			int p = tmp.size();
			while (a.charAt(a.length() - 1) == ')') {
				tmp.add(")");
				a = a.substring(0, a.length() - 1);
			}
			if (a.equals("AND") || a.equals("OR") || a.equals("NOT"))
				tmp.add(p, a);
			else {
				tmp.add(p, a.toLowerCase());
			}
			i++;
		}
		return tmp;
	}


}
