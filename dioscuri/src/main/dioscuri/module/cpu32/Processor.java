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

//import org.jpc.emulator.*;
//import org.jpc.emulator.motherboard.*;
//import org.jpc.emulator.memory.*;
//import org.jpc.emulator.processor.fpu64.*;
//import org.jpc.support.*;
import java.io.*;
import java.util.*;

import dioscuri.exception.ModuleException;
import dioscuri.exception.ModuleWriteOnlyPortException;
import dioscuri.module.Module;
import dioscuri.module.ModuleCPU;
import dioscuri.module.ModuleDevice;
import dioscuri.module.ModulePIC;

public class Processor extends ModuleCPU implements HardwareComponent {
    public static final int STATE_VERSION = 1;
    public static final int STATE_MINOR_VERSION = 0;

    public static final int CLOCK_SPEED = 50; // CPU "Clock Speed" in MHz

    public static final int IFLAGS_HARDWARE_INTERRUPT = 0x1;
    public static final int IFLAGS_PROCESSOR_EXCEPTION = 0x2;
    public static final int IFLAGS_RESET_REQUEST = 0x4;

    public static final int PROC_EXCEPTION_DE = 0x00; // Divide Error
    public static final int PROC_EXCEPTION_DB = 0x01; // Debug
    public static final int PROC_EXCEPTION_BP = 0x03; // Breakpoint
    public static final int PROC_EXCEPTION_OF = 0x04; // Overflow
    public static final int PROC_EXCEPTION_BR = 0x05; // BOUND Range Exceeded
    public static final int PROC_EXCEPTION_UD = 0x06; // Invalid Opcode
                                                      // (UnDefined)
    public static final int PROC_EXCEPTION_NM = 0x07; // Device Not Available
                                                      // (No Math Coprocessor)
    public static final int PROC_EXCEPTION_DF = 0x08; // Double Fault
    public static final int PROC_EXCEPTION_MF_09 = 0x09; // Coprocessor Segment
                                                         // Overrun
    public static final int PROC_EXCEPTION_TS = 0x0a; // Invalid TSS
    public static final int PROC_EXCEPTION_NP = 0x0b; // Segment Not Present
    public static final int PROC_EXCEPTION_SS = 0x0c; // Stack Segment Fault
    public static final int PROC_EXCEPTION_GP = 0x0d; // General Protection
    public static final int PROC_EXCEPTION_PF = 0x0e; // Page Fault
    public static final int PROC_EXCEPTION_MF_10 = 0x10; // Floating-Point Error
    public static final int PROC_EXCEPTION_AC = 0x11; // Alignment Check
    public static final int PROC_EXCEPTION_MC = 0x12; // Machine Check
    public static final int PROC_EXCEPTION_XF = 0x13; // SIMD Floating-Point
                                                      // Error
    public static final int PROC_EXCEPTION_MAX = 0x13; // Maximum exception
                                                       // vector value

    public static final int CR0_PROTECTION_ENABLE = 0x1;
    public static final int CR0_MONITOR_COPROCESSOR = 0x2;
    public static final int CR0_FPU_EMULATION = 0x4;
    public static final int CR0_TASK_SWITCHED = 0x8;
    public static final int CR0_NUMERIC_ERROR = 0x20;
    public static final int CR0_WRITE_PROTECT = 0x10000;
    public static final int CR0_ALIGNMENT_MASK = 0x40000;
    public static final int CR0_NOT_WRITETHROUGH = 0x20000000;
    public static final int CR0_CACHE_DISABLE = 0x40000000;
    public static final int CR0_PAGING = 0x80000000;

    public static final int CR3_PAGE_CACHE_DISABLE = 0x10;
    public static final int CR3_PAGE_WRITES_TRANSPARENT = 0x8;

    public static final int CR4_VIRTUAL8086_MODE_EXTENSIONS = 0x1;
    public static final int CR4_PROTECTED_MODE_VIRTUAL_INTERRUPTS = 0x2;
    public static final int CR4_TIME_STAMP_DISABLE = 0x4;
    public static final int CR4_DEBUGGING_EXTENSIONS = 0x8;
    public static final int CR4_PAGE_SIZE_EXTENSIONS = 0x10;
    public static final int CR4_PHYSICAL_ADDRESS_EXTENSION = 0x20;
    public static final int CR4_MACHINE_CHECK_ENABLE = 0x40;
    public static final int CR4_PAGE_GLOBAL_ENABLE = 0x80;
    public static final int CR4_PERFORMANCE_MONITORING_COUNTER_ENABLE = 0x100;
    public static final int CR4_OS_SUPPORT_FXSAVE_FXSTORE = 0x200;
    public static final int CR4_OS_SUPPORT_UNMASKED_SIMD_EXCEPTIONS = 0x400;

    public static final int SYSENTER_CS_MSR = 0x174;
    public static final int SYSENTER_ESP_MSR = 0x175;
    public static final int SYSENTER_EIP_MSR = 0x176;

    public int eax, ebx, edx, ecx;
    public int esi, edi, esp, ebp;
    public int eip;

    private int cr0, cr1, cr2, cr3, cr4;
    public int dr0, dr1, dr2, dr3, dr4, dr5, dr6, dr7;

    public Segment cs, ds, ss, es, fs, gs;
    public Segment idtr, gdtr, ldtr, tss;

    // protected int eflags;
    // program status and control register
    public boolean eflagsCarry; // done
    public boolean eflagsParity; // done
    public boolean eflagsAuxiliaryCarry; // done
    public boolean eflagsZero; // to do
    public boolean eflagsSign; // to do
    public boolean eflagsTrap;
    public boolean eflagsInterruptEnable;
    public boolean eflagsDirection;
    public boolean eflagsOverflow; // done
    public int eflagsIOPrivilegeLevel;
    public boolean eflagsNestedTask;
    public boolean eflagsResume;
    public boolean eflagsVirtual8086Mode;
    public boolean eflagsAlignmentCheck;
    public boolean eflagsVirtualInterrupt;
    public boolean eflagsVirtualInterruptPending;
    public boolean eflagsID;
    public boolean eflagsInterruptEnableSoon;

    public LinearAddressSpace linearMemory;
    public PhysicalAddressSpace physicalMemory;
    public AlignmentCheckedAddressSpace alignmentCheckedMemory;
    public IOPortHandler ioports;

    private int interruptFlags;
    private ModulePIC interruptController;
    // private InterruptController interruptController;
    // Seems unneeded
    // private Clock virtualClock;
    private boolean alignmentChecking;

    private Hashtable<Integer, Long> modelSpecificRegisters;

    private long resetTime;
    private int currentPrivilegeLevel;
    private boolean started = false;

    public FpuState fpu;

    public Processor() {
        fpu = new FpuState64(this);
        linearMemory = null;
        physicalMemory = null;
        alignmentCheckedMemory = null;
        ioports = null;
        alignmentChecking = false;
        modelSpecificRegisters = new Hashtable<Integer, Long>();
    }

    public void dumpState(DataOutput output) throws IOException {
        output.writeInt(this.eax);
        output.writeInt(this.ebx);
        output.writeInt(this.edx);
        output.writeInt(this.ecx);
        output.writeInt(this.esi);
        output.writeInt(this.edi);
        output.writeInt(this.esp);
        output.writeInt(this.ebp);
        output.writeInt(this.eip);
        output.writeInt(this.dr0);
        output.writeInt(this.dr1);
        output.writeInt(this.dr2);
        output.writeInt(this.dr3);
        output.writeInt(this.dr4);
        output.writeInt(this.dr5);
        output.writeInt(this.dr6);
        output.writeInt(this.dr7);
        output.writeInt(this.cr0);
        output.writeInt(this.cr1);
        output.writeInt(this.cr2);
        output.writeInt(this.cr3);
        output.writeInt(this.cr4);
        output.writeBoolean(this.eflagsCarry);
        output.writeBoolean(this.eflagsParity);
        output.writeBoolean(this.eflagsAuxiliaryCarry);
        output.writeBoolean(this.eflagsZero);
        output.writeBoolean(this.eflagsSign);
        output.writeBoolean(this.eflagsTrap);
        output.writeBoolean(this.eflagsInterruptEnable);
        output.writeBoolean(this.eflagsDirection);
        output.writeBoolean(this.eflagsOverflow);
        output.writeInt(this.eflagsIOPrivilegeLevel);
        output.writeBoolean(this.eflagsNestedTask);
        output.writeBoolean(this.eflagsResume);
        output.writeBoolean(this.eflagsVirtual8086Mode);
        output.writeBoolean(this.eflagsAlignmentCheck);
        output.writeBoolean(this.eflagsVirtualInterrupt);
        output.writeBoolean(this.eflagsVirtualInterruptPending);
        output.writeBoolean(this.eflagsID);
        output.writeBoolean(this.eflagsInterruptEnableSoon);
        fpu.dumpState(output);

        output.writeInt(interruptFlags);
        output.writeBoolean(alignmentChecking);
        output.writeLong(resetTime);
        output.writeInt(currentPrivilegeLevel);
        // modelSpecificRegisters hashtable
        output.writeInt(modelSpecificRegisters.size());
        Set<Map.Entry<Integer, Long>> entries = modelSpecificRegisters
                .entrySet();
        Iterator<Map.Entry<Integer, Long>> itt = entries.iterator();
        while (itt.hasNext()) {
            Map.Entry<Integer, Long> entry = itt.next();
            output.writeInt((entry.getKey()).intValue());
            output.writeLong((entry.getValue()).longValue());
        }
        cs.dumpState(output);
        ds.dumpState(output);
        ss.dumpState(output);
        es.dumpState(output);
        fs.dumpState(output);
        gs.dumpState(output);
        idtr.dumpState(output);
        gdtr.dumpState(output);
        ldtr.dumpState(output);
        tss.dumpState(output);
    }

    public void loadState(DataInput input) throws IOException {
        eax = input.readInt();
        ebx = input.readInt();
        edx = input.readInt();
        ecx = input.readInt();
        esi = input.readInt();
        edi = input.readInt();
        esp = input.readInt();
        ebp = input.readInt();
        eip = input.readInt();
        dr0 = input.readInt();
        dr1 = input.readInt();
        dr2 = input.readInt();
        dr3 = input.readInt();
        dr4 = input.readInt();
        dr5 = input.readInt();
        dr6 = input.readInt();
        dr7 = input.readInt();
        cr0 = input.readInt();
        cr1 = input.readInt();
        cr2 = input.readInt();
        cr3 = input.readInt();
        cr4 = input.readInt();
        eflagsCarry = input.readBoolean();
        eflagsParity = input.readBoolean();
        eflagsAuxiliaryCarry = input.readBoolean();
        eflagsZero = input.readBoolean();
        eflagsSign = input.readBoolean();
        eflagsTrap = input.readBoolean();
        eflagsInterruptEnable = input.readBoolean();
        eflagsDirection = input.readBoolean();
        eflagsOverflow = input.readBoolean();
        eflagsIOPrivilegeLevel = input.readInt();
        eflagsNestedTask = input.readBoolean();
        eflagsResume = input.readBoolean();
        eflagsVirtual8086Mode = input.readBoolean();
        eflagsAlignmentCheck = input.readBoolean();
        eflagsVirtualInterrupt = input.readBoolean();
        eflagsVirtualInterruptPending = input.readBoolean();
        eflagsID = input.readBoolean();
        eflagsInterruptEnableSoon = input.readBoolean();
        fpu.loadState(input);

        interruptFlags = input.readInt();
        alignmentChecking = input.readBoolean();
        resetTime = input.readLong();
        currentPrivilegeLevel = input.readInt();
        // modelSpecificRegisters hashtable
        int len = input.readInt();
        modelSpecificRegisters = new Hashtable<Integer, Long>();
        int key;
        long value;
        for (int i = 0; i < len; i++) {
            key = input.readInt();
            value = input.readLong();
            modelSpecificRegisters.put(new Integer(key), new Long(value));
        }
        cs = loadSegment(input);
        ds = loadSegment(input);
        ss = loadSegment(input);
        es = loadSegment(input);
        fs = loadSegment(input);
        gs = loadSegment(input);
        idtr = loadSegment(input);
        gdtr = loadSegment(input);
        ldtr = loadSegment(input);
        tss = loadSegment(input);
    }

    private Segment loadSegment(DataInput input) throws IOException {
        // isProtectedMode()
        // alignmentChecking
        int type = input.readInt();
        if (type == 1) {
            int selector = input.readInt();
            if (!isProtectedMode())
                return SegmentFactory.createRealModeSegment(physicalMemory,
                        selector);
            else {
                if (alignmentChecking)
                    return SegmentFactory.createRealModeSegment(
                            alignmentCheckedMemory, selector);
                else
                    return SegmentFactory.createRealModeSegment(linearMemory,
                            selector);
            }
        } else if (type == 2) {
            int base = input.readInt();
            int limit = input.readInt();
            if (!isProtectedMode())
                return SegmentFactory.createDescriptorTableSegment(
                        physicalMemory, base, limit);
            else {
                if (alignmentChecking)
                    return SegmentFactory.createDescriptorTableSegment(
                            alignmentCheckedMemory, base, limit);
                else
                    return SegmentFactory.createDescriptorTableSegment(
                            linearMemory, base, limit);
            }
        } else if (type == 3) {
            int selector = input.readInt();
            long descriptor = input.readLong();
            if (!isProtectedMode()) {
                System.out
                        .println("tried to load a Protected Mode segment in Real Mode");
                return null; // should never happen
            } else {
                if (alignmentChecking)
                    return SegmentFactory.createProtectedModeSegment(
                            alignmentCheckedMemory, selector, descriptor);
                else
                    return SegmentFactory.createProtectedModeSegment(
                            linearMemory, selector, descriptor);
            }
        } else if (type == 4) {
            return new SegmentFactory.NullSegment();
        } else
            throw new IOException();
    }

    public int getEFlags() {
        int result = 0x2;
        if (getCarryFlag())
            result |= 0x1;
        if (getParityFlag())
            result |= 0x4;
        if (getAuxiliaryCarryFlag())
            result |= 0x10;
        if (getZeroFlag())
            result |= 0x40;
        if (getSignFlag())
            result |= 0x80;
        if (eflagsTrap)
            result |= 0x100;
        if (eflagsInterruptEnable)
            result |= 0x200;
        if (eflagsDirection)
            result |= 0x400;
        if (getOverflowFlag())
            result |= 0x800;
        result |= (eflagsIOPrivilegeLevel << 12);
        if (eflagsNestedTask)
            result |= 0x4000;
        if (eflagsResume)
            result |= 0x10000;
        if (eflagsVirtual8086Mode)
            result |= 0x20000;
        if (eflagsAlignmentCheck)
            result |= 0x40000;
        if (eflagsVirtualInterrupt)
            result |= 0x80000;
        if (eflagsVirtualInterruptPending)
            result |= 0x100000;
        if (eflagsID)
            result |= 0x200000;

        return result;
    }

    public void setEFlags(int eflags) {
        // TODO: check that there aren't flags which can't be set this way!
        setCarryFlag((eflags & 1) != 0);
        setParityFlag((eflags & (1 << 2)) != 0);
        setAuxiliaryCarryFlag((eflags & (1 << 4)) != 0);
        setZeroFlag((eflags & (1 << 6)) != 0);
        setSignFlag((eflags & (1 << 7)) != 0);
        eflagsTrap = ((eflags & (1 << 8)) != 0);
        eflagsInterruptEnableSoon = eflagsInterruptEnable = ((eflags & (1 << 9)) != 0);
        eflagsDirection = ((eflags & (1 << 10)) != 0);
        setOverflowFlag((eflags & (1 << 11)) != 0);
        eflagsIOPrivilegeLevel = ((eflags >> 12) & 3);
        eflagsNestedTask = ((eflags & (1 << 14)) != 0);
        eflagsResume = ((eflags & (1 << 16)) != 0);

        eflagsVirtualInterrupt = ((eflags & (1 << 19)) != 0);
        eflagsVirtualInterruptPending = ((eflags & (1 << 20)) != 0);
        eflagsID = ((eflags & (1 << 21)) != 0);

        if (eflagsAlignmentCheck != ((eflags & (1 << 18)) != 0)) {
            eflagsAlignmentCheck = ((eflags & (1 << 18)) != 0);
            checkAlignmentChecking();
        }

        if (eflagsVirtual8086Mode != ((eflags & (1 << 17)) != 0)) {
            eflagsVirtual8086Mode = ((eflags & (1 << 17)) != 0);
            if (eflagsVirtual8086Mode) {
                throw ModeSwitchException.VIRTUAL8086_MODE_EXCEPTION;
            } else {
                throw ModeSwitchException.PROTECTED_MODE_EXCEPTION;
            }
        }
    }

    public void processClock() {
        System.out.println("JPC CPU called Clock.process()");
        // virtualClock.process();
    }

    public void setCPL(int value) {
        currentPrivilegeLevel = value;
        linearMemory.setSupervisor(currentPrivilegeLevel == 0);
        checkAlignmentChecking();
    }

    public int getCPL() {
        return currentPrivilegeLevel;
    }

    public void reportFPUException() {
        if ((cr0 & CR0_NUMERIC_ERROR) == 0) {
            System.err.println("Reporting FPU Error Via IRQ#13");
            // interruptController.setIRQ(13, 1);
            interruptController.setIRQ(13);
        } else {
            System.err.println("Reporting FPU Error Via Exception 0x10");
            throw new ProcessorException(Processor.PROC_EXCEPTION_MF_10, true);
        }
    }

    public void raiseInterrupt() {
        synchronized (this) {
            interruptFlags |= IFLAGS_HARDWARE_INTERRUPT;
            this.notifyAll();
        }
    }

    public void clearInterrupt() {
        interruptFlags &= ~IFLAGS_HARDWARE_INTERRUPT;
    }

    public boolean waitForInterrupt(long time) {
        synchronized (this) {
            if ((interruptFlags & IFLAGS_HARDWARE_INTERRUPT) != 0)
                return true;

            try {
                this.wait(time);
            } catch (InterruptedException e) {
            }
            ;
            return ((interruptFlags & IFLAGS_HARDWARE_INTERRUPT) != 0);
        }
    }

    public void requestReset() {
        interruptFlags |= IFLAGS_RESET_REQUEST;
    }

    public int getInterruptFlags() {
        return interruptFlags;
    }

    public boolean isProtectedMode() {
        return (cr0 & CR0_PROTECTION_ENABLE) == 1;
    }

    public boolean isVirtual8086Mode() {
        return eflagsVirtual8086Mode;
    }

    // Need to think about the TS flag for when we have an FPU - Section 2.5 Vol
    // 3
    public void setCR0(int value) {
        value |= 0x10;
        int changedBits = value ^ cr0;
        if (changedBits == 0)
            return;

        // actually set the value!
        cr0 = value;

        boolean pagingChanged = (changedBits & CR0_PAGING) != 0;
        boolean cachingChanged = (changedBits & CR0_CACHE_DISABLE) != 0;
        boolean modeSwitch = (changedBits & CR0_PROTECTION_ENABLE) != 0;
        boolean wpUserPagesChanged = (changedBits & CR0_WRITE_PROTECT) != 0;
        boolean alignmentChanged = (changedBits & CR0_ALIGNMENT_MASK) != 0;

        if ((changedBits & CR0_NOT_WRITETHROUGH) != 0)
            System.err.println("INFO: Unimplemented CR0 flags changed ("
                    + Integer.toHexString(changedBits) + "). Now is "
                    + Integer.toHexString(value));

        if (pagingChanged) {
            if (((value & CR0_PROTECTION_ENABLE) == 0)
                    && ((value & CR0_PAGING) != 0))
                throw new ProcessorException(PROC_EXCEPTION_GP, 0, true);
        }

        if (alignmentChanged)
            checkAlignmentChecking();

        if (pagingChanged || cachingChanged) {
            linearMemory.setPagingEnabled((value & CR0_PAGING) != 0);
            linearMemory.setPageCacheEnabled((value & CR0_CACHE_DISABLE) == 0);
        }

        if (wpUserPagesChanged)
            linearMemory
                    .setWriteProtectUserPages((value & CR0_WRITE_PROTECT) != 0);

        if (modeSwitch) {
            if ((value & CR0_PROTECTION_ENABLE) != 0) {
                convertSegmentsToProtectedMode();
                throw ModeSwitchException.PROTECTED_MODE_EXCEPTION;
            } else {
                // linearMemory.flush();
                setCPL(0);
                convertSegmentsToRealMode();
                throw ModeSwitchException.REAL_MODE_EXCEPTION;
            }
        }
    }

    public int getCR0() {
        return cr0;
    }

    public void setCR3(int value) {
        cr3 = value;
        linearMemory
                .setPageWriteThroughEnabled((value & CR3_PAGE_WRITES_TRANSPARENT) != 0);
        linearMemory.setPageCacheEnabled((value & CR3_PAGE_CACHE_DISABLE) == 0);
        linearMemory.setPageDirectoryBaseAddress(value);
    }

    public int getCR3() {
        return cr3;
    }

    public int getCR2() {
        return cr2;
    }

    public void setCR2(int value) {
        cr2 = value;
    }

    public void setCR4(int value) {
        if (cr4 == value)
            return;

        cr4 = value;
        if ((cr4 & CR4_VIRTUAL8086_MODE_EXTENSIONS) != 0)
            System.err
                    .println("WARNING: Virtual 8086 Mode Extensions enabled in the processor");
        if ((cr4 & CR4_PROTECTED_MODE_VIRTUAL_INTERRUPTS) != 0)
            System.err
                    .println("WARNING: Protected Mode Virtual Interrupts enabled in the processor");
        if ((cr4 & CR4_OS_SUPPORT_UNMASKED_SIMD_EXCEPTIONS) != 0)
            System.err
                    .println("WARNING: SIMD instruction support modified in the processor");
        if ((cr4 & CR4_OS_SUPPORT_FXSAVE_FXSTORE) != 0)
            System.err
                    .println("WARNING: FXSave and FXRStore flag enabled in the processor");
        if ((cr4 & CR4_DEBUGGING_EXTENSIONS) != 0)
            System.err.println("WARNING: debugging extensions enabled");
        if ((cr4 & CR4_TIME_STAMP_DISABLE) != 0)
            System.err.println("WARNING: timestamp restricted to CPL0");
        if ((cr4 & CR4_PHYSICAL_ADDRESS_EXTENSION) != 0)
            throw new IllegalStateException("36 Bit Addressing enabled");

        linearMemory
                .setGlobalPagesEnabled((value & CR4_PAGE_GLOBAL_ENABLE) != 0);
        linearMemory
                .setPageSizeExtensionsEnabled((cr4 & CR4_PAGE_SIZE_EXTENSIONS) != 0);
    }

    public int getCR4() {
        return cr4;
    }

    public void setDR0(int value) {
        dr0 = value;
    }

    public void setDR1(int value) {
        dr1 = value;
    }

    public void setDR2(int value) {
        dr2 = value;
    }

    public void setDR3(int value) {
        dr3 = value;
    }

    public void setDR6(int value) {
        dr6 = value;
    }

    public void setDR7(int value) {
        dr7 = value;
    }

    public int getDR0() {
        return dr0;
    }

    public int getDR1() {
        return dr1;
    }

    public int getDR2() {
        return dr2;
    }

    public int getDR3() {
        return dr3;
    }

    public int getDR6() {
        return dr6;
    }

    public int getDR7() {
        return dr7;
    }

    public long getMSR(int index) {
        try {
            return ((Long) modelSpecificRegisters.get(new Integer(index)))
                    .longValue();
        } catch (NullPointerException e) {
            System.err.println("Reading unset MSR " + index + " : Returning 0");
            return 0l;
        }

    }

    public void setMSR(int index, long value) {
        modelSpecificRegisters.put(new Integer(index), new Long(value));
    }

    private void convertSegmentsToRealMode() {
        try {
            cs = createRealModeSegment(cs.translateAddressRead(0) >>> 4);
        } catch (ProcessorException e) {
            cs = createRealModeSegment(0);
        }
        try {
            ds = createRealModeSegment(ds.translateAddressRead(0) >>> 4);
        } catch (ProcessorException e) {
            ds = createRealModeSegment(0);
        }
        try {
            ss = createRealModeSegment(ss.translateAddressRead(0) >>> 4);
        } catch (ProcessorException e) {
            ss = createRealModeSegment(0);
        }
        try {
            es = createRealModeSegment(es.translateAddressRead(0) >>> 4);
        } catch (ProcessorException e) {
            es = createRealModeSegment(0);
        }
        try {
            fs = createRealModeSegment(fs.translateAddressRead(0) >>> 4);
        } catch (ProcessorException e) {
            fs = createRealModeSegment(0);
        }
        try {
            gs = createRealModeSegment(gs.translateAddressRead(0) >>> 4);
        } catch (ProcessorException e) {
            gs = createRealModeSegment(0);
        }
    }

    private void convertSegmentsToProtectedMode() {
        cs.setAddressSpace(linearMemory);
        ds.setAddressSpace(linearMemory);
        ss.setAddressSpace(linearMemory);
        es.setAddressSpace(linearMemory);
        fs.setAddressSpace(linearMemory);
        gs.setAddressSpace(linearMemory);
    }

    private void updateAlignmentCheckingInDataSegments() {
        if (alignmentChecking) {
            ds.setAddressSpace(alignmentCheckedMemory);
            ss.setAddressSpace(alignmentCheckedMemory);
            es.setAddressSpace(alignmentCheckedMemory);
            fs.setAddressSpace(alignmentCheckedMemory);
            gs.setAddressSpace(alignmentCheckedMemory);
        } else {
            ds.setAddressSpace(linearMemory);
            ss.setAddressSpace(linearMemory);
            es.setAddressSpace(linearMemory);
            fs.setAddressSpace(linearMemory);
            gs.setAddressSpace(linearMemory);
        }
    }

    public Segment createRealModeSegment(int selector) {
        return SegmentFactory.createRealModeSegment(physicalMemory, selector);
    }

    public Segment createDescriptorTableSegment(int base, int limit) {
        return SegmentFactory.createDescriptorTableSegment(linearMemory, base,
                limit);
    }

    public void correctAlignmentChecking(Segment segment) {
        if (alignmentChecking) {
            if ((segment.getType() & 0x18) == 0x10) // Should make this a data
                                                    // segment
                segment.setAddressSpace(alignmentCheckedMemory);
        }
    }

    public Segment getSegment(int segmentSelector) {
        boolean isSup = linearMemory.isSupervisor();
        try {
            long segmentDescriptor = 0;
            linearMemory.setSupervisor(true);
            if ((segmentSelector & 0x4) != 0)
                segmentDescriptor = ldtr.getQuadWord(segmentSelector & 0xfff8);
            else {
                if (segmentSelector < 0x4)
                    return SegmentFactory.NULL_SEGMENT;
                segmentDescriptor = gdtr.getQuadWord(segmentSelector & 0xfff8);
            }

            Segment result = SegmentFactory.createProtectedModeSegment(
                    linearMemory, segmentSelector, segmentDescriptor);
            if (alignmentChecking) {
                if ((result.getType() & 0x18) == 0x10) // Should make this a
                                                       // data segment
                    result.setAddressSpace(alignmentCheckedMemory);
            }

            return result;
        } finally {
            linearMemory.setSupervisor(isSup);
        }
    }

    public Segment getSegment(int segmentSelector, Segment local, Segment global) {
        boolean isSup = linearMemory.isSupervisor();
        try {
            long segmentDescriptor = 0;
            linearMemory.setSupervisor(true);
            if ((segmentSelector & 0x4) != 0)
                segmentDescriptor = local.getQuadWord(segmentSelector & 0xfff8);
            else {
                if (segmentSelector < 0x4)
                    return SegmentFactory.NULL_SEGMENT;
                segmentDescriptor = global
                        .getQuadWord(segmentSelector & 0xfff8);
            }

            Segment result = SegmentFactory.createProtectedModeSegment(
                    linearMemory, segmentSelector, segmentDescriptor);
            if (alignmentChecking) {
                if ((result.getType() & 0x18) == 0x10) // Should make this a
                                                       // data segment
                    result.setAddressSpace(alignmentCheckedMemory);
            }

            return result;
        } finally {
            linearMemory.setSupervisor(isSup);
        }
    }

    public boolean reset() {
        resetTime = System.currentTimeMillis();
        eax = ebx = ecx = edx = 0;
        edi = esi = ebp = esp = 0;
        edx = 0x00000633; // Pentium II Model 3 Stepping 3

        interruptFlags = 0;
        currentPrivilegeLevel = 0;
        linearMemory.reset();
        alignmentChecking = false;

        eip = 0x0000fff0;

        cr0 = CR0_CACHE_DISABLE | CR0_NOT_WRITETHROUGH | 0x10;
        cr2 = cr3 = cr4 = 0x0;

        dr0 = dr1 = dr2 = dr3 = 0x0;
        dr6 = 0xffff0ff0;
        dr7 = 0x00000700;

        eflagsCarry = eflagsParity = eflagsAuxiliaryCarry = eflagsZero = eflagsSign = eflagsTrap = eflagsInterruptEnable = false;
        carryCalculated = parityCalculated = auxiliaryCarryCalculated = zeroCalculated = signCalculated = true;
        eflagsDirection = eflagsOverflow = eflagsNestedTask = eflagsResume = eflagsVirtual8086Mode = false;
        overflowCalculated = true;

        eflagsAlignmentCheck = eflagsVirtualInterrupt = eflagsVirtualInterruptPending = eflagsID = false;

        eflagsIOPrivilegeLevel = 0;
        eflagsZero = true; // eflags = 0x00000020;
        eflagsInterruptEnableSoon = false;

        cs = createRealModeSegment(0xf000);
        ds = createRealModeSegment(0);
        ss = createRealModeSegment(0);
        es = createRealModeSegment(0);
        fs = createRealModeSegment(0);
        gs = createRealModeSegment(0);

        idtr = SegmentFactory.createDescriptorTableSegment(physicalMemory, 0,
                0xFFFF);
        ldtr = SegmentFactory.NULL_SEGMENT;
        gdtr = SegmentFactory.createDescriptorTableSegment(physicalMemory, 0,
                0xFFFF);
        tss = SegmentFactory.NULL_SEGMENT;

        modelSpecificRegisters.clear();
        // Will need to set any MSRs here

        fpu.init();

        return true;
    }

    public long getClockCount() {
        return (System.currentTimeMillis() - resetTime) * 1000
                * Processor.CLOCK_SPEED;
    }

    public final int getInstructionPointer() {
        return cs.translateAddressRead(eip);
    }

    public final void processRealModeInterrupts() {
        if (eflagsInterruptEnable) {

            if ((interruptFlags & IFLAGS_RESET_REQUEST) != 0) {
                reset();
                return;
            }

            if ((interruptFlags & IFLAGS_HARDWARE_INTERRUPT) != 0) {
                interruptFlags &= ~IFLAGS_HARDWARE_INTERRUPT;
                // int vector = interruptController.cpuGetInterrupt();
                int vector = interruptController.interruptAcknowledge();
                handleRealModeInterrupt(vector);
            }
        }
        eflagsInterruptEnable = eflagsInterruptEnableSoon;
    }

    public final void processProtectedModeInterrupts() {
        if (eflagsInterruptEnable) {

            if ((interruptFlags & IFLAGS_RESET_REQUEST) != 0) {
                reset();
                return;
            }

            if ((interruptFlags & IFLAGS_HARDWARE_INTERRUPT) != 0) {
                interruptFlags &= ~IFLAGS_HARDWARE_INTERRUPT;
                // handleHardProtectedModeInterrupt(interruptController.cpuGetInterrupt());
                handleHardProtectedModeInterrupt(interruptController
                        .interruptAcknowledge());
            }
        }
        eflagsInterruptEnable = eflagsInterruptEnableSoon;
    }

    public final void processVirtual8086ModeInterrupts() {
        if (eflagsInterruptEnable) {

            if ((interruptFlags & IFLAGS_RESET_REQUEST) != 0) {
                reset();
                return;
            }

            if ((interruptFlags & IFLAGS_HARDWARE_INTERRUPT) != 0) {
                interruptFlags &= ~IFLAGS_HARDWARE_INTERRUPT;
                if ((getCR4() & CR4_VIRTUAL8086_MODE_EXTENSIONS) != 0)
                    throw new IllegalStateException();
                else
                    // handleHardVirtual8086ModeInterrupt(interruptController.cpuGetInterrupt());
                    handleHardVirtual8086ModeInterrupt(interruptController
                            .interruptAcknowledge());

            }
        }
        eflagsInterruptEnable = eflagsInterruptEnableSoon;
    }

    public final void handleRealModeException(int vector) {
        if (vector <= PROC_EXCEPTION_MAX)
            handleRealModeInterrupt(vector);
    }

    private final void handleRealModeInterrupt(int vector) {
        // System.out.println("Real Mode execption " +
        // Integer.toHexString(vector));

        // TODO: the following is for real-mode interrupts.
        // probably need to check mode here and act accordingly.
        // if ((esp & 0xffff) < 6 && (esp & 0xffff) != 0)
        if (((esp & 0xffff) < 6) && ((esp & 0xffff) > 0)) {
            throw new IllegalStateException(
                    "SS Processor Exception Thrown in \"handleInterrupt("
                            + vector + ")\"");
            // throw new ProcessorException(ProcessorException.SS);
            // maybe just change vector value
        }
        // hmm...an exception in an interrupt handler? maybe not...

        vector *= 4;
        int newEip = 0xffff & idtr.getWord(vector);
        int newSelector = 0xffff & idtr.getWord(vector + 2);

        short sesp = (short) esp;
        sesp -= 2;
        int eflags = getEFlags() & 0xffff;
        ss.setWord(sesp & 0xffff, (short) eflags);
        eflagsInterruptEnable = false;
        eflagsInterruptEnableSoon = false;
        eflagsTrap = false;
        eflagsAlignmentCheck = false;
        sesp -= 2;
        ss.setWord(sesp & 0xffff, (short) cs.getSelector());
        sesp -= 2;
        ss.setWord(sesp & 0xffff, (short) eip);
        esp = (0xFFFF0000 & esp) | (sesp & 0xFFFF);
        // read interrupt vector
        eip = newEip;

        if (!cs.setSelector(newSelector)) {
            cs = createRealModeSegment(newSelector);
            setCPL(0);
        }
    }

    public final void handleProtectedModeException(int vector,
            boolean hasErrorCode, int errorCode) {
        int savedESP = esp;
        int savedEIP = eip;
        Segment savedCS = cs;
        Segment savedSS = ss;

        try {
            followProtectedModeException(vector, hasErrorCode, errorCode,
                    false, false);
        } catch (ProcessorException e) {
            e.printStackTrace();
            // return cpu to original state
            esp = savedESP;
            eip = savedEIP;
            cs = savedCS;
            ss = savedSS;

            if (vector == PROC_EXCEPTION_DF) {
                System.err
                        .println("Triple-Fault: Unhandleable, machine will halt!");
                throw new IllegalStateException("Triple Fault " + e);
            } else if (e.combinesToDoubleFault(vector)) {
                System.err.println(vector);
                handleProtectedModeException(PROC_EXCEPTION_DF, true, 0);
            } else
                handleProtectedModeException(e.getVector(), e.hasErrorCode(), e
                        .getErrorCode());
        }
    }

    public final void handleSoftProtectedModeInterrupt(int vector) {
        int savedESP = esp;
        int savedEIP = eip;
        Segment savedCS = cs;
        Segment savedSS = ss;

        try {
            followProtectedModeException(vector, false, 0, false, true);
        } catch (ProcessorException e) {
            // return cpu to original state
            esp = savedESP;
            eip = savedEIP;
            cs = savedCS;
            ss = savedSS;

            // if (e.getVector() == PROC_EXCEPTION_DF) {
            // System.err.println("Triple-Fault: Unhandleable, machine will halt!");
            // throw new IllegalStateException("Triple Fault");
            // else
            handleProtectedModeException(e.getVector(), e.hasErrorCode(), e
                    .getErrorCode());
        }
    }

    public final void handleHardProtectedModeInterrupt(int vector) {
        int savedESP = esp;
        int savedEIP = eip;
        Segment savedCS = cs;
        Segment savedSS = ss;

        try {
            followProtectedModeException(vector, false, 0, true, false);
        } catch (ProcessorException e) {
            // return cpu to original state
            esp = savedESP;
            eip = savedEIP;
            cs = savedCS;
            ss = savedSS;

            // if (e.getVector() == PROC_EXCEPTION_DF) {
            // System.err.println("Triple-Fault: Unhandleable, machine will halt!");
            // throw new IllegalStateException("Triple Fault");
            // else
            handleProtectedModeException(e.getVector(), e.hasErrorCode(), e
                    .getErrorCode());
        }
    }

    private final void checkGate(Segment gate, int selector, boolean software) {
        if (software) {
            if (gate.getDPL() < currentPrivilegeLevel)
                throw new ProcessorException(PROC_EXCEPTION_GP, selector + 2,
                        true);
        }

        if (!gate.isPresent())
            throw new ProcessorException(PROC_EXCEPTION_NP, selector, true);
    }

    private final void followProtectedModeException(int vector,
            boolean hasErrorCode, int errorCode, boolean hardware,
            boolean software) {
        // System.out.println("protected Mode exception " +
        // Integer.toHexString(vector));
        // System.out.println(Integer.toHexString(cs.getBase()) +":"
        // +Integer.toHexString(eip));
        // Thread.dumpStack();
        if (vector == PROC_EXCEPTION_PF)
            setCR2(linearMemory.getLastWalkedAddress());

        int selector = vector << 3; // multiply by 8 to get offset into idt
        int EXT = hardware ? 1 : 0;

        Segment gate;
        boolean isSup = linearMemory.isSupervisor();
        try {
            linearMemory.setSupervisor(true);
            long descriptor = idtr.getQuadWord(selector);
            gate = SegmentFactory.createProtectedModeSegment(linearMemory,
                    selector, descriptor);
        } catch (ProcessorException e) {
            throw new ProcessorException(PROC_EXCEPTION_GP, selector + 2 + EXT,
                    true);
        } finally {
            linearMemory.setSupervisor(isSup);
        }

        switch (gate.getType()) {
        default:
            System.err.println("Invalid Gate Type For Throwing Interrupt: 0x"
                    + Integer.toHexString(gate.getType()));
            throw new ProcessorException(PROC_EXCEPTION_GP, selector + 2 + EXT,
                    true);
        case 0x05: // Interrupt Handler: Task Gate
            System.out.println("Unimplemented Interrupt Handler: Task Gate");
            throw new IllegalStateException(
                    "Unimplemented Interrupt Handler: Task Gate");
        case 0x06: // Interrupt Handler: 16-bit Interrupt Gate
            System.out
                    .println("Unimplemented Interrupt Handler: 16-bit Interrupt Gate");
            throw new IllegalStateException(
                    "Unimplemented Interrupt Handler: 16-bit Interrupt Gate");
        case 0x07: // Interrupt Handler: 16-bit Trap Gate
            System.out
                    .println("Unimplemented Interrupt Handler: 16-bit Trap Gate");
            throw new IllegalStateException(
                    "Unimplemented Interrupt Handler: 16-bit Trap Gate");
        case 0x0e: // Interrupt Handler: 32-bit Interrupt Gate
        {
            SegmentFactory.InterruptGate32Bit interruptGate = ((SegmentFactory.InterruptGate32Bit) gate);

            checkGate(gate, selector, software);

            int targetSegmentSelector = interruptGate.getTargetSegment();

            Segment targetSegment;
            try {
                targetSegment = getSegment(targetSegmentSelector);
            } catch (ProcessorException e) {
                throw new ProcessorException(PROC_EXCEPTION_GP,
                        targetSegmentSelector + EXT, true);
            }

            if (targetSegment.getDPL() > currentPrivilegeLevel)
                throw new ProcessorException(PROC_EXCEPTION_GP,
                        targetSegmentSelector + EXT, true);

            switch (targetSegment.getType()) {
            default:
                throw new ProcessorException(PROC_EXCEPTION_GP,
                        targetSegmentSelector + EXT, true);

            case 0x18: // Code, Execute-Only
            case 0x19: // Code, Execute-Only, Accessed
            case 0x1a: // Code, Execute/Read
            case 0x1b: // Code, Execute/Read, Accessed
            {
                if (!targetSegment.isPresent())
                    throw new ProcessorException(PROC_EXCEPTION_NP,
                            targetSegmentSelector + EXT, true);

                if (targetSegment.getDPL() < currentPrivilegeLevel) {
                    // INTER-PRIVILEGE-LEVEL
                    int newStackSelector = 0;
                    int newESP = 0;
                    if ((tss.getType() & 0x8) != 0) {
                        int tssStackAddress = (targetSegment.getDPL() * 8) + 4;
                        if ((tssStackAddress + 7) > tss.getLimit())
                            throw new ProcessorException(PROC_EXCEPTION_TS, tss
                                    .getSelector(), true);

                        isSup = linearMemory.isSupervisor();
                        try {
                            linearMemory.setSupervisor(true);
                            newStackSelector = 0xffff & tss
                                    .getWord(tssStackAddress + 4);
                            newESP = tss.getDoubleWord(tssStackAddress);
                        } finally {
                            linearMemory.setSupervisor(isSup);
                        }
                    } else {
                        int tssStackAddress = (targetSegment.getDPL() * 4) + 2;
                        if ((tssStackAddress + 4) > tss.getLimit())
                            throw new ProcessorException(PROC_EXCEPTION_TS, tss
                                    .getSelector(), true);
                        newStackSelector = 0xffff & tss
                                .getWord(tssStackAddress + 2);
                        newESP = 0xffff & tss.getWord(tssStackAddress);
                    }

                    Segment newStackSegment = null;
                    try {
                        newStackSegment = getSegment(newStackSelector);
                    } catch (ProcessorException e) {
                        throw new ProcessorException(PROC_EXCEPTION_TS,
                                newStackSelector, true);
                    }

                    if (newStackSegment.getRPL() != targetSegment.getDPL())
                        throw new ProcessorException(PROC_EXCEPTION_TS,
                                newStackSelector, true);

                    if ((newStackSegment.getDPL() != targetSegment.getDPL())
                            || ((newStackSegment.getType() & 0x1a) != 0x12))
                        throw new ProcessorException(PROC_EXCEPTION_TS,
                                newStackSelector, true);

                    if (!(newStackSegment.isPresent()))
                        throw new ProcessorException(PROC_EXCEPTION_SS,
                                newStackSelector, true);

                    if (hasErrorCode) {
                        if ((newStackSegment.getDefaultSizeFlag() && (esp < 24) && (esp > 0))
                                || !newStackSegment.getDefaultSizeFlag()
                                && ((esp & 0xffff) < 24)
                                && ((esp & 0xffff) > 0))
                            throw new ProcessorException(PROC_EXCEPTION_SS, 0,
                                    true);
                    } else {
                        if ((newStackSegment.getDefaultSizeFlag() && (esp < 20) && (esp > 0))
                                || !newStackSegment.getDefaultSizeFlag()
                                && ((esp & 0xffff) < 20)
                                && ((esp & 0xffff) > 0))
                            throw new ProcessorException(PROC_EXCEPTION_SS, 0,
                                    true);
                    }

                    int targetOffset = interruptGate.getTargetOffset();
                    targetSegment.checkAddress(targetOffset);

                    int oldSS = ss.getSelector();
                    int oldESP = esp;
                    int oldCS = cs.getSelector();
                    int oldEIP = eip;
                    ss = newStackSegment;
                    esp = newESP;

                    cs = targetSegment;
                    eip = targetOffset;
                    setCPL(cs.getDPL());

                    if (ss.getDefaultSizeFlag()) {
                        esp -= 4;
                        ss.setDoubleWord(esp, oldSS);
                        esp -= 4;
                        ss.setDoubleWord(esp, oldESP);
                        esp -= 4;
                        ss.setDoubleWord(esp, getEFlags());
                        esp -= 4;
                        ss.setDoubleWord(esp, oldCS);
                        esp -= 4;
                        ss.setDoubleWord(esp, oldEIP);
                        if (hasErrorCode) {
                            esp -= 4;
                            ss.setDoubleWord(esp, errorCode);
                        }
                    } else {
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, oldSS);
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, oldESP);
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, getEFlags());
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, oldCS);
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, oldEIP);
                        if (hasErrorCode) {
                            esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                            ss.setDoubleWord(esp & 0xffff, errorCode);
                        }
                    }

                    eflagsInterruptEnable = eflagsInterruptEnableSoon = false;

                    eflagsTrap = false;
                    eflagsNestedTask = false;
                    eflagsVirtual8086Mode = false;
                    eflagsResume = false;
                } else if (targetSegment.getDPL() == currentPrivilegeLevel) {
                    // SAME-PRIVILEGE
                    // check there is room on stack
                    if (hasErrorCode) {
                        if ((ss.getDefaultSizeFlag() && (esp < 16) && (esp > 0))
                                || !ss.getDefaultSizeFlag()
                                && ((esp & 0xffff) < 16)
                                && ((esp & 0xffff) > 0))
                            throw new ProcessorException(PROC_EXCEPTION_SS, 0,
                                    true);
                    } else {
                        if ((ss.getDefaultSizeFlag() && (esp < 12) && (esp > 0))
                                || !ss.getDefaultSizeFlag()
                                && ((esp & 0xffff) < 12)
                                && ((esp & 0xffff) > 0))
                            throw new ProcessorException(PROC_EXCEPTION_SS, 0,
                                    true);
                    }

                    int targetOffset = interruptGate.getTargetOffset();
                    targetSegment.checkAddress(targetOffset);

                    if (ss.getDefaultSizeFlag()) {
                        esp -= 4;
                        ss.setDoubleWord(esp, getEFlags());
                        esp -= 4;
                        ss.setDoubleWord(esp, cs.getSelector());
                        esp -= 4;
                        ss.setDoubleWord(esp, eip);
                        if (hasErrorCode) {
                            esp -= 4;
                            ss.setDoubleWord(esp, errorCode);
                        }
                    } else {
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, getEFlags());
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, cs.getSelector());
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, eip);
                        if (hasErrorCode) {
                            esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                            ss.setDoubleWord(esp & 0xffff, errorCode);
                        }
                    }

                    cs = targetSegment;
                    eip = targetOffset;

                    cs.setRPL(currentPrivilegeLevel);
                    eflagsInterruptEnable = eflagsInterruptEnableSoon = false;

                    eflagsTrap = false;
                    eflagsNestedTask = false;
                    eflagsVirtual8086Mode = false;
                    eflagsResume = false;
                } else {
                    throw new ProcessorException(PROC_EXCEPTION_GP,
                            targetSegmentSelector + EXT, true);
                }
            }
                break;
            case 0x1c: // Code: Execute-Only, Conforming
            case 0x1d: // Code: Execute-Only, Conforming, Accessed
            case 0x1e: // Code: Execute/Read, Conforming
            case 0x1f: // Code: Execute/Read, Conforming, Accessed
            {
                if (!targetSegment.isPresent())
                    throw new ProcessorException(PROC_EXCEPTION_NP, selector,
                            true);

                // SAME-PRIVILEGE
                // check there is room on stack
                if (hasErrorCode) {
                    if ((ss.getDefaultSizeFlag() && (esp < 16) && (esp > 0))
                            || !ss.getDefaultSizeFlag()
                            && ((esp & 0xffff) < 16) && ((esp & 0xffff) > 0))
                        throw new ProcessorException(PROC_EXCEPTION_SS, 0, true);
                } else {
                    if ((ss.getDefaultSizeFlag() && (esp < 12) && (esp > 0))
                            || !ss.getDefaultSizeFlag()
                            && ((esp & 0xffff) < 12) && ((esp & 0xffff) > 0))
                        throw new ProcessorException(PROC_EXCEPTION_SS, 0, true);
                }

                int targetOffset = interruptGate.getTargetOffset();

                targetSegment.checkAddress(targetOffset);

                if (ss.getDefaultSizeFlag()) {
                    esp -= 4;
                    ss.setDoubleWord(esp, getEFlags());
                    esp -= 4;
                    ss.setDoubleWord(esp, cs.getSelector());
                    esp -= 4;
                    ss.setDoubleWord(esp, eip);
                    if (hasErrorCode) {
                        esp -= 4;
                        ss.setDoubleWord(esp, errorCode);
                    }
                } else {
                    esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                    ss.setDoubleWord(esp & 0xffff, getEFlags());
                    esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                    ss.setDoubleWord(esp & 0xffff, cs.getSelector());
                    esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                    ss.setDoubleWord(esp & 0xffff, eip);
                    if (hasErrorCode) {
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, errorCode);
                    }
                }

                cs = targetSegment;
                eip = targetOffset;

                cs.setRPL(currentPrivilegeLevel);

                eflagsInterruptEnable = eflagsInterruptEnableSoon = false;
                eflagsTrap = false;
                eflagsNestedTask = false;
                eflagsVirtual8086Mode = false;
                eflagsResume = false;
            }
                break;
            }
        }
            break;
        case 0x0f: // Interrupt Handler: 32-bit Trap Gate
        {
            SegmentFactory.TrapGate32Bit trapGate = ((SegmentFactory.TrapGate32Bit) gate);

            checkGate(gate, selector, software);

            int targetSegmentSelector = trapGate.getTargetSegment();

            Segment targetSegment;
            try {
                targetSegment = getSegment(targetSegmentSelector);
            } catch (ProcessorException e) {
                throw new ProcessorException(PROC_EXCEPTION_GP,
                        targetSegmentSelector + EXT, true);
            }

            if (targetSegment.getDPL() > currentPrivilegeLevel)
                throw new ProcessorException(PROC_EXCEPTION_GP,
                        targetSegmentSelector + EXT, true);

            switch (targetSegment.getType()) {
            default:
                throw new ProcessorException(PROC_EXCEPTION_GP,
                        targetSegmentSelector + EXT, true);

            case 0x18: // Code, Execute-Only
            case 0x19: // Code, Execute-Only, Accessed
            case 0x1a: // Code, Execute/Read
            case 0x1b: // Code, Execute/Read, Accessed
            {
                if (!targetSegment.isPresent())
                    throw new ProcessorException(PROC_EXCEPTION_NP,
                            targetSegmentSelector + EXT, true);

                if (targetSegment.getDPL() < currentPrivilegeLevel) {
                    // INTER-PRIVILEGE-LEVEL
                    int newStackSelector = 0;
                    int newESP = 0;
                    if ((tss.getType() & 0x8) != 0) {
                        int tssStackAddress = (targetSegment.getDPL() * 8) + 4;
                        if ((tssStackAddress + 7) > tss.getLimit())
                            throw new ProcessorException(PROC_EXCEPTION_TS, tss
                                    .getSelector(), true);

                        isSup = linearMemory.isSupervisor();
                        try {
                            linearMemory.setSupervisor(true);
                            newStackSelector = 0xffff & tss
                                    .getWord(tssStackAddress + 4);
                            newESP = tss.getDoubleWord(tssStackAddress);
                        } finally {
                            linearMemory.setSupervisor(isSup);
                        }
                    } else {
                        int tssStackAddress = (targetSegment.getDPL() * 4) + 2;
                        if ((tssStackAddress + 4) > tss.getLimit())
                            throw new ProcessorException(PROC_EXCEPTION_TS, tss
                                    .getSelector(), true);
                        newStackSelector = 0xffff & tss
                                .getWord(tssStackAddress + 2);
                        newESP = 0xffff & tss.getWord(tssStackAddress);
                    }

                    Segment newStackSegment = null;
                    try {
                        newStackSegment = getSegment(newStackSelector);
                    } catch (ProcessorException e) {
                        throw new ProcessorException(PROC_EXCEPTION_TS,
                                newStackSelector, true);
                    }

                    if (newStackSegment.getRPL() != targetSegment.getDPL())
                        throw new ProcessorException(PROC_EXCEPTION_TS,
                                newStackSelector, true);

                    if ((newStackSegment.getDPL() != targetSegment.getDPL())
                            || ((newStackSegment.getType() & 0x1a) != 0x12))
                        throw new ProcessorException(PROC_EXCEPTION_TS,
                                newStackSelector, true);

                    if (!(newStackSegment.isPresent()))
                        throw new ProcessorException(PROC_EXCEPTION_SS,
                                newStackSelector, true);

                    if (hasErrorCode) {
                        if ((newStackSegment.getDefaultSizeFlag() && (esp < 24) && (esp > 0))
                                || !newStackSegment.getDefaultSizeFlag()
                                && ((esp & 0xffff) < 24)
                                && ((esp & 0xffff) > 0))
                            throw new ProcessorException(PROC_EXCEPTION_SS, 0,
                                    true);
                    } else {
                        if ((newStackSegment.getDefaultSizeFlag() && (esp < 20) && (esp > 0))
                                || !newStackSegment.getDefaultSizeFlag()
                                && ((esp & 0xffff) < 20)
                                && ((esp & 0xffff) > 0))
                            throw new ProcessorException(PROC_EXCEPTION_SS, 0,
                                    true);
                    }

                    int targetOffset = trapGate.getTargetOffset();
                    targetSegment.checkAddress(targetOffset);

                    int oldSS = ss.getSelector();
                    int oldESP = esp;
                    int oldCS = cs.getSelector();
                    int oldEIP = eip;

                    ss = newStackSegment;
                    esp = newESP;

                    cs = targetSegment;
                    eip = targetOffset;
                    setCPL(cs.getDPL());

                    if (ss.getDefaultSizeFlag()) {
                        esp -= 4;
                        ss.setDoubleWord(esp, oldSS);
                        esp -= 4;
                        ss.setDoubleWord(esp, oldESP);
                        esp -= 4;
                        ss.setDoubleWord(esp, getEFlags());
                        esp -= 4;
                        ss.setDoubleWord(esp, oldCS);
                        esp -= 4;
                        ss.setDoubleWord(esp, oldEIP);
                        if (hasErrorCode) {
                            esp -= 4;
                            ss.setDoubleWord(esp, errorCode);
                        }
                    } else {
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, oldSS);
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, oldESP);
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, getEFlags());
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, oldCS);
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, oldEIP);
                        if (hasErrorCode) {
                            esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                            ss.setDoubleWord(esp & 0xffff, errorCode);
                        }
                    }

                    eflagsTrap = false;
                    eflagsNestedTask = false;
                    eflagsVirtual8086Mode = false;
                    eflagsResume = false;
                } else if (targetSegment.getDPL() == currentPrivilegeLevel) {
                    // SAME-PRIVILEGE
                    // check there is room on stack
                    if (hasErrorCode) {
                        if ((ss.getDefaultSizeFlag() && (esp < 16) && (esp > 0))
                                || !ss.getDefaultSizeFlag()
                                && ((esp & 0xffff) < 16)
                                && ((esp & 0xffff) > 0))
                            throw new ProcessorException(PROC_EXCEPTION_SS, 0,
                                    true);
                    } else {
                        if ((ss.getDefaultSizeFlag() && (esp < 12) && (esp > 0))
                                || !ss.getDefaultSizeFlag()
                                && ((esp & 0xffff) < 12)
                                && ((esp & 0xffff) > 0))
                            throw new ProcessorException(PROC_EXCEPTION_SS, 0,
                                    true);
                    }

                    int targetOffset = trapGate.getTargetOffset();

                    targetSegment.checkAddress(targetOffset);

                    if (ss.getDefaultSizeFlag()) {
                        esp -= 4;
                        ss.setDoubleWord(esp, getEFlags());
                        esp -= 4;
                        ss.setDoubleWord(esp, cs.getSelector());
                        esp -= 4;
                        ss.setDoubleWord(esp, eip);
                        if (hasErrorCode) {
                            esp -= 4;
                            ss.setDoubleWord(esp, errorCode);
                        }
                    } else {
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, getEFlags());
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, cs.getSelector());
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, eip);
                        if (hasErrorCode) {
                            esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                            ss.setDoubleWord(esp & 0xffff, errorCode);
                        }
                    }

                    cs = targetSegment;
                    eip = targetOffset;

                    cs.setRPL(currentPrivilegeLevel);

                    eflagsTrap = false;
                    eflagsNestedTask = false;
                    eflagsVirtual8086Mode = false;
                    eflagsResume = false;
                } else {
                    throw new ProcessorException(PROC_EXCEPTION_GP,
                            targetSegmentSelector + EXT, true);
                }
            }
                break;
            case 0x1c: // Code: Execute-Only, Conforming
            case 0x1d: // Code: Execute-Only, Conforming, Accessed
            case 0x1e: // Code: Execute/Read, Conforming
            case 0x1f: // Code: Execute/Read, Conforming, Accessed
            {
                if (!targetSegment.isPresent())
                    throw new ProcessorException(PROC_EXCEPTION_NP, selector,
                            true);

                // SAME-PRIVILEGE
                // check there is room on stack
                if (hasErrorCode) {
                    if ((ss.getDefaultSizeFlag() && (esp < 16) && (esp > 0))
                            || !ss.getDefaultSizeFlag()
                            && ((esp & 0xffff) < 16) && ((esp & 0xffff) > 0))
                        throw new ProcessorException(PROC_EXCEPTION_SS, 0, true);
                } else {
                    if ((ss.getDefaultSizeFlag() && (esp < 12) && (esp > 0))
                            || !ss.getDefaultSizeFlag()
                            && ((esp & 0xffff) < 12) && ((esp & 0xffff) > 0))
                        throw new ProcessorException(PROC_EXCEPTION_SS, 0, true);
                }

                int targetOffset = trapGate.getTargetOffset();

                targetSegment.checkAddress(targetOffset);

                if (ss.getDefaultSizeFlag()) {
                    esp -= 4;
                    ss.setDoubleWord(esp, getEFlags());
                    esp -= 4;
                    ss.setDoubleWord(esp, cs.getSelector());
                    esp -= 4;
                    ss.setDoubleWord(esp, eip);
                    if (hasErrorCode) {
                        esp -= 4;
                        ss.setDoubleWord(esp, errorCode);
                    }
                } else {
                    esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                    ss.setDoubleWord(esp & 0xffff, getEFlags());
                    esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                    ss.setDoubleWord(esp & 0xffff, cs.getSelector());
                    esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                    ss.setDoubleWord(esp & 0xffff, eip);
                    if (hasErrorCode) {
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, errorCode);
                    }
                }

                cs = targetSegment;
                eip = targetOffset;
                cs.setRPL(currentPrivilegeLevel);

                eflagsTrap = false;
                eflagsNestedTask = false;
                eflagsVirtual8086Mode = false;
                eflagsResume = false;
            }
                break;
            }
        }
            break;
        }
    }

    public final void handleVirtual8086ModeException(int vector,
            boolean hasErrorCode, int errorCode) {
        int savedESP = esp;
        int savedEIP = eip;
        Segment savedCS = cs;
        Segment savedSS = ss;

        try {
            followVirtual8086ModeException(vector, hasErrorCode, errorCode,
                    false, false);
        } catch (ProcessorException e) {
            e.printStackTrace();
            // return cpu to original state
            esp = savedESP;
            eip = savedEIP;
            cs = savedCS;
            ss = savedSS;

            if (vector == PROC_EXCEPTION_DF) {
                System.err
                        .println("Triple-Fault: Unhandleable, machine will halt!");
                throw new IllegalStateException("Triple Fault " + e);
            } else if (e.combinesToDoubleFault(vector)) {
                System.err.println(vector);
                handleVirtual8086ModeException(PROC_EXCEPTION_DF, true, 0);
            } else
                handleVirtual8086ModeException(e.getVector(), e.hasErrorCode(),
                        e.getErrorCode());
        }
    }

    public final void handleSoftVirtual8086ModeInterrupt(int vector) {
        int savedESP = esp;
        int savedEIP = eip;
        Segment savedCS = cs;
        Segment savedSS = ss;

        if (eflagsIOPrivilegeLevel < 3)
            throw new ProcessorException(PROC_EXCEPTION_GP, 0, true);
        else {
            try {
                followVirtual8086ModeException(vector, false, 0, false, true);
            } catch (ProcessorException e) {
                e.printStackTrace();
                // return cpu to original state
                esp = savedESP;
                eip = savedEIP;
                cs = savedCS;
                ss = savedSS;

                // if (e.getVector() == PROC_EXCEPTION_DF) {
                // System.err.println("Triple-Fault: Unhandleable, machine will halt!");
                // throw new IllegalStateException("Triple Fault");
                // else
                handleVirtual8086ModeException(e.getVector(), e.hasErrorCode(),
                        e.getErrorCode());
            }
        }
    }

    public final void handleHardVirtual8086ModeInterrupt(int vector) {
        int savedESP = esp;
        int savedEIP = eip;
        Segment savedCS = cs;
        Segment savedSS = ss;

        try {
            followVirtual8086ModeException(vector, false, 0, true, false);
        } catch (ProcessorException e) {
            // return cpu to original state
            esp = savedESP;
            eip = savedEIP;
            cs = savedCS;
            ss = savedSS;

            // if (e.getVector() == PROC_EXCEPTION_DF) {
            // System.err.println("Triple-Fault: Unhandleable, machine will halt!");
            // throw new IllegalStateException("Triple Fault");
            // else
            handleVirtual8086ModeException(e.getVector(), e.hasErrorCode(), e
                    .getErrorCode());
        }
    }

    private final void followVirtual8086ModeException(int vector,
            boolean hasErrorCode, int errorCode, boolean hardware,
            boolean software) {
        if (vector == PROC_EXCEPTION_PF)
            setCR2(linearMemory.getLastWalkedAddress());

        int selector = vector << 3; // multiply by 8 to get offset into idt
        int EXT = hardware ? 1 : 0;

        Segment gate;
        boolean isSup = linearMemory.isSupervisor();
        try {
            linearMemory.setSupervisor(true);
            long descriptor = idtr.getQuadWord(selector);
            gate = SegmentFactory.createProtectedModeSegment(linearMemory,
                    selector, descriptor);
        } catch (ProcessorException e) {
            throw new ProcessorException(PROC_EXCEPTION_GP, selector + 2 + EXT,
                    true);
        } finally {
            linearMemory.setSupervisor(isSup);
        }

        switch (gate.getType()) {
        default:
            System.err.println("Invalid Gate Type For Throwing Interrupt: 0x"
                    + Integer.toHexString(gate.getType()));
            throw new ProcessorException(PROC_EXCEPTION_GP, selector + 2 + EXT,
                    true);
        case 0x05: // Interrupt Handler: Task Gate
            System.out.println("Unimplemented Interrupt Handler: Task Gate");
            throw new IllegalStateException(
                    "Unimplemented Interrupt Handler: Task Gate");
        case 0x06: // Interrupt Handler: 16-bit Interrupt Gate
            System.out
                    .println("Unimplemented Interrupt Handler: 16-bit Interrupt Gate");
            throw new IllegalStateException(
                    "Unimplemented Interrupt Handler: 16-bit Interrupt Gate");
        case 0x07: // Interrupt Handler: 16-bit Trap Gate
            System.out
                    .println("Unimplemented Interrupt Handler: 16-bit Trap Gate");
            throw new IllegalStateException(
                    "Unimplemented Interrupt Handler: 16-bit Trap Gate");
        case 0x0e: // Interrupt Handler: 32-bit Interrupt Gate
        {
            SegmentFactory.InterruptGate32Bit interruptGate = ((SegmentFactory.InterruptGate32Bit) gate);

            checkGate(gate, selector, software);

            int targetSegmentSelector = interruptGate.getTargetSegment();

            Segment targetSegment;
            try {
                targetSegment = getSegment(targetSegmentSelector);
            } catch (ProcessorException e) {
                throw new ProcessorException(PROC_EXCEPTION_GP,
                        targetSegmentSelector + EXT, true);
            }

            if (targetSegment.getDPL() > currentPrivilegeLevel)
                throw new ProcessorException(PROC_EXCEPTION_GP,
                        targetSegmentSelector + EXT, true);

            switch (targetSegment.getType()) {
            default:
                System.err.println(targetSegment);
                throw new ProcessorException(PROC_EXCEPTION_GP,
                        targetSegmentSelector + EXT, true);

            case 0x18: // Code, Execute-Only
            case 0x19: // Code, Execute-Only, Accessed
            case 0x1a: // Code, Execute/Read
            case 0x1b: // Code, Execute/Read, Accessed
            {
                if (!targetSegment.isPresent())
                    throw new ProcessorException(PROC_EXCEPTION_NP,
                            targetSegmentSelector + EXT, true);

                if (targetSegment.getDPL() < currentPrivilegeLevel) {
                    if (targetSegment.getDPL() != 0)
                        throw new ProcessorException(PROC_EXCEPTION_GP,
                                targetSegmentSelector, true);

                    // INTERRUPT-FROM-VIRTUAL-8086-MODE

                    int newStackSelector = 0;
                    int newESP = 0;
                    if ((tss.getType() & 0x8) != 0) {
                        int tssStackAddress = (targetSegment.getDPL() * 8) + 4;
                        if ((tssStackAddress + 7) > tss.getLimit())
                            throw new ProcessorException(PROC_EXCEPTION_TS, tss
                                    .getSelector(), true);

                        isSup = linearMemory.isSupervisor();
                        try {
                            linearMemory.setSupervisor(true);
                            newStackSelector = 0xffff & tss
                                    .getWord(tssStackAddress + 4);
                            newESP = tss.getDoubleWord(tssStackAddress);
                        } finally {
                            linearMemory.setSupervisor(isSup);
                        }
                    } else {
                        int tssStackAddress = (targetSegment.getDPL() * 4) + 2;
                        if ((tssStackAddress + 4) > tss.getLimit())
                            throw new ProcessorException(PROC_EXCEPTION_TS, tss
                                    .getSelector(), true);
                        newStackSelector = 0xffff & tss
                                .getWord(tssStackAddress + 2);
                        newESP = 0xffff & tss.getWord(tssStackAddress);
                    }

                    Segment newStackSegment = null;
                    try {
                        newStackSegment = getSegment(newStackSelector);
                    } catch (ProcessorException e) {
                        throw new ProcessorException(PROC_EXCEPTION_TS,
                                newStackSelector, true);
                    }

                    if (newStackSegment.getRPL() != targetSegment.getDPL())
                        throw new ProcessorException(PROC_EXCEPTION_TS,
                                newStackSelector, true);

                    if ((newStackSegment.getDPL() != targetSegment.getDPL())
                            || ((newStackSegment.getType() & 0x1a) != 0x12))
                        throw new ProcessorException(PROC_EXCEPTION_TS,
                                newStackSelector, true);

                    if (!(newStackSegment.isPresent()))
                        throw new ProcessorException(PROC_EXCEPTION_SS,
                                newStackSelector, true);

                    if (hasErrorCode) {
                        if ((newStackSegment.getDefaultSizeFlag() && (esp < 40) && (esp > 0))
                                || !newStackSegment.getDefaultSizeFlag()
                                && ((esp & 0xffff) < 40)
                                && ((esp & 0xffff) > 0))
                            throw new ProcessorException(PROC_EXCEPTION_SS, 0,
                                    true);
                    } else {
                        if ((newStackSegment.getDefaultSizeFlag() && (esp < 36) && (esp > 0))
                                || !newStackSegment.getDefaultSizeFlag()
                                && ((esp & 0xffff) < 36)
                                && ((esp & 0xffff) > 0))
                            throw new ProcessorException(PROC_EXCEPTION_SS, 0,
                                    true);
                    }

                    int targetOffset = interruptGate.getTargetOffset();
                    targetSegment.checkAddress(targetOffset);

                    int oldSS = ss.getSelector() & 0xffff;
                    int oldESP = esp;
                    int oldCS = cs.getSelector() & 0xffff;
                    int oldEIP = eip & 0xffff;
                    ss = newStackSegment;
                    esp = newESP;

                    cs = targetSegment;
                    eip = targetOffset;
                    setCPL(cs.getDPL());

                    if (ss.getDefaultSizeFlag()) {
                        esp -= 4;
                        ss.setDoubleWord(esp, gs.getSelector() & 0xffff);
                        esp -= 4;
                        ss.setDoubleWord(esp, fs.getSelector() & 0xffff);
                        esp -= 4;
                        ss.setDoubleWord(esp, ds.getSelector() & 0xffff);
                        esp -= 4;
                        ss.setDoubleWord(esp, es.getSelector() & 0xffff);

                        esp -= 4;
                        ss.setDoubleWord(esp, oldSS);
                        esp -= 4;
                        ss.setDoubleWord(esp, oldESP);
                        esp -= 4;
                        ss.setDoubleWord(esp, getEFlags());
                        esp -= 4;
                        ss.setDoubleWord(esp, oldCS);
                        esp -= 4;
                        ss.setDoubleWord(esp, oldEIP);
                        if (hasErrorCode) {
                            esp -= 4;
                            ss.setDoubleWord(esp, errorCode);
                        }
                    } else {
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff,
                                gs.getSelector() & 0xffff);
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff,
                                fs.getSelector() & 0xffff);
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff,
                                ds.getSelector() & 0xffff);
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff,
                                es.getSelector() & 0xffff);

                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, oldSS);
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, oldESP);
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, getEFlags());
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, oldCS);
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, oldEIP);
                        if (hasErrorCode) {
                            esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                            ss.setDoubleWord(esp & 0xffff, errorCode);
                        }
                    }

                    gs = SegmentFactory.NULL_SEGMENT;
                    fs = SegmentFactory.NULL_SEGMENT;
                    ds = SegmentFactory.NULL_SEGMENT;
                    es = SegmentFactory.NULL_SEGMENT;

                    eflagsInterruptEnable = eflagsInterruptEnableSoon = false;

                    eflagsTrap = false;
                    eflagsNestedTask = false;
                    eflagsVirtual8086Mode = false;
                    eflagsResume = false;
                    throw ModeSwitchException.PROTECTED_MODE_EXCEPTION;
                } else {
                    throw new ProcessorException(PROC_EXCEPTION_GP,
                            targetSegmentSelector, true);
                }
            }
            case 0x1c: // Code: Execute-Only, Conforming
            case 0x1d: // Code: Execute-Only, Conforming, Accessed
            case 0x1e: // Code: Execute/Read, Conforming
            case 0x1f: // Code: Execute/Read, Conforming, Accessed
                if (!targetSegment.isPresent())
                    throw new ProcessorException(PROC_EXCEPTION_NP, selector,
                            true);

                throw new ProcessorException(PROC_EXCEPTION_GP,
                        targetSegmentSelector, true);
            }
        }
        case 0x0f: // Interrupt Handler: 32-bit Trap Gate
        {
            SegmentFactory.TrapGate32Bit trapGate = ((SegmentFactory.TrapGate32Bit) gate);

            checkGate(gate, selector, software);

            int targetSegmentSelector = trapGate.getTargetSegment();

            Segment targetSegment;
            try {
                targetSegment = getSegment(targetSegmentSelector);
            } catch (ProcessorException e) {
                throw new ProcessorException(PROC_EXCEPTION_GP,
                        targetSegmentSelector + EXT, true);
            }

            if (targetSegment.getDPL() > currentPrivilegeLevel)
                throw new ProcessorException(PROC_EXCEPTION_GP,
                        targetSegmentSelector + EXT, true);

            switch (targetSegment.getType()) {
            default:
                throw new ProcessorException(PROC_EXCEPTION_GP,
                        targetSegmentSelector + EXT, true);

            case 0x18: // Code, Execute-Only
            case 0x19: // Code, Execute-Only, Accessed
            case 0x1a: // Code, Execute/Read
            case 0x1b: // Code, Execute/Read, Accessed
            {
                if (!targetSegment.isPresent())
                    throw new ProcessorException(PROC_EXCEPTION_NP,
                            targetSegmentSelector + EXT, true);

                if (targetSegment.getDPL() < currentPrivilegeLevel) {
                    if (targetSegment.getDPL() != 0)
                        throw new ProcessorException(PROC_EXCEPTION_GP,
                                targetSegmentSelector, true);

                    // INTERRUPT-FROM-VIRTUAL-8086-MODE

                    int newStackSelector = 0;
                    int newESP = 0;
                    if ((tss.getType() & 0x8) != 0) {
                        int tssStackAddress = (targetSegment.getDPL() * 8) + 4;
                        if ((tssStackAddress + 7) > tss.getLimit())
                            throw new ProcessorException(PROC_EXCEPTION_TS, tss
                                    .getSelector(), true);

                        isSup = linearMemory.isSupervisor();
                        try {
                            linearMemory.setSupervisor(true);
                            newStackSelector = 0xffff & tss
                                    .getWord(tssStackAddress + 4);
                            newESP = tss.getDoubleWord(tssStackAddress);
                        } finally {
                            linearMemory.setSupervisor(isSup);
                        }
                    } else {
                        int tssStackAddress = (targetSegment.getDPL() * 4) + 2;
                        if ((tssStackAddress + 4) > tss.getLimit())
                            throw new ProcessorException(PROC_EXCEPTION_TS, tss
                                    .getSelector(), true);
                        newStackSelector = 0xffff & tss
                                .getWord(tssStackAddress + 2);
                        newESP = 0xffff & tss.getWord(tssStackAddress);
                    }

                    Segment newStackSegment = null;
                    try {
                        newStackSegment = getSegment(newStackSelector);
                    } catch (ProcessorException e) {
                        throw new ProcessorException(PROC_EXCEPTION_TS,
                                newStackSelector, true);
                    }

                    if (newStackSegment.getRPL() != targetSegment.getDPL())
                        throw new ProcessorException(PROC_EXCEPTION_TS,
                                newStackSelector, true);

                    if ((newStackSegment.getDPL() != targetSegment.getDPL())
                            || ((newStackSegment.getType() & 0x1a) != 0x12))
                        throw new ProcessorException(PROC_EXCEPTION_TS,
                                newStackSelector, true);

                    if (!(newStackSegment.isPresent()))
                        throw new ProcessorException(PROC_EXCEPTION_SS,
                                newStackSelector, true);

                    if (hasErrorCode) {
                        if ((newStackSegment.getDefaultSizeFlag() && (esp < 40) && (esp > 0))
                                || !newStackSegment.getDefaultSizeFlag()
                                && ((esp & 0xffff) < 40)
                                && ((esp & 0xffff) > 0))
                            throw new ProcessorException(PROC_EXCEPTION_SS, 0,
                                    true);
                    } else {
                        if ((newStackSegment.getDefaultSizeFlag() && (esp < 36) && (esp > 0))
                                || !newStackSegment.getDefaultSizeFlag()
                                && ((esp & 0xffff) < 36)
                                && ((esp & 0xffff) > 0))
                            throw new ProcessorException(PROC_EXCEPTION_SS, 0,
                                    true);
                    }

                    int targetOffset = trapGate.getTargetOffset();
                    targetSegment.checkAddress(targetOffset);

                    int oldSS = ss.getSelector() & 0xffff;
                    int oldESP = esp;
                    int oldCS = cs.getSelector() & 0xffff;
                    int oldEIP = eip & 0xffff;

                    ss = newStackSegment;
                    esp = newESP;

                    cs = targetSegment;
                    eip = targetOffset;
                    setCPL(cs.getDPL());

                    if (ss.getDefaultSizeFlag()) {
                        esp -= 4;
                        ss.setDoubleWord(esp, gs.getSelector() & 0xffff);
                        esp -= 4;
                        ss.setDoubleWord(esp, fs.getSelector() & 0xffff);
                        esp -= 4;
                        ss.setDoubleWord(esp, ds.getSelector() & 0xffff);
                        esp -= 4;
                        ss.setDoubleWord(esp, es.getSelector() & 0xffff);

                        esp -= 4;
                        ss.setDoubleWord(esp, oldSS);
                        esp -= 4;
                        ss.setDoubleWord(esp, oldESP);
                        esp -= 4;
                        ss.setDoubleWord(esp, getEFlags());
                        esp -= 4;
                        ss.setDoubleWord(esp, oldCS);
                        esp -= 4;
                        ss.setDoubleWord(esp, oldEIP);
                        if (hasErrorCode) {
                            esp -= 4;
                            ss.setDoubleWord(esp, errorCode);
                        }
                    } else {
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff,
                                gs.getSelector() & 0xffff);
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff,
                                fs.getSelector() & 0xffff);
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff,
                                ds.getSelector() & 0xffff);
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff,
                                es.getSelector() & 0xffff);

                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, oldSS);
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, oldESP);
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, getEFlags());
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, oldCS);
                        esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                        ss.setDoubleWord(esp & 0xffff, oldEIP);
                        if (hasErrorCode) {
                            esp = (esp & ~0xffff) | ((esp - 4) & 0xffff);
                            ss.setDoubleWord(esp & 0xffff, errorCode);
                        }
                    }

                    gs = SegmentFactory.NULL_SEGMENT;
                    fs = SegmentFactory.NULL_SEGMENT;
                    ds = SegmentFactory.NULL_SEGMENT;
                    es = SegmentFactory.NULL_SEGMENT;

                    eflagsTrap = false;
                    eflagsNestedTask = false;
                    eflagsVirtual8086Mode = false;
                    eflagsResume = false;
                    throw ModeSwitchException.PROTECTED_MODE_EXCEPTION;
                } else {
                    throw new ProcessorException(PROC_EXCEPTION_GP,
                            targetSegmentSelector, true);
                }
            }
            case 0x1c: // Code: Execute-Only, Conforming
            case 0x1d: // Code: Execute-Only, Conforming, Accessed
            case 0x1e: // Code: Execute/Read, Conforming
            case 0x1f: // Code: Execute/Read, Conforming, Accessed
                if (!targetSegment.isPresent())
                    throw new ProcessorException(PROC_EXCEPTION_NP, selector,
                            true);

                throw new ProcessorException(PROC_EXCEPTION_GP,
                        targetSegmentSelector, true);
            }
        }
        }
    }

    private void checkAlignmentChecking() {
        if ((getCPL() == 3) && eflagsAlignmentCheck
                && ((cr0 & CR0_ALIGNMENT_MASK) != 0)) {
            if (!alignmentChecking) {
                System.err.println("Alignment Checking Enabled");
                alignmentChecking = true;
                updateAlignmentCheckingInDataSegments();
                // checking now enabled
            }
        } else {
            if (alignmentChecking) {
                System.err.println("Alignment Checking Disabled");
                alignmentChecking = false;
                updateAlignmentCheckingInDataSegments();
                // checking now disabled
            }
        }
    }

    public boolean initialised() {
        boolean result = ((physicalMemory != null) && (linearMemory != null)
                && (ioports != null) && (interruptController != null)); // &&
                                                                        // (virtualClock
                                                                        // !=
                                                                        // null));
        if (result && !started) {
            reset();
            started = true;
        }
        return result;
    }

    public void acceptComponent(HardwareComponent component) {
        if (component instanceof LinearAddressSpace) {
            linearMemory = (LinearAddressSpace) component;
            alignmentCheckedMemory = new AlignmentCheckedAddressSpace(
                    linearMemory);
        }
        if (component instanceof PhysicalAddressSpace)
            physicalMemory = (PhysicalAddressSpace) component;
        if (component instanceof IOPortHandler)
            ioports = (IOPortHandler) component;
        // if (component instanceof Clock)
        // virtualClock = (Clock)component;
    }

    /**
     * Sets up a connection with another module
     * 
     * @param mod
     *            Module that is to be connected to this class
     * 
     * @see Module
     */
    public boolean setConnection(Module mod) {
        if (mod.getType().equals("pic")) {
            this.interruptController = (ModulePIC) mod;
            return true;
        }
        // No connection has been established
        return false;
    }

    private int auxiliaryCarryOne, auxiliaryCarryTwo, auxiliaryCarryThree;
    private boolean auxiliaryCarryCalculated;
    private int auxiliaryCarryMethod;

    public static final int AC_XOR = 1;
    public static final int AC_BIT4_NEQ = 2;
    public static final int AC_LNIBBLE_MAX = 3;
    public static final int AC_LNIBBLE_ZERO = 4;
    public static final int AC_LNIBBLE_NZERO = 5;

    public boolean getAuxiliaryCarryFlag() {
        if (auxiliaryCarryCalculated)
            return eflagsAuxiliaryCarry;
        else {
            auxiliaryCarryCalculated = true;
            if (auxiliaryCarryMethod == AC_XOR)
                return (eflagsAuxiliaryCarry = ((((auxiliaryCarryOne ^ auxiliaryCarryTwo) ^ auxiliaryCarryThree) & 0x10) != 0));
            else if (auxiliaryCarryMethod == AC_LNIBBLE_MAX)
                return (eflagsAuxiliaryCarry = ((auxiliaryCarryOne & 0xf) == 0xf));
            else if (auxiliaryCarryMethod == AC_LNIBBLE_ZERO)
                return (eflagsAuxiliaryCarry = ((auxiliaryCarryOne & 0xf) == 0x0));
            else
                switch (auxiliaryCarryMethod) {
                case AC_BIT4_NEQ:
                    return (eflagsAuxiliaryCarry = ((auxiliaryCarryOne & 0x08) != (auxiliaryCarryTwo & 0x08)));
                case AC_LNIBBLE_NZERO:
                    return (eflagsAuxiliaryCarry = ((auxiliaryCarryOne & 0xf) != 0x0));
                }
            System.err.println("Missing AC Flag Calculation Method");
            return eflagsAuxiliaryCarry;
        }
    }

    private static final boolean[] parityMap;
    static {
        parityMap = new boolean[256];
        for (int i = 0; i < 256; i++) {
            boolean val = true;
            for (int j = 0; j < 8; j++)
                if ((0x1 & (i >> j)) == 1)
                    val = !val;

            parityMap[i] = val;
        }
    }

    public void setAuxiliaryCarryFlag(int dataOne, int dataTwo, int dataThree,
            int method) {
        auxiliaryCarryCalculated = false;
        auxiliaryCarryOne = dataOne;
        auxiliaryCarryTwo = dataTwo;
        auxiliaryCarryThree = dataThree;
        auxiliaryCarryMethod = method;
    }

    public void setAuxiliaryCarryFlag(int dataOne, int dataTwo, int method) {
        auxiliaryCarryCalculated = false;
        auxiliaryCarryOne = dataOne;
        auxiliaryCarryTwo = dataTwo;
        auxiliaryCarryMethod = method;
    }

    public void setAuxiliaryCarryFlag(int dataOne, int method) {
        auxiliaryCarryCalculated = false;
        auxiliaryCarryOne = dataOne;
        auxiliaryCarryMethod = method;
    }

    public void setAuxiliaryCarryFlag(boolean value) {
        auxiliaryCarryCalculated = true;
        eflagsAuxiliaryCarry = value;
    }

    private int parityOne;
    private boolean parityCalculated;

    public boolean getParityFlag() {
        if (parityCalculated)
            return eflagsParity;
        else
            parityCalculated = true;

        return (eflagsParity = parityMap[parityOne & 0xff]);

    }

    public void setParityFlag(boolean value) {
        parityCalculated = true;
        eflagsParity = value;
    }

    public void setParityFlag(int data) {
        parityCalculated = false;
        parityOne = data;
    }

    private int overflowOne, overflowTwo, overflowThree;
    private long overflowLong;
    private boolean overflowCalculated;
    private int overflowMethod;

    public static final int OF_NZ = 1;
    public static final int OF_NOT_BYTE = 2;
    public static final int OF_NOT_SHORT = 3;
    public static final int OF_NOT_INT = 4;

    public static final int OF_LOW_WORD_NZ = 5;
    public static final int OF_HIGH_BYTE_NZ = 6;

    public static final int OF_BIT6_XOR_CARRY = 7;
    public static final int OF_BIT7_XOR_CARRY = 8;
    public static final int OF_BIT14_XOR_CARRY = 9;
    public static final int OF_BIT15_XOR_CARRY = 10;
    public static final int OF_BIT30_XOR_CARRY = 11;
    public static final int OF_BIT31_XOR_CARRY = 12;

    public static final int OF_BIT7_DIFFERENT = 13;
    public static final int OF_BIT15_DIFFERENT = 14;
    public static final int OF_BIT31_DIFFERENT = 15;

    public static final int OF_MAX_BYTE = 16;
    public static final int OF_MAX_SHORT = 17;
    public static final int OF_MAX_INT = 18;

    public static final int OF_MIN_BYTE = 19;
    public static final int OF_MIN_SHORT = 20;
    public static final int OF_MIN_INT = 21;

    public static final int OF_ADD_BYTE = 22;
    public static final int OF_ADD_SHORT = 23;
    public static final int OF_ADD_INT = 24;

    public static final int OF_SUB_BYTE = 25;
    public static final int OF_SUB_SHORT = 26;
    public static final int OF_SUB_INT = 27;

    public boolean getOverflowFlag() {
        if (overflowCalculated)
            return eflagsOverflow;
        else {
            overflowCalculated = true;
            if (overflowMethod == OF_ADD_BYTE)
                return (((overflowTwo & 0x80) == (overflowThree & 0x80)) && ((overflowTwo & 0x80) != (overflowOne & 0x80)));
            else if (overflowMethod == OF_ADD_SHORT)
                return (((overflowTwo & 0x8000) == (overflowThree & 0x8000)) && ((overflowTwo & 0x8000) != (overflowOne & 0x8000)));
            else if (overflowMethod == OF_ADD_INT)
                return (((overflowTwo & 0x80000000) == (overflowThree & 0x80000000)) && ((overflowTwo & 0x80000000) != (overflowOne & 0x80000000)));
            else if (overflowMethod == OF_SUB_BYTE)
                return (((overflowTwo & 0x80) != (overflowThree & 0x80)) && ((overflowTwo & 0x80) != (overflowOne & 0x80)));
            else if (overflowMethod == OF_SUB_SHORT)
                return (((overflowTwo & 0x8000) != (overflowThree & 0x8000)) && ((overflowTwo & 0x8000) != (overflowOne & 0x8000)));
            else if (overflowMethod == OF_SUB_INT)
                return (((overflowTwo & 0x80000000) != (overflowThree & 0x80000000)) && ((overflowTwo & 0x80000000) != (overflowOne & 0x80000000)));
            else if (overflowMethod == OF_MAX_BYTE)
                return (eflagsOverflow = (overflowOne == 0x7f));
            else if (overflowMethod == OF_MAX_SHORT)
                return (eflagsOverflow = (overflowOne == 0x7fff));
            else if (overflowMethod == OF_MIN_SHORT)
                return (eflagsOverflow = (overflowOne == (short) 0x8000));
            else if (overflowMethod == OF_BIT15_XOR_CARRY)
                return (eflagsOverflow = (((overflowOne & 0x8000) != 0) ^ getCarryFlag()));
            else
                switch (overflowMethod) {
                case OF_NZ:
                    return (eflagsOverflow = (overflowOne != 0));
                case OF_NOT_BYTE:
                    return (eflagsOverflow = (overflowOne != (byte) overflowOne));
                case OF_NOT_SHORT:
                    return (eflagsOverflow = (overflowOne != (short) overflowOne));
                case OF_NOT_INT:
                    return (eflagsOverflow = (overflowLong != (int) overflowLong));

                case OF_LOW_WORD_NZ:
                    return (eflagsOverflow = ((overflowOne & 0xffff) != 0));
                case OF_HIGH_BYTE_NZ:
                    return (eflagsOverflow = ((overflowOne & 0xff00) != 0));

                case OF_BIT6_XOR_CARRY:
                    return (eflagsOverflow = (((overflowOne & 0x40) != 0) ^ getCarryFlag()));
                case OF_BIT7_XOR_CARRY:
                    return (eflagsOverflow = (((overflowOne & 0x80) != 0) ^ getCarryFlag()));
                case OF_BIT14_XOR_CARRY:
                    return (eflagsOverflow = (((overflowOne & 0x4000) != 0) ^ getCarryFlag()));
                case OF_BIT30_XOR_CARRY:
                    return (eflagsOverflow = (((overflowOne & 0x40000000) != 0) ^ getCarryFlag()));
                case OF_BIT31_XOR_CARRY:
                    return (eflagsOverflow = (((overflowOne & 0x80000000) != 0) ^ getCarryFlag()));

                case OF_BIT7_DIFFERENT:
                    return (eflagsOverflow = ((overflowOne & 0x80) != (overflowTwo & 0x80)));
                case OF_BIT15_DIFFERENT:
                    return (eflagsOverflow = ((overflowOne & 0x8000) != (overflowTwo & 0x8000)));
                case OF_BIT31_DIFFERENT:
                    return (eflagsOverflow = ((overflowOne & 0x80000000) != (overflowTwo & 0x80000000)));

                case OF_MAX_INT:
                    return (eflagsOverflow = (overflowOne == 0x7fffffff));

                case OF_MIN_BYTE:
                    return (eflagsOverflow = (overflowOne == (byte) 0x80));
                case OF_MIN_INT:
                    return (eflagsOverflow = (overflowOne == 0x80000000));
                }
            System.err.println("Missing OF Flag Calculation Method");
            return eflagsOverflow;
        }
    }

    public void setOverflowFlag(boolean value) {
        overflowCalculated = true;
        eflagsOverflow = value;
    }

    public void setOverflowFlag(long dataOne, int method) {
        overflowCalculated = false;
        overflowLong = dataOne;
        overflowMethod = method;
    }

    public void setOverflowFlag(int dataOne, int method) {
        overflowCalculated = false;
        overflowOne = dataOne;
        overflowMethod = method;
    }

    public void setOverflowFlag(int dataOne, int dataTwo, int method) {
        overflowCalculated = false;
        overflowOne = dataOne;
        overflowTwo = dataTwo;
        overflowMethod = method;
    }

    public void setOverflowFlag(int dataOne, int dataTwo, int dataThree,
            int method) {
        overflowCalculated = false;
        overflowOne = dataOne;
        overflowTwo = dataTwo;
        overflowThree = dataThree;
        overflowMethod = method;
    }

    @SuppressWarnings("unused")
    private int carryOne, carryTwo, carryThree;
    private long carryLong;
    private boolean carryCalculated;
    private int carryMethod;

    public static final int CY_NZ = 1;
    public static final int CY_NOT_BYTE = 2;
    public static final int CY_NOT_SHORT = 3;
    public static final int CY_NOT_INT = 4;

    public static final int CY_LOW_WORD_NZ = 5;
    public static final int CY_HIGH_BYTE_NZ = 6;

    public static final int CY_NTH_BIT_SET = 7;

    public static final int CY_GREATER_FF = 8;

    public static final int CY_TWIDDLE_FF = 9;
    public static final int CY_TWIDDLE_FFFF = 10;
    public static final int CY_TWIDDLE_FFFFFFFF = 11;

    public static final int CY_SHL_OUTBIT_BYTE = 12;
    public static final int CY_SHL_OUTBIT_SHORT = 13;
    public static final int CY_SHL_OUTBIT_INT = 14;

    public static final int CY_SHR_OUTBIT = 15;

    public static final int CY_LOWBIT = 16;

    public static final int CY_HIGHBIT_BYTE = 17;
    public static final int CY_HIGHBIT_SHORT = 18;
    public static final int CY_HIGHBIT_INT = 19;

    public static final int CY_OFFENDBIT_BYTE = 20;
    public static final int CY_OFFENDBIT_SHORT = 21;
    public static final int CY_OFFENDBIT_INT = 22;

    public boolean getCarryFlag() {
        if (carryCalculated)
            return eflagsCarry;
        else {
            carryCalculated = true;
            if (carryMethod == CY_TWIDDLE_FFFF)
                return (eflagsCarry = ((carryOne & (~0xffff)) != 0));
            else if (carryMethod == CY_TWIDDLE_FF)
                return (eflagsCarry = ((carryOne & (~0xff)) != 0));
            else if (carryMethod == CY_SHR_OUTBIT)
                return (eflagsCarry = (((carryOne >>> (carryTwo - 1)) & 0x1) != 0));
            else if (carryMethod == CY_TWIDDLE_FFFFFFFF)
                return (eflagsCarry = ((carryLong & (~0xffffffffl)) != 0));
            else if (carryMethod == CY_SHL_OUTBIT_SHORT)
                return (eflagsCarry = (((carryOne << (carryTwo - 1)) & 0x8000) != 0));
            else
                switch (carryMethod) {
                case CY_NZ:
                    return (eflagsCarry = (carryOne != 0));
                case CY_NOT_BYTE:
                    return (eflagsCarry = (carryOne != (byte) carryOne));
                case CY_NOT_SHORT:
                    return (eflagsCarry = (carryOne != (short) carryOne));
                case CY_NOT_INT:
                    return (eflagsCarry = (carryLong != (int) carryLong));

                case CY_LOW_WORD_NZ:
                    return (eflagsCarry = ((carryOne & 0xffff) != 0));
                case CY_HIGH_BYTE_NZ:
                    return (eflagsCarry = ((carryOne & 0xff00) != 0));

                case CY_NTH_BIT_SET:
                    return (eflagsCarry = ((carryOne & (1 << carryTwo)) != 0));

                case CY_GREATER_FF:
                    return (eflagsCarry = (carryOne > 0xff));

                case CY_SHL_OUTBIT_BYTE:
                    return (eflagsCarry = (((carryOne << (carryTwo - 1)) & 0x80) != 0));
                case CY_SHL_OUTBIT_INT:
                    return (eflagsCarry = (((carryOne << (carryTwo - 1)) & 0x80000000) != 0));

                case CY_LOWBIT:
                    return (eflagsCarry = ((carryOne & 0x1) != 0));
                case CY_HIGHBIT_BYTE:
                    return (eflagsCarry = ((carryOne & 0x80) != 0));
                case CY_HIGHBIT_SHORT:
                    return (eflagsCarry = ((carryOne & 0x8000) != 0));
                case CY_HIGHBIT_INT:
                    return (eflagsCarry = ((carryOne & 0x80000000) != 0));

                case CY_OFFENDBIT_BYTE:
                    return (eflagsCarry = ((carryOne & 0x100) != 0));
                case CY_OFFENDBIT_SHORT:
                    return (eflagsCarry = ((carryOne & 0x10000) != 0));
                case CY_OFFENDBIT_INT:
                    return (eflagsCarry = ((carryLong & 0x100000000l) != 0));
                }
            System.err.println("Missing CY Flag Calculation Method");
            return eflagsCarry;
        }
    }

    public void setCarryFlag(boolean value) {
        carryCalculated = true;
        eflagsCarry = value;
    }

    public void setCarryFlag(long dataOne, int method) {
        carryCalculated = false;
        carryLong = dataOne;
        carryMethod = method;
    }

    public void setCarryFlag(int dataOne, int method) {
        carryCalculated = false;
        carryOne = dataOne;
        carryMethod = method;
    }

    public void setCarryFlag(int dataOne, int dataTwo, int method) {
        carryCalculated = false;
        carryOne = dataOne;
        carryTwo = dataTwo;
        carryMethod = method;
    }

    public void setCarryFlag(int dataOne, int dataTwo, int dataThree, int method) {
        carryCalculated = false;
        carryOne = dataOne;
        carryTwo = dataTwo;
        carryThree = dataThree;
        carryMethod = method;
    }

    private int zeroOne;
    private boolean zeroCalculated;
    @SuppressWarnings("unused")
    private int zeroMethod;

    public boolean getZeroFlag() {
        if (zeroCalculated)
            return eflagsZero;
        else {
            zeroCalculated = true;
            return (eflagsZero = (zeroOne == 0));
        }
    }

    public void setZeroFlag(boolean value) {
        zeroCalculated = true;
        eflagsZero = value;
    }

    public void setZeroFlag(int data) {
        zeroCalculated = false;
        zeroOne = data;
    }

    private int signOne;
    private boolean signCalculated;
    @SuppressWarnings("unused")
    private int signMethod;

    public boolean getSignFlag() {
        if (signCalculated)
            return eflagsSign;
        else {
            signCalculated = true;
            return (eflagsSign = (signOne < 0));
        }
    }

    public void setSignFlag(boolean value) {
        signCalculated = true;
        eflagsSign = value;
    }

    public void setSignFlag(int data) {
        signCalculated = false;
        signOne = data;
    }

    public boolean updated() {
        boolean result = (physicalMemory.updated() && linearMemory.updated()
                && ioports.updated() && interruptController.isConnected());// &&
                                                                           // virtualClock.updated()
                                                                           // &&
                                                                           // interruptController.updated());
        return result;
    }

    public void updateComponent(HardwareComponent component) {
        if (component instanceof LinearAddressSpace) {
            alignmentCheckedMemory = new AlignmentCheckedAddressSpace(
                    linearMemory);
        }
    }

    public void timerCallback() {
    }

    @Override
    public String dumpRegisters() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getCpuInstructionDebug() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long getCurrentInstructionNumber() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected byte getIOPortByte(int portAddress) throws ModuleException,
            ModuleWriteOnlyPortException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected byte[] getIOPortDoubleWord(int portAddress)
            throws ModuleException, ModuleWriteOnlyPortException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected byte[] getIOPortWord(int portAddress) throws ModuleException,
            ModuleWriteOnlyPortException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dioscuri.module.ModuleCPU#getIPS() Added constant to keep Dioscuri
     * clock happy
     */
    public int getIPS() {

        // 5 MHz clock
        return 5 * 1000000;
    }

    @Override
    public String getNextInstructionInfo() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getRegisterHex(int register) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected byte[] getRegisterValue(String registerName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected void incrementInstructionCounter() {
        // TODO Auto-generated method stub

    }

    @Override
    protected boolean initInstructionTables() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected boolean initRegisters() {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see dioscuri.module.ModuleCPU#interruptRequest(boolean) Added method
     * body to propagate JPC generated commands.
     */
    public void interruptRequest(boolean value) {
        if (value)
            raiseInterrupt();
        else
            clearInterrupt();

    }

    @Override
    public boolean isAbnormalTermination() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setCpuInstructionDebug(boolean isDebugMode) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHoldRequest(boolean value, ModuleDevice origin) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void setIOPortByte(int portAddress, byte value)
            throws ModuleException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void setIOPortDoubleWord(int portAddress, byte[] value)
            throws ModuleException {
        // TODO Auto-generated method stub

    }

    @Override
    protected void setIOPortWord(int portAddress, byte[] value)
            throws ModuleException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setIPS(int ips) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setIPS(int ips, int lowestUpdatePeriod) {
        // TODO Auto-generated method stub

    }

    @Override
    protected boolean setRegisterValue(String registerName, byte[] value) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected void setRunning(boolean status) {
        // TODO Auto-generated method stub

    }

    @Override
    public String[] getConnection() {
        return new String[] {};
    }

    @Override
    public byte[] getData(Module module) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean getDebugMode() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getDump() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getID() {
        return 1;
    }

    public String getName() {
        return "8086 compatible CPU";
    }

    @Override
    public String getType() {
        return "cpu";
    }

    @Override
    public boolean isConnected() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean isObserved() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean setData(byte[] data, Module module) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean setData(String[] data, Module module) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setDebugMode(boolean status) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setObserved(boolean status) {
        // TODO Auto-generated method stub

    }

    @Override
    public void start() {
        // TODO Auto-generated method stub

    }

    @Override
    public void stop() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isShutdown() {
        // TODO Auto-generated method stub
        return false;
    }
}
