package computer;

import java.awt.FileDialog;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class EmulationEnvironment {
	private DefaultControllableDCPU dcpu = new DefaultControllableDCPU();
	private DefaultAWTMonitor monitor;
	private VirtualClock clock;
	private DefaultAWTKeyboard keyboard;
	private VirtualFloppyDrive floppyDrive;
	private VirtualSleepChamber sleepChamber;
  private VirtualVectorDisplay vectorDisplay;
	private JFrame frame;
	
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
    
    JMenuItem assembleToRamItem = new JMenuItem("Assemble to RAM");
    assembleToRamItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FileDialog fd = new FileDialog(frame, "Select Source File", FileDialog.LOAD);
				fd.setVisible(true);
				try {
					new Assembler(dcpu.ram).assemble(new File(fd.getDirectory()+fd.getFile()));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

    JMenuItem loadToFloppyItem = new JMenuItem("Load to Floppy");
    JMenuItem assembleToFloppyItem = new JMenuItem("Assemble to Floppy");
    
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
    	public void itemStateChanged(ItemEvent e) {
    		boolean selected = e.getStateChange() == ItemEvent.SELECTED;
    		if (selected) {
    			monitor = (DefaultAWTMonitor) new DefaultAWTMonitor().connectTo(dcpu);
    			frame.add(monitor.canvas);
    	    frame.pack();
    		} else {
    			frame.remove(monitor.canvas);
    			frame.pack();
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
    	public void itemStateChanged(ItemEvent e) {
    		boolean selected = e.getStateChange() == ItemEvent.SELECTED;
    		if (selected) {
    			vectorDisplay = (VirtualVectorDisplay) new VirtualVectorDisplay().connectTo(dcpu);
    		} else {
    			vectorDisplay.disconnect();
    			vectorDisplay = null;
    		}
      }
  	});
    
    final JMenu floppyMenu = new JMenu("Floppy");
  	floppyMenu.setEnabled(false);
  	
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
  	
  	JMenuItem insertFloppyItem = new JMenuItem("Insert Floppy");
  	
  	JMenuItem ejectFloppyItem = new JMenuItem("Eject Floppy");
  	ejectFloppyItem.setEnabled(false);
  	
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
    menuBar.add(fileMenu);
    menuBar.add(dcpuMenu);
    menuBar.add(hardwareMenu);
    frame.setJMenuBar(menuBar);
	}

	public void startDCPU() {
  	(new Thread(){
  		public void run() {
  			long ops = 0L;
  	    int hz = 1000000;
  	    int cyclesPerFrame = hz / 60;

  	    long nsPerFrame = 16666666L;
  	    long nextTime = System.nanoTime();

  	    double tick = 0;
  	    double total = 0;

  	    long time = System.currentTimeMillis();
  	    while (true) {//!stop) {
  	      long a = System.nanoTime();
  	      while (System.nanoTime() < nextTime) {
  	        try {
  	          Thread.sleep(1L);
  	        } catch (InterruptedException e) {
  	          e.printStackTrace();
  	        }
  	      }
  	      long b = System.nanoTime();
  	      while (dcpu.cycles < cyclesPerFrame) {
  	        dcpu.tick();
  	      }

  	      dcpu.tickHardware();
  	      dcpu.cycles -= cyclesPerFrame;
  	      long c = System.nanoTime();
  	      ops += cyclesPerFrame;
  	      nextTime += nsPerFrame;

  	      tick += (c - b) / 1000000000.0;
  	      total += (c - a) / 1000000000.0;

  	      while (System.currentTimeMillis() > time) {
  	        time += 1000L;
  	        System.out.println("1 DCPU at " + ops / 1000.0 + " khz, " + tick * 100.0 / total + "% cpu use");
  	        tick = total = ops = 0L;
  	      }
  	    }  			
  		}
  	}).start();
  }
  
  public static void main(String[] args) throws Exception {
  	EmulationEnvironment ee = new EmulationEnvironment();
  }
}
