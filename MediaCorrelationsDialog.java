import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class MediaCorrelationsDialog extends JDialog {
	private static final long serialVersionUID = -2690516807291160809L;
	private ArrayList<AlignmentMedium> media;

	private String[] columnNames; // = {"Used", "Residue", "RDC (Hz)", "Predicted (Hz)", "Uncert (Hz)", "Quality" };
	private int colCount;
	private JTable table;
	private Object[][] tableData;
	JScrollPane scrollPane;

	// Constants used to dynamically resize the dialog depending on the content
	private static final int BORDER_WIDTH = 10;
	private static final int COLUMN_WIDTH = 80;
	private static final int ROW_HEIGHT = 25;
	private static final int EXTRA_HEIGHT = 5;
	private static final int EXTRA_WIDTH = 5;
	private static final int SCROLL_BAR_HEIGHT = 20;
	private static final int MAX_DIALOG_WIDTH = 500;

	public MediaCorrelationsDialog(Frame owner, ArrayList<AlignmentMedium> media) {
		super(owner, "Media correlations", true);
		
		this.media = media;
				
		JPanel contentPane = (JPanel)getContentPane();
		contentPane.setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
		
		// Init table column names with media names
		// Leaving the first name empty
		colCount = media.size() + 1;
		columnNames = new String[colCount];
		columnNames[0] = "";
		for(int i=0; i<media.size(); i++)
			columnNames[i+1] = media.get(i).name;
		
		prepareTableData();
		
		table = new JTable(new CorrelationsTableModel());
		scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	//	table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		// Force the data to be displayed with three digits after the decimal point
		// Set the column widths
		TableColumnModel m = table.getColumnModel();
		CellRenderer renderer = new CellRenderer(3);
		for(int i=0; i<=media.size(); i++) {
			m.getColumn(i).setCellRenderer(renderer);
			m.getColumn(i).setMinWidth(COLUMN_WIDTH);
			m.getColumn(i).setMaxWidth(COLUMN_WIDTH);
		}

		// Set the row height or all rows
		table.setRowHeight(ROW_HEIGHT);
		
		scrollPane.setColumnHeader(new JViewport() {
		      @Override 
		      public Dimension getPreferredSize() {
		        Dimension d = super.getPreferredSize();
		        d.height = ROW_HEIGHT;
		        return d;
		      }
		    });
		
		// Center the table headers
		TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
		JLabel headerLabel = (JLabel) headerRenderer;
		headerLabel.setHorizontalAlignment(JLabel.CENTER);

		// Give the column with media names the same styling as the header
		m.getColumn(0).setCellRenderer(headerRenderer);

		// Show grid lines in the table
		table.setShowGrid(true);
		table.setGridColor(Color.lightGray);
		
		contentPane.add(scrollPane);
		
		// Size and position the dialog appropriately
		Point loc = owner.getLocation();
		loc.x += 50;
		loc.y += 50;
		this.setLocation(loc);
		
		// Calculated the scroll pane width and height to accommodate the table without scrolling
		int width = (1 + media.size())*COLUMN_WIDTH + EXTRA_WIDTH;
		int height = (1 + media.size())*ROW_HEIGHT + EXTRA_HEIGHT;
		
		// If the required width is too large then cap it at the maximum value
		if(width > MAX_DIALOG_WIDTH) {
			width = MAX_DIALOG_WIDTH;
			height += SCROLL_BAR_HEIGHT; // Compensate for the appearance of the horizontal scroll bar
		}
				
		scrollPane.setPreferredSize(new Dimension(width, height));
		
		this.pack();
	}
	
		
	private void prepareTableData() {
		int rowCount = media.size();
		
		// First allocate the 2D array
		tableData = new Object[rowCount][];
		for(int i=0; i<rowCount; i++)
			tableData[i] = new Object[colCount];
		
		// Remember that JTable takes Objects.
		for(int i=0; i<rowCount; i++) {
			AlignmentMedium medium = media.get(i);
			tableData[i][0] = medium.name;
			
			for(int column = 1; column <= rowCount; column++)
				tableData[i][column] = calculateCorrelation(medium, media.get(column-1));
		}
	}
	
	
	// This function will return a Float with the value of cos(theta) if both media have been fitted before,
	// and a String denoting "no value" otherwise
	private Object calculateCorrelation(AlignmentMedium medium1, AlignmentMedium medium2) {
		if(!medium1.fittingWasDone || !medium2.fittingWasDone)
			return "--";
		
		float [] S1 = makeSaupeVector(medium1.fittingResult.Saupe);
		float [] S2 = makeSaupeVector(medium2.fittingResult.Saupe);
		
		// Calculate the dot-product of the two 5D vectors
		float result = 0;
		for(int i=0; i<5; i++)
			result += S1[i]*S2[i];
		
		// Get the cos(theta) value
		result = result / (Norm(S1) * Norm(S2));
				
		return new Float(result);
	}
	
	// Create proper 5D vector representation of the alignment tensor
	// based on DC produced 5 Saupe coefficients
	/*
	 * The five values reported in the SAUPE line of the table
	#     correspond to the five coefficients c1 ... c5 which define
	#     the 3x3 order matrix S:
	#
	#      Sxx = -1/2(c1 - c2)  Syy = -1/2(c1 + c2)  Szz = c1
	#      Sxy = Syz = c3       Sxz = Szx = c4       Syz = Szy = c5	
	 */	
	private float[] makeSaupeVector(float[] C) {
		float [] S = new float[5];
		final double sqrt3 = Math.sqrt(3.0);
		
		S[0] = C[0];
		S[1] = (float)(C[1] / sqrt3);
		S[2] = (float)(2.0*C[2] / sqrt3);
		S[3] = (float)(2.0*C[3] / sqrt3);
		S[4] = (float)(2.0*C[4] / sqrt3);
		
		return S;
	}
	
	
	// Calculate the norm of the given 5D vector
	private float Norm(float [] S) {
		float result = 0;
		
		for(int i=0; i<5; i++)
			result += S[i]*S[i];
			
		return (float)Math.sqrt(result);
	}
	
	////  Table model /////////////////////////////////
	class CorrelationsTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 8689703353697168843L;
		
		public int getColumnCount() {
	        return columnNames.length;
	    }

	    public int getRowCount() {
	        return tableData.length;
	    }

	    public String getColumnName(int col) {
	        return columnNames[col];
	    //	return null;
	    }

	    public Object getValueAt(int row, int col) {
	        return tableData[row][col];
	    }

	    public boolean isCellEditable(int row, int col) {
	        return false;
	    }	    
	}
	////// End table model //////////////////////////////////////

	public class CellRenderer extends DefaultTableCellRenderer
	{
		private DecimalFormat formatter;

		/*
		 *   Use the specified formatter to format the Object
		 */
		public CellRenderer(int numDigits)
		{
			formatter = new DecimalFormat();
			formatter.setMaximumFractionDigits(numDigits);
			formatter.setMinimumFractionDigits(numDigits);
			setHorizontalAlignment( SwingConstants.CENTER);
		}

		public void setValue(Object value)
		{
			//  Format the Object before setting its value in the renderer
			if (value instanceof Float)
				value = formatter.format(value);

			super.setValue(value);
		}
	}

}
