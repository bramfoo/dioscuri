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

import dioscuri.Emulator;
import dioscuri.exception.CPUInstructionException;
import dioscuri.exception.CPU_DE_Exception;
import dioscuri.exception.ModuleException;
import dioscuri.interfaces.Module;
import dioscuri.module.*;

import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of an Intel 8086 hardware CPU module.
 * 
 * Contains 8 32-bit general registers, 4 16-bit segment registers, and 2
 * status/control registers 'flags' and 'ip'.
 * <p>
 * Reads instruction stored in Memory and executes these using an opcode
 * lookup-table.
 * 
 * @see dioscuri.module.AbstractModule
 * 
 *      Metadata module ********************************************
 *      general.type : cpu general.name : x86 CPU based on the Intel 8086
 *      processor general.architecture : Von Neumann general.description :
 *      Contains 8 32-bit general registers, 4 16-bit segment registers, and 2
 *      status/control registers 'flags' and 'ip'. general.creator : Tessella
 *      Support Services, Koninklijke Bibliotheek, Nationaal Archief of the
 *      Netherlands general.version : 1.0 general.keywords : CPU, processor,
 *      Intel, 8086 general.relations : memory, motherboard
 *      general.yearOfIntroduction : 1978 general.yearOfEnding : 1982
 *      general.ancestor : Intel 8080 general.successor : Intel 80186
 *      cpu.clockrateMinimum : 4.77 MHz cpu.clockrateMaximum : 10.00 MHz
 * 
 *      Notes: - Most instructions are implemented as 16 bit. However, a couple
 *      of them (including general purpose registers) allow 32-bit computing.
 * 
 *      General information about instruction format: 8086 has instruction
 *      layout: <opcode byte><address byte>. Opcode byte in bits: xxxx xxdw x is
 *      the opcode of the instruction d is the direction bit: if d=0, rrr -> sss
 *      (addressbyte) if d=1, sss -> rrr (addressbyte) w the size bit if w=0,
 *      indicates byte if w=1, indicates word Address byte in bits: 7654 3210
 *      mmrr rsss The interpretation of these bits depend on the opcode, but
 *      generally: m specifies what rrr, sss are 00 - memory reference
 *      (specified by rrr), sss specifies register 01 - memory reference plus 8
 *      bit offset (follows after addressbyte) 10 - memory reference plus 16 bit
 *      offset (follows after addressbyte) 11 - sss is a register, using same
 *      lookup table as rrr rrr specifies a process register, (toggle with w),
 *      or a memory reference if rrr w0 w1 memref 000 AL AX BX+SI 001 CL CX
 *      BX+DI 010 DL DX BP+SI 011 BL BX BP+DI 100 AH SP SI 101 CH BP DI 110 DH
 *      SI 2-byte offset specified after addressbyte 111 BH DI BX sss specifies
 *      a register
 * 
 * 
 */
public class CPU extends ModuleCPU {

    // Logging
    private static final Logger logger = Logger.getLogger(CPU.class.getName());
    
    // Toggles
    private boolean isRunning;
    private boolean debugMode; // Denotes if CPU module is in debug mode
    private boolean irqPending; // Denotes if an IRQ is pending
    private boolean irqWaited; // Denotes if CPU has waited an extra instruction
                               // before handling IRQ (as stated by Intel spec)
    private boolean holdReQuest; // Bus hold request for CPU
    protected boolean asyncEvent; // Denotes an asynchronous event (could be an
                                  // IRQ or DMA)
    private Module hRQorigin; // Device generating a Hold Request
    private boolean breakpointSet; // Denotes if a breakpoint is set at a
                                   // particular address (CS:IP)
    private boolean waitMessageShown;
    private boolean cpuInstructionDebug;// Used for outputting logging during
                                        // bug trace
    private boolean abnormalTermination;// Denotes if CPU halted due to an error
                                        // during execution
    private boolean shutDown; // Denotes if CPU halted for full emulator
                              // shutdown

    // Instruction and timing
    private long instructionCounter; // total number of executed instructions
    private byte prefixCounter; // total number of prefixes set before execution
                                // of current instruction
    public int ips; // instructions per second
    public int ipus; // instructions per microsecond
    private int lowestUpdatePeriod; // maximum suspend time in microseconds that
                                    // clockpulse may given to clock
    protected int codeByte; // Current single byte from code (prefix or
                            // instruction)
    protected int codeByte2; // Current double byte from code (prefix or
                             // instruction)

    // General variables
    private int tempByte;
    private byte[] tempWord;

    // Current flat-mode code address
    private static int segmentedCodeAddress;

    // Registers
    // General purpose registers
    protected byte[] ax;
    protected byte[] eax;
    protected byte[] bx;
    protected byte[] ebx;
    protected byte[] cx;
    protected byte[] ecx;
    protected byte[] dx;
    protected byte[] edx;

    // General and index registers
    protected byte[] sp;
    protected byte[] esp;
    protected byte[] bp;
    protected byte[] ebp;
    protected byte[] si;
    protected byte[] esi;
    protected byte[] di;
    protected byte[] edi;

    // Segment registers
    protected byte[] cs; // code segment register (16 bit)
    protected byte[] ds; // data segment register (16 bit)
    protected byte[] ss; // stack segment register (16 bit)
    protected byte[] es; // extra segment register (16 bit)

    // Special registers
    protected byte[] ip; // instruction pointer register (16 bit)
    protected byte[] oldIP; // backup IP
    protected boolean[] flags; // flags register (16 bit)

    // Control registers
    protected boolean[] cr0; // Control register 0 (32 bit)
    protected boolean[] cr1; // Control register 1 (32 bit)
    protected boolean[] cr2; // Control register 2 (32 bit)
    protected boolean[] cr3; // Control register 3 (32 bit)
    protected boolean[] cr4; // Control register 4 (32 bit)

    // GDTR and IDTR
    protected byte[] gdtr; // Global Descriptor Table Register (48 bits, 32 bit
                           // base and 16 bit limit)
    protected byte[] idtr; // Interrupt Descriptor Table Register (48 bits, 32
                           // bit base and 16 bit limit)
    protected byte[] ldtr; // Local Descriptor Table Register

    // Instruction prefix settings
    protected int prefixInstruction; // Indicating a prefix instruction is
                                     // encountered
    protected boolean prefixRep;
    protected int prefixRepType;
    protected boolean doubleWord; // Indicates if the extended registers are
                                  // used (e.g. eax), this is set by instruction
                                  // 66h
    protected boolean segmentOverride; // Overrides the segment to be read from
                                       // or written to by selected segment (see
                                       // next variable)
    protected int segmentOverridePointer; // Contains a pointer to: 0=CS, 1=DS,
                                          // 2=ES, 3=SS

    // Opcode lookup arrays
    // These arrays contains functions which implement the instructions
    protected Instruction[] singleByteInstructions;
    protected Instruction[] doubleByteInstructions;

    // Extra variables
    byte stackSize; // Used for ENTER and LEAVE instructions to denote the stack
                    // address size (32 / 16 bit)

    // Constants

    // Operation modes
    private final static int REALADDRESS_MODE = 1;
    private final static int PROTECTED_MODE = 2;

    // Register sizes
    private final static int BYTE = 8; // size of byte in bits
    public final static int REGISTER_SIZE_GENERAL = 16; // size of general
                                                        // registers in bits
    public final static int REGISTER_SIZE_INDEX = 16; // size of general and
                                                      // index registers in bits
    public final static int REGISTER_SIZE_SEGMENT = 16; // size of segment
                                                        // registers in bits
    public final static int REGISTER_SIZE_SPECIAL = 16; // size of special
                                                        // registers in bits

    // Register array locations
    // NOTE: Intel uses Little-Endian byte order (LSB is left-most byte)
    public final static int REGISTER_LOW = 1; // location of register in array
    public final static int REGISTER_HIGH = 0; // location of register in array
    public final static int REGISTER_GENERAL_LOW = 1; // location of register in
                                                      // array
    public final static int REGISTER_GENERAL_HIGH = 0; // location of register
                                                       // in array
    public final static int REGISTER_INDEX_LOW = 1; // location of register in
                                                    // array
    public final static int REGISTER_INDEX_HIGH = 0; // location of register in
                                                     // array
    public final static int REGISTER_SEGMENT_LOW = 1; // location of register in
                                                      // array
    public final static int REGISTER_SEGMENT_HIGH = 0; // location of register
                                                       // in array
    public final static int REGISTER_FLAGS_CF = 0; // Carry Flag (unsigned)
    public final static int REGISTER_FLAGS_PF = 2; // Parity Flag
    public final static int REGISTER_FLAGS_AF = 4; // Auxiliary Carry Flag
    public final static int REGISTER_FLAGS_ZF = 6; // Zero Flag
    public final static int REGISTER_FLAGS_SF = 7; // Sign Flag
    public final static int REGISTER_FLAGS_TF = 8; // Trap Flag
    public final static int REGISTER_FLAGS_IF = 9; // Interrupt Enable Flag
    public final static int REGISTER_FLAGS_DF = 10; // Direction Flag
    public final static int REGISTER_FLAGS_OF = 11; // Overflow Flag (signed)
    public final static int REGISTER_FLAGS_IOPL1 = 12; // I/O Privilege level,
                                                       // bit 0
    public final static int REGISTER_FLAGS_IOPL2 = 13; // I/O Privilege level,
                                                       // bit 1
    public final static int REGISTER_FLAGS_NT = 14; // Nested Task

    // Control register constants
    public final static int REGISTER_CR0_PE = 0; // Protection enable
    public final static int REGISTER_CR0_MP = 1; // Monitor coprocessor
    public final static int REGISTER_CR0_EM = 2; // Emulation
    public final static int REGISTER_CR0_TS = 3; // Task switched
    public final static int REGISTER_CR0_ET = 4; // Extension type
    public final static int REGISTER_CR0_NE = 5; // Numeric error
    public final static int REGISTER_CR0_WP = 16; // Write protect
    public final static int REGISTER_CR0_AM = 18; // Aligment mask
    public final static int REGISTER_CR0_NW = 29; // Not write-through
    public final static int REGISTER_CR0_CD = 30; // Cache disable
    public final static int REGISTER_CR0_PG = 31; // Paging

    // Segment override constants
    public final static int SEGMENT_OVERRIDE_CS = 0; // Override with segment CS
    public final static int SEGMENT_OVERRIDE_DS = 1; // Override with segment DS
    public final static int SEGMENT_OVERRIDE_ES = 2; // Override with segment ES
    public final static int SEGMENT_OVERRIDE_SS = 3; // Override with segment SS

    // Standard word values
    public final static byte[] WORD_0X0001 = new byte[] { 0x00, 0x01 };

    // Constructors

    /**
     * Class constructor specifying memory
     * 
     * @param owner
     */
    public CPU(Emulator owner) {

        debugMode = false;
        irqPending = false;
        irqWaited = false;
        abnormalTermination = false;
        shutDown = false;

        // Initialize timing variables
        ips = 1000000; // default value
        ipus = ips / 1000000; // Assumed that ipus is always > 0
        lowestUpdatePeriod = 1; // default value

        // Set breakpoint (if necesarry)
        breakpointSet = false;
        waitMessageShown = false;

        logger.log(Level.INFO, "[" + super.getType() + "] " + getClass().getName()
                + " AbstractModule created successfully.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean reset() {
        // Initialise toggles
        isRunning = true;
        irqPending = false;
        irqWaited = false;
        abnormalTermination = false;
        shutDown = false;

        // Initialise registers and tables
        this.initRegisters();
        this.initInstructionTables();

        // Reset instruction pointer
        instructionCounter = 0;
        prefixCounter = 0;
        prefixRep = false;
        prefixRepType = 0;

        // Initialise temporary values
        tempByte = 0;
        tempWord = new byte[2];

        // Initialise extra variables
        stackSize = 0;

        logger.log(Level.INFO, "[" + super.getType() + "] AbstractModule has been reset.");
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {

        ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
        ModulePIC pic = (ModulePIC)super.getConnection(Module.Type.PIC);
        ModuleClock clock = (ModuleClock)super.getConnection(Module.Type.CLOCK);

        // Keep track of runtime
        long startTime = System.currentTimeMillis();

        try {
            // Check if module is in debug mode
            if (debugMode == true) {

                // Check for any breakpoints set
                if (breakpointSet) {
                    if (((convertWordToInt(cs) << 4) + convertWordToInt(ip)) == 9336542) {
                        // this.cpuInstructionDebug = true;
                        if (waitMessageShown == false) {
                            logger.log(Level.SEVERE, "["
                                    + super.getType()
                                    + "]"
                                    + " Breakpoint set at "
                                    + Integer.toHexString(convertWordToInt(cs))
                                            .toUpperCase()
                                    + ":"
                                    + Integer.toHexString(convertWordToInt(ip))
                                            .toUpperCase()
                                    + ", wait for prompt...");
                            waitMessageShown = true;
                        }
                        return;
                    }
                }

                // Retrieve one byte from code segment (converting byte to
                // unsigned integer to avoid lookup in instruction array out of
                // bounds)
                codeByte = this.getByteFromCode() & 0xFF;

                try {
                    while (this.isPrefix()) {
                        // PREFIX encountered

                        // Execute prefix
                        singleByteInstructions[codeByte].execute();

                        // Increment prefix counter
                        prefixCounter++;

                        // Retrieve one byte from code segment (converting byte
                        // to unsigned integer to avoid lookup in instruction
                        // array out of bounds)
                        codeByte = this.getByteFromCode() & 0xFF;
                    }

                    // INSTRUCTION encountered

                    // Check if operand size is 32 bit (based on prefix 0x66)
                    // and if instruction supports 32-bits
                    if (doubleWord == true) {
                        if (this.isSingleByte32BitSupported() == false) {
                            logger.log(Level.SEVERE, "[" + super.getType()
                                    + "] Instruction problem (opcode "
                                    + Integer.toHexString(codeByte)
                                    + "h): 32-bit not supported!");
                        }
                    }

                    // Handle prefixes (if necesarry)
                    this.executeInstruction();

                    // Increment instruction counter
                    instructionCounter++;

                    // Reset prefixes
                    this.resetPrefixes();

                    // Handle asynchronous event (if any)
                    if (asyncEvent) {
                        if (this.handleAsyncEvent() == true) // Any event
                                                             // returning true
                                                             // stops CPU
                                                             // execution loop
                        {
                            return;
                        }
                    }
                } catch (CPU_DE_Exception e) {
                    logger.log(Level.SEVERE, "[" + super.getType()
                            + "] Instruction problem (opcode "
                            + Integer.toHexString(codeByte) + "h): "
                            + e.getMessage());
                }

                // TODO: Optimise routine to call only when a device timer goes
                // off, not every instruction
                // Send pulse to clock
                clock.pulse();

                // Debugging information; boolean to turn on
                if (this.cpuInstructionDebug) {
                    DecimalFormat myFormatter = new DecimalFormat("00000000000");
                    String instrCount = myFormatter.format(instructionCounter);

                    if (codeByte == 0x0F) {
                        // Show the 2nd byte of the opcode here for 2-byte
                        // escapes
                        logger.log(Level.CONFIG, instrCount
                                + "i["
                                + super.getType()
                                + "  ] $DEBUG$ 1"
                                + dumpDebug(Integer.toHexString(codeByte2)
                                        .toUpperCase()
                                        + " "));
                    } else {
                        logger.log(Level.CONFIG, instrCount
                                + "i["
                                + super.getType()
                                + "  ] $DEBUG$ "
                                + dumpDebug(Integer.toHexString(codeByte)
                                        .toUpperCase()
                                        + " "));
                    }

                }
            } else {
                // Perform continuous execution of instructions
                while (isRunning == true) {

                    // Retrieve one byte from code segment (converting byte to
                    // unsigned integer to avoid lookup in instruction array out
                    // of bounds)
                    codeByte = this.getByteFromCode() & 0xFF;

                    try {
                        while (this.isPrefix()) {
                            // PREFIX encountered

                            // Execute prefix
                            singleByteInstructions[codeByte].execute();

                            // Increment prefix counter
                            prefixCounter++;

                            // Retrieve one byte from code segment (converting
                            // byte to unsigned integer to avoid lookup in
                            // instruction array out of bounds)
                            codeByte = this.getByteFromCode() & 0xFF;
                        }

                        // INSTRUCTION encountered

                        // Handle prefixes (if necesarry)
                        this.executeInstruction();

                        // Increment instruction counter
                        instructionCounter++;

                        // Reset prefixes
                        this.resetPrefixes();

                        // Handle asynchronous event (if any)
                        if (asyncEvent) {
                            if (this.handleAsyncEvent() == true) // Any event
                                                                 // returning
                                                                 // true stops
                                                                 // CPU
                                                                 // execution
                                                                 // loop
                            {
                                return;
                            }
                        }
                    } catch (CPU_DE_Exception e) {
                        logger.log(Level.SEVERE, "[" + super.getType()
                                + "] Instruction problem (opcode "
                                + Integer.toHexString(codeByte) + "h): "
                                + e.getMessage());
                    }

                    // TODO: Optimise routine to call only when a device timer
                    // goes off, not every instruction
                    // Send pulse to clock
                    clock.pulse();

                }

                logger.log(Level.SEVERE, "[" + super.getType()
                        + "] Execution stopped");

                // Measure total execution time and print to standard output
                float diffTime = System.currentTimeMillis() - startTime;
                logger.log(Level.SEVERE, "[" + super.getType()
                        + "] Total execution time: " + diffTime / 1000 + " s");
                logger.log(Level.SEVERE, "[" + super.getType()
                        + "] Number of instructions executed: "
                        + instructionCounter);
                logger.log(Level.SEVERE, "[" + super.getType() + "] Performance: "
                        + instructionCounter / (diffTime / 1000)
                        + " instr/sec ( "
                        + (float) (instructionCounter / (diffTime / 1000))
                        / 1000000 + " MHz)");
            }
        } catch (ArrayIndexOutOfBoundsException e1) {
            isRunning = false;
            abnormalTermination = true;
            logger.log(Level.SEVERE, "[" + super.getType()
                    + "] Instruction failure at instruction "
                    + instructionCounter + " (opcode "
                    + Integer.toHexString(codeByte)
                    + "h): (Array out of bounds)");
        } catch (CPUInstructionException e2) {
            isRunning = false;
            abnormalTermination = true;
            if (codeByte == 0x0F) {
                // Show the 2nd byte of the opcode here for 2-byte escapes
                logger.log(Level.SEVERE, "[" + super.getType()
                        + "] Instruction failure at instruction "
                        + instructionCounter + " (opcode "
                        + Integer.toHexString(codeByte) + " "
                        + Integer.toHexString(codeByte2) + "h): "
                        + e2.getMessage());
            } else {
                logger.log(Level.SEVERE, "[" + super.getType()
                        + "] Instruction failure at instruction "
                        + instructionCounter + " (opcode "
                        + Integer.toHexString(codeByte) + "h): "
                        + e2.getMessage());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        // Stop execution of instructions
        this.setRunning(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDump() {

        ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
        ModulePIC pic = (ModulePIC)super.getConnection(Module.Type.PIC);
        ModuleClock clock = (ModuleClock)super.getConnection(Module.Type.CLOCK);

        String dump = "";
        String ret = "\r\n";
        String tab = "\t";

        // starting line
        dump += "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"
                + ret;

        // Print CS:IP and the first three bytes of memory
        dump += Integer.toHexString(0x100 | cs[REGISTER_GENERAL_HIGH] & 0xFF)
                .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | cs[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase()
                + ":"
                + Integer.toHexString(0x100 | ip[REGISTER_GENERAL_HIGH] & 0xFF)
                        .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | ip[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + "   ";
        try {
            dump += Integer
                    .toHexString(
                            0x100 | (memory
                                    .getByte(((((cs[REGISTER_SEGMENT_HIGH]) << 12) & 0xFFFFF)
                                            + (((cs[REGISTER_SEGMENT_LOW]) << 4) & 0xFFF)
                                            + (((ip[REGISTER_SEGMENT_HIGH]) << 8) & 0xFFFF) + ((ip[REGISTER_SEGMENT_LOW]) & 0xFF))) & 0xFF))
                    .substring(1).toUpperCase()
                    + " ";
            dump += Integer
                    .toHexString(
                            0x100 | (memory
                                    .getByte(((((cs[REGISTER_SEGMENT_HIGH]) << 12) & 0xFFFFF)
                                            + (((cs[REGISTER_SEGMENT_LOW]) << 4) & 0xFFF)
                                            + (((ip[REGISTER_SEGMENT_HIGH]) << 8) & 0xFFFF)
                                            + ((ip[REGISTER_SEGMENT_LOW]) & 0xFF) + 1)) & 0xFF))
                    .substring(1).toUpperCase()
                    + " ";
            dump += Integer
                    .toHexString(
                            0x100 | (memory
                                    .getByte(((((cs[REGISTER_SEGMENT_HIGH]) << 12) & 0xFFFFF)
                                            + (((cs[REGISTER_SEGMENT_LOW]) << 4) & 0xFFF)
                                            + (((ip[REGISTER_SEGMENT_HIGH]) << 8) & 0xFFFF)
                                            + ((ip[REGISTER_SEGMENT_LOW]) & 0xFF) + 2)) & 0xFF))
                    .substring(1).toUpperCase()
                    + tab;

            // Determine instruction from instruction table, print name
            // Cast byte to unsigned int first before instruction table lookup
            String instruct = singleByteInstructions[((int) (memory
                    .getByte((((cs[REGISTER_SEGMENT_HIGH] & 0xFF) << 12)
                            + ((cs[REGISTER_SEGMENT_LOW] & 0xFF) << 4)
                            + ((ip[REGISTER_SEGMENT_HIGH] & 0xFF) << 8) + (ip[REGISTER_SEGMENT_LOW] & 0xFF))) & 0xFF))]
                    .toString();
            instruct = instruct.substring(instruct.indexOf("_") + 1, instruct
                    .indexOf("@"));
            dump += instruct + ret;
        } catch (ModuleException e) {
            logger.log(Level.SEVERE, super.getType() + " -> AbstractModule exception: "
                    + e.getMessage());
            dump += "Failed to retrieve memory information";
        }

        // Print registers and their values
        dump += "ax:0x"
                + Integer.toHexString(0x100 | ax[REGISTER_GENERAL_HIGH] & 0xFF)
                        .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | ax[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + tab;
        dump += "bx:0x"
                + Integer.toHexString(0x100 | bx[REGISTER_GENERAL_HIGH] & 0xFF)
                        .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | bx[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + tab;
        dump += "cx:0x"
                + Integer.toHexString(0x100 | cx[REGISTER_GENERAL_HIGH] & 0xFF)
                        .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | cx[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + tab;
        dump += "dx:0x"
                + Integer.toHexString(0x100 | dx[REGISTER_GENERAL_HIGH] & 0xFF)
                        .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | dx[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + tab;
        dump += ret;
        dump += "bp:0x"
                + Integer.toHexString(0x100 | bp[REGISTER_GENERAL_HIGH] & 0xFF)
                        .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | bp[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + tab;
        dump += "sp:0x"
                + Integer.toHexString(0x100 | sp[REGISTER_GENERAL_HIGH] & 0xFF)
                        .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | sp[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + tab;
        dump += "si:0x"
                + Integer.toHexString(0x100 | si[REGISTER_GENERAL_HIGH] & 0xFF)
                        .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | si[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + tab;
        dump += "di:0x"
                + Integer.toHexString(0x100 | di[REGISTER_GENERAL_HIGH] & 0xFF)
                        .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | di[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + tab;
        dump += ret;
        dump += "ip:0x"
                + Integer.toHexString(0x100 | ip[REGISTER_GENERAL_HIGH] & 0xFF)
                        .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | ip[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + tab;

        int flagVal = 0;
        for (int i = 0; i < flags.length; i++)
            flagVal = flags[i] == true ? flagVal + (int) Math.pow(2, i)
                    : flagVal;
        dump += "flag:0x"
                + Integer.toHexString(0x100 | flagVal & 0xFF).substring(1)
                        .toUpperCase() + ret;

        dump += "ds:0x"
                + Integer.toHexString(0x100 | ds[REGISTER_GENERAL_HIGH] & 0xFF)
                        .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | ds[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + tab;
        dump += "es:0x"
                + Integer.toHexString(0x100 | es[REGISTER_GENERAL_HIGH] & 0xFF)
                        .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | es[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + tab;
        dump += "ss:0x"
                + Integer.toHexString(0x100 | ss[REGISTER_GENERAL_HIGH] & 0xFF)
                        .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | ss[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + tab;
        dump += "cs:0x"
                + Integer.toHexString(0x100 | cs[REGISTER_GENERAL_HIGH] & 0xFF)
                        .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | cs[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + tab;
        dump += ret;

        // Print ending line
        dump += "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~";

        return dump;
    }

    /**
     * Simple CPU register display
     */
    public String dumpRegisters() {

        ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
        ModulePIC pic = (ModulePIC)super.getConnection(Module.Type.PIC);
        ModuleClock clock = (ModuleClock)super.getConnection(Module.Type.CLOCK);

        String dump = "";
        String ret = "\r\n";
        String tab = "\t";

        // Bochs style register ('r') dump:
        // Print starting line
        dump += "------------------------" + ret;

        // Print registers and their hex values, followed by decimal values
        // Print 32-bit registers is operand size is specified

        try {
            if ((memory
                    .getByte(((((cs[REGISTER_SEGMENT_HIGH]) << 12) & 0xFFFFF)
                            + (((cs[REGISTER_SEGMENT_LOW]) << 4) & 0xFFF)
                            + (((ip[REGISTER_SEGMENT_HIGH]) << 8) & 0xFFFF) + ((ip[REGISTER_SEGMENT_LOW]) & 0xFF))) & 0xFF) == 0x66) {
                dump += "eax: 0x"
                        + Integer
                                .toHexString(eax[REGISTER_GENERAL_HIGH] & 0xFF)
                        + Integer.toHexString(eax[REGISTER_GENERAL_LOW] & 0xFF)
                        + Integer.toHexString(ax[REGISTER_GENERAL_HIGH] & 0xFF)
                        + Integer.toHexString(
                                0x100 | ax[REGISTER_GENERAL_LOW] & 0xFF)
                                .substring(1)
                        + tab
                        + (((((int) eax[REGISTER_GENERAL_HIGH]) & 0xFF) << 24)
                                + ((((int) eax[REGISTER_GENERAL_LOW]) & 0xFF) << 16)
                                + ((((int) ax[REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) ax[REGISTER_GENERAL_LOW]) & 0xFF))
                        + ret;
                dump += "ecx: 0x"
                        + Integer
                                .toHexString(ecx[REGISTER_GENERAL_HIGH] & 0xFF)
                        + Integer.toHexString(ecx[REGISTER_GENERAL_LOW] & 0xFF)
                        + Integer.toHexString(cx[REGISTER_GENERAL_HIGH] & 0xFF)
                        + Integer.toHexString(
                                0x100 | cx[REGISTER_GENERAL_LOW] & 0xFF)
                                .substring(1)
                        + tab
                        + (((((int) ecx[REGISTER_GENERAL_HIGH]) & 0xFF) << 24)
                                + ((((int) ecx[REGISTER_GENERAL_LOW]) & 0xFF) << 16)
                                + ((((int) cx[REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) cx[REGISTER_GENERAL_LOW]) & 0xFF))
                        + ret;
                dump += "edx: 0x"
                        + Integer
                                .toHexString(edx[REGISTER_GENERAL_HIGH] & 0xFF)
                        + Integer.toHexString(edx[REGISTER_GENERAL_LOW] & 0xFF)
                        + Integer.toHexString(dx[REGISTER_GENERAL_HIGH] & 0xFF)
                        + Integer.toHexString(
                                0x100 | dx[REGISTER_GENERAL_LOW] & 0xFF)
                                .substring(1)
                        + tab
                        + (((((int) edx[REGISTER_GENERAL_HIGH]) & 0xFF) << 24)
                                + ((((int) edx[REGISTER_GENERAL_LOW]) & 0xFF) << 16)
                                + ((((int) dx[REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) dx[REGISTER_GENERAL_LOW]) & 0xFF))
                        + ret;
                dump += "ebx: 0x"
                        + Integer
                                .toHexString(ebx[REGISTER_GENERAL_HIGH] & 0xFF)
                        + Integer.toHexString(ebx[REGISTER_GENERAL_LOW] & 0xFF)
                        + Integer.toHexString(bx[REGISTER_GENERAL_HIGH] & 0xFF)
                        + Integer.toHexString(
                                0x100 | bx[REGISTER_GENERAL_LOW] & 0xFF)
                                .substring(1)
                        + tab
                        + (((((int) ebx[REGISTER_GENERAL_HIGH]) & 0xFF) << 24)
                                + ((((int) ebx[REGISTER_GENERAL_LOW]) & 0xFF) << 16)
                                + ((((int) bx[REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) bx[REGISTER_GENERAL_LOW]) & 0xFF))
                        + ret;

            } else {
                dump += "ax: 0x"
                        + Integer.toHexString(ax[REGISTER_GENERAL_HIGH] & 0xFF)
                        + Integer.toHexString(
                                0x100 | ax[REGISTER_GENERAL_LOW] & 0xFF)
                                .substring(1)
                        + tab
                        + (((((int) ax[REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) ax[REGISTER_GENERAL_LOW]) & 0xFF))
                        + ret;
                dump += "cx: 0x"
                        + Integer.toHexString(cx[REGISTER_GENERAL_HIGH] & 0xFF)
                        + Integer.toHexString(
                                0x100 | cx[REGISTER_GENERAL_LOW] & 0xFF)
                                .substring(1)
                        + tab
                        + (((((int) cx[REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) cx[REGISTER_GENERAL_LOW]) & 0xFF))
                        + ret;
                dump += "dx: 0x"
                        + Integer.toHexString(dx[REGISTER_GENERAL_HIGH] & 0xFF)
                        + Integer.toHexString(
                                0x100 | dx[REGISTER_GENERAL_LOW] & 0xFF)
                                .substring(1)
                        + tab
                        + (((((int) dx[REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) dx[REGISTER_GENERAL_LOW]) & 0xFF))
                        + ret;
                dump += "bx: 0x"
                        + Integer.toHexString(bx[REGISTER_GENERAL_HIGH] & 0xFF)
                        + Integer.toHexString(
                                0x100 | bx[REGISTER_GENERAL_LOW] & 0xFF)
                                .substring(1)
                        + tab
                        + (((((int) bx[REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) bx[REGISTER_GENERAL_LOW]) & 0xFF))
                        + ret;
            }
        }

        catch (ModuleException e) {
            logger.log(Level.SEVERE, super.getType() + " -> AbstractModule exception: "
                    + e.getMessage());
            dump += "Failed to retrieve memory information";
        }

        dump += "sp: 0x"
                + Integer.toHexString(sp[REGISTER_GENERAL_HIGH] & 0xFF)
                + Integer.toHexString(0x100 | sp[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1)
                + tab
                + (((((int) sp[REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) sp[REGISTER_GENERAL_LOW]) & 0xFF))
                + ret;
        dump += "bp: 0x"
                + Integer.toHexString(bp[REGISTER_GENERAL_HIGH] & 0xFF)
                + Integer.toHexString(0x100 | bp[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1)
                + tab
                + (((((int) bp[REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) bp[REGISTER_GENERAL_LOW]) & 0xFF))
                + ret;
        dump += "si: 0x"
                + Integer.toHexString(si[REGISTER_GENERAL_HIGH] & 0xFF)
                + Integer.toHexString(0x100 | si[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1)
                + tab
                + (((((int) si[REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) si[REGISTER_GENERAL_LOW]) & 0xFF))
                + ret;
        dump += "di: 0x"
                + Integer.toHexString(di[REGISTER_GENERAL_HIGH] & 0xFF)
                + Integer.toHexString(0x100 | di[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1)
                + tab
                + (((((int) di[REGISTER_GENERAL_HIGH]) & 0xFF) << 8) + (((int) di[REGISTER_GENERAL_LOW]) & 0xFF))
                + ret;

        dump += "ip: 0x"
                + Integer.toHexString(ip[REGISTER_GENERAL_HIGH] & 0xFF)
                + Integer.toHexString(0x100 | ip[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1) + ret;

        int flagVal = 0;
        for (int i = 0; i < flags.length; i++)
            flagVal = flags[i] == true ? flagVal + (int) Math.pow(2, i)
                    : flagVal;
        dump += "flags 0x" + Integer.toHexString(flagVal) + ret;

        dump += "cs: 0x"
                + Integer.toHexString(cs[REGISTER_GENERAL_HIGH] & 0xFF)
                + Integer.toHexString(0x100 | cs[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1) + ret;
        dump += "ss: 0x"
                + Integer.toHexString(ss[REGISTER_GENERAL_HIGH] & 0xFF)
                + Integer.toHexString(0x100 | ss[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1) + ret;
        dump += "ds: 0x"
                + Integer.toHexString(ds[REGISTER_GENERAL_HIGH] & 0xFF)
                + Integer.toHexString(0x100 | ds[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1) + ret;
        dump += "es: 0x"
                + Integer.toHexString(es[REGISTER_GENERAL_HIGH] & 0xFF)
                + Integer.toHexString(0x100 | es[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1) + ret;

        // Print ending line
        dump += "------------------------";

        return dump;
    }

    /**
     *
     * @param data
     * @return -
     */
    public String dumpDebug(String data) {
        String dump = data;
        String space = " ";

        // Diff style dump
        // This dumps all registers + flags onto 1 line for every instruction.
        // No labels.
        // Sample: CS:IP SS:SP FLAGS AX BX CX DX BP SI DI DS ES
        int flagVal = 0;
        for (int i = 0; i < flags.length; i++) {
            flagVal = flags[i] == true ? flagVal + (int) Math.pow(2, i)
                    : flagVal;
        }
        dump += Integer.toHexString(0x100 | cs[REGISTER_GENERAL_HIGH] & 0xFF)
                .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | cs[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + ":";
        dump += Integer.toHexString(0x100 | ip[REGISTER_GENERAL_HIGH] & 0xFF)
                .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | ip[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + space;
        dump += Integer.toHexString(0x100 | ss[REGISTER_GENERAL_HIGH] & 0xFF)
                .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | ss[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + ":";
        dump += Integer.toHexString(0x100 | sp[REGISTER_GENERAL_HIGH] & 0xFF)
                .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | sp[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + space;
        dump += Integer.toHexString(0x10000 | flagVal).substring(1)
                .toUpperCase()
                + space;
        dump += Integer.toHexString(0x100 | ax[REGISTER_GENERAL_HIGH] & 0xFF)
                .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | ax[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + space;
        dump += Integer.toHexString(0x100 | bx[REGISTER_GENERAL_HIGH] & 0xFF)
                .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | bx[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + space;
        dump += Integer.toHexString(0x100 | cx[REGISTER_GENERAL_HIGH] & 0xFF)
                .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | cx[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + space;
        dump += Integer.toHexString(0x100 | dx[REGISTER_GENERAL_HIGH] & 0xFF)
                .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | dx[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + space;
        dump += Integer.toHexString(0x100 | bp[REGISTER_GENERAL_HIGH] & 0xFF)
                .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | bp[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + space;
        dump += Integer.toHexString(0x100 | si[REGISTER_GENERAL_HIGH] & 0xFF)
                .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | si[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + space;
        dump += Integer.toHexString(0x100 | di[REGISTER_GENERAL_HIGH] & 0xFF)
                .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | di[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + space;
        dump += Integer.toHexString(0x100 | ds[REGISTER_GENERAL_HIGH] & 0xFF)
                .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | ds[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase() + space;
        dump += Integer.toHexString(0x100 | es[REGISTER_GENERAL_HIGH] & 0xFF)
                .substring(1).toUpperCase()
                + Integer.toHexString(0x100 | es[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1).toUpperCase();

        return dump;
    }

    // ******************************************************************************
    // ModuleCPU Methods

    /**
     * Returns if CPU halted abnormally or not
     * 
     * @return boolean abnormalTermination true if abnormal, false otherwise
     */
    public boolean isAbnormalTermination() {
        return abnormalTermination;
    }

    /**
     * Returns if CPU halted due to full system shutdown or not
     * 
     * @return boolean shutDown true if emulator should shutdown, false
     *         otherwise
     */
    public boolean isShutdown() {
        return shutDown;
    }

    /**
     * Set shutdown status
     * 
     * @param status true if emulator should shutdown, false otherwise
     */
    protected void setShutdown(boolean status) {
        shutDown = status;
    }

    /**
     * Sets the CPU hold mode by asserting a Hold Request.<BR>
     * This informs the CPU to avoid using the (non-existent) bus as another
     * device (usually via DMA) is using it; it should be scheduled as a
     * asynchronous event in CPU.
     * 
     * @param value
     *            state of the Hold Request
     * @param originator
     */
    public void setHoldRequest(boolean value, Module originator) {
        holdReQuest = value;
        hRQorigin = originator;
        if (value == true) {
            // Asynchronous event has occurred and should be handled
            asyncEvent = true;
        }
    }

    /**
     * Processes special conditions and events, triggered by interrupts or
     * HALTs, etc.<BR>
     * 
     * @return True if event stops CPU start() loop<BR>
     *         False if CPU keeps running
     * 
     */
    private boolean handleAsyncEvent() {

        ModulePIC pic = (ModulePIC)super.getConnection(Module.Type.PIC);

        // Check if normal execution has been stopped
        if (!debugMode && !isRunning) {
            return true;
        }

        // INTERRUPTS
        // Sense if interrupt request is pending; also check it's properly
        // executed on NEXT instruction boundary.
        // Also check if interrupt flag is enabled
        if (irqPending && irqWaited && flags[REGISTER_FLAGS_IF]) {
            logger.log(Level.INFO, "[" + super.getType() + "]"
                    + " handleAsyncEvent: priority 5 - async int");
            // Handle IRQ; irqWaited has ensured this interrupt is executed one
            // instruction after it was generated
            this.handleIRQ(pic.interruptAcknowledge());

            // Reset irq variables
            irqPending = false;
            irqWaited = false;
        }

        // Priority 5: External Interrupts
        // NMI Interrupts
        // Maskable Hardware Interrupts

        // DMA
        if (holdReQuest) {
            logger.log(Level.INFO, "[" + super.getType() + "] handleAsyncEvent: priority 5 - DMA");
            // Assert Hold Acknowledge (HLDA) and go into a bus hold state
            if (hRQorigin.getType() == Module.Type.DMA) {
                ((ModuleDMA) hRQorigin).acknowledgeBusHold();
            }
        }

        if (irqPending && !irqWaited) {
            // According to the Intel specs interrupts should only execute after
            // NEXT instruction, so check this now:
            irqWaited = true;
        }

        // Reset asyncEvents if all have been handled
        if (!(irqPending || holdReQuest)) {
            asyncEvent = false;
        }
        return false;
    }

    /**
     * Return the Instructions Per Second (ips) for this CPU.
     * 
     * 
     */
    public int getIPS() {
        return ips;
    }

    /**
     * Set the Instructions Per Second (ips) for this CPU.
     * 
     * @param ips
     */
    public void setIPS(int ips) {
        this.ips = ips;
        ipus = this.ips / 1000000;
        logger.log(Level.INFO, "[" + super.getType() + "] IPS set to " + this.ips
                + " (" + (((double) this.ips) / 1000000) + " Mhz)");
    }

    /**
     * Set the Instructions Per Second (ips) for this CPU. Also, define what the
     * smallest period is for sending a clockpulse (in microseconds)
     * 
     * @param ips
     * @param lowestUpdatePeriod in microseconds
     */
    public void setIPS(int ips, int lowestUpdatePeriod) {
        this.ips = ips;
        this.lowestUpdatePeriod = lowestUpdatePeriod;
        ipus = this.ips / 1000000;
        logger.log(Level.INFO, "[" + super.getType() + "] IPS set to " + this.ips
                + " (" + (((double) this.ips) / 1000000) + " Mhz)");
    }

    /**
     * Initialise 8086 registers
     * <p>
     * Assigns default values to general, index and special registers. These
     * values correspond to startup values found in MS-DOS debug.exe
     * 
     * @return true if initialisation is successful, false otherwise
     * 
     */
    protected boolean initRegisters() {
        // Initialise general purpose registers
        ax = new byte[REGISTER_SIZE_GENERAL / BYTE];
        ax[REGISTER_GENERAL_HIGH] = ax[REGISTER_GENERAL_LOW] = (byte) 0;
        eax = new byte[REGISTER_SIZE_GENERAL / BYTE];
        eax[REGISTER_GENERAL_HIGH] = eax[REGISTER_GENERAL_LOW] = (byte) 0;

        bx = new byte[REGISTER_SIZE_GENERAL / BYTE];
        bx[REGISTER_GENERAL_HIGH] = bx[REGISTER_GENERAL_LOW] = (byte) 0;
        ebx = new byte[REGISTER_SIZE_GENERAL / BYTE];
        ebx[REGISTER_GENERAL_HIGH] = ebx[REGISTER_GENERAL_LOW] = (byte) 0;

        cx = new byte[REGISTER_SIZE_GENERAL / BYTE];
        cx[REGISTER_GENERAL_HIGH] = cx[REGISTER_GENERAL_LOW] = (byte) 0;
        ecx = new byte[REGISTER_SIZE_GENERAL / BYTE];
        ecx[REGISTER_GENERAL_HIGH] = ecx[REGISTER_GENERAL_LOW] = (byte) 0;

        dx = new byte[REGISTER_SIZE_GENERAL / BYTE];
        // Bochs uses these values for some reason, so for correspondence they
        // are init. here as well:
        dx[REGISTER_GENERAL_HIGH] = 0x05;
        dx[REGISTER_GENERAL_LOW] = 0x43;
        edx = new byte[REGISTER_SIZE_GENERAL / BYTE];
        edx[REGISTER_GENERAL_HIGH] = edx[REGISTER_GENERAL_LOW] = (byte) 0;

        // Initialise general and index registers
        sp = new byte[REGISTER_SIZE_INDEX / BYTE];
        sp[REGISTER_INDEX_HIGH] = (byte) 0x00;
        sp[REGISTER_INDEX_LOW] = (byte) 0x00;
        esp = new byte[REGISTER_SIZE_INDEX / BYTE];
        esp[REGISTER_INDEX_HIGH] = esp[REGISTER_INDEX_LOW] = (byte) 0;

        bp = new byte[REGISTER_SIZE_INDEX / BYTE];
        bp[REGISTER_INDEX_HIGH] = bp[REGISTER_INDEX_LOW] = (byte) 0;
        ebp = new byte[REGISTER_SIZE_INDEX / BYTE];
        ebp[REGISTER_INDEX_HIGH] = ebp[REGISTER_INDEX_LOW] = (byte) 0;

        si = new byte[REGISTER_SIZE_INDEX / BYTE];
        si[REGISTER_INDEX_HIGH] = si[REGISTER_INDEX_LOW] = (byte) 0;
        esi = new byte[REGISTER_SIZE_INDEX / BYTE];
        esi[REGISTER_INDEX_HIGH] = esi[REGISTER_INDEX_LOW] = (byte) 0;

        di = new byte[REGISTER_SIZE_INDEX / BYTE];
        di[REGISTER_INDEX_HIGH] = di[REGISTER_INDEX_LOW] = (byte) 0;
        edi = new byte[REGISTER_SIZE_INDEX / BYTE];
        edi[REGISTER_INDEX_HIGH] = edi[REGISTER_INDEX_LOW] = (byte) 0;

        // Initialise segment registers

        // Point CS to power-up entry/hardware reset entry FFFF:0000
        cs = new byte[REGISTER_SIZE_SEGMENT / BYTE];
        cs[REGISTER_SEGMENT_HIGH] = (byte) 0xF0;
        cs[REGISTER_SEGMENT_LOW] = (byte) 0x00;

        ds = new byte[REGISTER_SIZE_SEGMENT / BYTE];
        ds[REGISTER_SEGMENT_HIGH] = (byte) 0x00;
        ds[REGISTER_SEGMENT_LOW] = (byte) 0x00;

        ss = new byte[REGISTER_SIZE_SEGMENT / BYTE];
        ss[REGISTER_SEGMENT_HIGH] = (byte) 0x00;
        ss[REGISTER_SEGMENT_LOW] = (byte) 0x00;

        es = new byte[REGISTER_SIZE_SEGMENT / BYTE];
        es[REGISTER_SEGMENT_HIGH] = (byte) 0x00;
        es[REGISTER_SEGMENT_LOW] = (byte) 0x00;

        // Special registers
        // Point IP to power-up entry/hardware reset entry FFFF:0000
        ip = new byte[REGISTER_SIZE_SEGMENT / BYTE];
        ip[REGISTER_SEGMENT_HIGH] = (byte) 0xFF;
        ip[REGISTER_SEGMENT_LOW] = (byte) 0xF0;
        oldIP = new byte[REGISTER_SIZE_SEGMENT / BYTE];
        oldIP[REGISTER_SEGMENT_LOW] = 0x00;
        oldIP[REGISTER_SEGMENT_HIGH] = 0x00;

        // FLAGS register
        flags = new boolean[] { false, true, false, false, // Carry, 1, Parity,
                                                           // 0
                false, false, false, false, // Aux, 0, Zero, Sign
                false, false, false, false, // Trap, Interrupt, Direction,
                                            // Overflow
                false, false, false, false }; // IOPL, IOPL, NestedTask, 0

        // Control registers
        // CR0 - system control flags that control operating mode and states of
        // processor
        cr0 = new boolean[32];
        // CR1 - reserved
        cr1 = new boolean[32];
        // CR2 - page-fault linear address
        cr2 = new boolean[32];
        // CR3 - page-directory base register (PDBR)
        cr3 = new boolean[32];
        // CR4 - group of flags that enable several architectural extensions
        cr4 = new boolean[32];

        // Descriptor table registers
        // GDTR - global descriptor table register
        gdtr = new byte[6];
        // IDTR - interrupt descriptor table register
        idtr = new byte[6];
        // LDTR - local descriptor table register
        ldtr = null; // TODO: implement this register

        // Prefix settings
        doubleWord = false;
        segmentOverride = false;
        segmentOverridePointer = -1;

        return true;
    }

    /**
     * Initialise the single and double byte opcode lookup arrays with
     * instructions corresponding to the Intel hexadecimal machinecode values.
     * 
     * @return true if initialisation is successful, false otherwise
     * 
     */
    protected boolean initInstructionTables() {
        // Initialise single-byte opcode lookup array with instruction functions
        singleByteInstructions = new Instruction[256];
        /* 00 */singleByteInstructions[0] = new Instruction_ADD_EbGb(this); // Add
                                                                            // two
                                                                            // bytes
                                                                            // (destination
                                                                            // <-
                                                                            // source)
        /* 01 */singleByteInstructions[1] = new Instruction_ADD_EvGv(this); // Add
                                                                            // two
                                                                            // words
                                                                            // (destination
                                                                            // <-
                                                                            // source)
        /* 02 */singleByteInstructions[2] = new Instruction_ADD_GbEb(this); // Add
                                                                            // two
                                                                            // bytes
                                                                            // (source
                                                                            // ->
                                                                            // destination)
        /* 03 */singleByteInstructions[3] = new Instruction_ADD_GvEv(this); // Add
                                                                            // two
                                                                            // words
                                                                            // (source
                                                                            // ->
                                                                            // destination)
        /* 04 */singleByteInstructions[4] = new Instruction_ADD_ALIb(this); // Add
                                                                            // immediate
                                                                            // byte
                                                                            // to
                                                                            // register
                                                                            // AL
        /* 05 */singleByteInstructions[5] = new Instruction_ADD_AXIv(this); // Add
                                                                            // immediate
                                                                            // word
                                                                            // to
                                                                            // register
                                                                            // AX
        /* 06 */singleByteInstructions[6] = new Instruction_PUSH_ES(this); // Push
                                                                           // ES
                                                                           // onto
                                                                           // stack
                                                                           // top
                                                                           // SS:SP
        /* 07 */singleByteInstructions[7] = new Instruction_POP_ES(this); // Pop
                                                                          // word
                                                                          // at
                                                                          // top
                                                                          // stack
                                                                          // SS:SP
                                                                          // into
                                                                          // ES
        /* 08 */singleByteInstructions[8] = new Instruction_OR_EbGb(this); // Logical
                                                                           // byte-sized
                                                                           // OR
                                                                           // of
                                                                           // memory/register
                                                                           // (destination)
                                                                           // and
                                                                           // register
                                                                           // (source)
        /* 09 */singleByteInstructions[9] = new Instruction_OR_EvGv(this); // Logical
                                                                           // word-sized
                                                                           // OR
                                                                           // of
                                                                           // memory/register
                                                                           // (destination)
                                                                           // and
                                                                           // register
                                                                           // (source)
        /* 0A */singleByteInstructions[10] = new Instruction_OR_GbEb(this); // Logical
                                                                            // byte-sized
                                                                            // OR
                                                                            // of
                                                                            // register
                                                                            // (destination)
                                                                            // and
                                                                            // memory/register
                                                                            // (source)
        /* 0B */singleByteInstructions[11] = new Instruction_OR_GvEv(this); // Logical
                                                                            // word-sized
                                                                            // OR
                                                                            // of
                                                                            // register
                                                                            // (destination)
                                                                            // and
                                                                            // memory/register
                                                                            // (source)
        /* 0C */singleByteInstructions[12] = new Instruction_OR_ALIb(this); // Logical
                                                                            // OR
                                                                            // of
                                                                            // immediate
                                                                            // byte
                                                                            // and
                                                                            // AL
        /* 0D */singleByteInstructions[13] = new Instruction_OR_AXIv(this); // Logical
                                                                            // OR
                                                                            // of
                                                                            // immediate
                                                                            // word
                                                                            // and
                                                                            // AX
        /* 0E */singleByteInstructions[14] = new Instruction_PUSH_CS(this); // Push
                                                                            // CS
                                                                            // onto
                                                                            // stack
                                                                            // top
                                                                            // SS:SP
        /* 0F */singleByteInstructions[15] = new Instruction_2ByteEscape(this); // Escape
                                                                                // character
                                                                                // for
                                                                                // two-byte
                                                                                // opcodes
        /* 10 */singleByteInstructions[16] = new Instruction_ADC_EbGb(this); // Add
                                                                             // two
                                                                             // bytes
                                                                             // plus
                                                                             // carry
                                                                             // (destination
                                                                             // <-
                                                                             // source)
        /* 11 */singleByteInstructions[17] = new Instruction_ADC_EvGv(this); // Add
                                                                             // two
                                                                             // words
                                                                             // plus
                                                                             // carry
                                                                             // (destination
                                                                             // <-
                                                                             // source)
        /* 12 */singleByteInstructions[18] = new Instruction_ADC_GbEb(this); // Add
                                                                             // two
                                                                             // bytes
                                                                             // plus
                                                                             // carry
                                                                             // (source
                                                                             // ->
                                                                             // destination)
        /* 13 */singleByteInstructions[19] = new Instruction_ADC_GvEv(this); // Add
                                                                             // two
                                                                             // words
                                                                             // plus
                                                                             // carry
                                                                             // (source
                                                                             // ->
                                                                             // destination)
        /* 14 */singleByteInstructions[20] = new Instruction_ADC_ALIb(this); // Add
                                                                             // immediate
                                                                             // byte
                                                                             // to
                                                                             // register
                                                                             // AL
                                                                             // plus
                                                                             // carry
                                                                             // (destination
                                                                             // <-
                                                                             // source)
        /* 15 */singleByteInstructions[21] = new Instruction_ADC_AXIv(this); // Add
                                                                             // immediate
                                                                             // word
                                                                             // to
                                                                             // register
                                                                             // AX
                                                                             // plus
                                                                             // carry
                                                                             // (destination
                                                                             // <-
                                                                             // source)
        /* 16 */singleByteInstructions[22] = new Instruction_PUSH_SS(this); // Push
                                                                            // SS
                                                                            // onto
                                                                            // stack
                                                                            // top
                                                                            // SS:SP
        /* 17 */singleByteInstructions[23] = new Instruction_POP_SS(this); // Pop
                                                                           // word
                                                                           // at
                                                                           // top
                                                                           // stack
                                                                           // SS:SP
                                                                           // into
                                                                           // SS
        /* 18 */singleByteInstructions[24] = new Instruction_SBB_EbGb(this); // Subtract
                                                                             // byte
                                                                             // (+
                                                                             // CF)
                                                                             // in
                                                                             // register
                                                                             // from
                                                                             // memory/register
        /* 19 */singleByteInstructions[25] = new Instruction_SBB_EvGv(this); // Subtract
                                                                             // word
                                                                             // (+
                                                                             // CF)
                                                                             // in
                                                                             // register
                                                                             // from
                                                                             // memory/register
        /* 1A */singleByteInstructions[26] = new Instruction_SBB_GbEb(this); // Subtract
                                                                             // byte
                                                                             // (+
                                                                             // CF)
                                                                             // in
                                                                             // memory/register
                                                                             // from
                                                                             // register
        /* 1B */singleByteInstructions[27] = new Instruction_SBB_GvEv(this); // Subtract
                                                                             // word
                                                                             // (+
                                                                             // CF)
                                                                             // in
                                                                             // memory/register
                                                                             // from
                                                                             // register
        /* 1C */singleByteInstructions[28] = new Instruction_SBB_ALIb(this); // Subtract
                                                                             // (immediate
                                                                             // byte
                                                                             // +
                                                                             // CF)
                                                                             // from
                                                                             // AL
        /* 1D */singleByteInstructions[29] = new Instruction_SBB_AXIv(this); // Subtract
                                                                             // (immediate
                                                                             // word
                                                                             // +
                                                                             // CF)
                                                                             // from
                                                                             // AX
        /* 1E */singleByteInstructions[30] = new Instruction_PUSH_DS(this); // Push
                                                                            // DS
                                                                            // onto
                                                                            // stack
                                                                            // top
                                                                            // SS:SP
        /* 1F */singleByteInstructions[31] = new Instruction_POP_DS(this); // Pop
                                                                           // word
                                                                           // at
                                                                           // top
                                                                           // stack
                                                                           // SS:SP
                                                                           // into
                                                                           // DS
        /* 20 */singleByteInstructions[32] = new Instruction_AND_EbGb(this); // Logical
                                                                             // byte-sized
                                                                             // AND
                                                                             // of
                                                                             // memory/register
                                                                             // (destination)
                                                                             // and
                                                                             // register
                                                                             // (source)
        /* 21 */singleByteInstructions[33] = new Instruction_AND_EvGv(this); // Logical
                                                                             // word-sized
                                                                             // AND
                                                                             // of
                                                                             // memory/register
                                                                             // (destination)
                                                                             // and
                                                                             // register
                                                                             // (source)
        /* 22 */singleByteInstructions[34] = new Instruction_AND_GbEb(this); // Logical
                                                                             // byte-sized
                                                                             // AND
                                                                             // of
                                                                             // register
                                                                             // (destination)
                                                                             // and
                                                                             // memory/register
                                                                             // (source)
        /* 23 */singleByteInstructions[35] = new Instruction_AND_GvEv(this); // Logical
                                                                             // word-sized
                                                                             // AND
                                                                             // of
                                                                             // register
                                                                             // (destination)
                                                                             // and
                                                                             // memory/register
                                                                             // (source)
        /* 24 */singleByteInstructions[36] = new Instruction_AND_ALIb(this); // Logical
                                                                             // AND
                                                                             // of
                                                                             // immediate
                                                                             // byte
                                                                             // and
                                                                             // AL
        /* 25 */singleByteInstructions[37] = new Instruction_AND_AXIv(this); // Logical
                                                                             // AND
                                                                             // of
                                                                             // immediate
                                                                             // word
                                                                             // and
                                                                             // AX
        /* 26 */singleByteInstructions[38] = new Instruction_SEG_ES(this); // Prefix
                                                                           // segment
                                                                           // selector
                                                                           // for
                                                                           // ES
        /* 27 */singleByteInstructions[39] = new Instruction_DAA(this); // Decimal
                                                                        // adjust
                                                                        // AL
                                                                        // after
                                                                        // addition
        /* 28 */singleByteInstructions[40] = new Instruction_SUB_EbGb(this); // Subtract
                                                                             // byte
                                                                             // in
                                                                             // register
                                                                             // from
                                                                             // memory/register
        /* 29 */singleByteInstructions[41] = new Instruction_SUB_EvGv(this); // Subtract
                                                                             // word
                                                                             // in
                                                                             // register
                                                                             // from
                                                                             // memory/register
        /* 2A */singleByteInstructions[42] = new Instruction_SUB_GbEb(this); // Subtract
                                                                             // byte
                                                                             // in
                                                                             // memory/register
                                                                             // from
                                                                             // register
        /* 2B */singleByteInstructions[43] = new Instruction_SUB_GvEv(this); // Subtract
                                                                             // word
                                                                             // in
                                                                             // memory/register
                                                                             // from
                                                                             // register
        /* 2C */singleByteInstructions[44] = new Instruction_SUB_ALIb(this); // Subtract
                                                                             // immediate
                                                                             // byte
                                                                             // from
                                                                             // AL
        /* 2D */singleByteInstructions[45] = new Instruction_SUB_AXIv(this); // Subtract
                                                                             // immediate
                                                                             // word
                                                                             // from
                                                                             // AX
        /* 2E */singleByteInstructions[46] = new Instruction_SEG_CS(this); // Prefix
                                                                           // segment
                                                                           // selector
                                                                           // for
                                                                           // CS
        /* 2F */singleByteInstructions[47] = new Instruction_NULL(this);
        /* 30 */singleByteInstructions[48] = new Instruction_XOR_EbGb(this); // Logical
                                                                             // byte-sized
                                                                             // XOR
                                                                             // of
                                                                             // memory/register
                                                                             // (destination)
                                                                             // and
                                                                             // register
                                                                             // (source)
        /* 31 */singleByteInstructions[49] = new Instruction_XOR_EvGv(this); // Logical
                                                                             // word-sized
                                                                             // XOR
                                                                             // of
                                                                             // memory/register
                                                                             // (destination)
                                                                             // and
                                                                             // register
                                                                             // (source)
        /* 32 */singleByteInstructions[50] = new Instruction_XOR_GbEb(this); // Logical
                                                                             // byte-sized
                                                                             // XOR
                                                                             // of
                                                                             // register
                                                                             // (destination)
                                                                             // and
                                                                             // memory/register
                                                                             // (source)
        /* 33 */singleByteInstructions[51] = new Instruction_XOR_GvEv(this); // Logical
                                                                             // word-sized
                                                                             // XOR
                                                                             // of
                                                                             // register
                                                                             // (destination)
                                                                             // and
                                                                             // memory/register
                                                                             // (source)
        /* 34 */singleByteInstructions[52] = new Instruction_XOR_ALIb(this); // Logical
                                                                             // XOR
                                                                             // of
                                                                             // immediate
                                                                             // byte
                                                                             // and
                                                                             // AL
        /* 35 */singleByteInstructions[53] = new Instruction_XOR_AXIv(this); // Logical
                                                                             // XOR
                                                                             // of
                                                                             // immediate
                                                                             // word
                                                                             // and
                                                                             // AX
        /* 36 */singleByteInstructions[54] = new Instruction_SEG_SS(this); // Prefix
                                                                           // segment
                                                                           // selector
                                                                           // for
                                                                           // SS
        /* 37 */singleByteInstructions[55] = new Instruction_AAA(this); // ASCII
                                                                        // adjust
                                                                        // after
                                                                        // addition
        /* 38 */singleByteInstructions[56] = new Instruction_CMP_EbGb(this); // Byte-sized
                                                                             // comparison
                                                                             // (SUB)
                                                                             // of
                                                                             // memory/register
                                                                             // with
                                                                             // register
        /* 39 */singleByteInstructions[57] = new Instruction_CMP_EvGv(this); // Word-sized
                                                                             // comparison
                                                                             // (SUB)
                                                                             // of
                                                                             // memory/register
                                                                             // with
                                                                             // register
        /* 3A */singleByteInstructions[58] = new Instruction_CMP_GbEb(this); // Byte-sized
                                                                             // comparison
                                                                             // (SUB)
                                                                             // of
                                                                             // register
                                                                             // with
                                                                             // memory/register
        /* 3B */singleByteInstructions[59] = new Instruction_CMP_GvEv(this); // Word-sized
                                                                             // comparison
                                                                             // (SUB)
                                                                             // of
                                                                             // register
                                                                             // with
                                                                             // memory/register
        /* 3C */singleByteInstructions[60] = new Instruction_CMP_ALIb(this); // Comparison
                                                                             // of
                                                                             // immediate
                                                                             // byte
                                                                             // (SUB)
                                                                             // with
                                                                             // AL
        /* 3D */singleByteInstructions[61] = new Instruction_CMP_AXIv(this); // Comparison
                                                                             // of
                                                                             // immediate
                                                                             // word
                                                                             // (SUB)
                                                                             // with
                                                                             // AX
        /* 3E */singleByteInstructions[62] = new Instruction_SEG_DS(this); // Prefix
                                                                           // segment
                                                                           // selector
                                                                           // for
                                                                           // DS
        /* 3F */singleByteInstructions[63] = new Instruction_NULL(this);
        /* 40 */singleByteInstructions[64] = new Instruction_INC_AX(this); // Increment
                                                                           // register
                                                                           // AX
        /* 41 */singleByteInstructions[65] = new Instruction_INC_CX(this); // Increment
                                                                           // register
                                                                           // CX
        /* 42 */singleByteInstructions[66] = new Instruction_INC_DX(this); // Increment
                                                                           // register
                                                                           // DX
        /* 43 */singleByteInstructions[67] = new Instruction_INC_BX(this); // Increment
                                                                           // register
                                                                           // BX
        /* 44 */singleByteInstructions[68] = new Instruction_INC_SP(this); // Increment
                                                                           // register
                                                                           // SP
        /* 45 */singleByteInstructions[69] = new Instruction_INC_BP(this); // Increment
                                                                           // register
                                                                           // BP
        /* 46 */singleByteInstructions[70] = new Instruction_INC_SI(this); // Increment
                                                                           // register
                                                                           // SI
        /* 47 */singleByteInstructions[71] = new Instruction_INC_DI(this); // Increment
                                                                           // register
                                                                           // DI
        /* 48 */singleByteInstructions[72] = new Instruction_DEC_AX(this); // Decrement
                                                                           // register
                                                                           // AX
        /* 49 */singleByteInstructions[73] = new Instruction_DEC_CX(this); // Decrement
                                                                           // register
                                                                           // CX
        /* 4A */singleByteInstructions[74] = new Instruction_DEC_DX(this); // Decrement
                                                                           // register
                                                                           // DX
        /* 4B */singleByteInstructions[75] = new Instruction_DEC_BX(this); // Decrement
                                                                           // register
                                                                           // BX
        /* 4C */singleByteInstructions[76] = new Instruction_DEC_SP(this); // Decrement
                                                                           // register
                                                                           // SP
        /* 4D */singleByteInstructions[77] = new Instruction_DEC_BP(this); // Decrement
                                                                           // register
                                                                           // BP
        /* 4E */singleByteInstructions[78] = new Instruction_DEC_SI(this); // Decrement
                                                                           // register
                                                                           // SI
        /* 4F */singleByteInstructions[79] = new Instruction_DEC_DI(this); // Decrement
                                                                           // register
                                                                           // DI
        /* 50 */singleByteInstructions[80] = new Instruction_PUSH_AX(this); // Push
                                                                            // AX
                                                                            // onto
                                                                            // stack
                                                                            // top
                                                                            // SS:SP
        /* 51 */singleByteInstructions[81] = new Instruction_PUSH_CX(this); // Push
                                                                            // CX
                                                                            // onto
                                                                            // stack
                                                                            // top
                                                                            // SS:SP
        /* 52 */singleByteInstructions[82] = new Instruction_PUSH_DX(this); // Push
                                                                            // DX
                                                                            // onto
                                                                            // stack
                                                                            // top
                                                                            // SS:SP
        /* 53 */singleByteInstructions[83] = new Instruction_PUSH_BX(this); // Push
                                                                            // BX
                                                                            // onto
                                                                            // stack
                                                                            // top
                                                                            // SS:SP
        /* 54 */singleByteInstructions[84] = new Instruction_PUSH_SP(this); // Push
                                                                            // SP
                                                                            // onto
                                                                            // stack
                                                                            // top
                                                                            // SS:SP
        /* 55 */singleByteInstructions[85] = new Instruction_PUSH_BP(this); // Push
                                                                            // BP
                                                                            // onto
                                                                            // stack
                                                                            // top
                                                                            // SS:SP
        /* 56 */singleByteInstructions[86] = new Instruction_PUSH_SI(this); // Push
                                                                            // SI
                                                                            // onto
                                                                            // stack
                                                                            // top
                                                                            // SS:SP
        /* 57 */singleByteInstructions[87] = new Instruction_PUSH_DI(this); // Push
                                                                            // DI
                                                                            // onto
                                                                            // stack
                                                                            // top
                                                                            // SS:SP
        /* 58 */singleByteInstructions[88] = new Instruction_POP_AX(this); // Pop
                                                                           // word
                                                                           // at
                                                                           // top
                                                                           // stack
                                                                           // SS:SP
                                                                           // into
                                                                           // AX
        /* 59 */singleByteInstructions[89] = new Instruction_POP_CX(this); // Pop
                                                                           // word
                                                                           // at
                                                                           // top
                                                                           // stack
                                                                           // SS:SP
                                                                           // into
                                                                           // CX
        /* 5A */singleByteInstructions[90] = new Instruction_POP_DX(this); // Pop
                                                                           // word
                                                                           // at
                                                                           // top
                                                                           // stack
                                                                           // SS:SP
                                                                           // into
                                                                           // DX
        /* 5B */singleByteInstructions[91] = new Instruction_POP_BX(this); // Pop
                                                                           // word
                                                                           // at
                                                                           // top
                                                                           // stack
                                                                           // SS:SP
                                                                           // into
                                                                           // BX
        /* 5C */singleByteInstructions[92] = new Instruction_POP_SP(this); // Pop
                                                                           // word
                                                                           // at
                                                                           // top
                                                                           // stack
                                                                           // SS:SP
                                                                           // into
                                                                           // SP
        /* 5D */singleByteInstructions[93] = new Instruction_POP_BP(this); // Pop
                                                                           // word
                                                                           // at
                                                                           // top
                                                                           // stack
                                                                           // SS:SP
                                                                           // into
                                                                           // BP
        /* 5E */singleByteInstructions[94] = new Instruction_POP_SI(this); // Pop
                                                                           // word
                                                                           // at
                                                                           // top
                                                                           // stack
                                                                           // SS:SP
                                                                           // into
                                                                           // SI
        /* 5F */singleByteInstructions[95] = new Instruction_POP_DI(this); // Pop
                                                                           // word
                                                                           // at
                                                                           // top
                                                                           // stack
                                                                           // SS:SP
                                                                           // into
                                                                           // DI
        /* 60 */singleByteInstructions[96] = new Instruction_PUSHA(this); // Push
                                                                          // all
                                                                          // general
                                                                          // purpose
                                                                          // registers
                                                                          // onto
                                                                          // stack
                                                                          // SS:SP
        /* 61 */singleByteInstructions[97] = new Instruction_POPA(this); // Pop
                                                                         // top
                                                                         // 8
                                                                         // words
                                                                         // off
                                                                         // stack
                                                                         // into
                                                                         // general
                                                                         // purpose
                                                                         // registers
        /* 62 */singleByteInstructions[98] = new Instruction_BOUND_GvMa(this);// Check
                                                                              // array
                                                                              // index
                                                                              // against
                                                                              // bounds
        /* 63 */singleByteInstructions[99] = new Instruction_ARPL_EwGw(this); // Adjust
                                                                              // RPL
                                                                              // Field
                                                                              // of
                                                                              // Segment
                                                                              // Selector
        /* 64 */singleByteInstructions[100] = new Instruction_SEG_FS(this); // Segment
                                                                            // selector
                                                                            // FS.
                                                                            // Override
                                                                            // the
                                                                            // segment
                                                                            // selector
                                                                            // for
                                                                            // the
                                                                            // next
                                                                            // opcode.
        /* 65 */singleByteInstructions[101] = new Instruction_SEG_GS(this); // Segment
                                                                            // selector
                                                                            // GS.
                                                                            // Override
                                                                            // the
                                                                            // segment
                                                                            // selector
                                                                            // for
                                                                            // the
                                                                            // next
                                                                            // opcode.
        /* 66 */singleByteInstructions[102] = new Instruction_Opd_Size(this); // Instruction
                                                                              // prefix,
                                                                              // indicating
                                                                              // the
                                                                              // next
                                                                              // instruction
                                                                              // should
                                                                              // work
                                                                              // with
                                                                              // doublewords
        /* 67 */singleByteInstructions[103] = new Instruction_NULL(this);
        /* 68 */singleByteInstructions[104] = new Instruction_PUSH_Iv(this); // Push
                                                                             // immediate
                                                                             // word
                                                                             // onto
                                                                             // stack
                                                                             // top
                                                                             // SS:SP
        /* 69 */singleByteInstructions[105] = new Instruction_IMUL_GvEvIv(this); // Signed
                                                                                 // multiply
        /* 6A */singleByteInstructions[106] = new Instruction_PUSH_Ib(this); // Push
                                                                             // immediate
                                                                             // byte
                                                                             // onto
                                                                             // stack
                                                                             // top
                                                                             // SS:SP
        /* 6B */singleByteInstructions[107] = new Instruction_NULL(this);
        /* 6C */singleByteInstructions[108] = new Instruction_INSB_YbDX(this); // Copy
                                                                               // byte
                                                                               // from
                                                                               // I/O
                                                                               // port
                                                                               // to
                                                                               // ES:DI;
                                                                               // update
                                                                               // DI
                                                                               // register
                                                                               // according
                                                                               // to
                                                                               // DF.
        /* 6D */singleByteInstructions[109] = new Instruction_INSW_YvDX(this); // Copy
                                                                               // word
                                                                               // from
                                                                               // I/O
                                                                               // port
                                                                               // to
                                                                               // ES:DI;
                                                                               // update
                                                                               // DI
                                                                               // register
                                                                               // according
                                                                               // to
                                                                               // DF.
        /* 6E */singleByteInstructions[110] = new Instruction_OUTS_DXXb(this); // Output
                                                                               // byte
                                                                               // from
                                                                               // DS:SI
                                                                               // to
                                                                               // I/O
                                                                               // port
                                                                               // (specified
                                                                               // in
                                                                               // DX);
                                                                               // update
                                                                               // SI
                                                                               // register
                                                                               // according
                                                                               // to
                                                                               // DF
        /* 6F */singleByteInstructions[111] = new Instruction_OUTSW_DXXv(this); // Output
                                                                                // word
                                                                                // from
                                                                                // DS:(E)SI
                                                                                // to
                                                                                // I/O
                                                                                // port
                                                                                // (specified
                                                                                // in
                                                                                // DX);
                                                                                // update
                                                                                // SI
                                                                                // register
                                                                                // according
                                                                                // to
                                                                                // DF
        /* 70 */singleByteInstructions[112] = new Instruction_JO(this); // Conditional
                                                                        // short
                                                                        // jump
                                                                        // on
                                                                        // overflow
        /* 71 */singleByteInstructions[113] = new Instruction_JNO(this); // Conditional
                                                                         // short
                                                                         // jump
                                                                         // not
                                                                         // overflow
        /* 72 */singleByteInstructions[114] = new Instruction_JB_JNAE_JC(this);// Conditional
                                                                               // short
                                                                               // jump
                                                                               // on
                                                                               // carry
        /* 73 */singleByteInstructions[115] = new Instruction_JNB_JAE_JNC(this);// Conditional
                                                                                // short
                                                                                // jump
                                                                                // not
                                                                                // carry
        /* 74 */singleByteInstructions[116] = new Instruction_JZ_JE(this); // Conditional
                                                                           // short
                                                                           // jump
                                                                           // on
                                                                           // zero
        /* 75 */singleByteInstructions[117] = new Instruction_JNZ_JNE(this); // Conditional
                                                                             // short
                                                                             // jump
                                                                             // not
                                                                             // zero
        /* 76 */singleByteInstructions[118] = new Instruction_JBE_JNA(this); // Conditional
                                                                             // short
                                                                             // jump
                                                                             // on
                                                                             // carry
                                                                             // or
                                                                             // zero
        /* 77 */singleByteInstructions[119] = new Instruction_JNBE_JA(this); // Conditional
                                                                             // short
                                                                             // jump
                                                                             // not
                                                                             // carry
                                                                             // and
                                                                             // not
                                                                             // zero
        /* 78 */singleByteInstructions[120] = new Instruction_JS(this); // Conditional
                                                                        // short
                                                                        // jump
                                                                        // on
                                                                        // sign
        /* 79 */singleByteInstructions[121] = new Instruction_JNS(this); // Conditional
                                                                         // short
                                                                         // jump
                                                                         // not
                                                                         // sign
        /* 7A */singleByteInstructions[122] = new Instruction_JP_JPE(this); // Conditional
                                                                            // short
                                                                            // jump
                                                                            // on
                                                                            // parity
                                                                            // /
                                                                            // parity
                                                                            // even
        /* 7B */singleByteInstructions[123] = new Instruction_JNP_JPO(this); // Conditional
                                                                             // short
                                                                             // jump
                                                                             // not
                                                                             // parity
                                                                             // /
                                                                             // parity
                                                                             // odd
        /* 7C */singleByteInstructions[124] = new Instruction_JL_JNGE(this); // Conditional
                                                                             // short
                                                                             // jump
                                                                             // if
                                                                             // sign
                                                                             // !=
                                                                             // overflow
        /* 7D */singleByteInstructions[125] = new Instruction_JNL_JGE(this); // Conditional
                                                                             // short
                                                                             // jump
                                                                             // if
                                                                             // sign
                                                                             // ==
                                                                             // overflow
        /* 7E */singleByteInstructions[126] = new Instruction_JLE_JNG(this); // Conditional
                                                                             // short
                                                                             // jump
                                                                             // if
                                                                             // zero
                                                                             // or
                                                                             // sign
                                                                             // !=
                                                                             // overflow
        /* 7F */singleByteInstructions[127] = new Instruction_JNLE_JG(this); // Conditional
                                                                             // short
                                                                             // jump
                                                                             // if
                                                                             // zero
                                                                             // and
                                                                             // sign
                                                                             // ==
                                                                             // overflow
        /* 80 */singleByteInstructions[128] = new Instruction_ImmGRP1_EbIb(this); // Immediate
                                                                                  // Group
                                                                                  // 1
                                                                                  // opcode
                                                                                  // extension:
                                                                                  // ADD,
                                                                                  // OR,
                                                                                  // ADC,
                                                                                  // SBB,
                                                                                  // AND,
                                                                                  // SUB,
                                                                                  // XOR,
                                                                                  // CMP
        /* 81 */singleByteInstructions[129] = new Instruction_ImmGRP1_EvIv(this); // Immediate
                                                                                  // Group
                                                                                  // 1
                                                                                  // opcode
                                                                                  // extension:
                                                                                  // ADD,
                                                                                  // OR,
                                                                                  // ADC,
                                                                                  // SBB,
                                                                                  // AND,
                                                                                  // SUB,
                                                                                  // XOR,
                                                                                  // CMP
        /* 82 */singleByteInstructions[130] = new Instruction_ImmGRP1_EbIb(this); // Note:
                                                                                  // This
                                                                                  // instruction
                                                                                  // is
                                                                                  // identical
                                                                                  // to
                                                                                  // 0x80
        /* 83 */singleByteInstructions[131] = new Instruction_ImmGRP1_EvIb(this); // Immediate
                                                                                  // Group
                                                                                  // 1
                                                                                  // opcode
                                                                                  // extension:
                                                                                  // ADD,
                                                                                  // OR,
                                                                                  // ADC,
                                                                                  // SBB,
                                                                                  // AND,
                                                                                  // SUB,
                                                                                  // XOR,
                                                                                  // CMP
        /* 84 */singleByteInstructions[132] = new Instruction_TEST_EbGb(this); // Logical
                                                                               // byte-sized
                                                                               // comparison
                                                                               // (AND)
                                                                               // of
                                                                               // memory/register
                                                                               // (destination)
                                                                               // and
                                                                               // register
                                                                               // (source)
        /* 85 */singleByteInstructions[133] = new Instruction_TEST_EvGv(this); // Logical
                                                                               // word-sized
                                                                               // comparison
                                                                               // (AND)
                                                                               // of
                                                                               // memory/register
                                                                               // (destination)
                                                                               // and
                                                                               // register
                                                                               // (source)
        /* 86 */singleByteInstructions[134] = new Instruction_XCHG_EbGb(this); // Byte-sized
                                                                               // content
                                                                               // exchange
                                                                               // of
                                                                               // memory/register
                                                                               // (destination)
                                                                               // and
                                                                               // register
                                                                               // (source)
        /* 87 */singleByteInstructions[135] = new Instruction_XCHG_EvGv(this); // Word-sized
                                                                               // content
                                                                               // exchange
                                                                               // of
                                                                               // memory/register
                                                                               // (destination)
                                                                               // and
                                                                               // register
                                                                               // (source)
        /* 88 */singleByteInstructions[136] = new Instruction_MOV_EbGb(this); // Byte-sized
                                                                              // copy
                                                                              // of
                                                                              // memory/register
                                                                              // (destination)
                                                                              // from
                                                                              // register
                                                                              // (source)
        /* 89 */singleByteInstructions[137] = new Instruction_MOV_EvGv(this); // Word-sized
                                                                              // copy
                                                                              // of
                                                                              // memory/register
                                                                              // (destination)
                                                                              // from
                                                                              // register
                                                                              // (source)
        /* 8A */singleByteInstructions[138] = new Instruction_MOV_GbEb(this); // Byte-sized
                                                                              // copy
                                                                              // of
                                                                              // register
                                                                              // (destination)
                                                                              // from
                                                                              // memory/register
                                                                              // (source)
        /* 8B */singleByteInstructions[139] = new Instruction_MOV_GvEv(this); // Word-sized
                                                                              // copy
                                                                              // of
                                                                              // register
                                                                              // (destination)
                                                                              // from
                                                                              // memory/register
                                                                              // (source)
        /* 8C */singleByteInstructions[140] = new Instruction_MOV_EwSw(this); // Word-sized
                                                                              // copy
                                                                              // of
                                                                              // memory/register
                                                                              // (destination)
                                                                              // from
                                                                              // segment
                                                                              // register
                                                                              // (source)
        /* 8D */singleByteInstructions[141] = new Instruction_LEA_GvM(this); // Load
                                                                             // effective
                                                                             // address
                                                                             // computed
                                                                             // from
                                                                             // second
                                                                             // operand
                                                                             // (source)
                                                                             // to
                                                                             // register
                                                                             // (destination)
        /* 8E */singleByteInstructions[142] = new Instruction_MOV_SwEw(this); // Word-sized
                                                                              // copy
                                                                              // of
                                                                              // segment
                                                                              // register
                                                                              // (destination)
                                                                              // from
                                                                              // memory/register
                                                                              // (source)
        /* 8F */singleByteInstructions[143] = new Instruction_POP_Ev(this); // Pop
                                                                            // word
                                                                            // or
                                                                            // double
                                                                            // word
                                                                            // from
                                                                            // stack
                                                                            // into
                                                                            // segment
                                                                            // +
                                                                            // offset
        /* 90 */singleByteInstructions[144] = new Instruction_NOP(this); // No
                                                                         // operation
        /* 91 */singleByteInstructions[145] = new Instruction_XCHG_CXAX(this); // Exchange
                                                                               // contents
                                                                               // of
                                                                               // registers
                                                                               // CX
                                                                               // and
                                                                               // AX
        /* 92 */singleByteInstructions[146] = new Instruction_XCHG_DXAX(this); // Exchange
                                                                               // contents
                                                                               // of
                                                                               // registers
                                                                               // DX
                                                                               // and
                                                                               // AX
        /* 93 */singleByteInstructions[147] = new Instruction_XCHG_BXAX(this); // Exchange
                                                                               // contents
                                                                               // of
                                                                               // registers
                                                                               // BX
                                                                               // and
                                                                               // AX
        /* 94 */singleByteInstructions[148] = new Instruction_XCHG_SPAX(this); // Exchange
                                                                               // contents
                                                                               // of
                                                                               // registers
                                                                               // SP
                                                                               // and
                                                                               // AX
        /* 95 */singleByteInstructions[149] = new Instruction_XCHG_BPAX(this); // Exchange
                                                                               // contents
                                                                               // of
                                                                               // registers
                                                                               // BP
                                                                               // and
                                                                               // AX
        /* 96 */singleByteInstructions[150] = new Instruction_XCHG_SIAX(this); // Exchange
                                                                               // contents
                                                                               // of
                                                                               // registers
                                                                               // SI
                                                                               // and
                                                                               // AX
        /* 97 */singleByteInstructions[151] = new Instruction_XCHG_DIAX(this); // Exchange
                                                                               // contents
                                                                               // of
                                                                               // registers
                                                                               // DI
                                                                               // and
                                                                               // AX
        /* 98 */singleByteInstructions[152] = new Instruction_CBW(this); // Convert
                                                                         // Byte
                                                                         // to
                                                                         // Word,
                                                                         // extending
                                                                         // AL
                                                                         // sign
                                                                         // to
                                                                         // all
                                                                         // bits
                                                                         // in
                                                                         // AH
        /* 99 */singleByteInstructions[153] = new Instruction_CWD(this); // Convert
                                                                         // Word
                                                                         // to
                                                                         // DoubleWord,
                                                                         // extending
                                                                         // AX
                                                                         // sign
                                                                         // to
                                                                         // all
                                                                         // bits
                                                                         // in
                                                                         // DX
        /* 9A */singleByteInstructions[154] = new Instruction_CALLF_Ap(this); // Intersegment
                                                                              // call
                                                                              // indicated
                                                                              // by
                                                                              // immediate
                                                                              // signed
                                                                              // words
        /* 9B */singleByteInstructions[155] = new Instruction_NULL(this);
        /* 9C */singleByteInstructions[156] = new Instruction_PUSHF(this); // Transfer
                                                                           // FLAGS
                                                                           // register
                                                                           // onto
                                                                           // stack
                                                                           // SS:SP
        /* 9D */singleByteInstructions[157] = new Instruction_POPF(this); // Pop
                                                                          // word
                                                                          // from
                                                                          // stack
                                                                          // into
                                                                          // FLAGS
                                                                          // register
        /* 9E */singleByteInstructions[158] = new Instruction_SAHF(this); // 
        /* 9F */singleByteInstructions[159] = new Instruction_LAHF(this); // Move
                                                                          // low
                                                                          // byte
                                                                          // of
                                                                          // the
                                                                          // FLAGS
                                                                          // register
                                                                          // into
                                                                          // AH
                                                                          // register
        /* A0 */singleByteInstructions[160] = new Instruction_MOV_ALOb(this); // Copy
                                                                              // byte
                                                                              // DS:DISPL
                                                                              // (DISPL
                                                                              // follows
                                                                              // opcode)
                                                                              // into
                                                                              // AL
        /* A1 */singleByteInstructions[161] = new Instruction_MOV_AXOv(this); // Copy
                                                                              // word
                                                                              // DS:DISPL
                                                                              // (DISPL
                                                                              // follows
                                                                              // opcode)
                                                                              // into
                                                                              // AX
        /* A2 */singleByteInstructions[162] = new Instruction_MOV_ObAL(this); // Copy
                                                                              // byte
                                                                              // AL
                                                                              // into
                                                                              // DS:DISPL
                                                                              // (DISPL
                                                                              // follows
                                                                              // opcode)
        /* A3 */singleByteInstructions[163] = new Instruction_MOV_OvAX(this); // Copy
                                                                              // word
                                                                              // AX
                                                                              // into
                                                                              // DS:DISPL
                                                                              // (DISPL
                                                                              // follows
                                                                              // opcode)
        /* A4 */singleByteInstructions[164] = new Instruction_MOVS_XbYb(this);// Move
                                                                              // string
                                                                              // byte
                                                                              // at
                                                                              // address
                                                                              // DS:(E)SI
                                                                              // to
                                                                              // address
                                                                              // ES:(E)DI
        /* A5 */singleByteInstructions[165] = new Instruction_MOVS_XvYv(this);// Move
                                                                              // string
                                                                              // word
                                                                              // at
                                                                              // address
                                                                              // DS:(E)SI
                                                                              // to
                                                                              // address
                                                                              // ES:(E)DI
        /* A6 */singleByteInstructions[166] = new Instruction_CMPS_XbYb(this);// Compare
                                                                              // string
                                                                              // byte
                                                                              // at
                                                                              // address
                                                                              // DS:(E)SI
                                                                              // with
                                                                              // address
                                                                              // ES:(E)DI
        /* A7 */singleByteInstructions[167] = new Instruction_CMPS_XvYv(this);// Compare
                                                                              // string
                                                                              // word
                                                                              // at
                                                                              // address
                                                                              // DS:(E)SI
                                                                              // with
                                                                              // address
                                                                              // ES:(E)DI
        /* A8 */singleByteInstructions[168] = new Instruction_TEST_ALIb(this); // Logical
                                                                               // comparison
                                                                               // (AND)
                                                                               // of
                                                                               // immediate
                                                                               // byte
                                                                               // and
                                                                               // AL
        /* A9 */singleByteInstructions[169] = new Instruction_TEST_AXIv(this); // Logical
                                                                               // comparison
                                                                               // (AND)
                                                                               // of
                                                                               // immediate
                                                                               // word
                                                                               // and
                                                                               // AX
        /* AA */singleByteInstructions[170] = new Instruction_STOSB_YbAL(this); // Copy
                                                                                // byte
                                                                                // from
                                                                                // register
                                                                                // AL
                                                                                // to
                                                                                // ES:DI
        /* AB */singleByteInstructions[171] = new Instruction_STOSW_YvAX(this); // Copy
                                                                                // word
                                                                                // from
                                                                                // register
                                                                                // AX
                                                                                // to
                                                                                // ES:DI
        /* AC */singleByteInstructions[172] = new Instruction_LODS_ALXb(this); // Load
                                                                               // byte
                                                                               // from
                                                                               // DS:SI
                                                                               // into
                                                                               // AL
        /* AD */singleByteInstructions[173] = new Instruction_LODS_AXXv(this); // Load
                                                                               // word
                                                                               // from
                                                                               // DS:SI
                                                                               // into
                                                                               // AX
        /* AE */singleByteInstructions[174] = new Instruction_SCAS_ALYb(this); // Compare
                                                                               // AL
                                                                               // with
                                                                               // byte
                                                                               // at
                                                                               // ES:(E)DI
                                                                               // and
                                                                               // set
                                                                               // status
                                                                               // flags
        /* AF */singleByteInstructions[175] = new Instruction_SCAS_AXYv(this); // Compare
                                                                               // AX
                                                                               // with
                                                                               // word
                                                                               // at
                                                                               // ES:(E)DI
                                                                               // and
                                                                               // set
                                                                               // status
                                                                               // flags
        /* B0 */singleByteInstructions[176] = new Instruction_MOV_Imm_AL(this); // Copy
                                                                                // immediate
                                                                                // byte
                                                                                // into
                                                                                // AL
        /* B1 */singleByteInstructions[177] = new Instruction_MOV_Imm_CL(this); // Copy
                                                                                // immediate
                                                                                // byte
                                                                                // into
                                                                                // CL
        /* B2 */singleByteInstructions[178] = new Instruction_MOV_Imm_DL(this); // Copy
                                                                                // immediate
                                                                                // byte
                                                                                // into
                                                                                // DL
        /* B3 */singleByteInstructions[179] = new Instruction_MOV_Imm_BL(this); // Copy
                                                                                // immediate
                                                                                // byte
                                                                                // into
                                                                                // BL
        /* B4 */singleByteInstructions[180] = new Instruction_MOV_Imm_AH(this); // Copy
                                                                                // immediate
                                                                                // byte
                                                                                // into
                                                                                // AH
        /* B5 */singleByteInstructions[181] = new Instruction_MOV_Imm_CH(this); // Copy
                                                                                // immediate
                                                                                // byte
                                                                                // into
                                                                                // CH
        /* B6 */singleByteInstructions[182] = new Instruction_MOV_Imm_DH(this); // Copy
                                                                                // immediate
                                                                                // byte
                                                                                // into
                                                                                // DH
        /* B7 */singleByteInstructions[183] = new Instruction_MOV_Imm_BH(this); // Copy
                                                                                // immediate
                                                                                // byte
                                                                                // into
                                                                                // BH
        /* B8 */singleByteInstructions[184] = new Instruction_MOV_Imm_AX(this); // Copy
                                                                                // immediate
                                                                                // word
                                                                                // into
                                                                                // AX
        /* B9 */singleByteInstructions[185] = new Instruction_MOV_Imm_CX(this); // Copy
                                                                                // immediate
                                                                                // word
                                                                                // into
                                                                                // CX
        /* BA */singleByteInstructions[186] = new Instruction_MOV_Imm_DX(this); // Copy
                                                                                // immediate
                                                                                // word
                                                                                // into
                                                                                // DX
        /* BB */singleByteInstructions[187] = new Instruction_MOV_Imm_BX(this); // Copy
                                                                                // immediate
                                                                                // word
                                                                                // into
                                                                                // BX
        /* BC */singleByteInstructions[188] = new Instruction_MOV_Imm_SP(this); // Copy
                                                                                // immediate
                                                                                // word
                                                                                // into
                                                                                // SP
        /* BD */singleByteInstructions[189] = new Instruction_MOV_Imm_BP(this); // Copy
                                                                                // immediate
                                                                                // word
                                                                                // into
                                                                                // BP
        /* BE */singleByteInstructions[190] = new Instruction_MOV_Imm_SI(this); // Copy
                                                                                // immediate
                                                                                // word
                                                                                // into
                                                                                // SI
        /* BF */singleByteInstructions[191] = new Instruction_MOV_Imm_DI(this); // Copy
                                                                                // immediate
                                                                                // word
                                                                                // into
                                                                                // DI
        /* C0 */singleByteInstructions[192] = new Instruction_ShiftGRP2_EbIb(
                this); // Immediate Group 2 opcode extension: ROL, ROR, RCL,
                       // RCR, SHL/SAL, SHR, SAR
        /* C1 */singleByteInstructions[193] = new Instruction_ShiftGRP2_EvIb(
                this); // Immediate Group 2 opcode extension: ROL, ROR, RCL,
                       // RCR, SHL/SAL, SHR, SAR
        /* C2 */singleByteInstructions[194] = new Instruction_RETN_Iw(this); // Near
                                                                             // (intrasegment)
                                                                             // return
                                                                             // to
                                                                             // calling
                                                                             // procudure
                                                                             // and
                                                                             // increase
                                                                             // SP
                                                                             // with
                                                                             // imm
                                                                             // word
        /* C3 */singleByteInstructions[195] = new Instruction_RETN(this); // Near
                                                                          // (intrasegment)
                                                                          // return
                                                                          // to
                                                                          // calling
                                                                          // procedure
        /* C4 */singleByteInstructions[196] = new Instruction_LES_GvMp(this); // Load
                                                                              // far
                                                                              // pointer
                                                                              // from
                                                                              // memory
                                                                              // in
                                                                              // ES:r16
                                                                              // or
                                                                              // ES:r32
        /* C5 */singleByteInstructions[197] = new Instruction_LDS_GvMp(this); // Load
                                                                              // far
                                                                              // pointer
                                                                              // from
                                                                              // memory
                                                                              // in
                                                                              // DS:r16
                                                                              // or
                                                                              // DS:r32
        /* C6 */singleByteInstructions[198] = new Instruction_GRP11_MOV_EbIb(
                this); // Group 11 opcode extension: MOV immediate byte (source)
                       // into memory/register (destination)
        /* C7 */singleByteInstructions[199] = new Instruction_GRP11_MOV_EvIv(
                this); // Group 11 opcode extension: MOV immediate word (source)
                       // into memory/register (destination)
        /* C8 */singleByteInstructions[200] = new Instruction_ENTER_IwIb(this); // Make
                                                                                // stack
                                                                                // frame
                                                                                // for
                                                                                // procedure
                                                                                // parameters
        /* C9 */singleByteInstructions[201] = new Instruction_LEAVE(this); // High
                                                                           // Level
                                                                           // Procedure
                                                                           // Exit
        /* CA */singleByteInstructions[202] = new Instruction_RETF_Iw(this); // Far
                                                                             // (intersegment)
                                                                             // return
                                                                             // to
                                                                             // calling
                                                                             // procedure
                                                                             // and
                                                                             // pop
                                                                             // bytes
                                                                             // from
                                                                             // stack
        /* CB */singleByteInstructions[203] = new Instruction_RETF(this); // Far
                                                                          // (intersegment)
                                                                          // return
                                                                          // to
                                                                          // calling
                                                                          // procedure
        /* CC */singleByteInstructions[204] = new Instruction_INT3(this); // Call
                                                                          // to
                                                                          // Interrupt
                                                                          // 3 -
                                                                          // trap
                                                                          // to
                                                                          // debugger
        /* CD */singleByteInstructions[205] = new Instruction_INT_Ib(this); // Call
                                                                            // to
                                                                            // Interrupt
                                                                            // Procedure
        /* CE */singleByteInstructions[206] = new Instruction_NULL(this);
        /* CF */singleByteInstructions[207] = new Instruction_IRET(this); // Interrupt
                                                                          // Return
        /* D0 */singleByteInstructions[208] = new Instruction_ShiftGRP2_Eb1(
                this); // Immediate Group 2 opcode extension: ROL, ROR, RCL,
                       // RCR, SHL/SAL, SHR, SAR
        /* D1 */singleByteInstructions[209] = new Instruction_ShiftGRP2_Ev1(
                this); // Immediate Group 2 opcode extension: ROL, ROR, RCL,
                       // RCR, SHL/SAL, SHR, SAR
        /* D2 */singleByteInstructions[210] = new Instruction_ShiftGRP2_EbCL(
                this); // Immediate Group 2 opcode extension: ROL, ROR, RCL,
                       // RCR, SHL/SAL, SHR, SAR
        /* D3 */singleByteInstructions[211] = new Instruction_ShiftGRP2_EvCL(
                this); // Immediate Group 2 opcode extension: ROL, ROR, RCL,
                       // RCR, SHL/SAL, SHR, SAR
        /* D4 */singleByteInstructions[212] = new Instruction_AAM_Ib(this); // ASCII
                                                                            // adjust
                                                                            // AX
                                                                            // after
                                                                            // multiply
        /* D5 */singleByteInstructions[213] = new Instruction_AAD_Ib(this); // ASCII
                                                                            // adjust
                                                                            // AX
                                                                            // before
                                                                            // division
        /* D6 */singleByteInstructions[214] = new Instruction_SALC(this); // Set
                                                                          // AL
                                                                          // on
                                                                          // CF
                                                                          // (undocumented
                                                                          // Intel
                                                                          // instruction)
        /* D7 */singleByteInstructions[215] = new Instruction_XLAT(this); // Set
                                                                          // AL
                                                                          // to
                                                                          // memory
                                                                          // byte
                                                                          // DS:[BX
                                                                          // +
                                                                          // unsigned
                                                                          // AL]
        /* D8 */singleByteInstructions[216] = new Instruction_ESC_FPU(this); // Escape
                                                                             // to
                                                                             // coprocessor
                                                                             // instruction
                                                                             // set
                                                                             // (NOTE:
                                                                             // Empty
                                                                             // instruction
                                                                             // handler!!!)
        /* D9 */singleByteInstructions[217] = new Instruction_ESC_FPU(this); // Escape
                                                                             // to
                                                                             // coprocessor
                                                                             // instruction
                                                                             // set
                                                                             // (NOTE:
                                                                             // Empty
                                                                             // instruction
                                                                             // handler!!!)
        /* DA */singleByteInstructions[218] = new Instruction_ESC_FPU(this); // Escape
                                                                             // to
                                                                             // coprocessor
                                                                             // instruction
                                                                             // set
                                                                             // (NOTE:
                                                                             // Empty
                                                                             // instruction
                                                                             // handler!!!)
        /* DB */singleByteInstructions[219] = new Instruction_ESC_FPU(this); // Escape
                                                                             // to
                                                                             // coprocessor
                                                                             // instruction
                                                                             // set
                                                                             // (NOTE:
                                                                             // Empty
                                                                             // instruction
                                                                             // handler!!!)
        /* DC */singleByteInstructions[220] = new Instruction_ESC_FPU(this); // Escape
                                                                             // to
                                                                             // coprocessor
                                                                             // instruction
                                                                             // set
                                                                             // (NOTE:
                                                                             // Empty
                                                                             // instruction
                                                                             // handler!!!)
        /* DD */singleByteInstructions[221] = new Instruction_ESC_FPU(this); // Escape
                                                                             // to
                                                                             // coprocessor
                                                                             // instruction
                                                                             // set
                                                                             // (NOTE:
                                                                             // Empty
                                                                             // instruction
                                                                             // handler!!!)
        /* DE */singleByteInstructions[222] = new Instruction_ESC_FPU(this); // Escape
                                                                             // to
                                                                             // coprocessor
                                                                             // instruction
                                                                             // set
                                                                             // (NOTE:
                                                                             // Empty
                                                                             // instruction
                                                                             // handler!!!)
        /* DF */singleByteInstructions[223] = new Instruction_ESC_FPU(this); // Escape
                                                                             // to
                                                                             // coprocessor
                                                                             // instruction
                                                                             // set
                                                                             // (NOTE:
                                                                             // Empty
                                                                             // instruction
                                                                             // handler!!!)
        /* E0 */singleByteInstructions[224] = new Instruction_LOOPNE_LOOPNZ_Jb(
                this); // Loop while CX is not zero and ZF == 0
        /* E1 */singleByteInstructions[225] = new Instruction_LOOPE_LOOPZ_Jb(
                this); // Loop while CX is not zero and ZF == 1
        /* E2 */singleByteInstructions[226] = new Instruction_LOOP_Jb(this); // Loop
                                                                             // while
                                                                             // CX
                                                                             // is
                                                                             // not
                                                                             // zero
        /* E3 */singleByteInstructions[227] = new Instruction_JCXZ_JECXZ(this); // Conditional
                                                                                // short
                                                                                // jump
                                                                                // if
                                                                                // cx
                                                                                // is
                                                                                // zero
        /* E4 */singleByteInstructions[228] = new Instruction_IN_ALIb(this);
        /* E5 */singleByteInstructions[229] = new Instruction_NULL(this); // Put
                                                                          // byte
                                                                          // from
                                                                          // I/O
                                                                          // port
                                                                          // address
                                                                          // indicated
                                                                          // by
                                                                          // immediate
                                                                          // byte
                                                                          // into
                                                                          // AL
        /* E6 */singleByteInstructions[230] = new Instruction_OUT_IbAL(this); // Output
                                                                              // byte
                                                                              // in
                                                                              // AL
                                                                              // to
                                                                              // I/O
                                                                              // port
                                                                              // address
                                                                              // indicated
                                                                              // by
                                                                              // immediate
                                                                              // byte
        /* E7 */singleByteInstructions[231] = new Instruction_NULL(this);
        /* E8 */singleByteInstructions[232] = new Instruction_CALL_Jv(this); // Call
                                                                             // to
                                                                             // procedure
                                                                             // indicated
                                                                             // by
                                                                             // immediate
                                                                             // signed
                                                                             // word
        /* E9 */singleByteInstructions[233] = new Instruction_JMP_nearJv(this); // Unconditional
                                                                                // relative
                                                                                // near
                                                                                // jump
        /* EA */singleByteInstructions[234] = new Instruction_JMP_farAP(this); // Unconditional
                                                                               // absolute
                                                                               // far
                                                                               // jump
        /* EB */singleByteInstructions[235] = new Instruction_JMP_shortJb(this); // Unconditional
                                                                                 // relative
                                                                                 // short
                                                                                 // jump
        /* EC */singleByteInstructions[236] = new Instruction_IN_ALDX(this); // Put
                                                                             // byte
                                                                             // from
                                                                             // I/O
                                                                             // port
                                                                             // address
                                                                             // specified
                                                                             // by
                                                                             // DX
                                                                             // into
                                                                             // AL
        /* ED */singleByteInstructions[237] = new Instruction_IN_eAXDX(this); // Put
                                                                              // word/doubleword
                                                                              // from
                                                                              // I/O
                                                                              // port
                                                                              // address
                                                                              // specified
                                                                              // by
                                                                              // DX
                                                                              // into
                                                                              // eAX
        /* EE */singleByteInstructions[238] = new Instruction_OUT_DXAL(this); // Output
                                                                              // byte
                                                                              // in
                                                                              // AL
                                                                              // to
                                                                              // I/O
                                                                              // port
                                                                              // address
                                                                              // specified
                                                                              // by
                                                                              // DX
        /* EF */singleByteInstructions[239] = new Instruction_OUT_DXeAX(this); // Output
                                                                               // word/doubleword
                                                                               // in
                                                                               // eAX
                                                                               // to
                                                                               // I/O
                                                                               // port
                                                                               // address
                                                                               // specified
                                                                               // by
                                                                               // DX
        /* F0 */singleByteInstructions[240] = new Instruction_NULL(this);
        /* F1 */singleByteInstructions[241] = new Instruction_NULL(this);
        /* F2 */singleByteInstructions[242] = new Instruction_REPNE(this); // Repeat
                                                                           // execution
                                                                           // of
                                                                           // string
                                                                           // instruction
                                                                           // until
                                                                           // CX
                                                                           // ==
                                                                           // 0
                                                                           // or
                                                                           // ZF
                                                                           // is
                                                                           // set
                                                                           // to
                                                                           // 1
        /* F3 */singleByteInstructions[243] = new Instruction_REP_REPE(this); // Repeat
                                                                              // execution
                                                                              // of
                                                                              // string
                                                                              // instruction
                                                                              // until
                                                                              // CX
                                                                              // ==
                                                                              // 0
                                                                              // or
                                                                              // ZF
                                                                              // is
                                                                              // set
                                                                              // to
                                                                              // 0
        /* F4 */singleByteInstructions[244] = new Instruction_HLT(this); // HALT
        /* F5 */singleByteInstructions[245] = new Instruction_CMC(this); // Complement
                                                                         // Carry
                                                                         // Flag
        /* F6 */singleByteInstructions[246] = new Instruction_UnaryGrp3_Eb(this); // Unary
                                                                                  // Group
                                                                                  // 3
                                                                                  // opcode
                                                                                  // extension:
                                                                                  // TEST,
                                                                                  // NOT,
                                                                                  // NEG,
                                                                                  // MUL,
                                                                                  // IMUL,
                                                                                  // DIV,
                                                                                  // IDIV
        /* F7 */singleByteInstructions[247] = new Instruction_UnaryGrp3_Ev(this); // Unary
                                                                                  // Group
                                                                                  // 3
                                                                                  // opcode
                                                                                  // extension:
                                                                                  // TEST,
                                                                                  // NOT,
                                                                                  // NEG,
                                                                                  // MUL,
                                                                                  // IMUL,
                                                                                  // DIV,
                                                                                  // IDIV
        /* F8 */singleByteInstructions[248] = new Instruction_CLC(this); // Clear
                                                                         // Carry
                                                                         // Flag
        /* F9 */singleByteInstructions[249] = new Instruction_STC(this); // Set
                                                                         // Carry
                                                                         // Flag
        /* FA */singleByteInstructions[250] = new Instruction_CLI(this); // Clear
                                                                         // Interrupt
                                                                         // Enable
                                                                         // Flag
        /* FB */singleByteInstructions[251] = new Instruction_STI(this); // Set
                                                                         // Interrupt
                                                                         // Enable
                                                                         // Flag
        /* FC */singleByteInstructions[252] = new Instruction_CLD(this); // Clear
                                                                         // Direction
                                                                         // Flag
        /* FD */singleByteInstructions[253] = new Instruction_STD(this); // Set
                                                                         // Direction
                                                                         // Flag
        /* FE */singleByteInstructions[254] = new Instruction_INCDEC_GRP4(this); // INC/DEC
                                                                                 // Group
                                                                                 // 4
                                                                                 // opcode
                                                                                 // extension:
                                                                                 // INC,
                                                                                 // DEC
        /* FF */singleByteInstructions[255] = new Instruction_INCDEC_GRP5(this); // INC/DEC
                                                                                 // Group
                                                                                 // 5
                                                                                 // opcode
                                                                                 // extension:
                                                                                 // INC,
                                                                                 // DEC,
                                                                                 // CALLN,
                                                                                 // CALLF,
                                                                                 // JMPN,
                                                                                 // JMPF,
                                                                                 // PUSH

        // Initialise double-byte opcode lookup array with instruction functions
        doubleByteInstructions = new Instruction[256];
        /* 00 */doubleByteInstructions[0] = new Instruction_GRP6(this); // Group
                                                                        // 6
                                                                        // opcode
                                                                        // extension:
                                                                        // SLDT,
                                                                        // STR,
                                                                        // LLDT,
                                                                        // LTR,
                                                                        // VERR,
                                                                        // VERW
        /* 01 */doubleByteInstructions[1] = new Instruction_GRP7(this); // Group
                                                                        // 7
                                                                        // opcode
                                                                        // extension:
                                                                        // SGDT,
                                                                        // SIDT,
                                                                        // LGDT,
                                                                        // LIDT,
                                                                        // SMSW,
                                                                        // LMSW,
                                                                        // INVLPG
        /* 02 */doubleByteInstructions[2] = new Instruction_LAR(this); // Load
                                                                       // Access
                                                                       // Rights
                                                                       // byte
        /* 03 */doubleByteInstructions[3] = new Instruction_NULL(this);
        /* 04 */doubleByteInstructions[4] = new Instruction_NULL(this);
        /* 05 */doubleByteInstructions[5] = new Instruction_NULL(this);
        /* 06 */doubleByteInstructions[6] = new Instruction_NULL(this);
        /* 07 */doubleByteInstructions[7] = new Instruction_NULL(this);
        /* 08 */doubleByteInstructions[8] = new Instruction_NULL(this);
        /* 09 */doubleByteInstructions[9] = new Instruction_NULL(this);
        /* 0A */doubleByteInstructions[10] = new Instruction_NULL(this);
        /* 0B */doubleByteInstructions[11] = new Instruction_NULL(this);
        /* 0C */doubleByteInstructions[12] = new Instruction_NULL(this);
        /* 0D */doubleByteInstructions[13] = new Instruction_NULL(this);
        /* 0E */doubleByteInstructions[14] = new Instruction_NULL(this);
        /* 0F */doubleByteInstructions[15] = new Instruction_NULL(this);
        /* 10 */doubleByteInstructions[16] = new Instruction_NULL(this);
        /* 11 */doubleByteInstructions[17] = new Instruction_NULL(this);
        /* 12 */doubleByteInstructions[18] = new Instruction_NULL(this);
        /* 13 */doubleByteInstructions[19] = new Instruction_NULL(this);
        /* 14 */doubleByteInstructions[20] = new Instruction_NULL(this);
        /* 15 */doubleByteInstructions[21] = new Instruction_NULL(this);
        /* 16 */doubleByteInstructions[22] = new Instruction_NULL(this);
        /* 17 */doubleByteInstructions[23] = new Instruction_NULL(this);
        /* 18 */doubleByteInstructions[24] = new Instruction_NULL(this);
        /* 19 */doubleByteInstructions[25] = new Instruction_NULL(this);
        /* 1A */doubleByteInstructions[26] = new Instruction_NULL(this);
        /* 1B */doubleByteInstructions[27] = new Instruction_NULL(this);
        /* 1C */doubleByteInstructions[28] = new Instruction_NULL(this);
        /* 1D */doubleByteInstructions[29] = new Instruction_NULL(this);
        /* 1E */doubleByteInstructions[30] = new Instruction_NULL(this);
        /* 1F */doubleByteInstructions[31] = new Instruction_NULL(this);
        /* 20 */doubleByteInstructions[32] = new Instruction_NULL(this);
        /* 21 */doubleByteInstructions[33] = new Instruction_NULL(this);
        /* 22 */doubleByteInstructions[34] = new Instruction_NULL(this);
        /* 23 */doubleByteInstructions[35] = new Instruction_NULL(this);
        /* 24 */doubleByteInstructions[36] = new Instruction_NULL(this);
        /* 25 */doubleByteInstructions[37] = new Instruction_NULL(this);
        /* 26 */doubleByteInstructions[38] = new Instruction_NULL(this);
        /* 27 */doubleByteInstructions[39] = new Instruction_NULL(this);
        /* 28 */doubleByteInstructions[40] = new Instruction_NULL(this);
        /* 29 */doubleByteInstructions[41] = new Instruction_NULL(this);
        /* 2A */doubleByteInstructions[42] = new Instruction_NULL(this);
        /* 2B */doubleByteInstructions[43] = new Instruction_NULL(this);
        /* 2C */doubleByteInstructions[44] = new Instruction_NULL(this);
        /* 2D */doubleByteInstructions[45] = new Instruction_NULL(this);
        /* 2E */doubleByteInstructions[46] = new Instruction_NULL(this);
        /* 2F */doubleByteInstructions[47] = new Instruction_NULL(this);
        /* 30 */doubleByteInstructions[48] = new Instruction_NULL(this);
        /* 31 */doubleByteInstructions[49] = new Instruction_NULL(this);
        /* 32 */doubleByteInstructions[50] = new Instruction_NULL(this);
        /* 33 */doubleByteInstructions[51] = new Instruction_NULL(this);
        /* 34 */doubleByteInstructions[52] = new Instruction_NULL(this);
        /* 35 */doubleByteInstructions[53] = new Instruction_NULL(this);
        /* 36 */doubleByteInstructions[54] = new Instruction_NULL(this);
        /* 37 */doubleByteInstructions[55] = new Instruction_NULL(this);
        /* 38 */doubleByteInstructions[56] = new Instruction_NULL(this);
        /* 39 */doubleByteInstructions[57] = new Instruction_NULL(this);
        /* 3A */doubleByteInstructions[58] = new Instruction_NULL(this);
        /* 3B */doubleByteInstructions[59] = new Instruction_NULL(this);
        /* 3C */doubleByteInstructions[60] = new Instruction_NULL(this);
        /* 3D */doubleByteInstructions[61] = new Instruction_NULL(this);
        /* 3E */doubleByteInstructions[62] = new Instruction_NULL(this);
        /* 3F */doubleByteInstructions[63] = new Instruction_NULL(this);
        /* 40 */doubleByteInstructions[64] = new Instruction_NULL(this);
        /* 41 */doubleByteInstructions[65] = new Instruction_NULL(this);
        /* 42 */doubleByteInstructions[66] = new Instruction_NULL(this);
        /* 43 */doubleByteInstructions[67] = new Instruction_NULL(this);
        /* 44 */doubleByteInstructions[68] = new Instruction_NULL(this);
        /* 45 */doubleByteInstructions[69] = new Instruction_NULL(this);
        /* 46 */doubleByteInstructions[70] = new Instruction_NULL(this);
        /* 47 */doubleByteInstructions[71] = new Instruction_NULL(this);
        /* 48 */doubleByteInstructions[72] = new Instruction_NULL(this);
        /* 49 */doubleByteInstructions[73] = new Instruction_NULL(this);
        /* 4A */doubleByteInstructions[74] = new Instruction_NULL(this);
        /* 4B */doubleByteInstructions[75] = new Instruction_NULL(this);
        /* 4C */doubleByteInstructions[76] = new Instruction_NULL(this);
        /* 4D */doubleByteInstructions[77] = new Instruction_NULL(this);
        /* 4E */doubleByteInstructions[78] = new Instruction_NULL(this);
        /* 4F */doubleByteInstructions[79] = new Instruction_NULL(this);
        /* 50 */doubleByteInstructions[80] = new Instruction_NULL(this);
        /* 51 */doubleByteInstructions[81] = new Instruction_NULL(this);
        /* 52 */doubleByteInstructions[82] = new Instruction_NULL(this);
        /* 53 */doubleByteInstructions[83] = new Instruction_NULL(this);
        /* 54 */doubleByteInstructions[84] = new Instruction_NULL(this);
        /* 55 */doubleByteInstructions[85] = new Instruction_NULL(this);
        /* 56 */doubleByteInstructions[86] = new Instruction_NULL(this);
        /* 57 */doubleByteInstructions[87] = new Instruction_NULL(this);
        /* 58 */doubleByteInstructions[88] = new Instruction_NULL(this);
        /* 59 */doubleByteInstructions[89] = new Instruction_NULL(this);
        /* 5A */doubleByteInstructions[90] = new Instruction_NULL(this);
        /* 5B */doubleByteInstructions[91] = new Instruction_NULL(this);
        /* 5C */doubleByteInstructions[92] = new Instruction_NULL(this);
        /* 5D */doubleByteInstructions[93] = new Instruction_NULL(this);
        /* 5E */doubleByteInstructions[94] = new Instruction_NULL(this);
        /* 5F */doubleByteInstructions[95] = new Instruction_NULL(this);
        /* 60 */doubleByteInstructions[96] = new Instruction_NULL(this);
        /* 61 */doubleByteInstructions[97] = new Instruction_NULL(this);
        /* 62 */doubleByteInstructions[98] = new Instruction_NULL(this);
        /* 63 */doubleByteInstructions[99] = new Instruction_NULL(this);
        /* 64 */doubleByteInstructions[100] = new Instruction_NULL(this);
        /* 65 */doubleByteInstructions[101] = new Instruction_NULL(this);
        /* 66 */doubleByteInstructions[102] = new Instruction_NULL(this);
        /* 67 */doubleByteInstructions[103] = new Instruction_NULL(this);
        /* 68 */doubleByteInstructions[104] = new Instruction_NULL(this);
        /* 69 */doubleByteInstructions[105] = new Instruction_NULL(this);
        /* 6A */doubleByteInstructions[106] = new Instruction_NULL(this);
        /* 6B */doubleByteInstructions[107] = new Instruction_NULL(this);
        /* 6C */doubleByteInstructions[108] = new Instruction_NULL(this);
        /* 6D */doubleByteInstructions[109] = new Instruction_NULL(this);
        /* 6E */doubleByteInstructions[110] = new Instruction_NULL(this);
        /* 6F */doubleByteInstructions[111] = new Instruction_NULL(this);
        /* 70 */doubleByteInstructions[112] = new Instruction_NULL(this);
        /* 71 */doubleByteInstructions[113] = new Instruction_NULL(this);
        /* 72 */doubleByteInstructions[114] = new Instruction_NULL(this);
        /* 73 */doubleByteInstructions[115] = new Instruction_NULL(this);
        /* 74 */doubleByteInstructions[116] = new Instruction_NULL(this);
        /* 75 */doubleByteInstructions[117] = new Instruction_NULL(this);
        /* 76 */doubleByteInstructions[118] = new Instruction_NULL(this);
        /* 77 */doubleByteInstructions[119] = new Instruction_NULL(this);
        /* 78 */doubleByteInstructions[120] = new Instruction_NULL(this);
        /* 79 */doubleByteInstructions[121] = new Instruction_NULL(this);
        /* 7A */doubleByteInstructions[122] = new Instruction_NULL(this);
        /* 7B */doubleByteInstructions[123] = new Instruction_NULL(this);
        /* 7C */doubleByteInstructions[124] = new Instruction_NULL(this);
        /* 7D */doubleByteInstructions[125] = new Instruction_NULL(this);
        /* 7E */doubleByteInstructions[126] = new Instruction_NULL(this);
        /* 7F */doubleByteInstructions[127] = new Instruction_NULL(this);
        /* 80 */doubleByteInstructions[128] = new Instruction_NULL(this);
        /* 81 */doubleByteInstructions[129] = new Instruction_NULL(this);
        /* 82 */doubleByteInstructions[130] = new Instruction_JB_JNAE_JC_long(
                this); // Conditional long jump on carry
        /* 83 */doubleByteInstructions[131] = new Instruction_JNB_JAE_JNC_long(
                this); // Conditional long jump on not carry
        /* 84 */doubleByteInstructions[132] = new Instruction_JZ_JE_long(this); // Conditional
                                                                                // long
                                                                                // jump
                                                                                // on
                                                                                // zero
        /* 85 */doubleByteInstructions[133] = new Instruction_JNZ_JNE_long(this); // Conditional
                                                                                  // long
                                                                                  // jump
                                                                                  // not
                                                                                  // zero
        /* 86 */doubleByteInstructions[134] = new Instruction_JBE_JNA_long(this); // Conditional
                                                                                  // long
                                                                                  // jump
                                                                                  // on
                                                                                  // carry
                                                                                  // or
                                                                                  // zero
        /* 87 */doubleByteInstructions[135] = new Instruction_NULL(this);
        /* 88 */doubleByteInstructions[136] = new Instruction_NULL(this);
        /* 89 */doubleByteInstructions[137] = new Instruction_NULL(this);
        /* 8A */doubleByteInstructions[138] = new Instruction_NULL(this);
        /* 8B */doubleByteInstructions[139] = new Instruction_NULL(this);
        /* 8C */doubleByteInstructions[140] = new Instruction_NULL(this);
        /* 8D */doubleByteInstructions[141] = new Instruction_NULL(this);
        /* 8E */doubleByteInstructions[142] = new Instruction_NULL(this);
        /* 8F */doubleByteInstructions[143] = new Instruction_NULL(this);
        /* 90 */doubleByteInstructions[144] = new Instruction_NULL(this);
        /* 91 */doubleByteInstructions[145] = new Instruction_NULL(this);
        /* 92 */doubleByteInstructions[146] = new Instruction_NULL(this);
        /* 93 */doubleByteInstructions[147] = new Instruction_NULL(this);
        /* 94 */doubleByteInstructions[148] = new Instruction_NULL(this);
        /* 95 */doubleByteInstructions[149] = new Instruction_NULL(this);
        /* 96 */doubleByteInstructions[150] = new Instruction_NULL(this);
        /* 97 */doubleByteInstructions[151] = new Instruction_NULL(this);
        /* 98 */doubleByteInstructions[152] = new Instruction_NULL(this);
        /* 99 */doubleByteInstructions[153] = new Instruction_NULL(this);
        /* 9A */doubleByteInstructions[154] = new Instruction_NULL(this);
        /* 9B */doubleByteInstructions[155] = new Instruction_NULL(this);
        /* 9C */doubleByteInstructions[156] = new Instruction_NULL(this);
        /* 9D */doubleByteInstructions[157] = new Instruction_NULL(this);
        /* 9E */doubleByteInstructions[158] = new Instruction_NULL(this);
        /* 9F */doubleByteInstructions[159] = new Instruction_NULL(this);
        /* A0 */doubleByteInstructions[160] = new Instruction_NULL(this);
        /* A1 */doubleByteInstructions[161] = new Instruction_NULL(this);
        /* A2 */doubleByteInstructions[162] = new Instruction_NULL(this);
        /* A3 */doubleByteInstructions[163] = new Instruction_NULL(this);
        /* A4 */doubleByteInstructions[164] = new Instruction_NULL(this);
        /* A5 */doubleByteInstructions[165] = new Instruction_NULL(this);
        /* A6 */doubleByteInstructions[166] = new Instruction_NULL(this);
        /* A7 */doubleByteInstructions[167] = new Instruction_NULL(this);
        /* A8 */doubleByteInstructions[168] = new Instruction_NULL(this);
        /* A9 */doubleByteInstructions[169] = new Instruction_NULL(this);
        /* AA */doubleByteInstructions[170] = new Instruction_NULL(this);
        /* AB */doubleByteInstructions[171] = new Instruction_NULL(this);
        /* AC */doubleByteInstructions[172] = new Instruction_NULL(this);
        /* AD */doubleByteInstructions[173] = new Instruction_NULL(this);
        /* AE */doubleByteInstructions[174] = new Instruction_NULL(this);
        /* AF */doubleByteInstructions[175] = new Instruction_NULL(this);
        /* B0 */doubleByteInstructions[176] = new Instruction_NULL(this);
        /* B1 */doubleByteInstructions[177] = new Instruction_NULL(this);
        /* B2 */doubleByteInstructions[178] = new Instruction_NULL(this);
        /* B3 */doubleByteInstructions[179] = new Instruction_NULL(this);
        /* B4 */doubleByteInstructions[180] = new Instruction_NULL(this);
        /* B5 */doubleByteInstructions[181] = new Instruction_NULL(this);
        /* B6 */doubleByteInstructions[182] = new Instruction_NULL(this);
        /* B7 */doubleByteInstructions[183] = new Instruction_MOVZX_GvEw(this); // Move
                                                                                // with
                                                                                // zero-extend
        /* B8 */doubleByteInstructions[184] = new Instruction_NULL(this);
        /* B9 */doubleByteInstructions[185] = new Instruction_NULL(this);
        /* BA */doubleByteInstructions[186] = new Instruction_NULL(this);
        /* BB */doubleByteInstructions[187] = new Instruction_NULL(this);
        /* BC */doubleByteInstructions[188] = new Instruction_NULL(this);
        /* BD */doubleByteInstructions[189] = new Instruction_NULL(this);
        /* BE */doubleByteInstructions[190] = new Instruction_NULL(this);
        /* BF */doubleByteInstructions[191] = new Instruction_NULL(this);
        /* C0 */doubleByteInstructions[192] = new Instruction_NULL(this);
        /* C1 */doubleByteInstructions[193] = new Instruction_NULL(this);
        /* C2 */doubleByteInstructions[194] = new Instruction_NULL(this);
        /* C3 */doubleByteInstructions[195] = new Instruction_NULL(this);
        /* C4 */doubleByteInstructions[196] = new Instruction_NULL(this);
        /* C5 */doubleByteInstructions[197] = new Instruction_NULL(this);
        /* C6 */doubleByteInstructions[198] = new Instruction_NULL(this);
        /* C7 */doubleByteInstructions[199] = new Instruction_NULL(this);
        /* C8 */doubleByteInstructions[200] = new Instruction_NULL(this);
        /* C9 */doubleByteInstructions[201] = new Instruction_NULL(this);
        /* CA */doubleByteInstructions[202] = new Instruction_NULL(this);
        /* CB */doubleByteInstructions[203] = new Instruction_NULL(this);
        /* CC */doubleByteInstructions[204] = new Instruction_NULL(this);
        /* CD */doubleByteInstructions[205] = new Instruction_NULL(this);
        /* CE */doubleByteInstructions[206] = new Instruction_NULL(this);
        /* CF */doubleByteInstructions[207] = new Instruction_NULL(this);
        /* D0 */doubleByteInstructions[208] = new Instruction_NULL(this);
        /* D1 */doubleByteInstructions[209] = new Instruction_NULL(this);
        /* D2 */doubleByteInstructions[210] = new Instruction_NULL(this);
        /* D3 */doubleByteInstructions[211] = new Instruction_NULL(this);
        /* D4 */doubleByteInstructions[212] = new Instruction_NULL(this);
        /* D5 */doubleByteInstructions[213] = new Instruction_NULL(this);
        /* D6 */doubleByteInstructions[214] = new Instruction_NULL(this);
        /* D7 */doubleByteInstructions[215] = new Instruction_NULL(this);
        /* D8 */doubleByteInstructions[216] = new Instruction_NULL(this);
        /* D9 */doubleByteInstructions[217] = new Instruction_NULL(this);
        /* DA */doubleByteInstructions[218] = new Instruction_NULL(this);
        /* DB */doubleByteInstructions[219] = new Instruction_NULL(this);
        /* DC */doubleByteInstructions[220] = new Instruction_NULL(this);
        /* DD */doubleByteInstructions[221] = new Instruction_NULL(this);
        /* DE */doubleByteInstructions[222] = new Instruction_NULL(this);
        /* DF */doubleByteInstructions[223] = new Instruction_NULL(this);
        /* E0 */doubleByteInstructions[224] = new Instruction_NULL(this);
        /* E1 */doubleByteInstructions[225] = new Instruction_NULL(this);
        /* E2 */doubleByteInstructions[226] = new Instruction_NULL(this);
        /* E3 */doubleByteInstructions[227] = new Instruction_NULL(this);
        /* E4 */doubleByteInstructions[228] = new Instruction_NULL(this);
        /* E5 */doubleByteInstructions[229] = new Instruction_NULL(this);
        /* E6 */doubleByteInstructions[230] = new Instruction_NULL(this);
        /* E7 */doubleByteInstructions[231] = new Instruction_NULL(this);
        /* E8 */doubleByteInstructions[232] = new Instruction_NULL(this);
        /* E9 */doubleByteInstructions[233] = new Instruction_NULL(this);
        /* EA */doubleByteInstructions[234] = new Instruction_NULL(this);
        /* EB */doubleByteInstructions[235] = new Instruction_NULL(this);
        /* EC */doubleByteInstructions[236] = new Instruction_NULL(this);
        /* ED */doubleByteInstructions[237] = new Instruction_NULL(this);
        /* EE */doubleByteInstructions[238] = new Instruction_NULL(this);
        /* EF */doubleByteInstructions[239] = new Instruction_NULL(this);
        /* F0 */doubleByteInstructions[240] = new Instruction_NULL(this);
        /* F1 */doubleByteInstructions[241] = new Instruction_NULL(this);
        /* F2 */doubleByteInstructions[242] = new Instruction_NULL(this);
        /* F3 */doubleByteInstructions[243] = new Instruction_NULL(this);
        /* F4 */doubleByteInstructions[244] = new Instruction_NULL(this);
        /* F5 */doubleByteInstructions[245] = new Instruction_NULL(this);
        /* F6 */doubleByteInstructions[246] = new Instruction_NULL(this);
        /* F7 */doubleByteInstructions[247] = new Instruction_NULL(this);
        /* F8 */doubleByteInstructions[248] = new Instruction_NULL(this);
        /* F9 */doubleByteInstructions[249] = new Instruction_NULL(this);
        /* FA */doubleByteInstructions[250] = new Instruction_NULL(this);
        /* FB */doubleByteInstructions[251] = new Instruction_NULL(this);
        /* FC */doubleByteInstructions[252] = new Instruction_NULL(this);
        /* FD */doubleByteInstructions[253] = new Instruction_NULL(this);
        /* FE */doubleByteInstructions[254] = new Instruction_NULL(this);
        /* FF */doubleByteInstructions[255] = new Instruction_NULL(this);

        return true;
    }

    /**
     * Set the boolean that starts and stops the CPU loop
     * 
     * @param status
     *            sets the isRunning boolean
     */
    protected void setRunning(boolean status) {
        // Set the isRunning flag
        isRunning = status;
    }

    /**
     * Returns the value of a named register.
     * 
     * @param registerName
     * 
     * @return value of register, null otherwise
     */
    public byte[] getRegisterValue(String registerName) {
        byte[] register = this.convertStringToRegister(registerName);
        if (register != null) {
            return register;
        }
        return null;
    }

    /**
     * Sets the value of a named register to given value.
     * 
     * @param
     *            registerName
     * 
     * @param  value containing the value
     * 
     * @return true if set was successful, false otherwise
     */
    public boolean setRegisterValue(String registerName, byte[] value) {
        byte[] register = this.convertStringToRegister(registerName);
        if (register != null) {
            register[0] = value[0];
            register[1] = value[1];
            // TODO: Check if alternative below works
            // register = value;
            return true;
        }
        return false;
    }

    /**
     * Returns the value of a named flag.
     * 
     * @param flagLetter
     *            First letter of flag name
     * 
     * @return value of flag
     */
    public boolean getFlagValue(char flagLetter) {
        switch (flagLetter) {
        case 'c':
        case 'C':
            return flags[REGISTER_FLAGS_CF];
        case 'p':
        case 'P':
            return flags[REGISTER_FLAGS_PF];
        case 'a':
        case 'A':
            return flags[REGISTER_FLAGS_AF];
        case 'z':
        case 'Z':
            return flags[REGISTER_FLAGS_ZF];
        case 's':
        case 'S':
            return flags[REGISTER_FLAGS_SF];
        case 't':
        case 'T':
            return flags[REGISTER_FLAGS_TF];
        case 'i':
        case 'I':
            return flags[REGISTER_FLAGS_IF];
        case 'd':
        case 'D':
            return flags[REGISTER_FLAGS_DF];
        case 'o':
        case 'O':
            return flags[REGISTER_FLAGS_OF];

        default:
            return false;
        }

    }

    /**
     * Shows next instruction to be processed
     * 
     */
    public String getNextInstructionInfo() {

        ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
        ModulePIC pic = (ModulePIC)super.getConnection(Module.Type.PIC);
        ModuleClock clock = (ModuleClock)super.getConnection(Module.Type.CLOCK);

        String dump = "";
        String ret = "\r\n";
        String tab = "\t";

        // Show current instruction number
        dump = "Instruction number = " + instructionCounter + ret;

        // Print CS:IP and the first three bytes of memory
        dump += Integer.toHexString(0x100 | cs[REGISTER_GENERAL_HIGH] & 0xFF)
                .substring(1)
                + Integer.toHexString(0x100 | cs[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1)
                + ":"
                + Integer.toHexString(0x100 | ip[REGISTER_GENERAL_HIGH] & 0xFF)
                        .substring(1)
                + Integer.toHexString(0x100 | ip[REGISTER_GENERAL_LOW] & 0xFF)
                        .substring(1) + "   ";
        try {
            dump += Integer
                    .toHexString(
                            0x100 | (memory
                                    .getByte(((((cs[REGISTER_SEGMENT_HIGH]) << 12) & 0xFFFFF)
                                            + (((cs[REGISTER_SEGMENT_LOW]) << 4) & 0xFFF)
                                            + (((ip[REGISTER_SEGMENT_HIGH]) << 8) & 0xFFFF) + ((ip[REGISTER_SEGMENT_LOW]) & 0xFF))) & 0xFF))
                    .substring(1).toUpperCase()
                    + " ";
            dump += Integer
                    .toHexString(
                            0x100 | (memory
                                    .getByte(((((cs[REGISTER_SEGMENT_HIGH]) << 12) & 0xFFFFF)
                                            + (((cs[REGISTER_SEGMENT_LOW]) << 4) & 0xFFF)
                                            + (((ip[REGISTER_SEGMENT_HIGH]) << 8) & 0xFFFF)
                                            + ((ip[REGISTER_SEGMENT_LOW]) & 0xFF) + 1)) & 0xFF))
                    .substring(1).toUpperCase()
                    + " ";
            dump += Integer
                    .toHexString(
                            0x100 | (memory
                                    .getByte(((((cs[REGISTER_SEGMENT_HIGH]) << 12) & 0xFFFFF)
                                            + (((cs[REGISTER_SEGMENT_LOW]) << 4) & 0xFFF)
                                            + (((ip[REGISTER_SEGMENT_HIGH]) << 8) & 0xFFFF)
                                            + ((ip[REGISTER_SEGMENT_LOW]) & 0xFF) + 2)) & 0xFF))
                    .substring(1).toUpperCase()
                    + tab;

            // Determine instruction from instruction table, print name
            // Cast byte to unsigned int first before instruction table lookup
            String instruct = singleByteInstructions[((int) (memory
                    .getByte((((cs[REGISTER_SEGMENT_HIGH] & 0xFF) << 12)
                            + ((cs[REGISTER_SEGMENT_LOW] & 0xFF) << 4)
                            + ((ip[REGISTER_SEGMENT_HIGH] & 0xFF) << 8) + (ip[REGISTER_SEGMENT_LOW] & 0xFF))) & 0xFF))]
                    .toString();
            instruct = instruct.substring(instruct.indexOf("_") + 1, instruct
                    .indexOf("@"));
            dump += instruct;
        } catch (ModuleException e) {
            logger.log(Level.SEVERE, "[" + super.getType() + "] AbstractModule exception: "
                    + e.getMessage());
            dump += "Failed to retrieve memory information";
        }

        return dump;
    }

    /**
     * Retrieve current number of instruction (instructions executed so far)
     * 
     * @return long containing number of instructions
     * 
     */
    public long getCurrentInstructionNumber() {
        return instructionCounter;
    }

    /**
     * Increment current number of instruction by one
     * 
     */
    protected void incrementInstructionCounter() {
        // instructionCounter++;
    }

    /**
     * Returns the value (byte) in I/O address space at given port address.
     * 
     * @param portAddress
     * 
     * @return byte value
     * @throws ModuleException
     *             , ModuleWriteOnlyPortException
     */
    public byte getIOPortByte(int portAddress) throws ModuleException {
        // Retrieve data from I/O address space at port address
        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
        return motherboard.getIOPortByte(portAddress);
    }

    /**
     * Sets the value (byte) in I/O address space at given port address.
     * 
     * @param portAddress
     * 
     * @param data
     * 
     * @throws ModuleException
     */
    public void setIOPortByte(int portAddress, byte data)
            throws ModuleException {
        // Set data in I/O address space at port address
        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
        motherboard.setIOPortByte(portAddress, data);
    }

    /**
     * Returns the value (word) in I/O address space at given port address.
     * 
     * @param portAddress
     * 
     * @return byte[] word value
     * @throws ModuleException
     *             , ModuleWriteOnlyPortException
     */
    public byte[] getIOPortWord(int portAddress) throws ModuleException {
        // Retrieve data from I/O address space at port address
        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
        return motherboard.getIOPortWord(portAddress);
    }

    /**
     * Sets the value (word) in I/O address space at given port address.
     * 
     * @param portAddress
     * 
     * @param data word value
     * 
     * @throws ModuleException
     */
    public void setIOPortWord(int portAddress, byte[] data)
            throws ModuleException {
        // Set data in I/O address space at port address
        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
        motherboard.setIOPortWord(portAddress, data);
    }

    /**
     * Returns the value (double word) in I/O address space at given port
     * address.
     * 
     * @param portAddress
     * 
     * @return byte[] double word value
     * @throws ModuleException
     *             , ModuleWriteOnlyPortException
     */
    public byte[] getIOPortDoubleWord(int portAddress)
            throws ModuleException {
        // Retrieve data from I/O address space at port address
        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
        return motherboard.getIOPortDoubleWord(portAddress);
    }

    /**
     * Sets the value (double word) in I/O address space at given port address.
     * 
     * @param portAddress
     * 
     * @param data double word value
     * 
     * @throws ModuleException
     */
    public void setIOPortDoubleWord(int portAddress, byte[] data)
            throws ModuleException {
        // Set data in I/O address space at port address
        ModuleMotherboard motherboard = (ModuleMotherboard)super.getConnection(Module.Type.MOTHERBOARD);
        motherboard.setIOPortDoubleWord(portAddress, data);
    }

    /**
     * Sets the value of the interrupt request (IRQ).
     * 
     * @param value
     */
    public void interruptRequest(boolean value) {
        irqPending = value;
        asyncEvent = true;
    }

    // ******************************************************************************
    // Additional Methods

    /**
     * Check prefix Find out if current codeByte is a prefix or not
     * 
     * @return boolean true if codeByte is a prefix, false otherwise
     */
    private boolean isPrefix() {
        if (codeByte == 0xF2 || codeByte == 0xF3 || codeByte == 0x26
                || codeByte == 0x2E || codeByte == 0x36 || codeByte == 0x3E
                || codeByte == 0x66) {
            return true;
        }
        return false;
    }

    /**
     * Reset all prefixes
     * 
     */
    protected void resetPrefixes() {
        segmentOverride = false;
        doubleWord = false;
        prefixRep = false;
        // ...

        // Reset prefix counter
        prefixCounter = 0;
    }

    /**
     * Checks if 32-bit is supported by instruction Note: this instruction
     * should only be used in debug mode, because it unnecessarily slows down
     * the execution. Checking is done based on a known 32-bit list. All
     * instructions that are not on the list are assumed to be 16 bit.
     * 
     * @return boolean true if 32 bit is supported, false otherwise
     */
    private boolean isSingleByte32BitSupported() {
        // Current instructions that support 32 bit:
        switch (codeByte) {
        // Full 32-bit support
        case 0x01: // ADD_EvGv
        case 0x25: // AND_eAXIv
        case 0x31: // XOR_EvGv
        case 0x35: // XOR_AxIv
        case 0x3D: // CMP_eAXIv
        case 0x40: // INC_eAX
        case 0x50: // PUSH_eAX
        case 0x51: // PUSH_eCX
        case 0x52: // PUSH_eDX
        case 0x53: // PUSH_eBX
        case 0x54: // PUSH_eSP
        case 0x55: // PUSH_eBP
        case 0x56: // PUSH_eSI
        case 0x57: // PUSH_eDI
        case 0x58: // POP_eAX
        case 0x59: // POP_eCX
        case 0x5A: // POP_eDX
        case 0x5B: // POP_eBX
        case 0x5C: // POP_eSP
        case 0x5D: // POP_eBP
        case 0x5E: // POP_eSI
        case 0x5F: // POP_eDI
        case 0x68: // PUSH_Iv
        case 0x6D: // INSW_YvDX
        case 0x89: // MOV_EvGv
        case 0x9C: // PUSHF
        case 0x9D: // POPF
        case 0xA1: // MOV_eAxOv
        case 0xA3: // MOV_OVeAX
        case 0xB8: // MOV_eAX
        case 0xB9: // MOV_eCX
        case 0xBA: // MOV_eDX
        case 0xBB: // MOV_eBX
        case 0xBC: // MOV_eSP
        case 0xBD: // MOV_eBP
        case 0xBE: // MOV_eSI
        case 0xBF: // MOV_eDI
        case 0xED: // IN_eAXDX
        case 0xEF: // OUT_DXeAX
        case 0xF7: // UnaryGRP3_Ev
            break;

        // Part 32-bit support
        case 0x81: // ImmGRP1_EvIv (AND only)
        case 0x83: // ImmGRP1_EvIb (AND only)
        case 0xD1: // ShiftGRP2_Ev1 (SHL, SHR, SAR only)
        case 0xD3: // UnaryGRP2_EvCL (SHL, SHR only)
        case 0xC1: // ShiftGRP2_EvIb (SHL, SHR only)
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] Instruction problem (opcode "
                    + Integer.toHexString(codeByte) + "h, at "
                    + Integer.toHexString(convertWordToInt(cs)).toUpperCase()
                    + ":"
                    + Integer.toHexString(convertWordToInt(ip)).toUpperCase()
                    + "): 32-bit not fully supported!");
            break;

        // Double byte instruction encountered
        case 0x0F: // 2-byte escape, do nothing
            break;

        default:
            return false;
        }

        return true;
    }

    /**
     * Checks if 32-bit is supported by the double byte instruction Note: this
     * instruction should only be used in debug mode, because it unnecessarily
     * slows down the execution. Checking is done based on a known 32-bit list.
     * All instructions that are not on the list are assumed to be 16 bit.
     * 
     * @return boolean true if 32 bit is supported, false otherwise
     */
    protected boolean isDoubleByte32BitSupported() {
        // Current instructions that support 32 bit:
        switch (codeByte2) {
            // Full 32-bit support

            // break;

            // Partly 32-bit support
            case 0x01: // 01 GRP7 (LGDT, SGDT only)
                logger.log(Level.SEVERE, "[" + super.getType()
                        + "] Instruction problem (opcode "
                        + Integer.toHexString(codeByte) + " "
                        + Integer.toHexString(codeByte2)
                        + "h): 32-bit not fully supported!");
                break;
    
            default:
                return false;
        }

        return true;
    }

    /**
     * Execute instruction Performs one instruction, taking prefixes into
     * account (especially the repeat prefix)
     * 
     * @throws CPUInstructionException
     */
    private void executeInstruction() throws CPUInstructionException {
        // Check prefix REP
        if (prefixRep == true) {
            // Check terminate condition 1: CX
            if (cx[CPU.REGISTER_GENERAL_LOW] == 0
                    && cx[CPU.REGISTER_GENERAL_HIGH] == 0) {
                // CX is zero, so terminate
                return;
            } else {
                // Execute current instruction
                singleByteInstructions[codeByte].execute();

                // Decrement CX register with 1
                tempWord = Util.subtractWords(cx, WORD_0X0001, 0);
                System.arraycopy(tempWord, 0, cx, 0, tempWord.length);

                // Check terminate condition 1: CX
                if (cx[CPU.REGISTER_GENERAL_LOW] == 0
                        && cx[CPU.REGISTER_GENERAL_HIGH] == 0) {
                    return;
                }

                // Check ZF if necesarry (depending on type of rep)
                if (prefixRepType == (0xF3 & 0xFF)) {
                    // REPE/REPZ: 0xF3
                    // Check if ZF has also been taken into account
                    if (codeByte == (0xA6 & 0xFF) || codeByte == (0xA7 & 0xFF)
                            || codeByte == (0xAE & 0xFF)
                            || codeByte == (0xAF & 0xFF)) {
                        // Terminate condition 2: ZF is zero
                        if (flags[CPU.REGISTER_FLAGS_ZF] == false) {
                            // ZF is zero, so terminate
                            return;
                        }
                    }
                } else if (prefixRepType == (0xF2 & 0xFF)) {
                    // REPNE/REPNZ: 0xF2
                    // Terminate condition 2: ZF is not zero
                    // NOTE: According to the Intel docs, REPNE should only be
                    // called in combination with a string instruction that sets
                    // the zero flag (ZF),
                    // such as CMPS and SCAS (A6, A7; AE, AF). However, not all
                    // programs follow this convention (*cough* MS-DOS *cough*),
                    // so in that case
                    // checking the ZF has no use and may harm the execution
                    // flow. Hence an extra check is necesarry to see if next
                    // instruction is CMPS or SCAS.
                    if (codeByte == (0xA6 & 0xFF) || codeByte == (0xA7 & 0xFF)
                            || codeByte == (0xAE & 0xFF)
                            || codeByte == (0xAF & 0xFF)) {
                        if (flags[CPU.REGISTER_FLAGS_ZF] == true) {
                            // ZF is not zero, so terminate
                            return;
                        }
                    }
                }

                // Rep has to be repeated, so set back IP pointer to position
                // before ([prefix] [instruction])
                // Note: it is assumed (by Intel spec) that repeated instruction
                // consists only of 1 byte (no operands following opcode)
                tempWord = Util.subtractWords(ip, new byte[] { 0x00,
                        (byte) (prefixCounter + 1) }, 0);
                System.arraycopy(tempWord, 0, ip, 0, tempWord.length);
            }
        } else {
            // Normal operation (no REP prefix)
            // Execute current instruction
            singleByteInstructions[codeByte].execute();
        }
    }

    /**
     * Retrieves a single byte from the code memory segment; updates the ip by
     * one Note: use this method only when next
     * instruction/addressbyte/immediate is needed!
     * 
     * @return byte at memory address CS:IP
     */
    protected byte getByteFromCode() {
        // Get code memory address
        segmentedCodeAddress = this.getSegmentedCodeAddress();

        // Increment instruction pointer
        ip = Util.addWords(ip, new byte[] { 0x00, 0x01 }, 0);

        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            return memory.getByte(segmentedCodeAddress);
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Retrieves a byte from the code memory segment at given displacement.
     * Note: when working with addressbytes, do not use this method, use
     * getByteFromMemorySegment instead Note: does not update IP
     * 
     * @param displacement
     *            Two byte offset in the current segment
     * @return byte at memory address CS:DISP
     */
    protected byte getByteFromCode(byte[] displacement) {
        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            return memory.getByte(this.getSegmentedCodeAddress(displacement));
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Retrieves a byte from the data memory segment. Note: when working with
     * addressbytes, do not use this method, use getByteFromMemorySegment
     * instead
     * 
     * @param displacement
     *            Two byte offset in the current data segment
     * @return byte at memory address DS:DISP
     */
    protected byte getByteFromData(byte[] displacement) {
        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            return memory.getByte(this.getSegmentedDataAddress(displacement));
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Retrieves a single byte from the stack memory segment. Note: when working
     * with addressbytes, do not use this method, use getByteFromMemorySegment
     * instead
     * 
     * @param displacement
     *            Two byte offset in the current stack segment
     * @return byte at memory address SS:DISP
     */
    protected byte getByteFromStack(byte[] displacement) {
        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            return memory.getByte(this.getSegmentedStackAddress(displacement));
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Retrieves a single byte from the extra memory segment. Note: when working
     * with addressbytes, do not use this method, use getByteFromMemorySegment
     * instead
     * 
     * @param displacement
     * @return byte at memory address ES:DI
     */
    protected byte getByteFromExtra(byte[] displacement) {
        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            return memory.getByte(this.getSegmentedExtraAddress(displacement));
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Retrieves a word from the code memory segment; updates the ip by two
     * Note: use this method only when next instruction/addressbyte/immediate is
     * needed!
     * 
     * @return byte[] with word at memory address CS:IP
     */
    protected byte[] getWordFromCode() {
        // TODO: Check correct storage of bytes in array, [0][1] = [MSB][LSB]???
        segmentedCodeAddress = this.getSegmentedCodeAddress();

        // Increment instruction pointer twice
        ip = Util.addWords(ip, new byte[] { 0x00, 0x02 }, 0);

        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            return memory.getWord(segmentedCodeAddress);
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a word from the code memory segment at given displacement Note:
     * does not update IP
     * 
     * @param displacement
     * @return byte[] with word at memory address displacement
     */
    protected byte[] getWordFromCode(byte[] displacement) {
        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            return memory.getWord(this.getSegmentedCodeAddress(displacement));
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a word from the data memory segment. Note: when working with
     * addressbytes, do not use this method, use getWordFromMemorySegment
     * instead
     * 
     * @param displacement
     *            Two byte offset in the current data segment
     * @return word at memory address DS:DISP
     */
    protected byte[] getWordFromData(byte[] displacement) {
        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            return memory.getWord(this.getSegmentedDataAddress(displacement));
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a word from the stack; increments stack pointer SP by two.
     * Note: when working with addressbytes, do not use this method, use
     * getWordFromMemorySegment instead
     * 
     * @return word at memory address SS:SP
     */
    protected byte[] getWordFromStack() {
        // Need to retrieve data before incrementing SP
        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            byte[] word = memory.getWord(this.getSegmentedStackAddress());

            sp = Util.addWords(sp, new byte[] { 0x00, 0x02 }, 0);

            return word;
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a word from the stack segment. Note: when working with
     * addressbytes, do not use this method, use getWordFromMemorySegment
     * instead
     * 
     * @param displacement
     *            Two byte offset in the current stack segment
     * @return word at memory address SS:DISP
     */
    protected byte[] getWordFromStack(byte[] displacement) {
        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            return memory.getWord(this.getSegmentedStackAddress(displacement));
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a word from extra memory segment. Note: when working with
     * addressbytes, do not use this method, use getWordFromMemorySegment
     * instead
     * 
     * @param displacement
     *            Two byte offset in the current data segment
     * @return word at memory address ES:DISP
     */
    protected byte[] getWordFromExtra(byte[] displacement) {
        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            return memory.getWord(this.getSegmentedExtraAddress(displacement));
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a byte from segment memory DS or SS, which is determined from
     * the addressbyte
     * 
     * @param addressByte
     *            byte whose R/M bits indicate which segment register is to be
     *            used
     * @param offset
     *            byte[] indicating displacement of word in segment
     * 
     * @return byte at memory address SS:disp or DS:disp
     */
    protected byte getByteFromMemorySegment(byte addressByte, byte[] offset) {
        // "Default segment register is SS for effective addresses containing a
        // BP index,
        // DS for other effective addresses" -Intel IA_SDM2, p. 2-5
        // Check whether effective address contains a BP index

        // Computer temporary byte to optimize performance
        tempByte = addressByte & 0x07;

        // Check if segment is overriden
        if (segmentOverride == true) {
            // Segment is overriden, check which one
            switch (segmentOverridePointer) {
            case 0: // SEG=CS
                return getByteFromCode(offset);
            case 1: // SEG=DS
                return getByteFromData(offset);
            case 2: // SEG=ES
                return getByteFromExtra(offset);
            case 3: // SEG=SS
                return getByteFromStack(offset);
            default:
                logger
                        .log(
                                Level.WARNING,
                                "["
                                        + super.getType()
                                        + "] Read byte: segment overriden with unknown segment");
            }
            return getByteFromCode(offset);
        }
        // No segment is overridden
        else if (tempByte == 2 || tempByte == 3 || tempByte == 6) {
            // ...but there's always a bloody exception: Mod == 00b, R/M == 110b
            // does not contain BP
            if (tempByte == 6 && ((addressByte & 0xC0) >> 6) == 0) {
                // Need to address the DS register
                return getByteFromData(offset);
            } else {
                return getByteFromStack(offset);
            }
        } else {
            return getByteFromData(offset);
        }
    }

    /**
     * Retrieves a word from segment memory DS or SS, which is determined from
     * the addressbyte
     * 
     * @param addressByte
     *            byte whose R/M bits indicate which segment register is to be
     *            used
     * @param offset
     *            byte[] indicating offset of word in segment
     * @return word at memory address SS:disp or DS:disp
     */
    protected byte[] getWordFromMemorySegment(byte addressByte, byte[] offset) {
        // "Default segment register is SS for effective addresses containing a
        // BP index,
        // DS for other effective addresses" -Intel IA_SDM2, p. 2-5
        // Check whether effective address contains a BP index

        // Computer temporary byte to optimize performance
        tempByte = addressByte & 0x07;

        // Check if segment is overriden
        if (segmentOverride == true) {
            // Segment is overriden, check which one
            switch (segmentOverridePointer) {
            case 0: // SEG=CS
                return getWordFromCode(offset);
            case 1: // SEG=DS
                return getWordFromData(offset);
            case 2: // SEG=ES
                return getWordFromExtra(offset);
            case 3: // SEG=SS
                return getWordFromStack(offset);
            default:
                logger
                        .log(
                                Level.WARNING,
                                "["
                                        + super.getType()
                                        + "] Read word: segment overriden with unknown segment");
            }
            return getWordFromCode(offset);
        }
        // No segment is overridden
        else if (tempByte == 2 || tempByte == 3 || tempByte == 6) {
            // ...but there's always a bloody exception: Mod == 00b, R/M == 110b
            // does not contain BP
            if (tempByte == 6 && ((addressByte & 0xC0) >> 6) == 0) {
                // Need to address the DS register
                return getWordFromData(offset);
            } else {
                return getWordFromStack(offset);
            }
        } else {
            return getWordFromData(offset);
        }
    }

    /**
     * Assigns a byte in the code memory segment (CS:displacement) a new value
     * Note: this method should be avoided because it does not take care of R/M
     * byte differences Instead, use setByteToMemorySegment() Note: does not
     * update IP
     * 
     * @param displacement
     *            Two byte offset in the current code segment
     * @param value
     *            New value of the byte
     */
    protected void setByteToCode(byte[] displacement, byte value) {
        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            memory.setByte(this.getSegmentedCodeAddress(displacement), value);
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
    }

    /**
     * Assigns a word in the code memory segment (CS:displacement) a new value
     * Note: do not use this method directly, use setWordToMemorySegment instead
     * Note: does not update IP
     * 
     * @param displacement
     *            Two byte offset in the current code segment
     * @param value
     *            New value of the word
     */
    protected void setWordToCode(byte[] displacement, byte[] value) {
        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            memory.setWord(this.getSegmentedCodeAddress(displacement), value);
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
    }

    /**
     * Assigns a byte in the data memory segment (DS:displacement) a new value
     * Note: this method should be avoided because it does not take care of R/M
     * byte differences Instead, use setByteToMemorySegment()
     * 
     * @param displacement
     *            Two byte offset in the current data segment
     * @param value
     *            New value of the byte
     */
    protected void setByteToData(byte[] displacement, byte value) {
        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            memory.setByte(this.getSegmentedDataAddress(displacement), value);
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
    }

    /**
     * Assigns a word in the data memory segment (DS:displacement) a new value
     * Note: do not use this method, use setWordToMemorySegment instead
     * 
     * @param displacement
     *            Two byte offset in the current data segment
     * @param value
     *            New value of the word
     */
    protected void setWordToData(byte[] displacement, byte[] value) {
        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            memory.setWord(this.getSegmentedDataAddress(displacement), value);
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
    }

    /**
     * Sets byte in memory at ES:DI in the extra memory segment
     * @param displacement
     * @param value
     */
    protected void setByteToExtra(byte[] displacement, byte value) {
        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            memory.setByte(this.getSegmentedExtraAddress(displacement), value);
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
    }

    /**
     * Sets word in memory at ES:DI in the extra memory segment
     * @param displacement
     * @param word
     *            Value of word to assign
     */
    protected void setWordToExtra(byte[] displacement, byte[] word) {
        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            memory.setWord(this.getSegmentedExtraAddress(displacement), word);
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
    }

    /**
     * Pushes a byte on the stack memory segment (SS:SP) Note: do not use this
     * method, use setWordToMemorySegment instead
     * 
     * @param value to be pushed on the stack
     */
    protected void setByteToStack(byte value) {
        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            memory.setByte(this.getSegmentedStackAddress(), value);
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
    }

    /**
     * Pushes a byte on the stack memory segment Note: do not use this method,
     * use setByteToMemorySegment instead
     * 
     * @param displacement
     * @param value to be pushed onto the stack
     */
    protected void setByteToStack(byte[] displacement, byte value) {
        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            memory.setByte(this.getSegmentedStackAddress(displacement), value);
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
    }

    /**
     * Pushes a word on the stack memory segment (SS:SP); decrements the stack
     * pointer SP by two. Note: do not use this method, use
     * setWordToMemorySegment instead
     * 
     * @param value word to be pushed on the stack
     */
    protected void setWordToStack(byte[] value) {
        // According to MS-DOS debug.exe, the stack pointer is decrement FIRST
        // before writing data. This behaviour is reproduced here.

        sp = Util.subtractWords(sp, new byte[] { 0x00, 0x02 }, 0);

        // Write data to stack
        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            memory.setWord(this.getSegmentedStackAddress(), value);
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
    }

    /**
     * Sets a word in SS segment Note: when using addressbytes, do not use this
     * method, use setWordToMemorySegment instead
     * 
     * @param displacement Displacement within SS segment
     * @param value word to be stored in SS
     * 
     */
    protected void setWordToStack(byte[] displacement, byte[] value) {
        // Write data to SS
        try {
            ModuleMemory memory = (ModuleMemory)super.getConnection(Module.Type.MEMORY);
            memory.setWord(this.getSegmentedStackAddress(displacement), value);
        } catch (ModuleException e) {
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] AbstractModule exception: " + e.getMessage());
        }
    }

    /**
     * Sets a byte in segment memory DS or SS, which is determined from the
     * addressbyte
     * 
     * @param addressByte
     *            byte whose R/M bits indicate which segment register is to be
     *            used
     * @param disp
     *            byte[] indicating displacement of word in segment
     * @param value
     *            byte containing value to write to segment:disp
     */
    protected void setByteInMemorySegment(byte addressByte, byte[] disp,
            byte value) {
        // "Default segment register is SS for effective addresses containing a
        // BP index,
        // DS for other effective addresses" -Intel IA_SDM2, p. 2-5
        // Check whether effective address contains a BP index

        // Computer temporary byte to optimize performance
        tempByte = addressByte & 0x07;

        // Check if segment is overriden
        if (segmentOverride == true) {
            // Segment is overriden, check which one
            switch (segmentOverridePointer) {
            case 0: // SEG=CS
            // logger.log(Level.INFO, "[" + super.getType() + "]" +
            // " Writing to segment CS (may be dangerous!) [" +
            // instructionCounter + "]");
                setByteToCode(disp, value);
                break;
            case 1: // SEG=DS
                setByteToData(disp, value);
                break;
            case 2: // SEG=ES
                setByteToExtra(disp, value);
                break;
            case 3: // SEG=SS
                setByteToStack(disp, value);
                break;
            default:
                logger
                        .log(
                                Level.WARNING,
                                "["
                                        + super.getType()
                                        + "] Write byte: segment overriden with unknown segment");
            }
        }
        // No segment is overridden
        else if (tempByte == 2 || tempByte == 3 || tempByte == 6) {
            // ...but there's always a bloody exception: Mod == 00b, R/M == 110b
            // does not contain BP
            if (tempByte == 6 && ((addressByte & 0xC0) >> 6) == 0) {
                // Need to address the DS register
                setByteToData(disp, value);
            } else {
                setByteToStack(disp, value);
            }
        } else {
            setByteToData(disp, value);
        }
    }

    /**
     * Sets a word in segment memory DS or SS, which is determined from the
     * addressbyte
     * 
     * @param addressByte
     *            byte whose R/M bits indicate which segment register is to be
     *            used
     * @param disp
     *            byte[] indicating displacement of word in segment
     * @param value
     *            byte[] containing value to write to segment:disp
     */
    protected void setWordInMemorySegment(byte addressByte, byte[] disp,
            byte[] value) {
        // "Default segment register is SS for effective addresses containing a
        // BP index,
        // DS for other effective addresses" -Intel IA_SDM2, p. 2-5
        // Check whether effective address contains a BP index

        // Computer temporary byte to optimize performance
        tempByte = addressByte & 0x07;

        // Check if segment is overriden
        if (segmentOverride == true) {
            // Segment is overriden, check which one
            switch (segmentOverridePointer) {
            case 0: // SEG=CS
            // logger.log(Level.INFO, "[" + super.getType() +
            // "] Writing to segment CS (may be dangerous!) [" +
            // instructionCounter + "]");
                setWordToCode(disp, value);
                break;
            case 1: // SEG=DS
                setWordToData(disp, value);
                break;
            case 2: // SEG=ES
                setWordToExtra(disp, value);
                break;
            case 3: // SEG=SS
                setWordToStack(disp, value);
                break;
            default:
                logger
                        .log(
                                Level.WARNING,
                                "["
                                        + super.getType()
                                        + "] Write word: segment overriden with unknown segment");
            }
        }
        // No segment is overridden
        else if (tempByte == 2 || tempByte == 3 || tempByte == 6) {
            // ...but there's always a bloody exception: Mod == 00b, R/M == 110b
            // does not contain BP
            if (tempByte == 6 && ((addressByte >> 6) & 0x03) == 0) {
                // Need to address the DS register
                setWordToData(disp, value);
            } else {
                setWordToStack(disp, value);
            }
        } else {
            setWordToData(disp, value);
        }
    }

    /**
     * Using mm bits from addressbyte, retrieve value of displacement (if sss
     * indicates a memory destination).<BR>
     * 
     * @param addrByte
     *            addressbyte following opcode instruction
     * 
     * @return memory reference displacement indicated by extra bytes.
     */
    protected byte[] decodeMM(int addrByte) {
        // Determine sss specified by mm bits, by ANDing with 1100 0000 and
        // right-shifting 6
        switch ((addrByte >> 6) & 0x03) {
        case 0:
            // sss is a direct memory location with no offset
            // Check for sss == 110 exception, and retrieve 2 byte offset here
            if ((addrByte & 0x07) == 6) {
                return getWordFromCode();
            }
            return new byte[2];

        case 1:
            // sss is a memory location plus 8 bit (1 byte) offset
            // The Intel specs state 8-bit displacement is sign-extended before
            // added to index
            byte displacement = getByteFromCode();
            return new byte[] { Util.signExtend(displacement), displacement };

        case 2:
            // sss is a memory location plus 16 bit (2 byte) offset
            return getWordFromCode();

        case 3:
            // sss is a register, no offset
            return new byte[2];

        default:
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] Addressbyte MM-bits do not match");
            return new byte[2];
        }
    }

    /**
     * Determine memory destination based on sss bits from addressbyte.<BR>
     * 
     * @param addrByte
     *            addressbyte following opcode instruction
     * @param displacement
     *            memory reference displacement from extra bytes
     * 
     * @return memory location indication by sss and displacement
     */
    protected byte[] decodeSSSMemDest(byte addrByte, byte[] displacement) {
        // Determine memory specified by sss bits, by ANDing with 0000 0111
        switch (addrByte & 0x07) {
        case 0: // BX + SI
            return Util.addRegRegDisp(bx, si, displacement);

        case 1: // BX + DI
            return Util.addRegRegDisp(bx, di, displacement);

        case 2: // BP + SI
            return Util.addRegRegDisp(bp, si, displacement);

        case 3: // BP + DI
            return Util.addRegRegDisp(bp, di, displacement);

        case 4: // SI
            return Util.addRegRegDisp(si, new byte[2], displacement);

        case 5: // DI
            return Util.addRegRegDisp(di, new byte[2], displacement);

        case 6: // 2 byte displacement, or BP + displacement depending on mm
            if (((addrByte & 0xC0) >> 6) == 0) {
                // Direct memory reference, so only 2 byte displacement
                return displacement;
            }
            return Util.addRegRegDisp(bp, new byte[2], displacement);

        case 7: // BX
            return Util.addRegRegDisp(bx, new byte[2], displacement);

        default:
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] Addressbyte SSS-bits do not match");
            return new byte[] { 0, 0 };
        }
    }

    /**
     * Determine register based on rrr/sss bits from addressbyte.<BR>
     * Complete register is returned to ensure pass by reference.<BR>
     * 
     * @param operandWord
     *            boolean indicating byte (false) or word (true) size
     * @param rrrsssBits
     *            integer value (0 - 7) based on rrr/sss bits from addressbyte
     * 
     * @return register indicated by relevant bits from addressbyte
     */
    protected byte[] decodeRegister(boolean operandWord, int rrrsssBits) {
        // Check operand size first
        if (operandWord) {
            // Determine register specified by rrr/sss bits
            switch (rrrsssBits) {
            case 0: // AX
                return ax;
            case 1: // CX
                return cx;
            case 2: // DX
                return dx;
            case 3: // BX
                return bx;
            case 4: // SP
                return sp;
            case 5: // BP
                return bp;
            case 6: // SI
                return si;
            case 7: // DI
                return di;

            default:
                logger.log(Level.WARNING, "[" + super.getType()
                        + "] Addressbyte RRR/SSS-bits do not match");
                return new byte[2];
            }
        } else
        // Dealing with byte-sized operand
        {
            // Determine register specified by rrr/sss bits
            switch (rrrsssBits) {
            case 0: // AL
                return ax;
            case 1: // CL
                return cx;
            case 2: // DL
                return dx;
            case 3: // BL
                return bx;
            case 4: // AH
                return ax;
            case 5: // CH
                return cx;
            case 6: // DH
                return dx;
            case 7: // BH
                return bx;

            default:
                logger.log(Level.WARNING, "[" + super.getType()
                        + "] Addressbyte RRR/SSS-bits do not match");
                return new byte[2];
            }
        }
    }

    /**
     * Determine extra register based on rrr/sss bits from addressbyte.<BR>
     * Complete register is returned to ensure pass by reference.<BR>
     * 
     * @param rrrsssBits
     *            integer value (0 - 7) based on rrr/sss bits from addressbyte
     * 
     * @return register indicated by relevant bits from addressbyte
     */
    protected byte[] decodeExtraRegister(int rrrsssBits) {
        // Determine register specified by rrr/sss bits
        switch (rrrsssBits) {
        case 0: // eAX
            return eax;
        case 1: // eCX
            return ecx;
        case 2: // eDX
            return edx;
        case 3: // eBX
            return ebx;
        case 4: // eSP
            return esp;
        case 5: // eBP
            return ebp;
        case 6: // eSI
            return esi;
        case 7: // eDI
            return edi;

        default:
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] Addressbyte RRR/SSS-bits do not match");
            return new byte[2];
        }
    }

    /**
     * Determine segment register based on rrr/sss bits from addressbyte.<BR>
     * Complete segment register is returned to ensure pass by reference.<BR>
     * 
     * @param rrrsssBits
     *            integer value (0 - 7) based on rrr/sss bits from addressbyte
     * @return segment register indicated by relevant bits from addressbyte
     */
    protected byte[] decodeSegmentRegister(int rrrsssBits) {
        // Not dependant on operand size like decodeRegister, so check is
        // skipped
        // Determine register specified by rrr/sss bits
        switch (rrrsssBits) {
        case 0: // ES
            return es;
        case 1: // CS
            return cs;
        case 2: // SS
            return ss;
        case 3: // DS
            return ds;
        case 4: // FS (nonexistant in 186)
            return new byte[2]; // TODO: add FS register to CPU
        case 5: // GS (nonexistant in 186)
            return new byte[2]; // TODO: add GS register to CPU
        case 6: // reserved
            return new byte[2];
        case 7: // reserved
            return new byte[2];

        default:
            logger.log(Level.WARNING, "[" + super.getType()
                    + "] Addressbyte RRR/SSS-bits do not match");
            return new byte[2];
        }
    }

    /**
     * Calculates the flat-memory address for the code segment
     * 
     * @return flat-memory address of CS:IP
     */
    private int getSegmentedCodeAddress() {
        // Return segmented memory address for code segment: 4x left-shift CS
        // register, add IP
        // 
        // Using CS arrays, this involves an 12-bit left-shift for the high
        // register (8-bit shift to assign normal value, 4 extra for
        // segmentation),
        // a 4-bit left-shift for the low register (4-bit shift for
        // segmentation)
        return (((cs[REGISTER_SEGMENT_HIGH] & 0xFF) << 12)
                + ((cs[REGISTER_SEGMENT_LOW] & 0xFF) << 4)
                + ((ip[REGISTER_SEGMENT_HIGH] & 0xFF) << 8) + ((ip[REGISTER_SEGMENT_LOW] & 0xFF)));
    }

    /**
     * Calculates the flat-memory address for the code segment
     * 
     * @param displacement
     *            Offset in current segment
     * 
     * @return flat-memory address of CS:displacement
     */
    private int getSegmentedCodeAddress(byte[] displacement) {
        // Return segmented memory address for code segment: 4x left-shift
        // codesegment register, add offset
        return (((cs[REGISTER_SEGMENT_HIGH] & 0xFF) << 12)
                + ((cs[REGISTER_SEGMENT_LOW] & 0xFF) << 4)
                + ((displacement[REGISTER_SEGMENT_HIGH] & 0xFF) << 8) + (displacement[REGISTER_SEGMENT_LOW] & 0xFF));
    }

    /**
     * Calculates the flat-memory address for the data segment
     * 
     * @param displacement
     *            Offset in current data segment
     * 
     * @return flat-memory address of DS:displacement
     */
    private int getSegmentedDataAddress(byte[] displacement) {
        // Return segmented memory address for data segment: 4x left-shift
        // datasegment register, add offset
        return (((ds[REGISTER_SEGMENT_HIGH] & 0xFF) << 12)
                + ((ds[REGISTER_SEGMENT_LOW] & 0xFF) << 4)
                + ((displacement[REGISTER_SEGMENT_HIGH] & 0xFF) << 8) + (displacement[REGISTER_SEGMENT_LOW] & 0xFF));
    }

    /**
     * Calculates the flat-memory address for the stack segment
     * 
     * @return flat-memory address of SS:SP
     */
    private int getSegmentedStackAddress() {
        // Return segmented memory address for stack segment: 4x left-shift
        // stacksegment register, add stackpointer
        return (((ss[REGISTER_SEGMENT_HIGH] & 0xFF) << 12)
                + ((ss[REGISTER_SEGMENT_LOW] & 0xFF) << 4)
                + ((sp[REGISTER_SEGMENT_HIGH] & 0xFF) << 8) + (sp[REGISTER_SEGMENT_LOW] & 0xFF));
    }

    /**
     * Calculates the flat-memory address for the stack segment
     * 
     * @param displacement
     *            Offset in current stack segment
     * 
     * @return flat-memory address of SS:displacement
     */
    private int getSegmentedStackAddress(byte[] displacement) {
        // Return segmented memory address for data segment: 4x left-shift
        // datasegment register, add offset
        return (((ss[REGISTER_SEGMENT_HIGH] & 0xFF) << 12)
                + ((ss[REGISTER_SEGMENT_LOW] & 0xFF) << 4)
                + ((displacement[REGISTER_SEGMENT_HIGH] & 0xFF) << 8) + (displacement[REGISTER_SEGMENT_LOW] & 0xFF));
    }

    /**
     * Calculates the flat-memory address for the data segment
     * 
     * @param displacement
     *            Offset in current data segment
     * @return flat-memory address of ES:displacement
     */
    private int getSegmentedExtraAddress(byte[] displacement) {
        // Return segmented memory address for data segment: 4x left-shift
        // datasegment register, add offset
        return (((es[REGISTER_SEGMENT_HIGH] & 0xFF) << 12)
                + ((es[REGISTER_SEGMENT_LOW] & 0xFF) << 4)
                + ((displacement[REGISTER_SEGMENT_HIGH] & 0xFF) << 8) + (displacement[REGISTER_SEGMENT_LOW] & 0xFF));
    }

    /**
     * Converts a given string (registername) into a register reference. This
     * method is not case sensitive.
     * 
     * @return byte[] containing the requested register
     */
    private byte[] convertStringToRegister(String registerName) {
        if (registerName.equalsIgnoreCase("AX")) {
            return ax;
        } else if (registerName.equalsIgnoreCase("BX")) {
            return bx;
        } else if (registerName.equalsIgnoreCase("CX")) {
            return cx;
        } else if (registerName.equalsIgnoreCase("DX")) {
            return dx;
        } else if (registerName.equalsIgnoreCase("SP")) {
            return sp;
        } else if (registerName.equalsIgnoreCase("BP")) {
            return bp;
        } else if (registerName.equalsIgnoreCase("SI")) {
            return si;
        } else if (registerName.equalsIgnoreCase("DI")) {
            return di;
        } else if (registerName.equalsIgnoreCase("CS")) {
            return cs;
        } else if (registerName.equalsIgnoreCase("DS")) {
            return ds;
        } else if (registerName.equalsIgnoreCase("SS")) {
            return ss;
        } else if (registerName.equalsIgnoreCase("ES")) {
            return es;
        } else if (registerName.equalsIgnoreCase("IP")) {
            return ip;
        }
        return null;
    }

    /**
     * Handle interrupt Request (IRQ)
     * 
     */
    private void handleIRQ(int vector) {
        int offset;
        byte[] tempCS, tempIP, newCS, newIP;

        tempCS = new byte[2];
        tempIP = new byte[2];

        // Push flags register (16-bit) onto stack
        this.setWordToStack(Util.booleansToBytes(flags));

        // Push CS and IP onto stack
        this.setWordToStack(cs);
        this.setWordToStack(ip);

        // Push all other registers onto stack
        // Clear flags IF, TF, (and Alignment Check AC, but is not implemented
        // on 16-bit)
        flags[CPU.REGISTER_FLAGS_IF] = false;
        flags[CPU.REGISTER_FLAGS_TF] = false;

        // Retrieve the interrupt vector (IP:CS) from the IDT, based on the
        // index
        // Reset the CS and IP to interrupt vector in IDT
        cs[CPU.REGISTER_SEGMENT_LOW] = tempCS[CPU.REGISTER_SEGMENT_LOW] = 0x00; // refer
                                                                                // to
                                                                                // beginning
                                                                                // of
                                                                                // code
                                                                                // segment
        cs[CPU.REGISTER_SEGMENT_HIGH] = tempCS[CPU.REGISTER_SEGMENT_HIGH] = 0x00;

        offset = vector * 4; // define offset from code segment (index * 4
                             // bytes); remember to take in account intOffset
        // logger.log(Level.INFO, "[" + super.getType() + "] " + "IRQ raised: 0x" +
        // Integer.toHexString(vector).toUpperCase());

        ip[CPU.REGISTER_LOW] = tempIP[CPU.REGISTER_SEGMENT_LOW] = (byte) (offset & 0xFF);
        ip[CPU.REGISTER_HIGH] = tempIP[CPU.REGISTER_SEGMENT_HIGH] = (byte) ((offset >> 8) & 0xFF);

        // Fetch IP and CS
        newIP = this.getWordFromCode();
        newCS = this.getWordFromCode();

        // Assign new CS and IP to registers pointing to interrupt procedure
        cs[CPU.REGISTER_SEGMENT_LOW] = newCS[CPU.REGISTER_LOW];
        cs[CPU.REGISTER_SEGMENT_HIGH] = newCS[CPU.REGISTER_HIGH];
        ip[CPU.REGISTER_LOW] = newIP[CPU.REGISTER_LOW];
        ip[CPU.REGISTER_HIGH] = newIP[CPU.REGISTER_HIGH];

        // Clear interrupt flag
        flags[REGISTER_FLAGS_IF] = false;

        logger.log(Level.CONFIG, "[" + super.getType()
                + "] Handling IRQ: IDT at IP=0x"
                + Util.convertWordToString(tempIP) + ", CS=0x"
                + Util.convertWordToString(tempCS) + " and ISR at IP=0x"
                + Util.convertWordToString(newIP) + ", CS=0x"
                + Util.convertWordToString(newCS));

    }

    /**
     * Converts a given word into an integer
     * 
     * @param word
     * 
     * @return int
     */
    private int convertWordToInt(byte[] word) {
        int result;

        // Cast both bytes to integer, mask and add
        result = (((int) word[1]) & 0xFF);
        result += ((((int) word[0]) << 8) & 0xFF00);

        return result;
    }

    /**
     *
     * @param register
     * @return -
     */
    public String getRegisterHex(int register) {
        switch (register) {
        case 0: // CS
            return Integer.toHexString(
                    ((((int) cs[REGISTER_GENERAL_HIGH]) & 0xFF) << 8)
                            + (((int) cs[REGISTER_GENERAL_LOW]) & 0xFF))
                    .toUpperCase();
        case 1: // IP
            return Integer.toHexString(
                    ((((int) ip[REGISTER_GENERAL_HIGH]) & 0xFF) << 8)
                            + (((int) ip[REGISTER_GENERAL_LOW]) & 0xFF))
                    .toUpperCase();

        default:
            return "NULL";
        }
    }

    /**
     * Get CPU instruction debug.
     * 
     * @return cpuInstructionDebug.
     */
    public boolean getCpuInstructionDebug() {
        return this.cpuInstructionDebug;
    }

    /**
     * Set the CPU instruction debug.
     * 
     * @param cpuInstructionDebug status of instructionDebug (on/off)
     */
    public void setCpuInstructionDebug(boolean cpuInstructionDebug) {
        this.cpuInstructionDebug = cpuInstructionDebug;
    }

    /**
     * 
     * @return -
     */
    public String startDebug() {
        StringBuilder b = new StringBuilder();
        b.append("***************************\nBEFORE\n").append(registerDump());
        this.start();
        b.append("***************************\nAFTER\n").append(registerDump());
        return b.toString()+"***************************";
    }

    /**
     *
     * @return -
     */
    public String registerDump() {
        StringBuilder b = new StringBuilder();
        b.append("ax="+toHexString(ax)).append('\n');
        b.append("eax="+toHexString(eax)).append('\n');
        b.append("bx="+toHexString(bx)).append('\n');
        b.append("ebx="+toHexString(ebx)).append('\n');
        b.append("cx="+toHexString(cx)).append('\n');
        b.append("ecx="+toHexString(ecx)).append('\n');
        b.append("dx="+toHexString(dx)).append('\n');
        b.append("edx="+toHexString(edx)).append('\n');
        b.append("sp="+toHexString(sp)).append('\n');
        b.append("esp="+toHexString(esp)).append('\n');
        b.append("bp="+toHexString(bp)).append('\n');
        b.append("ebp="+toHexString(ebp)).append('\n');
        b.append("si="+toHexString(si)).append('\n');
        b.append("esi="+toHexString(esi)).append('\n');
        b.append("di="+toHexString(di)).append('\n');
        b.append("edi="+toHexString(edi)).append('\n');
        b.append("cs="+toHexString(cs)).append('\n');
        b.append("ds="+toHexString(ds)).append('\n');
        b.append("ss="+toHexString(ss)).append('\n');
        b.append("es="+toHexString(es)).append('\n');
        b.append("ip="+toHexString(ip)).append('\n');
        b.append("oldIP="+toHexString(oldIP)).append('\n');
        b.append("flags="+toHexString(flags)).append('\n');
        b.append("cr0="+toHexString(cr0)).append('\n');
        b.append("cr1="+toHexString(cr1)).append('\n');
        b.append("cr2="+toHexString(cr2)).append('\n');
        b.append("cr3="+toHexString(cr3)).append('\n');
        b.append("cr4="+toHexString(cr4)).append('\n');
        b.append("gdtr="+toHexString(gdtr)).append('\n');
        b.append("idtr="+toHexString(idtr)).append('\n');
        b.append("ldtr="+toHexString(ldtr)).append('\n');
        return b.toString();
    }

    private String toHexString(byte[] values) {
        if(values == null) return "<<NULL>>";
        if(values.length == 0) return "[]";
        StringBuilder b = new StringBuilder("["+byteToHex(values[0]));
        for(int i = 1; i < values.length; i++) {
            b.append(", "+byteToHex(values[i]));
        }
        return b.toString()+"]";
    }

    private String toHexString(boolean[] values) {
        if(values == null) return "<<NULL>>";
        if(values.length == 0) return "[]";
        StringBuilder b = new StringBuilder("["+(values[0] ? "1" : "0"));
        for(int i = 1; i < values.length; i++) {
            b.append(", "+(values[i] ? "1" : "0"));
        }
        return b.toString()+"]";
    }

    private String byteToHex(byte b) {
        String intHex = Integer.toHexString(b);
        if(intHex.length() == 1) {
            intHex = "0"+intHex;
        } else {
            intHex = intHex.substring(intHex.length()-2);
        }
        return "0x"+intHex;
    }
}
