package controller;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
//this class is a gui panel displaying tips for using the application
public class IntroductionPanel extends JPanel{
	
	Font font_Ariel32B = new Font("Ariel", Font.BOLD, 32);
	Font font_Ariel16P = new Font("Ariel", Font.PLAIN, 16);
	
	public IntroductionPanel() {
		JLabel labelTitle = new JLabel();
		JTextArea textArea = new JTextArea();
		
		labelTitle.setText("Welcome to the Game Hub!");
		labelTitle.setFont(font_Ariel32B);
		
		textArea.setText("- Click \"Play Games\" above to start setting up a game."
				+ "\n- You can return to this screen by choosing \"Return to Intro Screen\" from the \"Play Games\" menu."
				+ "\n- Set up a text based chat room or join another chat room by clicking \"Open Chat Room\" from the menu above."
				+ "\n- You can read the rules of a game from the \"Rules\" menu."
				+ "\n- You can save your score as of the current round while playing single player. Just click \"File\" and then \"Save Game\" from the menu above."
				+ "\n- Thank you for playing!"
				);
		textArea.setFont(font_Ariel16P);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		textArea.setCursor(null);
		textArea.setOpaque(false);
		textArea.setFocusable(false);
		
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		//add ui elements to panel using grid bag constraints
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.gridx = 0;
		gbc.gridy = 0;
		add(labelTitle, gbc);
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.insets = new Insets(20, 20, 20, 20);
		gbc.weighty = 4;
		gbc.gridy = 1;
		add(textArea, gbc);
	}
}
