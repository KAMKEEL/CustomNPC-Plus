package noppes.npcs.client.gui.swing;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import noppes.npcs.client.gui.util.IJTextAreaListener;

import org.lwjgl.opengl.Display;

public class GuiJTextArea extends JDialog implements WindowListener{
	public IJTextAreaListener listener;
	private JTextArea area;
	public GuiJTextArea(String text){
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setSize(Display.getWidth() - 40, Display.getHeight() - 40);
		setLocation(Display.getX() + 20, Display.getY() + 20);

	    JScrollPane scroll = new JScrollPane(area = new JTextArea(text));
	    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		this.add(scroll);
		this.addWindowListener(this);
		setVisible(true);
	}
	
	
	public GuiJTextArea setListener(IJTextAreaListener listener){
		this.listener = listener;
		return this;
	}

	@Override
	public void windowOpened(WindowEvent e) {
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		if(listener != null)
			listener.saveText(area.getText());
	}

	@Override
	public void windowIconified(WindowEvent e) {
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		
	}
}
