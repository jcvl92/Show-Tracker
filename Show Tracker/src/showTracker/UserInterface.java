package showTracker;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.Box;
import java.awt.BorderLayout;
import javax.swing.JTextArea;
import javax.swing.JTree;

public class UserInterface {

	private JFrame frame;
	private static JTree tree;
	private static JTextArea text;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UserInterface window = new UserInterface();
					window.frame.setVisible(true);
					
					//this needs to happen last
					new Main(text, tree).run();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public UserInterface() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Box horizontalBox = Box.createHorizontalBox();
		frame.getContentPane().add(horizontalBox, BorderLayout.CENTER);
		
		tree = new JTree();
		horizontalBox.add(tree);
		
		text = new JTextArea();
		text.setEditable(false);
		text.setLineWrap(true);
		text.setWrapStyleWord(true);
		horizontalBox.add(text);
	}

}