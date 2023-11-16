import java.awt.event.MouseEvent;
import java.io.Serializable;

public class ChatMsg implements Serializable{
	private static final long serialVersionUID= 1L;
	public String code; //프로토콜 100:로그인...
	//서버
	//150 게임방 정보 (로그인 시)
	//200 게임방 생성
	//250 게임방 입장
	//400 채팅메시지
	//410 서버 메시지 (메인에 출력용)
	//500 오목 바둑돌 두기
	//600 무르기 요청
	//650 무르기 요청 승인/거부
	//700 승리/항복 처리
	//900 로그아웃
	public String data;
	public String UserName;
	public MouseEvent mouse_e;
	public int BoardX;
	public int BoardY;
	public String Room_id;
	public String StoneColor; //black or white
	public String userlist;
	public int StoneIndex;
	public ChatMsg(String UserName, String code, String msg) {
		this.code = code;
		this.UserName = UserName;
		this.data = msg;
	}
}
