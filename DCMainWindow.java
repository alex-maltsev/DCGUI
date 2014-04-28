import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.filechooser.FileFilter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Pattern;


public class DCMainWindow {
	private enum Format { Simple, Full, Unknown, ReadError }
	
	private JFrame mainFrame;
	private JPanel pane = new JPanel();
	private JTree mediaTree;
	private DefaultMutableTreeNode mediaTreeRoot;
	
	private ImageIcon iconOK, iconStop, iconWarning;
	private JLabel picLabel1, picLabel2, picLabel3;
	private final JFileChooser browseDialog = new JFileChooser();
	private final StateFilter stateFilter = new StateFilter();
	private File pdbFile;
	private NoiseSetupDialog noiseDialog;
	private DCSetupDialog setupDialog;
	
	private GraphWindow graphWindow;
	
	private ArrayList<AlignmentMedium> media;
	private int lastSelectedMedium = 0;
	private ProtSequence sequence;

	private RDCSet testSet;
	
	DCMainWindow() throws IOException {
		sequence = new ProtSequence("");
		media = new ArrayList<AlignmentMedium>();
		media.add(new AlignmentMedium("(default)"));
		
		mainFrame = new JFrame("DC GUI");
		mainFrame.setBounds(300, 300, 500, 300);
		
		initMenu();
		initDragAndDrop();

		iconOK = new ImageIcon(ResourceLoader.getURL("images/icon-complete.png"));
		iconStop = new ImageIcon(ResourceLoader.getURL("images/icon-stop.png"));

		GridBagLayout thisLayout = new GridBagLayout();
		thisLayout.rowWeights = new double[] {0.1, 0.1, 0.1, 2.0, 0.1};
		thisLayout.rowHeights = new int[] {7, 7, 7, 7, 7};
		thisLayout.columnWeights = new double[] {2.0, 0.1, 0.1, 0.1, 0.1};
		thisLayout.columnWidths = new int[] {7, 100, 7, 7, 7};
		pane.setLayout(thisLayout);
		
		///////////// Row 1 //////////////////////////////////
		JLabel label = new JLabel();
		label.setText("Protein sequence");
		pane.add(label, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
			
		picLabel1 = new JLabel(iconStop);
	//	picLabel1.setSize(iconOK.getIconWidth(), iconOK.getIconHeight());	
		pane.add(picLabel1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		JButton button = new JButton("Edit...");
		pane.add(button, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				SequenceDialog seq = new SequenceDialog(mainFrame);
				seq.setSequence(sequence);
				seq.setVisible(true);
				
				if(sequence.getSequence().length() == 0)
					picLabel1.setIcon(iconStop);
				else
					picLabel1.setIcon(iconOK);

			}
		});
		
		///////////// Row 2 //////////////////////////////////
		label = new JLabel("Protein structure (PDB)     ");  // Notice added spacer on the end
		pane.add(label, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
		
		picLabel2 = new JLabel(iconStop);
	//	picLabel2.setSize(iconOK.getIconWidth(), iconOK.getIconHeight());	
		pane.add(picLabel2, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		button = new JButton("Browse");
		pane.add(button, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		button.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				PDBFilter filter = new PDBFilter();
				browseDialog.addChoosableFileFilter(filter);
				int returnVal = browseDialog.showOpenDialog(mainFrame);
				if(returnVal != JFileChooser.APPROVE_OPTION) {  // Stop if no selection was made
					browseDialog.removeChoosableFileFilter(stateFilter); // Clean up for the next use
					return; 
				}
				pdbFile = browseDialog.getSelectedFile();
				browseDialog.removeChoosableFileFilter(filter); // Clean up for the next use
				picLabel2.setIcon(iconOK);
			}
			
		});

		///////////// Row 3 //////////////////////////////////
		label = new JLabel("RDC data");
		pane.add(label, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 20, 0, 0), 0, 0));
		
		picLabel3 = new JLabel(iconStop);
	//	picLabel3.setSize(iconOK.getIconWidth(), iconOK.getIconHeight());	
		pane.add(picLabel3, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		button = new JButton("Add medium");
		pane.add(button, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				String name = (String)JOptionPane.showInputDialog(mainFrame, "Please enter medium name", "Alignment medium", JOptionPane.QUESTION_MESSAGE);
				if(name==null || name.length()==0) return;
				
				AlignmentMedium medium = new AlignmentMedium(name);
				media.add(medium);
				refreshMediaPane();
			}
		});
		////////////////////////////////////////////////
		
		mediaTreeRoot = new DefaultMutableTreeNode("(no media)", true);
	    mediaTree = new JTree(mediaTreeRoot);
	    mediaTree.setEditable(true);
	    mediaTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	    mediaTree.setRootVisible(false);

		mediaTree.getModel().addTreeModelListener(new RDCTreeListener());
	    
		JScrollPane scrollPane = new JScrollPane(mediaTree);
		pane.add(scrollPane, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 20, 0, 0), 0, 0));
		refreshMediaPane();
		
		//////////// Last row //////////////////////////////
		JPanel rdcButtonsPane = new JPanel();
		rdcButtonsPane.setLayout(new BoxLayout(rdcButtonsPane, BoxLayout.Y_AXIS));

		button = new JButton("Edit RDCs");
		rdcButtonsPane.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				DataEditDialog dialog = new DataEditDialog(mainFrame, media);
				if(dialog.hadSetupProblems) {
					JOptionPane.showMessageDialog(mainFrame, "Please add some RDC data first", "Alert", JOptionPane.WARNING_MESSAGE);
					return;
				}
				dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				dialog.setVisible(true);
			}
		});

		rdcButtonsPane.add(Box.createRigidArea(new Dimension(0, 5)));
		
		button = new JButton("  Delete  ");
		rdcButtonsPane.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				// Get selected node
				DefaultMutableTreeNode node = (DefaultMutableTreeNode)mediaTree.getLastSelectedPathComponent();
				if(node == null) return; // Nothing is selected
				
				if(node.getLevel() == 1) { // A medium is selected
					int index = mediaTreeRoot.getIndex(node); // Get the index of the medium
					media.remove(index); // Remove it from media list
					
					// If we ended up without any media, then add (default) medium back
					if(media.size() == 0)
						media.add(new AlignmentMedium("(default)"));
				} else if(node.getLevel() == 2) {  // An RDC set is selected
					// Figure out which medium the set belongs to by finding the parent of the node.
					DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
					int mediumIndex = mediaTreeRoot.getIndex(parent); // Get the index of the medium
					int setIndex = parent.getIndex(node); // Get the index of RDC set
					
					// If the medium is non-empty remove the selected set from the medium
					// (remember there are nodes saying "no data", which don't represent an actual set
					if(media.get(mediumIndex).getCount() > 0)
						media.get(mediumIndex).remove(setIndex); 
				}
				
				refreshMediaPane();
			}
		});

		pane.add(rdcButtonsPane, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		mainFrame.getContentPane().add(pane);
		
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setVisible(true);
		
		graphWindow = new GraphWindow(this);
	}
	
		
	// Can be used by child windows to decide how to position themselves
	public Rectangle getBounds() {
		return mainFrame.getBounds();
	}
	
	
	// Draws the tree structure for alignment media and their RDC types
	private void refreshMediaPane() {
		DefaultTreeModel treeModel = (DefaultTreeModel) mediaTree.getModel();
		DefaultMutableTreeNode mediumNode = null, setNode = null;
		int setsCount = 0; // Used to count currently present RDC sets
		
		mediaTreeRoot.removeAllChildren();
		if(media.size() == 0) { 
			mediumNode = new DefaultMutableTreeNode("(no media)", true);
			mediaTreeRoot.add(mediumNode);
		}
		else {
			mediaTreeRoot.setUserObject("");
			
			for(AlignmentMedium medium: media) {
				mediumNode = new DefaultMutableTreeNode(medium.name, true);
				mediaTreeRoot.add(mediumNode);
				
				if(medium.getCount() == 0) {
					setNode = new DefaultMutableTreeNode("(no data)");
					mediumNode.add(setNode);
				} else {
					for(RDCSet set: medium.getRDCSets()) {
						setNode = new DefaultMutableTreeNode(set.getTypeString());
						mediumNode.add(setNode);
						
						setsCount++;
					}
				}
			}		
		} 
		
		// Make sure that the icon correctly reflects whether any RDC sets are present
		if(setsCount > 0)
			picLabel3.setIcon(iconOK);
		else
			picLabel3.setIcon(iconStop);

		// Force tree refresh
		treeModel.reload(mediaTreeRoot);
		
		// Expand all nodes
		for(int i=0; i < mediaTree.getRowCount(); i++)
			mediaTree.expandRow(i);
		
    	// Discard the current setupDialog so that it would be recreated with new media setup
    	setupDialog = null;
	}
	
	
	// Initialize the Drag-and-Drop functionality
	// Simply determines the dropped file and calls readRDCInput to read it in
	private void initDragAndDrop() {
		new FileDrop(mainFrame, new FileDrop.Listener() {   
			public void filesDropped(File[] files) {
				readRDCInput(files[0]);
			} 
		}); // end FileDrop.Listener
	}

	
	// Initialization of the top-level menu
	private void initMenu() {
		//Create the main menu bar.
		JMenuBar menuBar = new JMenuBar();

		initFileMenu(menuBar);  // 'File'
		
	//	initSettingsMenu(menuBar); // 'Settings'
		
		initCalculationsMenu(menuBar); // 'Calculations'

		mainFrame.setJMenuBar(menuBar);	
	}

	//Build the File menu.	
	private void initFileMenu(JMenuBar menuBar) {
		JMenu menu = new JMenu("File");
		menuBar.add(menu);

		JMenuItem menuItem = new JMenuItem("Load RDC data...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Bring up the Load File dialog
				browseDialog.setAcceptAllFileFilterUsed(true);
				int returnVal = browseDialog.showOpenDialog(mainFrame);
				if(returnVal != JFileChooser.APPROVE_OPTION) return; // Stop if no selection was made
				File file = browseDialog.getSelectedFile();
				readRDCInput(file);
			}
		});
		menu.add(menuItem);

		menuItem = new JMenuItem("Save state...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Bring up the Save File dialog
				browseDialog.setAcceptAllFileFilterUsed(false);
				browseDialog.addChoosableFileFilter(stateFilter);
				int returnVal = browseDialog.showSaveDialog(mainFrame);
				if(returnVal != JFileChooser.APPROVE_OPTION) {  // Stop if no selection was made
					browseDialog.removeChoosableFileFilter(stateFilter); // Clean up for the next use
					return; 
				}
				
				File file = browseDialog.getSelectedFile();
				browseDialog.removeChoosableFileFilter(stateFilter); // Clean up for the next use
				
				saveState(file);
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Load state...");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Bring up the Open File dialog
				browseDialog.setAcceptAllFileFilterUsed(false);
				browseDialog.addChoosableFileFilter(stateFilter);
				
				int returnVal = browseDialog.showOpenDialog(mainFrame);
				if(returnVal != JFileChooser.APPROVE_OPTION) {  // Stop if no selection was made
					browseDialog.removeChoosableFileFilter(stateFilter); // Clean up for the next use
					return; 
				}
				
				File file = browseDialog.getSelectedFile();
				browseDialog.removeChoosableFileFilter(stateFilter); // Clean up for the next use
				
				loadState(file);
			}
		});
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Save data in DC format");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AlignmentMedium selectedMedium;
				
				if(media.size() > 1) {  // Only ask to select medium if there are more than one
					RDCOutputSelectDialog dialog = new RDCOutputSelectDialog(mainFrame, media);
					dialog.setVisible(true);
					
					if(dialog.selectionWasMade())
						selectedMedium = media.get(dialog.getSelectedMedium());
					else
						return; // If no selection was made then stop here
				} else
					selectedMedium = media.get(0);
				
				// Bring up the Save File dialog
				int returnVal = browseDialog.showSaveDialog(mainFrame);
				if(returnVal != JFileChooser.APPROVE_OPTION) return; // Stop if no selection was made
				File file = browseDialog.getSelectedFile();
				
				exportInDCFormat(file, selectedMedium);
			}
		});
		menu.add(menuItem);

		menuItem = new JMenuItem("Quit");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainFrame.dispose();			
			}
		});
		menu.add(menuItem);		
	}
	
	//Build the Settings menu.
	private void initSettingsMenu(JMenuBar menuBar) {
		JMenu menu = new JMenu("Settings");
		menuBar.add(menu);

		JMenuItem menuItem = new JMenuItem("Set working folder");
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Alignment media");
		menu.add(menuItem);
	}
	
	//Build the Calculations menu.
	private void initCalculationsMenu(JMenuBar menuBar) {
		JMenu menu = new JMenu("Calculations");
		menuBar.add(menu);

		JMenuItem menuItem = new JMenuItem("Run DC");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(pdbFile == null) {
					JOptionPane.showMessageDialog(mainFrame, "Please select a PDB file", "Alert", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				AlignmentMedium selectedMedium, tempMedium;

				if(setupDialog == null) setupDialog = new DCSetupDialog(mainFrame, media); // Only create setup dialog once
				setupDialog.setVisible(true);
				
				if(!setupDialog.wasSetupCompleted()) return; // If user canceled setup dialog then stop here
				
				selectedMedium = media.get(setupDialog.getSelectedMedium());
				if(setupDialog.doUseAllSets())
					tempMedium = selectedMedium;
				else {
					tempMedium = new AlignmentMedium("temp");
					int index = setupDialog.getSelectedSet();
					tempMedium.addRDCSet(selectedMedium.get(index));
				}

				String tempInputName = "rdc_" + System.currentTimeMillis() + ".tab";
				File tempInput = new File(tempInputName);
				exportInDCFormat(tempInput, tempMedium);
				
				DCWrapper dc = new DCWrapper(pdbFile, tempInput);
				
				if(setupDialog.doFixDa()) 
					dc.fixDa(setupDialog.getDa());
				if(setupDialog.doFixRh()) 
					dc.fixRh(setupDialog.getRh());
				if(setupDialog.doFixOrientation()) 
					dc.fixOrientation(setupDialog.getPsi(), setupDialog.getTheta(), setupDialog.getPhi());

				int ret = dc.runDC();
				
				// Delete the temporary RDC file used for calculations
				tempInput.delete();
				
				if(ret != 0) {
					JOptionPane.showMessageDialog(mainFrame, dc.getErrorMessage(), "DC Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				DCOutput out = new DCOutput();
				out.load(dc.getResultFile(), tempMedium);
				if(!out.wasLoadedSuccessfully()) {
					JOptionPane.showMessageDialog(mainFrame, "There was a problem reading DC output file", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				ArrayList<RDCSet> usedSets = new ArrayList<RDCSet>();
				for(int i=0; i<tempMedium.getCount(); i++)
					usedSets.add(tempMedium.get(i));
				graphWindow.setRDCSets(usedSets);
				graphWindow.refresh();
				graphWindow.setVisible(true);
				
				// Show the results dialog, where the user can do additional operations
				// NOTICE that when the user closes the dialog, the temporary result file is deleted.
				DCOutputDialog dialog = new DCOutputDialog(mainFrame, out);
				dialog.setVisible(true);
			}		
		});
		
		menu.add(menuItem);
		
		
		menuItem = new JMenuItem("Media correlations");
		menuItem.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				MediaCorrelationsDialog dialog = new MediaCorrelationsDialog(mainFrame, media);
				dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				dialog.setVisible(true);				
			}});
		
		menu.add(menuItem);
		
		
		menuItem = new JMenuItem("NH structural noise");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(pdbFile == null) {
					JOptionPane.showMessageDialog(mainFrame, "Please select a PDB file", "Alert", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				// Show the dialog asking for options setup
				if(noiseDialog == null) noiseDialog = new NoiseSetupDialog(mainFrame); // Only create the dialog once
				noiseDialog.setVisible(true);
				if(!noiseDialog.wasSetupCompleted()) return;
				
				// Work with N and HN atoms only, to make PDB reads and writes faster
				PDBReader reader = new PDBReader(pdbFile);
				PDBReader NHreader = reader.cloneNHOnly();
				NHreader.setupNHVectors();
				String tempPDBName = "struc_" + System.currentTimeMillis() + ".pdb";
				File pdb = new File(tempPDBName);
				
				// Create the temporary RDC input file
				String tempInputName = "rdc_" + System.currentTimeMillis() + ".tab";
				File tempInput = new File(tempInputName);
				AlignmentMedium selectedMedium = media.get(0);  // TEMPORARY!
				exportInDCFormat(tempInput, selectedMedium); 
				
				// Prepare DC wrapper
				DCWrapper dc = new DCWrapper(pdb, tempInput);
				dc.fixDaRh(2.85f, 0.151f); // Protease squalamine fitting
				dc.setInitialOrientation(45.90f, -14.47f, 38.70f);
				DCOutput out = new DCOutput();
				
				int numTries = noiseDialog.getNumStrucs();
				double coneAngle = noiseDialog.getConeAngle();
				
				float [] Qfactors = new float[numTries];
				float [] rmsds = new float[numTries];
				float [] xAngles = new float[numTries];
				float [] yAngles = new float[numTries];
				float [] zAngles = new float[numTries];
				
				// Special for Julien's project!!! //////
				// Prepare rotated vectors based on the rotation matrix calculated for bottom part at 1 bar
				Vector x0 = new Vector(0.680f, 0.665f, 0.309f);
				Vector y0 = new Vector(-0.663f, 0.380f, 0.645f);
				Vector z0 = new Vector(0.311f, -0.643f, 0.699f);
				// Prepare rotated vectors based on the rotation matrix calculated for bottom part at 500 bar
			//	Vector x0 = new Vector(0.567f, 0.755f, 0.329f);
			//	Vector y0 = new Vector(-0.728f, 0.273f, 0.629f);
			//	Vector z0 = new Vector(0.385f, -0.596f, 0.705f);
				
				Vector x = new Vector(), y = new Vector(), z = new Vector();
				float [][] rMat; // Rotation matrix reference to the result of DC calculations
				
				for(int i=0; i<numTries; i++) {
					// Create the PDB file with added noise
					NHreader.addNHNoise(coneAngle);
					NHreader.save(pdb);
					
					int ret = dc.runDC();
				
					if(ret != 0) {
						JOptionPane.showMessageDialog(mainFrame, dc.getErrorMessage(), "DC Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
						
					out.load(dc.getResultFile(), selectedMedium);
					dc.getResultFile().delete(); // Delete the DC result file
					
					Qfactors[i] = out.fittingResult.Qfactor;
					rmsds[i] = out.fittingResult.rms;
					
					rMat = out.fittingResult.RotMatrix;
					x.setTo(rMat[0][0], rMat[1][0], rMat[2][0]);
					y.setTo(rMat[0][1], rMat[1][1], rMat[2][1]);
					z.setTo(rMat[0][2], rMat[1][2], rMat[2][2]);
					
					xAngles[i] = x.angle(x0);
					yAngles[i] = y.angle(y0);
					zAngles[i] = z.angle(z0);
					
					
					float c = (float)(180.0 / Math.PI);
					if(xAngles[i]*c > 90) {  // Rotation matrix flipped - try again
						i--; 
						continue; 
					}
					
					System.out.printf("%d: x=%.3f, y=%.3f, z=%.3f\n", i, xAngles[i]*c, yAngles[i]*c, zAngles[i]*c);
				}

				// Calculate average Q-factor and rmsd
				float QfactorSum = 0, rmsSum = 0;
				for(int i=0; i<numTries; i++) {
					QfactorSum += Qfactors[i];
					rmsSum += rmsds[i];
				}
				
				System.out.printf("For cone angle %.2f, average Q-factor is %.3f, average rms is %.3f\n", coneAngle, arrayAvg(Qfactors), arrayAvg(rmsds));
				float c = (float)(180.0 / Math.PI);
				System.out.printf("Average angles: %.3f, %.3f, %.3f\n", arrayAvg(xAngles)*c, arrayAvg(yAngles)*c, arrayAvg(zAngles)*c);
				System.out.printf("Angle STDs: %.3f, %.3f, %.3f\n", arraySTD(xAngles)*c, arraySTD(yAngles)*c, arraySTD(zAngles)*c);
				
				// Delete the temporary files used for calculations
				tempInput.delete();
				pdb.delete();
			}
		});
		
		menu.add(menuItem);
	}

	
	// Load state from the given file
	private void loadState(File file) {
		try {
			//use buffering
			InputStream fileStream = new FileInputStream(file);
			InputStream buffer = new BufferedInputStream(fileStream);
			ObjectInput input = new ObjectInputStream (buffer);
			try {
				pdbFile = (File)input.readObject();
				media = (ArrayList<AlignmentMedium>)input.readObject();
		    }
		    finally {
		    	input.close();
		    	refreshMediaPane();
		    	picLabel3.setIcon(iconOK);
		    	
		    	// Find at least one non-empty RDC set, and use its sequence to update the current sequence
		    	for(AlignmentMedium medium: media) {
		    		if(medium.getCount() > 0) {
		    			sequence = medium.getRDCSets().get(0).getSequence();
		    			break;
		    		}
		    	}
		    	
		    	// Update the state icon for sequence
		    	if(sequence.getLength()==0)
		    		picLabel1.setIcon(iconStop);
		    	else
		    		picLabel1.setIcon(iconOK);
		    	
		    	// Update the state icon for PDB file
		    	if(pdbFile == null)
		    		picLabel2.setIcon(iconStop);
		    	else
		    		picLabel2.setIcon(iconOK);				    	
		    }
		}
		catch(ClassNotFoundException ex){
			ex.printStackTrace();
		}
		catch(IOException ex){
			ex.printStackTrace();
		}
	}
	
	
	// Save the state into the given file
	private void saveState(File file) {
		// If necessary, append the "state" extension to file name
		String file_name = file.toString();
		if (!file_name.endsWith(".state")) {
		    file_name += ".state";
		    file = new File(file_name);
		}
		
		try {
			//use buffering
			OutputStream fileStream = new FileOutputStream(file);
			OutputStream buffer = new BufferedOutputStream(fileStream);
			ObjectOutput output = new ObjectOutputStream(buffer);
			try{
				output.writeObject(pdbFile);
				output.writeObject(media);
			}
			finally {
		        output.close();
			}
		}  
		catch(IOException ex){
			//System.out.println(ex.printStackTrace());
			ex.printStackTrace();
		}				
	}
	
	// Export all the RDC sets for the given alignment media in the full DC format
	private void exportInDCFormat(File file, AlignmentMedium medium) {
		try {
	    	Writer writer = new BufferedWriter(new FileWriter(file));
	    	// Write out the necessary file header
	    	writer.write("VARS   RESID_I RESNAME_I ATOMNAME_I RESID_J RESNAME_J ATOMNAME_J D DD W\n");
	    	writer.write("FORMAT %5d %6s %6s %5d %6s %6s %9.3f %9.3f %.2f\n\n");

	    	for(RDCSet set: medium.getRDCSets()) {
	    		set.outputFormatted(writer);
		    	writer.write("\n\n");
	    	}
	    	
	    	writer.close();
		}  
		catch(IOException ex){
			ex.printStackTrace();
		}		

	}
	
	
	
	// This function will determine the format of the provided file
	// and will read its content according to the format (Simple or Full)
	private void readRDCInput(File file) {
		try {
			Format fileFormat = getFileFormat(file);
			switch(fileFormat) {
			case Simple:
				if(media.size() == 0) {
					JOptionPane.showMessageDialog(mainFrame, "Please add at least one medium", "Alert", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				RDCTypeSelectDialog dialog = new RDCTypeSelectDialog(mainFrame, media);
				dialog.setSelectedMedium(lastSelectedMedium); // Recall last selected medium
				dialog.setVisible(true);
				
				if(!dialog.selectionWasMade()) return; // Dialog was dismissed
				
				int mediumIndex = dialog.getSelectedMedium();
				lastSelectedMedium = mediumIndex; // Memorize selected medium
				RDCType selectedType = dialog.getSelectedType();
				
				testSet = parseSimpleInput(file, selectedType);
				testSet.setSequence(sequence);
			//	testSet.setType(selectedType);
				media.get(mediumIndex).addRDCSet(testSet);
										
				refreshMediaPane();

				break;
			case Full:
				AlignmentMedium selectedMedium;
				if(media.size() > 1) {  // Only ask to select medium if there are more than one
					RDCOutputSelectDialog mediumDialog = new RDCOutputSelectDialog(mainFrame, media);
					mediumDialog.setVisible(true);
					
					if(mediumDialog.selectionWasMade())
						selectedMedium = media.get(mediumDialog.getSelectedMedium());
					else
						return; // If no selection was made then stop here
				} else
					selectedMedium = media.get(0);

				Collection<RDCSet> newSets = parseFullInput(file);
				if(newSets == null) {
					JOptionPane.showConfirmDialog(mainFrame, 
							"Failed to extract RDCs from file",
							"Error", JOptionPane.ERROR_MESSAGE);
					return;
				} else {
					for(RDCSet set : newSets)
						selectedMedium.addRDCSet(set);
					
					refreshMediaPane();
				}
				
				break;
			default:
				JOptionPane.showConfirmDialog(mainFrame, 
						"Unknown file format!",
						"Error", JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e) {
			JOptionPane.showConfirmDialog(mainFrame, 
					"Can't resolve the dropped file name!",
					"Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
	}
	
	
	// The function takes the file to be parsed, and the RDCType to be used.
	// Reads in data, sets up RDC objects, and saves them into a new set.
	// If everything went well, returns the newly created RDC set
	private RDCSet parseSimpleInput(File file, RDCType rdcType) {
		BufferedReader br;
		String line;
		Scanner scanner;
		int resNum;
		float rdcValue;
		RDCSet rdcSet = new RDCSet();
		rdcSet.setType(rdcType);
		
		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				scanner = new Scanner(line);
				if(scanner.hasNextInt()) {
					resNum = scanner.nextInt();
					if(scanner.hasNextFloat()) {
						rdcValue = scanner.nextFloat();
					//	System.out.printf("res - %d, value - %f\n", resNum, rdcValue);
						rdcSet.addRDC(resNum, rdcValue);
					} else
						continue; // Something weird - go for the next line
				} else
					continue; // Empty line or comment?
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		if(rdcSet.getCount() == 0)
			return null;
		else
			return rdcSet;
	}

	
	// Parse a file in full format, producing a collection of RDC sets
	private Collection<RDCSet> parseFullInput(File file) {
		BufferedReader br;
		String line;
		Scanner scanner;
		int resNum;
		float rdcValue, uncertValue;
		int atom1Res, atom2Res;
		String atom1Type, atom2Type, res1Type, res2Type;
		Pattern resNamePattern = Pattern.compile("\\w\\w\\w");
		Pattern atomNamePattern = Pattern.compile("[\\w#]+");
		RDCType rdcType;
		
		HashMap<RDCType, RDCSet> sets = new HashMap<RDCType, RDCSet>();
		RDCSet curSet;
		
		try {
			br = new BufferedReader(new FileReader(file));
			while ((line = br.readLine()) != null) {
				scanner = new Scanner(line);
				
				// Read in residue number of the first atom
				// Bail if there is no number to read.
				if(scanner.hasNextInt()) atom1Res = scanner.nextInt();
				else continue;
				
				// Read in residue type of the first atom
				if(scanner.hasNext(resNamePattern)) res1Type = scanner.next(resNamePattern);
				else continue;
				
				// Read in type of the first atom
				if(scanner.hasNext(atomNamePattern)) atom1Type = scanner.next(atomNamePattern);
				else continue;
				
				// Read in residue number of the second atom
				if(scanner.hasNextInt()) atom2Res = scanner.nextInt();
				else continue;
				
				// Read in residue type of the second atom
				if(scanner.hasNext(resNamePattern)) res2Type = scanner.next(resNamePattern);
				else continue;
				
				// Read in type of the second atom
				if(scanner.hasNext(atomNamePattern)) atom2Type = scanner.next(atomNamePattern);
				else continue;
				
				// Read in the RDC value
				if(scanner.hasNextFloat()) rdcValue = scanner.nextFloat();
				else continue;
						
				// Read in the RDC value
				if(scanner.hasNextFloat()) uncertValue = scanner.nextFloat();
				else continue;

			//	System.out.printf("%d %s %s %d %s %s %.3f %.3f\n", atom1Res, res1Type, atom1Type, atom2Res, res2Type, atom2Type, rdcValue, uncertValue);

				// Figure out the RDC type, and the corresponding residue number
				rdcType = RDCTypeRecognizer.recognize(atom1Res, atom1Type, atom2Res, atom2Type);
				resNum = RDCTypeRecognizer.getResNum();
				
				// If we are not getting a RDCType, it means that it's an unknown RDC Type. Bail.
				if(rdcType == null) continue;					
				
				if(sets.containsKey(rdcType))
					curSet = sets.get(rdcType);
				else {
					curSet = new RDCSet();
					curSet.setSequence(sequence);
					curSet.setType(rdcType);
					sets.put(rdcType, curSet);
				}
				
				// Only save the atom types into RDC itself if the detected RDC type is NOT simple
				if(rdcType.isSimple)
					curSet.addRDC(resNum, rdcValue, uncertValue);
				else
					curSet.addRDC(resNum, rdcValue, uncertValue, atom1Type, atom2Type); // Note: Relying on both atoms being on the same residue!
			}
			
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		if(sets.size() == 0)
			return null;
		else
			return sets.values();
	}

	// Determine the file format
	private Format getFileFormat(File file) {
		BufferedReader br;
		String line;
		String [] parts;
		Scanner scanner = new Scanner("");
		
		try {
			br = new BufferedReader(new FileReader(file));
			
			// Skip the lines that don't contain data (comments and such)
			while((line = br.readLine()) != null) {
				scanner = new Scanner(line);
				if(scanner.hasNextInt()) break;
			}
			
			if(line == null) {
				br.close();
				return Format.Unknown;
			}
			
			// Count the number of substrings in line, by splitting it around sets of white-space characters
			line = line.trim(); // Remove leading and trailing whitespace, so it doesn't mess with 'split'
			parts = line.split("[\\s ]+");
			br.close();
			
			if(parts.length == 2)
				return Format.Simple;
			else if(parts.length >= 9)  // This allows comments in the data line. (Normal line gives the length of 9)
				return Format.Full;
		} catch (IOException e) {
			JOptionPane.showConfirmDialog(mainFrame, 
					"Can't read the file!",
					"Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			return Format.ReadError;
		}
		
		return Format.Unknown;
	}
	
	public static float arrayAvg(float [] array) {
		float mean = 0;
		for(int i=0; i<array.length; i++)
			mean += array[i];
		
		return mean / array.length;
	}
	
	public static float arraySTD(float [] array) {
		float mean = arrayAvg(array);
		float sumSquares = 0, delta;
		for(int i=0; i<array.length; i++) {
			delta = array[i] - mean;
			sumSquares += delta*delta;
		}
		
		return (float)Math.sqrt(sumSquares / array.length);
	}
	
	public static void main(String [] argc) throws IOException { 
		new DCMainWindow();
	}

	private class RDCTreeListener implements TreeModelListener {
		@Override
		public void treeNodesChanged(TreeModelEvent e) {
			DefaultMutableTreeNode node;
	        node = (DefaultMutableTreeNode)(e.getTreePath().getLastPathComponent());

	        // Get to the edited node
	        try {
	            int index = e.getChildIndices()[0];
	            node = (DefaultMutableTreeNode)(node.getChildAt(index));
	        } catch (NullPointerException exc) {} 

	        if(node.getLevel() == 2) {  // If a set was edited, revert back to its proper name
				// Figure out which medium the set belongs to by finding the parent of the node.
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
				int mediumIndex = mediaTreeRoot.getIndex(parent); // Get the index of the medium
				int setIndex = parent.getIndex(node); // Get the index of RDC set

				if(media.get(mediumIndex).getCount() > 0)
					node.setUserObject(media.get(mediumIndex).get(setIndex).getTypeString());
				else
					node.setUserObject("(no data)");
	        }
	        else {  // When medium name was edited, save that new name into the correct medium
				int index = mediaTreeRoot.getIndex(node); // Get the index of the medium
				media.get(index).name = (String)(node.getUserObject());
	        }
		}

		@Override
		public void treeNodesInserted(TreeModelEvent arg0) { }

		@Override
		public void treeNodesRemoved(TreeModelEvent arg0) { }

		@Override
		public void treeStructureChanged(TreeModelEvent arg0) { }
	};
	
	// Filter class for showing only files with extension "state" in a FileChooser
	public class StateFilter extends FileFilter {
		private String getExtension(File f) {
	        String ext = null;
	        String s = f.getName();
	        int i = s.lastIndexOf('.');

	        if (i > 0 &&  i < s.length() - 1) {
	            ext = s.substring(i+1).toLowerCase();
	        }
	        return ext;
	    }
		
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
		        return true;
		    }

		    String extension = getExtension(f);
		    if (extension != null) {
		        if (extension.equals("state")) {
		                return true;
		        } else {
		            return false;
		        }
		    }

		    return false;
		}
		
		public String getDescription() {
			return "State files (*.state)";
		}
	}
	
	
	// Filter class for showing only files with extension "pdb" in a FileChooser
	public class PDBFilter extends FileFilter {
		private String getExtension(File f) {
	        String ext = null;
	        String s = f.getName();
	        int i = s.lastIndexOf('.');

	        if (i > 0 &&  i < s.length() - 1) {
	            ext = s.substring(i+1).toLowerCase();
	        }
	        return ext;
	    }
		
		@Override
		public boolean accept(File f) {
			if (f.isDirectory()) {
		        return true;
		    }

		    String extension = getExtension(f);
		    if (extension != null) {
		        if (extension.equals("pdb") || extension.equals("sa")) {
		                return true;
		        } else {
		            return false;
		        }
		    }

		    return false;
		}
		
		public String getDescription() {
			return "PDB files (*.pdb)";
		}
	}

}
