package dioscuri.config.temp;

import dioscuri.GUI;
import dioscuri.interfaces.Module;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigDialog extends JDialog {

    private final dioscuri.config.Emulator emuConfig;
    private final GUI parent;
    private final Map<Module.Type, AbstractModulePanel> moduleMap;

    public ConfigDialog(GUI parent) {
        super(parent.asJFrame());
        this.parent = parent;
        this.emuConfig = parent.getEmuConfig();
        this.moduleMap = new LinkedHashMap<Module.Type, AbstractModulePanel>();
        super.setLayout(new BorderLayout(5, 5));
        setupModuleMap();
        setupGUI();
        super.setSize(parent.asJFrame().getWidth(), parent.asJFrame().getHeight());
        super.setLocation(parent.asJFrame().getLocation());
        super.setResizable(false);
        super.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        super.setVisible(true);
    }

    private void loadPanel(JPanel attributesPanel, JList moduleList) {
        attributesPanel.removeAll();
        AbstractModulePanel p = moduleMap.get((Module.Type)moduleList.getSelectedValue());
        attributesPanel.add(p, BorderLayout.SOUTH);
        attributesPanel.validate();
        repaint();
    }


    /*
        +--------------------------------------------------------+
        | +----------------------------------------------------+ |
        | | +------+ +---------------------------------------+ | |
        | | |      | |                                       | | |
        | | |      | |                                       | | |
        | | |    x----------------------------------------------------- moduleListPanel
        | | |      | |                                       | | |
        | | |      | |                                       | | |
        | | |      | |                                       | |x------ JDialog.getContentPane() :: BorderLayout
        | | |      | |                                       | | |
        | | |      | |                                     x----------- attributesPanel
        | | |      | |                                       | | |
        | | |      | |                                       | | |
        | | |      | |                                       |x-------- mainPanel :: BorderLayout
        | | |      | |                                       | | |
        | | +------+ +---------------------------------------+ | |
        | +----------------------------------------------------+ |
        | +----------------------------------------------------+ |
        | |                                                  x--------- buttonPanel :: FlowLayout, RIGHT
        | +----------------------------------------------------+ |
        +--------------------------------------------------------+      
     */
    private void setupGUI() {
        final JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        final JPanel attributesPanel = new JPanel(new BorderLayout(5, 5));
        attributesPanel.add(new JLabel("Select a module in the left menu."));
        final JPanel moduleListPanel = new JPanel(new BorderLayout(5, 5));
        moduleListPanel.setPreferredSize(new Dimension(180, 0));

        mainPanel.add(moduleListPanel, BorderLayout.WEST);
        mainPanel.add(attributesPanel, BorderLayout.CENTER);

        // tree panel
        final JList moduleList = new JList(moduleMap.keySet().toArray(new Module.Type[moduleMap.keySet().size()]));
        moduleListPanel.add(new JScrollPane(moduleList));
        moduleList.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                loadPanel(attributesPanel, moduleList);
            }
        });
        moduleList.addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent e) {
                loadPanel(attributesPanel, moduleList);
            }
        });

        // button panel
        final JButton cancel = new JButton("cancel");
        final JButton save = new JButton("save");
        buttonPanel.add(cancel);
        buttonPanel.add(save);
        cancel.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfigDialog.this.dispose();
            }
        });
        save.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                for(AbstractModulePanel p : ConfigDialog.this.moduleMap.values()) {
                    try {
                        p.saveAndWrite();
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        super.getContentPane().add(mainPanel, BorderLayout.CENTER);
        super.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setupModuleMap() {
        this.moduleMap.put(Module.Type.ATA, new AtaPanel(parent, emuConfig));
        this.moduleMap.put(Module.Type.BIOS, new BiosPanel(parent, emuConfig));
        this.moduleMap.put(Module.Type.BOOT, new BootPanel(parent, emuConfig));
        this.moduleMap.put(Module.Type.CPU, new CpuPanel(parent, emuConfig));
        this.moduleMap.put(Module.Type.FDC, new FdcPanel(parent, emuConfig));
        this.moduleMap.put(Module.Type.KEYBOARD, new KeyboardPanel(parent, emuConfig));
        this.moduleMap.put(Module.Type.MOUSE, new MousePanel(parent, emuConfig));
        this.moduleMap.put(Module.Type.MEMORY, new MemoryPanel(parent, emuConfig));
        this.moduleMap.put(Module.Type.PIT, new PitPanel(parent, emuConfig));
        this.moduleMap.put(Module.Type.VIDEO, new VideoPanel(parent, emuConfig));
    }
}
