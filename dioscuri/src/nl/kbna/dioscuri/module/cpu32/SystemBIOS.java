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
package nl.kbna.dioscuri.module.cpu32;


//import org.jpc.emulator.*;
//import org.jpc.emulator.memory.*;
import java.io.*;

import nl.kbna.dioscuri.module.clock.Clock;

public class SystemBIOS extends AbstractHardwareComponent implements IOPortCapable
{
    private byte[] imageData;
    private boolean ioportRegistered, loaded;
    private Clock clock;

    public SystemBIOS(byte[] image, Clock clk)
    {
    loaded = false;
    ioportRegistered = false;
    this.clock = clk;

    imageData = new byte[image.length];
    System.arraycopy(image, 0, imageData, 0, image.length);
    }

    public SystemBIOS(String imagefile) throws IOException
    {
        InputStream in = null;
        try 
        {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        in = getClass().getResourceAsStream("/" + imagefile);

        while (true) 
            {
                int ch = in.read();
                if (ch < 0)
                    break;
                bout.write((byte) ch);
            }

        imageData = bout.toByteArray();
        } 
        finally 
        {
        try 
            {
        in.close();
            } 
            catch (Exception e) {}
        }
    }

    public void dumpState(DataOutput output) throws IOException
    {
        output.writeInt(imageData.length);
        output.write(imageData);
    }

    public void loadState(DataInput input) throws IOException
    {
        loaded = false;
        ioportRegistered = false;
        int len = input.readInt();
        imageData = new byte[len];
        input.readFully(imageData,0,len);
    }

    public int[] ioPortsRequested()
    {
    return new int[]{0x400, 0x401, 0x402, 0x403, 0x8900};
    }

    public int ioPortReadByte(int address) { return 0xff; }
    public int ioPortReadWord(int address) { return 0xffff; }
    public int ioPortReadLong(int address) { return (int)0xffffffff; }

    public void ioPortWriteByte(int address, int data)
    {
    switch(address) 
        {
        /* Bochs BIOS Messages */
    case 0x402:
    case 0x403:
        try 
            {
            System.out.print(new String(new byte[]{(byte)data},"US-ASCII"));
            } 
            catch (Exception e) 
            {
            System.out.print(new String(new byte[]{(byte)data}));                
            }
        break;
    case 0x8900:
        System.err.println("Attempt to call Shutdown");
        break;
    default:
    }
    }

    public void ioPortWriteWord(int address, int data)
    {
    switch(address) {
        /* Bochs BIOS Messages */
    case 0x400:
    case 0x401:
        System.err.println("BIOS panic at rombios.c, line " + data);
    default:
    }
    }

    public void ioPortWriteLong(int address, int data) {}

    public void load(PhysicalAddressSpace physicalAddress)
    {
        int blockSize = AddressSpace.BLOCK_SIZE;
        int len = ((imageData.length-1)/ blockSize + 1)*blockSize;
        int fraction = len - imageData.length;
        int imageOffset = blockSize - fraction;
        
        EPROMMemory ep = new EPROMMemory(blockSize, fraction, imageData, 0, imageOffset, clock);
        physicalAddress.allocateMemory(0x100000 - len, ep);
        
        for (int i=1; i<len/blockSize; i++)
        {
            ep = new EPROMMemory(blockSize, 0, imageData, imageOffset, blockSize, clock);
            physicalAddress.allocateMemory(0x100000 - len + i*blockSize, ep);
            imageOffset += blockSize;
        }
    }

    public byte[] getImage()
    {
    return (byte[]) imageData.clone();
    }

    public boolean updated()
    {
    return (loaded && ioportRegistered);
    }

    public void updateComponent(HardwareComponent component)
    {
    if ((component instanceof PhysicalAddressSpace) && component.updated()) 
        {
        this.load((PhysicalAddressSpace)component);
        loaded = true;
    }

    if ((component instanceof IOPortHandler) && component.updated()) 
        {
        ((IOPortHandler)component).registerIOPortCapable(this);
        ioportRegistered = true;
    }
    }

    public boolean initialised()
    {
    return (loaded && ioportRegistered);
    }

    public void acceptComponent(HardwareComponent component)
    {
    if ((component instanceof PhysicalAddressSpace) && component.initialised()) 
        {
        this.load((PhysicalAddressSpace)component);
        loaded = true;
    }

    if ((component instanceof IOPortHandler) && component.initialised()) 
        {
        ((IOPortHandler)component).registerIOPortCapable(this);
        ioportRegistered = true;
    }
    }
    
    public boolean reset()
    {
        loaded = false;
        ioportRegistered = false;
        return true;
    }
}
