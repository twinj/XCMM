package org.xcom.mod.gui;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;


/**
 * This code uses a JList in two forms (layout orientation vertical & horizontal
 * wrap) to display a File[]. The renderer displays the file icon obtained from
 * FileSystemView.
 */
public class FileList extends JList<File> {

	ArrayListModel listModel;

	public ArrayListModel getListModel() {
		return listModel;
	}

	private static final long serialVersionUID = 1L;

	public FileList() {
		super();
		listModel = new ArrayListModel(this);
		this.setCellRenderer(new MyCellRenderer());
		this.setModel(listModel);
	}
}

class MyCellRenderer extends JLabel implements ListCellRenderer<Object> {

	private static final long serialVersionUID = 1L;
	final static ImageIcon longIcon = new ImageIcon(XCMGUI.class
			.getResource("/org/xcom/mod/gui/icons/page_white_text.png"));
	final static ImageIcon shortIcon = new ImageIcon(XCMGUI.class
			.getResource("/org/xcom/mod/gui/icons/page_white_text.png"));

	// This is the only method defined by ListCellRenderer.
	// We just reconfigure the JLabel each time we're called.

	public Component getListCellRendererComponent(JList<?> list, // the list
			Object value, // value to display
			int index, // cell index
			boolean isSelected, // is the cell selected
			boolean cellHasFocus) // does the cell have focus
	{
		File f = (File) value;
		String s = f.getName() + " @ " + f.getParent();
		setText(s);
		setIcon((s.length() > 10) ? longIcon : shortIcon);
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}
		setEnabled(list.isEnabled());
		setFont(list.getFont());
		setOpaque(true);
		return this;
	}
}

class ArrayListModel extends ArrayList<File> implements ListModel<File> {

	private static final long serialVersionUID = 1L;
	
	protected Object source;

	ArrayListModel(Object src) {
		source = src;
	}
	
	public File getElementAt(int index) {
		return get(index);
	}

	public int getSize() {
		return size();
	}

	ArrayList<javax.swing.event.ListDataListener> listeners = new ArrayList<javax.swing.event.ListDataListener>();

	public void removeListDataListener(javax.swing.event.ListDataListener l) {
		listeners.remove(l);
	}

	public void addListDataListener(javax.swing.event.ListDataListener l) {
		listeners.add(l);
	}

	void notifyListeners() {
		// no attempt at optimziation
		ListDataEvent le = new ListDataEvent(source,
				ListDataEvent.CONTENTS_CHANGED, 0, getSize());
		for (int i = 0; i < listeners.size(); i++) {
			((ListDataListener) listeners.get(i)).contentsChanged(le);
		}
	}

	// REMAINDER ARE OVERRIDES JUST TO CALL NOTIFYLISTENERS
	@Override
	public boolean add(File o) {
		boolean b = super.add(o);
		if (b)
			notifyListeners();
		return b;
	}

	public void add(int index, File element) {
		super.add(index, element);
		notifyListeners();
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean addAll(@SuppressWarnings("rawtypes") Collection o) {
		boolean b = super.addAll(o);
		if (b)
			notifyListeners();
		return b;
	}
	@Override
	public void clear() {
		super.clear();
		notifyListeners();
	}
	@Override
	public File remove(int i) {
		File o = super.remove(i);
		notifyListeners();
		return o;
	}
	@Override
	public boolean removeAll(@SuppressWarnings("rawtypes") Collection o) {
		boolean b = super.removeAll(o);
		if (b)
			notifyListeners();
		return b;
	}

	public boolean remove(File o) {
		boolean b = super.remove(o);
		if (b)
			notifyListeners();
		return b;
	}

	public File set(int index, File element) {
		File o = super.set(index, element);
		notifyListeners();
		return o;
	}
}