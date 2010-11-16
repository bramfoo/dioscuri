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

/**
 * @author Bram Lohman
 * @author Bart Kiers
 */
@SuppressWarnings("serial")
public final class ProcessorException extends RuntimeException {
    private int vector;
    private int errorCode;
    private boolean pointsToSelf;
    private boolean hasErrorCode;

    /**
     * @param vector
     * @param errorCode
     * @param pointsToSelf
     */
    public ProcessorException(int vector, int errorCode, boolean pointsToSelf) {
        this.vector = vector;
        this.hasErrorCode = true;
        this.errorCode = errorCode;
        this.pointsToSelf = pointsToSelf;
    }

    /**
     * @param vector
     * @param pointsToSelf
     */
    public ProcessorException(int vector, boolean pointsToSelf) {
        this.vector = vector;
        this.hasErrorCode = false;
        this.errorCode = 0;
        this.pointsToSelf = pointsToSelf;
    }

    /**
     * @return -
     */
    public int getVector() {
        return vector;
    }

    /**
     * @return -
     */
    public boolean hasErrorCode() {
        return hasErrorCode;
    }

    /**
     * @return -
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * @return -
     */
    public boolean pointsToSelf() {
        return pointsToSelf;
    }

    private static final boolean isContributory(int vector) {
        switch (vector) {
            case Processor.PROC_EXCEPTION_DE:
            case Processor.PROC_EXCEPTION_TS:
            case Processor.PROC_EXCEPTION_NP:
            case Processor.PROC_EXCEPTION_SS:
            case Processor.PROC_EXCEPTION_GP:
                return true;
            default:
                return false;
        }
    }

    private static final boolean isPageFault(int vector) {
        return (vector == Processor.PROC_EXCEPTION_PF);
    }

    /**
     * @param vector
     * @return -
     */
    public boolean combinesToDoubleFault(int vector) {
        // Here we are the "second exception"
        return isContributory(vector)
                && isContributory(this.getVector())
                || isPageFault(vector)
                && (isContributory(this.getVector()) || isPageFault(this
                .getVector()));
    }

    @Override
    public String toString() {
        if (hasErrorCode())
            return "Processor Exception: " + getVector() + " [errorcode:0x"
                    + Integer.toHexString(getErrorCode()) + "]";
        else
            return "Processor Exception: " + getVector();
    }
}
