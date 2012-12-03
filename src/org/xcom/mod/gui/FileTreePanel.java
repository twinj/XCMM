package org.xcom.mod.gui;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;

import org.xcom.main.shared.entities.Config;
import org.xcom.mod.gui.JFileTree.FileTreeNode;

/**
 * @author Kirill Grouchnikov
 */
@SuppressWarnings("rawtypes")
public class FileTreePanel extends JList {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * File system view.
	 */
	protected static FileSystemView fsv = FileSystemView.getFileSystemView();
	
	private List<File> roots;
	
	/**
	 * Renderer for the file tree.
	 * 
	 * @author Kirill Grouchnikov
	 */
	private static class FileTreeCellRenderer extends DefaultTreeCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Icon cache to speed the rendering.
		 */
		private Map<String, Icon> iconCache = new HashMap<String, Icon>();

		/**
		 * Root name cache to speed the rendering.
		 */
		private Map<File, String> rootNameCache = new HashMap<File, String>();

		private Boolean installTree;

	
		public FileTreeCellRenderer(Boolean installTree) {
			super();
			this.installTree = installTree;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.tree.DefaultTreeCellRenderer#getTreeCellRendererComponent
		 * (javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int,
		 * boolean)
		 */
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {
			FileTreeNode ftn = (FileTreeNode) value;
			File file = ftn.getFile();
			String filename = "";
			if (file != null) {
				if (ftn.isFileSystemRoot()) {
					// long start = System.currentTimeMillis();
					filename = this.rootNameCache.get(file);
					if (filename == null) {
						
						if (! installTree) {
							filename = fsv.getSystemDisplayName(file);
						} else {
							
//							XMod xmod = null;
//							
//							try {
//								xmod = (XMod) XCM.getU().unmarshal(file);
//							} catch (JAXBException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
							filename = (file.getName().replace(".xmod.export.xml", ""));
						}
						this.rootNameCache.put(file, filename);
					}
					// long end = System.currentTimeMillis();
					// System.out.println(filename + ":" + (end - start));
				} else {
					filename = file.getName();
				}
			}
			JLabel result = (JLabel) super.getTreeCellRendererComponent(tree,
					filename, sel, expanded, leaf, row, hasFocus);
			if (file != null) {
				Icon icon = this.iconCache.get(filename);
				if (icon == null) {
					// System.out.println("Getting icon of " + filename);
					icon = fsv.getSystemIcon(file);
					this.iconCache.put(filename, icon);
				}
				result.setIcon(icon);
			}
			return result;
		}
	}

	@SuppressWarnings("unused")
	private static class PrintFiles extends SimpleFileVisitor<Path> {

		// Print information about
		// each type of file.
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attr) {
			if (attr.isSymbolicLink()) {
				System.out.format("Symbolic link: %s ", file);
			} else if (attr.isRegularFile()) {
				System.out.format("Regular file: %s ", file);
			} else {
				System.out.format("Other: %s ", file);
			}
			System.out.println("(" + attr.size() + "bytes)");
			return CONTINUE;
		}

		// Print each directory visited.
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
			System.out.format("Directory: %s%n", dir);
			return CONTINUE;
		}

		// If there is some error accessing
		// the file, let the user know.
		// If you don't override this method
		// and an error occurs, an IOException
		// is thrown.
		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) {
			System.err.println(exc);
			return CONTINUE;
		}
	}

	private static class Finder extends SimpleFileVisitor<Path> {

		private final PathMatcher matcher;
		private int numMatches = 0;
		private java.util.List<File> modHomePaths = new java.util.ArrayList<File>();
		private Boolean installTree;

		Finder(String pattern, Boolean installTree) {
			this.installTree = installTree;
			
			matcher = FileSystems.getDefault()
					.getPathMatcher("glob:" + pattern);
		}

		// Compares the glob pattern against
		// the file or directory name.
		void find(Path file) {
			Path name = file.getFileName();
			if (name != null && matcher.matches(name)) {
				numMatches++;
				if (installTree) {
					modHomePaths.add(file.toFile());
				} else {
					modHomePaths.add(file.getParent().toFile());
				}
				//System.out.println(file);
			}
		}

		// Prints the total number of
		// matches to standard out.
		@SuppressWarnings("unused")
		void done() {
			System.out.println("Matched: " + numMatches);
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
		public FileVisitResult preVisitDirectory(Path dir,
				BasicFileAttributes attrs) {
			find(dir);
			return CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) {
			System.err.println(exc);
			return CONTINUE;
		}
		
		public List<File> getModHomePaths() {
			return this.modHomePaths;
		}
		
		@SuppressWarnings("unused")
		public int size() {
			return this.numMatches;
		}
	}

	/**
	 * The file tree.
	 */
	private JTree tree;
	private Boolean installTree;

	/**
	 * Creates the file tree panel.
	 */
	public FileTreePanel(Boolean installTree) {
		this.installTree = installTree;
		
		this.setLayout(new BorderLayout());

		try {
			roots = getValidMods();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// File[] roots = new File[12];
		FileTreeNode rootTreeNode = new FileTreeNode(roots.toArray(new File[roots.size()]));

		this.tree = new JFileTree(rootTreeNode);
		
		tree.setDragEnabled(true);
		add(tree, BorderLayout.NORTH);
		tree.setRootVisible(false);
		this.tree.setCellRenderer(new FileTreeCellRenderer(installTree));
		
		
	}

	private List<File> getValidMods() throws IOException {

		Path basePath = Config.getModPath();
		Finder find = new Finder("*.xmod.export.xml", this.installTree);

		Files.walkFileTree(basePath, find);
		return find.getModHomePaths();

	}
	
	public JFileTree getTree() {
		return (JFileTree) this.tree;
	}
	
	public void resetMods() {
			try {
				roots = getValidMods();
				this.tree.setModel(new DefaultTreeModel(new FileTreeNode(roots.toArray(new File[roots.size()])), false));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
	}

	public List<File> getRoots() {
		return roots;
	}
	
	
	
	
	

}
