
public class RDCTypeRecognizer {
	private static int resNum = 0;
	
	// The function returns the recognized RDCType, and also modifies the passed resNum Integer object
	// to reflect the correct residue number.
	static public RDCType recognize(int atom1Res, String atom1Type, int atom2Res, String atom2Type) {
	//	System.out.printf("Recognizing %d, %s, %d, %s\n", atom1Res, atom1Type, atom2Res, atom2Type);
		
		RDCType rdcType = null;
		
		// Figure out the RDC type, and the corresponding residue number
		if(atom1Res <= atom2Res) {
			resNum = atom1Res;
			rdcType = RDCType.RDCTypeFromAtoms(atom1Type, atom2Type);
			// In case when both atoms are on the same residue we may still have a problem
			if(rdcType == null)
				rdcType = RDCType.RDCTypeFromAtoms(atom2Type, atom1Type);
			
		} else {
			resNum = atom2Res;
			rdcType = RDCType.RDCTypeFromAtoms(atom2Type, atom1Type);
		}

		// In case we are still not getting a RDCType, it means that it's not one of the simple types
		// First see if it is CAHA for glycine
		if(rdcType == null)
			rdcType = recognizeCAHA(atom1Res, atom1Type, atom2Res, atom2Type);

		if(rdcType == null)
			rdcType = recognizeCBHB(atom1Res, atom1Type, atom2Res, atom2Type);

		return rdcType;
	}
	
	// To be called after the call to recognize(..) to get the correct residue number for the recognized RDC
	static int getResNum() {
		return resNum;
	}
	
	
	static private RDCType recognizeCAHA(int atom1Res, String atom1Type, int atom2Res, String atom2Type) {
		// This can't be CAHA if the two atoms are on different residues
		if(atom1Res != atom2Res)
			return null;
		
		resNum = atom1Res; // Save the residue number
		
		// Make sure that at least one of the atoms is CA
		String nonCAType; // Save the type of the atom coupled with CA
		
		if(atom1Type.equalsIgnoreCase("CA")) 
		{
			nonCAType = atom2Type;
		} 
		else if(atom2Type.equalsIgnoreCase("CA")) 
		{
			nonCAType = atom1Type;
		}
		else
			return null; // None of the atoms is CA, so bail here.
		
		// Now for the type to be CAHA the non-CA atom must be HA2, HA3, or HA#
		// Or simply something of the type HA*
		if(nonCAType.substring(0, 2).equalsIgnoreCase("HA"))
			return RDCType.CAHA;
		else
			return null;
	}
	
	
	static private RDCType recognizeCBHB(int atom1Res, String atom1Type, int atom2Res, String atom2Type) {
		// This can't be CAHA if the two atoms are on different residues
		if(atom1Res != atom2Res)
			return null;
		
		resNum = atom1Res; // Save the residue number
		
		// Make sure that at least one of the atoms is CA
		String nonCBType; // Save the type of the atom coupled with CA
		
		if(atom1Type.equalsIgnoreCase("CB")) 
		{
			nonCBType = atom2Type;
		} 
		else if(atom2Type.equalsIgnoreCase("CB")) 
		{
			nonCBType = atom1Type;
		}
		else
			return null; // None of the atoms is CB, so bail here.
		
		// Now for the type to be CAHA the non-CB atom must be HB, HB2, HB3, or HB#
		// Or simply something of the type HB*
		if(nonCBType.substring(0, 2).equalsIgnoreCase("HB"))
			return RDCType.CBHB;
		else
			return null;
	}

}
