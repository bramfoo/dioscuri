package dioscuri.config.temp;

import dioscuri.GUI;
import dioscuri.config.Emulator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.math.BigInteger;

public class BiosPanel extends AbstractModulePanel {

    final Emulator.Architecture.Modules.Bios bios;

    final JTextField sysBios = new JTextField();
    final JButton browseSysBios = new JButton("browse");
    final JTextField vgaBios = new JTextField();
    final JButton browseVgaBios = new JButton("browse");
    final JTextField sysBiosStart = new JTextField();
    final JTextField vgaBiosStart = new JTextField();

    BiosPanel(GUI parent, Emulator emuConfig) {
        super(parent, emuConfig);
        this.bios = emuConfig.getArchitecture().getModules().getBios().get(0);
        super.setLayout(new GridLayout(0, 3, 5, 5));

        sysBios.setText(new File(bios.getSysbiosfilepath()).getAbsolutePath());
        vgaBios.setText(new File(bios.getVgabiosfilepath()).getAbsolutePath());
        sysBiosStart.setText(bios.getRamaddresssysbiosstartdec().toString());
        vgaBiosStart.setText(bios.getRamaddressvgabiosstartdec().toString());

        sysBios.setToolTipText(sysBios.getText());
        vgaBios.setToolTipText(vgaBios.getText());

        super.add(new JLabel("System BIOS"));
        super.add(sysBios);
        super.add(browseSysBios);
        sysBios.setEditable(false);

        super.add(new JLabel("VGA BIOS"));
        super.add(vgaBios);
        super.add(browseVgaBios);
        vgaBios.setEditable(false);

        super.add(new JLabel("System BIOS start"));
        super.add(sysBiosStart);
        super.add(new JLabel());

        super.add(new JLabel("System BIOS start"));
        super.add(vgaBiosStart);
        super.add(new JLabel());

        browseSysBios.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = BiosPanel.super.chooseFile();
                if(file != null) {
                    sysBios.setText(file == null ? "" : file.getAbsolutePath());
                    sysBios.setToolTipText(sysBios.getText());
                }
            }
        });

        browseVgaBios.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = BiosPanel.super.chooseFile();
                if(file != null) {
                    vgaBios.setText(file == null ? "" : file.getAbsolutePath());
                    vgaBios.setToolTipText(vgaBios.getText());
                }
            }
        });
    }

    @Override
    void save() throws Exception {
        bios.setSysbiosfilepath(sysBios.getText());
        bios.setVgabiosfilepath(vgaBios.getText());
        bios.setRamaddresssysbiosstartdec(new BigInteger(sysBiosStart.getText()));
        bios.setRamaddressvgabiosstartdec(new BigInteger(vgaBiosStart.getText()));
    }
}
