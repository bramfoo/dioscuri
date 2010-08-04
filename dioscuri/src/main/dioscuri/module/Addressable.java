package dioscuri.module;

import dioscuri.exception.ModuleException;
import dioscuri.exception.ModuleWriteOnlyPortException;

public interface Addressable {

    byte getIOPortByte(int portAddress) throws ModuleException, ModuleWriteOnlyPortException;

    byte[] getIOPortDoubleWord(int portAddress) throws ModuleException, ModuleWriteOnlyPortException;

    byte[] getIOPortWord(int portAddress) throws ModuleException, ModuleWriteOnlyPortException;

    void setIOPortByte(int portAddress, byte value) throws ModuleException;

    void setIOPortDoubleWord(int portAddress, byte[] value) throws ModuleException;

    void setIOPortWord(int portAddress, byte[] value) throws ModuleException;
}
