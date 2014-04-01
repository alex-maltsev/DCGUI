import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;


public class DCWrapper {
	private File pdbFile, rdcFile;
	private String outName;
	private String errorMessage;
	private boolean fixingDa = false, fixingRh = false, fixingOrientation = false;
	private float Da, Rh, psi, theta, phi;
	
	private boolean useInitialOrientation = false;
	private float initPsi, initTheta, initPhi;
	
	// When using this constructor, the pdbFile and rdcFile will need to be set before running DC
	public DCWrapper() {
		pdbFile = null;
		rdcFile = null;
	}
	
	public DCWrapper(File pdbFile, File rdcFile) {
		this.pdbFile = pdbFile;
		this.rdcFile = rdcFile;
	}
	
	public void reset() {
		fixingDa = false; 
		fixingRh = false; 
		fixingOrientation = false;
		useInitialOrientation = false;
	}
	
	public void fixDaRh(float Da, float Rh) {
		this.Da = Da;
		this.Rh = Rh;
		fixingDa = true;
		fixingRh = true;
	}

	public void fixDa(float Da) {
		this.Da = Da;
		fixingDa = true;
	}

	public void fixRh(float Rh) {
		this.Rh = Rh;
		fixingRh = true;
	}
	
	public void fixOrientation(float psi, float theta, float phi) {
		this.psi = psi;
		this.theta = theta;
		this.phi = phi;
		fixingOrientation = true;
	}

	public void setInitialOrientation(float psi, float theta, float phi) {
		initPsi = psi;
		initTheta = theta;
		initPhi = phi;
		useInitialOrientation = true;
	}
	
	public void useSVD() {
		fixingDa = false;
		fixingRh = false;
	}
	
	public int runDC() {
		String command = composeCommand();
	//	System.out.println(command);
		
		try {
		    Process p = Runtime.getRuntime().exec(new String[] {"/bin/csh", "-c", command});
		//    String line;
		//    BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		    BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		    
		    int exitCode = p.waitFor();
		    
		    // If DC didn't run successfully then save the content of the error stream
		    if(exitCode != 0) {
		    	String line;
		    	StringBuffer buffer = new StringBuffer();
		        while ((line = input.readLine()) != null) {
		        	buffer.append(line);
		        	buffer.append("\n");
		        }
		     
		        input.close();
		        errorMessage = buffer.toString();
		    }
		    
		    return exitCode;
		} catch (Exception err) {
		    err.printStackTrace();
		}
		
		return -1;  // There was some problem
	}
	
	private String composeCommand() {
		outName = "result_" + System.currentTimeMillis() + ".tab";
		StringBuffer buffer = new StringBuffer("DC -verb");  // Working with verbose output
		
		// Add PDB-file name
		buffer.append(" -pdb ");
		buffer.append(pdbFile.getAbsolutePath());
		
		// Add RDC input file name
		buffer.append(" -inD ");
		buffer.append(rdcFile.getAbsolutePath());
		
		// Add output file name
		buffer.append(" -outD ");
		buffer.append(outName);
		
		// If necessary specify tensor parameters to be fixed
		// Notice that Da and orientation should note be fixed simultaneously!
		if(fixingDa) {
			float scaledDa = Da / 21585.20f; // DC uses fractional Da, rather than Da in Hz
			buffer.append(" -dadr");
			buffer.append(" -da " + String.format("%.6e", scaledDa));
			if(fixingRh) 
				buffer.append(" -dr " + String.format("%.6e", scaledDa*Rh));
		} else if(fixingOrientation) {
			if(fixingRh) {
				// Fixing both orientation and rhombicity
				buffer.append(" -rotrh");
				buffer.append(" -rh " + String.format("%.3f", Rh));
			} else {
				// Fixing orientation only
				buffer.append(" -rot");				
			}
			buffer.append(" -psi " + String.format("%.2f", psi));
			buffer.append(" -theta " + String.format("%.2f", theta));
			buffer.append(" -phi " + String.format("%.2f", phi));
		}
		
		if(fixingDa && useInitialOrientation) {
			buffer.append(" -psi " + String.format("%.2f", initPsi));
			buffer.append(" -theta " + String.format("%.2f", initTheta));
			buffer.append(" -phi " + String.format("%.2f", initPhi));			
		}
		
		return buffer.toString();
	}
	
	public File getResultFile() {
		return new File(outName);
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
}
