package nl.kbna.dioscuri.module.serialport;

import java.util.ArrayList;

public class FIFObuffer extends ArrayList
{

	// Attributes
	
	// Constructors
	public FIFObuffer()
	{
		super();
	}
	
	public FIFObuffer(int capacity)
	{
		super(capacity);
	}

	
	// Methods
	public boolean setByte(byte data)
	{
		return super.add(Byte.valueOf(data));
	}


	public byte getByte()
	{
		byte data = ((Byte)super.get(0)).byteValue();
		super.remove(0);
		return data;
	}

}
