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

//import org.jpc.emulator.processor.*;
//import org.jpc.emulator.*;
import java.io.*;

/**
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
public abstract class FpuState implements Hibernatable {
    // stack depth (common to all x87 FPU's)
    /**
     *
     */
    public final static int STACK_DEPTH = 8;

    /**
     *
     */
    public static final int FPU_PRECISION_CONTROL_SINGLE = 0;
    /**
     *
     */
    public static final int FPU_PRECISION_CONTROL_DOUBLE = 2;
    /**
     *
     */
    public static final int FPU_PRECISION_CONTROL_EXTENDED = 3;

    /**
     *
     */
    public static final int FPU_ROUNDING_CONTROL_EVEN = 0;
    /**
     *
     */
    public static final int FPU_ROUNDING_CONTROL_DOWN = 1;
    /**
     *
     */
    public static final int FPU_ROUNDING_CONTROL_UP = 2;
    /**
     *
     */
    public static final int FPU_ROUNDING_CONTROL_TRUNCATE = 3;

    /**
     *
     */
    public static final int FPU_TAG_VALID = 0;
    /**
     *
     */
    public static final int FPU_TAG_ZERO = 1;
    /**
     *
     */
    public static final int FPU_TAG_SPECIAL = 2;
    /**
     *
     */
    public static final int FPU_TAG_EMPTY = 3;

    // status word
    // note exception bits are "sticky" - cleared only explicitly
    // accessors to flag an exception - these will set the bit,
    // check the mask, and throw a ProcessorException if unmasked
    /**
     *
     */
    public abstract void setInvalidOperation();

    /**
     *
     */
    public abstract void setDenormalizedOperand();

    /**
     *
     */
    public abstract void setZeroDivide();

    /**
     *
     */
    public abstract void setOverflow();

    /**
     *
     */
    public abstract void setUnderflow();

    /**
     *
     */
    public abstract void setPrecision();

    /**
     *
     */
    public abstract void setStackFault();

    /**
     *
     */
    public abstract void clearExceptions();

    /**
     *
     * @throws ProcessorException
     */
    public abstract void checkExceptions() throws ProcessorException;

    // read accessors
    /**
     *
     * @return -
     */
    public abstract boolean getInvalidOperation();

    /**
     *
     * @return -
     */
    public abstract boolean getDenormalizedOperand();

    /**
     *
     * @return -
     */
    public abstract boolean getZeroDivide();

    /**
     *
     * @return -
     */
    public abstract boolean getOverflow();

    /**
     *
     * @return -
     */
    public abstract boolean getUnderflow();

    /**
     *
     * @return -
     */
    public abstract boolean getPrecision();

    /**
     *
     * @return -
     */
    public abstract boolean getStackFault();

    /**
     *
     * @return -
     */
    public abstract boolean getErrorSummaryStatus(); // derived from other bits

    /**
     *
     * @return -
     */
    public abstract boolean getBusy();// same as fpuErrorSummaryStatus()
                                      // (legacy)

    /**
     *
     */
    public int conditionCode; // 4 bits
    /**
     *
     */
    public int top; // top of stack pointer (3 bits)

    // control word
    /**
     *
     * @return -
     */
    public abstract boolean getInvalidOperationMask();

    /**
     *
     * @return -
     */
    public abstract boolean getDenormalizedOperandMask();

    /**
     *
     * @return -
     */
    public abstract boolean getZeroDivideMask();

    /**
     *
     * @return -
     */
    public abstract boolean getOverflowMask();

    /**
     *
     * @return -
     */
    public abstract boolean getUnderflowMask();

    /**
     *
     * @return -
     */
    public abstract boolean getPrecisionMask();

    /**
     *
     */
    public boolean infinityControl; // legacy: not really used anymore

    /**
     *
     * @return -
     */
    public abstract int getPrecisionControl(); // 2 bits

    /**
     *
     * @return -
     */
    public abstract int getRoundingControl(); // 2 bits

    /**
     *
     * @param value
     */
    public abstract void setInvalidOperationMask(boolean value);

    /**
     *
     * @param value
     */
    public abstract void setDenormalizedOperandMask(boolean value);

    /**
     *
     * @param value
     */
    public abstract void setZeroDivideMask(boolean value);

    /**
     *
     * @param value
     */
    public abstract void setOverflowMask(boolean value);

    /**
     *
     * @param value
     */
    public abstract void setUnderflowMask(boolean value);

    /**
     *
     * @param value
     */
    public abstract void setPrecisionMask(boolean value);

    /**
     *
     * @param value
     */
    public abstract void setPrecisionControl(int value);

    /**
     *
     * @param value
     */
    public abstract void setRoundingControl(int value);

    /**
     *
     * @param value
     */
    public abstract void setAllMasks(boolean value);

    // other registers
    /**
     *
     */
    public long lastIP; // last instruction pointer
    /**
     *
     */
    public long lastData; // last data (operand) pointer
    /**
     *
     */
    public int lastOpcode; // 11 bits

    // x87 access
    /**
     *
     */
    public abstract void init();

    /**
     *
     * @param x
     * @throws ProcessorException
     */
    public abstract void push(double x) throws ProcessorException;

    /**
     *
     * @return -
     * @throws ProcessorException
     */
    public abstract double pop() throws ProcessorException;

    /**
     *
     * @param index
     * @return -
     * @throws ProcessorException
     */
    public abstract double ST(int index) throws ProcessorException;

    /**
     *
     * @param index
     * @param value
     */
    public abstract void setST(int index, double value);

    // public abstract void pushBig(BigDecimal x) throws ProcessorException;
    // public abstract BigDecimal popBig() throws ProcessorException;
    // public abstract BigDecimal bigST(int index) throws ProcessorException;
    // public abstract void setBigST(int index, BigDecimal value);
    /**
     *
     * @return -
     */
    public abstract int getStatus();

    /**
     *
     * @param w
     */
    public abstract void setStatus(int w);

    /**
     *
     * @return -
     */
    public abstract int getControl();

    /**
     *
     * @param w
     */
    public abstract void setControl(int w);

    /**
     *
     * @return -
     */
    public abstract int getTagWord();

    /**
     *
     * @param w
     */
    public abstract void setTagWord(int w);

    /**
     *
     * @param index
     * @return -
     */
    public abstract int getTag(int index);

    /**
     *
     * @param output
     * @throws IOException
     */
    public abstract void dumpState(DataOutput output) throws IOException;

    /**
     *
     * @param input
     * @throws IOException
     */
    public abstract void loadState(DataInput input) throws IOException;

    /**
     *
     * @param copy
     */
    public void copyStateInto(FpuState copy) {
        copy.conditionCode = conditionCode;
        copy.top = top;
        copy.infinityControl = infinityControl;
        copy.lastIP = lastIP;
        copy.lastData = lastData;
        copy.lastOpcode = lastOpcode;
    }

    @Override
    public boolean equals(Object another) {
        if (!(another instanceof FpuState))
            return false;
        FpuState s = (FpuState) another;
        if ((s.conditionCode != conditionCode) || (s.top != top)
                || (s.infinityControl != infinityControl)
                || (s.lastIP != lastIP) || (s.lastData != lastData)
                || (s.lastOpcode != lastOpcode))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + this.conditionCode;
        hash = 79 * hash + this.top;
        hash = 79 * hash + (this.infinityControl ? 1 : 0);
        hash = 79 * hash + (int) (this.lastIP ^ (this.lastIP >>> 32));
        hash = 79 * hash + (int) (this.lastData ^ (this.lastData >>> 32));
        hash = 79 * hash + this.lastOpcode;
        return hash;
    }
}
