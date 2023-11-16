import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import com.sun.tools.javac.Main;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingConstants;



public class GameClientOmokView extends JFrame {

	public GameClientRoom mainview;	
	
	private JPanel contentPane;
	private static final long serialVersionUID = 1L;
	
	private JTextPane textArea;
	private JLabel lblCheckBoard;
	private JTextField txtInput;
	private JButton btnSurrender;
	private JButton btnChance;
	private JButton btnSend;
	private JButton btnGameRoom;
	private Image background;
	JPanel panel;
	private Graphics gc;
	private Image BlackStone;
	private Image WhiteStone;
	private JLabel lblMouseEvent;
	
	private String UserName;
	private String room_id;
	// 그려진 Image를 보관하는 용도, paint() 함수에서 이용한다.
	private Image panelImage = null; 
	private Graphics gc2 = null;
	
	//바둑판 좌표 행렬
	
	private int[] BoardCoordX = new int[19];
	private int[] BoardCoordY = new int[19];
	
	ArrayList<int[]> StoneList = new ArrayList<int[]>(); //돌 좌표 순서 저장
	// [0]은 x, [1]은 y // 대기방 형식에 맞춰 수정 필요
	private int StoneListIndex = 0;
	String[] colorList = new String[19*19];
	
	/**
	 * Launch the application.
	 */
	

	/**
	 * Create the frame.
	 */
	public GameClientOmokView(String username, GameClientRoom view) {
		
		mainview = view;
		UserName = username;
		
		
		setTitle("오목VS-"+UserName);
		setResizable(false);
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(1000, 2100, 922, 668);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		panel = new JPanel();
		panel.setBounds(12, 10, 540, 540);
		panel.setBackground(new Color(255, 255, 255));
		contentPane.add(panel);
		
		background = new ImageIcon("src/image/540_Go_board.png").getImage();
		//background = background.getScaledInstance(540, 540, Image.SCALE_SMOOTH);
		//lblCheckBoard = new JLabel("New label",background,JLabel.CENTER);
		//lblCheckBoard.setPreferredSize(new Dimension(100,100));
		//ImageIcon backgroundIcon = new ImageIcon(background);
		//lblCheckBoard = new JLabel(backgroundIcon);
		//panel.add(lblCheckBoard);
		
		
		//gc.drawImage(panelImage, 0, 0, panel);
		
		BlackStone = new ImageIcon("src/image/black_1.png").getImage();
		WhiteStone = new ImageIcon("src/image/white_2.png").getImage();
		
		
		
	    
	    
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(583, 62, 311, 488);
		contentPane.add(scrollPane);
		
		textArea = new JTextPane(); //채팅출력
		textArea.setEditable(true);
		textArea.setFont(new Font("굴림체", Font.PLAIN, 14));
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		
		txtInput = new JTextField(); //채팅입력
		txtInput.setBounds(583, 560, 216, 55);
		contentPane.add(txtInput);
		txtInput.setColumns(10);
		
		
		setVisible(true);
		
		
		
		btnSend = new JButton("전송");
		btnSend.setFont(new Font("굴림", Font.BOLD, 18));
		btnSend.setBounds(811, 560, 83, 55);
		contentPane.add(btnSend);
		
		
		//btnGameRoom = new JButton(" 대기방 입장");
		//btnGameRoom.setFont(new Font("굴림", Font.BOLD, 20));
		//btnGameRoom.setBounds(653, 10, 158, 68);
		//contentPane.add(btnGameRoom);
		
		//버튼클릭 시 대기방 창 열기 - 1:1 매칭 + 관전
		
		btnSurrender = new JButton("항복");
		btnSurrender.setFont(new Font("굴림", Font.PLAIN, 18));
		btnSurrender.setBounds(811, 10, 83, 42);
		contentPane.add(btnSurrender);
		
		btnChance = new JButton("무르기 요청");
		btnChance.setFont(new Font("굴림", Font.PLAIN, 18));
		btnChance.setBounds(583, 10, 136, 40);
		contentPane.add(btnChance);
		
		//lblMouseEvent = new JLabel("<dynamic>");
		//lblMouseEvent.setBounds(22, 560, 311, 55);
		//lblMouseEvent.setBorder(new LineBorder(new Color(0, 0, 0)));
		//lblMouseEvent.setHorizontalAlignment(SwingConstants.CENTER);
		//lblMouseEvent.setFont(new Font("굴림", Font.BOLD, 14));
		//마우스 좌표 출력
		//contentPane.add(lblMouseEvent);
		//lblMouseEvent.setVisible(false);
		
		//추가 기능 히스토리 기능(관전 시 앞, 뒤로 과거 기록 출력)
		//추가 기능 전적보기 기능(Username 기준)
		//
		
		gc =  panel.getGraphics();
		//gc.drawImage(background, 0, 0, panel);
		
		panelImage = createImage(panel.getWidth(), panel.getHeight());
		gc2 = panelImage.getGraphics();
		
		gc2.drawImage(background,0,0, panel);
		
		for(int i=0; i<19; i++) {
			BoardCoordX[i] = 18 + i*28;
			BoardCoordY[i] = 22 + i*28;
		}//바둑판 좌표 초기화
		//바둑판 이미지 크기 변경시 이 부분과 하단 바둑돌 찍는 부분 수정 필요 (바둑판 간격 등)
		
		
		//네트워크 
		
			
			
		TextSendAction action = new TextSendAction();
		btnSend.addActionListener(action);
		txtInput.addActionListener(action);
		txtInput.requestFocus();
			
		ChanceAction action2 = new ChanceAction();
		btnChance.addActionListener(action2);
		
		MyMouseEvent mouse = new MyMouseEvent();
		panel.addMouseMotionListener(mouse);
		panel.addMouseListener(mouse);
		
		btnSurrender.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				ChatMsg obcm = new ChatMsg(UserName, "750", "항복");
				obcm.Room_id = room_id;
				mainview.SendObject(obcm);
			}
		});
		
		setVisible(false);
		
		
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				ChatMsg obcm = new ChatMsg(UserName, "800", "퇴장");
				obcm.Room_id = room_id;
				mainview.SendObject(obcm);
			}
			
		});
		
		
	}
	
	public void paint(Graphics g) {
		super.paint(g);
		// Image 영역이 가려졌다 다시 나타날 때 그려준다.
		gc.drawImage(panelImage, 0, 0, this);
		
		
		/*
		for(int i=0; i<StoneList.size(); i++) {
			int x = StoneList.get(i)[0];
			int y = StoneList.get(i)[1];
			if(colorList[i].matches("black")) {
				gc2.drawImage(BlackStone,x*28 +8, y*28 +9, panel);
			}else {
				gc2.drawImage(WhiteStone,x*28 +8, y*28 +9, panel);
			}
		}*/
	}
	

	
	public void DoMouseEvent(ChatMsg cm) {
	//서버에서 받아서 출력 (흑돌/백돌) 
	//
		int x = cm.BoardX;
		int y = cm.BoardY;
		int[] stoneTemp = new int[2];
		stoneTemp[0] = x;
		stoneTemp[1] = y;
		StoneList.add(StoneListIndex,stoneTemp);
		colorList[StoneListIndex] = cm.StoneColor;
		StoneListIndex++;
		
		
		
		if(cm.StoneColor.toString().matches("black"))
			gc2.drawImage(BlackStone,x*28 +8, y*28 +9, panel); //24크기로 수정
		else
			gc2.drawImage(WhiteStone,x*28 +8, y*28 +9, panel);
		gc.drawImage(panelImage, 0, 0,panel);
		
	}
	
	//String형식으로 좌표 송신 "x,y" 형식 (ex: "10,19")
	public void SendMouseEvent(MouseEvent e) {
		ChatMsg cm = new ChatMsg(UserName, "500", "MOUSE");
		cm.mouse_e = e;
		int[] near = new int[2];
		near = nearest(e.getX(), e.getY());
		cm.BoardX = near[0];
		cm.BoardY = near[1];
		cm.StoneColor = "black";
		cm.Room_id = room_id;
		mainview.SendObject(cm);
		
	}
	//바둑판 좌표 한칸 대략 x=y=28정도 (바둑판 19x19) [1,1] -> [18,22]
	class MyMouseEvent implements MouseListener, MouseMotionListener{

	

		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
			//마우스 올렸을 때 반투명한 바둑돌 (위치 알려주기) *추가기능 도전과제
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			//lblMouseEvent.setText(e.getButton() + " mousePressed " + e.getX() + "," + e.getY());
			//임시(서버 사용x)
			
			//gc.drawImage(BlackStone, e.getX()-12, e.getY()-10, panel); 
			//바둑판 기준에 맞게 좌표 수정
			
			
			int[] near = new int[2];
			near = nearest(e.getX(), e.getY());
			//gc.drawImage(BlackStone,near[0]*28 +11, near[1]*28 +17, panel); 
			System.out.println(near[0]+","+near[1]);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void mouseDragged(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
			SendMouseEvent(e);
		}
		
	}
	public int[] nearest (int x, int y) { //가장 가까운 바둑판 좌표계산
		int tempX, nearX = Math.abs(BoardCoordX[0] - x);
		int tempY, nearY = Math.abs(BoardCoordY[0] - y);
		int nearBoardX=0, nearBoardY=0;
		int[] nearestCoord = new int[2];
		for(int i=0; i<19; i++) {
			
			tempX = Math.abs(BoardCoordX[i] - x);
			tempY = Math.abs(BoardCoordY[i] - y);
			if(tempX <= nearX) {
				nearX = tempX;
				nearBoardX = i;
			}
			if(tempY <= nearY) {
				nearY = tempY;
				nearBoardY = i;
			}
		}
		nearestCoord[0] = nearBoardX;
		nearestCoord[1] = nearBoardY;
		return nearestCoord;
		
	}
	public void setRoomid(String room_id) {
		this.room_id = room_id;
	}
	
	class ChanceAction implements ActionListener{
		
		
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			
			
			ChatMsg obcm = new ChatMsg(UserName, "600", "Chance");
			
			obcm.Room_id = room_id;
			
			mainview.SendObject(obcm);
			
		}
		
	}
	
	class TextSendAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Send button을 누르거나 메시지 입력하고 Enter key 치면
			if (e.getSource() == btnSend || e.getSource() == txtInput) {
				String msg = null;
				msg = txtInput.getText();
				ChatMsg obcm = new ChatMsg(UserName, "400", msg);
				//mainview.SendMessage(msg);
				obcm.Room_id = room_id;
				obcm.StoneIndex = StoneListIndex;
				mainview.SendObject(obcm);
				
				txtInput.setText(""); // 메세지를 보내고 나면 메세지 쓰는창을 비운다.
				txtInput.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다
				if (msg.contains("/exit")) // 종료 처리
					System.exit(0);
			}
		}
	}
	
	
	public void ChanceOk() { //무르기 받았을 때 승인/거부
		String[] options = new String[2];
		options[0] = "승인";
		options[1] = "거부";
		int answer = JOptionPane.showOptionDialog(null, "상대방이 무르기를 요청했습니다.", "무르기 요청", 0, JOptionPane.QUESTION_MESSAGE, null, options, null);
		
		
		ChatMsg cm = new ChatMsg(UserName, "650", "");
		cm.Room_id = room_id;
		if(answer == JOptionPane.OK_OPTION) {
			//System.out.println("yes");
			cm.data = "YES";
			
			mainview.SendObject(cm);
			
		}else if(answer == JOptionPane.NO_OPTION) {
			//System.out.println("nop");
			cm.data = "NO";
			
			mainview.SendObject(cm);
		}
		
	}
	
	public void StoneCancel(){
		//gc.drawImage(background, 0, 0, panel);
		//paint(gc2);
		
		StoneList.remove(StoneListIndex-1);
		StoneListIndex--;
		//paint(gc);
		//paint(gc2);
		gc2.drawImage(background, 0, 0, panel);
		for(int i=0; i<StoneList.size(); i++) {
			int x = StoneList.get(i)[0];
			int y = StoneList.get(i)[1];
			if(colorList[i].matches("black")) {
				gc2.drawImage(BlackStone,x*28 +8, y*28 +9, panel);
			}else {
				gc2.drawImage(WhiteStone,x*28 +8, y*28 +9, panel);
			}
		}
		gc.drawImage(panelImage,0,0,panel);
	}
	
	public void Win(ChatMsg cm) {
		JOptionPane.showMessageDialog(null, cm.UserName+"의 승리입니다! " , "게임종료", JOptionPane.PLAIN_MESSAGE);
		
		StoneList.clear();
		StoneListIndex = 0;
		
		gc2.drawImage(background,0,0,panel);
		gc.drawImage(panelImage,0,0,panel);
		
		AppendText(cm.UserName+"의 승리입니다! ");
	}
	
	public void AppendText(String msg) {
		msg = msg.trim(); // 앞뒤 blank와 \n을 제거한다.	
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
		int len = textArea.getDocument().getLength();
		textArea.setCaretPosition(len);
	}
	
	
	
}
