import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;


public class RDCSet implements Serializable {
	private static final long serialVersionUID = -8571699570805326856L;
	
	RDCType type;
	ArrayList<RDC> rdcs;
	ProtSequence seq;
	
	RDCSet() {
		rdcs = new ArrayList<RDC>();
		type = RDCType.NH; // Amide RDC is the default
	}
	
	// Sets the RDC type for the set, 
	// and resets uncertainties and weights for all RDCs within the set. 
	public void setType(RDCType type) {
		this.type = type;
		
		float uncert = (float)type.uncert;
		for(RDC rdc: rdcs)
			rdc.uncert = uncert;
	}
	
	public void setType(String typeStr) {
		setType(RDCType.RDCTypeFromString(typeStr));		
	}
	
	public String getTypeString() {
		return type.name;
	}
	
	// Returns the factor to multiply these RDCs by to scale them to NH
	public double getNHScaling() {
		return type.getNHScaling();
	}
	
	public void addRDC(int resNum, float value) {
		RDC rdc = new RDC(resNum, value);
		rdcs.add(rdc);
	}
	
	public void addRDC(int resNum, float value, float uncert) {
		RDC rdc = new RDC(resNum, value);
		rdc.uncert = uncert;
		rdcs.add(rdc);
	}

	public void addRDC(int resNum, float value, float uncert, String atom1, String atom2) {
		RDC rdc = new RDC(resNum, value);
		rdc.atom1 = atom1;
		rdc.atom2 = atom2;
		rdc.uncert = uncert;
		rdcs.add(rdc);
	}

	public void addRDC(RDC rdc) {
		rdcs.add(rdc);
	}
	
	// Return RDC at given position within the set
	public RDC get(int i) {
		if(i>rdcs.size()) 
			return null;
		else
			return rdcs.get(i);
	}
	
	public int getCount() {
		return rdcs.size();
	}
	
	public void setSequence(ProtSequence seq) {
		this.seq = seq;
	}
	
	// This is only used when loading state from file
	public ProtSequence getSequence() {
		return seq;
	}
	
	public void outputFormatted(Writer writer) throws IOException {
		String aaCode1, aaCode2;
		for(RDC rdc: rdcs) {
			if(!rdc.isUsed) continue; // Skip the RDC if it's not supposed to be used
			
			if(seq == null) {
				aaCode1 = "ALA";
				aaCode2 = "ALA";
			}
			else { 
				aaCode1 = seq.getAACodeAt(rdc.resNum).toUpperCase();
				aaCode2 = seq.getAACodeAt(rdc.resNum + type.resNumDelta).toUpperCase();
			}
			
			String atom1, atom2;
			if(type.isSimple)
			{
				atom1 = type.atom1; atom2 = type.atom2;
			}
			else
			{
				atom1 = rdc.atom1; atom2 = rdc.atom2;
			}
			
			float weight = 1.0f / rdc.uncert;
			
			String temp;
			temp = String.format("%5d %6s %6s", rdc.resNum, aaCode1, atom1);
			writer.write(temp);
			temp = String.format("%6d %6s %6s", rdc.resNum + type.resNumDelta, aaCode2, atom2);
			writer.write(temp);
			temp = String.format("%10.3f %9.3f %.2f\n", rdc.value, rdc.uncert, weight);
			writer.write(temp);
		}
	}
}
