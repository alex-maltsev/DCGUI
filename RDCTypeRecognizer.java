
public class RDCTypeRecognizer {
	private static int resNum = 0;
	
	// The function returns the recognized RDCType, and also modifies the passed resNum Integer object
	// to reflect the correct residue number.
	static public RDCType recognize(int atom1Res, String atom1Type, int atom2Res, String atom2Type) {
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

		// In case we are still not getting a RDCType, it means that it's an unknown RDC Type.

		return rdcType;
	}
	
	// To be called after the call to recognize(..) to get the correct residue number for the recognized RDC
	static int getResNum() {
		return resNum;
	}
}
