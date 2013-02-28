package computer;

import java.awt.FileDialog;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedHashMap;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

public class EmulationEnvironment {
	private DefaultControllableDCPU dcpu = new DefaultControllableDCPU();
	private DefaultAWTMonitor monitor;
	private VirtualClock clock;
	private DefaultAWTKeyboard keyboard;
	private VirtualFloppyDrive floppyDrive;
	private VirtualSleepChamber sleepChamber;
  private DefaultAWTVectorDisplay vectorDisplay;
	private JFrame frame;
	private LinkedHashMap<String, FloppyDisk> floppies = new LinkedHashMap<String, FloppyDisk>();
	
  public EmulationEnvironment() {
    frame = new JFrame("DevCPU");
    createMenu();
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setResizable(false);
    frame.setDefaultCloseOperation(3);
    frame.setVisible(true);
	}
  
  private void createMenu() {
  	JMenuBar menuBar = new JMenuBar();
  	JMenu fileMenu = new JMenu("File");
  	JMenu dcpuMenu = new JMenu("DCPU");
    JMenu hardwareMenu = new JMenu("Hardware");
    
    JMenuItem loadToRamItem = new JMenuItem("Load to RAM");
    loadToRamItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileDialog fd = new FileDialog(frame, "Select Binary File", FileDialog.LOAD);
				fd.setVisible(true);
				if (fd.getFile() != null) {
					DataInputStream dis = null;
					try {
						dis = new DataInputStream(new FileInputStream(fd.getDirectory()+fd.getFile()));
						for (int i = 0; i < 65536; i++) {
							dcpu.ram[i] = dis.readChar();
						}
					}
					catch (IOException e) {
						try {
							dis.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
    
    JMenuItem assembleToRamItem = new JMenuItem("Assemble to RAM");
    assembleToRamItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileDialog fd = new FileDialog(frame, "Select Source File", FileDialog.LOAD);
				fd.setVisible(true);
				if (fd.getFile() != null) {
					try {
						new Assembler(dcpu.ram).assemble(new File(fd.getDirectory()+fd.getFile()));
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});

    JMenuItem loadToFloppyItem = new JMenuItem("Load to Floppy");
    loadToFloppyItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileDialog fd = new FileDialog(frame, "Select Binary File", FileDialog.LOAD);
				fd.setVisible(true);
				if (fd.getFile() != null) {
					try {
						int protect = JOptionPane.showConfirmDialog(frame, "Do you wish to write-protect this disk?", "Choose Protection Option", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
						FloppyDisk disk = new FloppyDisk(new File(fd.getDirectory()+fd.getFile()), protect==JOptionPane.YES_OPTION);
						String name = null;
						while (name == null) {
							name = JOptionPane.showInputDialog(frame, "Enter a name for this disk");
							if (name == null) return;
							if (name.length() == 0 || floppies.containsKey(name)) {
								JOptionPane.showMessageDialog(frame, "Entered name, \"" + name + "\", is " + (name.length()==0 ? "blank." : "already in use."));
								name = null;
							} else {
								floppies.put(name, disk);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

    
    JMenuItem assembleToFloppyItem = new JMenuItem("Assemble to Floppy");
    
    JMenuItem switchByteOrderItem = new JMenuItem("Convert to Big Endian");
    switchByteOrderItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				FileDialog fdi = new FileDialog(frame, "Select Little Endian File", FileDialog.LOAD);
				fdi.setVisible(true);
				if (fdi.getFile() != null) {
					FileDialog fdo = new FileDialog(frame, "Name Big Endian File", FileDialog.SAVE);
					fdo.setVisible(true);
					if (fdo.getFile() != null) {
						DataInputStream dis = null;
						DataOutputStream dos = null;
						try {
							File iFile = new File(fdi.getDirectory()+fdi.getFile());
							File oFile = new File(fdo.getDirectory()+fdo.getFile());
							dis = new DataInputStream(new BufferedInputStream(new FileInputStream(iFile)));
							dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(oFile)));
							for (int i = 0; i < iFile.length()/2 ; i++) {
								dos.writeChar(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putChar(dis.readChar()).order(ByteOrder.BIG_ENDIAN).getChar(0));
							}
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
						} finally {
							try {
								dis.close();
								dos.close();
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						}
					}
				}
			}
		});
    
    JMenuItem runDCPUItem = new JMenuItem("Run");
    runDCPUItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dcpu.run();
			}
		});
    
    JMenuItem stopDCPUItem = new JMenuItem("Stop");
    stopDCPUItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dcpu.stop();
			}
		});
    
    JMenuItem clearRamItem = new JMenuItem("Clear RAM");
    clearRamItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dcpu.ram = new char[65536];
			}
		});
    
    JCheckBoxMenuItem genericClockItem = new JCheckBoxMenuItem("Generic Clock");
    genericClockItem.addItemListener(new ItemListener() {
    	public void itemStateChanged(ItemEvent e) {
    		boolean selected = e.getStateChange() == ItemEvent.SELECTED;
    		if (selected) {
    			clock = (VirtualClock) new VirtualClock().connectTo(dcpu);
    		} else {
    			clock.disconnect();
    			clock = null;
    		}
      }
  	});
    
    JCheckBoxMenuItem genericKeyboardItem = new JCheckBoxMenuItem("Generic Keyboard");
    genericKeyboardItem.addItemListener(new ItemListener() {
    	public void itemStateChanged(ItemEvent e) {
    		boolean selected = e.getStateChange() == ItemEvent.SELECTED;
    		if (selected) {
    			keyboard = (DefaultAWTKeyboard) new DefaultAWTKeyboard().connectTo(dcpu);
    			KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyboard);
    		} else {
    			KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(keyboard);
    			keyboard.disconnect();
    			keyboard = null;
    		}
      }
  	});
    
    JCheckBoxMenuItem monitorItem = new JCheckBoxMenuItem("LEM1802");
    monitorItem.addItemListener(new ItemListener() {
    	JFrame frame;
    	
    	public void itemStateChanged(ItemEvent e) {
    		boolean selected = e.getStateChange() == ItemEvent.SELECTED;
    		if (selected) {
    			monitor = (DefaultAWTMonitor) new DefaultAWTMonitor().connectTo(dcpu);
    			frame = new JFrame("LEM1802");
    			frame.add(monitor.canvas);
    			frame.pack();
    	    frame.setLocationRelativeTo(null);
    	    frame.setResizable(false);
//    	    frame.setDefaultCloseOperation(3);
    	    frame.setVisible(true);
    		} else {
    			frame.removeAll();
    			frame.setVisible(false);
    			frame.dispose();
    			monitor.disconnect();
    			monitor = null;
    		}
      }
  	});
    
    JCheckBoxMenuItem sleepChamberItem = new JCheckBoxMenuItem("SPC2000");
    sleepChamberItem.addItemListener(new ItemListener() {
    	public void itemStateChanged(ItemEvent e) {
    		boolean selected = e.getStateChange() == ItemEvent.SELECTED;
    		if (selected) {
    			sleepChamber = (VirtualSleepChamber) new VirtualSleepChamber().connectTo(dcpu);
    		} else {
    			sleepChamber.disconnect();
    			sleepChamber = null;
    		}
      }
  	});
    
    JCheckBoxMenuItem vectorDisplayItem = new JCheckBoxMenuItem("SPED-3");
    vectorDisplayItem.addItemListener(new ItemListener() {
    	JFrame frame;
    	
    	public void itemStateChanged(ItemEvent e) {
    		boolean selected = e.getStateChange() == ItemEvent.SELECTED;
    		if (selected) {
    			vectorDisplay = (DefaultAWTVectorDisplay) new DefaultAWTVectorDisplay().connectTo(dcpu);
    			frame = new JFrame("SPED-3");
    			frame.add(vectorDisplay.canvas);
    			frame.pack();
    	    frame.setLocationRelativeTo(null);
    	    frame.setResizable(false);
//    	    frame.setDefaultCloseOperation(3);
    	    frame.setVisible(true);
    		} else {
    			frame.removeAll();
    			frame.setVisible(false);
    			frame.dispose();
    			vectorDisplay.disconnect();
    			vectorDisplay = null;
    		}
      }
  	});
    
    final JMenu floppyMenu = new JMenu("Floppy");
    final JMenu insertFloppyItem = new JMenu("Insert Floppy");
  	final JMenuItem ejectFloppyItem = new JMenuItem("Eject Floppy");
  	
    JCheckBoxMenuItem floppyDriveItem = new JCheckBoxMenuItem("M35FD");
    floppyDriveItem.addItemListener(new ItemListener() {
    	public void itemStateChanged(ItemEvent e) {
    		boolean selected = e.getStateChange() == ItemEvent.SELECTED;
    		if (selected) {
    			floppyDrive = (VirtualFloppyDrive) new VirtualFloppyDrive().connectTo(dcpu);
    			floppyMenu.setEnabled(true);
    		} else {
    			floppyDrive.disconnect();
    			floppyDrive = null;
    		}
      }
  	});
  	
    floppyMenu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent arg0) {
				if (floppyDrive.getDisk() == null) {
					insertFloppyItem.setEnabled(true);
					ejectFloppyItem.setEnabled(false);
					insertFloppyItem.removeAll();
					for (String name : floppies.keySet()) {
						JMenuItem diskItem = new JMenuItem(name);
						diskItem.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								floppyDrive.insert(floppies.get(e.getActionCommand()));
							}
						});
						insertFloppyItem.add(diskItem);
					}
				} else {
					insertFloppyItem.setEnabled(false);
					ejectFloppyItem.setEnabled(true);
				}
			}

			@Override public void menuCanceled(MenuEvent arg0) {}
			@Override public void menuDeselected(MenuEvent arg0) {}
		});
  	floppyMenu.setEnabled(false);
  	
  	ejectFloppyItem.setEnabled(false);
  	ejectFloppyItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				floppyDrive.eject();
			}
		});
  	
  	floppyMenu.add(insertFloppyItem);
  	floppyMenu.add(ejectFloppyItem);
  	hardwareMenu.add(genericClockItem);
  	hardwareMenu.add(genericKeyboardItem);
  	hardwareMenu.add(monitorItem);
  	hardwareMenu.add(sleepChamberItem);
  	hardwareMenu.add(vectorDisplayItem);
  	hardwareMenu.add(floppyDriveItem);
  	hardwareMenu.add(floppyMenu);
    dcpuMenu.add(runDCPUItem);
    dcpuMenu.add(stopDCPUItem);
    dcpuMenu.add(clearRamItem);
    fileMenu.add(loadToRamItem);
    fileMenu.add(assembleToRamItem);
    fileMenu.add(loadToFloppyItem);
    fileMenu.add(assembleToFloppyItem);
    fileMenu.add(switchByteOrderItem);
    menuBar.add(fileMenu);
    menuBar.add(dcpuMenu);
    menuBar.add(hardwareMenu);
    frame.setJMenuBar(menuBar);
	}

  public static void main(String[] args) throws Exception {
  	new EmulationEnvironment();
  }
}
