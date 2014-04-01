import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class DCOutputDialog extends JDialog implements WindowListener {
	private final DCOutput dcOut;
	private final JFrame owner;
	
	public DCOutputDialog(JFrame parent, DCOutput dcOutArg) {
		super(parent, "DC Results", true);
		dcOut = dcOutArg;
		owner = parent;
		
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		Point loc = owner.getLocation();
		loc.x += 50;
		loc.y += 50;
		this.setLocation(loc);
		this.setSize(300, 150);
		JPanel contentPane = (JPanel)getContentPane();
		contentPane.setBorder(BorderFactory.createEmptyBorder(10,30,15,30));
		
		JLabel label = new JLabel(String.format("Q-factor %.3f, Rcorr %.3f, rms %.3f\n", dcOut.Qfactor, dcOut.Rcorr, dcOut.rms));
		label.setAlignmentX(CENTER_ALIGNMENT);
		contentPane.add(label);

		contentPane.add(Box.createRigidArea(new Dimension(0, 5)));
		
		label = new JLabel(String.format("Da %.3f, Rh %.3f\n", dcOut.Da, dcOut.Rh));
		label.setAlignmentX(CENTER_ALIGNMENT);
		contentPane.add(label);
	
		contentPane.add(Box.createRigidArea(new Dimension(0, 10)));
		
		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.X_AXIS));

		JButton button = new JButton("Save output");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Bring up the Save File dialog
				JFileChooser browseDialog = new JFileChooser();
				int returnVal = browseDialog.showSaveDialog(owner);
				if(returnVal != JFileChooser.APPROVE_OPTION) return; // Stop if no selection was made
				File file = browseDialog.getSelectedFile();
				try {
					copyFile(dcOut.getResultFile(), file);
				} catch(IOException exc) {
					exc.printStackTrace();
				}
			}
		});
		bottomPane.add(button);
		bottomPane.add(Box.createRigidArea(new Dimension(1, 0)));

		button = new JButton("Close");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dcOut.getResultFile().delete();
				DCOutputDialog.this.dispose();				
			}
		});
		bottomPane.add(button);
		
		bottomPane.setAlignmentX(CENTER_ALIGNMENT);
		add(bottomPane);
		pack();
		
		addWindowListener(this);
	}
	
	public static void copyFile(File sourceFile, File destFile) throws IOException {
	    if(!destFile.exists()) {
	        destFile.createNewFile();
	    }

	    FileChannel source = null;
	    FileChannel destination = null;

	    try {
	        source = new FileInputStream(sourceFile).getChannel();
	        destination = new FileOutputStream(destFile).getChannel();
	        destination.transferFrom(source, 0, source.size());
	    }
	    finally {
	        if(source != null) {
	            source.close();
	        }
	        if(destination != null) {
	            destination.close();
	        }
	    }
	}


	// Using the "window closing" event to see when user clicks standard "x" to close the dialog;
	// deleting the temporary result file
	@Override
	public void windowClosing(WindowEvent arg0) {
		dcOut.getResultFile().delete();		
	}

	@Override
	public void windowClosed(WindowEvent arg0) { }

	@Override
	public void windowDeactivated(WindowEvent arg0) { }

	@Override
	public void windowActivated(WindowEvent arg0) {	}
	
	@Override
	public void windowDeiconified(WindowEvent arg0) { }

	@Override
	public void windowIconified(WindowEvent arg0) {	}

	@Override
	public void windowOpened(WindowEvent arg0) { }
}
