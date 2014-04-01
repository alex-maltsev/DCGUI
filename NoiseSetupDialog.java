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
import javax.swing.JTextField;


public class NoiseSetupDialog extends JDialog {
	private boolean setupCompleted = false;
	private JTextField numStrucsField, angleField;
	private float coneAngle;
	private int numStrucs;
	
	public NoiseSetupDialog(JFrame owner) {
		super(owner, "Structural Noise Setup", true); // Create a modal dialog with title
		
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		Point loc = owner.getLocation();
		loc.x += 50;
		loc.y += 50;
		this.setLocation(loc);
		this.setSize(300, 150);
		JPanel contentPane = (JPanel)getContentPane();
		contentPane.setBorder(BorderFactory.createEmptyBorder(10,30,15,30));
		
		JLabel label = new JLabel("Number of structures");
		label.setAlignmentX(CENTER_ALIGNMENT);
		contentPane.add(label);
		
		contentPane.add(Box.createRigidArea(new Dimension(0, 5)));
		
		numStrucsField = new JTextField(6);
		numStrucsField.setAlignmentX(CENTER_ALIGNMENT);
		numStrucsField.setText("100");
		contentPane.add(numStrucsField);
		
		contentPane.add(Box.createRigidArea(new Dimension(0, 5)));

		label = new JLabel("Cone angle for structural noise");
		label.setAlignmentX(CENTER_ALIGNMENT);
		contentPane.add(label);
		
		contentPane.add(Box.createRigidArea(new Dimension(0, 5)));
		
		angleField = new JTextField(6);
		angleField.setAlignmentX(CENTER_ALIGNMENT);
		angleField.setText("5.0");
		contentPane.add(angleField);
		
		contentPane.add(Box.createRigidArea(new Dimension(0, 15)));
		
		// The two buttons are placed into their own pane
		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.X_AXIS));
				
		JButton button = new JButton("OK");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					numStrucs = Integer.parseInt(numStrucsField.getText());
					coneAngle = Float.parseFloat(angleField.getText());
				} catch(Exception exc) {
					return; // User entered non-number values - refuse to close the dialog
				}
				setupCompleted = true;
				NoiseSetupDialog.this.dispose();
			}		
		});
		bottomPane.add(button);
		
		bottomPane.add(Box.createRigidArea(new Dimension(5, 0)));
		
		button = new JButton("Cancel");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setupCompleted = false;
				NoiseSetupDialog.this.dispose();
			}		
		});
		
		bottomPane.add(button);
		bottomPane.setAlignmentX(CENTER_ALIGNMENT);
		this.add(bottomPane);
		this.pack();
	}

	public int getNumStrucs() {
		return numStrucs;
	}
	
	public float getConeAngle() {
		return coneAngle;
	}
	
	public boolean wasSetupCompleted() {
		return setupCompleted;
	}
}
