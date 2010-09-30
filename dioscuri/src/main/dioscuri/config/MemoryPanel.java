package dioscuri.config;

import dioscuri.GUI;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class MemoryPanel extends AbstractModulePanel {

    final Emulator.Architecture.Modules.Memory memory;
    final JTextField size = new JTextField();

    MemoryPanel(GUI parent, Emulator emuConfig) {
        super(parent, emuConfig);
        this.memory = emuConfig.getArchitecture().getModules().getMemory();
        super.setLayout(new GridLayout(0, 3, 5, 5));

        size.setText(memory.getSizemb().toString());

        super.add(new JLabel("size (MB)"));
        super.add(size);
        super.add(new JLabel());
    }

    @Override
    void save() throws Exception {
        memory.setSizemb(new BigDecimal(size.getText()));
    }
}
