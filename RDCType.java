import java.io.Serializable;


public enum RDCType implements Serializable {
	NH("NH", "N", "HN", 0, -21585.20, 1.000),
	CAHA("CAHA", "CA", "HA", 0, 44539.47, 1.000),
	CACO("CACO", "CA", "C", 0, 4284.77, 0.200),
	NCO("NCO", "C", "N", 1, -2609.05, 0.125),
	HNCO("HNCO", "C", "HN", 1, 6666.07, 0.333),
	CACB("CACB", "CA", "CB", 0, 4200.00, 0.200), // Notice that coupling constant varies for this type!
	CBCG("CBCG", "CB", "CG", 0, 4285.00, 0.200);

	String name;	 // String representation of the type name
	String atom1, atom2;
	int resNumDelta;  // Difference in the residue number of the two atoms. Only positive by definition!
	double coupling;  // Full dipolar coupling in Hz
	double uncert; // The default experimental uncertainty for this RDC type
	
	private static final long serialVersionUID = -8571699570L;

	RDCType(String name, String atom1, String atom2, int resNumDelta, double coupling, double uncert) {
		this.name = name;
		this.atom1 = atom1;
		this.atom2 = atom2;
		this.resNumDelta = resNumDelta;
		this.coupling = coupling;
		this.uncert = uncert;
	}
	
	// Returns the factor to multiply these RDCs by to scale them to NH
	public double getNHScaling() {
		return Math.abs(RDCType.NH.coupling / this.coupling);
	}
	
	public String toString() {
		return name + " : " + Double.toString(coupling);
	}
	// Return the RDCType object given its name as a String
	public static RDCType RDCTypeFromString(String str) {
		for(RDCType type: RDCType.values()) {
			if(type.name.equalsIgnoreCase(str))
				return type;
		}
		
		return null;
	}

	// Return the RDCType object given its name as a String
	public static RDCType RDCTypeFromAtoms(String atom1, String atom2) {
		for(RDCType type: RDCType.values()) {
			if(type.atom1.equalsIgnoreCase(atom1) && type.atom2.equalsIgnoreCase(atom2))
				return type;
		}
		
		return null;
	}
}
