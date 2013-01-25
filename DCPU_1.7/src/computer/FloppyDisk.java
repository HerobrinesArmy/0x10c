package computer;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FloppyDisk {
		public char[] data = new char[737280];

		private boolean writeProtected;
		
		public FloppyDisk(File file, boolean writeProtected) throws Exception {
			this.writeProtected = writeProtected;
			DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
			try {
	      for (int i = 0; ; i++)
	        data[i] = dis.readChar();
	    }
	    catch (IOException e) {
	      dis.close();
	    }
		}		
		
		public FloppyDisk() {	
		}
		
		public boolean isWriteProtected() {
			return writeProtected;
		}
		
		public void setWriteProtected(boolean writeProtected) {
			this.writeProtected = writeProtected;
		}
	}