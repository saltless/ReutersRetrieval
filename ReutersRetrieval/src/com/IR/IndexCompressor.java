package com.IR;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

/**
 * The invert-index compressor
 * @author Soap
 */
public class IndexCompressor {

    static public void compress(TermForm termForm, String filename) {
        ArrayList<Byte> uncompressed = encode(termForm);
        /*
        byte[] compressed;
        if (uncompressed.length % 8 == 0) {
            compressed = new byte[uncompressed.length / 8];
        } else {
            compressed = new byte[uncompressed.length / 8 + 1];
            compressed[compressed.length - 1] |= (0xFF >>> (uncompressed.length % 8));
        }
        for (int i = 0; i < uncompressed.length; ++i) {
            int bit = i % 8;
            switch (bit) {
                case 0: compressed[i / 8] |= ((byte)uncompressed[i] == 1 ? 0x80 : 0); break;
                case 1: compressed[i / 8] |= ((byte)uncompressed[i] == 1 ? 0x40 : 0); break;
                case 2: compressed[i / 8] |= ((byte)uncompressed[i] == 1 ? 0x20 : 0); break;
                case 3: compressed[i / 8] |= ((byte)uncompressed[i] == 1 ? 0x10 : 0); break;
                case 4: compressed[i / 8] |= ((byte)uncompressed[i] == 1 ? 0x08 : 0); break;
                case 5: compressed[i / 8] |= ((byte)uncompressed[i] == 1 ? 0x04 : 0); break;
                case 6: compressed[i / 8] |= ((byte)uncompressed[i] == 1 ? 0x02 : 0); break;
                case 7: compressed[i / 8] |= ((byte)uncompressed[i] == 1 ? 0x01 : 0); break;
            }
        }
        */
        
        // write invert-index file
        try {
            FileOutputStream fos = new FileOutputStream(filename + ".idx");
            int blockSize = 40960;
            for (int i = 0; i < uncompressed.size()/blockSize; ++i) {
                byte[] compressed = new byte[blockSize];
                for (int j = 0; j < blockSize; ++j)
                    compressed[j] = uncompressed.get(i*blockSize + j);
                fos.write(compressed);
            }
            byte[] compressed = new byte[uncompressed.size()%blockSize];
            for (int j = 0; j < uncompressed.size()%blockSize; ++j)
                compressed[j] = uncompressed.get(uncompressed.size()/blockSize*blockSize + j);
            fos.write(compressed);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // write doc-length file without compressing
        try {
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(filename + ".dl"));
            Map<Integer, Double> docLength = termForm.getDocLength();
            dos.writeInt(docLength.size());
            for (int i : docLength.keySet()) {
                dos.writeInt(i);
                dos.writeDouble(docLength.get(i));
            }
            dos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // write additional-grade file without compressing
        try {
            DataOutputStream dos = new DataOutputStream(new FileOutputStream(filename + ".ag"));
            Map<Integer, Double> additionalGrade = termForm.getAdditionalGrade();
            dos.writeInt(additionalGrade.size());
            for (int i : additionalGrade.keySet()) {
                dos.writeInt(i);
                dos.writeDouble(additionalGrade.get(i));
            }
            dos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public TermForm uncompress(String filename) {
        ArrayList<Byte> compressed = null;
        try {
            // read the invert-index file
            File file = new File(filename + ".idx");
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            long length = file.length();
            compressed = new ArrayList<Byte>();
            int blockSize = 4096;
            while (length >= blockSize) {
                byte[] bytes = new byte[blockSize];
                int count = 0;
                while (count != blockSize)
                    count += bis.read(bytes, count, blockSize - count);
                length -= blockSize;
                for (byte b : bytes)
                    compressed.add(b);
            }
            byte[] bytes = new byte[(int)length];
            int count = 0;
            while (count != length)
                count += bis.read(bytes, count, (int)length - count);
            for (byte b : bytes)
                compressed.add(b);
            bis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        if (compressed == null)
            return null;
            
        // uncompress
        /*
        ArrayList<Byte> uncompressed = new ArrayList<Byte>();
        for (byte compressedByte : compressed) {
            uncompressed.add((byte)((compressedByte & 0x80) >>> 7));
            uncompressed.add((byte)((compressedByte & 0x40) >>> 6));
            uncompressed.add((byte)((compressedByte & 0x20) >>> 5));
            uncompressed.add((byte)((compressedByte & 0x10) >>> 4));
            uncompressed.add((byte)((compressedByte & 0x08) >>> 3));
            uncompressed.add((byte)((compressedByte & 0x04) >>> 2));
            uncompressed.add((byte)((compressedByte & 0x02) >>> 1));
            uncompressed.add((byte)((compressedByte & 0x01) >>> 0));
        }
        */
        
        // decode
        ArrayList<Integer> intArray = decodeGamma(compressed);
        HashMap<String, Integer>                   docFrequency      = new HashMap<String, Integer>();
        HashMap<String, ArrayList<TermFreqItem>>   termFrequency     = new HashMap<String, ArrayList<TermFreqItem>>();
        HashMap<String, LinkedList<DocAppearItem>> docAppearPosition = new HashMap<String, LinkedList<DocAppearItem>>();
        int cursor = 0;
        while (cursor < intArray.size()) {
            int wordLength = intArray.get(cursor++);
            char[] charSequence = new char[wordLength];
            for (int i = 0; i < wordLength; ++i) {
                int ascii = intArray.get(cursor++);
                charSequence[i] = (char)ascii;
            }
            String word = new String(charSequence);
            int df = intArray.get(cursor++);
            docFrequency.put(word, df);
            termFrequency.put(word, new ArrayList<TermFreqItem>());
            docAppearPosition.put(word, new LinkedList<DocAppearItem>());
            
            int lastDocID = 0;
            for (int i = 0; i < df; ++i) {
                int docID = intArray.get(cursor++) + lastDocID;
                int tf = intArray.get(cursor++);
                TermFreqItem tfItem = new TermFreqItem(docID, tf);
                termFrequency.get(word).add(tfItem);
                
                for (int j = 0; j < tf; ++j) {
                    int docPos = intArray.get(cursor++);
                    DocAppearItem docAppearItem = new DocAppearItem(docID, docPos);
                    docAppearPosition.get(word).add(docAppearItem);
                }
                lastDocID = docID;
            }
        }
        TermForm termForm = new TermForm();
        termForm.setTermFrequency(termFrequency);
        termForm.setDocAppearPosition(docAppearPosition);

        // read the doc-length file
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(filename + ".dl"));
            HashMap<Integer, Double> docLength = new HashMap<Integer, Double>();
            int size = dis.readInt();
            for (int i = 0; i < size; ++i)
                docLength.put(dis.readInt(), dis.readDouble());
            dis.close();
            termForm.setDocLength(docLength);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // read the additional-grade file
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(filename + ".ag"));
            HashMap<Integer, Double> additionalGrade = new HashMap<Integer, Double>();
            int size = dis.readInt();
            for (int i = 0; i < size; ++i)
                additionalGrade.put(dis.readInt(), dis.readDouble());
            dis.close();
            termForm.setAdditionalGrade(additionalGrade);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return termForm;
    }

    static private int appendBits(ArrayList<Byte> binary, ArrayList<Byte> bits, int offset) {
        if (binary.isEmpty() == true)
            binary.add((byte)0xFF);
        for (byte b : bits) {
            byte B = binary.get(binary.size() - 1);
            switch (offset++) {
                case (0): binary.set(binary.size() - 1, (byte)(B & (b == 1 ? 0xFF : 0x7F))); break;
                case (1): binary.set(binary.size() - 1, (byte)(B & (b == 1 ? 0xFF : 0xBF))); break;
                case (2): binary.set(binary.size() - 1, (byte)(B & (b == 1 ? 0xFF : 0xDF))); break;
                case (3): binary.set(binary.size() - 1, (byte)(B & (b == 1 ? 0xFF : 0xEF))); break;
                case (4): binary.set(binary.size() - 1, (byte)(B & (b == 1 ? 0xFF : 0xF7))); break;
                case (5): binary.set(binary.size() - 1, (byte)(B & (b == 1 ? 0xFF : 0xFB))); break;
                case (6): binary.set(binary.size() - 1, (byte)(B & (b == 1 ? 0xFF : 0xFD))); break;
                case (7): binary.set(binary.size() - 1, (byte)(B & (b == 1 ? 0xFF : 0xFE))); break;
            }
            if (offset == 8) {
                binary.add((byte)0xFF);
                offset = 0;
            }
        }
        return offset;
    }

    static private ArrayList<Byte> encode(TermForm termForm) {
        int innerByteOffset = 0;
        ArrayList<Byte> binary = new ArrayList<Byte>();
        Map<String, LinkedList<DocAppearItem>> indexSet = termForm.getDocAppearPosition();
        Map<String, ArrayList<TermFreqItem>> termFreqSet = termForm.getTermFrequency();
        for (String word : indexSet.keySet()) {
            // encode the word
            ArrayList<Byte> wordLength = encodeGamma(word.length());
            innerByteOffset = appendBits(binary, wordLength, innerByteOffset);
            for (int i = 0; i < word.length(); ++i) {
                char c = word.charAt(i);
                innerByteOffset = appendBits(binary, encodeGamma(c), innerByteOffset);
            }
            
            ArrayList<TermFreqItem> tfs = termFreqSet.get(word);
            LinkedList<DocAppearItem> docs = indexSet.get(word);
            Iterator<TermFreqItem> tfIt = tfs.iterator();
            Iterator<DocAppearItem> docIt = docs.iterator();
            
            // encode the df
            int df = tfs.size();
            innerByteOffset = appendBits(binary, encodeGamma(df), innerByteOffset);

            // encode all the tfs
            int lastDocID = 0;
            while (tfIt.hasNext() == true) {
                TermFreqItem tfItem = tfIt.next();
                int docID = tfItem.docID - lastDocID;
                int tf = tfItem.freq;
                innerByteOffset = appendBits(binary, encodeGamma(docID), innerByteOffset);
                innerByteOffset = appendBits(binary, encodeGamma(tf), innerByteOffset);
                for (int i = 0; i < tf; ++i) {
                    DocAppearItem docAppearItem = docIt.next();
                    if (docID != docAppearItem.docID - lastDocID) {
                        System.out.println("Inconsistent DocAppearItem and TermFreqItem: " + word);
                        return null;
                    }
                    int docPos = docAppearItem.docPos;
                    innerByteOffset = appendBits(binary, encodeGamma(docPos), innerByteOffset);
                }
                lastDocID = tfItem.docID;
            }
        }
        return binary;
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
        for (int i = 0; i < 2*length + 1; ++i)
            gamma.add((byte)0);
        int base = 1;
        for (int i = 0; i < length; ++i) {
            base <<= 1;
            gamma.set(i, (byte)1);
        }
        gamma.set(length, (byte)0);
        byte[] offset = intToBinary(num - base, length);
        for (int i = length + 1; i < 2*length + 1; ++i)
            gamma.set(i, offset[i - length - 1]);
        return gamma;
    }

    static private byte getBit(byte B, int offset) {
        switch (offset) {
            case (0): return (byte)((B & 0x80) >>> 7);
            case (1): return (byte)((B & 0x40) >>> 6);
            case (2): return (byte)((B & 0x20) >>> 5);
            case (3): return (byte)((B & 0x10) >>> 4);
            case (4): return (byte)((B & 0x08) >>> 3);
            case (5): return (byte)((B & 0x04) >>> 2);
            case (6): return (byte)((B & 0x02) >>> 1);
            case (7): return (byte)((B & 0x01) >>> 0);
        }
        return -1;
    }

    /**
     * Decode gamma code back to integer
     * @param gamma The gamma code to be decoded, use Byte to store one bit
     * @return The origin integer
     */
    static private ArrayList<Integer> decodeGamma(ArrayList<Byte> gamma) {
        int cursor = 0;
        int innerByteOffset = 0;
        ArrayList<Integer> intArray = new ArrayList<Integer>();
        while (cursor < gamma.size()) {
            if (getBit(gamma.get(cursor), innerByteOffset) == 0) {
                ++innerByteOffset;
                intArray.add(1);
                if (innerByteOffset == 8) {
                    innerByteOffset = 0;
                    ++cursor;
                }
            } else {
                int num = 1;
                int length = 0;
                while (getBit(gamma.get(cursor), innerByteOffset++) == 1) {
                    if (innerByteOffset == 8) {
                        innerByteOffset = 0;
                        ++cursor;
                    }
                    if (cursor == gamma.size()) {
                        length = 0;
                        break;
                    }
                    ++length;
                    num <<= 1;
                }
                if (length == 0) {
                    break;
                }
                if (innerByteOffset == 8) {
                    innerByteOffset = 0;
                    ++cursor;
                }
                byte[] offset = new byte[length];
                for (int i = 0; i < length; ++i) {
                    offset[i] = getBit(gamma.get(cursor), innerByteOffset++);
                    if (innerByteOffset == 8) {
                        innerByteOffset = 0;
                        ++cursor;
                    }
                }
                num += binaryToInt(offset);
                intArray.add(num);
            }
        }
        return intArray;
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
        ArrayList<Byte> gamma = new ArrayList<Byte>();
        int innerByteOffset = 0;

        innerByteOffset = appendBits(gamma, encodeGamma(1), innerByteOffset);
        innerByteOffset = appendBits(gamma, encodeGamma(2), innerByteOffset);
        innerByteOffset = appendBits(gamma, encodeGamma(3), innerByteOffset);
        innerByteOffset = appendBits(gamma, encodeGamma(4), innerByteOffset);
        innerByteOffset = appendBits(gamma, encodeGamma(9), innerByteOffset);
        innerByteOffset = appendBits(gamma, encodeGamma(13), innerByteOffset);
        innerByteOffset = appendBits(gamma, encodeGamma(24), innerByteOffset);
        innerByteOffset = appendBits(gamma, encodeGamma(511), innerByteOffset);
        innerByteOffset = appendBits(gamma, encodeGamma(1025), innerByteOffset);
        innerByteOffset = appendBits(gamma, encodeGamma(482), innerByteOffset);
        for (byte b : gamma)
            System.out.printf("%x ", b);
        System.out.println();
        ArrayList<Integer> intArray = decodeGamma(gamma);
        for (int num : intArray)
            System.out.print(num + " ");
        System.out.println();

        /*
        gamma = encodeGamma(2);
        for (Byte b : gamma)
            System.out.print(b);
        System.out.println(" " + decodeGamma(gamma)[0]);

        gamma = encodeGamma(3);
        for (Byte b : gamma)
            System.out.print(b);
        System.out.println(" " + decodeGamma(gamma)[0]);

        gamma = encodeGamma(4);
        for (Byte b : gamma)
            System.out.print(b);
        System.out.println(" " + decodeGamma(gamma)[0]);

        gamma = encodeGamma(9);
        for (Byte b : gamma)
            System.out.print(b);
        System.out.println(" " + decodeGamma(gamma)[0]);

        gamma = encodeGamma(13);
        for (Byte b : gamma)
            System.out.print(b);
        System.out.println(" " + decodeGamma(gamma)[0]);

        gamma = encodeGamma(24);
        for (Byte b : gamma)
            System.out.print(b);
        System.out.println(" " + decodeGamma(gamma)[0]);

        gamma = encodeGamma(511);
        for (Byte b : gamma)
            System.out.print(b);
        System.out.println(" " + decodeGamma(gamma)[0]);

        gamma = encodeGamma(1025);
        for (Byte b : gamma)
            System.out.print(b);
        System.out.println(" " + decodeGamma(gamma)[0]);

        gamma = encodeGamma(482);
        for (Byte b : gamma)
            System.out.print(b);
        System.out.println(" " + decodeGamma(gamma)[0]);

        gamma = encodeGamma(511);
        for (Byte b : gamma)
            System.out.print(b);
        System.out.println(" " + decodeGamma(gamma)[0]);
        */
    }
}
