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
package dioscuri.module.cpu32;

public class DefaultCodeBlockFactory implements CodeBlockFactory
{
    private Decoder decoder;
    private CodeBlockCompiler compiler;

    public DefaultCodeBlockFactory(Decoder decoder, CodeBlockCompiler compiler)
    {
    this.decoder = decoder;
    this.compiler = compiler;
    }

    public RealModeCodeBlock getRealModeCodeBlock(ByteSource source)
    {
    return compiler.getRealModeCodeBlock(decoder.decodeReal(source));
    }


    public ProtectedModeCodeBlock getProtectedModeCodeBlock(ByteSource source, boolean operandSize)
    {
    return compiler.getProtectedModeCodeBlock(decoder.decodeProtected(source, operandSize));
    }

    public Virtual8086ModeCodeBlock getVirtual8086ModeCodeBlock(ByteSource source)
    {
    return compiler.getVirtual8086ModeCodeBlock(decoder.decodeVirtual8086(source));
    }
}
