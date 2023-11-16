import javax.swing.JPanel;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JLabel;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import java.awt.Color;

public class GameClientRoomList extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JLabel lblRoomId;
	public GameClientRoom mainview;
	
	
	public GameClientRoomList(String roomid,GameClientRoom view) {
		mainview = view;
		//setBorder(new LineBorder(new Color(0, 0, 0)));
		//FlowLayout flowLayout = (FlowLayout) getLayout();
		
		lblRoomId = new JLabel(roomid);
		lblRoomId.setHorizontalAlignment(SwingConstants.CENTER);
		lblRoomId.setBounds(12, 32, 82, 37);
		add(lblRoomId);
		
		JButton btnEnter = new JButton("Enter");
		btnEnter.setBounds(128, 28, 122, 44);
		add(btnEnter);
		btnEnter.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				mainview.EnterRoom(roomid);
			}
		});
		
		setLayout(null);
		setVisible(true);
		//setBounds(0, 0, 250, 96);
		setPreferredSize(new Dimension(250, 96));
	}

}
