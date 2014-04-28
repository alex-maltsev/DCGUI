import java.io.Serializable;


public class RDC implements Serializable {
	private static final long serialVersionUID = -8731500096817821107L;

	float value,  // Experimental value of RDC (Hz)
			uncert, // Experimental uncertainty (Hz)
			predValue; // Value predicted from fitting
	
	int resNum; // Residue number
	boolean isUsed; // Flag showing whether this RDC is to be used in fitting
	boolean wasPredicted;
	
	// The names of atoms involved in the RDC.
	// Particularly important when loading unusual RDCs from a 'full' input file.
	String atom1, atom2; 
	
	RDC(int resNum, float value) {
		this.resNum = resNum;
		this.value = value;
		
		// The below values are set temporarily. They should be properly set by manipulating
		// RDCSet object housing this RDC
		uncert = 1.0f;
		predValue = 0.0f;
		isUsed = true;
		wasPredicted = false;
	}
}
