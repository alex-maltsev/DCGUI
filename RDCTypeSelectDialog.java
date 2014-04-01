import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class RDCTypeSelectDialog extends JDialog {
	private int mediumIndex; // Used to return result of medium selection
	private RDCType type;  // Used to return result of RDC type selection
	private ArrayList<AllignmentMedium> media;
	private boolean selectionMade = false;
	JComboBox comboType, comboMedium;
	
	public RDCTypeSelectDialog(JFrame owner, ArrayList<AllignmentMedium> media) {
		super(owner, "RDC set info", true); // Create a modal dialog with title
		
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

		contentPane.add(Box.createRigidArea(new Dimension(0, 10)));

		label = new JLabel("Select RDC type");
		label.setAlignmentX(CENTER_ALIGNMENT);
		contentPane.add(label);
		
		contentPane.add(Box.createRigidArea(new Dimension(0, 5)));
		
		ArrayList<String> types = new ArrayList<String>();
		for(RDCType type: RDCType.values())
			types.add(type.name);
		comboType = new JComboBox(types.toArray());
		comboType.setAlignmentX(CENTER_ALIGNMENT);
		contentPane.add(comboType);
		
		contentPane.add(Box.createRigidArea(new Dimension(0, 15)));
		
		// The two buttons are placed into their own pane
		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.X_AXIS));
				
		JButton button = new JButton("OK");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mediumIndex = comboMedium.getSelectedIndex();
				type = RDCType.RDCTypeFromString((String)comboType.getSelectedItem());
				selectionMade = true;
				RDCTypeSelectDialog.this.dispose();
			}		
		});
		bottomPane.add(button);
		
		bottomPane.add(Box.createRigidArea(new Dimension(5, 0)));
		
		button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RDCTypeSelectDialog.this.dispose();
			}		
		});
		bottomPane.add(button);
		bottomPane.setAlignmentX(CENTER_ALIGNMENT);
		this.add(bottomPane);
		this.pack();
	}

	// To be called before making the dialog visible to set the starting selection for medium
	public void setSelectedMedium(int index) {
		if(index < media.size())
			mediumIndex = index;
		else
			mediumIndex = 0;
		
		comboMedium.setSelectedIndex(mediumIndex);
	}
	
	public int getSelectedMedium() {
		return mediumIndex;
	}
	
	public RDCType getSelectedType() {
		return type;
	}
	
	public boolean selectionWasMade() {
		return selectionMade;
	}
}
