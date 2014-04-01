import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class DCSetupDialog extends JDialog {
	private static final long serialVersionUID = -3374569389699929123L;
	private int mediumIndex; // Used to return result of medium selection
	private ArrayList<AllignmentMedium> media;
	private boolean setupCompleted = false;
	private JComboBox comboMedium, comboSets;
	private JCheckBox cbUseAll, cbDa, cbRh, cbOrientation;
	private JTextField fieldDa, fieldRh, fieldPsi, fieldTheta, fieldPhi;
	private JPanel anglesPane;
	
	private float Da, Rh, psi, theta, phi;
	private boolean fixDa = false, fixRh = false, fixOrientation = false;
	
	public DCSetupDialog(JFrame owner, ArrayList<AllignmentMedium> media) {
		super(owner, "Setup calculation", true); // Create a modal dialog with title
		
		this.media = media;
		
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		Point loc = owner.getLocation();
		loc.x += 50;
		loc.y += 50;
		this.setLocation(loc);
		this.setSize(300, 150);
		JPanel contentPane = (JPanel)getContentPane();
		contentPane.setBorder(BorderFactory.createEmptyBorder(10,30,15,30));
		
		JPanel mainPane = new JPanel();
		GridBagLayout thisLayout = new GridBagLayout();
		thisLayout.rowWeights = new double[] {0.1, 0.1, 0.1, 0.1, 0.1, 0.1 };
		thisLayout.rowHeights = new int[] {7, 7, 7, 7, 7, 7 };
		thisLayout.columnWeights = new double[] {0.1, 0.1 };
		thisLayout.columnWidths = new int[] {7, 7 };
		mainPane.setLayout(thisLayout);

		// Row 1 //////
		JLabel label = new JLabel("Alignment medium");
		mainPane.add(label, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		String [] mediaNames = new String[media.size()];
		for(int i=0; i<mediaNames.length; i++)
			mediaNames[i] = media.get(i).name;
		comboMedium = new JComboBox(mediaNames);
		
		// Add an action listener to reset the content of the sets combo box upon selection of a different medium
		comboMedium.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediumIndex = comboMedium.getSelectedIndex();
				AllignmentMedium medium = DCSetupDialog.this.media.get(mediumIndex);
				String [] setTypes = new String[medium.getCount()];
				for(int i=0; i<medium.getCount(); i++) {
					setTypes[i] = medium.get(i).getTypeString();
				}
				DefaultComboBoxModel model = new DefaultComboBoxModel(setTypes);
				comboSets.setModel(model);	
			}
		});
		mainPane.add(comboMedium, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
		
		// Row 2 //////
		cbUseAll = new JCheckBox("Use all RDC sets");
		cbUseAll.setSelected(true);
		
		// Add action listener coupling the states of "use all sets" checkbox, and the RDC set selection combo box
		cbUseAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(cbUseAll.isSelected()) {
					comboSets.setEnabled(false);
				} else {
					comboSets.setEnabled(true);
				}				
			}
		});
		
		mainPane.add(cbUseAll, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		AllignmentMedium medium = media.get(0);
		String [] setTypes = new String[medium.getCount()];
		for(int i=0; i<medium.getCount(); i++) {
			setTypes[i] = medium.get(i).getTypeString();
		}
		comboSets = new JComboBox(setTypes);
		comboSets.setEnabled(false); // Start with disabled combo box, and checked "Use all sets" checkbox
		mainPane.add(comboSets, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
		
		// Row 3 //////
		cbDa = new JCheckBox("Fix Da");
		mainPane.add(cbDa, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		fieldDa = new JTextField(6);
		fieldDa.setText("10.0");
		mainPane.add(fieldDa, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
		
		// Row 4 ////////
		cbRh = new JCheckBox("Fix rhombicity");
		mainPane.add(cbRh, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		fieldRh = new JTextField(6);
		fieldRh.setText("0.30");
		mainPane.add(fieldRh, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
		
		// Row 5 ////////
		cbOrientation = new JCheckBox("Fix orientation");
		mainPane.add(cbOrientation, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		JButton button = new JButton("Read from file");
		mainPane.add(button, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser browseDialog = new JFileChooser();
				int returnVal = browseDialog.showOpenDialog(DCSetupDialog.this);
				if(returnVal != JFileChooser.APPROVE_OPTION) return; // Stop if no selection was made

				File file = browseDialog.getSelectedFile();
				DCOutput out = new DCOutput();
				out.loadMeta(file); // Load meta-data only from the result file
				
				if(!out.wasLoadedSuccessfully()) {
					JOptionPane.showMessageDialog(DCSetupDialog.this, "There was a problem reading the file", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				fieldPsi.setText(String.valueOf(out.psi));
				fieldTheta.setText(String.valueOf(out.theta));
				fieldPhi.setText(String.valueOf(out.phi));
			}
		});
		
		// Row 6 /////////
		anglesPane = new JPanel();
		anglesPane.setLayout(new BoxLayout(anglesPane, BoxLayout.X_AXIS));
		label = new JLabel("Psi: ");
		fieldPsi = new JTextField(4);
		fieldPsi.setText("45.0");
		anglesPane.add(label);
		anglesPane.add(fieldPsi);
		
		label = new JLabel("   Theta: ");
		fieldTheta = new JTextField(4);
		fieldTheta.setText("45.0");
		anglesPane.add(label);
		anglesPane.add(fieldTheta);
		
		label = new JLabel("   Phi: ");
		fieldPhi = new JTextField(4);
		fieldPhi.setText("45.0");
		anglesPane.add(label);
		anglesPane.add(fieldPhi);

		mainPane.add(anglesPane, new GridBagConstraints(0, 5, 2, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 0, 0));
		anglesPane.setVisible(false);  // Make the angles pane invisible until user checks "fix orientation"

		cbOrientation.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(cbOrientation.isSelected()) {
					anglesPane.setVisible(true);
					cbDa.setSelected(false); // Enforcing the rule that Da and orientation can't be fixed simultaneously
				}
				else
					anglesPane.setVisible(false);
				
				DCSetupDialog.this.pack();
			}
			
		});
		
		cbDa.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Enforcing the rule that Da and orientation can't be fixed simultaneously
				if(cbDa.isSelected() && cbOrientation.isSelected()) {
					cbOrientation.setSelected(false);
					anglesPane.setVisible(false);
				}				
			}		
		});
		
		this.add(mainPane);
		contentPane.add(Box.createRigidArea(new Dimension(0, 15)));
		
		// The two buttons are placed into their own pane
		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.X_AXIS));
				
		button = new JButton("OK");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediumIndex = comboMedium.getSelectedIndex();
				
				fixDa = cbDa.isSelected();
				fixRh = cbRh.isSelected();
				fixOrientation = cbOrientation.isSelected();
				
				try {
					if(fixDa) 
						Da = Float.parseFloat(fieldDa.getText());
					
					if(fixRh)
						Rh = Float.parseFloat(fieldRh.getText());
					
					if(fixOrientation) {
						psi = Float.parseFloat(fieldPsi.getText());
						theta = Float.parseFloat(fieldTheta.getText());
						phi = Float.parseFloat(fieldPhi.getText());
					}
				} catch(Exception exc) {
					JOptionPane.showMessageDialog(DCSetupDialog.this, "One or more of the values have problems", "Error", JOptionPane.ERROR_MESSAGE);
					setupCompleted = false;
					return;
				}

				setupCompleted = true;
				DCSetupDialog.this.dispose();
			}		
		});
		bottomPane.add(button);
		
		bottomPane.add(Box.createRigidArea(new Dimension(5, 0)));
		
		button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setupCompleted = false;
				DCSetupDialog.this.dispose();
			}		
		});
		bottomPane.add(button);
		bottomPane.setAlignmentX(CENTER_ALIGNMENT);
		this.add(bottomPane);
		this.pack();
	//	addWindowListener(this);
	}

	public int getSelectedMedium() {
		return mediumIndex;
	}
	
	public boolean wasSetupCompleted() {
		return setupCompleted;
	}

	public boolean doUseAllSets() {
		return cbUseAll.isSelected();
	}
	
	public int getSelectedSet() {
		return comboSets.getSelectedIndex();
	}
	
	public boolean doFixDa() {
		return fixDa;
	}
	
	public boolean doFixRh() {
		return fixRh;
	}
	
	public boolean doFixOrientation() {
		return fixOrientation;
	}
	
	public float getDa() {
		return Da;
	}
	
	public float getRh() {
		return Rh;
	}
	
	public float getPsi() {
		return psi;
	}
	
	public float getTheta() {
		return theta;
	}
	
	public float getPhi() {
		return phi;
	}
}
