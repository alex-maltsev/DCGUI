
import java.util.*;
import java.io.*;

public class PDBReader {
	private boolean fileOkay = false;
    private ArrayList<PDBData> atoms = new ArrayList<PDBData>();
    private ArrayList<NHVector> NHVectors = new ArrayList<NHVector>();
    
    private int atomCount = 0;
    private double RAD = 57.29577951;

    public PDBReader() { }
    
    public PDBReader(String inName){
    	 readPDBFile(new File(inName));
    }

    public PDBReader(File in){
         readPDBFile(in);
    }
     
    private void readPDBFile(File file) {
         BufferedReader rstream = null;
         String buf;
         
	     try { // exception occurs on FileInputStream or File
	    	 rstream = new BufferedReader(new FileReader(file));
	     } catch (FileNotFoundException e) { 
	    	 System.out.println("FileNotFoundException = "+e);
	     }
	
	     try {
	    	 while((buf=rstream.readLine())!=null){
	    		 setData(buf);
	    	 }
	     } catch (Exception e) { 
	    	 System.out.println("IO Exception = "+e); 
	     }
	     
	     if(atomCount == 0) {
	    	 System.err.println(" No data found in file " + file.getName());
	    	 return;
	     }
	     
	     fileOkay = true;
	     System.out.println(atomCount + " atoms Read!");
    }

    public boolean isFileOkay() {
    	return fileOkay;
    }
    
    public void save(File file) {
		try {
	    	Writer writer = new BufferedWriter(new FileWriter(file));
	    	for(PDBData atom: atoms)
	    		saveAtom(atom, writer);
	    	
	    	writer.close();
		}  
		catch(IOException ex){
			ex.printStackTrace();
		}		
    }
    
    public void saveNHOnly(File file) {
		try {
	    	Writer writer = new BufferedWriter(new FileWriter(file));
	    	for(PDBData atom: atoms) {
	    		String atomName = atom.atomName.trim();
	    		if(atomName.equals("N") || atomName.equals("HN"))
	    			saveAtom(atom, writer);
	    	}
	    	
	    	writer.close();
		}  
		catch(IOException ex){
			ex.printStackTrace();
		}		
    }
    
    public PDBReader clone() {
    	PDBReader newReader = new PDBReader();
    	
    	// Making a deep copy of atoms
    	for(PDBData atom: atoms)
    		newReader.atoms.add(atom.clone());
    	
    	newReader.atomCount = atomCount;
    	newReader.fileOkay = fileOkay;
    	
    	return newReader;
    }
    
    public PDBReader cloneNHOnly() {
    	PDBReader newReader = new PDBReader();
    	int atomCounter  = 0;
    	
    	// Making a deep copy of N and HN atoms only, and counting them
    	for(PDBData atom: atoms) {
    		String atomName = atom.atomName.trim();
    		if(atomName.equals("N") || atomName.equals("HN")) {
    			newReader.atoms.add(atom.clone());
    			atomCounter++;
    		}
    	}
    	
    	newReader.atomCount = atomCounter;
    	if(atomCounter > 0)
    		newReader.fileOkay = true;
    	else
    		newReader.fileOkay = false;
    	
    	return newReader;
    }

    public void setupNHVectors() {
    	// First create a map from residue number to the index of corresponding HN atom
    	HashMap<Integer, Integer> HAtoms = new HashMap<Integer, Integer>();
    	PDBData atom;
    	for(int i=0; i<atoms.size(); i++) {
    		atom = atoms.get(i);
    		if(atom.atomName.trim().equals("HN"))
    			HAtoms.put(atom.resID, i);
    	}
    	
    	NHVectors = new ArrayList<NHVector>();
    	Vector bond, n1, n2;
    	for(int i=0; i<atoms.size(); i++) {
    		atom = atoms.get(i);
    		// If we find N atom and there is a HN atom on the same residue then we save the
    		// corresponding NH vector in the list
    		if(atom.atomName.trim().equals("N") && HAtoms.containsKey(atom.resID)) {
    			int Hindex = HAtoms.get(atom.resID);
    			PDBData Hatom = atoms.get(Hindex);
    			NHVector vec = new NHVector();
    			//Save the coordinates of the NH vector
    			bond = new Vector(Hatom.x - atom.x, Hatom.y - atom.y, Hatom.z - atom.z);
    			vec.bond = bond;
    			
     			// Save the coordinates of the N atom
    			vec.Ncoords = new Vector(atom.x, atom.y, atom.z);
    			
     			// Save the indexes of the N and HN atoms
    			vec.indexH = Hindex;
    			vec.indexN = i;
    			vec.resID = atom.resID;
    			
    			// Set the vectors perpendicular to the bond vector. 
    			// Make their lengths equal to that of the bond vector, to simplify future calculations
    			float length = bond.norm(); // bond length
    			float x = bond.x, y = bond.y, z = bond.z;  // Convenience variables to shorten formulas below
    			
    			if(y == 0.0f && z == 0.0f) {  // First deal with the special case to avoid devision by zero
    				n1 = new Vector(0, length, 0);
    				n2 = new Vector(0, 0, length);
    			} else {
        			n1 = new Vector(0, z, -y);
        			n1.multiply(length / n1.norm()); // Set the length of n1 properly
        			
        			n2 = new Vector(-y*y - z*z, x*y, x*z);
        			n2.multiply(length / n2.norm()); // Set the length of n2 properly
    			}
    			
    		//	System.out.printf("Norms: bond=%.3f, n1=%.3f, n2=%.3f\n", bond.norm(), n1.norm(), n2.norm());
    		//	System.out.printf("Dot products: n1*v=%.3f, n2*v=%.3f, n1*n2=%.3f\n", n1.dot(bond), n2.dot(bond), n1.dot(n2));
    			vec.norm1 = n1;
    			vec.norm2 = n2;
    			
    			NHVectors.add(vec);
    		}
    	}
    	
    	System.out.println(NHVectors.size() + " NH vectors were found");
     }
    
    // This function takes the input in degrees and adds structural noise to HNs of this structure
    // The noise can be removed by calling removeNHNoise()
    public void addNHNoise(double sigma) {
    	// But then internally we turn sigma into radians, of course
    	sigma = sigma * Math.PI/180;
    	// Find the maximum of the probability distribution for the deviation angle
    	final int numSteps = 400;
    	double max = 0, step = 3*sigma / numSteps, dist;
    	double angle = step;
    	while((dist = probDistrib(angle, sigma)) > max) {
    		max = dist;
    		angle += step;
    	}
     	
    	// Create a copy of this structure
    	PDBReader pdb = this.clone();
    	// Go through all the NH vectors (that were supposed to be setup by now!!!)
    	// calculate their rotated versions, and modify the cloned structure.
		double Pbeta, beta, phi;
		Vector newBond = new Vector(), Hcoords = new Vector();
    	for(NHVector vec: NHVectors) {
    		// Generate beta (angle of deviation from the original bond vector)
    		// using rejection sampling
    		do {
    			beta = Math.random() * 3*sigma; // Generate beta within the range of (0, 3*sigma)
    			Pbeta = Math.random() * max; // Generate Pbeta within the range (0, Pmax)
    		} while(Pbeta > probDistrib(beta, sigma)); // If the point lies above the curve then try again
    		// Generate phi (angle of rotation around the bond vector) in the range (0, 2pi)
    		phi = Math.random()*2*Math.PI;
    		
    		newBond.setTo(vec.bond);
    		newBond.multiply((float)Math.cos(beta));
    		newBond.add(vec.norm1.times((float)(Math.sin(beta)*Math.cos(phi))));
    		newBond.add(vec.norm2.times((float)(Math.sin(beta)*Math.sin(phi))));
    		
    		// Set the positions of the H atom based on the N position and the new bond vector
    		Hcoords.setTo(vec.Ncoords);
    		Hcoords.add(newBond);
    		PDBData Hatom = atoms.get(vec.indexH);
    		Hatom.x = Hcoords.x;
    		Hatom.y = Hcoords.y;
    		Hatom.z = Hcoords.z;    		
    		
    	//	float cosBeta = newBond.dot(vec.bond) / (newBond.norm()*vec.bond.norm());
    	//	System.out.printf("Angle %.3f degrees; Norms: %.3f, %.3f\n", Math.acos(cosBeta)*180.0f/Math.PI, vec.bond.norm(), newBond.norm());
    	//	System.out.printf("%.3f\n", Math.acos(cosBeta)*180.0f/Math.PI);
    	}
    }
    
    public void removeNHNoise() {
    	Vector Hcoords = new Vector();
    	for(NHVector vec: NHVectors) {    		
    		// Set the positions of the H atom based on the N position and the original NH bond vector
    		Hcoords.setTo(vec.Ncoords);
    		Hcoords.add(vec.bond);
    		PDBData Hatom = atoms.get(vec.indexH);
    		Hatom.x = Hcoords.x;
    		Hatom.y = Hcoords.y;
    		Hatom.z = Hcoords.z;    		
    	}

    }
    
    // Both arguments are in radians
    private double probDistrib(double beta, double sigma) {
    	return Math.sin(beta)*Math.exp(-beta*beta / (sigma*sigma));
    }
    
    private void saveAtom(PDBData atom, Writer writer) throws IOException {
    	String temp;
		temp = String.format("ATOM %6d %4s %3s %5d", atom.atomNumber, atom.atomName, atom.resName, atom.resID);
		writer.write(temp);
		temp = String.format("     %7.3f %7.3f %7.3f %5.2f %5.2f\n", atom.x, atom.y, atom.z, atom.occupancy, atom.tempFactor);
		writer.write(temp);
    }
    
    void setData(String line){
         int len = line.length();
         if(len <=6 ) return;

         String header = line.substring(0, 4);
         if(header.compareTo("ATOM") != 0) return;

         atomCount++;
         PDBData temp = new PDBData();

         try{
             temp.atomNumber = Integer.parseInt((line.substring(7,11)).trim() );
           //  temp.atomName   = (line.substring(12,16)).trim();
             temp.atomName   = line.substring(12,16);
         //    temp.altLoc     = (line.substring(17,17)).trim();
             temp.resName    = (line.substring(17,20)).trim();
             temp.chainID    = (line.substring(22,22)).trim();
             temp.resID      = Integer.parseInt((line.substring(23,26)).trim() );
             temp.x          = Float.parseFloat((line.substring(31,38)).trim() );
             temp.y          = Float.parseFloat((line.substring(39,46)).trim() );
             temp.z          = Float.parseFloat((line.substring(47,54)).trim() );
             temp.occupancy  = Float.parseFloat((line.substring(55,60)).trim() );
             temp.tempFactor = Float.parseFloat((line.substring(61,66)).trim() );
             temp.element    = (line.substring(77,78)).trim();
             temp.charge     = (line.substring(79,80)).trim();
         } catch (Exception e) {}

         atoms.add(temp);
    }

class PDBData
{
     int atomNumber;  // col 2, 7-11
     String atomName; // col 3, 13-16
     String altLoc;   // col 4, 17
     String resName;  // col 5, 18-20
     String chainID;  // col 6, 22
     int resID;       // col 7, 23-26
     float x,y,z;     // col 8-10,
                      //         31-38, 39-46, 47-54
     float occupancy; // col 11, 55-60
     float tempFactor;// col 12, 61-66
     String element;  // col 13, 77-78
     String charge;   // col 14, 79-80

     public PDBData()
     {
         atomNumber=9999; resID=9999;
         atomName = null;  altLoc = null;  resName = null;
         chainID = null;  element = null;   charge = null;
     }
     
     public PDBData clone() {
    	 PDBData temp = new PDBData();
    	 temp.atomNumber = atomNumber;
    	 temp.resID = resID;
    	 temp.x = x;
    	 temp.y = y;
    	 temp.z = z;
    	 temp.occupancy = occupancy;
    	 temp.tempFactor = tempFactor;
    	 
    	 // I don't clone the strings because the won't need to be modified
    	 temp.atomName = atomName;
    	 temp.altLoc = altLoc;
    	 temp.resName = resName;
    	 temp.chainID = chainID;
    	 temp.element = element;
    	 temp.charge = charge;
    	 
    	 return temp;
     }
   }

	class NHVector {
		Vector bond; // Coordinates of the NH vector
		Vector Ncoords; // Coordinates of the N atom
		Vector norm1, norm2; // Coordinates of the two normals to the bond vector
		
		int indexN, indexH;
		int resID;
	}
}