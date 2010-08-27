package dioscuri.module;

import dioscuri.exception.ModuleException;
import dioscuri.exception.ModuleUnknownPort;
import dioscuri.exception.ModuleWriteOnlyPortException;

/**
 * User: bki010
 * Date: Aug 26, 2010
 * Time: 4:09:11 PM
 */
public interface Addressable {
                                                                                                               // TODO remove 'Module' from exceptions
    byte getIOPortByte(int address) throws ModuleException, ModuleUnknownPort, ModuleWriteOnlyPortException;   // TODO rename ModuleUnknownPort -> UnknownPortException
    byte[] getIOPortWord(int address) throws ModuleException, ModuleUnknownPort, ModuleWriteOnlyPortException;
    byte[] getIOPortDoubleWord(int address) throws ModuleException, ModuleUnknownPort, ModuleWriteOnlyPortException;

    void setIOPortByte(int address, byte value) throws ModuleException, ModuleUnknownPort;
    void setIOPortWord(int address, byte[] value) throws ModuleException, ModuleUnknownPort;
    void setIOPortDoubleWord(int address, byte[] value) throws ModuleException, ModuleUnknownPort;
}
