import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;


public class DataEditDialog extends JDialog implements TableModelListener, ActionListener {
	private static final long serialVersionUID = -1659515848383070790L;
	
	private ArrayList<AlignmentMedium> media;
	private RDCSet curSet;
	private int selectedSet=0, selectedMedium=0;

	public boolean hadSetupProblems = false;
	
	private static final String[] columnNames = {"Used", "Residue", "RDC (Hz)", "Error (Hz)" };
	private static final int colCount = 4;
	private JTable table;
	private Object[][] tableData;
	
	private JButton prevMedium, nextMedium, prevType, nextType;
	private JLabel mediumName, typeName;
	
	public DataEditDialog(Frame owner, ArrayList<AlignmentMedium> media) {
		super(owner, "Edit RDC data", true);
		
		if(media == null) {
			hadSetupProblems = true;
			return;
		}
		
		// Check to make sure that at least one medium has RDC sets added.
		// If so, choose it as the first medium to edit.
		boolean hasNonEmpty = false;
		for(selectedMedium = 0; selectedMedium < media.size(); selectedMedium++) {
			if(media.get(selectedMedium).getCount() > 0) {
				hasNonEmpty = true;
				break;
			}
		}
		if(!hasNonEmpty) {
			hadSetupProblems = true;
			return;
		}
		
		this.media = media;
		curSet = media.get(selectedMedium).get(selectedSet); // Use the first RDC set of the selected medium as default 

		Point loc = owner.getLocation();
		loc.x += 50;
		loc.y += 50;
		this.setLocation(loc);
		this.setSize(500, 400);
		JPanel contentPane = (JPanel)getContentPane();
		contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

		// Try to load arrow images to use on next/previous buttons
		try {
			BufferedImage leftArrowImage = ImageIO.read(new File("dcgui_left_arrow.png"));
			ImageIcon leftArrowIcon = new ImageIcon(leftArrowImage);
			BufferedImage rightArrowImage = ImageIO.read(new File("dcgui_right_arrow.png"));
			ImageIcon rightArrowIcon = new ImageIcon(rightArrowImage);
			
			prevMedium = new JButton(leftArrowIcon);
			nextMedium = new JButton(rightArrowIcon);
			prevType = new JButton(leftArrowIcon);
			nextType = new JButton(rightArrowIcon);

		} catch(IOException ex) {
			// If load fails then just make the buttons show + and -
			prevMedium = new JButton("-");
			nextMedium = new JButton("+");
			prevType = new JButton("-");
			nextType = new JButton("+");
		}
		

		Dimension buttonDim = new Dimension(30, 30);
		prevMedium.setPreferredSize(buttonDim);
		prevMedium.addActionListener(this);
		
		nextMedium.setPreferredSize(buttonDim);
		nextMedium.addActionListener(this);
		
		prevType.setPreferredSize(buttonDim);
		prevType.addActionListener(this);
		
		nextType.setPreferredSize(buttonDim);
		nextType.addActionListener(this);
		
		mediumName = new JLabel();
		mediumName.setPreferredSize(new Dimension(90, 30));
		mediumName.setHorizontalAlignment(SwingConstants.CENTER);
		
		typeName = new JLabel();
		typeName.setPreferredSize(new Dimension(50, 30));
		typeName.setHorizontalAlignment(SwingConstants.CENTER);
		
		updateNavigation(); // Set the labels, and enable/disable navigation buttons as appropriate
		
		JPanel topPane = new JPanel();
	//	topPane.setLayout(new BoxLayout(topPane, BoxLayout.X_AXIS));
		topPane.setLayout(new FlowLayout());

		topPane.add(prevMedium);
		topPane.add(mediumName);
		topPane.add(nextMedium);
		topPane.add(Box.createRigidArea(new Dimension(10, 0)));
		topPane.add(prevType);
		topPane.add(typeName);
		topPane.add(nextType);
		this.add(topPane, BorderLayout.NORTH);
				
		prepareTableData();

		table = new JTable(new MyTableModel());
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		table.getModel().addTableModelListener(this);

		TableColumnModel m = table.getColumnModel();
		m.getColumn(2).setCellRenderer(new MyNumberRenderer(3));
		m.getColumn(3).setCellRenderer(new MyNumberRenderer(3));
		
	//	table.removeColumn(table.getColumn("Error (Hz)"));

		this.add(scrollPane);
	}

	private void prepareTableData() {
		int rowCount = curSet.getCount();
		
		// First allocate the 2D array
		tableData = new Object[rowCount][];
		for(int i=0; i<rowCount; i++)
			tableData[i] = new Object[colCount];
		
		// No copy the data from RDC set into the tableData. Remember that JTable takes Objects.
		for(int i=0; i<rowCount; i++) {
			RDC rdc = curSet.get(i);
			tableData[i][0] = new Boolean(rdc.isUsed);
			tableData[i][1] = new Integer(rdc.resNum);
			tableData[i][2] = new Float(rdc.value);
			tableData[i][3] = new Float(rdc.uncert);
		}
	}

	private void updateNavigation() {
		if(media.size() == 1) {
			nextMedium.setEnabled(false);
			prevMedium.setEnabled(false);
		} else {
			nextMedium.setEnabled(true);
			prevMedium.setEnabled(true);
		}
		
		if(media.get(selectedMedium).getCount() == 1) {
			nextType.setEnabled(false);
			prevType.setEnabled(false);
		} else {
			nextType.setEnabled(true);
			prevType.setEnabled(true);
		}
		
		mediumName.setText(media.get(selectedMedium).name);
		typeName.setText(curSet.type.name);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		JButton origin = (JButton)e.getSource();
		if(origin == nextMedium) {
			selectedSet = 0;
			// Try to switch to the next medium, but skip all media that don't have RDC sets added yet
			do {
				selectedMedium++;
				if(selectedMedium == media.size()) selectedMedium = 0;
			} while(media.get(selectedMedium).getCount() == 0);
		} else if(origin == prevMedium) {
			selectedSet = 0;
			// Try to switch to the previous medium, but skip all media that don't have RDC sets added yet
			do {
				selectedMedium--;
				if(selectedMedium == -1) selectedMedium = media.size() - 1;
			} while(media.get(selectedMedium).getCount() == 0);
		} else if(origin == nextType) {
			selectedSet++;
			if(selectedSet == media.get(selectedMedium).getCount()) selectedSet = 0;
		} else if(origin == prevType) {
			selectedSet--;
			if(selectedSet == -1) selectedSet = media.get(selectedMedium).getCount() - 1;
		}
		
		curSet = media.get(selectedMedium).get(selectedSet);
		prepareTableData();
		table.invalidate();
		table.repaint();
	//	((MyTableModel)table.getModel()).fireTableDataChanged();
		
		updateNavigation();
	}

	@Override
	// Called when data inside the table was edited. We want this to alter the data in the current RDC set
	public void tableChanged(TableModelEvent e) {
		if(e.getType() != TableModelEvent.UPDATE) return; // We only react to UPDATE events
		
		int row = e.getFirstRow();
        int column = e.getColumn();
        RDC rdc = curSet.get(row);
        Object data = tableData[row][column];
       // MyTableModel model = (MyTableModel)e.getSource();
       // Object data = model.getValueAt(row, column);
        switch(column) {
        case 0:
        	rdc.isUsed = (Boolean)data; break;
        case 2:
        	rdc.value = (Float)data; break;
        case 3:
        	rdc.uncert = (Float)data; break;
        }

	}

	////  Table model /////////////////////////////////
	class MyTableModel extends AbstractTableModel {
	    public int getColumnCount() {
	        return columnNames.length;
	    }

	    public int getRowCount() {
	        return tableData.length;
	    }

	    public String getColumnName(int col) {
	        return columnNames[col];
	    }

	    public Object getValueAt(int row, int col) {
	        return tableData[row][col];
	    }

	    public Class getColumnClass(int c) {
	        return getValueAt(0, c).getClass();
	    }

	    public boolean isCellEditable(int row, int col) {
	        if (col == 1) {
	            return false;
	        } else {
	            return true;
	        }
	    }
	    
	    public void setValueAt(Object value, int row, int col) {
	        tableData[row][col] = value;
	        fireTableCellUpdated(row, col);
	    }
	}
	////// End table model //////////////////////////////////////
	
	public class MyNumberRenderer extends DefaultTableCellRenderer
	{
		private DecimalFormat formatter;

		/*
		 *   Use the specified formatter to format the Object
		 */
		public MyNumberRenderer(int numDigits)
		{
			formatter = new DecimalFormat();
			formatter.setMaximumFractionDigits(numDigits);
			formatter.setMinimumFractionDigits(numDigits);
		//	formatter.applyPattern("#0.000###");
			setHorizontalAlignment( SwingConstants.RIGHT );
		}

		public void setValue(Object value)
		{
			//  Format the Object before setting its value in the renderer
			try
			{
				if (value != null)
					value = formatter.format(value);
			}
			catch(IllegalArgumentException e) {}

			super.setValue(value);
		}
	}

}
