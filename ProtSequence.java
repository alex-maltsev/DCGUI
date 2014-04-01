import java.io.Serializable;
import java.util.ArrayList;


public class ProtSequence implements Serializable {
	private static final long serialVersionUID = 4952107422581697967L;
	
	ArrayList<String> sequence;
	static String allowedCodes = "ACDEFGHIKLMNPQRSTVWY";  // All one-letter codes
	// Array of three-letter codes. Note that the order matches that of one-letter codes
	static String [] threeLetterCodes = {
		"Ala", "Cys", "Asp", "Glu", "Phe", "Gly", "His", "Ile", "Lys", "Leu", "Met", "Asn", "Pro", "Gln", "Arg", "Ser", "Thr", "Val", "Trp", "Tyr" 
	};
	
	ProtSequence() {
		sequence = new ArrayList<String>();
	}
	
	// Construct sequence based on the provided string of one-letter code
	ProtSequence(String str) {
		sequence = new ArrayList<String>();
		setSequence(str);
	}
	
	public void setSequence(String str) {
		if(str == null) return;
		
		sequence.clear();
		str = str.toUpperCase();
		for(int i=0; i<str.length(); i++) {
			String c = String.valueOf(str.charAt(i));
			if(allowedCodes.contains(c))
				sequence.add(c);  // If the code is among allowed codes then add it to sequence. Otherwise - continue.
		}		
	}
	
	// Return sequence as a string
	public String getSequence() {
		StringBuilder builder = new StringBuilder();
		for(String str: sequence)
			builder.append(str);
		return builder.toString();
	}
	
	// Return the number of amino acids in the sequence
	public int getLength() {
		return sequence.size();
	}
	
	// Return the one-letter code of the amino acid at position 'pos' 
	public String getAcidAt(int pos) {
		if(sequence.size() == 0) return "A"; // If sequence has not been initialized then return alanine
		
		pos--; // To correct for the fact that residue number is 1-based rather than 0-based
		if(pos < sequence.size())
			return sequence.get(pos);  // If position is within the provided sequence then return the corresponding AA
		else
			return "A";  // Otherwise return alanine
	}
	
	// Return the three-letter code of the amino acid at position 'pos'
	public String getAACodeAt(int pos) {
		return convertCode(getAcidAt(pos));
	}
	
	// Convert one-letter code string into the corresponding three-letter code string
	static String convertCode(String c) {
		if(c == null || c.length() != 1) return null; // Make sure that it's a one-letter string
		c = c.toUpperCase();
		int pos = allowedCodes.indexOf(c);
		if(pos == -1) 
			return null; // Make sure that it is a valid code
		else
			return threeLetterCodes[pos];
	}
}
