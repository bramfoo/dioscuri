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

//import java.lang.reflect.*;
//import java.util.*;
//import java.io.*;

//import org.jpc.emulator.processor.Processor;
//import org.jpc.emulator.memory.codeblock.optimised.*;
//import org.jpc.classfile.*;
/**
 *
 * @author Bram Lohman
 * @author Bart Kiers
 */
@SuppressWarnings("unchecked")
public class BytecodeFragments implements MicrocodeSet {
    /**
     *
     */
    public static final Object IMMEDIATE = new Object();
    /**
     *
     */
    public static final Object X86LENGTH = new Object();

    private static Object[][] pushCodeArray = new Object[FASTCompiler.ELEMENT_COUNT][];
    private static Object[][] popCodeArray = new Object[FASTCompiler.PROCESSOR_ELEMENT_COUNT][];

    static {
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_EAX] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("eax") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_ECX] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("ecx") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_EDX] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("edx") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_EBX] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("ebx") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_ESP] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("esp") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_EBP] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("ebp") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_ESI] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("esi") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_EDI] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("edi") };

        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_EIP] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("eip") };

        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_CFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.INVOKEVIRTUAL), method("getCarryFlag") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_PFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.INVOKEVIRTUAL), method("getParityFlag") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_AFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.INVOKEVIRTUAL),
                method("getAuxiliaryCarryFlag") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_ZFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.INVOKEVIRTUAL), method("getZeroFlag") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_SFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.INVOKEVIRTUAL), method("getSignFlag") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_TFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("eflagsTrap") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_IFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD),
                field("eflagsInterruptEnable") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_DFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("eflagsDirection") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_OFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.INVOKEVIRTUAL),
                method("getOverflowFlag") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_IOPL] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD),
                field("eflagsIOPrivilegeLevel") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_NTFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("eflagsNestedTask") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_RFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("eflagsResume") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_VMFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD),
                field("eflagsVirtual8086Mode") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_ACFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("eflagsAlignmentCheck") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_VIFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD),
                field("eflagsVirtualInterrupt") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_VIPFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD),
                field("eflagsVirtualInterruptPending") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_IDFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("eflagsID") };

        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_ES] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("es") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_CS] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("cs") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_SS] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("ss") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_DS] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("ds") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_FS] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("fs") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_GS] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("gs") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_IDTR] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("idtr") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_GDTR] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("gdtr") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_LDTR] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("ldtr") };
        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_TSS] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("tss") };

        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_CPL] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.INVOKEVIRTUAL), method("getCPL") };

        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_IOPORTS] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.GETFIELD), field("ioports") };

        pushCodeArray[FASTCompiler.PROCESSOR_ELEMENT_ADDR0] = new Object[] { new Integer(
                JavaOpcode.ICONST_0) };
    }

    static {

        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_EAX] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("eax") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_ECX] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("ecx") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_EDX] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("edx") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_EBX] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("ebx") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_ESP] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("esp") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_EBP] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("ebp") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_ESI] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("esi") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_EDI] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("edi") };

        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_EIP] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("eip") };

        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_CFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.INVOKEVIRTUAL),
                method("setCarryFlag", Boolean.TYPE) };

        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_PFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.INVOKEVIRTUAL),
                method("setParityFlag", Boolean.TYPE) };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_AFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.INVOKEVIRTUAL),
                method("setAuxiliaryCarryFlag", Boolean.TYPE) };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_ZFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.INVOKEVIRTUAL),
                method("setZeroFlag", Boolean.TYPE) };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_SFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.INVOKEVIRTUAL),
                method("setSignFlag", Boolean.TYPE) };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_TFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("eflagsTrap") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_IFLAG] = new Object[] {
                new Integer(JavaOpcode.DUP), new Integer(JavaOpcode.ALOAD_1),
                new Integer(JavaOpcode.DUP_X2), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD),
                field("eflagsInterruptEnable"),
                new Integer(JavaOpcode.PUTFIELD),
                field("eflagsInterruptEnableSoon") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_DFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("eflagsDirection") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_OFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.INVOKEVIRTUAL),
                method("setOverflowFlag", Boolean.TYPE) };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_IOPL] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD),
                field("eflagsIOPrivilegeLevel") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_NTFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("eflagsNestedTask") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_RFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("eflagsResume") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_VMFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD),
                field("eflagsVirtual8086Mode") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_ACFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("eflagsAlignmentCheck") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_VIFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD),
                field("eflagsVirtualInterrupt") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_VIPFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD),
                field("eflagsVirtualInterruptPending") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_IDFLAG] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("eflagsID") };

        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_ES] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("es") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_CS] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("cs") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_SS] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("ss") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_DS] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("ds") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_FS] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("fs") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_GS] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("gs") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_IDTR] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("idtr") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_GDTR] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("gdtr") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_LDTR] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("ldtr") };
        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_TSS] = new Object[] {
                new Integer(JavaOpcode.ALOAD_1), new Integer(JavaOpcode.SWAP),
                new Integer(JavaOpcode.PUTFIELD), field("tss") };

        popCodeArray[FASTCompiler.PROCESSOR_ELEMENT_ADDR0] = new Object[] { new Integer(
                JavaOpcode.POP) };
    }

    /**
     *
     */
    protected BytecodeFragments() {
    }

    /**
     *
     * @param element
     * @return
     */
    public static Object[] pushCode(int element) {
        Object[] temp = pushCodeArray[element];
        if (temp == null)
            throw new IllegalStateException("Non existant CPU Element: "
                    + element);
        return temp;
    }

    /**
     *
     * @param element
     * @return
     */
    public static Object[] popCode(int element) {
        Object[] temp = popCodeArray[element];
        if (temp == null)
            throw new IllegalStateException("Non existant CPU Element: "
                    + element);
        return temp;
    }

    /**
     *
     * @param name
     * @return
     */
    public static Object field(String name) {
        try {
            return new ConstantPoolSymbol(Processor.class
                    .getDeclaredField(name));
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     *
     * @param cls
     * @param name
     * @return
     */
    public static Object field(Class cls, String name) {
        try {
            return new ConstantPoolSymbol(cls.getDeclaredField(name));
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     *
     * @param name
     * @return
     */
    public static Object method(String name) {
        return method(name, new Class[0]);
    }

    /**
     *
     * @param name
     * @param arg
     * @return
     */
    public static Object method(String name, Class arg) {
        return method(name, new Class[] { arg });
    }

    /**
     *
     * @param name
     * @param arg0
     * @param arg1
     * @return
     */
    public static Object method(String name, Class arg0, Class arg1) {
        return method(name, new Class[] { arg0, arg1 });
    }

    /**
     *
     * @param name
     * @param args
     * @return
     */
    public static Object method(String name, Class[] args) {
        try {
            return new ConstantPoolSymbol(Processor.class.getDeclaredMethod(
                    name, args));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     *
     * @param cls
     * @param name
     * @return
     */
    public static Object method(Class cls, String name) {
        return method(cls, name, new Class[0]);
    }

    /**
     *
     * @param cls
     * @param name
     * @param arg
     * @return
     */
    public static Object method(Class cls, String name, Class arg) {
        return method(cls, name, new Class[] { arg });
    }

    /**
     *
     * @param cls
     * @param name
     * @param arg0
     * @param arg1
     * @return
     */
    public static Object method(Class cls, String name, Class arg0, Class arg1) {
        return method(cls, name, new Class[] { arg0, arg1 });
    }

    /**
     *
     * @param cls
     * @param name
     * @param arg0
     * @param arg1
     * @param arg2
     * @return
     */
    public static Object method(Class cls, String name, Class arg0, Class arg1,
            Class arg2) {
        return method(cls, name, new Class[] { arg0, arg1, arg2 });
    }

    /**
     *
     * @param cls
     * @param name
     * @param args
     * @return
     */
    public static Object method(Class cls, String name, Class[] args) {
        try {
            return new ConstantPoolSymbol(cls.getMethod(name, args));
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     *
     * @param value
     * @return
     */
    public static Object integer(int value) {
        return new ConstantPoolSymbol(new Integer(value));
    }

    /**
     *
     * @param value
     * @return
     */
    public static Object longint(long value) {
        return new ConstantPoolSymbol(new Long(value));
    }
}
