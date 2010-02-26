/* $Revision: 159 $ $Date: 2009-08-17 12:52:56 +0000 (ma, 17 aug 2009) $ $Author: blohman $ 
 * 
 * Copyright (C) 2007-2009  National Library of the Netherlands, 
 *                          Nationaal Archief of the Netherlands, 
 *                          Planets
 *                          KEEP
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 *
 * For more information about this project, visit
 * http://dioscuri.sourceforge.net/
 * or contact us via email:
 *   jrvanderhoeven at users.sourceforge.net
 *   blohman at users.sourceforge.net
 *   bkiers at users.sourceforge.net
 * 
 * Developed by:
 *   Nationaal Archief               <www.nationaalarchief.nl>
 *   Koninklijke Bibliotheek         <www.kb.nl>
 *   Tessella Support Services plc   <www.tessella.com>
 *   Planets                         <www.planets-project.eu>
 *   KEEP                            <www.keep-project.eu>
 * 
 * Project Title: DIOSCURI
 */

package dioscuri.module.cpu;

/**
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
@SuppressWarnings("unused")
public class Util {

    // Attributes
    private static boolean[] parityLookupByte = { true, false, false, true,
            false, true, true, false, false, true, true, false, true, false,
            false, true, false, true, true, false, true, false, false, true,
            true, false, false, true, false, true, true, false, false, true,
            true, false, true, false, false, true, true, false, false, true,
            false, true, true, false, true, false, false, true, false, true,
            true, false, false, true, true, false, true, false, false, true,
            false, true, true, false, true, false, false, true, true, false,
            false, true, false, true, true, false, true, false, false, true,
            false, true, true, false, false, true, true, false, true, false,
            false, true, true, false, false, true, false, true, true, false,
            false, true, true, false, true, false, false, true, false, true,
            true, false, true, false, false, true, true, false, false, true,
            false, true, true, false, false, true, true, false, true, false,
            false, true, true, false, false, true, false, true, true, false,
            true, false, false, true, false, true, true, false, false, true,
            true, false, true, false, false, true, true, false, false, true,
            false, true, true, false, false, true, true, false, true, false,
            false, true, false, true, true, false, true, false, false, true,
            true, false, false, true, false, true, true, false, true, false,
            false, true, false, true, true, false, false, true, true, false,
            true, false, false, true, false, true, true, false, true, false,
            false, true, true, false, false, true, false, true, true, false,
            false, true, true, false, true, false, false, true, true, false,
            false, true, false, true, true, false, true, false, false, true,
            false, true, true, false, false, true, true, false, true, false,
            false, true };

    // Temporary flag variables
    /**
     *
     */
    protected static boolean cf = false;

    // Temporary registers
    // protected static byte[] result = new byte[2];
    /**
     *
     */
    protected static int lowerByteCarry = 0;

    // Constants
    private final static int REGISTER_LOW = 1;
    private final static int REGISTER_HIGH = 0;

    private final static int OVERFLOW_FLAG = 0;
    private final static int CARRY_FLAG = 1;

    // Constructor
    private Util() {
        //cf = false;
    }

    // Methods

    /**
     * Check the parity of a given byte and given size in bits.
     * 
     * @param data
     * @return
     */
    protected static boolean checkParityOfByte(byte data) {
        // Extend byte to unsigned int
        int check = data & 0x000000FF;

        // Lookup parity
        return parityLookupByte[check];
    }

    /**
     * Adds two words (16-bit) including a possible carry bit.<BR>
     * Takes care of possible carry from LSB to MSB. Not beyond MSB. Does not
     * care about signed/unsigned as this doesn't effect the actual result in
     * hex.
     * 
     * @param word1
     *            first word
     * @param word2
     *            second word
     * @param carryBit
     * @return sum of both words including possible carry between LSB and MSB
     */
    protected static byte[] addWords(byte[] word1, byte[] word2, int carryBit) {
        byte[] result = new byte[2];
        lowerByteCarry = ((int) word1[CPU.REGISTER_LOW] & 0xFF)
                + ((int) word2[CPU.REGISTER_LOW] & 0xFF) + carryBit;
        result[CPU.REGISTER_LOW] = (byte) (lowerByteCarry);
        result[CPU.REGISTER_HIGH] = (byte) (((int) word1[CPU.REGISTER_HIGH] & 0xFF) + ((int) word2[CPU.REGISTER_HIGH] & 0xFF));

        // Check for carry in least significant byte (LSB)
        if (lowerByteCarry > 0xFF) {
            // Increase MSB once
            result[CPU.REGISTER_HIGH]++;
        }
        return result;
    }

    /**
     * Subtracts two words (16-bit) including a possible borrow bit.<BR>
     * Takes care of possible borrow from LSB to MSB. Not beyond MSB. Does not
     * care about signed/unsigned as this doesn't effect the actual result in
     * hex. Operation: word1 - (word2 + borrow)
     * 
     * @param word1
     *            first word
     * @param word2
     *            second word
     * @param borrowBit
     * @return subtraction of both words plus borrow including possible borrow
     *         between LSB and MSB
     */
    protected static byte[] subtractWords(byte[] word1, byte[] word2,
            int borrowBit) {
        byte[] result = new byte[2];
        result[CPU.REGISTER_LOW] = (byte) (word1[CPU.REGISTER_LOW] - (word2[CPU.REGISTER_LOW] + borrowBit));
        result[CPU.REGISTER_HIGH] = (byte) (word1[CPU.REGISTER_HIGH] - word2[CPU.REGISTER_HIGH]);

        // Check for borrow in least significant byte (LSB)
        if ((((int) word2[CPU.REGISTER_LOW]) & 0xFF) + borrowBit > (((int) word1[CPU.REGISTER_LOW] & 0xFF))) {
            // Decrease MSB once
            result[CPU.REGISTER_HIGH]--;
        }
        return result;
    }

    /**
     * Adds two registers and the memory reference displacement.<BR>
     * Does not check carry or overflow flags.
     * 
     * @param reg1
     *            first register to be added
     * @param reg2
     *            second register to be added
     * @param displacement
     * @return sum of both registers and memory displacement
     */
    protected static byte[] addRegRegDisp(byte[] reg1, byte[] reg2,
            byte[] displacement) {
        byte[] result = new byte[2];
        result[CPU.REGISTER_GENERAL_LOW] = (byte) (reg1[CPU.REGISTER_GENERAL_LOW]
                + reg2[CPU.REGISTER_GENERAL_LOW] + displacement[CPU.REGISTER_GENERAL_LOW]);
        result[CPU.REGISTER_GENERAL_HIGH] = (byte) (reg1[CPU.REGISTER_GENERAL_HIGH]
                + reg2[CPU.REGISTER_GENERAL_HIGH] + displacement[CPU.REGISTER_GENERAL_HIGH]);
        // Check for overflow; because adding 3 registers, overflow of 2
        // possible!
        int intermediateResult = (((int) reg1[CPU.REGISTER_GENERAL_LOW]) & 0xFF)
                + (((int) reg2[CPU.REGISTER_GENERAL_LOW]) & 0xFF)
                + (((int) displacement[CPU.REGISTER_GENERAL_LOW]) & 0xFF);
        if (intermediateResult > 0xFF) {
            // Check for double overflow
            if (intermediateResult >= 0x200) {
                // Increase high register twice
                result[CPU.REGISTER_GENERAL_HIGH] += 2;
            } else {
                // Increase high register once
                result[CPU.REGISTER_GENERAL_HIGH]++;
            }
        }
        return result;
    }

    /**
     * Test the auxiliary flag (AF) for addition with possible carry.<BR>
     * AF is set when carry occurs to higher nibble.
     * 
     * @param input
     * @param result
     * @return true if carry occurs, false otherwise
     */
    protected static boolean test_AF_ADD(byte input, byte result) {
        // Carry has occurred if result < input
        // E.g. 0000.1111 + 0000.0001 = 0001.0000
        if ((result & 0x0F) < (input & 0x0F)) {
            return true;
        }
        return false;
    }

    /**
     * Test the auxiliary flag (AF) for subtraction with possible borrow.<BR>
     * AF is set when borrow occurs from higher nibble.
     * 
     * @param input
     * @param result
     * @return true if borrow occurs, false otherwise
     */
    protected static boolean test_AF_SUB(byte input, byte result) {
        // Borrow has occurred if result > input
        // E.g. 0001.0000 - 0000.0001 = 0000.1111
        if ((result & 0x0F) > (input & 0x0F)) {
            return true;
        }
        return false;
    }

    /**
     * Test the auxiliary flag (AF) for shift operations with byte.<BR>
     * AF is set when carry occurs. This happens when last shift causes a carry
     * out of lowest nibble
     * 
     * @param input
     * @param shifts
     * 
     * @return true if carry/borrow occurs, false otherwise
     */
    protected static boolean test_AF_ShiftLeft(byte input, int shifts) {
        // Check if number of shifts is in range of bits in lowest nibble, else
        // no carry happens
        if (shifts > 0 && shifts <= 4) {
            // Make sure possible carry is on bit position 0 and return status
            // Do this by making (4 - shifts) to the right
            if (((input >> (4 - shifts)) & 0x01) == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test the carry flag for addition operations with bytes.<BR>
     * CF is set when unsigned overflow occurs. This happens when we cross over
     * from 255 to 0 while adding or cross over from 0 to 255 while subtracting.
     * The operation is (input1 + carry) + input2
     * 
     * @param input1
     * @param input2
     * @param carry
     * 
     * @return true if carry occurs, false otherwise
     */
    protected static boolean test_CF_ADD(byte input1, byte input2, int carry) {
        // There is one unsigned case when CF occurs:
        // addition: input1 + input2 + carry > 255
        if ((((int) input1) & 0xFF) + (((int) input2) & 0xFF) + carry > (((int) 0xFF) & 0xFF)) {
            return true;
        }
        return false;
    }

    /**
     * Test the carry flag for addition operations with words.<BR>
     * CF is set when unsigned overflow occurs. This happens when we cross over
     * from 65535 (0xFFFF) to 0 while adding or cross over from 0 to 65535
     * (0xFFFF) while subtracting. The operation is (input1 + carry) + input2
     * 
     * @param input1
     * @param input2
     * @param carry
     * @return true if carry occurs, false otherwise
     */
    protected static boolean test_CF_ADD(byte[] input1, byte[] input2, int carry) {
        // There is one unsigned case when CF occurs:
        // addition: input1 + input2 + carry > 0xFFFF
        if (((((int) input1[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8)
                + (((int) input1[CPU.REGISTER_GENERAL_LOW]) & 0xFF)
                + ((((int) input2[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8)
                + (((int) input2[CPU.REGISTER_GENERAL_LOW]) & 0xFF) + carry > (((int) 0xFFFF) & 0xFFFF)) {
            return true;
        }
        return false;
    }

    /**
     * Test the carry flag for subtraction operations with bytes.<BR>
     * CF is set when unsigned overflow occurs. This happens when we cross over
     * from 255 to 0 while adding or cross over from 0 to 255 while subtracting.
     * The operation is input1 - (input2 + carry)
     * 
     * @param input1
     *            unsigned input1 of operation
     * @param input2
     *            unsigned input2 of operation
     * @param carry
     *            possible carry bit
     * 
     * @return true if borrow occurs, false otherwise
     */
    protected static boolean test_CF_SUB(byte input1, byte input2, int carry) {
        // There is one unsigned case when CF occurs:
        // subtraction (input1 - (input2 + carry)): input1 + carry < input2
        // if ((((int) input1) & 0xFF) + carry < (((int) input2) & 0xFF))
        if ((((int) input1) & 0xFF) < (((int) input2) & 0xFF) + carry) {
            return true;
        }
        return false;
    }

    /**
     * Test the carry flag for subtraction operations with words.<BR>
     * CF is set when unsigned overflow occurs. This happens when we cross over
     * from 255 to 0 while adding or cross over from 0 to 255 while subtracting.
     * The operation is input1 - (input2 + carry)
     * 
     * @param input1
     * @param input2
     * @param carry
     * @return true if borrow occurs, false otherwise
     */
    protected static boolean test_CF_SUB(byte[] input1, byte[] input2, int carry) {
        // There is one unsigned case when CF occurs:
        // subtraction (input1 - (input2 + carry)): input1 < input2 + carry
        if (((((int) input1[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8)
                + (((int) input1[CPU.REGISTER_GENERAL_LOW]) & 0xFF) < ((((int) input2[CPU.REGISTER_GENERAL_HIGH]) & 0xFF) << 8)
                + (((int) input2[CPU.REGISTER_GENERAL_LOW]) & 0xFF) + carry) {
            return true;
        }
        return false;
    }

    /**
     * Test the overflow flag for addition operations with bytes.<BR>
     * OF is set when 2's complement signed overflow occur. This happens when we
     * cross over from 127 to -128 while adding or cross over from -128 to 127
     * while subtracting.
     * 
     * @param input1
     * @param input2 
     * @param result
     * @param carry
     * @return true if overflow occurs, false otherwise
     */
    protected static boolean test_OF_ADD(byte input1, byte input2, byte result,
            int carry) {
        // There are two signed cases when OF occurs:
        // "input1" + "input2" = "result"
        // (a) (positive) + (positive) = (negative)
        // (b) (negative) + (negative) = (positive)
        // Test these two possibilities here:
        if (result >= 0) {
            // Result (signed) positive, Input1 and Input2 must be (signed)
            // negative to set OF true
            if (input1 + carry < 0 && input2 < 0)
                return true;
            else
                return false;
        } else {
            // Result (signed) negative, Input1 and Input2 must be (signed)
            // positive to set OF true
            if (input1 + carry >= 0 && input2 >= 0)
                return true;
            else
                return false;
        }
    }

    /**
     * Test the overflow flag for addition operations with words.<BR>
     * OF is set when 2's complement signed overflow occur. This happens when we
     * cross over from 32767 (0x7FFF) to -32768 (0x8000) while adding or cross
     * over from -32768 to 32767 while subtracting.
     * 
     * @param input1
     * @param result 
     * @param input2
     * @param carry
     * @return true if overflow occurs, false otherwise
     */
    protected static boolean test_OF_ADD(byte[] input1, byte[] input2,
            byte[] result, int carry) {
        // There are two signed cases when OF occurs:
        // "input1" + "input2" = "result"
        // (a) (positive) + (positive) = (negative)
        // (b) (negative) + (negative) = (positive)
        // Test these two possibilities here:
        if ((((int) result[CPU.REGISTER_GENERAL_HIGH]) << 8)
                + (((int) result[CPU.REGISTER_GENERAL_LOW] & 0xFF)) >= 0) {
            // Result (signed) positive, Input1 and Input2 must be (signed)
            // negative to set OF true
            if ((((int) input1[CPU.REGISTER_GENERAL_HIGH]) << 8)
                    + (((int) input1[CPU.REGISTER_GENERAL_LOW]) & 0xFF) + carry < 0
                    && (((int) input2[CPU.REGISTER_GENERAL_HIGH]) << 8)
                            + (((int) input2[CPU.REGISTER_GENERAL_LOW]) & 0xFF) < 0)
                return true;
            else
                return false;
        } else {
            // Result (signed) negative, Input1 and Input2 must be (signed)
            // positive to set OF true
            if ((((int) input1[CPU.REGISTER_GENERAL_HIGH]) << 8)
                    + (((int) input1[CPU.REGISTER_GENERAL_LOW]) & 0xFF) + carry >= 0
                    && (((int) input2[CPU.REGISTER_GENERAL_HIGH]) << 8)
                            + (((int) input2[CPU.REGISTER_GENERAL_LOW]) & 0xFF) >= 0)
                return true;
            else
                return false;
        }
    }

    /**
     * Test the overflow flag for subtraction operations with bytes.<BR>
     * OF is set when 2's complement signed overflow occurs. This happens when
     * we cross over from 127 to -128 while adding or cross over from -128 to
     * 127 while subtracting.
     * 
     * @param input1
     * @param input2
     * @param carry
     * @param result
     * @return true if overflow occurs, false otherwise
     */
    protected static boolean test_OF_SUB(byte input1, byte input2, byte result,
            int carry) {
        // There are two signed cases when OF occurs:
        // "input1" - "input2" = "result"
        // (a) (positive) - (negative) = (negative)
        // (b) (negative) - (positive) = (positive)
        // Test these two possibilities here:
        if (input1 >= 0) {
            // Input1 (signed) positive, Input2 and Result must be (signed)
            // negative to set OF true
            if (input2 + carry < 0 && result < 0)
                return true;
            else
                return false;
        } else {
            // Input1 (signed) negative, Input2 and Result must be (signed)
            // positive to set OF true
            if (input2 + carry >= 0 && result >= 0)
                return true;
            else
                return false;
        }
    }

    /**
     * Test the overflow flag for subtraction operations with words.<BR>
     * OF is set when 2's complement signed overflow occurs. This happens when
     * we cross over from 32767 (0x7FFF) to -32768 (0x8000) while adding or
     * cross over from -32768 to 32767 while subtracting.
     * 
     * @param input1
     * @param input2
     * @param result
     * @param carry
     * @return true if overflow occurs, false otherwise
     */
    protected static boolean test_OF_SUB(byte[] input1, byte[] input2,
            byte[] result, int carry) {
        // There are two signed cases when OF occurs:
        // "input1" - "input2" = "result"
        // (a) (positive) - (negative) = (negative)
        // (b) (negative) - (positive) = (positive)
        // Test these two possibilities here:
        if ((((int) input1[CPU.REGISTER_GENERAL_HIGH]) << 8)
                + (((int) input1[CPU.REGISTER_GENERAL_LOW]) & 0xFF) >= 0) {
            // Input1 (signed) positive, Input2 and Result must be (signed)
            // negative to set OF true
            if ((((int) input2[CPU.REGISTER_GENERAL_HIGH]) << 8)
                    + (((int) input2[CPU.REGISTER_GENERAL_LOW]) & 0xFF) + carry < 0
                    && (((int) result[CPU.REGISTER_GENERAL_HIGH]) << 8)
                            + (((int) result[CPU.REGISTER_GENERAL_LOW]) & 0xFF) < 0)
                return true;
            else
                return false;
        } else {
            // Input1 (signed) negative, Input2 and Result must be (signed)
            // positive to set OF true
            if ((((int) input2[CPU.REGISTER_GENERAL_HIGH]) << 8)
                    + (((int) input2[CPU.REGISTER_GENERAL_LOW]) & 0xFF) + carry >= 0
                    && (((int) result[CPU.REGISTER_GENERAL_HIGH]) << 8)
                            + (((int) result[CPU.REGISTER_GENERAL_LOW]) & 0xFF) >= 0)
                return true;
            else
                return false;
        }
    }

    /**
     * Determines the sign of the input byte and returns the complementary
     * (sign-extended) byte
     * 
     * @param inputByte
     *            Byte whose sign determines value of sign-extension
     * 
     * @return Byte of value 0x00 if no sign, 0xFF if signed
     */
    protected static byte signExtend(byte inputByte) {
        // Depending on sign from input byte, return either 0x00 (positive) or
        // 0xFF (negative)
        return (inputByte >> 7) == 0 ? (byte) 0x00 : (byte) 0xFF;
    }

    /**
     * Converts a boolean[] into a byte[] Each boolean value is converted into a
     * hexadecimal (0 or 1) value and placed in a byte[]. Note: boolean[] must
     * be of multiple of 8 booleans.
     * 
     * @param booleans
     * @return byte[] containing the converted booleans
     */
    protected static byte[] booleansToBytes(boolean[] booleans) {
        int tempValue = 0;
        // Create temporary array of integers
        int[] tempHex = new int[booleans.length / 8]; // Assumes boolean array
                                                      // is multiple of 8

        // Build integer array
        for (int b = tempHex.length - 1; b >= 0; b--) {
            for (int j = 0; j <= 7; j++) {
                // Convert boolean to integer for low register
                tempValue = booleans[j + ((tempHex.length - 1 - b) * 8)] ? 1
                        : 0;
                // Multiply by corresponding power of 2, add to decimal
                // representation
                tempHex[b] += tempValue * (int) Math.pow(2, j);
            }
        }

        // Convert int[] into byte[]
        byte[] bytes = new byte[tempHex.length];
        for (int t = 0; t < bytes.length; t++) {
            bytes[t] = (byte) (tempHex[t] & 0xFF);
        }

        return bytes;
    }

    /**
     * Converts a byte[] into a boolean[] Each bit is converted into a boolean
     * value and placed in a boolean[].
     * 
     * @param bytes
     * @return boolean[] containing the converted bytes
     */
    protected static boolean[] bytesToBooleans(byte[] bytes) {
        int tempValue = 0;
        boolean[] booleans = new boolean[bytes.length * 8];

        // Build FLAGS register
        for (int b = bytes.length - 1; b >= 0; b--) {
            for (int j = 0; j < 8; j++) {
                // Convert flag into boolean
                tempValue = (bytes[b] >> j) & 0x01;
                if (tempValue == 1) {
                    booleans[((bytes.length - 1 - b) * 8) + j] = true;
                } else {
                    booleans[((bytes.length - 1 - b) * 8) + j] = false;
                }
            }
        }
        return booleans;
    }

    /**
     *
     * @param b
     * @return
     */
    protected static String convertByteToString(byte b) {
        return (Integer.toHexString(0x100 | (b & 0xFF)).substring(1));
    }

    /**
     *
     * @param word
     * @return
     */
    protected static String convertWordToString(byte[] word) {
        return (Integer.toHexString(0x100 | (word[0] & 0xFF)).substring(1) + Integer
                .toHexString(0x100 | (word[1] & 0xFF)).substring(1));
    }

    /**
     * Converts a given string into a byte of one integer
     * 
     * @param strValue
     * @return int as byte
     */
    protected static byte convertStringToByte(String strValue) {
        // FIXME: Check if correct byte implementation
        // Parse from string to int (hex)
        try {
            byte intRegVal = 0;
            for (int i = strValue.length(); i > 0; i--) {
                intRegVal = (byte) (intRegVal + ((int) Math.pow(16, strValue
                        .length()
                        - i))
                        * Integer.parseInt(strValue.substring(i - 1, i), 16));
            }

            return intRegVal;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Converts a given string into a word of bytes
     * 
     * @param strValue
     * @return byte[] as word
     */
    protected static byte[] convertStringToWord(String strValue) {
        // Create empty word
        byte[] word = new byte[2];

        // Parse from string to int (hex)
        try {
            byte intRegVal = 0;
            for (int i = strValue.length(); i > 0; i--) {
                intRegVal = (byte) (intRegVal + Math.pow(16, strValue.length()
                        - i)
                        * Byte.parseByte(strValue.substring(i - 1, i), 16));
                // intRegVal = Byte.parseByte(strValue.substring(i-1,i),16);
            }

            // Enter values in word
            word[0] = (byte) (intRegVal >>> 8 & 0xFF);
            word[1] = (byte) (intRegVal & 0xFF);
        } catch (NumberFormatException e) {
            return null;
        }
        return word;
    }

    /*
     * public static void testGetExponent(int numTests) { java.util.Random rand
     * = new java.util.Random(); for(int i = 0; i < numTests; i++) { double d =
     * rand.nextDouble()*rand.nextInt(); if(getExponent(d) !=
     * Math.getExponent(d)) { System.out.println("ERROR -> "+
     * "getExponent("+d+") = "+getExponent(d)+ " != "+
     * "Math.getExponent("+d+") = "+Math.getExponent(d)); } } }
     */
    /**
     *
     * @param val
     * @return
     */
    public static int getExponent(double val) {
        return (int) (((Double.doubleToRawLongBits(val) & 0x7ff0000000000000L) >> 52) - 1023L);
    }

    /**
     *
     * @param val
     * @return
     */
    public static int getExponent(float val) {
        return ((Float.floatToRawIntBits(val) & 0x7f800000) >> 23) - 127;
    }

    /*
     * public static void testScalb(int numTests) { java.util.Random rand = new
     * java.util.Random(); for(int j = 0; j < numTests; j++) { double d =
     * rand.nextDouble()*rand.nextInt(); float f =
     * rand.nextFloat()*rand.nextInt(); int i = rand.nextInt(); if(scalb(d, i)
     * != Math.scalb(d, i)) { System.out.println("ERROR -> "+
     * "scalb("+d+", "+i+") = "+scalb(d, i)+ " != "+
     * "Math.scalb("+d+", "+i+") = "+Math.scalb(d, i)); } if(scalb(f, i) !=
     * Math.scalb(f, i)) { System.out.println("ERROR -> "+
     * "scalb("+f+", "+i+") = "+scalb(f, i)+ " != "+
     * "Math.scalb("+f+", "+i+") = "+Math.scalb(f, i)); } } }
     */
    /**
     *
     * @param d
     * @param i
     * @return
     */
    public static double scalb(double d, int i) {
        int j = 0;
        char c = '\0';
        double d1 = 0.0;
        if (i < 0) {
            i = Math.max(i, -2099);
            c = '\uFE00';
            d1 = Math.pow(2, -512);
        } else {
            i = Math.min(i, 2099);
            c = '\u0200';
            d1 = Math.pow(2, 512);
        }
        int k = (i >> 8) >>> 23;
        j = (i + k & 0x1ff) - k;
        d *= Math.pow(2, j);
        for (i -= j; i != 0; i -= c) {
            d *= d1;
        }
        return d;
    }

    /**
     *
     * @param f
     * @param i
     * @return
     */
    public static float scalb(float f, int i) {
        i = Math.max(Math.min(i, 278), -278);
        return (float) ((double) f * Math.pow(2, i));
    }
}
