import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
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
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;


public class DataEditDialog extends JDialog implements TableModelListener, ActionListener {
	private static final long serialVersionUID = -1659515848383070790L;
	
	private ArrayList<AlignmentMedium> media;
	private RDCSet curSet;
	private int selectedSet=0, selectedMedium=0;

	public boolean hadSetupProblems = false;
	
	private static final String[] columnNames = {"Used", "Residue", "RDC (Hz)", "Predicted (Hz)", "Uncert (Hz)", "Quality" };
	private static final int colCount = 6;
	private JTable table;
	private Object[][] tableData;
	private ImageIcon iconGood, iconFair, iconBad, iconNone;
	
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
		ImageIcon leftArrowIcon = new ImageIcon(ResourceLoader.getURL("images/dcgui_left_arrow.png"));
		ImageIcon rightArrowIcon = new ImageIcon(ResourceLoader.getURL("images/dcgui_right_arrow.png"));
		
		prevMedium = new JButton(leftArrowIcon);
		nextMedium = new JButton(rightArrowIcon);
		prevType = new JButton(leftArrowIcon);
		nextType = new JButton(rightArrowIcon);
		

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
		mediumName.setPreferredSize(new Dimension(180, 30));
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
		
		
		// Prepare icons to be used for indicating the quality of RDCs
		// Good quality
		BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();
		g.setPaint(Color.green);
		Rectangle rect = new Rectangle(1, 1, 8, 8);
		g.fill(rect);
		iconGood = new ImageIcon(img);

		// Fair quality
		img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
		g = img.createGraphics();
		g.setPaint(Color.yellow);
		g.fill(rect);
		iconFair = new ImageIcon(img);

		// Bad quality
		img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
		g = img.createGraphics();
		g.setPaint(Color.red);
		g.fill(rect);
		iconBad = new ImageIcon(img);
		
		// Empty icon
		img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
		g = img.createGraphics();
		g.setPaint(Color.white);
		g.fill(rect);
		iconNone = new ImageIcon(img);

		prepareTableData();

		table = new JTable(new MyTableModel());
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		table.getModel().addTableModelListener(this);

		// Force the data to be displayed with three digits after the decimal point
		TableColumnModel m = table.getColumnModel();
		MyNumberRenderer numberRenderer = new MyNumberRenderer(3);
		m.getColumn(2).setCellRenderer(numberRenderer);
		m.getColumn(3).setCellRenderer(numberRenderer);
		m.getColumn(4).setCellRenderer(numberRenderer);
		
		// Set the column widths to be fixed for columns: Used, Residue, and Quality.
		// The other three columns will resize, when the dialog is resized.
		m.getColumn(0).setMinWidth(50);
		m.getColumn(0).setMaxWidth(50);
		m.getColumn(1).setMinWidth(70);
		m.getColumn(1).setMaxWidth(70);
		m.getColumn(5).setMinWidth(70);
		m.getColumn(5).setMaxWidth(70);		
		
		// Center the table headers
		TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
		JLabel headerLabel = (JLabel) headerRenderer;
		headerLabel.setHorizontalAlignment(JLabel.CENTER);
		
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
			tableData[i][4] = new Float(rdc.uncert);
			
			if(rdc.wasPredicted) {
				tableData[i][3] = new Float(rdc.predValue);
				
				// Chose the icon based on how much the predicted and observed RDC differ
				if(Math.abs(rdc.predValue - rdc.value) < 1.5*rdc.uncert)
					tableData[i][5] = iconGood;
				else if(Math.abs(rdc.predValue - rdc.value) > 4*rdc.uncert)
					tableData[i][5] = iconBad;
				else
					tableData[i][5] = iconFair;
			}
			else {
				tableData[i][3] = null;
				tableData[i][5] = iconNone; // empty icon
			}
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

        switch(column) {
        case 0:
        	rdc.isUsed = (Boolean)data; 
        	if(!rdc.isUsed)
        		rdc.wasPredicted = false;
        	break;
        case 2:
        	rdc.value = (Float)data; break;
        case 4:
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
	    	// Residue number and predicted RDC are not editable
	        if (col == 1 || col == 3) {
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
