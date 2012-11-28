package org.xcom.mod.gui.dialogues;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;

import org.xcom.mod.Main;
import org.xcom.mod.exceptions.XmlSaveException;
import org.xcom.mod.gui.XCMGUI;
import org.xcom.mod.gui.workers.DecompressInBackGround;
import org.xcom.mod.gui.workers.ExtractInBackGround;
import org.xcom.mod.pojos.Config;

import com.lipstikLF.LipstikLookAndFeel;
import com.lipstikLF.theme.DefaultTheme;
import com.lipstikLF.theme.KlearlooksTheme;
import com.lipstikLF.theme.LightGrayTheme;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class SettingsDialog extends JDialog {
	
	private static final long serialVersionUID = 1L;
	private JTextField textFieldName;
	private JTextField textFieldInstall;
	private JTextField textFieldUnpacked;
	
	private final ButtonGroup themeButtonGroup = new ButtonGroup();
	private JTextField textFieldDecompressor;
	private JTextField textFieldExtractor;
	
	private SettingsDialog THIS = this;
	private JButton close;
	
	public SettingsDialog(final Config config) {
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				textFieldName.setText(config.getAuthor());
				textFieldInstall.setText(config.getXcomPath());
				textFieldUnpacked.setText(config.getUnpackedPath());
				textFieldDecompressor.setText(config.getCompressorPath());
				textFieldExtractor.setText(config.getExtractorPath());
				setLocationRelativeTo(XCMGUI.getFrame());
			}
		});
		setMinimumSize(new Dimension(275, 275));
		setResizable(false);
		
		initUI();
		
	}
	
	public final void initUI() {
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		setTitle("XCMM Config Settings");
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane);
		
		JPanel generalTab = new JPanel();
		tabbedPane.addTab("General", null, generalTab, null);
		generalTab.setPreferredSize(new Dimension(400, 300));
		generalTab.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "",
					TitledBorder.LEADING, TitledBorder.TOP, null, null));
		generalTab.setLayout(null);
		
		close = new JButton("Cancel");
		close.setBounds(167, 183, 89, 23);
		generalTab.add(close);
		close.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});
		
		close.setAlignmentX(0.5f);
		
		Box getAuthorHBox = Box.createHorizontalBox();
		getAuthorHBox.setBounds(10, 11, 246, 20);
		generalTab.add(getAuthorHBox);
		
		JLabel lblYourAuthorName = new JLabel("Your author name:");
		getAuthorHBox.add(lblYourAuthorName);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		getAuthorHBox.add(horizontalStrut);
		
		textFieldName = new JTextField();
		lblYourAuthorName.setLabelFor(textFieldName);
		getAuthorHBox.add(textFieldName);
		textFieldName.setColumns(10);
		
		JButton btnSaveSettings = new JButton("Save");
		btnSaveSettings.addActionListener(new SaveAction());
		btnSaveSettings.setBounds(10, 183, 89, 23);
		generalTab.add(btnSaveSettings);
		
		textFieldInstall = new JTextField();
		textFieldInstall.setBounds(10, 80, 246, 20);
		generalTab.add(textFieldInstall);
		textFieldInstall.setColumns(10);
		
		Box horizontalXCOMBrowse = Box.createHorizontalBox();
		horizontalXCOMBrowse.setBounds(10, 55, 246, 23);
		generalTab.add(horizontalXCOMBrowse);
		horizontalXCOMBrowse.setMinimumSize(new Dimension(235, 0));
		horizontalXCOMBrowse.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		JLabel lblNewLabel = new JLabel("XCOM installation path:");
		horizontalXCOMBrowse.add(lblNewLabel);
		lblNewLabel.setHorizontalAlignment(SwingConstants.LEFT);
		lblNewLabel.setLabelFor(textFieldInstall);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		horizontalXCOMBrowse.add(horizontalGlue);
		
		JButton browseButton2 = new JButton("Browse");
		browseButton2.setAlignmentX(Component.RIGHT_ALIGNMENT);
		horizontalXCOMBrowse.add(browseButton2);
		
		Box horizontalUnpackedBrowse = Box.createHorizontalBox();
		horizontalUnpackedBrowse.setBounds(10, 124, 246, 23);
		generalTab.add(horizontalUnpackedBrowse);
		
		JLabel lblUnpackedGameResources = new JLabel("Unpacked game resources:");
		lblUnpackedGameResources.setAlignmentX(Component.CENTER_ALIGNMENT);
		horizontalUnpackedBrowse.add(lblUnpackedGameResources);
		lblUnpackedGameResources.setHorizontalTextPosition(SwingConstants.LEFT);
		lblUnpackedGameResources.setHorizontalAlignment(SwingConstants.LEFT);
		
		textFieldUnpacked = new JTextField();
		textFieldUnpacked.setBounds(10, 152, 246, 20);
		generalTab.add(textFieldUnpacked);
		textFieldUnpacked.setColumns(10);
		lblUnpackedGameResources.setLabelFor(textFieldUnpacked);
		
		Component horizontalGlue_1 = Box.createHorizontalGlue();
		horizontalUnpackedBrowse.add(horizontalGlue_1);
		
		JButton browseButton = new JButton("Browse");
		horizontalUnpackedBrowse.add(browseButton);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 42, 246, 20);
		generalTab.add(separator);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(10, 111, 246, 20);
		generalTab.add(separator_1);
		
		JPanel toolsTab = new JPanel();
		tabbedPane.addTab("Gildor's Tools", null, toolsTab, null);
		toolsTab.setRequestFocusEnabled(false);
		toolsTab.setVisible(false);
		toolsTab.setEnabled(false);
		toolsTab.setLayout(null);
		
		JSeparator separator_3 = new JSeparator();
		separator_3.setBounds(10, 42, 246, 10);
		toolsTab.add(separator_3);
		
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.setBounds(10, 55, 246, 23);
		horizontalBox.setMinimumSize(new Dimension(235, 0));
		horizontalBox.setAlignmentX(0.0f);
		toolsTab.add(horizontalBox);
		
		JLabel lblUnrealPackageDecompresor = new JLabel("Unreal Package Decompresor:");
		lblUnrealPackageDecompresor.setHorizontalAlignment(SwingConstants.LEFT);
		horizontalBox.add(lblUnrealPackageDecompresor);
		
		Component horizontalGlue_2 = Box.createHorizontalGlue();
		horizontalBox.add(horizontalGlue_2);
		
		JButton button = new JButton("Browse");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				
				fc.setCurrentDirectory(new java.io.File(System.getProperty("user.dir")));
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				
				int returnVal = fc.showOpenDialog(THIS);
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					java.io.File file = fc.getSelectedFile();
					textFieldDecompressor.setText(file.getAbsolutePath());
				}
			}
		});
		button.setAlignmentX(1.0f);
		horizontalBox.add(button);
		
		textFieldDecompressor = new JTextField();
		textFieldDecompressor.setBounds(10, 80, 246, 20);
		textFieldDecompressor.setText((String) null);
		textFieldDecompressor.setColumns(10);
		toolsTab.add(textFieldDecompressor);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setBounds(10, 111, 246, 10);
		toolsTab.add(separator_2);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		horizontalBox_1.setMinimumSize(new Dimension(235, 0));
		horizontalBox_1.setAlignmentX(0.0f);
		horizontalBox_1.setBounds(10, 124, 246, 23);
		toolsTab.add(horizontalBox_1);
		
		JLabel lblUnrealPackageExtractor = new JLabel("Unreal Package Extractor:");
		lblUnrealPackageExtractor.setHorizontalAlignment(SwingConstants.LEFT);
		horizontalBox_1.add(lblUnrealPackageExtractor);
		
		Component horizontalGlue_3 = Box.createHorizontalGlue();
		horizontalBox_1.add(horizontalGlue_3);
		
		JButton button_1 = new JButton("Browse");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				
				fc.setCurrentDirectory(new java.io.File(System.getProperty("user.dir")));
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				
				int returnVal = fc.showOpenDialog(THIS);
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					java.io.File file = fc.getSelectedFile();
					textFieldExtractor.setText(file.getAbsolutePath());
				}
			}
		});
		button_1.setAlignmentX(1.0f);
		horizontalBox_1.add(button_1);
		
		textFieldExtractor = new JTextField();
		textFieldExtractor.setText((String) null);
		textFieldExtractor.setColumns(10);
		textFieldExtractor.setBounds(10, 152, 246, 20);
		toolsTab.add(textFieldExtractor);
		
		JButton button_2 = new JButton("Save");
		button_2.addActionListener(new SaveAction());
		button_2.setBounds(10, 183, 89, 23);
		toolsTab.add(button_2);
		
		JPanel themeTab = new JPanel();
		tabbedPane.addTab("Themes", null, themeTab, null);
		tabbedPane.setEnabledAt(2, false);
		themeTab.setVisible(false);
		themeTab.setEnabled(false);
		themeTab.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP,
					null, null));
		
		JRadioButton rdbtnNewRadioButton_1 = new JRadioButton("System default");
		rdbtnNewRadioButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnsupportedLookAndFeelException ex) {
					ex.printStackTrace(System.err);
				}
				
				SwingUtilities.updateComponentTreeUI(XCMGUI.getFrame());
				SwingUtilities.updateComponentTreeUI(THIS);
				XCMGUI.getFrame().pack();
			}
			
		});
		themeButtonGroup.add(rdbtnNewRadioButton_1);
		
		JRadioButton rdbtnNewRadioButton_2 = new JRadioButton("Lipstik - Default");
		rdbtnNewRadioButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					UIManager.setLookAndFeel("com.lipstikLF.LipstikLookAndFeel");
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnsupportedLookAndFeelException ex) {
					ex.printStackTrace(System.err);
				}
				
				LipstikLookAndFeel.setMyCurrentTheme(new DefaultTheme());
				
				SwingUtilities.updateComponentTreeUI(XCMGUI.getFrame());
				SwingUtilities.updateComponentTreeUI(THIS);
				
				XCMGUI.getFrame().pack();
			}
		});
		themeButtonGroup.add(rdbtnNewRadioButton_2);
		
		JRadioButton rdbtnNewRadioButton = new JRadioButton("Lipstik - Light grey");
		rdbtnNewRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					UIManager.setLookAndFeel("com.lipstikLF.LipstikLookAndFeel");
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnsupportedLookAndFeelException ex) {
					ex.printStackTrace(System.err);
				}
				
				LipstikLookAndFeel.setMyCurrentTheme(new LightGrayTheme());
				
				SwingUtilities.updateComponentTreeUI(XCMGUI.getFrame());
				SwingUtilities.updateComponentTreeUI(THIS);
				
				XCMGUI.getFrame().pack();
			}
		});
		themeButtonGroup.add(rdbtnNewRadioButton);
		
		JRadioButton rdbtnNewRadioButton_3 = new JRadioButton("Lipstik - Klearlooks");
		rdbtnNewRadioButton_3.setSelected(true);
		rdbtnNewRadioButton_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					UIManager.setLookAndFeel("com.lipstikLF.LipstikLookAndFeel");
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnsupportedLookAndFeelException ex) {
					ex.printStackTrace(System.err);
				}
				LipstikLookAndFeel.setMyCurrentTheme(new KlearlooksTheme());
				
				SwingUtilities.updateComponentTreeUI(XCMGUI.getFrame());
				SwingUtilities.updateComponentTreeUI(THIS);
				
				XCMGUI.getFrame().pack();
			}
		});
		themeButtonGroup.add(rdbtnNewRadioButton_3);
		
		JButton btnNewButton = new JButton("Ok");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		GroupLayout gl_themeTab = new GroupLayout(themeTab);
		gl_themeTab
					.setHorizontalGroup(gl_themeTab
								.createParallelGroup(Alignment.TRAILING)
								.addGroup(
											gl_themeTab
														.createSequentialGroup()
														.addContainerGap(6, Short.MAX_VALUE)
														.addGroup(
																	gl_themeTab
																				.createParallelGroup(Alignment.LEADING)
																				.addGroup(
																							gl_themeTab
																										.createSequentialGroup()
																										.addGroup(
																													gl_themeTab
																																.createParallelGroup(
																																			Alignment.LEADING)
																																.addComponent(
																																			rdbtnNewRadioButton_1)
																																.addGroup(
																																			gl_themeTab
																																						.createSequentialGroup()
																																						.addGroup(
																																									gl_themeTab
																																												.createParallelGroup(
																																															Alignment.LEADING)
																																												.addComponent(
																																															rdbtnNewRadioButton_3)
																																												.addComponent(
																																															rdbtnNewRadioButton_2)
																																												.addComponent(
																																															rdbtnNewRadioButton))
																																						.addGap(72)))
																										.addGap(72)).addGroup(
																							Alignment.TRAILING,
																							gl_themeTab.createSequentialGroup()
																										.addComponent(btnNewButton)
																										.addContainerGap()))));
		gl_themeTab.setVerticalGroup(gl_themeTab.createParallelGroup(Alignment.LEADING)
					.addGroup(
								Alignment.TRAILING,
								gl_themeTab.createSequentialGroup().addContainerGap(42, Short.MAX_VALUE)
											.addComponent(rdbtnNewRadioButton_1).addPreferredGap(
														ComponentPlacement.RELATED).addComponent(
														rdbtnNewRadioButton_2).addPreferredGap(
														ComponentPlacement.RELATED).addComponent(
														rdbtnNewRadioButton_3).addPreferredGap(
														ComponentPlacement.RELATED).addComponent(rdbtnNewRadioButton)
											.addGap(46).addComponent(btnNewButton).addGap(21)));
		themeTab.setLayout(gl_themeTab);
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				
				fc.setCurrentDirectory(new java.io.File(textFieldUnpacked.getText()));
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				
				int returnVal = fc.showOpenDialog(THIS);
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					java.io.File file = fc.getSelectedFile();
					textFieldUnpacked.setText(file.getAbsolutePath());
				}
			}
		});
		browseButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				
				fc.setCurrentDirectory(new java.io.File(textFieldInstall.getText()));
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				
				int returnVal = fc.showOpenDialog(THIS);
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					java.io.File file = fc.getSelectedFile();
					textFieldInstall.setText(file.getAbsolutePath());
				}
			}
		});
	}
	
	/**
	 * Save action
	 * 
	 * @author Daniel Kemp
	 * 
	 */
	class SaveAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JComponent src = (JButton) e.getSource();
			
			final Config config = XCMGUI.getConfig();
			
			final String name = textFieldName.getText();
			final String unpackedPath = textFieldUnpacked.getText();
			final String xComPath = textFieldInstall.getText();
			final String compressorPath = textFieldDecompressor.getText();
			final String extractorPath = textFieldExtractor.getText();
			
			final Path cookedCore = Paths.get(config.getCookedPath().toString(), "Core.upk");
			
			Boolean isOk = true;
			
			String msg = "ERROR";
			String title = "Incorrect setting.";
			int op = JOptionPane.ERROR_MESSAGE;
			
			if (Files.exists(Paths.get(compressorPath))) {
				config.setCompressorPath(compressorPath);
			}
			if (Files.exists(Paths.get(extractorPath))) {
				config.setExtractorPath(extractorPath);
			}
			
			if (name.isEmpty() || unpackedPath.isEmpty() || xComPath.isEmpty()) {
				msg = "Setting field cannot be empty.";
				
			} else if (!XCMGUI.isXComPathValid(xComPath)) {
				isOk = false;
				msg = "The system cannot verify your XCOM installtion.";
				
			} else if (name.equals("unknown")) {
				isOk = false;
				msg = "Name field cannot be 'unknown'.";
				
			} else if (Files.notExists(Paths.get(compressorPath))
						|| Files.notExists(Paths.get(extractorPath))) {
				
				boolean yes = false;
				
				try {
					yes = getToolsToPerformInitialVerification(unpackedPath, cookedCore, src);
				} catch (HeadlessException | MalformedURLException ignore) {
					ignore.printStackTrace(System.err);
				}
				if (!yes) {
					msg = "You need to select a valid path for Gildor's tools.";
				} else {
					msg = null;
				}
			} else if (!Main.isUnPackedPathValid(unpackedPath)) {
				
				if (Files.isDirectory(Paths.get(unpackedPath))) {
					isOk = false;
					try {
						if (!Main.isDecompressed(cookedCore)) {
							int n = JOptionPane
										.showConfirmDialog(
													THIS,
													"Would you like to unpack the core upk file here to verify the current path?",
													"Unpack Core.upk", JOptionPane.YES_NO_OPTION);
							
							switch (n) {
								case JOptionPane.YES_OPTION :
									DecompressInBackGround dib = new DecompressInBackGround(cookedCore);
									
									dib.execute();
									try {
										dib.get();
									} catch (InterruptedException | ExecutionException ignore) {
										ignore.printStackTrace(); // already handled
										break;
									}
									new ExtractInBackGround(cookedCore, THIS).execute();
									
									break;
							}
							msg = null;
							isOk = true;
						} else {
							msg = "Your Core.upk is already decompressed. Unpacking now.";
							isOk = true;
							new ExtractInBackGround(cookedCore, THIS).execute();
						}
					} catch (HeadlessException | IOException ignore) {
						ignore.printStackTrace(System.err);
					}
					
				} else {
					isOk = false;
					msg = "The system cannot verify your unpacked resources. Please choose a valid path.";
				}
				
				if (!isOk) {
					msg = "The system cannot verify your unpacked resources.";
				}
			} else if (isOk) { // Is Ok
			
				msg = "The settings have been updated.";
				title = "Settings saved.";
				op = JOptionPane.PLAIN_MESSAGE;
				
				config.setAuthor(name);
				config.setUnpackedPath(unpackedPath);
				config.setXcomPath(xComPath);
				
				try {
					Main.saveXml(config);
				} catch (XmlSaveException ex) {
					msg = "There was an error saving the settings file. Try again. Check for OS permissions.";
					title = "Settings not saved.";
					ex.printStackTrace(System.err);
				}
				dispose();
			}
			if (msg != null) {
				JOptionPane.showMessageDialog(getContentPane(), msg, title, op);
			}
			if (!isOk) {
				src.dispatchEvent(e);
			}
		}
		/**
		 * 
		 * @param unpackedPath
		 * @param cookedCore
		 * @param src
		 * @throws HeadlessException
		 * @throws MalformedURLException
		 */
		public boolean getToolsToPerformInitialVerification(final String unpackedPath,
					final Path cookedCore, final JComponent src) throws HeadlessException,
					MalformedURLException {
			
			final Path decompress = Paths.get("tools\\decompress.exe");
			final Path extract = Paths.get("tools\\extract.exe");
			final Path upkToExtract = Paths.get(unpackedPath, "Core.upk");
			
			int n1 = JOptionPane
						.showConfirmDialog(
									THIS,
									"To get started you need to download Gildor's tools or select a working path.\nWould you like to download?",
									"Download Gildor's tools?", JOptionPane.YES_NO_OPTION);
			
			switch (n1) {
				case JOptionPane.YES_OPTION :
					new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							if (src != null) {
								src.setEnabled(false);
							}
							close.setEnabled(false);
							THIS.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
							Main.openDesktopBrowser("http://www.gildor.org/");
							XCMGUI.decompressWithToolCheck(cookedCore, decompress, true);
							XCMGUI.extractWithToolCheck(upkToExtract, extract, null, true, THIS);
							
							return null;
						}
						@Override
						protected void done() {
							try {
								get();
							} catch (InterruptedException | ExecutionException ex) {
								ex.printStackTrace();
							}
							THIS.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
							if (src != null) {
								src.setEnabled(true);
							}
							close.setEnabled(true);
						}
					}.execute();
					break;
				default :
					return false;
			}
			return true;
		}
	}
}
