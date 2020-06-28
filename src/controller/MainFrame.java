package controller;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.BlackjackSavedState;
import model.SixCardGolfSavedState;
//this class controls the frame, menu system, and navigation between most screens of the application
public class MainFrame extends JFrame implements WindowListener, ActionListener{
	CardLayout cardLayout = new CardLayout();
	
	ChatFrame chatWindow;
	
	public int visibleCard = 0;
	
	JMenuBar menuBar = new JMenuBar();
	JMenu fileMenu = new JMenu("File");
	JMenuItem itemFileSave = new JMenuItem("Save Game");
	JMenuItem itemFileLoad = new JMenuItem("Load Game");
	JMenuItem itemExit = new JMenuItem("Exit");
	JMenu playGamesMenu = new JMenu("Play Games");
	JMenuItem itemIntroScreen = new JMenuItem("Return to Intro Screen");
	JMenuItem itemPlayBlackjack = new JMenuItem("Blackjack");
	JMenuItem itemPlaySixCardGolf = new JMenuItem("Six Card Golf");
	JMenuItem itemOpenChat = new JMenuItem("Open Chat Window");
	JMenu rulesMenu = new JMenu("Rules");
	JMenuItem itemRulesBlackjack = new JMenuItem("How to Play Blackjack");
	JMenuItem itemRulesSixCardGolf = new JMenuItem("How to Play Six Card Golf");
	
	JPanel contentPanel = new JPanel();
	IntroductionPanel introPanel = new IntroductionPanel();
	BlackjackSetupPanel blackjack_SetupPanel = new BlackjackSetupPanel();
	public BlackjackGamePanel blackjack_GamePanel = new BlackjackGamePanel();
	SixCardGolfSetupPanel sixCardGolf_SetupPanel = new SixCardGolfSetupPanel();
	public SixCardGolfGamePanel sixCardGolf_GamePanel = new SixCardGolfGamePanel();
	
	public MainFrame() {
		super("GameHubApp");
		addWindowListener(this);
		
		itemFileSave.addActionListener(this);
		itemFileLoad.addActionListener(this);
		itemExit.addActionListener(this);
		itemIntroScreen.addActionListener(this);
		itemPlayBlackjack.addActionListener(this);
		itemPlaySixCardGolf.addActionListener(this);
		itemRulesBlackjack.addActionListener(this);
		itemRulesSixCardGolf.addActionListener(this);
		itemOpenChat.addActionListener(this);
		
		contentPanel.setLayout(cardLayout);

		contentPanel.add(introPanel, "0");
		contentPanel.add(blackjack_SetupPanel, "1");
		contentPanel.add(blackjack_GamePanel, "2");
		contentPanel.add(sixCardGolf_SetupPanel, "3");
		contentPanel.add(sixCardGolf_GamePanel, "4");
		//show intro card
		cardLayout.show(contentPanel, "0");
		visibleCard = 0;
		//add menu bar to frame
		setJMenuBar(makeMenuBar());
		
		add(contentPanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setSize(800, 600);
		setVisible(true);
	}
	//creates and returns the menu bar for the frame
	private JMenuBar makeMenuBar() {
		//add menu items to menus
		fileMenu.add(itemFileSave);
		fileMenu.add(itemFileLoad);
		fileMenu.addSeparator();
		fileMenu.add(itemExit);
		playGamesMenu.add(itemIntroScreen);
		playGamesMenu.addSeparator();
		playGamesMenu.add(itemPlayBlackjack);
		playGamesMenu.add(itemPlaySixCardGolf);
		rulesMenu.add(itemRulesBlackjack);
		rulesMenu.add(itemRulesSixCardGolf);
		//add menus to menu bar
		menuBar.add(fileMenu);
		menuBar.add(playGamesMenu);
		menuBar.add(rulesMenu);
		menuBar.add(itemOpenChat);
		
		return menuBar;
	}
	//creates and displays the contents of a file in a scrollable window
	public void showGameRules(String fileName, String gameName) {
		try {
			//read text from file
			JTextArea rulesText = new JTextArea("");
			InputStream iStream = this.getClass().getResourceAsStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
			String rulesLine = br.readLine();
			while (rulesLine != null) {
				rulesText.append("\n" + rulesLine);
				rulesLine = br.readLine();
			}
			iStream.close();
			//put text area into a scroll pane
			JScrollPane scrollPane = new JScrollPane(rulesText);
			rulesText.setLineWrap(true);  
			rulesText.setWrapStyleWord(true);
			rulesText.setEditable(false);
			scrollPane.setPreferredSize(new Dimension( 500, 500 ));
			rulesText.setCaretPosition(0);
			JOptionPane.showMessageDialog(null, scrollPane, "How to play " + gameName, JOptionPane.INFORMATION_MESSAGE);
		}
		catch(FileNotFoundException e) {
			String rules = "Error: File " + fileName + " not found!";
			JOptionPane.showMessageDialog(null, rules, rules, JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		} catch (IOException e) {
			String rules = "Error: File " + fileName + " not found!";
			JOptionPane.showMessageDialog(null, rules, rules, JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}

	}
	//button event handling
	@Override
	public void actionPerformed(ActionEvent event) {
		if(event.getSource() == itemExit) {
			int action = JOptionPane.showConfirmDialog(MainFrame.this, "Are you sure you want to exit the application?");			
			if(action == JOptionPane.YES_OPTION)
				System.exit(0);
		}
		else if(event.getSource() == itemIntroScreen) {
			//close connections for games
			blackjack_GamePanel.close();
			sixCardGolf_GamePanel.close();
			//show intro screen panel
			cardLayout.show(contentPanel, "0");
			visibleCard = 0;
		}
		else if(event.getSource() == itemPlayBlackjack) {
			//close connections for games
			blackjack_GamePanel.close();
			sixCardGolf_GamePanel.close();
			//show blackjack setup panel
			cardLayout.show(contentPanel, "1");
			visibleCard = 1;
		}
		else if(event.getSource() == itemPlaySixCardGolf) {
			//close connections for games
			blackjack_GamePanel.close();
			sixCardGolf_GamePanel.close();
			//show six card golf setup panel
			cardLayout.show(contentPanel, "3");
			visibleCard = 3;
		}
		else if(event.getSource() == itemOpenChat) {
			if(chatWindow != null) {
				chatWindow.setVisible(true);
				chatWindow.setState(Frame.NORMAL);
			}
			else
				chatWindow = new ChatFrame();
		}
		else if(event.getSource() == itemFileSave) {
			if(visibleCard == 2) {
				if(blackjack_GamePanel.isSinglePlayer() == true) {
					System.out.println("saving blackjack");
					//open file chooser window
					FileNameExtensionFilter filter = new FileNameExtensionFilter("Game Hub Save Files (.ghb)", "ghb");
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileFilter(filter);
		            int saveOption = fileChooser.showSaveDialog(this);
		            
		            if(saveOption == JFileChooser.APPROVE_OPTION) {
		            	File file = fileChooser.getSelectedFile();
		            	try {
							blackjack_GamePanel.saveStateToFile(file);
						} catch (IOException e) {
							JOptionPane.showConfirmDialog(this,
									"An error occurred! Your progress was not saved.", 
									"Save Game Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						}
		            }
				}
				else {
					JOptionPane.showConfirmDialog(this,
							"You cannot save game progress in multiplayer! Your progress was not saved.", 
							"Save Game Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
				}
			}
			else if(visibleCard == 4) {
				if(sixCardGolf_GamePanel.isSinglePlayer() == true) {
					System.out.println("saving six card golf");
					//open file chooser
					FileNameExtensionFilter filter = new FileNameExtensionFilter("Game Hub Save Files (.ghb)", "ghb");
					JFileChooser fileChooser = new JFileChooser();
					fileChooser.setFileFilter(filter);
		            int saveOption = fileChooser.showSaveDialog(this);
		            
		            if(saveOption == JFileChooser.APPROVE_OPTION) {
		            	File file = fileChooser.getSelectedFile();
		            	try {
							sixCardGolf_GamePanel.saveStateToFile(file);
						} catch (IOException e) {
							JOptionPane.showConfirmDialog(this,
									"An error occurred! Your progress was not saved.", 
									"Save Game Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						}
		            }
				}
				else {
					JOptionPane.showConfirmDialog(this,
							"You cannot save game progress in multiplayer! Your progress was not saved.", 
							"Save Game Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		else if(event.getSource() == itemFileLoad) {
			blackjack_GamePanel.close();
			sixCardGolf_GamePanel.close();
			//open file chooser
			FileNameExtensionFilter filter = new FileNameExtensionFilter("Game Hub Save Files (.ghb)", "ghb");
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(filter);
			int loadOption = fileChooser.showOpenDialog(this);
            if(loadOption == JFileChooser.APPROVE_OPTION) {
            	//open the selected file
            	File file = fileChooser.getSelectedFile();
    			FileInputStream fis;
    			ObjectInputStream ois;
				try {
					fis = new FileInputStream(file);
					ois = new ObjectInputStream(fis);
					Object savedState = ois.readObject();
					ois.close();
					fis.close();
					//load and show game based upon what type of object is saved in the file
					if(savedState instanceof BlackjackSavedState) {
						System.out.println("loading blackjack");
		            	try {
							blackjack_GamePanel.loadStateFromFile(file);
							cardLayout.show(contentPanel, "2");
							visibleCard = 2;
						} catch (IOException e) {
							JOptionPane.showConfirmDialog(this,
									"An error occurred! The game progress could not be loaded.", 
									"Load Game Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						}
					}
					else if(savedState instanceof SixCardGolfSavedState){
						System.out.println("loading six card golf");
						try {
							sixCardGolf_GamePanel.loadStateFromFile(file);
							cardLayout.show(contentPanel, "4");
							visibleCard = 4;
						} catch(IOException e) {
							JOptionPane.showConfirmDialog(this,
									"An error occurred! The game progress could not be loaded.", 
									"Load Game Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
							e.printStackTrace();
						}
					}
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
            }
		}
		else if(event.getSource() == itemRulesBlackjack) {
			showGameRules("/rules/blackjack_rules.txt", "Blackjack");
		}
		else if(event.getSource() == itemRulesSixCardGolf) {
			showGameRules("/rules/sixcardgolf_rules.txt", "Six Card Golf");
		}
	}
	//window event handling
	@Override
	public void windowActivated(WindowEvent e) {
		
	}
	@Override
	public void windowClosed(WindowEvent e) {
		
	}
	@Override
	public void windowClosing(WindowEvent e) {
		//Send a window closing event to the chat window since closing the main window will terminate the application.
		if(chatWindow != null) {
			chatWindow.dispatchEvent(new WindowEvent(chatWindow, WindowEvent.WINDOW_CLOSING));
		}
		//close connections for blackjack game
		blackjack_GamePanel.close();
		sixCardGolf_GamePanel.close();
	}
	@Override
	public void windowDeactivated(WindowEvent e) {
		
	}
	@Override
	public void windowDeiconified(WindowEvent e) {
		
	}
	@Override
	public void windowIconified(WindowEvent e) {
		
	}
	@Override
	public void windowOpened(WindowEvent e) {
		
	}
}
