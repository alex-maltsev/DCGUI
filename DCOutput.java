import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;


public class DCOutput {
	public FittingResult fittingResult;
	
	private File resultFile;
	private AlignmentMedium medium;
	private RDCSet currentSet;
	private int setIndex, rdcIndex; // Indexes used to track where to put predicted RDCs
	
	private boolean loadedSuccessfully = false; 
	
	public DCOutput() {
		fittingResult = new FittingResult();
	}
	
	// This function loads the results from DC output file. The meta results such as Q-factor
	// are stored in respective member variables. The predicted RDCs are copied into the AlignmentMedium object
	// passed as the second argument. IMPORTANT: the AlignmentMedium object must be the same that was used to
	// generate the DC input!
	public void load(File file, AlignmentMedium medium) {
		this.medium = medium;
		resultFile = file;
		if(medium != null) currentSet = medium.get(0);
		setIndex = 0;
		rdcIndex = 0;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			Scanner scanner;
			
			while ((line = br.readLine()) != null) {
				scanner = new Scanner(line);
				if(scanner.hasNextInt()) {
					if(medium == null) break; // Apparently not interested in RDC data itself - break out of the loop
					loadRDC(scanner); // actual RDC data
				} else if(scanner.hasNext("DATA")) {
					loadMetaInfo(scanner); // meta info, such as Q-factor etc
				} else
					continue; // Empty line or comment
			}
			br.close();
		} catch (IOException e) {
			loadedSuccessfully = false;
			e.printStackTrace();
			return;
		}
		
		loadedSuccessfully = true;
		
		// Pass the fitting result to the alignment medium if necessary
		if(medium != null) {
			medium.fittingResult = fittingResult;
			medium.fittingWasDone = true;
		}
	}
	
	// From the provided DC output file load meta data only
	public void loadMeta(File file) {
		load(file, null);
	}

	public boolean wasLoadedSuccessfully() {
		return loadedSuccessfully;
	}
	
	public File getResultFile() {
		return resultFile;
	}
	
	private void loadRDC(Scanner scanner) {
		// First read in the relevant info about the predicted RDC
		int resNum = scanner.nextInt(); // Get the residue number for the predicted RDC
		// Skip the next 5 columns (integers and strings only)
		for(int i=0; i<5; i++)
			scanner.next("\\w+");
		
		// Skip 2 more columns (floating point numbers)
		scanner.nextFloat();
		scanner.nextFloat();
		
		// Finally got to the good stuff. Grab the predicted RDC value.
		float predValue = scanner.nextFloat();
		
		// Now find the correct RDC object to put the predicted value into
		// What can happen is if experimental RDC was given for atoms that don't exist in PDB
		// then this RDC will be missing in the output file
		RDC rdc;
		do {
			rdc = getNextRDC();			
			if(rdc == null) return; // Something is wrong with the input - stop here
			rdc.wasPredicted = false; // Mark that the RDC wasn't predicted
		} while(rdc.resNum < resNum);
				
		rdc.predValue = predValue;
		rdc.wasPredicted = true; // Notice that this overwrites the false value assigned in the loop
	}
	
	private RDC getNextRDC() {
		do {
			// If rdcIndex is past the number of RDCs in the current set then
			// try to move to the next set
			if(rdcIndex == currentSet.getCount()) {
				setIndex++;
				// If we are somehow advancing past the number of sets then return null
				// because this should never happen
				if(setIndex == medium.getCount()) return null;
				
				currentSet = medium.get(setIndex);
				rdcIndex = 0; // Reset the rdcIndex to the start of the new current set
			}
			
			// If we are currently looking at a used RDC then get out of the loop
			if(currentSet.get(rdcIndex).isUsed) break;
			rdcIndex++;
		} while(true);
		
		RDC rdc = currentSet.get(rdcIndex);
		rdcIndex++;
		
		return rdc;
	}
	
	private void loadMetaInfo(Scanner scanner) {
		scanner.next("DATA"); // Skip DATA keyword
		
		String key = scanner.next("\\w+");
		if(key.equals("Q_FACTOR")) {
			fittingResult.Qfactor = scanner.nextFloat();
		} else if(key.equals("CORR_R")) {
			fittingResult.Rcorr = scanner.nextFloat();
		} else if(key.equals("RMS")) {
			fittingResult.rms = scanner.nextFloat();
		} else if(key.equals("Chi2")) {
			fittingResult.chi2 = scanner.nextFloat();
		} else if(key.equals("Da_HN")) {
			fittingResult.Da = scanner.nextFloat();
		} else if(key.equals("Rhombicity")) {
			fittingResult.Rh = scanner.nextFloat();
		} else if(key.equals("SAUPE")) {
			for(int i=0; i<5; i++)
				fittingResult.Saupe[i] = scanner.nextFloat();
		} else if(key.equals("ROTATION_MATRIX")) {
			int row = scanner.nextInt() - 1; // Read in the matrix row number
			for(int i=0; i<3; i++)
				fittingResult.RotMatrix[row][i] = scanner.nextFloat();
		} else if(key.equals("PSI")) {
			fittingResult.psi = scanner.nextFloat();
		} else if(key.equals("THETA")) {
			fittingResult.theta = scanner.nextFloat();
		} else if(key.equals("PHI")) {
			fittingResult.phi = scanner.nextFloat();
		}
	}
}
