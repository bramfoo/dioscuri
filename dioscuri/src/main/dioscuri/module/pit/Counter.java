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

package dioscuri.module.pit;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A single counter of the PIT based on the Intel 82C54 chipset.
 * 
 * This counter works following the convention rules of the PIT: 1. For each
 * counter, the control word must be written before the initial count is
 * written. 2. The initial count must follow the count format specified in the
 * Control Word (LSB, MSB, etc.)
 * 
 * 
 */
@SuppressWarnings("unused")
public class Counter {
    // Attributes
    private PIT pit; // The owner of the counter
    private int counterNumber; // The number of the counter (ID)

    // Signals
    private boolean signalClock; // Input signal CLK from the clock
    private boolean signalGate; // Input signal GATE from other module
    private boolean signalOut; // Output signal OUT

    // Mode settings
    protected int counterMode; // States the counter mode: 0,1,2,3,4,5
    protected int rwMode; // States the mode to R/W counter: 0 (latched), 1
                          // (LSB), 2 (MSB), 3 (LSB -> MSB)
    protected boolean bcd; // false = Binary counter, true = Binary Code Decimal
                           // counter

    // Registers
    protected byte[] ce; // Counting element, the actual counter
    protected byte[] cr; // Count Register, holding the count value to be loaded
                         // in counter
    protected byte[] ol; // Output Latch, holding a snapshot of the count of the
                         // counter

    // Toggles
    private boolean isEnabled; // Denotes if this counter is enabled or not
    private boolean parity; // Denotes the parity of the count value: false=ODD,
                            // true=EVEN
    private boolean lsbWritten;
    private boolean lsbRead;
    private boolean isLatched;
    private boolean newCount;
    private boolean isTriggered; // Defines if this counter has been triggered
                                 // (used for modes 1 and 5)
    private boolean isGateRising; // Defines if GATE signal is on rising edge
    protected boolean readBackCmd; // Defines if this counter is in read-back
                                   // mode

    // Logging
    private static final Logger logger = Logger.getLogger(Counter.class.getName());

    // Constants
    protected final static int LSB = 1;
    protected final static int MSB = 0;

    private final static boolean ODD = false;
    private final static boolean EVEN = true;

    private final static boolean BINARY = false;
    private final static boolean BCD = true;

    private final static int RWMODE_0 = 0; // Counter Latch Command
    private final static int RWMODE_1 = 1; // R/W LSB only
    private final static int RWMODE_2 = 2; // R/W MSB only
    private final static int RWMODE_3 = 3; // R/W LSB first, then MSB

    private final static int COUNTERMODE_0 = 0; // Interrupt on terminal count
    private final static int COUNTERMODE_1 = 1; // Hardware retriggerable
                                                // one-shot
    private final static int COUNTERMODE_2 = 2; // Period rate generator
    private final static int COUNTERMODE_3 = 3;
    private final static int COUNTERMODE_4 = 4;
    private final static int COUNTERMODE_5 = 5;

    // Constructor
    /**
     * Constructor of the counter class
     * 
     * @param pit
     * @param counterNumber 
     */
    public Counter(PIT pit, int counterNumber) {
        // Initialise relations
        this.pit = pit;
        this.counterNumber = counterNumber;

        // Initialise input/output signals
        signalClock = false;
        signalGate = true;
        signalOut = true;

        // Initialise mode settings
        rwMode = RWMODE_0;
        counterMode = COUNTERMODE_2;
        bcd = BINARY;

        // Initialise internal registers
        ce = new byte[] { 0x00, 0x00 };
        cr = new byte[] { 0x00, 0x00 };
        ol = new byte[] { 0x00, 0x00 };

        // Initialise toggles
        isEnabled = false;
        parity = EVEN;
        lsbWritten = false;
        lsbRead = false;
        isLatched = false;
        newCount = false;
        isTriggered = false;
        isGateRising = false;
    }

    // Methods
    /**
     * Performs counter action on one clockpulse. The action depends on the mode
     * the counter is set to, the GATE signal, R/W mode and BCD setting.
     * 
     */
    public void clockPulse() {
        // Check if this counter is used
        if (isEnabled == true) {
            // Check bcd setting
            if (bcd == BINARY) {
                switch (counterMode) {
                case COUNTERMODE_0:
                    // Mode 0: Interrupt on terminal count
                    // OUT: initially low, turns high when counter reaches zero.
                    // Stays high until new count or CW is loaded.
                    // GATE: initially high. When turning low, counting is
                    // suspended.

                    // FIXME: maybe rwmode check should be replaced by another
                    // variable.
                    // In case where Control Word is reset but counter
                    // continues, current counter will be
                    // influenced which is not correct!!!

                    // Check if a new count value should be loaded
                    if (newCount == true) {
                        this.loadCounter();
                        newCount = false;

                        // Lower interrupt
                        pit.lowerIRQ(this);
                    } else if (signalGate == true) {
                        // Perform counting

                        // Binary countdown
                        ce[LSB]--;

                        // Check R/W Mode
                        if (rwMode == RWMODE_1) {
                            // LSB only
                            if (ce[LSB] == 0) {
                                // Set OUT to high
                                signalOut = true;

                                // Raise interrupt
                                pit.raiseIRQ(this);
                            }
                        } else if (rwMode == RWMODE_2) {
                            // FIXME: not sure what to do
                            logger
                                    .log(
                                            Level.WARNING,
                                            "["
                                                    + pit.getType()
                                                    + "] counter "
                                                    + counterNumber
                                                    + " countermode0 r/wmode2 not implemented.");
                        } else if (rwMode == RWMODE_3) {
                            // LSB and MSB
                            if (ce[LSB] == -1) // comparing signed bytes,
                                               // checking 0xFF
                            {
                                // LSB passed by zero
                                // MSB must be decremented
                                ce[MSB]--;
                            } else if (ce[LSB] == 0 && ce[MSB] == 0) {
                                // Set OUT to high
                                signalOut = true;

                                // Raise interrupt
                                pit.raiseIRQ(this);
                            }
                        }
                    }
                    break;

                case COUNTERMODE_1:
                    // Mode 1: Hardware retriggerable one-shot
                    // OUT: initially high, turns low after trigger on
                    // clockpulse. Stays low until count reaches zero and
                    // remains high until next trigger.
                    // GATE: initially low. When turning high (trigger), counter
                    // starts counting from the start.

                    // Check if a new count value should be loaded
                    if (isGateRising == true) {
                        // A trigger has been given
                        this.loadCounter();
                        newCount = false;
                        isTriggered = true;
                        signalOut = false;

                        // Lower interrupt
                        pit.lowerIRQ(this);
                    } else if (isTriggered == true) {
                        // Perform counting

                        // Binary countdown
                        ce[LSB]--;

                        // Check R/W Mode
                        if (rwMode == RWMODE_1) {
                            // LSB only
                            if (ce[LSB] == 0) {
                                // Set OUT to high
                                signalOut = true;

                                // Raise interrupt
                                pit.raiseIRQ(this);
                            }
                        } else if (rwMode == RWMODE_2) {
                            // FIXME: not sure what to do
                            logger
                                    .log(
                                            Level.WARNING,
                                            "["
                                                    + pit.getType()
                                                    + "] counter "
                                                    + counterNumber
                                                    + " countermode1 r/wmode2 not implemented.");
                        } else if (rwMode == RWMODE_3) {
                            // LSB and MSB
                            if (ce[LSB] == -1) // comparing signed bytes,
                                               // checking 0xFF
                            {
                                // LSB passed by zero
                                // MSB must be decremented
                                ce[MSB]--;
                            } else if (ce[LSB] == 0 && ce[MSB] == 0) {
                                // Set OUT to high
                                signalOut = true;

                                // Raise interrupt
                                pit.raiseIRQ(this);
                            }
                        }
                    }
                    break;

                case COUNTERMODE_2:
                    // Mode 2: Rate generator (periodic)
                    // OUT: initially high, turns low for one clockcycle when
                    // count has decremented to one and goes high again.
                    // GATE: initially high. When turning low, counting is
                    // suspended. If OUT is low as well, OUT is set to high
                    // immediately

                    // Reset OUT signal to make sure that when it's low, it will
                    // return high again
                    // Even when GATE is turned low, the OUT signal should
                    // become high again!
                    signalOut = true;

                    // Check if a new count value should be loaded
                    if (newCount == true) {
                        this.loadCounter();
                        newCount = false;

                        // Lower interrupt
                        pit.lowerIRQ(this);
                    } else if (signalGate == true) {
                        // Perform counting

                        // Binary countdown
                        ce[LSB]--;

                        // Check R/W Mode
                        if (rwMode == RWMODE_1) {
                            // LSB only
                            if (ce[LSB] == 1) {
                                // Set OUT to low
                                signalOut = false;

                                logger.log(Level.CONFIG, "[" + pit.getType()
                                        + "] counter " + counterNumber
                                        + " countermode2 r/wmode1 expired.");

                                // Raise interrupt
                                pit.raiseIRQ(this);

                                newCount = true;
                            }
                        } else if (rwMode == RWMODE_2) {
                            // FIXME: not sure what to do
                            logger
                                    .log(
                                            Level.WARNING,
                                            "["
                                                    + pit.getType()
                                                    + "] counter "
                                                    + counterNumber
                                                    + " countermode2 r/wmode2 not implemented.");
                        } else if (rwMode == RWMODE_3) {
                            // LSB and MSB
                            if (ce[LSB] == -1) // comparing signed bytes,
                                               // checking 0xFF
                            {
                                // LSB passed by zero
                                // MSB must be decremented
                                ce[MSB]--;
                            } else if (ce[LSB] == 1 && ce[MSB] == 0) {
                                // Set OUT to low
                                signalOut = false;

                                logger.log(Level.CONFIG, "[" + pit.getType()
                                        + "] counter " + counterNumber
                                        + " countermode2 r/wmode3 expired.");
                                // Raise interrupt
                                pit.raiseIRQ(this);

                                newCount = true;
                            }
                        }
                    }
                    break;

                case COUNTERMODE_3:
                    // Mode 3: Square wave mode (periodic)
                    // OUT: initially high, turns low for when count has
                    // decremented to half of its value. Goes high again after
                    // half of the count.
                    // GATE: initially high. When turning low, counting is
                    // suspended. If OUT is low as well, OUT is set to high
                    // immediately

                    // Check if a new count value should be loaded
                    if (newCount == true) {
                        this.loadCounter();

                        // Lower interrupt
                        pit.lowerIRQ(this);

                        // Check parity of count element
                        if (parity == ODD) {
                            // Subtract one more to become even
                            ce[LSB]--;

                            // Check if OUT signal is still high
                            if (signalOut == true) {
                                // OUT signal should be turned low one CLK
                                // signal AFTER ce has become zero!!
                                signalOut = false;
                            }
                        }
                        newCount = false;
                    } else if (signalGate == true) {
                        // Perform counting

                        // Binary countdown
                        ce[LSB]--;
                        ce[LSB]--;

                        // Check R/W Mode
                        if (rwMode == RWMODE_1) {
                            // LSB only
                            if (ce[LSB] == 0) {
                                // Raise interrupt
                                pit.raiseIRQ(this);

                                // Check state of OUT signal
                                if (signalOut == true && parity == EVEN) {
                                    // Set OUT to low
                                    signalOut = false;
                                } else if (signalOut == false) // this behaves
                                                               // the same for
                                                               // ODD and EVEN
                                {
                                    // Set OUT to high
                                    signalOut = true;
                                }

                                // Indicate that counter must start counting
                                // from the start again
                                newCount = true;
                            }
                        } else if (rwMode == RWMODE_2) {
                            // FIXME: not sure what to do
                            logger
                                    .log(
                                            Level.WARNING,
                                            "["
                                                    + pit.getType()
                                                    + "] counter "
                                                    + counterNumber
                                                    + " countermode3 r/wmode2 not implemented.");
                        } else if (rwMode == RWMODE_3) {
                            // LSB and MSB
                            if (ce[LSB] == -2) // comparing signed bytes,
                                               // checking 0xFE
                            {
                                // LSB passed by zero
                                // MSB must be decremented
                                ce[MSB]--;
                            } else if (ce[LSB] == 0 && ce[MSB] == 0) {
                                logger.log(Level.CONFIG, "[" + pit.getType()
                                        + "] counter " + counterNumber
                                        + " countermode3 r/wmode3 expired.");

                                // Raise interrupt
                                pit.raiseIRQ(this);

                                if (parity == EVEN) {
                                    // Check state of OUT signal
                                    if (signalOut == true) {
                                        // Set OUT to low
                                        signalOut = false;

                                    } else if (signalOut == false) {
                                        // Set OUT to high
                                        signalOut = true;
                                    }
                                } else // Parity is ODD
                                {
                                    // Signal OUT should become high again.
                                    // Note: if signal OUT is high, it will be
                                    // set low after the NEXT clock cycle.
                                    if (signalOut == false) {
                                        // Set OUT to high
                                        signalOut = true;
                                    }
                                }

                                // Indicate that counter must start counting
                                // from the start again
                                newCount = true;
                            }

                        }
                    }
                    break;

                case COUNTERMODE_4:
                    // Mode 4: Software triggered strobe
                    // OUT: initially high, turns low for one clockcycle when
                    // count has decremented to zero and goes high again.
                    // GATE: initially high. When turning low, counting is
                    // suspended.

                    // Reset OUT signal to make sure that when it's low, it will
                    // return high again
                    // Even when GATE is turned low, the OUT signal should
                    // become high again!
                    signalOut = true;

                    // Check if a new count value should be loaded
                    if (newCount == true) {
                        this.loadCounter();
                        newCount = false;

                        // Lower interrupt
                        pit.lowerIRQ(this);
                    } else if (signalGate == true) {
                        // Perform counting

                        // Binary countdown
                        ce[LSB]--;

                        // Check R/W Mode
                        if (rwMode == RWMODE_1) {
                            // LSB only
                            if (ce[LSB] == 0) {
                                // Set OUT to low
                                signalOut = false;

                                // Raise interrupt
                                pit.raiseIRQ(this);

                                newCount = true;
                            }
                        } else if (rwMode == RWMODE_2) {
                            // FIXME: not sure what to do
                            logger
                                    .log(
                                            Level.WARNING,
                                            "["
                                                    + pit.getType()
                                                    + "] counter "
                                                    + counterNumber
                                                    + " countermode4 r/wmode2 not implemented.");
                        } else if (rwMode == RWMODE_3) {
                            // LSB and MSB
                            if (ce[LSB] == -1) // comparing signed bytes,
                                               // checking 0xFF
                            {
                                // LSB passed by zero
                                // MSB must be decremented
                                ce[MSB]--;
                            } else if (ce[LSB] == 0 && ce[MSB] == 0) {
                                // Set OUT to low
                                signalOut = false;

                                // Raise interrupt
                                pit.raiseIRQ(this);

                                newCount = true;
                            }
                        }
                    }
                    break;

                case COUNTERMODE_5:
                    // Mode 5: Hardware triggered strobe (retriggerable)
                    // OUT: initially high, turns low one clockcycle when
                    // counting expires. Then turns high again.
                    // GATE: initially low. When turning high (trigger), counter
                    // starts counting from the start.

                    // Reset OUT signal to make sure that when it's low, it will
                    // return high again
                    // Even when GATE is turned low, the OUT signal should
                    // become high again!
                    signalOut = true;

                    // Check if a new count value should be loaded
                    if (isGateRising == true) {
                        // A trigger has been given
                        this.loadCounter();
                        newCount = false;
                        isTriggered = true;

                        // Lower interrupt
                        pit.lowerIRQ(this);
                    } else if (isTriggered == true) {
                        // Perform counting

                        // Binary countdown
                        ce[LSB]--;

                        // Check R/W Mode
                        if (rwMode == RWMODE_1) {
                            // LSB only
                            if (ce[LSB] == 0) {
                                // Set OUT to high
                                signalOut = false;

                                // Raise interrupt
                                pit.raiseIRQ(this);
                            }
                        } else if (rwMode == RWMODE_2) {
                            // FIXME: not sure what to do
                            logger
                                    .log(
                                            Level.WARNING,
                                            "["
                                                    + pit.getType()
                                                    + "] counter "
                                                    + counterNumber
                                                    + " countermode5 r/wmode2 not implemented.");
                        } else if (rwMode == RWMODE_3) {
                            // LSB and MSB
                            if (ce[LSB] == -1) // comparing signed bytes,
                                               // checking 0xFF
                            {
                                // LSB passed by zero
                                // MSB must be decremented
                                ce[MSB]--;
                            } else if (ce[LSB] == 0 && ce[MSB] == 0) {
                                // Set OUT to high
                                signalOut = false;

                                // Raise interrupt
                                pit.raiseIRQ(this);
                            }
                        }
                    }
                    break;

                default:
                    // FIXME: throw exception. No countermode match
                    logger.log(Level.WARNING, "[" + pit.getType()
                            + "] counter " + counterNumber
                            + " no countermode match.");
                }
            } else {
                // BCD countdown
                // FIXME: implement BCD counting
                logger.log(Level.WARNING, "[" + pit.getType() + "] counter "
                        + counterNumber + " BCD counting not implemented.");
            }
        }

        // Reset rising signal
        isGateRising = false;
    }

    /**
     * Retrieves the GATE signal
     * 
     * @return boolean true if GATE is high, false if GATE is low
     */
    protected boolean getGateSignal() {
        // Returns the GATE signal
        return signalGate;
    }

    /**
     * Sets the GATE signal to given status GATE signal is compared with its
     * previous state. If GATE is rising (low -> high), variable isGateRising is
     * set to true If GATE is falling (high -> low), signal OUT is turned high
     * immediately for some counter modes
     * 
     * @param status
     */
    protected void setGateSignal(boolean status) {
        // FIXME: this method is never used!
        // Set the GATE to given status and determine if it is a rising or
        // falling signal
        if (status == true) {
            if (signalGate == false) {
                // Signal is rising
                isGateRising = true;
            } else {
                // Reset rising signal
                isGateRising = false;
            }
        } else {
            // status == false
            if (signalGate == false) {
                // Do nothing -> GATE does not change
            } else {
                // GATE signal is falling
                // Higher OUT signal in particular counter modes
                if ((counterMode == COUNTERMODE_2 || counterMode == COUNTERMODE_3)
                        && signalOut == false) {
                    signalOut = true;

                    // Send notification to user
                    pit.raiseIRQ(this);
                }
            }
        }
        signalGate = status;
    }

    /**
     * Retrieves the OUT signal
     * 
     * @return boolean true if OUT is high, false if OUT is low
     */
    protected boolean getOutSignal() {
        // Returns the OUT signal
        return signalOut;
    }

    /**
     * Returns the counter value depending on the R/W mode
     * 
     * @return byte data containing the LSB/MSB from Counting Element (ce) or
     *         Output latch (ol)
     */
    protected byte getCounterValue() {
        // Check if this counter is in read-back mode
        if (readBackCmd == true) {
            // FIXME: implement appropriate action for read-back command
            readBackCmd = false;
        } else {
            // Check the R/W mode
            if (rwMode == RWMODE_0) {
                // Check if LSB is already read
                if (lsbRead == true) {
                    // Return MSB, reset lsbRead and release latch
                    lsbRead = false;
                    isLatched = false;
                    return ol[MSB];
                } else {
                    // Return LSB, set lsbRead
                    lsbRead = true;
                    return ol[LSB];
                }
            } else if (rwMode == RWMODE_1) {
                // Read operation: LSB only
                return ce[LSB];
            } else if (rwMode == RWMODE_2) {
                // Read operation: MSB only
                return ce[MSB];
            } else if (rwMode == RWMODE_3) {
                // Read operation: LSB first, then MSB
                // Check if this is LSB or MSB byte (first or second byte)
                if (lsbRead == true) {
                    // Return MSB and reset lsbRead
                    lsbRead = false;
                    return ce[MSB];
                } else {
                    // Return LSB and set lsbRead
                    lsbRead = true;
                    return ce[LSB];
                }
            }
        }

        // Else, return default value
        return 0x00;
    }

    /**
     * Set counter value depending on the R/W mode Note: it is assumed that data
     * is always loaded in LSB, MSB order. So, first LSB then MSB.
     * 
     * @param data
     */
    protected void setCounterValue(byte data) {
        // Check the R/W mode
        if (rwMode == RWMODE_0) {
            // Do nothing as data can not be written while in Counter Latch mode
        } else if (rwMode == RWMODE_1) {
            // Write operation: LSB only
            cr[LSB] = data;
            newCount = true;
            // Enable counter
            if (isEnabled) {
                logger
                        .log(
                                Level.CONFIG,
                                "["
                                        + PIT.MODULE_TYPE
                                        + "] Counter is already in use. Resetting may cause timing issues.");
            }
            isEnabled = true;
        } else if (rwMode == RWMODE_2) {
            // Write operation: MSB only
            cr[MSB] = data;
            newCount = true;
            // Enable counter
            if (isEnabled) {
                logger
                        .log(
                                Level.CONFIG,
                                "["
                                        + PIT.MODULE_TYPE
                                        + "] Counter is already in use. Resetting may cause timing issues.");
            }
            isEnabled = true;
        } else if (rwMode == RWMODE_3) {
            // Write operation: LSB first, then MSB
            // Check if this is LSB or MSB byte (first or second byte)
            if (lsbWritten == true) {
                // Store data in Count Register
                cr[MSB] = data;
                lsbWritten = false;
                newCount = true;
            } else {
                // Store LSB of count
                cr[LSB] = data;
                lsbWritten = true;
            }
            // Enable counter
            if (isEnabled) {
                logger
                        .log(
                                Level.CONFIG,
                                "["
                                        + PIT.MODULE_TYPE
                                        + "] Counter is already in use. Resetting may cause timing issues.");
            }
            isEnabled = true;
        }
    }

    /**
     * Set counter mode
     * 
     * @param mode
     */
    protected void setCounterMode(int mode) {
        counterMode = mode;
        // Check wildcards in counter mode settings for mode 2 and 3:
        if (counterMode == 6) {
            counterMode = 2;
        } else if (counterMode == 7) {
            counterMode = 3;
        }

        // Initial setup of signals depending on counter mode:
        if (counterMode == COUNTERMODE_0) {
            // Interrupt on terminal count
            signalGate = true;
            signalOut = false;
        } else if (counterMode == COUNTERMODE_1) {
            // Hardware retriggerable one-shot
            signalGate = false;
            signalOut = true;
        } else if (counterMode == COUNTERMODE_2) {
            // Rate generator
            signalGate = true;
            signalOut = true;
        } else if (counterMode == COUNTERMODE_3) {
            // Square wave mode
            signalGate = true;
            signalOut = true;
        } else if (counterMode == COUNTERMODE_4) {
            // Software triggered strobe
            signalGate = true;
            signalOut = true;
        } else if (counterMode == COUNTERMODE_5) {
            // Hardware triggered strobe
            signalGate = false;
            signalOut = true;
        }

    }

    /**
     * Latch this counter (read back current count value) Note: this function
     * only works if the counter is in latchmode. The latched value is stored in
     * register ol.
     * 
     */
    protected void latchCounter() {
        if (isLatched) {
            logger.log(Level.WARNING, "[" + pit.getType()
                    + "] Counter already latched, cannot latch");
        } else {
            // Copy ce value to Output Latch register
            ol[LSB] = ce[LSB];
            ol[MSB] = ce[MSB];
            isLatched = true;
        }
    }

    /**
     * Load counter with new value Note: new value is stored in register cr.
     * 
     */
    private void loadCounter() {
        // Cannot guarantee the following command since update to 32-bit:
        // logger.log(Level.CONFIG,
        // pit.motherboard.getCurrentInstructionNumber() + " " + "[" +
        // pit.getType() + "] Counter " + counterNumber +
        // " loaded with new values");
        // Load new count from cr to ce, based on RW mode
        if (rwMode == RWMODE_1) {
            // Write operation: LSB only
            ce[LSB] = cr[LSB];
            ce[MSB] = 0;
        } else if (rwMode == RWMODE_2) {
            // Write operation: MSB only
            ce[LSB] = 0;
            ce[MSB] = cr[MSB];
        } else if (rwMode == RWMODE_3) {
            // Write operation: LSB first, then MSB
            ce[LSB] = cr[LSB];
            ce[MSB] = cr[MSB];
            // Cannot guarantee the following command since update to 32-bit:
            // logger.log(Level.CONFIG,
            // pit.motherboard.getCurrentInstructionNumber() + " " + "[" +
            // pit.getType() + "] Counter  " + counterNumber + " (in rwMode " +
            // rwMode + ") loaded with [" +ce[MSB] + "][" +ce[LSB] + "]");
        }

        // Check parity of ce value
        if ((((ce[MSB] & 0x000000FF) << 8) + (ce[LSB] & 0x000000FF)) % 2 == 0) {
            parity = EVEN;
        } else {
            parity = ODD;
        }
    }

    /**
     * Return the parity of the count value
     * 
     * @return boolean true=EVEN, false=ODD
     */
    protected boolean getParity() {
        // Returns the parity of counting element
        return parity;
    }

    /**
     * Return if this counter is in BCD mode
     * 
     * @return boolean true=BCD, false=decimal
     */
    protected boolean getBCD() {
        // Returns the parity of counting element
        return bcd;
    }

    /**
     * Enable/disable the counter
     * 
     * @param status
     */
    protected void setEnabled(boolean status) {
        // Set current counter in given state
        isEnabled = status;
    }

    /**
     * Return if this counter has been enabled
     * 
     * @return true if enabled, false otherwise
     */
    protected boolean isEnabled() {
        // Return status of this counter
        return isEnabled;
    }

    /**
     * Return the counter number (ID)
     * 
     * @return int counternumber
     */
    protected int getCounterNumber() {
        // Return the number of the counter
        return counterNumber;
    }
}
