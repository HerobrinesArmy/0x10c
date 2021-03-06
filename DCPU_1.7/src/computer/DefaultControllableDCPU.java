package computer;

public class DefaultControllableDCPU extends DCPU {
	private boolean keepAlive;

	@Override
	public void interrupt(char a) {
		if (keepAlive) {
			super.interrupt(a);
		}
	}
	
	public void run() {
		keepAlive = true;
		(new Thread() {
			@Override
			public void run() {
//				opcounts = new int[64];
				long ops = 0L;
		    int hz = 1000 * khz;
		    int cyclesPerFrame = hz / 60 + 1;

		    long nsPerFrame = 16666666L;
		    long nextTime = System.nanoTime();

		    double tick = 0;
		    double total = 0;

		    long time = System.currentTimeMillis();
		    while (keepAlive) {
		      long a = System.nanoTime();
		      while (System.nanoTime() < nextTime) {
		        try {
		          Thread.sleep(1L);
		        } catch (InterruptedException e) {
		          e.printStackTrace();
		        }
		      }
		      long b = System.nanoTime();
		      while (cycles < cyclesPerFrame) {
		        tick();
		      }

		      tickHardware();
		      cycles -= cyclesPerFrame;
		      long c = System.nanoTime();
		      ops += cyclesPerFrame;
		      nextTime += nsPerFrame;

		      tick += (c - b) / 1000000000.0;
		      total += (c - a) / 1000000000.0;

		      while (System.currentTimeMillis() > time) {
		        time += 1000L;
//		        System.out.println("1 DCPU at " + ops / 1000.0 + " khz, " + tick * 100.0 / total + "% cpu use");
		        tick = total = ops = 0L;
		      }
		    }
		    pc = 0;
		    sp = 0;
		    ex = 0;
		    ia = 0;
		    registers = new char[8];
		    cycles = 0;
		    stop = false;
		    isSkipping = false;
		    isOnFire = false;
		    queueingEnabled = false;
		    interrupts = new char[256];
		    ip = 0;
		    iwp = 0;
		    for (DCPUHardware hw : hardware) {
		    	hw.powerOff();
		    }
			}
		}).start();
	}
	
	public void stop() {
		keepAlive = false;
//		System.out.flush();
//		System.out.println("OpCodes used:");
//		for (int i = 0; i < 64; i++) {
//			if (opcounts[i] > 0) {
//				if (i > 31) {
//					System.out.println(OpCodes.special.getName(i-32));
//				} else {
//					System.out.println(OpCodes.basic.getName(i));
//				}
//			} else {
//				System.out.println();
//			}
//		}
	}
}
