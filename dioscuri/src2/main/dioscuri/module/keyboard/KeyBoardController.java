/*
 * $Revision: 159 $ $Date: 2009-08-17 12:52:56 +0000 (ma, 17 aug 2009) $ $Author: blohman $
 * 
 * Copyright (C) 2007  National Library of the Netherlands, Nationaal Archief of the Netherlands
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information about this project, visit
 * http://dioscuri.sourceforge.net/
 * or contact us via email:
 * jrvanderhoeven at users.sourceforge.net
 * blohman at users.sourceforge.net
 * 
 * Developed by:
 * Nationaal Archief               <www.nationaalarchief.nl>
 * Koninklijke Bibliotheek         <www.kb.nl>
 * Tessella Support Services plc   <www.tessella.com>
 *
 * Project Title: DIOSCURI
 *
 */

package dioscuri.module.keyboard;

/**
 * Keyboard controller
 * Virtual controller chip, located on motherboard  
 * Contains data intended for/from, and about keyboard
 *
 */
public class KeyBoardController
{
      // Status bits matching the status port
      byte parityError; // Bit 7: Parity error
                        // 0: OK; 1: Parity error with last byte from keyboard/mouse (ignored)
      byte timeOut;     // Bit 6: Timeout
                        // 0: OK; 1: Timeout from keyboard (ignored)
      byte auxBuffer;   // Bit 5: Auxiliary output buffer full
                        // 0: Keyboard data / OK; 1: Mouse data / waiting for CPU to read.
      byte keyboardLock;// Bit 4:  Keyboard lock
                        // 0: Keyboard locked; 1: Unlocked (ignored)
      byte commandData; // Bit 3: Command/Data
                        // 0: Last write was data (0x60); 1: Last write was command (0x64)
      byte systemFlag;  // Bit 2: System flag
                        // Set to 0 after power on reset; Set to 1 after completion of POST
      byte inputBuffer; // Bit 1: Input buffer status
                        // 0: Input empty, write enabled; 1: Input full, write disabled
      byte outputBuffer;// Bit 0: Output buffer status
                        // 0: Output empty, read disabled; 1: Output full, read enabled

      // Keyboard and mouse controller variables
      byte translateScancode;   // Command byte Bit 6: Translate
                                // 0: No translation; 1: Translate keyboard scancodes using translation table 
      byte auxClockEnabled;     // Mouse clock enabled - 0: No; 1: Yes. This is the negation of:
                                // Command byte Bit 5: Mouse enable
                                // 0: enable keyboard; 1: disable keyboard by driving clock line low
      byte kbdClockEnabled;     // Keyboard clock enabled - 0: No; 1: Yes. This is the negation of:
                                // Command byte Bit 4: Keyboard enable
                                // 0: enable keyboard; 1: disable keyboard by driving clock line low
      byte allowIRQ12;          // Command byte Bit 1: Mouse Interrupt Enable
                                // 0: Do not use mouse interrupts. 1: Send IRQ12 when mouse output buffer is full
      byte allowIRQ1;           // Command byte Bit 0: Keyboard Interrupt Enable
                                // 0: Do not use keyboard interrupts. 1: Send IRQ1 when keyboard output buffer is full
      byte kbdOutputBuffer;     // Current keyboard data in controller buffer
      byte auxOutputBuffer;     // ? Output buffer Bit 2: Mouse data
      byte lastCommand;         // Last command byte written to port 0x64 (needed to process data byte that follows)
      byte expectingPort60h;    // Data byte expected from last command (last_comm) sent to 0x64
      int  timerPending;        // timer is activated - essentially, data from keyboard is waiting to be processed
      byte irq1Requested;       // Raise IRQ1 (keyboard) - if allowed by allow_irq1  
      byte irq12Requested;      // Raise IRQ12 (mouse) - if allowed by allow_irq12
      byte expectingMouseParameter; // 
      byte currentScancodeSet;
      byte batInProgress;       // Selft test (Basic Assurance Test) in progress
}
