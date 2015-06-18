package com.IR;

import java.lang.Math;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * The invert-index compressor
 * @author Soap
 */
public class IndexCompressor {

    static public void compress(TermForm termForm, String filename) {
        Byte[] binaryCode = encode(termForm);
        byte[] compressed;
        if (binaryCode.length % 8 == 0)
            compressed = new byte[binaryCode.length / 8];
        else
            compressed = new byte[binaryCode.length / 8 + 1];
        for (int i = 0; i < binaryCode.length; ++i) {
            int bit = i % 8;
            switch (bit) {
                case 0: compressed[i / 8] |= (bit == 1) ? 0x10000000 : 0x00000000; break;
                case 1: compressed[i / 8] |= (bit == 1) ? 0x01000000 : 0x00000000; break;
                case 2: compressed[i / 8] |= (bit == 1) ? 0x00100000 : 0x00000000; break;
                case 3: compressed[i / 8] |= (bit == 1) ? 0x00010000 : 0x00000000; break;
                case 4: compressed[i / 8] |= (bit == 1) ? 0x00001000 : 0x00000000; break;
                case 5: compressed[i / 8] |= (bit == 1) ? 0x00000100 : 0x00000000; break;
                case 6: compressed[i / 8] |= (bit == 1) ? 0x00000010 : 0x00000000; break;
                case 7: compressed[i / 8] |= (bit == 1) ? 0x00000001 : 0x00000000; break;
            }
        }
        
        try {
            FileOutputStream fos = new FileOutputStream(new File(filename));
            fos.write(compressed);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public TermForm uncompress(String filename) {
        return null;
    }

    static private Byte[] encode(TermForm termForm) {
        ArrayList<Byte> binary = new ArrayList<Byte>();
        Map<String, LinkedList<DocAppearItem>> indexSet = termForm.getDocAppearPosition();
        Map<String, ArrayList<TermFreqItem>> termFreqSet = termForm.getTermFrequency();
        for (String word : indexSet.keySet()) {
            ArrayList<Byte> wordLength = encodeGamma(word.length());
            binary.addAll(wordLength);
            for (int i = 0; i < word.length(); ++i) {
                char c = word.charAt(i);
                binary.addAll(encodeGamma(c));
            }
            
            ArrayList<TermFreqItem> tfs = termFreqSet.get(word);
            LinkedList<DocAppearItem> docs = indexSet.get(word);
            Iterator<DocAppearItem> docIt = docs.iterator();
            Iterator<TermFreqItem> tfIt = tfs.iterator();
            
            int df = tfs.size();
            binary.addAll(encodeGamma(df));
            
            int lastDocID = 0;
            while (tfIt.hasNext() == true) {
                TermFreqItem tfItem = tfIt.next();
                int docID = tfItem.docID - lastDocID;
                
                binary.addAll(encodeGamma(docID));
                
                int tf = tfItem.freq;
                for (int i = 0; i < tf; ++i) {
                    DocAppearItem docAppearItem = docIt.next();
                    if (docID != docAppearItem.docID - lastDocID) {
                        System.out.println("Inconsistent DocAppearItem and TermFreqItem: " + word);
                        return null;
                    }
                    int docPos = docAppearItem.docPos;
                    binary.addAll(encodeGamma(docPos));
                }
                lastDocID = tfItem.docID;
            }
        }
        return (Byte[])binary.toArray();
    }

    /**
     * Encode integer to gamma code
     * @param num The integer to be encoded
     * @return The byte array of gamma code, which is in big-endian mode
     */
    static private ArrayList<Byte> encodeGamma(int num) {
        if (num == 1) {
            ArrayList<Byte> gamma = new ArrayList<Byte>();
            gamma.add((byte)0);
            return gamma;
        }

        int length = log(2, num);
        ArrayList<Byte> gamma = new ArrayList<Byte>();
        gamma.add(2*length + 1, (byte)0);
        int base = 1;
        for (int i = 0; i < length; ++i) {
            base <<= 1;
            gamma.set(i, (byte)1);
        }
        gamma.set(length, (byte)0);
        byte[] offset = intToBinary(num - base, length);
        for (int i = length + 1; i < 2*length + 1; ++i)
            gamma.set(1, offset[i - length - 1]);
        return gamma;
    }

    /**
     * Decode gamma code back to integer
     * @param num The gamma code to be decoded
     * @return The origin integer
     */
    static private int decodeGamma(ArrayList<Byte> gamma) {
        int num = 1;
        int cursor = 0;
        while (gamma.get(cursor++) == 1)
            num <<= 1;
        int length = cursor - 1;
        byte[] offset = new byte[length];
        for (int i = 0; i < length; ++i)
            offset[i] = gamma.get(length + i + 1);
        num += binaryToInt(offset);
        return num;
    }

    /**
     * Encode integer to binary code
     * @param num The integer to be encoded
     * @param length The length of the binary code
     * @return The byte array of binary code, which is in big-endian mode
     */
    static private byte[] intToBinary(int num, int length) {
        byte[] binary = new byte[length];
        int cursor = length - 1;
        do {
            binary[cursor--] = (byte)(num % 2);
            num >>= 1;
        } while (num != 0);
        return binary;
    }

    /**
     * Decode binary code to integer
     * @param binary The integer to be encoded
     * @return The byte array of binary code, which is in big-endian mode
     */
    static private int binaryToInt(byte[] binary) {
        int num = 0;
        for (int i = 0; i < binary.length; ++i) {
            num <<= 1;
            num += binary[i];
        }
        return num;
    }

    static private int log(int base, int original) {
        return (int)(Math.log(original) / Math.log(base));
    }

    public static void main(String[] args) {
        byte[] gamma;

        gamma = encodeGamma(1);
        for (byte b : gamma)
            System.out.print(b);
        System.out.println(" " + decodeGamma(gamma));

        gamma = encodeGamma(2);
        for (byte b : gamma)
            System.out.print(b);
        System.out.println(" " + decodeGamma(gamma));

        gamma = encodeGamma(3);
        for (byte b : gamma)
            System.out.print(b);
        System.out.println(" " + decodeGamma(gamma));

        gamma = encodeGamma(4);
        for (byte b : gamma)
            System.out.print(b);
        System.out.println(" " + decodeGamma(gamma));

        gamma = encodeGamma(9);
        for (byte b : gamma)
            System.out.print(b);
        System.out.println(" " + decodeGamma(gamma));

        gamma = encodeGamma(13);
        for (byte b : gamma)
            System.out.print(b);
        System.out.println(" " + decodeGamma(gamma));

        gamma = encodeGamma(24);
        for (byte b : gamma)
            System.out.print(b);
        System.out.println(" " + decodeGamma(gamma));

        gamma = encodeGamma(511);
        for (byte b : gamma)
            System.out.print(b);
        System.out.println(" " + decodeGamma(gamma));

        gamma = encodeGamma(1025);
        for (byte b : gamma)
            System.out.print(b);
        System.out.println(" " + decodeGamma(gamma));
    }
}
