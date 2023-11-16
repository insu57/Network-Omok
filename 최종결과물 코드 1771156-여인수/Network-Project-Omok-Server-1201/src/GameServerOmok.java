import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;




public class GameServerOmok extends JFrame{
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	JTextArea textArea;
	private JTextField txtPortNumber;

	private ServerSocket socket; // ��������
	private Socket client_socket; // accept() ���� ������ client ����
	private Vector UserVec = new Vector(); // ����� ����ڸ� ������ ����
	private Vector<Room> RoomVec = new Vector<Room>();
	private int RoomNum = 1;
	
	private static final int BUF_LEN = 128; // Windows ó�� BUF_LEN �� ����

	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GameServerOmok frame = new GameServerOmok();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public GameServerOmok() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 338, 440);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 300, 298);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		JLabel lblNewLabel = new JLabel("Port Number");
		lblNewLabel.setBounds(13, 318, 87, 26);
		contentPane.add(lblNewLabel);

		txtPortNumber = new JTextField();
		txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
		txtPortNumber.setText("30000");
		txtPortNumber.setBounds(112, 318, 199, 26);
		contentPane.add(txtPortNumber);
		txtPortNumber.setColumns(10);
		
		JButton btnServerStart = new JButton("Server Start");
		btnServerStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
				} catch (NumberFormatException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				AppendText("Omok Server Running..");
				btnServerStart.setText("Omok Server Running..");
				btnServerStart.setEnabled(false); // ������ ���̻� �����Ű�� �� �ϰ� ���´�
				txtPortNumber.setEnabled(false); // ���̻� ��Ʈ��ȣ ������ �ϰ� ���´�
				AcceptServer accept_server = new AcceptServer();
				accept_server.start();
			}
		});
		
		
		btnServerStart.setBounds(12, 356, 300, 35);
		contentPane.add(btnServerStart);
	}
	
	
	class AcceptServer extends Thread {
		@SuppressWarnings("unchecked")
		public void run() {
			while (true) { // ����� ������ ����ؼ� �ޱ� ���� while��
				try {
					AppendText("Waiting new clients ...");
					client_socket = socket.accept(); // accept�� �Ͼ�� �������� ���� �����
					AppendText("���ο� ������ from " + client_socket);
					// User �� �ϳ��� Thread ����
					UserService new_user = new UserService(client_socket);
					UserVec.add(new_user); // ���ο� ������ �迭�� �߰�
					new_user.start(); // ���� ��ü�� ������ ����
					AppendText("���� ������ �� " + UserVec.size());
				} catch (IOException e) {
					AppendText("accept() error");
					// System.exit(0);
				}
			}
		}
	}

	
	
	public void AppendText(String str) {
		// textArea.append("����ڷκ��� ���� �޼��� : " + str+"\n");
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	public void AppendObject(ChatMsg msg) {
		// textArea.append("����ڷκ��� ���� object : " + str+"\n");
		textArea.append("code = " + msg.code + "\n");
		textArea.append("id = " + msg.UserName + "\n");
		textArea.append("data = " + msg.data + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}
	
	
	class Room {
		
		
		private String room_id;
		private String userlist = null;
		private int[][] BoardCoord = new int[19][19];//0��� 1�� 2��
		private ArrayList<int[]> StoneList = new ArrayList<int[]>(); //�� ��ǥ ���� ����
		// [0]�� x, [1]�� y // ���� ���Ŀ� ���� ���� �ʿ�
		
		private int StoneListIndex = 0;
		public Room(String room_id) {
			
			this.room_id = room_id;
			
		}
		
		
	}
	
	
	class UserService extends Thread {
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;

		private ObjectInputStream ois;
		private ObjectOutputStream oos;

		private Socket client_socket;
		private Vector user_vc;
		public String UserName = "";
		
		
		public UserService(Socket client_socket) {
			// TODO Auto-generated constructor stub
			// �Ű������� �Ѿ�� �ڷ� ����
			this.client_socket = client_socket;
			this.user_vc = UserVec;
			
			try {

				oos = new ObjectOutputStream(client_socket.getOutputStream());
				oos.flush();
				ois = new ObjectInputStream(client_socket.getInputStream());

			} catch (Exception e) {
				AppendText("userService error");
			}
		}

		
		public void Login() {
			AppendText("���ο� ������ " + UserName + " ����.");
			WriteOne("Welcome to ���� VS\n");
			WriteOne(UserName + "�� ȯ���մϴ�.\n"); // ����� ����ڿ��� ���������� �˸�
			
			
			
			for(int i=0; i<RoomVec.size(); i++) {
				ChatMsg obcm =  new ChatMsg(UserName, "150", "Room Info");
				obcm.Room_id = RoomVec.get(i).room_id;
				WriteOneObject(obcm);
			}
			
			String msg = "[" + UserName + "]���� ���� �Ͽ����ϴ�.\n";
			WriteOthers(msg); // ���� user_vc�� ���� ������ user�� ���Ե��� �ʾҴ�.
		}

		public void Logout() {
			String msg = "[" + UserName + "]���� ���� �Ͽ����ϴ�.\n";
			UserVec.removeElement(this); // Logout�� ���� ��ü�� ���Ϳ��� �����
			WriteAll(msg); // ���� ������ �ٸ� User�鿡�� ����
			AppendText("����� " + "[" + UserName + "] ����. ���� ������ �� " + UserVec.size());
		}
		
		

		// ��� User�鿡�� ���. ������ UserService Thread�� WriteONe() �� ȣ���Ѵ�.
		public void WriteAll(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				user.WriteOne(str);
			
					
			}
		}
		// ��� User�鿡�� Object�� ���. ä�� message�� image object�� ���� �� �ִ�
		public void WriteAllObject(Object ob) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				user.WriteOneObject(ob);
			}
		}

		// ���� ������ User�鿡�� ���. ������ UserService Thread�� WriteONe() �� ȣ���Ѵ�.
		public void WriteOthers(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user != this)
					user.WriteOne(str);
			}
		}
		public void WriteOne(String msg) { //�������� ���ο��� ���� �뵵
			try {
				
				ChatMsg obcm = new ChatMsg("SERVER", "410", msg);
				oos.writeObject(obcm);
			} catch (IOException e) {
				AppendText("dos.writeObject() error");
				try {

					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout(); // �������� ���� ��ü�� ���Ϳ��� �����
			}
		}
		
		public void WriteOneObject(Object ob) {
			try {
			    oos.writeObject(ob);
			} 
			catch (IOException e) {
				AppendText("oos.writeObject(ob) error");		
				try {
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;				
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout();
			}
		}
		
		public void WritePrivate(String msg) {
			try {
				ChatMsg obcm = new ChatMsg("�ӼӸ�", "200", msg);
				oos.writeObject(obcm);
			} catch (IOException e) {
				AppendText("dos.writeObject() error");
				try {
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout(); // �������� ���� ��ü�� ���Ϳ��� �����
			}
		}
		
		
		public void WriteRoomObject(Object ob, String[] userlist) {
	
			
			for(int i = 0; i < user_vc.size(); i++) {
				UserService  user = (UserService)user_vc.elementAt(i);
				for(int j=0; j<userlist.length; j++) {
					if(userlist[j].matches(user.UserName)) {
						user.WriteOneObject(ob);
					}
				}
			}
			
		}
		
		
		
		
		public void Win(ChatMsg cm, String[] userlist) { //�¸�
			String msg = cm.UserName + "�� �¸��Դϴ�!!";
			System.out.println("[SERVER] "+msg);
			
			//ChatMsg obcm = new ChatMsg("SERVER","400",msg);
			ChatMsg obcm =  new ChatMsg(cm.UserName,"700", msg);
			obcm.Room_id = cm.Room_id;
			
			for(int i=0; i<RoomVec.size(); i++) {
				if(cm.Room_id.matches(RoomVec.get(i).room_id)) {
					RoomVec.get(i).StoneList.clear();
					RoomVec.get(i).StoneListIndex = 0;
					for(int j=0; j<RoomVec.get(i).BoardCoord.length; j++) {
						Arrays.fill(RoomVec.get(i).BoardCoord[j], 0);
					}
				}
			}
			
			WriteRoomObject(obcm, userlist);
			AppendText("[SERVER]"+obcm.data);
			
			
		}
		public void six(ChatMsg cm) {
			String msg = "6�� �Դϴ�!!!";
			ChatMsg obcm = new ChatMsg("SERVER", "400", msg);
			obcm.Room_id = cm.Room_id;
			WriteOneObject(obcm);
		}
		public void double3(ChatMsg cm) {
			String msg = "3.3 �Դϴ�!!!";
			ChatMsg obcm = new ChatMsg("SERVER", "400", msg);
			obcm.Room_id = cm.Room_id;
			WriteOneObject(obcm);
		}
		public void stoneInfo(int index) {
			
			
			for(int i=0; i<RoomVec.get(index).StoneList.size(); i++) {
				ChatMsg obcm = new ChatMsg("SERVER", "500" , "StoneInfo");
				obcm.Room_id =RoomVec.get(index).room_id;
				obcm.BoardX = RoomVec.get(index).StoneList.get(i)[0];
				obcm.BoardY = RoomVec.get(index).StoneList.get(i)[1];
				
				obcm.StoneIndex = i;
				int color = RoomVec.get(index).BoardCoord[obcm.BoardX][obcm.BoardY];
				System.out.println("x: "+obcm.BoardX+" y: "+obcm.BoardY);
				if(color == 1)
					obcm.StoneColor = "black";
				else if(color == 2)
					obcm.StoneColor = "white";
				
				WriteOneObject(obcm); 
			}
			
		}
		
		
		
		
		public void run() {
			while (true) { // ����� ������ ����ؼ� �ޱ� ���� while��
				try {
					
					Object obcm = null;
					String msg = null;
					ChatMsg cm = null;
					if (socket == null)
						break;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						AppendObject(cm);
					} else
						continue;
					if (cm.code.matches("100")) {
						UserName = cm.UserName;
						
						Login();
					} else if (cm.code.matches("200")) { //�� ����
						String roomid = RoomNum+"";
						Room room = new Room(roomid);
						RoomNum++;
						room.userlist = "";
						RoomVec.add(room);
						AppendText("�� ����- room_id: "+room.room_id);
						
						WriteOne("�� ���� �Ϸ�!");
						
						cm.UserName = "SERVER";
						cm.data = "�� ����";
						cm.Room_id = room.room_id;
						//cm.code = "210"; //���� �˸�
						//WriteOneObject(cm);
						WriteAllObject(cm);
						//ChatMsg cm2 = new ChatMsg("SERVER", "220", "Room Id");
						//cm2.Room_id = cm.Room_id;
						//WriteAllObject(cm2);
						
					} else if(cm.code.matches("250")) { //�����û
						
						for(int i=0; i<RoomVec.size(); i++) {
							if(cm.Room_id.matches(RoomVec.get(i).room_id)) {
								ChatMsg cm2 = new ChatMsg("SERVER", "250", "Room Enter");
								
								RoomVec.get(i).userlist = RoomVec.get(i).userlist  + UserName + " ";
								
								cm2.Room_id = cm.Room_id;
								cm2.userlist = RoomVec.get(i).userlist;
								System.out.println("!!!"+cm2.userlist);
								
								WriteOneObject(cm2);
								
								stoneInfo(i);
								
							}
						}
							
						
						
						
					} else if (cm.code.matches("400")) { //ä��
						msg = String.format("[%s] %s", cm.UserName, cm.data);
						
						AppendText(msg); // server ȭ�鿡 ���
						String[] args = msg.split(" "); // �ܾ���� �и��Ѵ�.
						
						if(args.length == 1){
							
						}
						else if (args[1].matches("/exit")) {
							Logout();
							break;
		
						} else { // �Ϲ� ä�� �޽���
							
							WriteAllObject(cm);
						}
					} else if(cm.code.matches("600")) { //������ ��û
						//�˾�â Ŭ��
						
						for(int i=0; i<RoomVec.size(); i++) {
							if(cm.Room_id.matches(RoomVec.get(i).room_id)) {
								String[] userlist = RoomVec.get(i).userlist.split(" ");
								
								if(userlist[0].matches(UserName)) {
									
									if(RoomVec.get(i).StoneListIndex%2 == 1) {
										for(int j=0; j<user_vc.size(); j++) {
											UserService user = (UserService) user_vc.elementAt(j);
											if(user.UserName.matches(userlist[1])) {
												//System.out.println("who "+userlist[1]);
												
												user.WriteOneObject(cm);
											}
										}
									}else {
										cm.code = "400";
										cm.data = "���� �ڽ��� ���Դϴ�.";
										WriteOneObject(cm);
									}
									
									
								
								}else if(userlist[1].matches(UserName)) {
									
									if(RoomVec.get(i).StoneListIndex%2 == 0) {
										for(int j=0; j<user_vc.size(); j++) {
											UserService user = (UserService) user_vc.elementAt(j);
											if(user.UserName.matches(userlist[0])) {
												//System.out.println("who "+userlist[0]);
												user.WriteOneObject(cm);
										
											}
										}
									}
									else {
										cm.code = "400";
										cm.data = "���� �ڽ��� ���Դϴ�.";
										WriteOneObject(cm);
									}
									
									
								}
							}
						}
						
					}else if(cm.code.matches("650")) {
						
						for(int i=0; i<RoomVec.size(); i++) {
							if(cm.Room_id.matches(RoomVec.get(i).room_id)) {
								String[] userlist = RoomVec.get(i).userlist.split(" ");
								
								if(cm.data.matches("YES")) {	
									int[] Coord = new int[2];
									Coord = RoomVec.get(i).StoneList.get(RoomVec.get(i).StoneListIndex-1);
									RoomVec.get(i).StoneList.remove(RoomVec.get(i).StoneListIndex-1);
									RoomVec.get(i).StoneListIndex--;
									RoomVec.get(i).BoardCoord[Coord[0]][Coord[1]] = 0;
									WriteRoomObject(cm, userlist);
								}
								
								
								
								else if(cm.data.matches("NO")) {
									if(userlist[0].matches(UserName)) {
										for(int j=0; j<user_vc.size(); j++) {
											UserService user = (UserService) user_vc.elementAt(j);
											if(user.UserName.matches(userlist[1])) {
												cm.UserName = "SERVER";
												cm.data = "��밡 �����߽��ϴ�.";
												cm.code = "400";
												user.WriteOneObject(cm);
										
											}
										}
										
									}else if(userlist[1].matches(UserName)) {
										for(int j=0; j<user_vc.size(); j++) {
											UserService user = (UserService) user_vc.elementAt(j);
											if(user.UserName.matches(userlist[0])) {
												cm.UserName = "SERVER";
												cm.data = "��밡 �����߽��ϴ�.";
												cm.code = "400";
												user.WriteOneObject(cm);
										
											}
										}
									}
									
								}
							}
								
								
						}
						
					} else if (cm.code.matches("900")) { // logout message ó��
						Logout();
						break;
					} else if (cm.code.matches("500")) { //���콺
						for(int i=0; i<RoomVec.size(); i++) {
							if(cm.Room_id.matches(RoomVec.get(i).room_id)){
								OmokRule(cm, RoomVec.get(i).userlist);
							}
						}
						
						
						
						//WriteAllObject(cm);
						//break;
					} else if(cm.code.matches("750")) {//�׺�ó��
						for(int i=0; i<RoomVec.size(); i++) {
							if(cm.Room_id.matches(RoomVec.get(i).room_id)) {
								String[] userlist = RoomVec.get(i).userlist.split(" ");
								if(userlist[0].matches(cm.UserName)) {
									cm.UserName = userlist[1];
									Win(cm, userlist);
								}else if(userlist[1].matches(cm.UserName)) {
									cm.UserName = userlist[0];
									Win(cm, userlist);
								}
								
							}
						}
					} 
				
					else { // 300, 500, ... ��Ÿ object�� ��� ����Ѵ�.
						WriteAllObject(cm);
					} 
				} catch (IOException e) {
					AppendText("ois.readObject() error");
					try {

						ois.close();
						oos.close();
						client_socket.close();
						Logout(); // �������� ���� ��ü�� ���Ϳ��� �����
						break;
					} catch (Exception ee) {
						break;
					} // catch�� ��
				} // �ٱ� catch����
			} // while
		} // run
		
		public void OmokRule(ChatMsg cm, String Userlist) {
			String[] userlist = Userlist.split(" ");
			
			ChatMsg obcm;
			
			int RoomIndex = 0;
			for(int i=0; i<RoomVec.size(); i++) {
				if(cm.Room_id.matches(RoomVec.get(i).room_id)){
					RoomIndex = i;
				}
				
			}
			
			
			int[] coord = new int[2];
			coord[0] = cm.BoardX;
			coord[1] = cm.BoardY;
			int stoneColor = 0;
			
			
			if(userlist[0].matches(cm.UserName)) {
				if(RoomVec.get(RoomIndex).StoneListIndex%2 != 0) {
					cm.UserName = "SERVER";
					cm.code = "400";
					cm.data = "��� �� �Դϴ�.";
					WriteOneObject(cm);
					AppendText("[SERVER]"+cm.data);
					return;
				}
				cm.StoneColor = "black";
				stoneColor = 1;
				
			}else if(userlist[1].matches(cm.UserName)) {
				
				if(RoomVec.get(RoomIndex).StoneListIndex%2 != 1) {
					//obcm = new ChatMsg("Server", cm.UserName,"��� �� �Դϴ�.");
					cm.UserName = "SERVER";
					cm.code = "400";
					cm.data = "��� �� �Դϴ�.";
					WriteOneObject(cm);
					AppendText("[SERVER]"+cm.data);
					return;
				}
				
				cm.StoneColor = "white";
				stoneColor = 2;
				
			}else {
				//obcm = new ChatMsg("Server", cm.UserName,"�÷��̾ �ƴմϴ�.");
				cm.UserName = "SERVER";
				cm.code = "400";
				cm.data = "�÷��̾ �ƴմϴ�.";
				WriteOneObject(cm);
				return;
				
			}
			
			//if(RoomVec.get(RoomIndex).StoneListIndex%2 == 0)
			//	cm.StoneColor = "black";
			//else
			//	cm.StoneColor = "white";
			
			if(RoomVec.get(RoomIndex).StoneListIndex != 0) {//ù ��(��)�� �Ѿ
				//��ġ �ߺ� �˻�
				for(int i=0; i<RoomVec.get(RoomIndex).StoneList.size(); i++) {
					if(RoomVec.get(RoomIndex).StoneList.get(i)[0] == cm.BoardX && 
							RoomVec.get(RoomIndex).StoneList.get(i)[1] == cm.BoardY) {
						
						//WriteAllObject(cm);
						//WriteAll("�ߺ���ġ�Դϴ�"+coord[0]+","+coord[1]);
						String msg = "�ߺ���ġ�Դϴ�"+cm.BoardX+","+cm.BoardY;
						//obcm = new ChatMsg("Server", "400", msg);
						cm.UserName = "SERVER";
						cm.code = "400";
						cm.data = msg;
						WriteOneObject(cm);
						AppendText("[SERVER]"+cm.data);
						return;
					}
				}
			}
			
			
			
			
			//���� �� ó��
			//�¸�(����)
			//BoardCoord[][] - coord[0]coord[1]
			int[][] boardCoord = RoomVec.get(RoomIndex).BoardCoord; //stoneColor 0-��� 1-�浹 2-�鵹
			
			
			
			//33�˻�
			//���� - ���ʺ���
			int ThreeCount = 0;
			if(cm.BoardX>=5 && cm.BoardX<=16) {
				if( (boardCoord[cm.BoardX-5][cm.BoardY] == 0 && boardCoord[cm.BoardX-4][cm.BoardY] == 0) && 
						(boardCoord[cm.BoardX+2][cm.BoardY] == 0 && boardCoord[cm.BoardX+1][cm.BoardY] == 0)) {
					
					if(boardCoord[cm.BoardX-3][cm.BoardY] == stoneColor && boardCoord[cm.BoardX-2][cm.BoardY] == stoneColor ) {
						//33
						ThreeCount++;
					}
					else if(boardCoord[cm.BoardX-3][cm.BoardY] == stoneColor && boardCoord[cm.BoardX-1][cm.BoardY] == stoneColor) {
						ThreeCount++;
					}
				}
			}
				
			if(cm.BoardX>=4 && cm.BoardX<=16) {
				if((boardCoord[cm.BoardX-4][cm.BoardY] == 0 && boardCoord[cm.BoardX-3][cm.BoardY] == 0) &&
						(boardCoord[cm.BoardX+2][cm.BoardY] == 0 && boardCoord[cm.BoardX+1][cm.BoardY] == 0)) {
					if(boardCoord[cm.BoardX-2][cm.BoardY] == stoneColor && boardCoord[cm.BoardX-1][cm.BoardY] == stoneColor ) {
						ThreeCount++;
					}
				}
			}
			
			if(cm.BoardX>=3 && cm.BoardX<=14) {
				if((boardCoord[cm.BoardX-3][cm.BoardY] == 0 && boardCoord[cm.BoardX-2][cm.BoardY] == 0) &&
						(boardCoord[cm.BoardX+3][cm.BoardY] == 0 && boardCoord[cm.BoardX+4][cm.BoardY] == 0)) {
					if(boardCoord[cm.BoardX-1][cm.BoardY] == stoneColor && boardCoord[cm.BoardX+2][cm.BoardY] == stoneColor ) {
						ThreeCount++;
					}
				}
			}
			
			if(cm.BoardX>=4 && cm.BoardX<=15) {
				if((boardCoord[cm.BoardX-4][cm.BoardY] == 0 && boardCoord[cm.BoardX-3][cm.BoardY] == 0) &&
						(boardCoord[cm.BoardX+3][cm.BoardY] == 0 && boardCoord[cm.BoardX+2][cm.BoardY] == 0)) {
					if(boardCoord[cm.BoardX-2][cm.BoardY] == stoneColor && boardCoord[cm.BoardX+1][cm.BoardY] == stoneColor ) {
						ThreeCount++;
					}
				}
			}
			
			if(cm.BoardX>=3 && cm.BoardX<=15) {
				if((boardCoord[cm.BoardX-3][cm.BoardY] == 0 && boardCoord[cm.BoardX-2][cm.BoardY] == 0) &&
						(boardCoord[cm.BoardX+3][cm.BoardY] == 0 && boardCoord[cm.BoardX+2][cm.BoardY] == 0)) {
					if(boardCoord[cm.BoardX-1][cm.BoardY] == stoneColor && boardCoord[cm.BoardX+1][cm.BoardY] == stoneColor ) {
						ThreeCount++;
					}
				}
			}
			
			if(cm.BoardX>=3 && cm.BoardX<=14) {
				if((boardCoord[cm.BoardX-3][cm.BoardY] == 0 && boardCoord[cm.BoardX-2][cm.BoardY] == 0) &&
						(boardCoord[cm.BoardX+3][cm.BoardY] == 0 && boardCoord[cm.BoardX+4][cm.BoardY] == 0)) {
					if(boardCoord[cm.BoardX-1][cm.BoardY] == stoneColor && boardCoord[cm.BoardX+2][cm.BoardY] == stoneColor ) {
						ThreeCount++;
					}
				}
			}
			
			if(cm.BoardX>=2 && cm.BoardX<=14) {
				if((boardCoord[cm.BoardX-2][cm.BoardY] == 0 && boardCoord[cm.BoardX-1][cm.BoardY] == 0) &&
						(boardCoord[cm.BoardX+3][cm.BoardY] == 0 && boardCoord[cm.BoardX+4][cm.BoardY] == 0)) {
					if(boardCoord[cm.BoardX+1][cm.BoardY] == stoneColor && boardCoord[cm.BoardX+2][cm.BoardY] == stoneColor ) {
						ThreeCount++;
					}
				}
			}
			
			if(cm.BoardX>=2 && cm.BoardX<=13) {
				if((boardCoord[cm.BoardX-2][cm.BoardY] == 0 && boardCoord[cm.BoardX-1][cm.BoardY] == 0) &&
						(boardCoord[cm.BoardX+5][cm.BoardY] == 0 && boardCoord[cm.BoardX+4][cm.BoardY] == 0)) {
					if(boardCoord[cm.BoardX+1][cm.BoardY] == stoneColor && boardCoord[cm.BoardX+3][cm.BoardY] == stoneColor ) {
						ThreeCount++;
					}
					else if(boardCoord[cm.BoardX+2][cm.BoardY] == stoneColor && boardCoord[cm.BoardX+3][cm.BoardY] == stoneColor) {
						ThreeCount++;
					}
				}
			}
			
			System.out.println("33"+ThreeCount);
			
			//���� 	
			
			if(cm.BoardY>=5 && cm.BoardY<=16) {
				if( (boardCoord[cm.BoardX][cm.BoardY-5] == 0 && boardCoord[cm.BoardX][cm.BoardY-4] == 0) && 
						(boardCoord[cm.BoardX][cm.BoardY+2] == 0 && boardCoord[cm.BoardX][cm.BoardY+1] == 0)) {
					
					if(boardCoord[cm.BoardX][cm.BoardY-3] == stoneColor && boardCoord[cm.BoardX][cm.BoardY-2] == stoneColor ) {
						//33
						ThreeCount++;
					}
					else if(boardCoord[cm.BoardX][cm.BoardY-3] == stoneColor && boardCoord[cm.BoardX][cm.BoardY-1] == stoneColor) {
						ThreeCount++;
					}
				}
			}
				
			if(cm.BoardY>=4 && cm.BoardY<=16) {
				if((boardCoord[cm.BoardX][cm.BoardY-4] == 0 && boardCoord[cm.BoardX][cm.BoardY-3] == 0) &&
						(boardCoord[cm.BoardX][cm.BoardY+2] == 0 && boardCoord[cm.BoardX][cm.BoardY+1] == 0)) {
					if(boardCoord[cm.BoardX][cm.BoardY-2] == stoneColor && boardCoord[cm.BoardX][cm.BoardY-1] == stoneColor ) {
						ThreeCount++;
					}
				}
			}
			
			if(cm.BoardY>=3 && cm.BoardY<=14) {
				if((boardCoord[cm.BoardX][cm.BoardY-3] == 0 && boardCoord[cm.BoardX][cm.BoardY-2] == 0) &&
						(boardCoord[cm.BoardX][cm.BoardY+3] == 0 && boardCoord[cm.BoardX][cm.BoardY+4] == 0)) {
					if(boardCoord[cm.BoardX][cm.BoardY-1] == stoneColor && boardCoord[cm.BoardX][cm.BoardY+2] == stoneColor ) {
						ThreeCount++;
					}
				}
			}
			
			if(cm.BoardY>=4 && cm.BoardY<=15) {
				if((boardCoord[cm.BoardX][cm.BoardY-4] == 0 && boardCoord[cm.BoardX][cm.BoardY-3] == 0) &&
						(boardCoord[cm.BoardX][cm.BoardY+3] == 0 && boardCoord[cm.BoardX][cm.BoardY+2] == 0)) {
					if(boardCoord[cm.BoardX][cm.BoardY-2] == stoneColor && boardCoord[cm.BoardX][cm.BoardY+1] == stoneColor ) {
						ThreeCount++;
					}
				}
			}
			
			if(cm.BoardY>=3 && cm.BoardY<=15) {
				if((boardCoord[cm.BoardX][cm.BoardY-3] == 0 && boardCoord[cm.BoardX][cm.BoardY-2] == 0) &&
						(boardCoord[cm.BoardX][cm.BoardY+3] == 0 && boardCoord[cm.BoardX][cm.BoardY+2] == 0)) {
					if(boardCoord[cm.BoardX][cm.BoardY-1] == stoneColor && boardCoord[cm.BoardX][cm.BoardY+1] == stoneColor ) {
						ThreeCount++;
					}
				}
			}
			
			if(cm.BoardY>=3 && cm.BoardY<=14) {
				if((boardCoord[cm.BoardX][cm.BoardY-3] == 0 && boardCoord[cm.BoardX][cm.BoardY-2] == 0) &&
						(boardCoord[cm.BoardX][cm.BoardY+3] == 0 && boardCoord[cm.BoardX][cm.BoardY+4] == 0)) {
					if(boardCoord[cm.BoardX][cm.BoardY-1] == stoneColor && boardCoord[cm.BoardX][cm.BoardY+2] == stoneColor ) {
						ThreeCount++;
					}
				}
			}
			
			if(cm.BoardY>=2 && cm.BoardY<=14) {
				if((boardCoord[cm.BoardX][cm.BoardY-2] == 0 && boardCoord[cm.BoardX][cm.BoardY-1] == 0) &&
						(boardCoord[cm.BoardX][cm.BoardY+3] == 0 && boardCoord[cm.BoardX][cm.BoardY+4] == 0)) {
					if(boardCoord[cm.BoardX][cm.BoardY+1] == stoneColor && boardCoord[cm.BoardX][cm.BoardY+2] == stoneColor ) {
						ThreeCount++;
					}
				}
			}
			
			if(cm.BoardY>=2 && cm.BoardY<=13) {
				if((boardCoord[cm.BoardX][cm.BoardY-2] == 0 && boardCoord[cm.BoardX][cm.BoardY-1] == 0) &&
						(boardCoord[cm.BoardX][cm.BoardY+5] == 0 && boardCoord[cm.BoardX][cm.BoardY+4] == 0)) {
					if(boardCoord[cm.BoardX][cm.BoardY+1] == stoneColor && boardCoord[cm.BoardX][cm.BoardY+3] == stoneColor ) {
						ThreeCount++;
					}
					else if(boardCoord[cm.BoardX][cm.BoardY+2] == stoneColor && boardCoord[cm.BoardX][cm.BoardY+3] == stoneColor) {
						ThreeCount++;
					}
				}
			}
			
			//33�밢�� �»�\
			
			
			//33�밢�� ���/ 
			
			
			
			if(ThreeCount>=2) {
				//double3(cm);
				//return;
			}
			
			
			//5��(�¸�),6�� üũ
			//������ x
			if(cm.BoardX>=4) {
				if(boardCoord[cm.BoardX -4][cm.BoardY] == stoneColor &&
						boardCoord[cm.BoardX -3][cm.BoardY] == stoneColor &&
						boardCoord[cm.BoardX -2][cm.BoardY] == stoneColor &&
						boardCoord[cm.BoardX -1][cm.BoardY] == stoneColor){
					
					if(cm.BoardX>=5) {//6�� üũ
						if(boardCoord[cm.BoardX -5][cm.BoardY]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardX<=17) {
						if(boardCoord[cm.BoardX +1][cm.BoardY] == stoneColor) {
							six(cm);
							return;
						}
					}
					
					
					
					Win(cm, userlist);
					return;
				}
			}
			if(cm.BoardX>=3 && cm.BoardX<=17) {
				if(boardCoord[cm.BoardX -3][cm.BoardY] == stoneColor &&
						boardCoord[cm.BoardX -2][cm.BoardY] == stoneColor &&
						boardCoord[cm.BoardX -1][cm.BoardY] == stoneColor &&
						boardCoord[cm.BoardX +1][cm.BoardY] == stoneColor){
					
					
					if(cm.BoardX>=4) {//6�� üũ
						if(boardCoord[cm.BoardX -4][cm.BoardY]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardX<=16) {
						if(boardCoord[cm.BoardX +2][cm.BoardY] == stoneColor) {
							six(cm);
							return;
						}
					}
					
					
					
					Win(cm, userlist);
					return;
				}
			}
			if(cm.BoardX>=2 && cm.BoardX<=16) {
				if(boardCoord[cm.BoardX -2][cm.BoardY] == stoneColor &&
						boardCoord[cm.BoardX -1][cm.BoardY] == stoneColor &&
						boardCoord[cm.BoardX +1][cm.BoardY] == stoneColor &&
						boardCoord[cm.BoardX +2][cm.BoardY] == stoneColor){
					
					if(cm.BoardX>=3) {//6�� üũ
						if(boardCoord[cm.BoardX -3][cm.BoardY]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardX<=15) {
						if(boardCoord[cm.BoardX +3][cm.BoardY] == stoneColor) {
							six(cm);
							return;
						}
					}
					
					Win(cm, userlist);
					return;
				}
			}
			if(cm.BoardX>=1 && cm.BoardX<=15) {
				if(boardCoord[cm.BoardX -1][cm.BoardY] == stoneColor &&
						boardCoord[cm.BoardX +1][cm.BoardY] == stoneColor &&
						boardCoord[cm.BoardX +2][cm.BoardY] == stoneColor &&
						boardCoord[cm.BoardX +3][cm.BoardY] == stoneColor){
					
					if(cm.BoardX>=2) {//6�� üũ
						if(boardCoord[cm.BoardX -2][cm.BoardY]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardX<=14) {
						if(boardCoord[cm.BoardX +4][cm.BoardY] == stoneColor) {
							six(cm);
							return;
						}
					}
					
					
					Win(cm, userlist);
					return;
				}
			}
			if(cm.BoardX<=14) {
				if(boardCoord[cm.BoardX +1][cm.BoardY] == stoneColor &&
						boardCoord[cm.BoardX +2][cm.BoardY] == stoneColor &&
						boardCoord[cm.BoardX +3][cm.BoardY] == stoneColor &&
						boardCoord[cm.BoardX +4][cm.BoardY] == stoneColor){
					
					
					if(cm.BoardX>=1) {//6�� üũ
						if(boardCoord[cm.BoardX -1][cm.BoardY]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardX<=13) {
						if(boardCoord[cm.BoardX +5][cm.BoardY] == stoneColor) {
							six(cm);
							return;
						}
					}
					
					
					
					Win(cm, userlist);
					return;
				}
			}
			//������ y
			if(cm.BoardY>=4) {
				if(boardCoord[cm.BoardX][cm.BoardY -4] == stoneColor &&
						boardCoord[cm.BoardX][cm.BoardY -3] == stoneColor &&
						boardCoord[cm.BoardX][cm.BoardY -2] == stoneColor &&
						boardCoord[cm.BoardX][cm.BoardY -1] == stoneColor){
					if(cm.BoardY>=5) {//6�� üũ
						if(boardCoord[cm.BoardX][cm.BoardY -5]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardY<=17) {
						if(boardCoord[cm.BoardX][cm.BoardY +1] == stoneColor) {
							six(cm);
							return;
						}
					}
				
					Win(cm, userlist);
					return;
				}
			}
			if(cm.BoardY>=3 && cm.BoardY<=17) {
				if(boardCoord[cm.BoardX][cm.BoardY -3] == stoneColor &&
						boardCoord[cm.BoardX][cm.BoardY -2] == stoneColor &&
						boardCoord[cm.BoardX][cm.BoardY -1] == stoneColor &&
						boardCoord[cm.BoardX][cm.BoardY +1] == stoneColor){
					

					if(cm.BoardY>=4) {//6�� üũ
						if(boardCoord[cm.BoardX][cm.BoardY -4]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardY<=16) {
						if(boardCoord[cm.BoardX][cm.BoardY +2] == stoneColor) {
							six(cm);
							return;
						}
					}
					
					
					Win(cm, userlist);
					return;
				}
			}
			if(cm.BoardY>=2 && cm.BoardY<=16) {
				if(boardCoord[cm.BoardX][cm.BoardY -2] == stoneColor &&
						boardCoord[cm.BoardX][cm.BoardY -1] == stoneColor &&
						boardCoord[cm.BoardX][cm.BoardY +1] == stoneColor &&
						boardCoord[cm.BoardX][cm.BoardY +2] == stoneColor){
					
					if(cm.BoardY>=3) {//6�� üũ
						if(boardCoord[cm.BoardX][cm.BoardY -3]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardY<=15) {
						if(boardCoord[cm.BoardX][cm.BoardY +3] == stoneColor) {
							six(cm);
							return;
						}
					}
					Win(cm, userlist);
					return;
				}
			}	 
			if(cm.BoardY>=1 && cm.BoardY<=15) {
				if(boardCoord[cm.BoardX][cm.BoardY -1] == stoneColor &&
						boardCoord[cm.BoardX][cm.BoardY +1] == stoneColor &&
						boardCoord[cm.BoardX][cm.BoardY +2] == stoneColor &&
						boardCoord[cm.BoardX][cm.BoardY +3] == stoneColor){
					if(cm.BoardY>=2) {//6�� üũ
						if(boardCoord[cm.BoardX][cm.BoardY -2]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardY<=14) {
						if(boardCoord[cm.BoardX][cm.BoardY +4] == stoneColor) {
							six(cm);
							return;
						}
					}
				
					Win(cm, userlist);
					return;
				}
			}
			if(cm.BoardY<=14) {
				if(boardCoord[cm.BoardX][cm.BoardY +1] == stoneColor &&
						boardCoord[cm.BoardX][cm.BoardY +2] == stoneColor &&
						boardCoord[cm.BoardX][cm.BoardY +3] == stoneColor &&
						boardCoord[cm.BoardX][cm.BoardY +4] == stoneColor){
					
					if(cm.BoardY>=1) {//6�� üũ
						if(boardCoord[cm.BoardX][cm.BoardY -1]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardY<=13) {
						if(boardCoord[cm.BoardX][cm.BoardY +5] == stoneColor) {
							six(cm);
							return;
						}
					}
					
					Win(cm, userlist);
					return;
				}
			}
			
			//�»� �밢�� '\'
			if(cm.BoardX>=4 && cm.BoardY>=4){
				if(boardCoord[cm.BoardX -4][cm.BoardY -4] == stoneColor &&
						boardCoord[cm.BoardX -3][cm.BoardY -3] == stoneColor &&
						boardCoord[cm.BoardX -2][cm.BoardY -2] == stoneColor &&
						boardCoord[cm.BoardX -1][cm.BoardY -1] == stoneColor){
					
					if(cm.BoardX>=5 && cm.BoardY>=5) {//6�� üũ
						if(boardCoord[cm.BoardX -5][cm.BoardY -5]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardX<=17 && cm.BoardY<=17) {
						if(boardCoord[cm.BoardX +1][cm.BoardY +1] == stoneColor) {
							six(cm);
							return;
						}
					}
				
					Win(cm, userlist);
					return;
				}
			}
			if(cm.BoardX>=3 && cm.BoardX<=17 && cm.BoardY>=3 && cm.BoardY<=17) {
				if(boardCoord[cm.BoardX -3][cm.BoardY -3] == stoneColor &&
						boardCoord[cm.BoardX -2][cm.BoardY -2] == stoneColor &&
						boardCoord[cm.BoardX -1][cm.BoardY -1] == stoneColor &&
						boardCoord[cm.BoardX +1][cm.BoardY +1] == stoneColor){
					if(cm.BoardX>=4 && cm.BoardY>=4) {//6�� üũ
						if(boardCoord[cm.BoardX -4][cm.BoardY -4]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardX<=16 && cm.BoardY<=16) {
						if(boardCoord[cm.BoardX +2][cm.BoardY +2] == stoneColor) {
							six(cm);
							return;
						}
					}
					
					Win(cm, userlist);
					return;
				}
			}
			if(cm.BoardX>=2 && cm.BoardX<=16 && cm.BoardY>=2 && cm.BoardY<=16) {
				if(boardCoord[cm.BoardX -2][cm.BoardY -2] == stoneColor &&
						boardCoord[cm.BoardX -1][cm.BoardY -1] == stoneColor &&
						boardCoord[cm.BoardX  +1][cm.BoardY +1] == stoneColor &&
						boardCoord[cm.BoardX +2][cm.BoardY +2] == stoneColor){
					if(cm.BoardX>=3 && cm.BoardY>=3) {//6�� üũ
						if(boardCoord[cm.BoardX -3][cm.BoardY -3]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardX<=15 && cm.BoardY<=15) {
						if(boardCoord[cm.BoardX +3][cm.BoardY +3] == stoneColor) {
							six(cm);
							return;
						}
					}
				
					Win(cm, userlist);
					return;
				}
			}
			if(cm.BoardX>=1 && cm.BoardX<=15 && cm.BoardY>=1 && cm.BoardY<=15) {
				if(boardCoord[cm.BoardX -1][cm.BoardY -1] == stoneColor &&
						boardCoord[cm.BoardX +1][cm.BoardY +1] == stoneColor &&
						boardCoord[cm.BoardX +2][cm.BoardY +2] == stoneColor &&
						boardCoord[cm.BoardX +3][cm.BoardY +3] == stoneColor){
					if(cm.BoardX>=2 && cm.BoardY>=2) {//6�� üũ
						if(boardCoord[cm.BoardX -2][cm.BoardY -2]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardX<=14 && cm.BoardY<=14) {
						if(boardCoord[cm.BoardX +4][cm.BoardY +4] == stoneColor) {
							six(cm);
							return;
						}
					}
					
					Win(cm, userlist);
					return;
					
				}
			}
			if(cm.BoardX<=14 && cm.BoardY<=14) {
				if(boardCoord[cm.BoardX +1][cm.BoardY +1] == stoneColor &&
						boardCoord[cm.BoardX +2][cm.BoardY +2] == stoneColor &&
						boardCoord[cm.BoardX +3][cm.BoardY +3] == stoneColor &&
						boardCoord[cm.BoardX +4][cm.BoardY +4] == stoneColor){
					if(cm.BoardX>=1 && cm.BoardY>=1) {//6�� üũ
						if(boardCoord[cm.BoardX -1][cm.BoardY -1]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardX<=13 && cm.BoardY<=13) {
						if(boardCoord[cm.BoardX +5][cm.BoardY +5] == stoneColor) {
							six(cm);
							return;
						}
					}
					
					Win(cm, userlist);
					return;
				}
			}
			//��� �밢�� '/'
			if(cm.BoardX>=4 && cm.BoardY<=14) {
				if(boardCoord[cm.BoardX -1][cm.BoardY +1] == stoneColor &&
						boardCoord[cm.BoardX -2][cm.BoardY +2] == stoneColor &&
						boardCoord[cm.BoardX -3][cm.BoardY +3] == stoneColor &&
						boardCoord[cm.BoardX -4][cm.BoardY +4] == stoneColor){
					
					if(cm.BoardX>=5 && cm.BoardY<=13) {//6�� üũ
						if(boardCoord[cm.BoardX -5][cm.BoardY +5]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardX<=17 && cm.BoardY>=1) {
						if(boardCoord[cm.BoardX +1][cm.BoardY -1] == stoneColor) {
							six(cm);
							return;
						}
					}
					
					Win(cm, userlist);
					return;
				}
			}
			if(cm.BoardX>=3 && cm.BoardX<=17 && cm.BoardY>=1 && cm.BoardY<=15) {
				if(boardCoord[cm.BoardX -1][cm.BoardY +1] == stoneColor &&
						boardCoord[cm.BoardX -2][cm.BoardY +2] == stoneColor &&
						boardCoord[cm.BoardX -3][cm.BoardY +3] == stoneColor &&
						boardCoord[cm.BoardX +1][cm.BoardY -1] == stoneColor){
					
					if(cm.BoardX>=4 && cm.BoardY<=14) {//6�� üũ
						if(boardCoord[cm.BoardX -4][cm.BoardY +4]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardX<=16 && cm.BoardY>=2) {
						if(boardCoord[cm.BoardX +2][cm.BoardY -2] == stoneColor) {
							six(cm);
							return;
						}
					}
					
					Win(cm, userlist);
					return;
				}
			}
			if(cm.BoardX>=2 && cm.BoardX<=16 && cm.BoardY>=2 && cm.BoardY<=16) {
				if(boardCoord[cm.BoardX -1][cm.BoardY +1] == stoneColor &&
						boardCoord[cm.BoardX -2][cm.BoardY +2] == stoneColor &&
						boardCoord[cm.BoardX +2][cm.BoardY -2] == stoneColor &&
						boardCoord[cm.BoardX +1][cm.BoardY -1] == stoneColor){
					if(cm.BoardX>=3 && cm.BoardY<=15) {//6�� üũ
						if(boardCoord[cm.BoardX -3][cm.BoardY +3]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardX<=15 && cm.BoardY>=3) {
						if(boardCoord[cm.BoardX +3][cm.BoardY -3] == stoneColor) {
							six(cm);
							return;
						}
					}
					
					Win(cm, userlist);
					return;
				}
			}
			if(cm.BoardX>=1 && cm.BoardX<=15 && cm.BoardY>=3 && cm.BoardY<=17) {
				if(boardCoord[cm.BoardX -1][cm.BoardY +1] == stoneColor &&
						boardCoord[cm.BoardX +3][cm.BoardY -3] == stoneColor &&
						boardCoord[cm.BoardX +2][cm.BoardY -2] == stoneColor &&
						boardCoord[cm.BoardX +1][cm.BoardY -1] == stoneColor){
					
					if(cm.BoardX>=2 && cm.BoardY<=16) {//6�� üũ
						if(boardCoord[cm.BoardX -2][cm.BoardY +2]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardX<=14 && cm.BoardY>=4) {
						if(boardCoord[cm.BoardX +4][cm.BoardY -4] == stoneColor) {
							six(cm);
							return;
						}
					}
					
					Win(cm, userlist);
					return;
				}
			}
			if(cm.BoardX<=14 && cm.BoardY>=4) {
				if(boardCoord[cm.BoardX +4][cm.BoardY -4] == stoneColor &&
						boardCoord[cm.BoardX +3][cm.BoardY -3] == stoneColor &&
						boardCoord[cm.BoardX +2][cm.BoardY -2] == stoneColor &&
						boardCoord[cm.BoardX +1][cm.BoardY -1] == stoneColor){
					if(cm.BoardX>=1 && cm.BoardY<=17) {//6�� üũ
						if(boardCoord[cm.BoardX -1][cm.BoardY +1]  == stoneColor) {
							six(cm);
							return;
						}
					}
					if(cm.BoardX<=13 && cm.BoardY>=5) {
						if(boardCoord[cm.BoardX +5][cm.BoardY -5] == stoneColor) {
							six(cm);
							return;
						}
					}
					
					Win(cm, userlist);
					return;
				}
			}
			//���� ó�� ��
			
			WriteRoomObject(cm, userlist); //�ٵϾ� �α�
			
			
			RoomVec.get(RoomIndex).StoneList.add(RoomVec.get(RoomIndex).StoneListIndex,coord);
			RoomVec.get(RoomIndex).StoneListIndex++;
			RoomVec.get(RoomIndex).BoardCoord[cm.BoardX][cm.BoardY] = stoneColor; //�ٵϵ� ���, �� �ѱ��
			
		}
		
	}
}
