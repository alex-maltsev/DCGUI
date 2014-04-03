import java.io.Serializable;
import java.util.ArrayList;

public class AlignmentMedium  implements Serializable {
	private static final long serialVersionUID = 5598148801041899642L;
	
	private ArrayList<RDCSet> sets;
	public String name;
	
	public boolean fittingWasDone = false;
	public FittingResult fittingResult;
	
	AlignmentMedium(String name) {
		sets = new ArrayList<RDCSet> ();
		this.name = name;
	}
	
	public void addRDCSet(RDCSet set) {
		sets.add(set);
	}
	
	public int getCount() {
		return sets.size();
	}
	
	public ArrayList<RDCSet> getRDCSets() {
		return sets;
	}
	
	public RDCSet get(int index) {
		if(index >= sets.size())
			return null;
		else
			return sets.get(index);
	}
	
	public void remove(int index) {
		sets.remove(index);
	}
}
