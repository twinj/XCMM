package org.xcom.mod.gui;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
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
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.xml.bind.JAXBException;

import org.xcom.main.shared.Main;
import org.xcom.main.shared.entities.Config;
import org.xcom.main.shared.entities.XMod;
import org.xcom.mod.gui.JFileTree.FileTreeNode;

public class TableList extends JPanel implements TableModelListener {
	
	private static final long serialVersionUID = -7822486091093857438L;
	
	private final static boolean DEBUG = true;
	
	private JTable table;
	
	protected String[] columnToolTips = {
				"Mod's name", // "Mod Name" assumed obvious
				"Mod's version", "If checked, mod is installed and ready"
	};
	
	/**
	 * File system view.
	 */
	protected static FileSystemView fsv = FileSystemView.getFileSystemView();
	
	private TableList(FileTreeNode rootTreeNode, Boolean installTree, List<File> roots) {
		super(new GridLayout(1, 0));
		table = new JTable(new MyTableModel(roots)) {

			private static final long serialVersionUID = 460701972275507320L;

			// Implement table header tool tips.
			@Override
			protected JTableHeader createDefaultTableHeader() {
				return new JTableHeader(columnModel) {
					private static final long serialVersionUID = 1L;

					public String getToolTipText(MouseEvent e) {
						//String tip = null;
						java.awt.Point p = e.getPoint();
						int index = columnModel.getColumnIndexAtX(p.x);
						int realIndex = columnModel.getColumn(index).getModelIndex();
						return columnToolTips[realIndex];
					}
				};
			}
		};
		table.setAutoCreateRowSorter(true);
		table.getModel().addTableModelListener(this);
		
		table.setDefaultRenderer(ModFile.class, new FileCellRenderer());	
		table.setDefaultRenderer(String.class, new StringCellRenderer());		
		
		table.setFillsViewportHeight(true);
		
		// Create the scroll pane and add the table to it.
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		// Add the scroll pane to this panel.
		add(scrollPane);
		
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	public static TableList createTable() {
		List<File> roots = null;
		
		try {
			roots = getValidMods();
		} catch (IOException ex) {}
		FileTreeNode rootTreeNode = new FileTreeNode(roots.toArray(new File[roots.size()]));
		
		return new TableList(rootTreeNode, true, roots);
	}
	
	public JTable getTable() {
		return this.table;
	}
	
	public void tableChanged(TableModelEvent e) {
		// int row = e.getFirstRow();
		// int column = e.getColumn();
		// MyTableModel model = (MyTableModel) e.getSource();
		// String columnName = model.getColumnName(column);
		// Object data = model.getValueAt(row, column);
		
		// Do something with the data...
	}
	@SuppressWarnings("serial")
	class MyTableModel extends AbstractTableModel {
		protected String[] columnNames = {
					"Mod Name", "Version", "Active"
		};
		private List<XMod> mods;
		
		private Data data;
		
		class Data extends Vector<Data.Row> {
			public Data(int size) {
				super(size);
				this.setSize(size);
			}
			static final int columnCount = 3;
			
			class Row extends Vector<Object> {
				public Row(ModFile file, String version, Boolean active) {
					super(0);
					this.add(0, file);
					this.add(1, version);
					this.add(2, active);
					this.setSize(columnCount);
				}
			}
		}
		
		public MyTableModel(List<File> roots) {
			this.data = new Data(0);
			this.mods = new Vector<XMod>(0);
			
			for (File f : roots) {
				XMod mod = null;
				try {
					mod = (XMod) Main.getUnMarshaller().unmarshal(f);
				} catch (JAXBException ex) {}
				data.add(data.new Row(new ModFile(f, mod.getName()),
							(mod.getModVersion() == null ? "not set" : mod.getModVersion()), mod
										.getIsInstalled() == null ? false : mod.getIsInstalled()));
				mods.add(mod);
			}
			// fireTableStructureChanged();
			fireTableDataChanged();
		}
		
		public int getColumnCount() {
			return columnNames.length;
		}
		
		public int getRowCount() {
			return data.size();
		}
		
		public String getColumnName(int col) {
			return columnNames[col];
		}
		
		public Object getValueAt(int row, int col) {
			return data.get(row).get(col);
		}
		
		public Boolean getIsInstalled(int row) {
			return (Boolean) data.get(row).get(2);
		}
		
		public ModFile getFile(int row) {
			return (ModFile) data.get(row).get(0);
		}
		
		public Path getPath(int row) {
			return getFile(row).file.toPath();
		}
		
		public String getVersion(int row) {
			return (String) data.get(row).get(1);
		}
		
		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column would
		 * contain text ("true"/"false"), rather than a check box.
		 */
		@SuppressWarnings({
					"unchecked", "rawtypes"
		})
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}
		
		/*
		 * Don't need to implement this method unless your table's editable.
		 */
		public boolean isCellEditable(int row, int col) {
			// Note that the data/cell address is constant,
			// no matter where the cell appears onscreen.
			// if (col < 2) {
			// return false;
			// } else {
			// return true;
			// }
			return false;
		}
		
		/*
		 * Don't need to implement this method unless your table's data can change.
		 */
		public void setValueAt(Object value, int row, int col) {
			if (DEBUG) {
				System.out.println("Setting value at " + row + "," + col + " to " + value
							+ " (an instance of " + value.getClass() + ")");
			}
			
			data.get(row).remove(col);
			data.get(row).add(col, value);
			
			fireTableCellUpdated(row, col);
			
			if (DEBUG) {
				System.out.println("New value of data:");
				printDebugData();
			}
		}
		
		private void printDebugData() {
			int numRows = getRowCount();
			int numCols = getColumnCount();
			
			for (int i = 0; i < numRows; i++) {
				System.out.print("    row " + i + ":");
				for (int j = 0; j < numCols; j++) {
					System.out.print("  " + getValueAt(i, j));
				}
				System.out.println();
			}
			System.out.println("--------------------------");
		}
	}
	
	class ModFile {
		File file;
		String shortName;
		ModFile(File f, String name) {
			this.file = f;
			this.shortName = name;
		}
		@Override 
		public String toString() {
			return shortName;
		}
	}
	
	private static class Finder extends SimpleFileVisitor<Path> {
		
		private final PathMatcher matcher;
		private int numMatches = 0;
		private java.util.List<File> modHomePaths = new java.util.ArrayList<File>();
		private Boolean installTree = true;
		
		Finder(String pattern) {
			matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
		}
		
		void find(Path file) {
			Path name = file.getFileName();
			if (name != null && matcher.matches(name)) {
				numMatches++;
				if (installTree) {
					modHomePaths.add(file.toFile());
				} else {
					modHomePaths.add(file.getParent().toFile());
				}
				System.out.println(file);
			}
		}
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
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
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
	
	private static List<File> getValidMods() throws IOException {
		
		Path basePath = Config.getModPath();
		Finder find = new Finder("*.xmod.export.xml");
		
		Files.walkFileTree(basePath, find);
		return find.getModHomePaths();
	}
	
	private static class FileCellRenderer extends DefaultTableCellRenderer {
		
		private static final long serialVersionUID = 8794801734041065167L;
		
		private boolean installTree = true;
		
		/**
		 * Icon cache to speed the rendering.
		 */
		private Map<String, Icon> iconCache = new HashMap<String, Icon>();
		
		/**
		 * Root name cache to speed the rendering.
		 */
		private Map<File, String> rootNameCache = new HashMap<File, String>();
		
		public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
			
			ModFile mod = (ModFile) value;
			File file = mod.file;
			String filename = "";
			if (file != null) {
				filename = this.rootNameCache.get(file);
				if (filename == null) {
					if (!installTree) {
						filename = fsv.getSystemDisplayName(file);
					} else {
						filename = (file.getName().replace(".xmod.export.xml", ""));
					}
					this.rootNameCache.put(file, filename);
				}
			}
			JLabel result = (JLabel) super.getTableCellRendererComponent(table, filename,
						isSelected, hasFocus, row, column);
			if (file != null) {
				Icon icon = this.iconCache.get(filename);
				if (icon == null) {
					icon = fsv.getSystemIcon(file);
					this.iconCache.put(filename, icon);
				}
				result.setIcon(icon);
			}
			return result;
		}
	}
	
private static class StringCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -3357526347914093895L;

		public Component getTableCellRendererComponent(JTable table, Object value,
					boolean isSelected, boolean hasFocus, int row, int column) {
						
			JLabel result = (JLabel) super.getTableCellRendererComponent(table, value,
						isSelected, hasFocus, row, column);
			
			result.setHorizontalAlignment(CENTER);				
			return result;
		}
	}
	public List<XMod> getMods() {
		return ((MyTableModel) table.getModel()).mods;
	}
	
	public MyTableModel getModel() {
		return (MyTableModel) table.getModel();
	}
	
	public void setModel(MyTableModel model) {
		table.setModel(model);
	}
	
	public void resetMods() {
		try {
			this.setModel(new MyTableModel(getValidMods()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		TableColumn column = null;
		for (int i = 0; i < 3; i++) {
		    column = table.getColumnModel().getColumn(i);
		    if (i == 0) {
		        column.setPreferredWidth(200); //first column is bigger
		        column.setWidth(200); //first column is bigger
		        column.sizeWidthToFit();

		    } else {
		        column.setPreferredWidth(30);
		    }
		}
	}
	
}