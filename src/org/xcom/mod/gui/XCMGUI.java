package org.xcom.mod.gui;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipException;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.DefaultCaret;
import javax.xml.bind.JAXBException;

import org.xcom.main.shared.DownloadFailedException;
import org.xcom.main.shared.Main;
import org.xcom.main.shared.entities.Config;
import org.xcom.main.shared.entities.ModConfig;
import org.xcom.main.shared.entities.XMod;
import org.xcom.mod.gui.dialogues.AboutDialog;
import org.xcom.mod.gui.dialogues.SettingsDialog;
import org.xcom.mod.gui.listeners.GetHashButton;
import org.xcom.mod.gui.shared.GetFilePanel;
import org.xcom.mod.gui.streams.Stream;
import org.xcom.mod.gui.workers.DecompressInBackGround;
import org.xcom.mod.gui.workers.DownloadWorker;
import org.xcom.mod.gui.workers.ExtractInBackGround;
import org.xcom.mod.gui.workers.RunInBackground;
import org.xcom.mod.tools.installer.Installer;
import org.xcom.mod.tools.maker.Maker;
import org.xcom.mod.tools.xshape.XShape;

import com.lipstikLF.LipstikLookAndFeel;
import com.lipstikLF.theme.KlearlooksTheme;

public class XCMGUI extends Main {
	
	public static final String GUI_NAME = "XCMM";
	
	private static FileList filesOriginal;
	private static FileList filesEdited;
	private JScrollPane scrollPaneFDOriginal;
	private JScrollPane scrollPaneFDEdited;
	private JPanel fileManagerHome;
	private JPanel fileManagerMake;
	private JPanel fileManagerInstall;
	private JTabbedPane tabbedPane;
	
	private static final int HOME_TAB = 0;
	private static final int MAKER_TAB = 1;
	private static final int INSTALL_TAB = 2;
	
	private static JEditorPane fieldModDescription;
	private static JTextField fieldModName;
	private static JTextField fieldModAuthor;
	
	private static JTextArea mos = new JTextArea();
	private static JTextArea ios = new JTextArea();
	private static JTextArea hos = new JTextArea();
	
	private JButton oFTRemoveButton;
	private JButton oFTClearButton;
	private JButton oETClearButton;
	private JButton oETRemoveButton;
	private JButton installButton;
	private static FileTreePanel modDirectoryTree;
	private JButton makeButton;
	private static FileTreePanel modFileTree;
	
	private JButton xShapeButton;
	
	private JButton getDecompressorButton;
	private JButton getExtractorButton;
	
	private static SettingsDialog ad;
	public static Stream FACING_STREAM;
	private Random random = new Random();
	
	private static JFrame frame;
	private GetFilePanel selectIni;
	
	/**
	 * Create the gui.
	 * 
	 * @param config
	 */
	public XCMGUI() throws HeadlessException {
		frame = new JFrame(GUI_NAME);
		Main.contentPane = frame;
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(XCMGUI.class.getResource("/org/xcom/mod/gui/icons/XCMM-096x096.png")));
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setPreferredSize(new Dimension(800, 600));
		
		// Try to set the look and feel for the program.
		try {
			// UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			UIManager.setLookAndFeel("com.lipstikLF.LipstikLookAndFeel");
			
			// LipstikLookAndFeel.setMyCurrentTheme(new DefaultTheme());
			// LipstikLookAndFeel.setMyCurrentTheme(new LightGrayTheme());
			LipstikLookAndFeel.setMyCurrentTheme(new KlearlooksTheme());
			
			// Throw exception if there is an error setting the look and feel.
		} catch (Exception e) {
			print("An error has ocurred while setting the look " + "and feel of the program");
		} // should probably add metal L&F setting for backup
		
		MAKE = Stream.getStream(MAKE_DELEGATE, mos);
		INSTALL = Stream.getStream(INSTALL_DELEGATE, ios);
		MAIN = Stream.getStream(MAIN_DELEGATE, hos);
		FACING_STREAM = MAIN;
		
		initialise();
		
		// fileManagerHome = new ImagePanel(new
		// ImageIcon("images/1024X768_wall.jpg").getImage());
		
		new FileDrop(/* System.out, */filesOriginal, /* dragBorder, */
		new FileDrop.Listener() {
			
			public void filesDropped(java.util.List<File> files) {
				
				FileList fileList = filesOriginal;
				
				JButton clearButton = oFTClearButton;
				fileListGetFilesDropped(files, fileList, clearButton);
			} // end filesDropped
			
		}, scrollPaneFDOriginal); // end FileDrop.Listener
		
		new FileDrop(/* System.out, */filesEdited, /* dragBorder, */
		new FileDrop.Listener() {
			
			public void filesDropped(java.util.List<File> files) {
				
				FileList fileList = filesEdited;
				JButton clearButton = oETClearButton;
				
				fileListGetFilesDropped(files, fileList, clearButton);
			} // end filesDropped
		}, scrollPaneFDEdited);
		
		JPanel footer = new JPanel();
		frame.getContentPane().add(footer, BorderLayout.SOUTH);
		footer.setLayout(new BorderLayout(0, 0));
		
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.setVisible(false);
		footer.add(horizontalBox, BorderLayout.EAST);
		
		JLabel lblNewLabel_1 = new JLabel("Tasks");
		lblNewLabel_1.setVisible(false);
		horizontalBox.add(lblNewLabel_1);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setVisible(false);
		horizontalBox.add(horizontalStrut);
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setVisible(false);
		horizontalBox.add(progressBar);
		
		tabbedPane.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent e) {
				
				int tab = tabbedPane.getSelectedIndex();
				
				switch (tab) {
					case MAKER_TAB :
						fileManagerHome.setVisible(false);
						fileManagerInstall.setVisible(false);
						fileManagerMake.setVisible(true);
						installButton.setEnabled(false);
						makeButton.setEnabled(true);
						FACING_STREAM = MAKE;
						//System.setOut(MAKE);
						break;
					case INSTALL_TAB :
						fileManagerHome.setVisible(false);
						fileManagerMake.setVisible(false);
						fileManagerInstall.setVisible(true);
						makeButton.setEnabled(false);
						modDirectoryTree.getTree().clearSelection();
						modFileTree.getTree().clearSelection();
						FACING_STREAM = INSTALL;
						//System.setOut(INSTALL);
						break;
					case HOME_TAB :
						fileManagerInstall.setVisible(false);
						fileManagerMake.setVisible(false);
						fileManagerHome.setVisible(true);
						makeButton.setEnabled(false);
						installButton.setEnabled(false);
						FACING_STREAM = MAIN;
						//System.setOut(MAIN);
					default :
						break;
				}
			}
		});
		
		frame.pack();
		frame.setLocationRelativeTo(null);
		checkConfig();
	}
	
	private void checkConfig() {
		
		String name = config.getAuthor();
		Path decompress = Paths.get(config.getCompressorPath());
		Path extract = Paths.get(config.getExtractorPath());
		
		if (name.equals("unknown") || name.isEmpty() || (!isXComPathValid(config.getXcomPath()))
					|| (!isUnPackedPathValid(config.getUnpackedPath())) 
					|| Files.notExists(decompress)
					|| Files.notExists(extract)) {
			
			JOptionPane.showMessageDialog(frame, "Please set up the application correctly.",
						"Incorrect settings.", JOptionPane.ERROR_MESSAGE);
			ad.setVisible(true);
		}
		
		if (Files.exists(decompress)) {
			this.getDecompressorButton.setEnabled(false);
		}	
		if (Files.exists(extract)) {
			this.getExtractorButton.setEnabled(false);
		}
		Path history = Paths.get("temp", "history.xml");
		
		if (Files.exists(history)) {
			ModConfig mod = null;
			
			try {
				mod = (ModConfig) u.unmarshal(history.toFile());
			} catch (JAXBException ex) {
				ex.printStackTrace();
			}
			fieldModName.setText(mod.getName());
			fieldModAuthor.setText(mod.getAuthor());
			fieldModDescription.setText(mod.getDescription());
			selectIni.getTextField().setText(mod.getIni());
			for (String s : mod.getOriginalFilePaths()) {
				history = Paths.get(config.getUnpackedPath(), s).toAbsolutePath();
				print(System.err, "Added original history file: " + history.toString() + "\n");
				filesOriginal.getListModel().add(history);
				oFTClearButton.setEnabled(true);
			}
			
			for (String s : mod.getEditedFilePaths()) {
				history = Paths.get(s).normalize().toAbsolutePath();
				print(System.err, "Added edited history file: " + history.toString() + "\n");
				filesEdited.getListModel().add(history);
				oETClearButton.setEnabled(true);
			}
		} else {
			fieldModAuthor.setText(config.getAuthor());
		}
	}
	/**
	 * Initialise the contents of the frame.
	 */
	private void initialise() {
		frame.setMinimumSize(new Dimension(655, 460));
		frame.setBounds(100, 100, 639, 503);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frame.getContentPane().add(menuBar, BorderLayout.NORTH);
		
		ad = new SettingsDialog(config);
		
		JMenu menuHome = new JMenu("File");
		menuHome.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menuHome);
		
		JMenuItem homeMiExit = new JMenuItem("Exit");
		homeMiExit.setMnemonic(KeyEvent.VK_E);
		homeMiExit.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				print("CLOSING", "");
				if (frame.isDisplayable()) {
					frame.dispose();
				}
			}
		});
		menuHome.add(homeMiExit);
		
		JMenu menuTools = new JMenu("Tools");
		menuTools.setMnemonic(KeyEvent.VK_T);
		menuBar.add(menuTools);
		
		JMenuItem toolsMiCmd = new JMenuItem("Open console");
		toolsMiCmd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					@SuppressWarnings("unused")
					Process p = new ProcessBuilder("cmd.exe", "/C", "start").start();
				} catch (IOException ex) {
					ex.printStackTrace(System.err);
				}
			}
		});
		toolsMiCmd.setMnemonic(KeyEvent.VK_O);
		menuTools.add(toolsMiCmd);
		
		JMenu menuSettings = new JMenu("Settings");
		menuSettings.setMnemonic(KeyEvent.VK_S);
		menuBar.add(menuSettings);
		
		JMenuItem settingsMiConfig = new JMenuItem("Edit Config");
		settingsMiConfig.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				ad.setVisible(true);
			}
		});
		settingsMiConfig.setMnemonic(KeyEvent.VK_C);
		menuSettings.add(settingsMiConfig);
		
		JMenu menuHelp = new JMenu("Help");
		menuHelp.setMnemonic(KeyEvent.VK_H);
		menuBar.add(menuHelp);
		
		JMenuItem helpMiAbout = new JMenuItem("About");
		helpMiAbout.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent event) {
				
				AboutDialog ad = new AboutDialog();
				ad.setVisible(true);
			}
		});
		helpMiAbout.setMnemonic(KeyEvent.VK_A);
		menuHelp.add(helpMiAbout);
		
		JPanel modManager = new JPanel();
		modManager.setPreferredSize(new Dimension(235, 350));
		modManager.setMinimumSize(new Dimension(235, 350));
		modManager.setBorder(new TitledBorder(null, "Mod manager", TitledBorder.LEFT,
					TitledBorder.TOP, null, null));
		frame.getContentPane().add(modManager, BorderLayout.WEST);
		modManager.setLayout(new BoxLayout(modManager, BoxLayout.X_AXIS));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setMinimumSize(new Dimension(224, 347));
		tabbedPane.setPreferredSize(new Dimension(224, 347));
		modManager.add(tabbedPane);
		
		JPanel homeTab = new JPanel();
		homeTab.setToolTipText("");
		homeTab.setBackground(Color.WHITE);
		homeTab.setOpaque(false);
		tabbedPane.addTab("Home", null, homeTab, "XCom Edit home");
		
		JButton btnGetFileHash = new JButton("Cooked");
		btnGetFileHash.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnGetFileHash.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		btnGetFileHash.addActionListener(new GetHashButton(frame) {
			
			@Override
			protected File getFile() {
				
				return config.getCookedPath().toFile();
			}
		});
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setPreferredSize(new Dimension(200, 3));
		homeTab.add(separator_2);
		
		JButton btnRestoreOriginalGame = new JButton("Restore Original");
		btnRestoreOriginalGame.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnRestoreOriginalGame.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				final Path home = Paths.get(config.getUnpackedPath());
				final String uSize = ".uncompressed_size.bkp";
				final String oComp = ".original_compressed";
				
				int n = JOptionPane
							.showConfirmDialog(
										frame,
										"This will replace any existing resources in the Cooked directory.\nDo you still wish to continue?",
										"Resotre Upk Backup", JOptionPane.YES_NO_OPTION);
				
				switch (n) {
					case JOptionPane.YES_OPTION :
						String msg = "The files have been restored.";
						String title = "Upk File Restore";
						int op = JOptionPane.PLAIN_MESSAGE;
						
						print("ATTEMPT RESTORE ORIGINAL UPK FILES", "");
						print("WALKING UNPACKED PATH [" + home, "]");
						
						Finder f = null;
						try {
							f = new Finder(uSize, oComp);
							Files.walkFileTree(home, f);
							
						} catch (IOException ex) {
							msg = "There was an error restoring the files.";
							ex.printStackTrace(System.err);
						}
						if (f != null && !(f.getNumMatches() > 0)) {
							msg = "There were no files to restore.";
							
						} else {}
						
						JOptionPane.showMessageDialog(frame, msg, title, op);
						
						if (f != null && (f.getNumMatches() > 0)) {
							
							int n1 = JOptionPane
										.showConfirmDialog(
													frame,
													"If you have edited files you need to patch the XComGame.exe.\nWould you like to run XShape on the XComGame.exe?",
													"Run XShape.", JOptionPane.YES_NO_OPTION);
							
							switch (n1) {
								case JOptionPane.YES_OPTION :
									Path exeFile = Paths.get(config.getXcomPath(), RELATIVE_EXE_PATH);
									runXShapeInBackGround(exeFile, (ArrayList<Path>) f.getUpks(),
												(JComponent) e.getSource(), FACING_STREAM);
									break;
							}
						}
						break;
				}
			}
			class Finder extends SimpleFileVisitor<Path> {
				
				private final Path cooked = config.getCookedPath();
				private final Path home = Paths.get(config.getUnpackedPath());
				
				private final PathMatcher matcherUSize;
				private final PathMatcher matcherOrigCompress;
				
				private final String uSize;
				private final String oComp;
				
				private List<Path> upks = new ArrayList<Path>();
				private int numMatches = 0;
				
				Finder(String u_SizeBack, String orig_Compress) {
					
					this.uSize = u_SizeBack;
					this.oComp = orig_Compress;
					
					print("COOKED PATH [" + cooked, "]");
					matcherUSize = FileSystems.getDefault().getPathMatcher("glob:" + "*" + uSize);
					matcherOrigCompress = FileSystems.getDefault().getPathMatcher(
								"glob:" + "*" + orig_Compress);
					
				}
				
				// Compares the glob pattern against
				// the file or directory name.
				void find(Path file) {
					Path name = file.getFileName();
					String temp = name.toString();
					print("MATCH ATTEMPT [" + name, "]");
					
					if (name != null && matcherUSize.matches(name)) {
						String baseName = name.toString()
									.substring(0, temp.length() - uSize.length());
						
						Path to = Paths.get(cooked.toString(), baseName + ".uncompressed_size");
						numMatches++;
						
						try {
							Files.move(file, to);
						} catch (IOException ex) {
							ex.printStackTrace(System.err);
						}
						print("MOVE & RENAME [" + name, "] TO [" + to.getFileName(), "]");
					}
					
					if (name != null && matcherOrigCompress.matches(name)) {
						String baseName = name.toString()
									.substring(0, temp.length() - oComp.length());
						
						Path to = Paths.get(cooked.toString(), baseName);
						numMatches++;
						upks.add(Paths.get(config.getUnpackedPath(), baseName));
						
						try {
							Files.move(file, to, StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException ex) {
							ex.printStackTrace();
						}
						print("MOVING & RENAMING [" + name + "] TO [" + to.getFileName(), "]");
					}
				}
				// Invoke the pattern matching
				// method on each file.
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					find(file);
					return CONTINUE;
				}
				
				// Invoke the pattern matching
				// method on each directory.
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					if (dir.equals(home)) return CONTINUE;
					else return SKIP_SUBTREE;
				}
				
				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					System.err.println(exc);
					return CONTINUE;
				}
				
				public int getNumMatches() {
					return numMatches;
				}
				
				public List<Path> getUpks() {
					return this.upks;
				}
				
			}
		});
		
		Component rigidArea_14 = Box.createRigidArea(new Dimension(20, 20));
		rigidArea_14.setPreferredSize(new Dimension(50, 20));
		homeTab.add(rigidArea_14);
		
		JLabel lblManageUpkFiles = new JLabel("Manage Upk files");
		homeTab.add(lblManageUpkFiles);
		
		Component rigidArea_7 = Box.createRigidArea(new Dimension(20, 20));
		rigidArea_7.setPreferredSize(new Dimension(50, 20));
		homeTab.add(rigidArea_7);
		
		Component rigidArea_8 = Box.createRigidArea(new Dimension(20, 20));
		homeTab.add(rigidArea_8);
		homeTab.add(btnRestoreOriginalGame);
		
		JButton btnUnpack = new JButton("Extract");
		btnUnpack.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnUnpack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File(getConfig().getUnpackedPath()));
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setDragEnabled(true);
				
				int returnVal = fc.showOpenDialog(frame);
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					java.io.File file = fc.getSelectedFile();
					try {
						extractWithToolCheck(file.toPath(), Paths.get("tools", "extract.exe"),
									(JComponent) e.getSource(), true, getFrame());
					} catch (DownloadFailedException ex) {
						JOptionPane.showMessageDialog(getFrame(), "The download failed...", "Error",
									JOptionPane.ERROR_MESSAGE);
						ex.printStackTrace(System.err);
					} catch (ZipException ex) {
						JOptionPane.showMessageDialog(getFrame(), "Zip extraction failed...",
									"Error", JOptionPane.ERROR_MESSAGE);
						ex.printStackTrace(System.err);
					}
				} else {}
			}
		});
		
		JButton btnDecompressUpk = new JButton("Decompress");
		btnDecompressUpk.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnDecompressUpk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(getConfig().getCookedPath().toFile());
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setDragEnabled(true);
				
				int returnVal = fc.showOpenDialog(frame);
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					java.io.File file = fc.getSelectedFile();
					try {
						decompressWithToolCheck(file.toPath(), Paths.get("tools", "decompress.exe"),
									true);
					} catch (DownloadFailedException ex) {
						JOptionPane.showMessageDialog(getFrame(), "The download failed...", "Error",
									JOptionPane.ERROR_MESSAGE);
						ex.printStackTrace(System.err);
					} catch (ZipException ex) {
						JOptionPane.showMessageDialog(getFrame(), "Zip extraction failed...",
									"Error", JOptionPane.ERROR_MESSAGE);
						ex.printStackTrace(System.err);
					}
				} else {}
			}
		});
		
		Component rigidArea_10 = Box.createRigidArea(new Dimension(20, 20));
		homeTab.add(rigidArea_10);
		homeTab.add(btnDecompressUpk);
		homeTab.add(btnUnpack);
		
		JSeparator separator_5 = new JSeparator();
		separator_5.setPreferredSize(new Dimension(200, 3));
		homeTab.add(separator_5);
		
		Component rigidArea_1 = Box.createRigidArea(new Dimension(20, 20));
		homeTab.add(rigidArea_1);
		
		JLabel lblNewLabel_2 = new JLabel("Calculate SHA file hash");
		homeTab.add(lblNewLabel_2);
		
		Component rigidArea = Box.createRigidArea(new Dimension(20, 20));
		homeTab.add(rigidArea);
		homeTab.add(btnGetFileHash);
		
		JButton btnGetUnpackedFile = new JButton("Unpacked");
		btnGetUnpackedFile.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnGetUnpackedFile.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		btnGetUnpackedFile.addActionListener(new GetHashButton(frame) {
			
			@Override
			protected File getFile() {
				return new File(config.getUnpackedPath());
			}
		});
		homeTab.add(btnGetUnpackedFile);
		
		JButton btnGetModFileHash = new JButton("Mods");
		btnGetModFileHash.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnGetModFileHash.addActionListener(new GetHashButton(frame) {
			
			@Override
			protected File getFile() {
				return Config.getModPath().toFile();
			}
		});
		homeTab.add(btnGetModFileHash);
		
		getDecompressorButton = new JButton("Decompressor");
		getDecompressorButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		getDecompressorButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JComponent src = (JComponent) e.getSource();
				
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URL("http://www.gildor.org/").toURI());
					} catch (IOException | URISyntaxException ex) {
						ex.printStackTrace(System.err);
					}
				}
				final String url = "http://www.gildor.org/down/32/umodel/decompress.zip";
				final String saveAs = "decompress.zip";
				
				try {
					downloadZippedTool(url, saveAs, src, null);
				} catch (MalformedURLException ex) {}
			}
		});
		
		JSeparator separator_3 = new JSeparator();
		separator_3.setPreferredSize(new Dimension(200, 3));
		homeTab.add(separator_3);
		
		Component rigidArea_15 = Box.createRigidArea(new Dimension(20, 20));
		rigidArea_15.setPreferredSize(new Dimension(45, 20));
		homeTab.add(rigidArea_15);
		
		JLabel lblOpenExplorer = new JLabel("Open Explorer");
		homeTab.add(lblOpenExplorer);
		
		Component rigidArea_13 = Box.createRigidArea(new Dimension(20, 20));
		rigidArea_13.setPreferredSize(new Dimension(45, 20));
		homeTab.add(rigidArea_13);
		
		JButton btnExploreGameFiles = new JButton("Cooked");
		homeTab.add(btnExploreGameFiles);
		btnExploreGameFiles.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnExploreGameFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().open(XCMGUI.getConfig().getCookedPath().toFile());
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		
		JButton btnExploreUnpacked = new JButton("Unpacked");
		homeTab.add(btnExploreUnpacked);
		btnExploreUnpacked.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnExploreUnpacked.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().open(new File(XCMGUI.getConfig().getUnpackedPath()));
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		});
		
		JButton btnExploreModFiles = new JButton("Mods");
		homeTab.add(btnExploreModFiles);
		btnExploreModFiles.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnExploreModFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					XCMGUI.getConfig();
					Desktop.getDesktop().open(Config.getModPath().toFile());
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}
		});
		
		Box verticalBox_2 = Box.createVerticalBox();
		homeTab.add(verticalBox_2);
		
		Component verticalStrut_6 = Box.createVerticalStrut(20);
		verticalStrut_6.setPreferredSize(new Dimension(0, 5));
		verticalBox_2.add(verticalStrut_6);
		
		Component verticalStrut_7 = Box.createVerticalStrut(20);
		verticalStrut_7.setPreferredSize(new Dimension(0, 5));
		verticalBox_2.add(verticalStrut_7);
		
		JSeparator separator_4 = new JSeparator();
		separator_4.setPreferredSize(new Dimension(200, 3));
		homeTab.add(separator_4);
		
		Component rigidArea_2 = Box.createRigidArea(new Dimension(20, 20));
		homeTab.add(rigidArea_2);
		
		JLabel lblDownloadTools = new JLabel("Download Gildor's Unreal tools");
		homeTab.add(lblDownloadTools);
		
		Component rigidArea_3 = Box.createRigidArea(new Dimension(20, 20));
		homeTab.add(rigidArea_3);
		homeTab.add(getDecompressorButton);
		
		getExtractorButton = new JButton("Extractor");
		getExtractorButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		getExtractorButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				JComponent src = (JComponent) e.getSource();
				final String url = "http://www.gildor.org/down/32/umodel/extract.zip";
				final String saveAs = "extract.zip";
				
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URL("http://www.gildor.org/").toURI());
					} catch (IOException | URISyntaxException ex) {
						ex.printStackTrace(System.err);
					}
				}
				try {
					downloadZippedTool(url, saveAs, src, null);
				} catch (MalformedURLException ignore) {}
			}
		});
		homeTab.add(getExtractorButton);
		
		JButton gildorsLinkButton = new JButton(
					"<HTML>Click the <FONT color=\\\"#000099\\\"><U>link</U></FONT> to go to Gildor's website.</HTML>");
		gildorsLinkButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		gildorsLinkButton.setToolTipText("");
		gildorsLinkButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				if (Desktop.isDesktopSupported()) {
					try {
						Desktop.getDesktop().browse(new URL("http://www.gildor.org/").toURI());
					} catch (IOException | URISyntaxException ex) {
						ex.printStackTrace(System.err);
					}
				}
			}
		});
		gildorsLinkButton.setBorderPainted(false);
		homeTab.add(gildorsLinkButton);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setPreferredSize(new Dimension(200, 3));
		homeTab.add(separator_1);
		
		JButton btnOpenIni = new JButton("Open Ini");
		btnOpenIni.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File(getConfig().getXcomPath(), "XComGame\\Config"));
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setDragEnabled(true);
				
				int returnVal = fc.showOpenDialog(frame);
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					java.io.File file = fc.getSelectedFile();
					try {
						Desktop.getDesktop().open(file);
					} catch (IOException ex) {
						// TODO Auto-generated catch block
						ex.printStackTrace();
					}
				}
				
			}
			
		});
		homeTab.add(btnOpenIni);
		
		JPanel makerTab = new JPanel();
		
		makerTab.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		makerTab.setMinimumSize(new Dimension(230, 315));
		tabbedPane.addTab("Make", null, makerTab, "XCOM Mod maker");
		makerTab.setLayout(new BorderLayout(0, 0));
		
		JPanel makerPanel = new JPanel();
		makerPanel.setMinimumSize(new Dimension(230, 319));
		makerPanel.setPreferredSize(new Dimension(220, 319));
		makerPanel.setBorder(new TitledBorder(null, "Mod Attributes", TitledBorder.RIGHT,
					TitledBorder.TOP, null, null));
		makerTab.add(makerPanel, BorderLayout.CENTER);
		makerPanel.setLayout(null);
		
		Box verticalBox = Box.createVerticalBox();
		verticalBox.setBounds(10, 30, 200, 154);
		makerPanel.add(verticalBox);
		
		Box hBoxModName = Box.createHorizontalBox();
		verticalBox.add(hBoxModName);
		hBoxModName.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		JLabel lblName = new JLabel("Mod Name: ");
		hBoxModName.add(lblName);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setPreferredSize(new Dimension(19, 0));
		hBoxModName.add(horizontalStrut);
		
		fieldModName = new JTextField();
		hBoxModName.add(fieldModName);
		fieldModName.setColumns(10);
		
		Component verticalStrut_1 = Box.createVerticalStrut(20);
		verticalBox.add(verticalStrut_1);
		
		Box hBoxAuthor = Box.createHorizontalBox();
		verticalBox.add(hBoxAuthor);
		hBoxAuthor.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		JLabel lblAuthor = new JLabel("Author:");
		hBoxAuthor.add(lblAuthor);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		horizontalStrut_1.setPreferredSize(new Dimension(40, 0));
		hBoxAuthor.add(horizontalStrut_1);
		
		fieldModAuthor = new JTextField();
		hBoxAuthor.add(fieldModAuthor);
		fieldModAuthor.setText(config.getAuthor());
		fieldModAuthor.setColumns(10);
		
		Component verticalStrut_2 = Box.createVerticalStrut(20);
		verticalBox.add(verticalStrut_2);
		
		selectIni = new GetFilePanel("XCom Game Core Ini:", new File(config.getXcomPath(),
					"\\XComGame\\Config"), JFileChooser.FILES_ONLY);
		selectIni.setPreferredSize(new Dimension(210, 50));
		verticalBox.add(selectIni);
		
		Component verticalStrut_8 = Box.createVerticalStrut(20);
		verticalStrut_8.setMinimumSize(new Dimension(0, 10));
		verticalStrut_8.setPreferredSize(new Dimension(0, 10));
		verticalBox.add(verticalStrut_8);
		
		Box vBoxModDescription = Box.createVerticalBox();
		verticalBox.add(vBoxModDescription);
		vBoxModDescription.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		JLabel lblDescription = new JLabel("Mod Description:");
		vBoxModDescription.add(lblDescription);
		
		Component verticalStrut = Box.createVerticalStrut(20);
		verticalStrut.setPreferredSize(new Dimension(0, 5));
		vBoxModDescription.add(verticalStrut);
		verticalStrut.setMinimumSize(new Dimension(0, 5));
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setBounds(10, 186, 200, 195);
		makerPanel.add(splitPane);
		splitPane.setBorder(null);
		splitPane.setOneTouchExpandable(true);
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		
		fieldModDescription = new JEditorPane();
		fieldModDescription.setMinimumSize(new Dimension(6, 46));
		fieldModDescription.setFont(new Font("Tahoma", Font.PLAIN, 11));
		splitPane.setLeftComponent(fieldModDescription);
		fieldModDescription.setAlignmentX(Component.LEFT_ALIGNMENT);
		fieldModDescription.setPreferredSize(new Dimension(110, 20));
		fieldModDescription.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 100));
		splitPane.setRightComponent(panel);
		
		JSeparator separator = new JSeparator();
		separator.setPreferredSize(new Dimension(200, 3));
		separator.setMinimumSize(new Dimension(200, 0));
		panel.add(separator);
		
		JPanel installerTab = new JPanel();
		
		installerTab.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		installerTab.setPreferredSize(new Dimension(230, 315));
		installerTab.setMinimumSize(new Dimension(230, 315));
		tabbedPane.addTab("Install", null, installerTab, "XCom mod installer");
		installerTab.setLayout(new BorderLayout(0, 0));
		
		JPanel installPanel = new JPanel();
		installPanel.setMinimumSize(new Dimension(220, 319));
		installPanel.setPreferredSize(new Dimension(220, 319));
		installPanel.setBorder(new TitledBorder(null, "Mod Attributes", TitledBorder.RIGHT,
					TitledBorder.TOP, null, null));
		installerTab.add(installPanel, BorderLayout.CENTER);
		installPanel.setLayout(null);
		
		Box vBoxInstall = Box.createVerticalBox();
		vBoxInstall.setBounds(10, 30, 200, 100);
		installPanel.add(vBoxInstall);
		
		Box hBModSelected = Box.createHorizontalBox();
		vBoxInstall.add(hBModSelected);
		
		JLabel lnlModSelected = new JLabel("Current mod:");
		hBModSelected.add(lnlModSelected);
		
		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		horizontalStrut_2.setPreferredSize(new Dimension(12, 0));
		hBModSelected.add(horizontalStrut_2);
		
		final JTextField textFieldModSelected = new JTextField();
		textFieldModSelected.setBackground(SystemColor.info);
		textFieldModSelected.setEditable(false);
		hBModSelected.add(textFieldModSelected);
		textFieldModSelected.setColumns(10);
		
		Component verticalStrut_3 = Box.createVerticalStrut(20);
		vBoxInstall.add(verticalStrut_3);
		
		Box horizontalBox = Box.createHorizontalBox();
		vBoxInstall.add(horizontalBox);
		horizontalBox.setAlignmentY(0.5f);
		
		JLabel label = new JLabel("Author:");
		horizontalBox.add(label);
		
		Component horizontalStrut_3 = Box.createHorizontalStrut(20);
		horizontalStrut_3.setPreferredSize(new Dimension(40, 0));
		horizontalBox.add(horizontalStrut_3);
		
		final JTextField textFieldInstallAuthor = new JTextField();
		textFieldInstallAuthor.setBackground(SystemColor.info);
		textFieldInstallAuthor.setEditable(false);
		textFieldInstallAuthor.setText((String) null);
		textFieldInstallAuthor.setColumns(10);
		horizontalBox.add(textFieldInstallAuthor);
		
		Component verticalStrut_5 = Box.createVerticalStrut(20);
		vBoxInstall.add(verticalStrut_5);
		
		Box verticalBox_1 = Box.createVerticalBox();
		vBoxInstall.add(verticalBox_1);
		verticalBox_1.setAlignmentX(0.5f);
		
		JLabel label_1 = new JLabel("Mod Description:");
		verticalBox_1.add(label_1);
		
		Component verticalStrut_4 = Box.createVerticalStrut(20);
		verticalStrut_4.setPreferredSize(new Dimension(0, 5));
		verticalStrut_4.setMinimumSize(new Dimension(0, 5));
		verticalBox_1.add(verticalStrut_4);
		
		JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPane_1.setOneTouchExpandable(true);
		splitPane_1.setBorder(null);
		splitPane_1.setBounds(10, 130, 200, 195);
		installPanel.add(splitPane_1);
		
		final JEditorPane installModDescription = new JEditorPane();
		installModDescription.setEditable(false);
		installModDescription.setBackground(SystemColor.info);
		installModDescription.setPreferredSize(new Dimension(110, 20));
		installModDescription.setMinimumSize(new Dimension(6, 46));
		installModDescription.setFont(new Font("Tahoma", Font.PLAIN, 11));
		installModDescription.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null,
		
		null));
		installModDescription.setAlignmentX(0.0f);
		splitPane_1.setLeftComponent(installModDescription);
		
		JPanel panel_1 = new JPanel();
		panel_1.setPreferredSize(new Dimension(10, 100));
		splitPane_1.setRightComponent(panel_1);
		
		JSeparator separator_6 = new JSeparator();
		separator_6.setPreferredSize(new Dimension(200, 3));
		separator_6.setMinimumSize(new Dimension(200, 0));
		panel_1.add(separator_6);
		
		JPanel centre = new JPanel();
		frame.getContentPane().add(centre, BorderLayout.CENTER);
		centre.setLayout(new BorderLayout(0, 0));
		
		JPanel border = new JPanel();
		centre.add(border, BorderLayout.CENTER);
		border.setLayout(new BorderLayout(0, 0));
		
		JToolBar panelToolBar = new JToolBar();
		border.add(panelToolBar, BorderLayout.NORTH);
		panelToolBar.setAlignmentY(Component.CENTER_ALIGNMENT);
		
		makeButton = new JButton("Make");
		makeButton.setMnemonic(KeyEvent.VK_F2);
		makeButton.setEnabled(false);
		makeButton.addActionListener(new MakeButtonAction());
		makeButton.setIcon(new ImageIcon(XCMGUI.class
					.getResource("/org/xcom/mod/gui/icons/building_go.png")));
		panelToolBar.add(makeButton);
		
		installButton = new JButton("Install");
		installButton.addActionListener(new InstallButtonAction());
		installButton.setIcon(new ImageIcon(XCMGUI.class
					.getResource("/org/xcom/mod/gui/icons/application_go.png")));
		installButton.setEnabled(false);
		panelToolBar.add(installButton);
		
		xShapeButton = new JButton("XShape");
		xShapeButton.addActionListener(new XShapeButtonAction());
		xShapeButton.setIcon(new ImageIcon(XCMGUI.class
					.getResource("/org/xcom/mod/gui/icons/application_xp_terminal.png")));
		xShapeButton
					.setToolTipText("Run XShape once you have modded files. Keeps track of modded files.");
		panelToolBar.add(xShapeButton);
		
		JButton btnNewButton = new JButton("PatchIni");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComponent src = (JComponent) e.getSource();
				
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(new File(getConfig().getXcomPath(), "XComGame\\Config"));
				fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fc.setDragEnabled(true);
				
				int returnVal = fc.showOpenDialog(frame);
				
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					java.io.File file = fc.getSelectedFile();
					Path exeFile = Paths.get(config.getXcomPath(), RELATIVE_EXE_PATH);
					
					int n = JOptionPane.showConfirmDialog(frame,
								"Are you sure you wish to continue?", "IniPatcher.",
								JOptionPane.YES_NO_OPTION);
					
					switch (n) {
						case JOptionPane.YES_OPTION :
							runXShapeInBackGround(exeFile, file.toPath(), src, FACING_STREAM);
							
							return;
						default :
							return;
					}
				}
			}
		});
		btnNewButton.setIcon(new ImageIcon(XCMGUI.class
					.getResource("/org/xcom/mod/gui/icons/application_xp_terminal.png")));
		panelToolBar.add(btnNewButton);
		
		JPanel fileManager = new JPanel();
		border.add(fileManager);
		fileManager.setDoubleBuffered(false);
		fileManager.setMinimumSize(new Dimension(0, 0));
		fileManager.setBorder(null);
		GridBagLayout gbl_fileManager = new GridBagLayout();
		gbl_fileManager.columnWidths = new int[]{
					400, 0
		};
		gbl_fileManager.rowHeights = new int[]{
					368, 0, 0, 0, 0
		};
		gbl_fileManager.columnWeights = new double[]{
					0.0, Double.MIN_VALUE
		};
		gbl_fileManager.rowWeights = new double[]{
					0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE
		};
		fileManager.setLayout(gbl_fileManager);
		
		fileManagerInstall = new JPanel();
		fileManagerInstall.setVisible(false);
		
		fileManagerMake = new JPanel();
		fileManagerMake.setVisible(false);
		fileManagerMake.setDoubleBuffered(false);
		GridBagConstraints gbc_fileManagerMake = new GridBagConstraints();
		gbc_fileManagerMake.weighty = 1.0;
		gbc_fileManagerMake.weightx = 1.0;
		gbc_fileManagerMake.fill = GridBagConstraints.BOTH;
		gbc_fileManagerMake.anchor = GridBagConstraints.NORTHWEST;
		gbc_fileManagerMake.gridx = 0;
		gbc_fileManagerMake.gridy = 0;
		fileManager.add(fileManagerMake, gbc_fileManagerMake);
		fileManagerMake.setAutoscrolls(true);
		fileManagerMake.setBorder(new TitledBorder(null, "File Manager", TitledBorder.RIGHT,
					TitledBorder.TOP, null, null));
		fileManagerMake.setLayout(new BorderLayout(0, 0));
		
		JSplitPane makeExtra = new JSplitPane();
		makeExtra.setResizeWeight(0.3);
		makeExtra.setOneTouchExpandable(true);
		makeExtra.setBorder(null);
		makeExtra.setOrientation(JSplitPane.VERTICAL_SPLIT);
		fileManagerMake.add(makeExtra, BorderLayout.CENTER);
		
		JSplitPane splitPaneFileDrop = new JSplitPane();
		splitPaneFileDrop.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitPaneFileDrop.setResizeWeight(0.5);
		splitPaneFileDrop.setAlignmentY(Component.CENTER_ALIGNMENT);
		splitPaneFileDrop.setAlignmentX(Component.CENTER_ALIGNMENT);
		splitPaneFileDrop.setBorder(null);
		splitPaneFileDrop.setOneTouchExpandable(true);
		makeExtra.setLeftComponent(splitPaneFileDrop);
		
		JPanel panelFileDropOriginal = new JPanel();
		panelFileDropOriginal.setMinimumSize(new Dimension(100, 75));
		splitPaneFileDrop.setLeftComponent(panelFileDropOriginal);
		panelFileDropOriginal.setToolTipText("Drag and drop ORIGINAL files here");
		panelFileDropOriginal.setBorder(new TitledBorder(null, "Original Files",
					TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelFileDropOriginal.setAutoscrolls(true);
		panelFileDropOriginal.setLayout(new BorderLayout(0, 0));
		
		scrollPaneFDOriginal = new JScrollPane();
		scrollPaneFDOriginal
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneFDOriginal.setToolTipText("");
		scrollPaneFDOriginal.setAutoscrolls(true);
		scrollPaneFDOriginal.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		scrollPaneFDOriginal.setViewportBorder(null);
		panelFileDropOriginal.add(scrollPaneFDOriginal);
		
		filesOriginal = new FileList();
		filesOriginal.addListSelectionListener(new ListSelectionListener() {
			
			public void valueChanged(ListSelectionEvent e) {
				
				FileList fileList = filesOriginal;
				JButton removeButton = oFTRemoveButton;
				selectAdjustRemoveButtonEnabled(e, fileList, removeButton);
			}
		});
		filesOriginal.setVisibleRowCount(-1);
		filesOriginal.setToolTipText("");
		filesOriginal.setFont(new Font("Tahoma", Font.PLAIN, 11));
		scrollPaneFDOriginal.setViewportView(filesOriginal);
		filesOriginal.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		filesOriginal.setBorder(null);
		filesOriginal.setDragEnabled(true);
		
		JToolBar originalFilesToolbar = new JToolBar("Original files toolbar");
		originalFilesToolbar.setOrientation(SwingConstants.VERTICAL);
		originalFilesToolbar.setAlignmentY(0.5f);
		panelFileDropOriginal.add(originalFilesToolbar, BorderLayout.WEST);
		
		JButton oFTAddButton = new JButton("");
		oFTAddButton.setToolTipText("Add file");
		oFTAddButton.setActionCommand("");
		oFTAddButton.setIcon(new ImageIcon(XCMGUI.class
					.getResource("/org/xcom/mod/gui/icons/add.png")));
		oFTAddButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser fc = new JFileChooser();
				FileList fileList = filesOriginal;
				JButton clearButton = oFTClearButton;
				
				addFileToFileList(fc, fileList, clearButton, new java.io.File(config
							.getUnpackedPath()));
			}
		});
		originalFilesToolbar.add(oFTAddButton);
		
		oFTRemoveButton = new JButton("");
		oFTRemoveButton.setToolTipText("Delete selected");
		oFTRemoveButton.setIcon(new ImageIcon(XCMGUI.class
					.getResource("/org/xcom/mod/gui/icons/delete.png")));
		oFTRemoveButton.setActionCommand("");
		oFTRemoveButton.setEnabled(false);
		oFTRemoveButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				FileList fileList = filesOriginal;
				JButton clearButton = oFTClearButton;
				JButton removeButton = oFTRemoveButton;
				
				removeSelectedFiles(fileList, clearButton, removeButton);
			}
		});
		originalFilesToolbar.add(oFTRemoveButton);
		
		oFTClearButton = new JButton("");
		oFTClearButton.setIcon(new ImageIcon(XCMGUI.class
					.getResource("/org/xcom/mod/gui/icons/table_delete.png")));
		oFTClearButton.setToolTipText("Delete all");
		oFTClearButton.setEnabled(false);
		oFTClearButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				FileList fileList = filesOriginal;
				JButton clearButton = oFTClearButton;
				JButton removeButton = oFTRemoveButton;
				
				clearAllFiles(fileList, clearButton, removeButton);
			}
		});
		oFTClearButton.setFont(new Font("Tahoma", Font.PLAIN, 11));
		originalFilesToolbar.add(oFTClearButton);
		
		JPanel panelFileDropEdited = new JPanel();
		panelFileDropEdited.setMinimumSize(new Dimension(100, 75));
		splitPaneFileDrop.setRightComponent(panelFileDropEdited);
		panelFileDropEdited.setToolTipText("Drag and drop EDITED files here");
		panelFileDropEdited.setBorder(new TitledBorder(null, "Edited Files",
					TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelFileDropEdited.setAutoscrolls(true);
		panelFileDropEdited.setLayout(new BorderLayout(0, 0));
		
		scrollPaneFDEdited = new JScrollPane();
		scrollPaneFDEdited
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneFDEdited.setToolTipText("");
		scrollPaneFDEdited.setAutoscrolls(true);
		scrollPaneFDEdited.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelFileDropEdited.add(scrollPaneFDEdited);
		
		filesEdited = new FileList();
		filesEdited.addListSelectionListener(new ListSelectionListener() {
			
			public void valueChanged(ListSelectionEvent e) {
				
				FileList fileList = filesEdited;
				JButton removeButton = oETRemoveButton;
				selectAdjustRemoveButtonEnabled(e, fileList, removeButton);
			}
		});
		filesEdited.setVisibleRowCount(-1);
		filesEdited.setFont(new Font("Tahoma", Font.PLAIN, 11));
		filesEdited.setToolTipText("");
		scrollPaneFDEdited.setViewportView(filesEdited);
		filesEdited.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		filesEdited.setDragEnabled(true);
		filesEdited.setBorder(null);
		
		JToolBar editedFilesToolbar = new JToolBar("Edited files toolbar");
		editedFilesToolbar.setOrientation(SwingConstants.VERTICAL);
		editedFilesToolbar.setAlignmentY(0.5f);
		panelFileDropEdited.add(editedFilesToolbar, BorderLayout.WEST);
		
		JButton oETAddButton = new JButton("");
		oETAddButton.setToolTipText("Add file");
		oETAddButton.setIcon(new ImageIcon(XCMGUI.class
					.getResource("/org/xcom/mod/gui/icons/add.png")));
		oETAddButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser fc = new JFileChooser();
				FileList fileList = filesEdited;
				JButton clearButton = oETClearButton;
				
				addFileToFileList(fc, fileList, clearButton, Config.getModPath().toFile());
			}
		});
		editedFilesToolbar.add(oETAddButton);
		
		oETRemoveButton = new JButton("");
		oETRemoveButton.setToolTipText("Delete selected");
		oETRemoveButton.setIcon(new ImageIcon(XCMGUI.class
					.getResource("/org/xcom/mod/gui/icons/delete.png")));
		oETRemoveButton.setEnabled(false);
		oETRemoveButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				FileList fileList = filesEdited;
				JButton clearButton = oETClearButton;
				JButton removeButton = oETRemoveButton;
				
				removeSelectedFiles(fileList, clearButton, removeButton);
			}
		});
		editedFilesToolbar.add(oETRemoveButton);
		
		oETClearButton = new JButton("");
		oETClearButton.setIcon(new ImageIcon(XCMGUI.class
					.getResource("/org/xcom/mod/gui/icons/table_delete.png")));
		oETClearButton.setToolTipText("Delete all");
		oETClearButton.setEnabled(false);
		oETClearButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
				FileList fileList = filesEdited;
				JButton clearButton = oETClearButton;
				JButton removeButton = oETRemoveButton;
				
				clearAllFiles(fileList, clearButton, removeButton);
			}
		});
		oETClearButton.setFont(new Font("Tahoma", Font.PLAIN, 11));
		editedFilesToolbar.add(oETClearButton);
		
		JPanel panelMakeOutput = new JPanel();
		panelMakeOutput.setBorder(new TitledBorder(null, "Make Console",
					TitledBorder.LEADING, TitledBorder.TOP, null, null));
		makeExtra.setRightComponent(panelMakeOutput);
		panelMakeOutput.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPMO = new JScrollPane();
		scrollPMO.setAutoscrolls(true);
		scrollPMO.setMinimumSize(new Dimension(0, 200));
		scrollPMO.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelMakeOutput.add(scrollPMO, BorderLayout.CENTER);
		scrollPMO.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPMO
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		mos.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		mos.setFont(new Font("Tahoma", Font.PLAIN, 11));
		mos.setTabSize(4);
		mos.setBorder(null);
		scrollPMO.setViewportView(mos);
		mos.setEditable(false);
		
		((DefaultCaret) mos.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		fileManagerHome = new JPanel();
		fileManagerHome.setBorder(new TitledBorder(null, "Home", TitledBorder.RIGHT,
					TitledBorder.TOP, null, null));
		fileManagerHome.setAutoscrolls(true);
		GridBagConstraints gbc_fileManagerHome = new GridBagConstraints();
		gbc_fileManagerHome.weighty = 1.0;
		gbc_fileManagerHome.weightx = 1.0;
		gbc_fileManagerHome.fill = GridBagConstraints.BOTH;
		gbc_fileManagerHome.anchor = GridBagConstraints.NORTHWEST;
		gbc_fileManagerHome.gridx = 0;
		gbc_fileManagerHome.gridy = 0;
		fileManager.add(fileManagerHome, gbc_fileManagerHome);
		fileManagerHome.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPaneNews = new JScrollPane();
		scrollPaneNews.setPreferredSize(new Dimension(3, 103));
		
		JEditorPane newsHome = new JEditorPane();
		newsHome
					.setText("<html>\r\n\t<head>\r\n\t\t<title>HTML Online Editor Sample</title>\r\n\t</head>\r\n\t<body>\r\n\t\t<h1>\r\n\t\t\tXcom Mod manager</h1>\r\n\t\t<p>\r\n\t\t\t&nbsp;</p>\r\n\t\t<p>\r\n\t\t\tGitHub repository - <a href=\"https://github.com/twinj/XCMM\">https://github.com/twinj/XCMM</a></p>\r\n\t\t<p>\r\n\t\t\tForum - <a href=\"http://forums.nexusmods.com/index.php?/topic/839384-xcmm-mod-manager-in-java/\">http://forums.nexusmods.com/index.php?/topic/839384-xcmm-mod-manager-in-java/</a></p>\r\n\t\t<p>\r\n\t\t\t&nbsp;</p>\r\n\t\t<p>\r\n\t\t\t&nbsp;</p>\r\n\t\t<p>\r\n\t\t\t&nbsp;</p></body>\r\n</html>\r\n");
		newsHome.setContentType("text/html");
		scrollPaneNews.setViewportView(newsHome);
		newsHome.setPreferredSize(new Dimension(106, 123));
		
		newsHome.setEditable(false);
		newsHome.setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		newsHome.setAlignmentY(0.5f);
		newsHome.setAlignmentX(0.5f);
		
		JSplitPane homeExtra = new JSplitPane();
		homeExtra.setResizeWeight(0.3);
		homeExtra.setOrientation(JSplitPane.VERTICAL_SPLIT);
		homeExtra.setOneTouchExpandable(true);
		homeExtra.setBorder(null);
		fileManagerHome.add(homeExtra, BorderLayout.CENTER);
		
		JPanel panelHomeOutput = new JPanel();
		panelHomeOutput.setBorder(new TitledBorder(null, "Home Console",
					TitledBorder.LEADING, TitledBorder.TOP, null, null));
		homeExtra.setRightComponent(panelHomeOutput);
		panelHomeOutput.setLayout(new BorderLayout(0, 0));
		homeExtra.setLeftComponent(scrollPaneNews);
		
		JScrollPane scrollPHO = new JScrollPane();
		scrollPHO.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPHO.setMinimumSize(new Dimension(0, 200));
		scrollPHO
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPHO.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		scrollPHO.setAutoscrolls(true);
		panelHomeOutput.add(scrollPHO, BorderLayout.CENTER);
		
		scrollPHO.setViewportView(hos);
		hos.setTabSize(4);
		hos.setFont(new Font("Tahoma", Font.PLAIN, 11));
		hos.setEditable(false);
		hos.setBorder(null);
		
		((DefaultCaret) hos.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		fileManagerInstall.setBorder(new TitledBorder(null, "Browser", TitledBorder.RIGHT,
					TitledBorder.TOP, null, null));
		fileManagerInstall.setAutoscrolls(true);
		GridBagConstraints gbc_fileManagerInstall = new GridBagConstraints();
		gbc_fileManagerInstall.weighty = 1.0;
		gbc_fileManagerInstall.weightx = 1.0;
		gbc_fileManagerInstall.fill = GridBagConstraints.BOTH;
		gbc_fileManagerInstall.anchor = GridBagConstraints.NORTHWEST;
		gbc_fileManagerInstall.gridx = 0;
		gbc_fileManagerInstall.gridy = 0;
		fileManager.add(fileManagerInstall, gbc_fileManagerInstall);
		fileManagerInstall.setLayout(new BorderLayout(0, 0));
		
		JSplitPane installExtra = new JSplitPane();
		installExtra.setResizeWeight(0.3);
		installExtra.setOrientation(JSplitPane.VERTICAL_SPLIT);
		installExtra.setOneTouchExpandable(true);
		installExtra.setBorder(null);
		fileManagerInstall.add(installExtra, BorderLayout.CENTER);
		
		JPanel panelInstallOutput = new JPanel();
		installExtra.setRightComponent(panelInstallOutput);
		panelInstallOutput.setBorder(new TitledBorder(null, "Install Console",
					TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelInstallOutput.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPIO = new JScrollPane();
		scrollPIO.setAutoscrolls(true);
		scrollPIO.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPIO.setMinimumSize(new Dimension(0, 200));
		scrollPIO
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPIO.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelInstallOutput.add(scrollPIO, BorderLayout.CENTER);
		
		ios.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		scrollPIO.setViewportView(ios);
		ios.setTabSize(4);
		ios.setFont(new Font("Tahoma", Font.PLAIN, 11));
		ios.setEditable(false);
		ios.setBorder(null);
		
		((DefaultCaret) ios.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		JPanel panel_2 = new JPanel();
		installExtra.setLeftComponent(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JToolBar toolBar = new JToolBar();
		toolBar.setAlignmentY(Component.CENTER_ALIGNMENT);
		panel_2.add(toolBar, BorderLayout.SOUTH);
		
		JButton button = new JButton("");
		button.setMnemonic(KeyEvent.VK_F5);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try {
					Thread.sleep(random.nextInt(2000));
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
				modDirectoryTree.resetMods();
				modFileTree.resetMods();
				getFrame().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		});
		button.setIcon(new ImageIcon(XCMGUI.class
					.getResource("/org/xcom/mod/gui/icons/table_refresh.png")));
		button.setToolTipText("Refresh");
		button.setActionCommand("");
		toolBar.add(button);
		
		JSplitPane splitPaneFileBrowser = new JSplitPane();
		panel_2.add(splitPaneFileBrowser, BorderLayout.CENTER);
		splitPaneFileBrowser.setResizeWeight(0.5);
		splitPaneFileBrowser.setOneTouchExpandable(true);
		splitPaneFileBrowser.setBorder(null);
		splitPaneFileBrowser.setAlignmentY(0.5f);
		splitPaneFileBrowser.setAlignmentX(0.5f);
		
		JPanel modDirectiresPane = new JPanel();
		modDirectiresPane.setToolTipText("These mods are ready to install.");
		modDirectiresPane.setMinimumSize(new Dimension(100, 160));
		modDirectiresPane.setBorder(new TitledBorder(null, "Mods", TitledBorder.LEADING,
					TitledBorder.TOP, null, null));
		modDirectiresPane.setAutoscrolls(true);
		splitPaneFileBrowser.setLeftComponent(modDirectiresPane);
		modDirectiresPane.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPDirectoryTree = new JScrollPane();
		scrollPDirectoryTree
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		modDirectiresPane.add(scrollPDirectoryTree, BorderLayout.CENTER);
		scrollPDirectoryTree.setPreferredSize(new Dimension(3, 223));
		scrollPDirectoryTree.setMinimumSize(new Dimension(22, 222));
		
		modDirectoryTree = new FileTreePanel(true);
		scrollPDirectoryTree.setViewportView(modDirectoryTree);
		
		modDirectoryTree.getTree().addTreeSelectionListener(new TreeSelectionListener() {
			
			public void valueChanged(TreeSelectionEvent e) {
				
				JFileTree tree = (JFileTree) e.getSource();
				
				int select = tree.getSelectionCount();
				
				if (select == 0) {
					installButton.setEnabled(false);
					textFieldModSelected.setText("");
					textFieldInstallAuthor.setText("");
					installModDescription.setText("");
					
				} else if (select == 1) {
					installButton.setEnabled(true);
					
					File f = tree.getRoot().getChildAt(tree.getSelectionRows()[0]).getFile();
					
					XMod mod = null;
					try {
						mod = (XMod) u.unmarshal(f);
					} catch (JAXBException ex) {
						ex.printStackTrace(System.err);
					}
					
					if (mod != null) {
						textFieldModSelected.setText(mod.getName());
						textFieldInstallAuthor.setText(mod.getAuthor());
						installModDescription.setText(mod.getDescription());
					}
					
				} else if (select > 1) {
					installButton.setEnabled(false);
					textFieldModSelected.setText("");
					textFieldInstallAuthor.setText("");
					installModDescription.setText("");
				}
			}
		});
		
		JPanel modFilesPane = new JPanel();
		modFilesPane.setToolTipText("The mod files.");
		modFilesPane.setMinimumSize(new Dimension(100, 160));
		modFilesPane.setBorder(new TitledBorder(null, "Mod Files", TitledBorder.LEADING,
					TitledBorder.TOP, null, null));
		modFilesPane.setAutoscrolls(true);
		splitPaneFileBrowser.setRightComponent(modFilesPane);
		modFilesPane.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPaneFileTree = new JScrollPane();
		scrollPaneFileTree
					.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPaneFileTree.setMinimumSize(new Dimension(22, 222));
		scrollPaneFileTree.setPreferredSize(new Dimension(22, 223));
		modFilesPane.add(scrollPaneFileTree, BorderLayout.CENTER);
		
		modFileTree = new FileTreePanel(false);
		scrollPaneFileTree.setViewportView(modFileTree);
		
		((DefaultCaret) ios.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
	}
	public static Config getConfig() {
		
		return config;
	}
	
	public static void setConfig(Config config) {
		
		XCMGUI.config = config;
	}
	
	public JTextArea getMos() {
		
		return mos;
	}
	
	public JTextArea getIos() {
		
		return ios;
	}
	
	private void addFileToFileList(JFileChooser fc, FileList fileList, JButton clearButton,
				java.io.File path) {
		
		fc.setCurrentDirectory(path);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setDragEnabled(true);
		
		int returnVal = fc.showOpenDialog(frame);
		
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			Path p = fc.getSelectedFile().toPath();
			// System.out.append("Adding: " + file.getName() + ".\n");
			fileList.getListModel().add(p);
			clearButton.setEnabled(true);
			
		} else {
			// System.out.append("Add command cancelled by user.\n");
		}
	}
	
	private void removeSelectedFiles(FileList fileList, JButton clearButton,
				JButton removeButton) {
		
		String message = "Are you sure you want to remove the selected files?";
		String title = "Remove file(s)?";
		List<Path> removeList = fileList.getSelectedValuesList();
		int n;
		if (removeList.size() <= 1) {
			n = JOptionPane.YES_OPTION;
		} else {
			n = JOptionPane.showConfirmDialog(frame, message, title, JOptionPane.YES_NO_OPTION);
		}
		
		switch (n) {
			case JOptionPane.YES_OPTION :
				
				fileList.getListModel().removeAll(removeList);
				
				if (fileList.getListModel().getSize() == 0) {
					fileList.clearSelection();
					removeButton.setEnabled(false);
					clearButton.setEnabled(false);
				}
				return;
			default :
				return;
		}
	}
	
	private void clearAllFiles(FileList fileList, JButton clearButton, JButton removeButton) {
		
		String message = "Are you sure you want to remove all the files?";
		String title = "Remove all files?";
		int n = JOptionPane.showConfirmDialog(frame, message, title,
					JOptionPane.YES_NO_OPTION);
		
		switch (n) {
			case JOptionPane.YES_OPTION :
				fileList.getListModel().clear();
				fileList.clearSelection();
				clearButton.setEnabled(false);
				removeButton.setEnabled(false);
				return;
			default :
				return;
		}
	}
	
	private void selectAdjustRemoveButtonEnabled(ListSelectionEvent e, FileList fileList,
				JButton removeButton) {
		
		if (e.getValueIsAdjusting() == false) {
			
			if (fileList.getSelectedIndex() == -1) {
				// No selection, disable remove button.
				removeButton.setEnabled(false);
			} else {
				// Selection, enable the fire button.
				removeButton.setEnabled(true);
			}
		}
	}
	
	private void fileListGetFilesDropped(java.util.List<File> files, FileList fileList,
				JButton clearButton) {
		
		Boolean containedDirsAsked = false;
		String message = "You have imported a directory.\nWould you still like to add the other files?";
		String title = "File drop error.";
		
		for (File f : files) {
			if (Files.isDirectory(f.toPath())) {
				if (!containedDirsAsked) {
					int n = JOptionPane.showConfirmDialog(fileList, message, title,
								JOptionPane.YES_NO_OPTION);
					
					switch (n) {
						case JOptionPane.YES_OPTION :
							containedDirsAsked = true;
							return;
						default :
							return;
					}
				}
			} else {
				fileList.getListModel().add(f.toPath().toAbsolutePath());
				fileList.setSelectedIndex(-1);
				clearButton.setEnabled(true);
			}
		}
	}
	
	public static void run(Boolean test) {
		
		EventQueue.invokeLater(new Runnable() {
			
			public void run() {
				
				try {
					new XCMGUI();
					XCMGUI.getFrame().setVisible(true);
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		});
	}
	
	class MakeButtonAction extends AbstractAction implements ActionListener {
		
		private static final long serialVersionUID = 1L;
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			JComponent src = (JComponent) e.getSource();
			
			String modName = fieldModName.getText();
			String modAuthor = fieldModAuthor.getText();
			String modDesc = fieldModDescription.getText();
			String ini = selectIni.getTextField().getText();
			
			List<Path> originalFiles = filesOriginal.getListModel();
			List<Path> editedFiles = filesEdited.getListModel();
			
			runMake(modName, modAuthor, modDesc, ini, originalFiles, editedFiles, src);
		}
		
		private void runMake(String modName, String modAuthor, String modDesc, String ini,
					List<Path> originalFiles, List<Path> editedFiles, final JComponent src) {
			
			Path iniPath = Paths.get(ini);
			
			if (modName.isEmpty() || modAuthor.isEmpty() || modDesc.isEmpty()) {
				// custom title, no icon
				JOptionPane.showMessageDialog(frame,
							"Fields cannot be empty. Please correct mistake.",
							"Incorrect mod settings.", JOptionPane.ERROR_MESSAGE);
				return;
				
			} else if (ini.isEmpty() && !Files.exists(iniPath)
						&& iniPath.getFileName().toString().contains("ini")) {
				JOptionPane.showMessageDialog(frame,
							"If you are slecting an ini file to patch please makre sure it is valid.",
							"Incorrect mod settings.", JOptionPane.ERROR_MESSAGE);
				return;
			} else if (originalFiles.isEmpty() || originalFiles.isEmpty()) {
				
				JOptionPane.showMessageDialog(frame,
							"You must add files to a mod. Please correct mistake.",
							"Files cannot be empty.", JOptionPane.ERROR_MESSAGE);
				return;
			} else if (!Maker.fileNamesMatch(originalFiles, editedFiles)) {
				JOptionPane.showMessageDialog(frame,
							"The number of files must be the same. Please correct mistake.",
							"Files must match.", JOptionPane.ERROR_MESSAGE);
				return;
			} else {
				List<File> files = modDirectoryTree.getRoots();
				
				for (File f : files) {
					if (f.getName().equals(modName + ".xmod.export.xml")) {
						JOptionPane.showMessageDialog(frame,
									"This mod name already exists. Please change.", "Invalid mod name.",
									JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
			}
			
			int n = JOptionPane.showConfirmDialog(frame, "Confirm to continue.", "Make mod.",
						JOptionPane.OK_CANCEL_OPTION);
			
			switch (n) {
				case JOptionPane.NO_OPTION :
					return;
				case JOptionPane.CANCEL_OPTION :
					return;
				default :
					break;
			}
			ModConfig modConfig = new ModConfig();
			
			printActionMessage("MAKE");
			
			modConfig.setAuthor(modAuthor);
			modConfig.setName(modName);
			modConfig.setDescription(modDesc);
			modConfig.setOriginalFiles(originalFiles);
			modConfig.setEditedFiles(editedFiles);
			modConfig.setIni(ini);
			
			List<String> originalPaths = new java.util.ArrayList<String>();
			List<String> editedPaths = new java.util.ArrayList<String>();
			
			String files = "";
			
			for (Path f : originalFiles) {
				files += (" " + f.getFileName());
				int unpackedECount = Paths.get(config.getUnpackedPath()).normalize()
							.getNameCount();
				
				Path p = f.toAbsolutePath();
				print(System.err, "Added original make file: " + p.toString() + "\n");
				
				originalPaths.add(p.subpath(unpackedECount, p.getNameCount()).toString());
			}
			
			print(MAKE, "ORIGINAL FILES [" + files.trim(), "]");
			modConfig.setOriginalFilePaths(originalPaths);
			
			files = "";
			
			for (Path f : editedFiles) {
				files += (" " + f.getFileName());
				
				Path p = f.toAbsolutePath();
				print(System.err, "Added edited make file: " + p.toString() + "\n");
				
				editedPaths.add(p.toAbsolutePath().toString());
			}
			
			print(MAKE, "EDITED FILES [" + files.trim(), "]");
			modConfig.setEditedFilePaths(editedPaths);
			
			final Maker main = new Maker(modConfig);
			RunInBackground<Object> work = new RunInBackground<Object>(frame, main, "Making mod "
						+ modName, src) {

				@Override
				public void after(Error e, Object r) {
					
					String msg = null;
					String title = null;
					
					switch (e) {
						case NOTHING :
							JOptionPane.showMessageDialog(frame, "Mod creation has finished.",
										"Finished Mod.", JOptionPane.PLAIN_MESSAGE);
							modDirectoryTree.resetMods();
							modFileTree.resetMods();
							break;
						case MAK_MOD_ACCESS_ERROR :
							msg = "There was an error processing the export output. Please try again.";
							title = "Unable to create export file.";
							break;
						case MAK_MOD_IO_ERROR :
							msg = "There was an IO error determining any changes between a set of files.";
							title = "Could not determine changes.";
							break;
						case MAK_HASH_GET_ERROR :
							msg = "There was an error calculating a search hash or file hash.";
							title = "Hash Calculation error.";
							break;
						case XML_SAVE_ERROR :
							msg = "The export file could not be saved. Try again.";
							title = "Could not save the export file.";
							break;
						case MAK_SAVE_MOD_FILES :
							msg = "Could not save mod files. Try again.";
							title = "Files could not be saved.";
							break;
						case MAK_UPK_FILE_NOTEXTRACTED:
							@SuppressWarnings("unchecked")
							List<Path> uncFiles = (List<Path>) r;
							
							String fileNames = "";
							
							for (Path p : uncFiles) {
								fileNames += (p.getFileName() + " ");
							}
							
							int n = JOptionPane.showConfirmDialog(XCMGUI.getFrame(),
										"Cannot continue. [" + fileNames + "] "
													+ (uncFiles.size() > 1 ? "are" : "is")
													+ " not extracted.\nExtraction will take some time especially if more than one file.\n\nDo you want to extract now?", title,
										JOptionPane.YES_NO_OPTION);
							
							switch (n) {
								case JOptionPane.YES_OPTION :
									for (Path p : uncFiles) {
										new ExtractInBackGround(p, src, XCMGUI.getFrame()).execute();
									}
									break;
								default :
							}
							
							msg = null;
							
						default :
							break;
					}
					
					if (msg != null) {
						JOptionPane.showMessageDialog(frame, msg, title, JOptionPane.ERROR_MESSAGE);
					}
				}
				
			};
			work.addPropertyChangeListener(work);
			work.execute();
		}
	}
	
	class InstallButtonAction implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			final JComponent src = (JComponent) e.getSource();
			
			JFileTree tree = modDirectoryTree.getTree();
			java.io.File xmod = tree.getRoot().getChildAt(tree.getSelectionRows()[0]).getFile();
			
			int n = JOptionPane.showConfirmDialog(frame, "Confirm to continue.",
						"Install mod.", JOptionPane.OK_CANCEL_OPTION);
			
			switch (n) {
				case JOptionPane.NO_OPTION :
					return;
				case JOptionPane.CANCEL_OPTION :
					return;
				default :
					break;
			}
			
			final Installer main = new Installer(xmod);
			RunInBackground<Object> install = new RunInBackground<Object>(frame, main,
						"Installing " + xmod.getName(), src) {
				
				@Override
				public void after(Error e, Object ret) {
					
					XMod installed = ((Installer) main).getInstallPackage();
					
					if (installed.getIsInstalled()) {
						int n = JOptionPane
									.showConfirmDialog(
												frame,
												"If you have changed files they need to be patched into XComGame.exe.\nWould you like to run XShape on XComGame.exe?",
												"Run XShape.", JOptionPane.YES_NO_OPTION);
						
						switch (n) {
							case JOptionPane.YES_OPTION :
								Path exeFile = Paths.get(config.getXcomPath(), RELATIVE_EXE_PATH);
								
								runXShapeInBackGround(exeFile, editedUpks, src, FACING_STREAM);
								
								return;
							default :
								return;
						}
					} else {
						
						String msg = null;
						String title = e.getMsg();
						
						print(title, "");
						
						switch (e) {
							case INS_UPK_FILE_NF :
								msg = "Please check your settings and check the install file.";
								break;
							case INS_UPK_FILE_NA :
								msg = "Upk file could not be accessed please close other programs.";
								break;
							case INS_UPK_RES_NF :
								msg = "Upk resource could not be found. The mod may already be installed.";
								break;
							case XML_SAVE_ERROR :
								msg = "The log could not be saved. Changes may need to be reverted.";
								break;
							case INS_FATAL :
								msg = "Fatal error: The searcher threads were closed unexpectedly. Try again.";
								break;
							case INS_UPK_FILE_COMPRESSED :
								@SuppressWarnings("unchecked")
								List<Path> uncFiles = (List<Path>) ret;
								
								String fileNames = "";
								
								for (Path p : uncFiles) {
									fileNames += (p.getFileName() + " ");
								}
								
								int n = JOptionPane.showConfirmDialog(XCMGUI.getFrame(),
											"Cannot continue. [" + fileNames + "] "
														+ (uncFiles.size() > 1 ? "are" : "is")
														+ " not decompressed do you want to decompress now?", title,
											JOptionPane.YES_NO_OPTION);
								
								switch (n) {
									case JOptionPane.YES_OPTION :
										new DecompressInBackGround(uncFiles, src).execute();
										break;
									default :
								}
								
								msg = null;
							default :
								break;
						}
						
						if (msg != null) {
							JOptionPane.showMessageDialog(frame, msg, title, JOptionPane.ERROR_MESSAGE);
						}
					}
					setProgress(0);
				}
			};
			install.addPropertyChangeListener(install);
			install.execute();
		}
	}
	
	class XShapeButtonAction implements ActionListener {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			JComponent src = (JComponent) e.getSource();
			
			Path exeFile = Paths.get(config.getXcomPath(), RELATIVE_EXE_PATH);
			
			if (editedUpks.isEmpty()) {
				int n = JOptionPane
							.showConfirmDialog(
										frame,
										"It seems there have been no edited upk files. Would you still like to run XShape on the XCOM exe with the default files?",
										"Run XShape.", JOptionPane.YES_NO_OPTION);
				switch (n) {
					case JOptionPane.YES_OPTION :
						
						java.util.ArrayList<Path> paths = new java.util.ArrayList<>();
						
						paths.add(Paths.get(config.getCookedPath().toString(), "Core.upk"));
						paths.add(Paths.get(config.getCookedPath().toString(), "XComGame.upk"));
						paths.add(Paths
									.get(config.getCookedPath().toString(), "XComStrategyGame.upk"));
						
						runXShapeInBackGround(exeFile, paths, src, FACING_STREAM);
						return;
					default :
						return;
				}
			} else {
				int n = JOptionPane.showConfirmDialog(frame,
							"Would you like to run XShape on XComGame.exe?", "Run XShape.",
							JOptionPane.YES_NO_OPTION);
				switch (n) {
					case JOptionPane.YES_OPTION :
						
						runXShapeInBackGround(exeFile, editedUpks, src, FACING_STREAM);
						return;
					default :
						return;
				}
			}
		}
	}
	
	/**
	 * Runs XSaphe in the background thread.
	 * 
	 * @param exeFile
	 * @param paths
	 * @param src
	 * @param src2
	 * @param stream
	 */
	public static void runXShapeInBackGround(final Path exeFile,
				final java.util.ArrayList<Path> paths, final Path ini, final JComponent src,
				final Stream stream) {
		XShape xs = null;
		String title = "XShape";
		try {
			if (ini == null) {
				xs = new XShape(exeFile, paths, stream);
			} else if (paths == null) {
				xs = new XShape(exeFile, ini, stream);
				title = "IniPatch";
			} else {
				xs = new XShape(exeFile, paths, ini, stream);
				title = "XPatch";
			}
		} catch (IOException ex) {
			String msg = "There was an error backing up your XComGame.exe.";
			ex.printStackTrace();
			JOptionPane.showMessageDialog(Main.getFrame(), msg, title,
						JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		RunInBackground<Void> swingXShape = new RunInBackground<Void>(Main.getFrame(), xs,
					"Shaping " + exeFile.getFileName(), src) {
			
			@Override
			public void after(Error e, Void r) {
				String title = "XShape";
				String msg = e.getMsg();
				int op = JOptionPane.ERROR_MESSAGE;
				
				if (ini == null) {} else if (paths == null) {
					title = "IniPatch";
				} else {
					title = "XPatch";
				}
				
				switch (e) {
					case NOTHING :
						msg = "Finished patching XComGame.exe";
						op = JOptionPane.PLAIN_MESSAGE;
						if (editedUpks != null) {
							editedUpks.clear();
						}
						break;
					case XSHA_HASH_GET_ERROR :
					case XSHA_MOD_ACCESS_ERROR :
					case XSHA_UPK_FILE_COMPRESSED :
					case XSHA_UPK_FILENAME_ERROR :
					case XSHA_PATCH_NOT_REQUIRED :
					case XSHA_INI_PATCHERROR :
					default :
						break;
				}
				JOptionPane.showMessageDialog(Main.getFrame(), msg, title, op);
			}
		};
		swingXShape.addPropertyChangeListener(swingXShape);
		swingXShape.execute();
	}
	
	/**
	 * Runs XSaphe in the background thread.
	 * 
	 * @param exeFile
	 * @param paths
	 * @param src
	 * @param stream
	 */
	public static void runXShapeInBackGround(Path exeFile, java.util.ArrayList<Path> paths,
				JComponent src, Stream stream) {
		runXShapeInBackGround(exeFile, paths, null, src, stream);
	}
	
	/**
	 * Runs XSaphe in the background thread.
	 * 
	 * @param exeFile
	 * @param paths
	 * @param src
	 * @param stream
	 */
	public static void runXShapeInBackGround(Path exeFile, Path ini, JComponent src,
				Stream stream) {
		runXShapeInBackGround(exeFile, null, ini, src, stream);
	}
	
	/**
	 * Downloads a zipped tool into the temp folder and extracts it into the tool
	 * folder. Caution unzipping occurs in the event thread dispatcher. If speed
	 * becomes an issue refactor to work in the background bt creating a
	 * swingWorker in the done method.
	 * 
	 * @param url
	 * @param saveAs
	 * @param src
	 * @throws MalformedURLException
	 */
	public static void downloadZippedTool(String url, String saveAs, final JComponent src,
				final Runnable task) throws MalformedURLException {
		
		DownloadWorker dlDecom = new DownloadWorker(url, Paths.get("tools"), saveAs, src) {
			@Override
			protected void done() {
				Path zipDir = null;
				try {
					zipDir = this.get();
				} catch (InterruptedException | ExecutionException ex) {
					downloadFailedMsg();
					ex.printStackTrace(System.err);
					return;
				}
				if (!this.isCancelled()) {
					try {
						
						Main.unZip(zipDir, Paths.get("tools"));
					} catch (ZipException ex) {
						downloadUnzipFailed();
						ex.printStackTrace(System.err);
						return;
					}
					downloadCompleteAndUnzipped();
					if (src != null) {
						src.setEnabled(false);
					}
					if (task != null) {
						task.run();
					}
				}
			}
		};
		dlDecom.execute();
	}
	
	/**
	 * Will decompress a chosen file. Checks for tool existence and gets it if
	 * needed.
	 * 
	 * @param fileToDecom
	 * @param decompress
	 * @throws DownloadFailedException
	 * @throws ZipException
	 */
	public static void decompressWithToolCheck(final Path fileToDecom, Path decompress,
				Boolean inBackGround) throws DownloadFailedException, ZipException {
		if (Files.notExists(decompress)) {
			URL url = null;
			try {
				url = new URL("http://www.gildor.org/down/32/umodel/decompress.zip");
			} catch (MalformedURLException ignore) {
				ignore.printStackTrace(System.err);
			}
			final String saveAs = "decompress.zip";
			Path zip = Main.download(saveAs, url);
			Main.unZip(zip, Paths.get("tools"));
		}
		if (inBackGround) {
			new DecompressInBackGround(fileToDecom).execute();
		} else {
			Main.decompress(fileToDecom);
		}
	}
	
	/**
	 * Will decompress a chosen file. Checks for tool existence and gets it if
	 * needed.
	 * 
	 * @param extract
	 * @param upkToExtract
	 * @param src
	 * @param parent
	 * @throws DownloadFailedException
	 * @throws ZipException
	 */
	public static void extractWithToolCheck(final Path upkToExtract, final Path extract,
				final JComponent src, Boolean inBackGround, Component parent)
				throws DownloadFailedException, ZipException {
		
		if (Files.notExists(extract)) {
			
			URL url = null;
			try {
				url = new URL("http://www.gildor.org/down/32/umodel/extract.zip");
			} catch (MalformedURLException ignore) {
				ignore.printStackTrace(System.err);
			}
			final String saveAs = "extract.zip";
			
			Path zip = Main.download(saveAs, url);
			Main.unZip(zip, Paths.get("tools"));
		}
		if (inBackGround) {
			new ExtractInBackGround(upkToExtract, src, parent).execute();
		} else Main.extract(upkToExtract);
		
	}
	
	@Override
	public void run() {
		
	}
	
	private static void print(String... strings) {
		print(MAIN, strings);
	}
}
