package dioscuri.config;

import dioscuri.GUI;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class CpuPanel extends AbstractModulePanel {

    final Emulator.Architecture.Modules.Cpu cpu;

    final JRadioButton cpu16 = new JRadioButton("16 bit");
    final JRadioButton cpu32 = new JRadioButton("32 bit");
    final JTextField mhz = new JTextField();

    CpuPanel(GUI parent, Emulator emuConfig)
    {
        super(parent, emuConfig);
        this.cpu = emuConfig.getArchitecture().getModules().getCpu();
        super.setLayout(new GridLayout(0, 3, 5, 5));

        ButtonGroup group = new ButtonGroup();
        group.add(cpu16);
        group.add(cpu32);

        cpu16.setSelected(!cpu.isCpu32Bit());
        cpu32.setSelected(cpu.isCpu32Bit());
        mhz.setText(cpu.getSpeedmhz().toString());

        super.add(new JLabel("CPU bits"));
        super.add(cpu16);
        super.add(cpu32);

        super.add(new JLabel("Speed (MHZ)"));
        super.add(mhz);
        super.add(new JLabel());
    }

    @Override
    void save() throws Exception
    {
        cpu.setCpu32Bit(cpu32.isSelected());
        cpu.setSpeedmhz(new BigDecimal(mhz.getText()));
    }
}
