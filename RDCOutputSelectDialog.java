import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class RDCOutputSelectDialog extends JDialog {
	private int mediumIndex; // Used to return result of medium selection
	private ArrayList<AllignmentMedium> media;
	private boolean selectionMade = false;
	JComboBox comboMedium;
	
	public RDCOutputSelectDialog(JFrame owner, ArrayList<AllignmentMedium> media) {
		super(owner, "Select medium", true); // Create a modal dialog with title
		
		this.media = media;
		
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		Point loc = owner.getLocation();
		loc.x += 50;
		loc.y += 50;
		this.setLocation(loc);
		this.setSize(300, 150);
		JPanel contentPane = (JPanel)getContentPane();
		contentPane.setBorder(BorderFactory.createEmptyBorder(10,30,15,30));
		
		JLabel label = new JLabel("Select alignment medium");
		label.setAlignmentX(CENTER_ALIGNMENT);
		contentPane.add(label);
		
		contentPane.add(Box.createRigidArea(new Dimension(0, 5)));

		String [] mediaNames = new String[media.size()];
		for(int i=0; i<mediaNames.length; i++)
			mediaNames[i] = media.get(i).name;
		comboMedium = new JComboBox(mediaNames);
		comboMedium.setAlignmentX(CENTER_ALIGNMENT);
		contentPane.add(comboMedium);
		
		contentPane.add(Box.createRigidArea(new Dimension(0, 15)));
		
		// The two buttons are placed into their own pane
		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.X_AXIS));
				
		JButton button = new JButton("OK");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediumIndex = comboMedium.getSelectedIndex();
				selectionMade = true;
				RDCOutputSelectDialog.this.dispose();
			}		
		});
		bottomPane.add(button);
		
		bottomPane.add(Box.createRigidArea(new Dimension(5, 0)));
		
		button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RDCOutputSelectDialog.this.dispose();
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
	
	public boolean selectionWasMade() {
		return selectionMade;
	}

}
