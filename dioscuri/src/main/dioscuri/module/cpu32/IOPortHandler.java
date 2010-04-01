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
// Edit (bram): Changed class to communicate via Dioscuri motherboard

package dioscuri.module.cpu32;

//import org.jpc.emulator.HardwareComponent;
import java.io.*;

import dioscuri.exception.ModuleException;
import dioscuri.module.Module;
import dioscuri.module.ModuleDevice;
import dioscuri.module.ModuleMotherboard;

/**
 * Class for storing the I/O port map, and handling the required redirection.
 */
public class IOPortHandler implements IOPortCapable, HardwareComponent {
    private ModuleMotherboard mb;
    // private final int MAX_IOPORTS = mb.ioSpaceSize;
    private final int MAX_IOPORTS = 65536;

    IOPortCapable[] ioPortDevice;

    private static final IOPortCapable defaultDevice;
    static {
        defaultDevice = new UnconnectedIOPort();
    }

    /**
     *
     */
    public IOPortHandler() {
        ioPortDevice = new IOPortCapable[MAX_IOPORTS];
        for (int i = 0; i < ioPortDevice.length; i++)
            ioPortDevice[i] = defaultDevice;
    }

    /**
     *
     * @param mod
     * @return -
     */
    public boolean setConnection(Module mod) {
        // Set connection for motherboard
        if (mod.getType().equalsIgnoreCase("motherboard")) {
            this.mb = (ModuleMotherboard) mod;
            return true;
        } else
            return false;
    }

    /**
     *
     * @param output
     * @throws IOException
     */
    public void dumpState(DataOutput output) throws IOException {
    }

    /**
     *
     * @param input
     * @throws IOException
     */
    public void loadState(DataInput input) throws IOException {
        reset();
    }

    /**
     *
     * @param address
     * @return -
     * @throws ModuleException
     */
    public int ioPortReadByte(int address) throws ModuleException {
        // return ioPortDevice[address].ioPortReadByte(address);
        int result = mb.getIOPortByte(address);
        result &= 0xFF;
        return result;
    }

    /**
     *
     * @param address
     * @return -
     * @throws ModuleException
     */
    public int ioPortReadWord(int address) throws ModuleException {
        // return ioPortDevice[address].ioPortReadWord(address);
        byte[] bytes = mb.getIOPortWord(address);
        int result = ((((int) bytes[0]) & 0xFF) << 8)
                + (((int) bytes[1]) & 0xFF);
        // int result = ((((int) bytes[1]) & 0xFF) << 8) + (((int) bytes[0]) &
        // 0xFF);
        return result;
    }

    /**
     *
     * @param address
     * @return -
     * @throws ModuleException
     */
    public int ioPortReadLong(int address) throws ModuleException {
        // return ioPortDevice[address].ioPortReadLong(address);
        byte[] bytes = mb.getIOPortDoubleWord(address);
        // int result = ((((int) bytes[0]) & 0xFF) << 24) + ((((int) bytes[1]) &
        // 0xFF) << 16) + ((((int) bytes[2]) & 0xFF) << 8) + (((int) bytes[3]) &
        // 0xFF);
        int result = ((((int) bytes[3]) & 0xFF) << 24)
                + ((((int) bytes[2]) & 0xFF) << 16)
                + ((((int) bytes[1]) & 0xFF) << 8) + (((int) bytes[0]) & 0xFF);
        return result;
    }

    /**
     *
     * @param address
     * @param data
     * @throws ModuleException
     */
    public void ioPortWriteByte(int address, int data) throws ModuleException {
        // ioPortDevice[address].ioPortWriteByte(address, data);
        mb.setIOPortByte(address, (byte) data);
    }

    /**
     *
     * @param address
     * @param data
     * @throws ModuleException
     */
    public void ioPortWriteWord(int address, int data) throws ModuleException {
        // ioPortDevice[address].ioPortWriteWord(address, data);
    	// FIXME: Different byte order than ioPortWriteLong(), but this is correct for module.video
        byte[] dataWord = new byte[] { ((byte) ((data >> 8) & 0xFF)),
        		((byte) (data & 0xFF))};
        mb.setIOPortWord(address, dataWord);
    }

    /**
     *
     * @param address
     * @param data
     * @throws ModuleException
     */
    public void ioPortWriteLong(int address, int data) throws ModuleException {
        // ioPortDevice[address].ioPortWriteLong(address, data);
    	// FIXME: Different byte order than ioPortWriteWord(), but this is correct for module.ata
        byte[] dataDWord = new byte[] { ((byte) (data & 0xFF)),
        		((byte) ((data >> 8) & 0xFF)),
        		((byte) ((data >> 16) & 0xFF)),
        		((byte) ((data >> 24) & 0xFF))};
        mb.setIOPortDoubleWord(address, dataDWord);
    }

    /**
     *
     * @return -
     */
    public int[] ioPortsRequested() {
        return null;
    }

    /**
     *
     * @param device
     */
    public void registerIOPortCapable(IOPortCapable device) {
        int[] portArray = device.ioPortsRequested();
        if (portArray == null)
            return;
        for (int i = 0; i < portArray.length; i++) {
            int port = portArray[i];
            if (ioPortDevice[port] == defaultDevice
                    || ioPortDevice[port] == device) {
                // ioPortDevice[port] = device;
                mb.setIOPort(port, (ModuleDevice) device);
            }
        }
    }

    /**
     *
     * @param device
     */
    public void deregisterIOPortCapable(IOPortCapable device) {
        int[] portArray = device.ioPortsRequested();
        for (int i = 0; i < portArray.length; i++) {
            int port = portArray[i];
            ioPortDevice[port] = defaultDevice;
        }
    }

    /**
     *
     * @return -
     */
    public String map() {
        String tempString = "";
        tempString += "IO Port Handler:\n";
        tempString += "Registered Ports:\n";
        for (int i = 0; i < MAX_IOPORTS; i++) {
            if (ioPortDevice[i] == defaultDevice)
                continue;
            tempString += "Port: 0x" + Integer.toHexString(0xffff & i) + " - ";
            tempString += ioPortDevice[i].getClass().getName() + "\n";
        }
        return tempString;
    }

    /**
     *
     * @return -
     */
    public boolean reset() {
        ioPortDevice = new IOPortCapable[MAX_IOPORTS];
        for (int i = 0; i < ioPortDevice.length; i++)
            ioPortDevice[i] = defaultDevice;

        return true;
    }

    /**
     *
     * @return -
     */
    public boolean initialised() {
        return true;
    }

    /**
     *
     * @param component
     */
    public void acceptComponent(HardwareComponent component) {
    }

    @Override
    public String toString() {
        return "IOPort Bus";
    }

    static class UnconnectedIOPort implements IOPortCapable {
        public int ioPortReadByte(int address) {
            // if (address != 0x80)
            // System.out.println("RB IO[0x" + Integer.toHexString(0xffff &
            // address) + "]");
            return 0xff;
        }

        public int ioPortReadWord(int address) {
            // if (address != 0x80)
            // System.out.println("RW IO[0x" + Integer.toHexString(0xffff &
            // address) + "]");
            return 0xffff;
        }

        public int ioPortReadLong(int address) {
            // if (address != 0x80)
            // System.out.println("RL IO[0x" + Integer.toHexString(0xffff &
            // address) + "]");
            return 0xffffffff;
        }

        public void ioPortWriteByte(int address, int data) {
            // if (address != 0x80)
            // System.out.println("WB IO[0x" + Integer.toHexString(0xffff &
            // address) + "]");
        }

        public void ioPortWriteWord(int address, int data) {
            // if (address != 0x80)
            // System.out.println("WW IO[0x" + Integer.toHexString(0xffff &
            // address) + "]");
        }

        public void ioPortWriteLong(int address, int data) {
            // if (address != 0x80)
            // System.out.println("WL IO[0x" + Integer.toHexString(0xffff &
            // address) + "]");
        }

        public int[] ioPortsRequested() {
            return null;
        }

        public void timerCallback() {
        }

        public void dumpState(DataOutput output) {
        }

        public void loadState(DataInput input) {
        }

        public boolean reset() {
            return true;
        }

        public void updateComponent(HardwareComponent component) {
        }

        public boolean updated() {
            return true;
        }

        public void acceptComponent(HardwareComponent component) {
        }

        public boolean initialised() {
            return true;
        }
    }

    /**
     *
     * @param component
     */
    public void updateComponent(HardwareComponent component) {
    }

    /**
     *
     * @return -
     */
    public boolean updated() {
        return true;
    }

    /**
     *
     */
    public void timerCallback() {
    }
}
