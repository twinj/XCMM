package org.xcom.pinstaller.gui;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.zip.ZipException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EtchedBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.DefaultCaret;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.xcom.main.shared.DownloadFailedException;
import org.xcom.main.shared.Main;
import org.xcom.main.shared.XmlSaveException;
import org.xcom.main.shared.entities.Config;
import org.xcom.main.shared.entities.HexEdit;
import org.xcom.main.shared.entities.ModConfig;
import org.xcom.main.shared.entities.ResFile;
import org.xcom.main.shared.entities.XMod;
import org.xcom.mod.gui.shared.GetFilePanel;
import org.xcom.mod.gui.streams.Stream;
import org.xcom.mod.gui.workers.DecompressInBackGround;
import org.xcom.mod.gui.workers.RunInBackground;
import org.xcom.mod.tools.installer.Installer;
import org.xcom.mod.tools.xshape.MHash;
import org.xcom.mod.tools.xshape.XShape;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class PInstall extends Main {
	
	
	private JButton close;
	
	private static JFrame frame;
	private XMod mod = null;
	
	Path modPath = Paths.get("install", "install.xmod.export.xml");
	Path iniPath = Paths.get("install", "install.ini");
	private JTextArea hos;
	private GetFilePanel xcomInstallPanel;
	private GetFilePanel gilderDecomPanel;
	private GetFilePanel chooseModPanel;
	
	/**
	 * @wbp.parser.entryPoint
	 */
	public PInstall() {
		frame = new JFrame();
		Main.contentPane = frame;
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(
					PInstall.class.getResource("/org/xcom/pinstaller/gui/XCMM-096x096.png")));
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {}
		
		frame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				xcomInstallPanel.getTextField().setText(config.getXcomPath());
				gilderDecomPanel.getTextField().setText(config.getCompressorPath());
				
				if (Files.exists(modPath)) {
					try {
						mod = (XMod) u.unmarshal(modPath.toFile());
					} catch (JAXBException ex) {
						ex.printStackTrace(System.err);
					}
					chooseModPanel.getTextField().setText(mod.getName());
				} else {
					chooseModPanel.getTextField().setText("Select a valid .xmod.* to install.");
				}
			}
		});
		frame.setMinimumSize(new Dimension(275, 275));
		
		initUI();
		
		MAIN = Stream.getStream(MAIN_DELEGATE, hos);
		INSTALL = Stream.getStream(INSTALL_DELEGATE, hos);
		frame.setLocation(200, 200);
		frame.pack();
		
	}
	
	public final void initUI() {
		frame.setTitle("XCMM Installer");
		frame.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JPanel aboutModPanel = new JPanel();
		tabbedPane.addTab("XMod", null, aboutModPanel, null);
		aboutModPanel.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		aboutModPanel.add(scrollPane);
		
		JEditorPane dtrpnWidthHeight = new JEditorPane();
		dtrpnWidthHeight.setOpaque(false);
		dtrpnWidthHeight.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		dtrpnWidthHeight.setEditable(false);
		dtrpnWidthHeight.setContentType("text/html");
		scrollPane.setViewportView(dtrpnWidthHeight);
		
		dtrpnWidthHeight.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent event) {
				if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
					
					if (Desktop.isDesktopSupported()) {
						try {
							Desktop.getDesktop().browse(event.getURL().toURI());
						} catch (IOException | URISyntaxException ex) {}
					}
				}
			}
		});
		
		//if (Files.exists(Paths.get("install", "install.html"))) {
			//try {
				//dtrpnWidthHeight.setPage(Paths.get("install", "install.html").toUri().toURL());
			//} catch (IOException ex) {}
		//} else {
			
			dtrpnWidthHeight
						.setText("<html><p>"
									+ "<a href='https://github.com/twinj/XCMM'>GitHub</a></p>"
									+ "<a href='http://forums.nexusmods.com/index.php?/topic/839384-xcmm-mod-manager-in-java/'>Forums</a>"
									+ "</img></html>");
	//	}
		
		Box horizontalBox_3 = Box.createHorizontalBox();
		aboutModPanel.add(horizontalBox_3, BorderLayout.SOUTH);
		
		JButton btnNewButton = new JButton("Install");
		btnNewButton.addActionListener(new SaveAction());
		horizontalBox_3.add(btnNewButton);
		
		JLabel lblNewLabel = new JLabel("  V:1.07c-BETA");
		horizontalBox_3.add(lblNewLabel);
		
		Component horizontalGlue_4 = Box.createHorizontalGlue();
		horizontalBox_3.add(horizontalGlue_4);
		
		JButton button = new JButton("Restore Original");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				final Path home = Paths.get(config.getUnpackedPath());
				final String uSize = ".uncompressed_size.bkp";
				final String oComp = ".original_compressed";
				
				int n = JOptionPane
							.showConfirmDialog(
										frame.getContentPane(),
										"This will replace existing resources in your XCom Installation that have been changed by a mod.\n\nDo you want to continue?",
										"Restore Game.", JOptionPane.YES_NO_OPTION);
				
				switch (n) {
					case JOptionPane.YES_OPTION :
						String msg = "The files have been restored.";
						String title = "Restore Game";
						int op = JOptionPane.PLAIN_MESSAGE;
						
						print("ATTEMPT RESTORE ORIGINAL UPK FILES", "");
						print("WALKING UNPACKED PATH [" + home, "]");
						
						Finder f = null;
						try {
							f = new Finder(uSize, oComp);
							Files.walkFileTree(home, f);
							
						} catch (IOException ex) {
							msg = "There was an error restoring the files. Please report.";
							ex.printStackTrace(System.err);
						}
						if (f != null && !(f.getNumMatches() > 0)) {
							msg = "There were no files to restore.";
							
						} else {}
						
						JOptionPane.showMessageDialog(frame.getContentPane(), msg, title, op);
						
						if (f != null && (f.getNumMatches() > 0)) {
							
							Path exeFile = Paths.get(config.getXcomPath(), RELATIVE_EXE_PATH);
							Path iniFile = Paths.get(config.getUnpackedPath(), "install.ini");
							runXShapeInBackGround(exeFile, (ArrayList<Path>) f.getUpks(), Files
										.exists(iniFile) == true ? iniFile : null,
										(JComponent) e.getSource(), Main.MAIN,
										"XCMM Installer has finished re-patching the game.");							
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
		
		horizontalBox_3.add(button);
		button.setAlignmentX(0.5f);
		
		JPanel settingsTab = new JPanel();
		tabbedPane.addTab("Settings", null, settingsTab, null);
		tabbedPane.setEnabledAt(1, true);
		GridBagLayout gbl_settingsTab = new GridBagLayout();
		gbl_settingsTab.columnWidths = new int[]{329, 0};
		gbl_settingsTab.rowHeights = new int[]{284, 0};
		gbl_settingsTab.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_settingsTab.rowWeights = new double[]{0.0, Double.MIN_VALUE};
		settingsTab.setLayout(gbl_settingsTab);
		
		JPanel generalTab = new JPanel();
		generalTab.setAlignmentY(Component.TOP_ALIGNMENT);
		generalTab.setAlignmentX(Component.RIGHT_ALIGNMENT);
		GridBagConstraints gbc_generalTab = new GridBagConstraints();
		gbc_generalTab.fill = GridBagConstraints.BOTH;
		gbc_generalTab.gridx = 0;
		gbc_generalTab.gridy = 0;
		settingsTab.add(generalTab, gbc_generalTab);
		generalTab.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		
		xcomInstallPanel = new GetFilePanel("XCOM installation path:", null,
					JFileChooser.DIRECTORIES_ONLY);
		
		gilderDecomPanel = new GetFilePanel("Gildor's Unreal Decompresor:", new java.io.File(
					System.getProperty("user.dir")), JFileChooser.FILES_ONLY);
		
		chooseModPanel = new GetFilePanel("Choose a mod:", new java.io.File(USER_DIR
					+ "\\mods"), JFileChooser.FILES_ONLY);
		
		Box horizontalBox_2 = Box.createHorizontalBox();
		
		JButton btnVerifySettings = new JButton("Install");
		horizontalBox_2.add(btnVerifySettings);
		
		Component horizontalGlue_1 = Box.createHorizontalGlue();
		horizontalBox_2.add(horizontalGlue_1);
		
		close = new JButton("Done");
		horizontalBox_2.add(close);
		close.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent event) {
				frame.dispose();
			}
		});
		
		close.setAlignmentX(0.5f);
		
		JSeparator separator = new JSeparator();
		separator.setPreferredSize(new Dimension(86, 2));
		GroupLayout gl_generalTab = new GroupLayout(generalTab);
		gl_generalTab.setHorizontalGroup(
			gl_generalTab.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_generalTab.createSequentialGroup()
					.addGap(3)
					.addGroup(gl_generalTab.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_generalTab.createSequentialGroup()
							.addComponent(xcomInstallPanel, GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
							.addGap(4))
						.addGroup(gl_generalTab.createSequentialGroup()
							.addComponent(gilderDecomPanel, GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
							.addGap(4))
						.addGroup(gl_generalTab.createSequentialGroup()
							.addGap(68)
							.addComponent(separator, GroupLayout.PREFERRED_SIZE, 1, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_generalTab.createSequentialGroup()
							.addComponent(chooseModPanel, GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
							.addGap(4))
						.addGroup(gl_generalTab.createSequentialGroup()
							.addComponent(horizontalBox_2, GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
							.addGap(4)))
					.addGap(78))
		);
		gl_generalTab.setVerticalGroup(
			gl_generalTab.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_generalTab.createSequentialGroup()
					.addGap(6)
					.addComponent(xcomInstallPanel, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
					.addGap(8)
					.addComponent(gilderDecomPanel, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
					.addGap(11)
					.addGroup(gl_generalTab.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_generalTab.createSequentialGroup()
							.addGap(48)
							.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(chooseModPanel, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED, 5, Short.MAX_VALUE)
					.addComponent(horizontalBox_2, GroupLayout.PREFERRED_SIZE, 29, GroupLayout.PREFERRED_SIZE)
					.addGap(71))
		);
		generalTab.setLayout(gl_generalTab);
		btnVerifySettings.addActionListener(new SaveAction());
		
		JScrollPane outPutTab = new JScrollPane();
		tabbedPane.addTab("Console output", null, outPutTab, null);
		outPutTab.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		outPutTab
					.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		outPutTab.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		outPutTab.setAutoscrolls(true);
		
		hos = new JTextArea();
		hos.setLineWrap(true);
		hos.setEditable(false);
		hos.setTabSize(4);
		hos.setFont(new Font("Tahoma", Font.PLAIN, 11));
		hos.setBorder(null);
		outPutTab.setViewportView(hos);
		
		((DefaultCaret) hos.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
	}
	
	/**
	 * Save action
	 * 
	 * @author Daniel Kemp
	 * 
	 */
	class SaveAction implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			final JComponent src = (JButton) e.getSource();
			
			final String xComPath = xcomInstallPanel.getTextField().getText();
			final String compressorPath = gilderDecomPanel.getTextField().getText();
			String modSelected = chooseModPanel.getTextField().getText();
			
			final String unpackedPath = config.getUnpackedPath();
			
			Boolean isOk = true;
			
			String msg = "ERROR";
			String title = "Incorrect setting.";
			int op = JOptionPane.ERROR_MESSAGE;
			
			boolean xPathValid = Main.isXComPathValid(xComPath);
			
			if (xPathValid) {
				config.setXcomPath(xComPath);
			}
			
			if (Files.exists(Paths.get(compressorPath))) {
				config.setCompressorPath(compressorPath);
			}
			
			if (modSelected.isEmpty()
						|| modSelected.equals("Select a valid .xmod.* to install.")) {
				isOk = false;
				msg = "You must select a valid mod.";
				
			} else if (!modSelected.equals(mod.getName())
						&& Files.notExists(Paths.get(modSelected))) {
				isOk = false;
				msg = "You must select a valid mod.";
				
			} else if (xComPath.isEmpty()) {
				
				isOk = false;
				msg = "You must set the XCom installation path.";
				
			} else if (!Main.isXComPathValid(xComPath)) {
				isOk = false;
				msg = "The system cannot verify your XCom installtion.";
				
			} else if (Files.notExists(Paths.get(compressorPath))) {
				isOk = false;
				
				try {
					isOk = getToolsToPerformInitialVerification(unpackedPath, null, src);
				} catch (HeadlessException | MalformedURLException ignore) {}
				
				if (!isOk) {
					msg = "You need to select a valid path for Gildor's Unreal Package Decompressor to install the mod.";
				} else {
					msg = null;
				}
			} else if (isOk) { // Is Ok
			
				msg = null;
			}
			config.setUnpackedPath(unpackedPath);
			
			try {
				Main.saveXml(config);
			} catch (XmlSaveException ex) {
				msg = "Cannot install. There was an error saving the settings. Try again.";
				title = "Settings not saved.";
				ex.printStackTrace(System.err);
			}
			if (msg != null) {
				JOptionPane.showMessageDialog(frame.getContentPane(), msg, title, op);
			}
			
			if (isOk) {
				
				if (!modSelected.equals(mod.getName())) {
					
					modPath = Paths.get(modSelected);
					
					try {
						mod = (XMod) u.unmarshal(modPath.toFile());
					} catch (JAXBException ex) {
						JOptionPane.showMessageDialog(frame.getContentPane(),
									"Cannot continue. Mod file could not be marshalled.", "Installer",
									JOptionPane.ERROR_MESSAGE);
						ex.printStackTrace(System.err);
						return;
					}
				}
				
				if (mod.getIni() != null) {
					iniPath = Paths.get(modPath.getParent().toString(), mod.getIni());
					if (Files.notExists(iniPath)) {
						iniPath = null;
					}
				}
				
				int n = JOptionPane.showConfirmDialog(frame.getContentPane(),
							"Confirm to install [" + mod.getName() + "]", "Installer",
							JOptionPane.OK_CANCEL_OPTION);
				
				switch (n) {
					case JOptionPane.NO_OPTION :
						return;
					case JOptionPane.CANCEL_OPTION :
						return;
					default :
						break;
				}
				
				final Path iniFile = iniPath;
				
				RunInBackground<Object> install = new RunInBackground<Object>(frame,
							new Installer(modPath.toFile()), "Installing " + mod.getName(), src) {
					
					@Override
					protected void after(Error e, Object ret) {
						afterInstallTry(this, (Installer) main, src);
					}				
					protected void afterInstallTry(final RunInBackground<Object> install,
								final Installer main, final JComponent src) {
						Object ret = null;
						try {
							ret = install.get();
						} catch (InterruptedException | ExecutionException ex) {}
						
						final XMod installed = ((Installer) main).getInstallPackage();
						Error e = main.getError();
						
						if (installed.getIsInstalled()) {
							Path exeFile = Paths.get(config.getXcomPath(), RELATIVE_EXE_PATH);
							
							runXShapeInBackGround(exeFile, editedUpks, iniFile, src, MAIN,
										"XCMM Installer has finished installing the mod.");
							
						} else {
							
							String msg = null;
							String title = e.getMsg();
							
							print(MAIN, title, "");
							
							switch (e) {
								case INS_UPK_FILE_NF :
									msg = "Please check your settings.";
									break;
								case INS_UPK_FILE_NA :
									msg = "Upk file could not be accessed please close other programs that may be using the file.";
									break;
								case INS_UPK_RES_NF :
									msg = "Unedited upk resource could not be found. The mod may already be installed.";
									break;
								case XML_SAVE_ERROR :
									msg = "The log could not be saved. Changes may need to be reverted.";
									break;
								case INS_FATAL :
									msg = "The searcher threads closed unexpectedly";
									break;
								case INS_UPK_FILE_COMPRESSED :
									@SuppressWarnings("unchecked")
									List<Path> uncFiles = (List<Path>) ret;
									
									String fileNames = "";
									
									for (Path p : uncFiles) {
										fileNames += (p.getFileName() + " ");
									}
									
									JOptionPane
												.showMessageDialog(
															frame.getContentPane(),
															"Cannot continue. ["
																		+ fileNames
																		+ "] "
																		+ (uncFiles.size() > 1 ? "are" : "is")
																		+ " not decompressed.\nThe system will decompress the files now.\n\nAfter it has finished restart the installaltion.",
															title, JOptionPane.WARNING_MESSAGE);
									
									DecompressInBackGround dib = new DecompressInBackGround(uncFiles, src) {
										@Override
										protected void after() {
											final RunInBackground<Object> install = new RunInBackground<Object>(frame, main,
														"Trying install again..." + installed.getName(), src) {
												@Override
												protected void after(Error e, Object ret) {
													afterInstallTry(this, (Installer) main, src);
												}
											};
											install.addPropertyChangeListener(install);
											install.execute();
										}
									};
									dib.execute();
									
									msg = null;
								default :
									break;
							}
							if (msg != null) {
								JOptionPane.showMessageDialog(frame.getContentPane(), msg, title,
											JOptionPane.ERROR_MESSAGE);
							}
						}
					}						
				};
				install.addPropertyChangeListener(install);
				install.execute();
			}
		}

		public boolean getToolsToPerformInitialVerification(final String unpackedPath,
					final Path cookedCore, final JComponent src) throws HeadlessException,
					MalformedURLException {
			
			final Path decompress = Paths.get("tools\\decompress.exe");
			
			int n1 = JOptionPane
						.showConfirmDialog(
									frame.getContentPane(),
									"To get started XCMM Installer can download Gildor's Unreal decompressor.\nOr you can select its location.\n\nWould you like XCMM to download?",
									"Download Decompressor?", JOptionPane.YES_NO_OPTION);
			
			switch (n1) {
				case JOptionPane.YES_OPTION :
					new SwingWorker<Void, Void>() {
						@Override
						protected Void doInBackground() throws Exception {
							if (src != null) {
								src.setEnabled(false);
							}
							close.setEnabled(false);
							frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
							Main.openDesktopBrowser(HTTP_WWW_GILDOR_ORG);
							decompressWithToolCheck(null, decompress, true);
							return null;
						}
						@Override
						protected void done() {
							try {
								get();
							} catch (InterruptedException | ExecutionException ex) {
								ex.printStackTrace();
							}
							frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
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

	public static void main(String[] args) {
		Path path = Paths.get(Config.PATH);
		
		try {
			md = MessageDigest.getInstance(MHash.ALGORITHM);
			jc = JAXBContext.newInstance(Config.class, HexEdit.class, ModConfig.class,
						ResFile.class, XMod.class);
			u = jc.createUnmarshaller();
			m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			
		} catch (Exception e) {
			print("FATAL ERROR: Could not create services.\n");
			e.printStackTrace(System.err);
			System.exit(Error.SYS_SERVICE_CREATE_FAIL.ordinal());
		}
		
		if (Files.exists(path)) {
			try {
				PInstall.config = (Config) PInstall.u.unmarshal(path.toFile());
			} catch (JAXBException ex) {}
		} else {
			PInstall.config = new Config();
		}
		
		PInstall.runc();
	}
	
	public static void runc() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {			
				try {
					new PInstall();
					Main.contentPane.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
		});
	}	
	@Override
	public void run() {}
	
	/**
	 * Runs XSaphe in the background thread.
	 */
	public static void runXShapeInBackGround(final Path exeFile,
				final java.util.ArrayList<Path> paths, final Path ini, final JComponent src,
				final Stream stream, final String doneMessage) {
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
				String title = "Installer";
				String msg = e.getMsg();
				int op = JOptionPane.ERROR_MESSAGE;
				
				if (ini == null) {} else if (paths == null) {
					title = "IniPatch";
				} else {
					title = "XPatch";
				}
				
				switch (e) {
					case NOTHING :
						msg = doneMessage;
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
	
	private static void print(String... strings) {
		print(MAIN, strings);
	}
	
	/**
	 * Will decompress a chosen file. Checks for tool existence and gets it if
	 * needed.
	 */
	public static void decompressWithToolCheck(final Path fileToDecom, Path decompress,
				Boolean inBackGround) throws DownloadFailedException, ZipException {
		if (Files.notExists(decompress)) {
			URL url = null;
			try {
				url = new URL(HTTP_WWW_GILDOR_ORG_DOWN_DECOMPRESS_ZIP);
			} catch (MalformedURLException ignore) {
				ignore.printStackTrace(System.err);
			}
			final String saveAs = "decompress.zip";
			Path zip = Main.download(saveAs, url);
			Main.unZip(zip, Paths.get("tools"));
		}
	}
}
