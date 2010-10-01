package dioscuri.config;

import dioscuri.GUI;

import javax.swing.*;
import java.awt.*;

public class BootPanel extends AbstractModulePanel {

    final Emulator.Architecture.Modules.Bios bios;

    final String[] choices = {"Floppy Drive", "Hard Drive", "None"};

    final JComboBox boot0 = new JComboBox(choices);
    final JComboBox boot1 = new JComboBox(choices);
    final JComboBox boot2 = new JComboBox(choices);
    final JCheckBox floppyCheckDisabled = new JCheckBox();

    BootPanel(GUI parent, Emulator emuConfig)
    {
        super(parent, emuConfig);
        this.bios = emuConfig.getArchitecture().getModules().getBios().get(0);
        super.setLayout(new GridLayout(0, 3, 5, 5));

        boot0.setSelectedItem(bios.getBootdrives().getBootdrive0());
        boot1.setSelectedItem(bios.getBootdrives().getBootdrive1());
        boot2.setSelectedItem(bios.getBootdrives().getBootdrive2());
        floppyCheckDisabled.setSelected(bios.isFloppycheckdisabled());

        super.add(new JLabel("Boot drive 1"));
        super.add(boot0);
        super.add(new JLabel());

        super.add(new JLabel("Boot drive 2"));
        super.add(boot1);
        super.add(new JLabel());

        super.add(new JLabel("Boot drive 3"));
        super.add(boot2);
        super.add(new JLabel());

        super.add(new JLabel("Floppy check disabled"));
        super.add(floppyCheckDisabled);
        super.add(new JLabel());
    }

    @Override
    void save() throws Exception
    {
        bios.getBootdrives().setBootdrive0(boot0.getSelectedItem().toString());
        bios.getBootdrives().setBootdrive1(boot1.getSelectedItem().toString());
        bios.getBootdrives().setBootdrive2(boot2.getSelectedItem().toString());
        bios.setFloppycheckdisabled(floppyCheckDisabled.isSelected());
    }
}
