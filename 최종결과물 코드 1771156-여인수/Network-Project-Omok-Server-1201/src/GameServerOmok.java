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

	private ServerSocket socket; // 서버소켓
	private Socket client_socket; // accept() 에서 생성된 client 소켓
	private Vector UserVec = new Vector(); // 연결된 사용자를 저장할 벡터
	private Vector<Room> RoomVec = new Vector<Room>();
	private int RoomNum = 1;
	
	private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의

	
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
				btnServerStart.setEnabled(false); // 서버를 더이상 실행시키지 못 하게 막는다
				txtPortNumber.setEnabled(false); // 더이상 포트번호 수정못 하게 막는다
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
			while (true) { // 사용자 접속을 계속해서 받기 위해 while문
				try {
					AppendText("Waiting new clients ...");
					client_socket = socket.accept(); // accept가 일어나기 전까지는 무한 대기중
					AppendText("새로운 참가자 from " + client_socket);
					// User 당 하나씩 Thread 생성
					UserService new_user = new UserService(client_socket);
					UserVec.add(new_user); // 새로운 참가자 배열에 추가
					new_user.start(); // 만든 객체의 스레드 실행
					AppendText("현재 참가자 수 " + UserVec.size());
				} catch (IOException e) {
					AppendText("accept() error");
					// System.exit(0);
				}
			}
		}
	}

	
	
	public void AppendText(String str) {
		// textArea.append("사용자로부터 들어온 메세지 : " + str+"\n");
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	public void AppendObject(ChatMsg msg) {
		// textArea.append("사용자로부터 들어온 object : " + str+"\n");
		textArea.append("code = " + msg.code + "\n");
		textArea.append("id = " + msg.UserName + "\n");
		textArea.append("data = " + msg.data + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}
	
	
	class Room {
		
		
		private String room_id;
		private String userlist = null;
		private int[][] BoardCoord = new int[19][19];//0빈거 1흑 2백
		private ArrayList<int[]> StoneList = new ArrayList<int[]>(); //돌 좌표 순서 저장
		// [0]은 x, [1]은 y // 대기방 형식에 맞춰 수정 필요
		
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
			// 매개변수로 넘어온 자료 저장
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
			AppendText("새로운 참가자 " + UserName + " 입장.");
			WriteOne("Welcome to 오목 VS\n");
			WriteOne(UserName + "님 환영합니다.\n"); // 연결된 사용자에게 정상접속을 알림
			
			
			
			for(int i=0; i<RoomVec.size(); i++) {
				ChatMsg obcm =  new ChatMsg(UserName, "150", "Room Info");
				obcm.Room_id = RoomVec.get(i).room_id;
				WriteOneObject(obcm);
			}
			
			String msg = "[" + UserName + "]님이 입장 하였습니다.\n";
			WriteOthers(msg); // 아직 user_vc에 새로 입장한 user는 포함되지 않았다.
		}

		public void Logout() {
			String msg = "[" + UserName + "]님이 퇴장 하였습니다.\n";
			UserVec.removeElement(this); // Logout한 현재 객체를 벡터에서 지운다
			WriteAll(msg); // 나를 제외한 다른 User들에게 전송
			AppendText("사용자 " + "[" + UserName + "] 퇴장. 현재 참가자 수 " + UserVec.size());
		}
		
		

		// 모든 User들에게 방송. 각각의 UserService Thread의 WriteONe() 을 호출한다.
		public void WriteAll(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				user.WriteOne(str);
			
					
			}
		}
		// 모든 User들에게 Object를 방송. 채팅 message와 image object를 보낼 수 있다
		public void WriteAllObject(Object ob) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				user.WriteOneObject(ob);
			}
		}

		// 나를 제외한 User들에게 방송. 각각의 UserService Thread의 WriteONe() 을 호출한다.
		public void WriteOthers(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user != this)
					user.WriteOne(str);
			}
		}
		public void WriteOne(String msg) { //서버에서 메인에만 보낼 용도
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
				Logout(); // 에러가난 현재 객체를 벡터에서 지운다
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
				ChatMsg obcm = new ChatMsg("귓속말", "200", msg);
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
				Logout(); // 에러가난 현재 객체를 벡터에서 지운다
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
		
		
		
		
		public void Win(ChatMsg cm, String[] userlist) { //승리
			String msg = cm.UserName + "의 승리입니다!!";
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
			String msg = "6목 입니다!!!";
			ChatMsg obcm = new ChatMsg("SERVER", "400", msg);
			obcm.Room_id = cm.Room_id;
			WriteOneObject(obcm);
		}
		public void double3(ChatMsg cm) {
			String msg = "3.3 입니다!!!";
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
			while (true) { // 사용자 접속을 계속해서 받기 위해 while문
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
					} else if (cm.code.matches("200")) { //방 생성
						String roomid = RoomNum+"";
						Room room = new Room(roomid);
						RoomNum++;
						room.userlist = "";
						RoomVec.add(room);
						AppendText("방 생성- room_id: "+room.room_id);
						
						WriteOne("방 생성 완료!");
						
						cm.UserName = "SERVER";
						cm.data = "방 생성";
						cm.Room_id = room.room_id;
						//cm.code = "210"; //생성 알림
						//WriteOneObject(cm);
						WriteAllObject(cm);
						//ChatMsg cm2 = new ChatMsg("SERVER", "220", "Room Id");
						//cm2.Room_id = cm.Room_id;
						//WriteAllObject(cm2);
						
					} else if(cm.code.matches("250")) { //입장요청
						
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
							
						
						
						
					} else if (cm.code.matches("400")) { //채팅
						msg = String.format("[%s] %s", cm.UserName, cm.data);
						
						AppendText(msg); // server 화면에 출력
						String[] args = msg.split(" "); // 단어들을 분리한다.
						
						if(args.length == 1){
							
						}
						else if (args[1].matches("/exit")) {
							Logout();
							break;
		
						} else { // 일반 채팅 메시지
							
							WriteAllObject(cm);
						}
					} else if(cm.code.matches("600")) { //무르기 요청
						//팝업창 클라
						
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
										cm.data = "현재 자신의 턴입니다.";
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
										cm.data = "현재 자신의 턴입니다.";
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
												cm.data = "상대가 거절했습니다.";
												cm.code = "400";
												user.WriteOneObject(cm);
										
											}
										}
										
									}else if(userlist[1].matches(UserName)) {
										for(int j=0; j<user_vc.size(); j++) {
											UserService user = (UserService) user_vc.elementAt(j);
											if(user.UserName.matches(userlist[0])) {
												cm.UserName = "SERVER";
												cm.data = "상대가 거절했습니다.";
												cm.code = "400";
												user.WriteOneObject(cm);
										
											}
										}
									}
									
								}
							}
								
								
						}
						
					} else if (cm.code.matches("900")) { // logout message 처리
						Logout();
						break;
					} else if (cm.code.matches("500")) { //마우스
						for(int i=0; i<RoomVec.size(); i++) {
							if(cm.Room_id.matches(RoomVec.get(i).room_id)){
								OmokRule(cm, RoomVec.get(i).userlist);
							}
						}
						
						
						
						//WriteAllObject(cm);
						//break;
					} else if(cm.code.matches("750")) {//항복처리
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
				
					else { // 300, 500, ... 기타 object는 모두 방송한다.
						WriteAllObject(cm);
					} 
				} catch (IOException e) {
					AppendText("ois.readObject() error");
					try {

						ois.close();
						oos.close();
						client_socket.close();
						Logout(); // 에러가난 현재 객체를 벡터에서 지운다
						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝
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
					cm.data = "상대 턴 입니다.";
					WriteOneObject(cm);
					AppendText("[SERVER]"+cm.data);
					return;
				}
				cm.StoneColor = "black";
				stoneColor = 1;
				
			}else if(userlist[1].matches(cm.UserName)) {
				
				if(RoomVec.get(RoomIndex).StoneListIndex%2 != 1) {
					//obcm = new ChatMsg("Server", cm.UserName,"상대 턴 입니다.");
					cm.UserName = "SERVER";
					cm.code = "400";
					cm.data = "상대 턴 입니다.";
					WriteOneObject(cm);
					AppendText("[SERVER]"+cm.data);
					return;
				}
				
				cm.StoneColor = "white";
				stoneColor = 2;
				
			}else {
				//obcm = new ChatMsg("Server", cm.UserName,"플레이어가 아닙니다.");
				cm.UserName = "SERVER";
				cm.code = "400";
				cm.data = "플레이어가 아닙니다.";
				WriteOneObject(cm);
				return;
				
			}
			
			//if(RoomVec.get(RoomIndex).StoneListIndex%2 == 0)
			//	cm.StoneColor = "black";
			//else
			//	cm.StoneColor = "white";
			
			if(RoomVec.get(RoomIndex).StoneListIndex != 0) {//첫 턴(돌)은 넘어감
				//위치 중복 검사
				for(int i=0; i<RoomVec.get(RoomIndex).StoneList.size(); i++) {
					if(RoomVec.get(RoomIndex).StoneList.get(i)[0] == cm.BoardX && 
							RoomVec.get(RoomIndex).StoneList.get(i)[1] == cm.BoardY) {
						
						//WriteAllObject(cm);
						//WriteAll("중복위치입니다"+coord[0]+","+coord[1]);
						String msg = "중복위치입니다"+cm.BoardX+","+cm.BoardY;
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
			
			
			
			
			//오목 룰 처리
			//승리(오목)
			//BoardCoord[][] - coord[0]coord[1]
			int[][] boardCoord = RoomVec.get(RoomIndex).BoardCoord; //stoneColor 0-빈곳 1-흑돌 2-백돌
			
			
			
			//33검사
			//가로 - 왼쪽부터
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
			
			//세로 	
			
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
			
			//33대각선 좌상\
			
			
			//33대각선 우상/ 
			
			
			
			if(ThreeCount>=2) {
				//double3(cm);
				//return;
			}
			
			
			//5목(승리),6목 체크
			//가로줄 x
			if(cm.BoardX>=4) {
				if(boardCoord[cm.BoardX -4][cm.BoardY] == stoneColor &&
						boardCoord[cm.BoardX -3][cm.BoardY] == stoneColor &&
						boardCoord[cm.BoardX -2][cm.BoardY] == stoneColor &&
						boardCoord[cm.BoardX -1][cm.BoardY] == stoneColor){
					
					if(cm.BoardX>=5) {//6목 체크
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
					
					
					if(cm.BoardX>=4) {//6목 체크
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
					
					if(cm.BoardX>=3) {//6목 체크
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
					
					if(cm.BoardX>=2) {//6목 체크
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
					
					
					if(cm.BoardX>=1) {//6목 체크
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
			//세로줄 y
			if(cm.BoardY>=4) {
				if(boardCoord[cm.BoardX][cm.BoardY -4] == stoneColor &&
						boardCoord[cm.BoardX][cm.BoardY -3] == stoneColor &&
						boardCoord[cm.BoardX][cm.BoardY -2] == stoneColor &&
						boardCoord[cm.BoardX][cm.BoardY -1] == stoneColor){
					if(cm.BoardY>=5) {//6목 체크
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
					

					if(cm.BoardY>=4) {//6목 체크
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
					
					if(cm.BoardY>=3) {//6목 체크
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
					if(cm.BoardY>=2) {//6목 체크
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
					
					if(cm.BoardY>=1) {//6목 체크
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
			
			//좌상 대각선 '\'
			if(cm.BoardX>=4 && cm.BoardY>=4){
				if(boardCoord[cm.BoardX -4][cm.BoardY -4] == stoneColor &&
						boardCoord[cm.BoardX -3][cm.BoardY -3] == stoneColor &&
						boardCoord[cm.BoardX -2][cm.BoardY -2] == stoneColor &&
						boardCoord[cm.BoardX -1][cm.BoardY -1] == stoneColor){
					
					if(cm.BoardX>=5 && cm.BoardY>=5) {//6목 체크
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
					if(cm.BoardX>=4 && cm.BoardY>=4) {//6목 체크
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
					if(cm.BoardX>=3 && cm.BoardY>=3) {//6목 체크
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
					if(cm.BoardX>=2 && cm.BoardY>=2) {//6목 체크
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
					if(cm.BoardX>=1 && cm.BoardY>=1) {//6목 체크
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
			//우상 대각선 '/'
			if(cm.BoardX>=4 && cm.BoardY<=14) {
				if(boardCoord[cm.BoardX -1][cm.BoardY +1] == stoneColor &&
						boardCoord[cm.BoardX -2][cm.BoardY +2] == stoneColor &&
						boardCoord[cm.BoardX -3][cm.BoardY +3] == stoneColor &&
						boardCoord[cm.BoardX -4][cm.BoardY +4] == stoneColor){
					
					if(cm.BoardX>=5 && cm.BoardY<=13) {//6목 체크
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
					
					if(cm.BoardX>=4 && cm.BoardY<=14) {//6목 체크
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
					if(cm.BoardX>=3 && cm.BoardY<=15) {//6목 체크
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
					
					if(cm.BoardX>=2 && cm.BoardY<=16) {//6목 체크
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
					if(cm.BoardX>=1 && cm.BoardY<=17) {//6목 체크
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
			//오목 처리 끝
			
			WriteRoomObject(cm, userlist); //바둑알 두기
			
			
			RoomVec.get(RoomIndex).StoneList.add(RoomVec.get(RoomIndex).StoneListIndex,coord);
			RoomVec.get(RoomIndex).StoneListIndex++;
			RoomVec.get(RoomIndex).BoardCoord[cm.BoardX][cm.BoardY] = stoneColor; //바둑돌 기록, 턴 넘기기
			
		}
		
	}
}
