package nl.kbna.dioscuri.interfaces;

public interface UART {

	public boolean isDataAvailable();
	
	public byte getSerialData();
	
	public void setSerialData(byte data);
}
