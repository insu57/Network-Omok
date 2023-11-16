import java.awt.event.MouseEvent;
import java.io.Serializable;

public class ChatMsg implements Serializable{
	private static final long serialVersionUID= 1L;
	public String code; //�������� 100:�α���...
	//����
	//150 ���ӹ� ���� (�α��� ��)
	//200 ���ӹ� ����
	//250 ���ӹ� ����
	//400 ä�ø޽���
	//410 ���� �޽��� (���ο� ��¿�)
	//500 ���� �ٵϵ� �α�
	//600 ������ ��û
	//650 ������ ��û ����/�ź�
	//700 �¸�/�׺� ó��
	//900 �α׾ƿ�
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
