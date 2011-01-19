package dioscuri.config;

import dioscuri.GUI;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

public class VncPanel extends AbstractModulePanel {

    final Emulator.Architecture.Modules.Vnc vnc;
    // Attributes
    final JCheckBox enabled = new JCheckBox();
    final JTextField port = new JTextField();
    final JTextField password = new JTextField();


    // Constructor
    VncPanel(GUI parent, Emulator emuConfig)
    {
        super(parent, emuConfig);
        this.vnc = emuConfig.getArchitecture().getModules().getVnc();
        super.setLayout(new GridLayout(0, 3, 5, 5));

        enabled.setSelected(vnc.isEnabled());
        port.setText(vnc.getPort().toString());
        password.setText(vnc.getPassword());

        super.add(new JLabel("enabled"));
        super.add(enabled);
        super.add(new JLabel());

        JPanel portPanel = new JPanel(new BorderLayout(5,5));
        portPanel.add(new JLabel("port"), BorderLayout.WEST);
        portPanel.add(new JLabel("59"), BorderLayout.EAST);
        super.add(portPanel);
        super.add(port);
        super.add(new JLabel());

        super.add(new JLabel("password"));
        super.add(password);
        super.add(new JLabel());
    }

    @Override
    void save() throws Exception
    {
        vnc.setEnabled(enabled.isSelected());
        vnc.setPort(new BigDecimal(port.getText()));
        vnc.setPassword(password.getText());
    }
}
