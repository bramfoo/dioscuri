package dioscuri.module;

import dioscuri.exception.ModuleException;
import dioscuri.exception.ModuleUnknownPortException;
import dioscuri.exception.ModuleWriteOnlyPortException;

public interface Addressable {

    byte getIOPortByte(int portAddress)
            throws ModuleException, ModuleWriteOnlyPortException, ModuleUnknownPortException;

    byte[] getIOPortDoubleWord(int portAddress)
            throws ModuleException, ModuleWriteOnlyPortException, ModuleUnknownPortException;

    byte[] getIOPortWord(int portAddress)
            throws ModuleException, ModuleWriteOnlyPortException, ModuleUnknownPortException;

    void setIOPortByte(int portAddress, byte value)
            throws ModuleException, ModuleUnknownPortException;

    void setIOPortDoubleWord(int portAddress, byte[] value)
            throws ModuleException, ModuleUnknownPortException;

    void setIOPortWord(int portAddress, byte[] value)
            throws ModuleException, ModuleUnknownPortException;
}
