import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.*;


public class SequenceDialog extends JDialog implements WindowListener, ActionListener {
	private static final long serialVersionUID = -5343139144708349723L;
	private ProtSequence sequence;
	private JTextArea textArea;
	private JLabel infoLabel;

	private final static String acceptButtonText = "Accept";
	private final static String cleanupButtonText = "Format";
	
	public SequenceDialog(JFrame owner) {
		super(owner, "Edit protein sequence", true); // Create a modal dialog with title
		
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		
		Point loc = owner.getLocation();
		loc.x += 50;
		loc.y += 50;
		this.setLocation(loc);
		this.setSize(400, 230);
		JPanel contentPane = (JPanel)getContentPane();
		contentPane.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
				
		textArea = new JTextArea();
		textArea.setSize(0, 80);
		textArea.setFont(new Font("Courier", Font.BOLD, 15));
		textArea.setLineWrap(true);
		
		JScrollPane areaScrollPane = new JScrollPane(textArea);
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		areaScrollPane.setAlignmentX(RIGHT_ALIGNMENT);
		
        this.add(areaScrollPane);
		this.add(Box.createRigidArea(new Dimension(0, 15)));
		
		// The two buttons are placed into their own pane
		JPanel bottomPane = new JPanel();
		bottomPane.setLayout(new BoxLayout(bottomPane, BoxLayout.X_AXIS));
		
		infoLabel = new JLabel("");
		bottomPane.add(infoLabel);
		
		bottomPane.add(Box.createHorizontalGlue());
		
		JButton button = new JButton(cleanupButtonText);
		button.setSize(60, 30);
		button.addActionListener(this);
		bottomPane.add(button);
		
		bottomPane.add(Box.createRigidArea(new Dimension(5, 0)));
		
		button = new JButton(acceptButtonText);
		button.setSize(60, 30);
		button.addActionListener(this);
		bottomPane.add(button);

		bottomPane.setAlignmentX(RIGHT_ALIGNMENT);
		this.add(bottomPane);
		
		addWindowListener(this);
	}
	
	public void setSequence(ProtSequence sequence) {
		this.sequence = sequence;
		textArea.setText(sequence.getSequence());
		infoLabel.setText("Length: " + sequence.getLength());
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getActionCommand().equals(cleanupButtonText)) {  // Button 'Check' was pressed
			// We don't want to affect the global sequence yet, so create a temporary protein sequence
			ProtSequence tempSeq = new ProtSequence(textArea.getText());  // based on the content of the text area
			textArea.setText(tempSeq.getSequence());  // Replace the content of text area with cleaned-up sequence
			infoLabel.setText("Length: " + tempSeq.getLength());
		} else {  // Button 'Accept' was pressed
			sequence.setSequence(textArea.getText());  // Simply initialize the sequence with the text in the text area	
			this.dispose();
		}
	}

	@Override
	public void windowClosing(WindowEvent event) {
	//	System.out.println("Dialog closing");
		this.dispose();
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
	//	System.out.println("Dialog closed");
	}


	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
