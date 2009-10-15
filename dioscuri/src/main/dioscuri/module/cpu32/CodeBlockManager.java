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

import dioscuri.module.clock.Clock;

//import org.jpc.emulator.memory.*;
//import org.jpc.emulator.memory.codeblock.optimised.*;
//import org.jpc.emulator.memory.codeblock.fastcompiler.*;

public class CodeBlockManager
{
private CodeBlockFactory realModeChain, protectedModeChain, virtual8086ModeChain;
private CodeBlockFactory compilingRealModeChain, compilingProtectedModeChain;
private CodeBlockCombiner combiner;
private ByteSourceWrappedMemory byteSource;

private SpanningRealModeCodeBlock spanningRealMode;
private SpanningProtectedModeCodeBlock spanningProtectedMode;
private SpanningVirtual8086ModeCodeBlock spanningVirtual8086Mode;

private BackgroundCompiler bgc;

@SuppressWarnings("unused")
private Clock clock;

public CodeBlockManager(Clock clk)
{
    this.clock = clk;
    
    byteSource = new ByteSourceWrappedMemory();

realModeChain = new DefaultCodeBlockFactory(new RealModeUDecoder(), new OptimisedCompiler(clk));
protectedModeChain = new DefaultCodeBlockFactory(new ProtectedModeUDecoder(), new OptimisedCompiler(clk));
virtual8086ModeChain = new DefaultCodeBlockFactory(new RealModeUDecoder(), new OptimisedCompiler(clk));

spanningRealMode = new SpanningRealModeCodeBlock(new CodeBlockFactory[]{realModeChain});
spanningProtectedMode = new SpanningProtectedModeCodeBlock(new CodeBlockFactory[]{protectedModeChain});
spanningVirtual8086Mode = new SpanningVirtual8086ModeCodeBlock(new CodeBlockFactory[]{virtual8086ModeChain});

    try 
    {
        SecurityManager sm  = System.getSecurityManager();
        if (sm != null)
            sm.checkCreateClassLoader();
        System.out.println("Security Manager allows creation of classloader: attempting to use advanced compilers.");
        bgc = new BackgroundCompiler(new OptimisedCompiler(clk), new FASTCompiler());
        compilingRealModeChain = new DefaultCodeBlockFactory(new RealModeUDecoder(), bgc);
        compilingProtectedModeChain = new DefaultCodeBlockFactory(new ProtectedModeUDecoder(), bgc);
    } 
    catch (SecurityException e) 
    {
        System.out.println("Security Manager doesn't allow creation of classloader: Not using advanced compilers.");
        bgc = null;
        compilingRealModeChain = realModeChain;
        compilingProtectedModeChain = protectedModeChain;
    }

    combiner = new CodeBlockCombiner(new CompositeFactory());
}

class CompositeFactory implements CodeBlockFactory
{
    private RealModeCodeBlock tryFactory(CodeBlockFactory ff, ByteSource source, RealModeCodeBlock spanningBlock)
    {
        try 
        {
            return ff.getRealModeCodeBlock(source);
        } 
        catch(ArrayIndexOutOfBoundsException e) 
        {
            return spanningBlock;
        } 
        catch (Exception e)
        {
            return null;
        }
    }

    public RealModeCodeBlock getRealModeCodeBlock(ByteSource source)
    {
        RealModeCodeBlock block = tryFactory(compilingRealModeChain, source, spanningRealMode);
        if (block != null)
            return block;

        source.reset();
        return tryFactory(realModeChain, source, spanningRealMode);
        
    }
    
    public ProtectedModeCodeBlock getProtectedModeCodeBlock(ByteSource source, boolean operandSize)
    {
        return null;
    }

    public Virtual8086ModeCodeBlock getVirtual8086ModeCodeBlock(ByteSource source)
    {
        return null;
    }
}

private RealModeCodeBlock tryRealModeFactory(CodeBlockFactory ff, Memory memory, int offset, SpanningRealModeCodeBlock spanningBlock)
{
    try 
    {
        byteSource.set(memory, offset & AddressSpace.BLOCK_MASK);
        return ff.getRealModeCodeBlock(byteSource);
    } 
    catch(ArrayIndexOutOfBoundsException e) 
    {
    return spanningBlock;
    } 
    catch (Exception e)
    {
        return null;
    }
}

private ProtectedModeCodeBlock tryProtectedModeFactory(CodeBlockFactory ff, Memory memory, int offset, boolean operandSizeFlag, SpanningProtectedModeCodeBlock spanningBlock)
{
    try 
    {
        byteSource.set(memory, offset & AddressSpace.BLOCK_MASK);
        return ff.getProtectedModeCodeBlock(byteSource, operandSizeFlag);
    } 
    catch(ArrayIndexOutOfBoundsException e) 
    {
    return spanningBlock;
    } 
    catch (Exception e)
    {
        return null;
    }
}

private Virtual8086ModeCodeBlock tryVirtual8086ModeFactory(CodeBlockFactory ff, Memory memory, int offset, SpanningVirtual8086ModeCodeBlock spanningBlock)
{
    try 
    {
        byteSource.set(memory, offset & AddressSpace.BLOCK_MASK);
        return ff.getVirtual8086ModeCodeBlock(byteSource);
    } 
    catch(ArrayIndexOutOfBoundsException e) 
    {
    return spanningBlock;
    } 
    catch (Exception e)
    {
        return null;
    }
}

public RealModeCodeBlock getRealModeCodeBlockAt(Memory memory, int offset)
{
    RealModeCodeBlock block = null;

    if ((block = combiner.getRealModeCodeBlockAt(memory, offset)) == null)      
    if ((block = tryRealModeFactory(compilingRealModeChain, memory, offset, spanningRealMode)) == null)
    if ((block = tryRealModeFactory(realModeChain, memory, offset, spanningRealMode)) == null)
        throw new IllegalStateException("Couldn't find capable block");
        
((LazyCodeBlockMemory)memory).setRealCodeBlockAt(offset & AddressSpace.BLOCK_MASK, block);
return block;

}

public ProtectedModeCodeBlock getProtectedModeCodeBlockAt(Memory memory, int offset, boolean operandSizeFlag)
{
ProtectedModeCodeBlock block = null;

if ((block = tryProtectedModeFactory(compilingProtectedModeChain, memory, offset, operandSizeFlag, spanningProtectedMode)) == null)
    if ((block = tryProtectedModeFactory(protectedModeChain, memory, offset, operandSizeFlag, spanningProtectedMode)) == null)
    throw new IllegalStateException("Couldn't find capable block");

((LazyCodeBlockMemory)memory).setProtectedCodeBlockAt(offset & AddressSpace.BLOCK_MASK, block);
return block;
}

public Virtual8086ModeCodeBlock getVirtual8086ModeCodeBlockAt(Memory memory, int offset)
{
Virtual8086ModeCodeBlock block = null;

if ((block = tryVirtual8086ModeFactory(virtual8086ModeChain, memory, offset, spanningVirtual8086Mode)) == null)
    throw new IllegalStateException("Couldn't find capable block");

((LazyCodeBlockMemory)memory).setVirtual8086CodeBlockAt(offset & AddressSpace.BLOCK_MASK, block);
return block;
}

public void dispose()
{
    if (bgc != null)
        bgc.stop();
    bgc = null;
}

}
