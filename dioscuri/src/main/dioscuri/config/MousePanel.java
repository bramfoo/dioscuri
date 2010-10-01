package dioscuri.config;

import dioscuri.GUI;

import javax.swing.*;
import java.awt.*;

public class MousePanel extends AbstractModulePanel {

    final Emulator.Architecture.Modules.Mouse mouse;

    final String[] typeChoices = {"ps/2", "serial"};

    final JCheckBox enabled = new JCheckBox();
    final JComboBox typeCombo = new JComboBox(typeChoices);

    MousePanel(GUI parent, Emulator emuConfig)
    {
        super(parent, emuConfig);
        this.mouse = emuConfig.getArchitecture().getModules().getMouse();
        super.setLayout(new GridLayout(0, 3, 5, 5));

        enabled.setSelected(mouse.isEnabled());
        typeCombo.setSelectedItem(mouse.getMousetype());

        super.add(new JLabel("enabled"));
        super.add(enabled);
        super.add(new JLabel());

        super.add(new JLabel("type"));
        super.add(typeCombo);
        super.add(new JLabel());
    }

    @Override
    void save() throws Exception
    {
        mouse.setEnabled(enabled.isSelected());
        mouse.setMousetype(typeCombo.getSelectedItem().toString());
    }
}
