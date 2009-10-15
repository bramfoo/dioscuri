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

import java.util.*;

//import org.jpc.emulator.memory.codeblock.optimised.*;
//import org.jpc.emulator.memory.codeblock.fastcompiler.*;
//import org.jpc.emulator.memory.*;
//import org.jpc.emulator.processor.*;
@SuppressWarnings("unused")
public class BackgroundCompiler implements CodeBlockCompiler
{
    private static final int COMPILER_QUEUE_SIZE = 20;
    private static final int COMPILE_REQUEST_THRESHOLD = 64;

    private CodeBlockCompiler immediate, delayed, testing;
    private CompilerQueue compilerQueue;

    private boolean running;

    public BackgroundCompiler(CodeBlockCompiler immediate, CodeBlockCompiler delayed)
    {
        this.immediate = immediate;
        this.delayed = delayed;
    compilerQueue = new CompilerQueue(COMPILER_QUEUE_SIZE);

        running = true;
        new CompilerThread();
    }

    public void stop()
    {
        running = false;
    }        

    class CompilerThread extends Thread
    {
        CompilerThread()
        {
            super("Background CodeBlock Compiler Task");
            start();
            setPriority(Math.max(Thread.MIN_PRIORITY, Thread.currentThread().getPriority()-1));
        }

        public void run()
        {
            while (running)
            {
        ExecuteCountingCodeBlockWrapper target = compilerQueue.getBlock();
                try
                {
                    if (target == null)
                    {
                        Thread.sleep(100);
                        continue;
                    }

                    CodeBlock src = target.getBlock();

            if (src instanceof RealModeUBlock) 
                    {
            RealModeCodeBlock result = delayed.getRealModeCodeBlock(((RealModeUBlock)src).getAsInstructionSource());
            target.replaceInOwner(result);
            } 
                    else if (src instanceof ProtectedModeUBlock) 
                    {
            ProtectedModeCodeBlock result = delayed.getProtectedModeCodeBlock(((ProtectedModeUBlock)src).getAsInstructionSource());
            target.replaceInOwner(result);
            }
                } 
                catch (ClassFormatError e) 
                {
            System.out.println(e);
            target.replaceInOwner(target.getBlock());
        } 
                catch (IllegalStateException e) 
                {
//            System.out.println(e);
            target.replaceInOwner(target.getBlock());
        } 
                catch (Throwable e) 
                {
            System.out.println(e);
            target.replaceInOwner(target.getBlock());
                }
            }
        }
    }

    public RealModeCodeBlock getRealModeCodeBlock(InstructionSource source)
    {
        RealModeCodeBlock imm = immediate.getRealModeCodeBlock(source);
    return new RealModeCodeBlockWrapper(imm);
    }

    public ProtectedModeCodeBlock getProtectedModeCodeBlock(InstructionSource source)
    {
    ProtectedModeCodeBlock imm = immediate.getProtectedModeCodeBlock(source);
    return new ProtectedModeCodeBlockWrapper(imm);
    }

    public Virtual8086ModeCodeBlock getVirtual8086ModeCodeBlock(InstructionSource source)
    {
//  Virtual8086ModeCodeBlock imm = immediate.getVirtual8086ModeCodeBlock(source);
//  return new Virtual8086ModeCodeBlockWrapper(imm);

    return immediate.getVirtual8086ModeCodeBlock(source);
    }

    abstract class ExecuteCountingCodeBlockWrapper extends AbstractCodeBlockWrapper implements Comparable<ExecuteCountingCodeBlockWrapper>
    {
        int executeCount, diff;

    int loadedExecuteCount;

    public ExecuteCountingCodeBlockWrapper(CodeBlock block)
    {
            super(block);
        }

        public int execute(Processor cpu)
        {
        if ((++executeCount & COMPILE_REQUEST_THRESHOLD) == 0) {
        loadedExecuteCount = executeCount;
        compilerQueue.addBlock(this);
        }

            return super.execute(cpu);
        }

    public int compareTo(ExecuteCountingCodeBlockWrapper o)
    {
        
        if (loadedExecuteCount > o.loadedExecuteCount)
        return 1;
        else
        return -1;
    }
    }

    class RealModeCodeBlockWrapper extends ExecuteCountingCodeBlockWrapper implements RealModeCodeBlock
    {
    public RealModeCodeBlockWrapper(RealModeCodeBlock block)
    {
            super(block);
    }
    }

    class ProtectedModeCodeBlockWrapper extends ExecuteCountingCodeBlockWrapper implements ProtectedModeCodeBlock
    {
    public ProtectedModeCodeBlockWrapper(ProtectedModeCodeBlock block)
    {
        super(block);
    }
    }

    static class CompilerQueue
    {
    private final PriorityDeque queue;

    private final int capacity;

    private final Object lock = new Object();

    CompilerQueue(int size)
    {
        queue = new PriorityDeque(size);
        capacity = size;
    }

    void addBlock(ExecuteCountingCodeBlockWrapper block)
    {
        synchronized (lock) {
        queue.remove(block);
        queue.offer(block);
        
        while (queue.size() >= capacity)
            queue.pollFirst();
        }
    }

    ExecuteCountingCodeBlockWrapper getBlock()
    {
        synchronized (lock) {
        return (ExecuteCountingCodeBlockWrapper)(queue.pollLast());
        }
    }
    }

    protected void finalize()
    {
        stop();
    }
}
