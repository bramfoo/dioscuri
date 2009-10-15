/*
    JPC: A x86 PC Hardware Emulator for a pure Java Virtual Machine
    Release Version 2.0

    A project from the Physics Dept, The University of Oxford

    Copyright (C) 2007 Isis Innovation Limited

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 2 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 
    Details (including contact information) can be found at: 

    www.physics.ox.ac.uk/jpc
*/
package dioscuri.module.cpu32;

public class JavaOpcode
{
    public static final int NOP = 0;
    public static final int ACONST_NULL = 1;
    public static final int ICONST_M1 = 2;
    public static final int ICONST_0 = 3;
    public static final int ICONST_1 = 4;
    public static final int ICONST_2 = 5;
    public static final int ICONST_3 = 6;
    public static final int ICONST_4 = 7;
    public static final int ICONST_5 = 8;
    public static final int LCONST_0 = 9;
    public static final int LCONST_1 = 10;
    public static final int FCONST_0 = 11;
    public static final int FCONST_1 = 12;
    public static final int FCONST_2 = 13;
    public static final int DCONST_0 = 14;
    public static final int DCONST_1 = 15;
    public static final int BIPUSH = 16;
    public static final int SIPUSH = 17;
    public static final int LDC = 18;
    public static final int LDC_W = 19;
    public static final int LDC2_W = 20;
    public static final int ILOAD = 21;
    public static final int LLOAD = 22;
    public static final int FLOAD = 23;
    public static final int DLOAD = 24;
    public static final int ALOAD = 25;
    public static final int ILOAD_0 = 26;
    public static final int ILOAD_1 = 27;
    public static final int ILOAD_2 = 28;
    public static final int ILOAD_3 = 29;
    public static final int LLOAD_0 = 30;
    public static final int LLOAD_1 = 31;
    public static final int LLOAD_2 = 32;
    public static final int LLOAD_3 = 33;
    public static final int FLOAD_0 = 34;
    public static final int FLOAD_1 = 35;
    public static final int FLOAD_2 = 36;
    public static final int FLOAD_3 = 37;
    public static final int DLOAD_0 = 38;
    public static final int DLOAD_1 = 39;
    public static final int DLOAD_2 = 40;
    public static final int DLOAD_3 = 41;
    public static final int ALOAD_0 = 42;
    public static final int ALOAD_1 = 43;
    public static final int ALOAD_2 = 44;
    public static final int ALOAD_3 = 45;
    public static final int IALOAD = 46;
    public static final int LALOAD = 47;
    public static final int FALOAD = 48;
    public static final int DALOAD = 49;
    public static final int AALOAD = 50;
    public static final int BALOAD = 51;
    public static final int CALOAD = 52;
    public static final int SALOAD = 53;
    public static final int ISTORE = 54;
    public static final int LSTORE = 55;
    public static final int FSTORE = 56;
    public static final int DSTORE = 57;
    public static final int ASTORE = 58;
    public static final int ISTORE_0 = 59;
    public static final int ISTORE_1 = 60;
    public static final int ISTORE_2 = 61;
    public static final int ISTORE_3 = 62;
    public static final int LSTORE_0 = 63;
    public static final int LSTORE_1 = 64;
    public static final int LSTORE_2 = 65;
    public static final int LSTORE_3 = 66;
    public static final int FSTORE_0 = 67;
    public static final int FSTORE_1 = 68;
    public static final int FSTORE_2 = 69;
    public static final int FSTORE_3 = 70;
    public static final int DSTORE_0 = 71;
    public static final int DSTORE_1 = 72;
    public static final int DSTORE_2 = 73;
    public static final int DSTORE_3 = 74;
    public static final int ASTORE_0 = 75;
    public static final int ASTORE_1 = 76;
    public static final int ASTORE_2 = 77;
    public static final int ASTORE_3 = 78;
    public static final int IASTORE = 79;
    public static final int LASTORE = 80;
    public static final int FASTORE = 81;
    public static final int DASTORE = 82;
    public static final int AASTORE = 83;
    public static final int BASTORE = 84;
    public static final int CASTORE = 85;
    public static final int SASTORE = 86;
    public static final int POP = 87;
    public static final int POP2 = 88;
    public static final int DUP = 89;
    public static final int DUP_X1 = 90;
    public static final int DUP_X2 = 91;
    public static final int DUP2 = 92;
    public static final int DUP2_X1 = 93;
    public static final int DUP2_X2 = 94;
    public static final int SWAP = 95;
    public static final int IADD = 96;
    public static final int LADD = 97;
    public static final int FADD = 98;
    public static final int DADD = 99;
    public static final int ISUB = 100;
    public static final int LSUB = 101;
    public static final int FSUB = 102;
    public static final int DSUB = 103;
    public static final int IMUL = 104;
    public static final int LMUL = 105;
    public static final int FMUL = 106;
    public static final int DMUL = 107;
    public static final int IDIV = 108;
    public static final int LDIV = 109;
    public static final int FDIV = 110;
    public static final int DDIV = 111;
    public static final int IREM = 112;
    public static final int LREM = 113;
    public static final int FREM = 114;
    public static final int DREM = 115;
    public static final int INEG = 116;
    public static final int LNEG = 117;
    public static final int FNEG = 118;
    public static final int DNEG = 119;
    public static final int ISHL = 120;
    public static final int LSHL = 121;
    public static final int ISHR = 122;
    public static final int LSHR = 123;
    public static final int IUSHR = 124;
    public static final int LUSHR = 125;
    public static final int IAND = 126;
    public static final int LAND = 127;
    public static final int IOR = 128;
    public static final int LOR = 129;
    public static final int IXOR = 130;
    public static final int LXOR = 131;
    public static final int IINC = 132;
    public static final int I2L = 133;
    public static final int I2F = 134;
    public static final int I2D = 135;
    public static final int L2I = 136;
    public static final int L2F = 137;
    public static final int L2D = 138;
    public static final int F2I = 139;
    public static final int F2L = 140;
    public static final int F2D = 141;
    public static final int D2I = 142;
    public static final int D2L = 143;
    public static final int D2F = 144;
    public static final int I2B = 145;
    public static final int I2C = 146;
    public static final int I2S = 147;
    public static final int LCMP = 148;
    public static final int FCMPL = 149;
    public static final int FCMPG = 150;
    public static final int DCMPL = 151;
    public static final int DCMPG = 152;
    public static final int IFEQ = 153;
    public static final int IFNE = 154;
    public static final int IFLT = 155;
    public static final int IFGE = 156;
    public static final int IFGT = 157;
    public static final int IFLE = 158;
    public static final int IF_ICMPEQ = 159;
    public static final int IF_ICMPNE = 160;
    public static final int IF_ICMPLT = 161;
    public static final int IF_ICMPGE = 162;
    public static final int IF_ICMPGT = 163;
    public static final int IF_ICMPLE = 164;
    public static final int IF_ACMPEQ = 165;
    public static final int IF_ACMPNE = 166;
    public static final int GOTO = 167;
    public static final int JSR = 168;
    public static final int RET = 169;
    public static final int TABLESWITCH = 170;
    public static final int LOOKUPSWITCH = 171;
    public static final int IRETURN = 172;
    public static final int LRETURN = 173;
    public static final int FRETURN = 174;
    public static final int DRETURN = 175;
    public static final int ARETURN = 176;
    public static final int RETURN = 177;
    public static final int GETSTATIC = 178;
    public static final int PUTSTATIC = 179;
    public static final int GETFIELD = 180;
    public static final int PUTFIELD = 181;
    public static final int INVOKEVIRTUAL = 182;
    public static final int INVOKESPECIAL = 183;
    public static final int INVOKESTATIC = 184;
    public static final int INVOKEINTERFACE = 185;
    public static final int XXXUNUSEDXXX = 186;
    public static final int NEW = 187;
    public static final int NEWARRAY = 188;
    public static final int ANEWARRAY = 189;
    public static final int ARRAYLENGTH = 190;
    public static final int ATHROW = 191;
    public static final int CHECKCAST = 192;
    public static final int INSTANCEOF = 193;
    public static final int MONITORENTER = 194;
    public static final int MONITOREXIT = 195;
    public static final int WIDE = 196;
    public static final int MULTIANEWARRAY = 197;
    public static final int IFNULL = 198;
    public static final int IFNONNULL = 199;
    public static final int GOTO_W = 200;
    public static final int JSR_W = 201;
    public static final int BREAKPOINT = 202;
    public static final int IMPDEP1 = 254;
    public static final int IMPDEP2 = 255;

    private static final String[] opcodes = 
    {
        "NOP",
        "ACONST_NULL",
        "ICONST_M1",
        "ICONST_0",
        "ICONST_1",
        "ICONST_2",
        "ICONST_3",
        "ICONST_4",
        "ICONST_5",
        "LCONST_0",
        "LCONST_1",
        "FCONST_0",
        "FCONST_1",
        "FCONST_2",
        "DCONST_0",
        "DCONST_1",
        "BIPUSH",
        "SIPUSH",
        "LDC",
        "LDC_W",
        "LDC2_W",
        "ILOAD",
        "LLOAD",
        "FLOAD",
        "DLOAD",
        "ALOAD",
        "ILOAD_0",
        "ILOAD_1",
        "ILOAD_2",
        "ILOAD_3",
        "LLOAD_0",
        "LLOAD_1",
        "LLOAD_2",
        "LLOAD_3",
        "FLOAD_0",
        "FLOAD_1",
        "FLOAD_2",
        "FLOAD_3",
        "DLOAD_0",
        "DLOAD_1",
        "DLOAD_2",
        "DLOAD_3",
        "ALOAD_0",
        "ALOAD_1",
        "ALOAD_2",
        "ALOAD_3",
        "IALOAD",
        "LALOAD",
        "FALOAD",
        "DALOAD",
        "AALOAD",
        "BALOAD",
        "CALOAD",
        "SALOAD",
        "ISTORE",
        "LSTORE",
        "FSTORE",
        "DSTORE",
        "ASTORE",
        "ISTORE_0",
        "ISTORE_1",
        "ISTORE_2",
        "ISTORE_3",
        "LSTORE_0",
        "LSTORE_1",
        "LSTORE_2",
        "LSTORE_3",
        "FSTORE_0",
        "FSTORE_1",
        "FSTORE_2",
        "FSTORE_3",
        "DSTORE_0",
        "DSTORE_1",
        "DSTORE_2",
        "DSTORE_3",
        "ASTORE_0",
        "ASTORE_1",
        "ASTORE_2",
        "ASTORE_3",
        "IASTORE",
        "LASTORE",
        "FASTORE",
        "DASTORE",
        "AASTORE",
        "BASTORE",
        "CASTORE",
        "SASTORE",
        "POP",
        "POP2",
        "DUP",
        "DUP_X1",
        "DUP_X2",
        "DUP2",
        "DUP2_X1",
        "DUP2_X2",
        "SWAP",
        "IADD",
        "LADD",
        "FADD",
        "DADD",
        "ISUB",
        "LSUB",
        "FSUB",
        "DSUB",
        "IMUL",
        "LMUL",
        "FMUL",
        "DMUL",
        "IDIV",
        "LDIV",
        "FDIV",
        "DDIV",
        "IREM",
        "LREM",
        "FREM",
        "DREM",
        "INEG",
        "LNEG",
        "FNEG",
        "DNEG",
        "ISHL",
        "LSHL",
        "ISHR",
        "LSHR",
        "IUSHR",
        "LUSHR",
        "IAND",
        "LAND",
        "IOR",
        "LOR",
        "IXOR",
        "LXOR",
        "IINC",
        "I2L",
        "I2F",
        "I2D",
        "L2I",
        "L2F",
        "L2D",
        "F2I",
        "F2L",
        "F2D",
        "D2I",
        "D2L",
        "D2F",
        "I2B",
        "I2C",
        "I2S",
        "LCMP",
        "FCMPL",
        "FCMPG",
        "DCMPL",
        "DCMPG",
        "IFEQ",
        "IFNE",
        "IFLT",
        "IFGE",
        "IFGT",
        "IFLE",
        "IF_ICMPEQ",
        "IF_ICMPNE",
        "IF_ICMPLT",
        "IF_ICMPGE",
        "IF_ICMPGT",
        "IF_ICMPLE",
        "IF_ACMPEQ",
        "IF_ACMPNE",
        "GOTO",
        "JSR",
        "RET",
        "TABLESWITCH",
        "LOOKUPSWITCH",
        "IRETURN",
        "LRETURN",
        "FRETURN",
        "DRETURN",
        "ARETURN",
        "RETURN",
        "GETSTATIC",
        "PUTSTATIC",
        "GETFIELD",
        "PUTFIELD",
        "INVOKEVIRTUAL",
        "INVOKESPECIAL",
        "INVOKESTATIC",
        "INVOKEINTERFACE",
        "XXXUNUSEDXXX",
        "NEW",
        "NEWARRAY",
        "ANEWARRAY",
        "ARRAYLENGTH",
        "ATHROW",
        "CHECKCAST",
        "INSTANCEOF",
        "MONITORENTER",
        "MONITOREXIT",
        "WIDE",
        "MULTIANEWARRAY",
        "IFNULL",
        "IFNONNULL",
        "GOTO_W",
        "JSR_W",
        "BREAKPOINT",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "RESERVED",
        "IMPDEP1",
        "IMPDEP2"
    };



    public static final int CALC_FROM_CONST_POOL = 0xBEEF;

    public static String toString(int value)
    {
        return opcodes[value];
    }

    public static boolean isBranchInstruction(int code)
    {
        switch (code) {
        case IFEQ:
        case IFNE:
        case IFLT:
        case IFGE:
        case IFGT:
        case IFLE:
        case IF_ICMPEQ:
        case IF_ICMPNE:
        case IF_ICMPLT:
        case IF_ICMPGE:
        case IF_ICMPGT:
        case IF_ICMPLE:
        case IF_ACMPEQ:
        case IF_ACMPNE:
        case IFNULL:
        case IFNONNULL:
            return true;

        default:
            return false;
//         case GOTO:
//         case JSR:
//         case RET:
//         case GOTO_W:
//         case JSR_W:
        }
    }

    public static boolean isReturn(int code)
    {
    switch (code) {
    case IRETURN:
    case LRETURN:
    case FRETURN:
    case DRETURN:
    case ARETURN:
    case RETURN:
        case ATHROW:
        return true;
    default:
        return false;
    }
    }

    public static int getJumpOffset(int[] code, int i)
    {
        switch (code[i]) {
        case IFEQ:
        case IFNE:
        case IFLT:
        case IFGE:
        case IFGT:
        case IFLE:
        case IF_ICMPEQ:
        case IF_ICMPNE:
        case IF_ICMPLT:
        case IF_ICMPGE:
        case IF_ICMPGT:
        case IF_ICMPLE:
        case IF_ACMPEQ:
        case IF_ACMPNE:
        case IFNULL:
        case IFNONNULL:
            return (short)((code[i+1] << 8) | code[i+2]);

        case GOTO:
        case JSR:
            return (short)((code[i+1] << 8) | code[i+2]);

        case GOTO_W:
        case JSR_W:
            return  (code[i+1] << 24) | (code[i+2] << 16) | (code[i+3] << 8) | code[i+4];

        case RET:
        throw new IllegalStateException("Must fix stack delta measurement on methods with subroutines");

        default:
            return 0;
        }
    }

    public static int getStackDelta(int[] code, int i)
    {
        switch (code[i])
        {
        case AALOAD:
            return -1;
        case AASTORE:
            return -3;
        case ACONST_NULL:
            return +1;
        case ALOAD:
            return +1;
        case ALOAD_0:
            return +1;
        case ALOAD_1:
            return +1;
        case ALOAD_2:
            return +1;
        case ALOAD_3:
            return +1;
        case ANEWARRAY:
            return 0;
        case ARRAYLENGTH:
            return 0;
        case ASTORE:
            return -1;
        case ASTORE_0:
            return -1;
        case ASTORE_1:
            return -1;
        case ASTORE_2:
            return -1;
        case ASTORE_3:
            return -1;
        case ATHROW:
            // technically this isn't true, but I'm not sure what is...
            return 0;
        case BALOAD:
            return -1;
        case BASTORE:
            return -3;
        case BIPUSH:
            return +1;
        case CALOAD:
            return -1;
        case CASTORE:
            return -3;
        case CHECKCAST:
            return 0;
        case D2F:
            return -1;
        case D2I:
            return -1;
        case D2L:
            return 0;
        case DADD:
            return -2;
        case DALOAD:
            return 0;
        case DASTORE:
            return -4;
        case DCMPG:
            return -3;
        case DCMPL:
            return -3;
        case DCONST_0:
            return +2;
        case DCONST_1:
            return +2;
        case DDIV:
            return -2;
        case DLOAD:
            return +2;
        case DLOAD_0:
            return +2;
        case DLOAD_1:
            return +2;
        case DLOAD_2:
            return +2;
        case DLOAD_3:
            return +2;
        case DMUL:
            return -2;
        case DNEG:
            return 0;
        case DREM:
            return -2;
        case DSTORE:
            return -2;
        case DSTORE_0:
            return -2;
        case DSTORE_1:
            return -2;
        case DSTORE_2:
            return -2;
        case DSTORE_3:
            return -2;
        case DSUB:
            return -2;
        case DUP2:
            return +2;
        case DUP2_X1:
            return +2;
        case DUP2_X2:
            return +2;
        case DUP:
            return +1;
        case DUP_X1:
            return +1;
        case DUP_X2:
            return +1;
        case F2D:
            return +1;
        case F2I:
            return 0;
        case F2L:
            return +1;
        case FADD:
            return -1;
        case FALOAD:
            return -1;
        case FASTORE:
            return -3;
        case FCMPG:
            return -1;
        case FCMPL:
            return -1;
        case FCONST_0:
            return +1;
        case FCONST_1:
            return +1;
        case FCONST_2:
            return +1;
        case FDIV:
            return -1;
        case FLOAD:
            return +1;
        case FLOAD_0:
            return +1;
        case FLOAD_1:
            return +1;
        case FLOAD_2:
            return +1;
        case FLOAD_3:
            return +1;
        case FMUL:
            return -1;
        case FNEG:
            return 0;
        case FREM:
            return -1;
        case FSTORE:
            return -1;
        case FSTORE_0:
            return -1;
        case FSTORE_1:
            return -1;
        case FSTORE_2:
            return -1;
        case FSTORE_3:
            return -1;
        case FSUB:
            return -1;
        case GETFIELD:
            return CALC_FROM_CONST_POOL;
        case GETSTATIC:
            return CALC_FROM_CONST_POOL;
        case GOTO:
            return 0;
        case GOTO_W:
            return 0;
        case I2B:
            return 0;
        case I2C:
            return 0;
        case I2D:
            return +1;
        case I2F:
            return 0;
        case I2L:
            return +1;
        case I2S:
            return 0;
        case IADD:
            return -1;
        case IALOAD:
            return -1;
        case IAND:
            return -1;
        case IASTORE:
            return -3;
        case ICONST_0:
            return +1;
        case ICONST_1:
            return +1;
        case ICONST_2:
            return +1;
        case ICONST_3:
            return +1;
        case ICONST_4:
            return +1;
        case ICONST_5:
            return +1;
        case ICONST_M1:
            return +1;
        case IDIV:
            return -1;
        case IFEQ:
            return -1;
        case IFGE:
            return -1;
        case IFGT:
            return -1;
        case IFLE:
            return -1;
        case IFLT:
            return -1;
        case IFNE:
            return -1;
        case IFNONNULL:
            return -1;
        case IFNULL:
            return -1;
        case IF_ACMPEQ:
            return -2;
        case IF_ACMPNE:
            return -2;
        case IF_ICMPEQ:
            return -2;
        case IF_ICMPGE:
            return -2;
        case IF_ICMPGT:
            return -2;
        case IF_ICMPLE:
            return -2;
        case IF_ICMPLT:
            return -2;
        case IF_ICMPNE:
            return -2;
        case IINC:
            return 0;
        case ILOAD:
            return +1;
        case ILOAD_0:
            return +1;
        case ILOAD_1:
            return +1;
        case ILOAD_2:
            return +1;
        case ILOAD_3:
            return +1;
        case IMUL:
            return -1;
        case INEG:
            return 0;
        case INSTANCEOF:
            return 0;
        case INVOKEINTERFACE:
            return CALC_FROM_CONST_POOL;
//         case INVOKEINTERFACE:
//             return -(code[i+3] + 1);
        case INVOKESPECIAL:
            return CALC_FROM_CONST_POOL;
        case INVOKESTATIC:
            return CALC_FROM_CONST_POOL;
        case INVOKEVIRTUAL:
            return CALC_FROM_CONST_POOL;
        case IOR:
            return -1;
        case IREM:
            return -1;
        case ISHL:
            return -1;
        case ISHR:
            return -1;
        case ISTORE:
            return -1;
        case ISTORE_0:
            return -1;
        case ISTORE_1:
            return -1;
        case ISTORE_2:
            return -1;
        case ISTORE_3:
            return -1;
        case ISUB:
            return -1;
        case IUSHR:
            return -1;
        case IXOR:
            return -1;
        case JSR:
            return +1;
        case JSR_W:
            return +1;
        case L2D:
            return 0;
        case L2F:
            return -1;
        case L2I:
            return -1;
        case LADD:
            return -2;
        case LALOAD:
            return 0;
        case LAND:
            return -2;
        case LASTORE:
            return -4;
        case LCMP:
            return -3;
        case LCONST_0:
            return +2;
        case LCONST_1:
            return +2;
        case LDC2_W:
            return +2;
        case LDC:
            return +1;
        case LDC_W:
            return +1;
        case LDIV:
            return -2;
        case LLOAD:
            return +2;
        case LLOAD_0:
            return +2;
        case LLOAD_1:
            return +2;
        case LLOAD_2:
            return +2;
        case LLOAD_3:
            return +2;
        case LMUL:
            return -2;
        case LNEG:
            return 0;
        case LOOKUPSWITCH:
            return -1;
        case LOR:
            return -2;
        case LREM:
            return -2;
        case LSHL:
            return -1;
        case LSHR:
            return -1;
        case LSTORE:
            return -2;
        case LSTORE_0:
            return -2;
        case LSTORE_1:
            return -2;
        case LSTORE_2:
            return -2;
        case LSTORE_3:
            return -2;
        case LSUB:
            return -2;
        case LUSHR:
            return -1;
        case LXOR:
            return -2;
        case MONITORENTER:
            return -1;
        case MONITOREXIT:
            return -1;
        case MULTIANEWARRAY:
            return 1 - code[i+3];
        case NEW:
            return +1;
        case NEWARRAY:
            return 0;
        case NOP:
            return 0;
        case POP2:
            return -2;
        case POP:
            return -1;
        case PUTFIELD:
            return CALC_FROM_CONST_POOL;
        case PUTSTATIC:
            return CALC_FROM_CONST_POOL;
        case RET:
            return 0;
        case SALOAD:
            return -1;
        case SASTORE:
            return -3;
        case SIPUSH:
            return +1;
        case SWAP:
            return 0;
        case TABLESWITCH:
            return -1;
        case WIDE:
            return getStackDelta(code, i+1);
        // all returns actually flatten the stack completely....
        case ARETURN:
        case FRETURN:
        case IRETURN:
            return -1;
        case DRETURN:
        case LRETURN:
            return -2;
        case RETURN:
            return 0;
        case BREAKPOINT:
        case IMPDEP1:
        case IMPDEP2:
        case XXXUNUSEDXXX:
        default:
            throw new IllegalStateException("JavaOpcode - getStackDelta - reserved instrution!");
        }
    }


    public static int getLocalVariableAccess(int[] code, int i)
    {
        switch(code[i])
        {
        case ALOAD:
            return code[i+1];
        case ALOAD_0:
            return 0;
        case ALOAD_1:
            return 1;
        case ALOAD_2:
            return 2;
        case ALOAD_3:
            return 3;
        case ASTORE:
            return code[i+1];
        case ASTORE_0:
            return 0;
        case ASTORE_1:
            return 1;
        case ASTORE_2:
            return 2;
        case ASTORE_3:
            return 3;
        case DLOAD:
            return code[i+1] + 1;
        case DLOAD_0:
            return 1;
        case DLOAD_1:
            return 2;
        case DLOAD_2:
            return 3;
        case DLOAD_3:
            return 4;
        case DSTORE:
            return code[i+1] + 1;
        case DSTORE_0:
            return 1;
        case DSTORE_1:
            return 2;
        case DSTORE_2:
            return 3;
        case DSTORE_3:
            return 4;
        case FLOAD:
            return code[i+1];
        case FLOAD_0:
            return 0;
        case FLOAD_1:
            return 1;
        case FLOAD_2:
            return 2;
        case FLOAD_3:
            return 3;
        case FSTORE:
            return code[i+1];
        case FSTORE_0:
            return 0;
        case FSTORE_1:
            return 1;
        case FSTORE_2:
            return 2;
        case FSTORE_3:
            return 3;
        case ILOAD:
            return code[i+1];
        case ILOAD_0:
            return 0;
        case ILOAD_1:
            return 1;
        case ILOAD_2:
            return 2;
        case ILOAD_3:
            return 3;
        case ISTORE:
            return code[i+1];
        case ISTORE_0:
            return 0;
        case ISTORE_1:
            return 1;
        case ISTORE_2:
            return 2;
        case ISTORE_3:
            return 3;
        case LLOAD:
            return code[i+1] + 1;
        case LLOAD_0:
            return 1;
        case LLOAD_1:
            return 2;
        case LLOAD_2:
            return 3;
        case LLOAD_3:
            return 4;
        case LSTORE:
            return code[i+1] + 1;
        case LSTORE_0:
            return 1;
        case LSTORE_1:
            return 2;
        case LSTORE_2:
            return 3;
        case LSTORE_3:
            return 4;
        case IINC:
            return code[i+1];
        case RET:
            return code[i+1];
        default:
            return 0;
        }
    }

    public static int getConstantPoolIndexSize(int code)
    {
        switch(code) {
        case LDC:
            return 1;
            
        case ANEWARRAY:
        case CHECKCAST:
        case GETFIELD:
        case GETSTATIC:
        case INSTANCEOF:
        case INVOKEINTERFACE:
        case INVOKESPECIAL:
        case INVOKESTATIC:
        case INVOKEVIRTUAL:
        case LDC2_W:
        case LDC_W:
        case MULTIANEWARRAY:
        case NEW:
        case PUTFIELD:
        case PUTSTATIC:
            return 2;

        default:
            throw new IllegalStateException();
        }
    }

    public static int getOpcodeLength(int[] code, int i)
    {
        switch(code[i])
        {
        case AALOAD:
        case AASTORE:
        case ACONST_NULL:
        case ALOAD_0:
        case ALOAD_1:
        case ALOAD_2:
        case ALOAD_3:
        case ARETURN:
        case ARRAYLENGTH:
        case ASTORE_0:
        case ASTORE_1:
        case ASTORE_2:
        case ASTORE_3:
        case ATHROW:
        case BALOAD:
        case BASTORE:
        case CALOAD:
        case CASTORE:
        case D2F:
        case D2I:
        case D2L:
        case DADD:
        case DALOAD:
        case DASTORE:
        case DCMPG:
        case DCMPL:
        case DCONST_0:
        case DCONST_1:
        case DDIV:
        case DLOAD_0:
        case DLOAD_1:
        case DLOAD_2:
        case DLOAD_3:
        case DMUL:
        case DNEG:
        case DREM:
        case DRETURN:
        case DSTORE_0:
        case DSTORE_1:
        case DSTORE_2:
        case DSTORE_3:
        case DSUB:
        case DUP2:
        case DUP2_X1:
        case DUP2_X2:
        case DUP:
        case DUP_X1:
        case DUP_X2:
        case F2D:
        case F2I:
        case F2L:
        case FADD:
        case FALOAD:
        case FASTORE:
        case FCMPG:
        case FCMPL:
        case FCONST_0:
        case FCONST_1:
        case FCONST_2:
        case FDIV:
        case FLOAD_0:
        case FLOAD_1:
        case FLOAD_2:
        case FLOAD_3:
        case FMUL:
        case FNEG:
        case FREM:
        case FRETURN:
        case FSTORE_0:
        case FSTORE_1:
        case FSTORE_2:
        case FSTORE_3:
        case FSUB:
        case I2B:
        case I2C:
        case I2D:
        case I2F:
        case I2L:
        case I2S:
        case IADD:
        case IALOAD:
        case IAND:
        case IASTORE:
        case ICONST_0:
        case ICONST_1:
        case ICONST_2:
        case ICONST_3:
        case ICONST_4:
        case ICONST_5:
        case ICONST_M1:
        case IDIV:
        case ILOAD_0:
        case ILOAD_1:
        case ILOAD_2:
        case ILOAD_3:
        case IMUL:
        case INEG:
        case IOR:
        case IREM:
        case IRETURN:
        case ISHL:
        case ISHR:
        case ISTORE_0:
        case ISTORE_1:
        case ISTORE_2:
        case ISTORE_3:
        case ISUB:
        case IUSHR:
        case IXOR:
        case L2D:
        case L2F:
        case L2I:
        case LADD:
        case LALOAD:
        case LAND:
        case LASTORE:
        case LCMP:
        case LCONST_0:
        case LCONST_1:
        case LDIV:
        case LLOAD_0:
        case LLOAD_1:
        case LLOAD_2:
        case LLOAD_3:
        case LMUL:
        case LNEG:
        case LOR:
        case LREM:
        case LRETURN:
        case LSHL:
        case LSHR:
        case LSTORE_0:
        case LSTORE_1:
        case LSTORE_2:
        case LSTORE_3:
        case LSUB:
        case LUSHR:
        case LXOR:
        case MONITORENTER:
        case MONITOREXIT:
        case NOP:
        case POP2:
        case POP:
        case RETURN:
        case SALOAD:
        case SASTORE:
        case SWAP:
            return 1;
        case ALOAD:
        case ASTORE:
        case BIPUSH:
        case DLOAD:
        case DSTORE:
        case FLOAD:
        case FSTORE:
        case ILOAD:
        case ISTORE:
        case LDC:
        case LLOAD:
        case LSTORE:
        case NEWARRAY:
        case RET:
            return 2;
        case ANEWARRAY:
        case CHECKCAST:
        case GETFIELD:
        case GETSTATIC:
        case GOTO:
        case IFEQ:
        case IFGE:
        case IFGT:
        case IFLE:
        case IFLT:
        case IFNE:
        case IFNONNULL:
        case IFNULL:
        case IF_ACMPEQ:
        case IF_ACMPNE:
        case IF_ICMPEQ:
        case IF_ICMPGE:
        case IF_ICMPGT:
        case IF_ICMPLE:
        case IF_ICMPLT:
        case IF_ICMPNE:
        case IINC:
        case INSTANCEOF:
        case INVOKESPECIAL:
        case INVOKESTATIC:
        case INVOKEVIRTUAL:
        case JSR:
        case LDC2_W:
        case LDC_W:
        case NEW:
        case PUTFIELD:
        case PUTSTATIC:
        case SIPUSH:
            return 3;
        case MULTIANEWARRAY:
            return 4;
        case GOTO_W:
        case INVOKEINTERFACE:
        case JSR_W:
            return 5;
        case LOOKUPSWITCH:
            return getLookupSwitchLength(code, i);
        case TABLESWITCH:
            return getTableSwitchLength(code, i);
        case WIDE:
            if (code[i+1] == IINC)
                return 6;
            return 4;
        default:
            // reserved instrs -- shouldn't really be here....
           System.err.println("Java Opcode - getOpcodeLength - reserved instrution!");
            return 1;
        }
    }


    private static int getLookupSwitchLength(int[] code, int i)
    {
        int initPosition = i;
        // skip the zeros
        for(i = (initPosition + 1); i < (initPosition + 5); i++)
            if ((i % 4) == 0)
                break;
        // skip the default byte
        i += 4;
        // read the number of pairs
        int npairs = (code[i] << 24) | (code[i+1] << 16) | (code[i+2] << 8) | (code[i+3]);
        i += 4;
        // skip the pairs
        i += 8 * npairs;

        return i - initPosition;
    }

    private static int getTableSwitchLength(int[] code, int i)
    {
        int initPosition = i;
        // skip the zeros
        for(i = (initPosition + 1); i < (initPosition + 4); i++)
            if ((i % 4) == 0)
                break;
        // skip the default byte
        i += 4;
        // read the lowbyte
        int low = (code[i] << 24) | (code[i+1] << 16) | (code[i+2] << 8) | (code[i+3]);
        i += 4;
        // read the highbyte
        int high = (code[i] << 24) | (code[i+1] << 16) | (code[i+2] << 8) | (code[i+3]);
        i += 4;
        // skip the table
        i += 4 * (high - low + 1);

        return i - initPosition;
    }




/*
        case NOP:
        case ACONST_NULL:
        case ICONST_M1:
        case ICONST_0:
        case ICONST_1:
        case ICONST_2:
        case ICONST_3:
        case ICONST_4:
        case ICONST_5:
        case LCONST_0:
        case LCONST_1:
        case FCONST_0:
        case FCONST_1:
        case FCONST_2:
        case DCONST_0:
        case DCONST_1:
        case BIPUSH:
        case SIPUSH:
        case LDC:
        case LDC_W:
        case LDC2_W:
        case ILOAD:
        case LLOAD:
        case FLOAD:
        case DLOAD:
        case ALOAD:
        case ILOAD_0:
        case ILOAD_1:
        case ILOAD_2:
        case ILOAD_3:
        case LLOAD_0:
        case LLOAD_1:
        case LLOAD_2:
        case LLOAD_3:
        case FLOAD_0:
        case FLOAD_1:
        case FLOAD_2:
        case FLOAD_3:
        case DLOAD_0:
        case DLOAD_1:
        case DLOAD_2:
        case DLOAD_3:
        case ALOAD_0:
        case ALOAD_1:
        case ALOAD_2:
        case ALOAD_3:
        case IALOAD:
        case LALOAD:
        case FALOAD:
        case DALOAD:
        case AALOAD:
        case BALOAD:
        case CALOAD:
        case SALOAD:
        case ISTORE:
        case LSTORE:
        case FSTORE:
        case DSTORE:
        case ASTORE:
        case ISTORE_0:
        case ISTORE_1:
        case ISTORE_2:
        case ISTORE_3:
        case LSTORE_0:
        case LSTORE_1:
        case LSTORE_2:
        case LSTORE_3:
        case FSTORE_0:
        case FSTORE_1:
        case FSTORE_2:
        case FSTORE_3:
        case DSTORE_0:
        case DSTORE_1:
        case DSTORE_2:
        case DSTORE_3:
        case ASTORE_0:
        case ASTORE_1:
        case ASTORE_2:
        case ASTORE_3:
        case IASTORE:
        case LASTORE:
        case FASTORE:
        case DASTORE:
        case AASTORE:
        case BASTORE:
        case CASTORE:
        case SASTORE:
        case POP:
        case POP2:
        case DUP:
        case DUP_X1:
        case DUP_X2:
        case DUP2:
        case DUP2_X1:
        case DUP2_X2:
        case SWAP:
        case IADD:
        case LADD:
        case FADD:
        case DADD:
        case ISUB:
        case LSUB:
        case FSUB:
        case DSUB:
        case IMUL:
        case LMUL:
        case FMUL:
        case DMUL:
        case IDIV:
        case LDIV:
        case FDIV:
        case DDIV:
        case IREM:
        case LREM:
        case FREM:
        case DREM:
        case INEG:
        case LNEG:
        case FNEG:
        case DNEG:
        case ISHL:
        case LSHL:
        case ISHR:
        case LSHR:
        case IUSHR:
        case LUSHR:
        case IAND:
        case LAND:
        case IOR:
        case LOR:
        case IXOR:
        case LXOR:
        case IINC:
        case I2L:
        case I2F:
        case I2D:
        case L2I:
        case L2F:
        case L2D:
        case F2I:
        case F2L:
        case F2D:
        case D2I:
        case D2L:
        case D2F:
        case I2B:
        case I2C:
        case I2S:
        case LCMP:
        case FCMPL:
        case FCMPG:
        case DCMPL:
        case DCMPG:
        case IFEQ:
        case IFNE:
        case IFLT:
        case IFGE:
        case IFGT:
        case IFLE:
        case IF_ICMPEQ:
        case IF_ICMPNE:
        case IF_ICMPLT:
        case IF_ICMPGE:
        case IF_ICMPGT:
        case IF_ICMPLE:
        case IF_ACMPEQ:
        case IF_ACMPNE:
        case GOTO:
        case JSR:
        case RET:
        case TABLESWITCH:
        case LOOKUPSWITCH:
        case IRETURN:
        case LRETURN:
        case FRETURN:
        case DRETURN:
        case ARETURN:
        case RETURN:
        case GETSTATIC:
        case PUTSTATIC:
        case GETFIELD:
        case PUTFIELD:
        case INVOKEVIRTUAL:
        case INVOKESPECIAL:
        case INVOKESTATIC:
        case INVOKEINTERFACE:
        case XXXUNUSEDXXX:
        case NEW:
        case NEWARRAY:
        case ANEWARRAY:
        case ARRAYLENGTH:
        case ATHROW:
        case CHECKCAST:
        case INSTANCEOF:
        case MONITORENTER:
        case MONITOREXIT:
        case WIDE:
        case MULTIANEWARRAY:
        case IFNULL:
        case IFNONNULL:
        case GOTO_W:
        case JSR_W:
        case BREAKPOINT:
        case IMPDEP1:
        case IMPDEP2:
*/

}
