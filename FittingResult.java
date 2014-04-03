import java.io.Serializable;


// Class, encompassing all the data coming from RDC fit
public class FittingResult implements Serializable {
	private static final long serialVersionUID = -8729750263450040414L;

	// Fit quality parameters
	public float Qfactor, Rcorr, rms, chi2;
	
	// Alignment tensor parameters
	public float Da, Rh;
	public float psi, theta, phi;
	public float [] Saupe = new float[5];
	public float [][] RotMatrix = new float[3][3];
}
