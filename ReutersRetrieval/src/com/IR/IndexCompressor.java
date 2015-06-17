package com.IR;

import java.lang.Math;

/**
 * The invert-index compressor
 * @author Soap
 */
public class IndexCompressor {

    /**
     * Encode integer to gamma code
     * @param num The integer to be encoded
     * @return The byte array of gamma code, which is in big-endian mode
     */
    static private byte[] encodeGamma(int num) {
        if (num == 1)
            return new byte[]{0};

        int length = log(2, num);
        byte[] gamma =  new byte[2*length + 1];
        int base = 1;
        for (int i = 0; i < length; ++i) {
            base <<= 1;
            gamma[i] = 1;
        }
        gamma[length] = 0;
        byte[] offset = intToBinary(num - base, length);
        for (int i = length + 1; i < 2*length + 1; ++i)
            gamma[i] = offset[i - length - 1];
        return gamma;
    }

    /**
     * Decode gamma code back to integer
     * @param num The gamma code to be decoded
     * @return The origin integer
     */
    static private int decodeGamma(byte[] gamma) {
        int num = 1;
        int cursor = 0;
        while (gamma[cursor++] == 1)
            num <<= 1;
        int length = cursor - 1;
        byte[] offset = new byte[length];
        for (int i = 0; i < length; ++i)
            offset[i] = gamma[length + i + 1];
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
