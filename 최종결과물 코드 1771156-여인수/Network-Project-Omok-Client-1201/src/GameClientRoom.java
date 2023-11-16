import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;



import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JLabel;



public class GameClientRoom  extends JFrame{
	
	private static final long serialVersionUID = 1L;
	private static final int BUF_LEN = 128;
	private Socket socket;
	private InputStream is;
	private OutputStream os;
	private ObjectInputStream ois;
	private ObjectOutputStream oos;
	private String UserName;
	
	private JScrollPane scrollPane;
	
	public GameClientRoom RoomView;
	public GameClientOmokView Omok;
	private JTextField textField;
	private JButton btnEnterTest;
	private Vector<Room> RoomVec =  new Vector<Room>();
	private ArrayList<String> RoomidList= new ArrayList<String>();
	private JTextPane textArea;
	
	private JScrollPane scrollRoom;
	private JPanel panel;
	public GameClientRoom(String username, String ip_addr, String port_no) {
		
		//mainview = view;
		
		
		setTitle(username);
		setResizable(false);
		setBounds(100, 100, 585, 627);
		getContentPane().setLayout(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 278, 570);
		scrollPane.setBackground(new Color(255, 255, 255));
		getContentPane().add(scrollPane);
		
		textArea = new JTextPane();
		scrollPane.setViewportView(textArea);
		
		
		
		RoomView = this;
		
		//textField = new JTextField();
		//textField.setBounds(384, 10, 175, 46);
		//getContentPane().add(textField);
		//textField.setColumns(10);
		
		//btnEnterTest = new JButton("방 입장");
		//btnEnterTest.setBounds(291, 10, 87, 22);
		//getContentPane().add(btnEnterTest);
		
		
		
		JButton btnMake = new JButton("방 생성");
		btnMake.setBounds(302, 10, 257, 46);
		getContentPane().add(btnMake);
		
		scrollRoom = new JScrollPane();
		scrollRoom.setBounds(302, 66, 257, 514);
		getContentPane().add(scrollRoom);
		
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		scrollRoom.setViewportView(panel);
		scrollRoom.setHorizontalScrollBar(null);
		//insert
		
		
		btnMake.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				//MakeRoom();
				ChatMsg obcm = new ChatMsg(UserName, "200", "Make Room");
				SendObject(obcm);
			}
		});
		
		
		
		setVisible(true);
		
		AppendText("User " + username + " connecting " + ip_addr + " " + port_no);
		UserName = username;
		
		
		
		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));
			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());
			
			ChatMsg obcm = new ChatMsg(UserName, "100", "Hello");
			SendObject(obcm);
			
			ListenNetwork net = new ListenNetwork();
			net.start();
			
			
			}catch(NumberFormatException | IOException e) {
				e.printStackTrace();
				AppendText("connect error");
			}
		
		}
	
	
	
	class ListenNetwork extends Thread{
		
		
		
		public void run() {
			while (true) {
				try {

					Object obcm = null;
					String msg = null;
					ChatMsg cm;
					
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						msg = String.format("[%s]\n%s", cm.UserName, cm.data);
					} else
						continue;
					switch (cm.code) {
					case "150": //새로 로그인시 기존 방 정보 알림
						Room room2 = new Room(cm.Room_id);
					
						RoomVec.add(room2);
						
						MakeRoom(cm.Room_id);
						
						break;
					case "400": // chat message
						//AppendText(msg); 
						for(int i=0; i<RoomVec.size(); i++) {
							if(RoomVec.get(i).room_id.matches(cm.Room_id)) {
								RoomVec.get(i).omok.AppendText(msg);
							}
						}
						break;
					case "410":
						
						AppendText(msg); 
						
						
						break;
					case "200": // 게임방 생성 알림(생성자)
						
						Room room = new Room(cm.Room_id);
						//room.userlist = cm.userlist;
						
						RoomVec.add(room);
						
						AppendText("게임방 생성 #"+cm.Room_id);
						
						MakeRoom(cm.Room_id);
						
						break;
					
					case "250":
						//게임방 입장
						for(int i=0; i<RoomVec.size(); i++) {
							if(cm.Room_id.matches(RoomVec.get(i).room_id)) {
								RoomVec.get(i).omok.setBounds(1000,100,920, 668);
								RoomVec.get(i).omok.setVisible(true);
								
							}
						}
					
						AppendText("Room Enter");
						break;
				
					case "500": // Mouse Event 수신
						//DoMouseEvent(cm);
						for(int i=0; i<RoomVec.size(); i++) {
							if(cm.Room_id.matches(RoomVec.get(i).room_id)) {
								RoomVec.get(i).omok.DoMouseEvent(cm);
							}
							
						}
						
						
						break;
					case "600":
						//무르기 요청 수신
						for(int i=0; i<RoomVec.size(); i++) {
							if(cm.Room_id.matches(RoomVec.get(i).room_id)) {
								//RoomVec.get(i).omok.AppendText(cm.data);
								
								RoomVec.get(i).omok.ChanceOk();
								
							}
							
						}
						
						
						break;
					case "650":
						//무르기 승인 - 최근 돌 지우기
						for(int i=0; i<RoomVec.size(); i++) {
							if(cm.Room_id.matches(RoomVec.get(i).room_id)) {
								
								
								
								RoomVec.get(i).omok.StoneCancel();
								
							}
						}
						
						break;
					case "700": 
						//승리(항복)
						for(int i=0; i<RoomVec.size(); i++) {
							if(cm.Room_id.matches(RoomVec.get(i).room_id)) {
									RoomVec.get(i).omok.Win(cm);
							}
						}
						
						
						
						
						break;
					}
				} catch (IOException e) {
					AppendText("ois.readObject() error");
					try {
						ois.close();
						oos.close();
						socket.close();

						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝

			}
		}
	}
	
	public void AppendText(String msg) {
		msg = msg.trim();
		StyledDocument doc = textArea.getStyledDocument();
		SimpleAttributeSet left = new SimpleAttributeSet();
		StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
		StyleConstants.setForeground(left, Color.BLACK);	
	    doc.setParagraphAttributes(doc.getLength(), 1, left, false);
		try {
			doc.insertString(doc.getLength(),msg+"\n", left );
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	public void SendMessage(String msg) {
		try {

			ChatMsg obcm = new ChatMsg(UserName, "400", msg);
			oos.writeObject(obcm);
		} catch (IOException e) {
			
			AppendText("oos.writeObject() error");
			try {
				ois.close();
				oos.close();
				socket.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}
*/
	
	
	
	public void SendObject(Object ob) { // 서버로 메세지를 보내는 메소드
		try {
			oos.writeObject(ob);
		} catch (IOException e) {
			// textArea.append("메세지 송신 에러!!\n");
			AppendText("SendObject Error");
		}
	}
	
	public void MakeRoom(String Roomid) {
		
		GameClientRoomList RoomList = new GameClientRoomList(Roomid, RoomView);
		RoomList.setPreferredSize(new Dimension(250, 96));
		panel.add(RoomList);
		scrollRoom.revalidate();
		//ChatMsg obcm = new ChatMsg(UserName, "200", "Make Room");
		//SendObject(obcm);
	}
	
	public void EnterRoom(String room_id) {
		
	    ChatMsg obcm = new ChatMsg(UserName, "250", "Enter Room");
	    obcm.Room_id = room_id;
	    SendObject(obcm);
	}
	
	
	
	
	class Room {
		
		private String room_id;
		//private String userlist = null;
		
		private int[][] BoardCoord = new int[19][19];//0빈거 1흑 2백
		private ArrayList<int[]> StoneList = new ArrayList<int[]>(); //돌 좌표 순서 저장
		// [0]은 x, [1]은 y // 대기방 형식에 맞춰 수정 필요
		
		private int StoneListIndex = 0;
		private GameClientOmokView omok = new GameClientOmokView(UserName, RoomView);
		
		public Room( String room_id) {
			
			this.room_id = room_id;
			
			omok.setRoomid(room_id);
			//omok.setVisible(false);
			
		}
		
		
	}
}
