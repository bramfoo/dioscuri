package dioscuri.config;

import dioscuri.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.math.BigInteger;

public class FdcPanel extends AbstractModulePanel {

    final Emulator.Architecture.Modules.Fdc fdc;
    final Emulator.Architecture.Modules.Fdc.Floppy floppy;

    final String[] driveChoices = {"A", "B"};
    final String[] formatChoices = {"2.88M", "1.44M", "1.2M", "720K", "360K", "320K", "180K", "160K"};

    final JTextField updateInterval = new JTextField();
    final JCheckBox enabled = new JCheckBox();
    final JCheckBox inserted = new JCheckBox();
    final JComboBox driveCombo = new JComboBox(driveChoices);
    final JComboBox formatCombo = new JComboBox(formatChoices);
    final JCheckBox writeProtected = new JCheckBox();
    final JTextField imageFile = new JTextField();
    final JButton browse = new JButton("browse");

    FdcPanel(GUI parent, Emulator emuConfig) {
        super(parent, emuConfig);
        this.fdc = emuConfig.getArchitecture().getModules().getFdc();
        this.floppy = emuConfig.getArchitecture().getModules().getFdc().getFloppy().get(0);
        super.setLayout(new GridLayout(0, 3, 5, 5));

        updateInterval.setText(fdc.getUpdateintervalmicrosecs().toString());
        enabled.setSelected(floppy.isEnabled());
        inserted.setSelected(floppy.isInserted());
        driveCombo.setSelectedItem(floppy.getDriveletter());
        formatCombo.setSelectedItem(floppy.getDiskformat());
        writeProtected.setSelected(floppy.isWriteprotected());
        imageFile.setText(floppy.getImagefilepath());

        imageFile.setToolTipText(imageFile.getText());

        imageFile.addFocusListener(new FocusAdapter(){
            @Override
            public void focusGained(FocusEvent e) {
                imageFile.setCaretPosition(imageFile.getText().length());
            }
        });

        super.add(new JLabel("update interval"));
        super.add(updateInterval);
        super.add(new JLabel());

        super.add(new JLabel("enabled"));
        super.add(enabled);
        super.add(new JLabel());

        super.add(new JLabel("inserted"));
        super.add(inserted);
        super.add(new JLabel());

        super.add(new JLabel("drive letter"));
        super.add(driveCombo);
        super.add(new JLabel());

        super.add(new JLabel("disk format"));
        super.add(formatCombo);
        super.add(new JLabel());

        super.add(new JLabel("write protected"));
        super.add(writeProtected);
        super.add(new JLabel());

        super.add(new JLabel("image file"));
        super.add(imageFile);
        super.add(browse);

        browse.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                File file = FdcPanel.super.chooseFile();
                if(file != null) {
                    imageFile.setText(file.getAbsolutePath());
                    imageFile.setToolTipText(imageFile.getText());
                }
            }
        });
    }

    @Override
    void save() throws Exception {
        fdc.setUpdateintervalmicrosecs(new BigInteger(updateInterval.getText()));
        floppy.setEnabled(enabled.isSelected());
        floppy.setInserted(inserted.isSelected());
        floppy.setDriveletter(driveCombo.getSelectedItem().toString());
        floppy.setDiskformat(formatCombo.getSelectedItem().toString());
        floppy.setWriteprotected(writeProtected.isSelected());
        floppy.setImagefilepath(imageFile.getText());
    }
}
