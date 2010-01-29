package dioscuri.module.memory;

import dioscuri.DummyEmulator;

public class DummyMemory extends Memory {
    public DummyMemory() {
        super(new DummyEmulator());
    }
}
