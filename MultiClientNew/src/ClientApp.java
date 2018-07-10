
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.List;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Vector;
import java.util.StringTokenizer;

import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Composite;

public class ClientApp {

	Socket socket=null;
	BufferedReader cin=null;
	PrintStream cout=null;
	String clientName="";
	private Shell sShell = null;
	private Text ipAddress;
	private Text textPort;
	private Text ClientName;
	private Text talkMessage;
	private Text textAreaMessage;
	List ClientsList;
	Button User;
	Button BD;
	


	/**
	 * This method initializes sShell
	 * @wbp.parser.entryPoint
	 */
	private void createSShell() {
		sShell = new Shell();
		sShell.setBackgroundImage(SWTResourceManager.getImage(ClientApp.class, "/images/bg.png"));
		sShell.setBackgroundMode(SWT.INHERIT_DEFAULT);
		
		sShell.setText("\u591A\u7528\u6237\u4FE1\u606F\u5E7F\u64AD\u7CFB\u7EDF-\u5BA2\u6237\u7AEF");
		sShell.setSize(new Point(520, 350));
		sShell.setLayout(null);
		
		Label lblip = new Label(sShell, SWT.NONE);
		lblip.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		lblip.setFont(SWTResourceManager.getFont("宋体", 10, SWT.NORMAL));
		lblip.setBounds(10, 10, 64, 14);
		lblip.setText("\u670D\u52A1\u5668ip\uFF1A");
		
		Label label = new Label(sShell, SWT.NONE);
		label.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		label.setText("\u7AEF\u53E3\uFF1A");
		label.setFont(SWTResourceManager.getFont("宋体", 10, SWT.NORMAL));
		label.setBounds(156, 10, 39, 14);
		
		Label label_1 = new Label(sShell, SWT.NONE);
		label_1.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		label_1.setText("\u5BA2\u6237\u540D\u79F0\uFF1A");
		label_1.setFont(SWTResourceManager.getFont("宋体", 10, SWT.NORMAL));
		label_1.setBounds(277, 10, 64, 14);
		
		ipAddress = new Text(sShell, SWT.BORDER);
		ipAddress.setBackground(SWTResourceManager.getColor(204, 255, 204));
		ipAddress.setText("127.0.0.1");
		ipAddress.setBounds(80, 8, 70, 18);
		
		textPort = new Text(sShell, SWT.BORDER);
		textPort.setText("8080");
		textPort.setBackground(SWTResourceManager.getColor(204, 255, 204));
		textPort.setBounds(201, 8, 70, 18);
		
		ClientName = new Text(sShell, SWT.BORDER);
		ClientName.setBackground(SWTResourceManager.getColor(204, 255, 204));
		ClientName.setBounds(347, 8, 70, 18);
		
		Button connectServer = new Button(sShell, SWT.NONE);
		connectServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try{
					InetAddress ip=InetAddress.getByName(ipAddress.getText());
					int port=Integer.parseInt(textPort.getText());
					socket=new Socket(ip,port);
					textAreaMessage.append("系统提示：与服务器开始连接......\n");
				}catch(IOException e1){
					textAreaMessage.append("服务器端口打开出错\n");
				}
				if(socket!=null){
					textAreaMessage.append("系统提示：与服务器连接成功......\n");
					clientName=ClientName.getText().trim();
					try{
						cin=new BufferedReader(new InputStreamReader(socket.getInputStream()));
						cout=new PrintStream(socket.getOutputStream());
						String str="PEOPLE:"+clientName;
						cout.println(str);
						ReadMessageThread readThread=new ReadMessageThread();
						readThread.start();
					}catch(IOException e3){
						textAreaMessage.append("输入/输出异常\n");
					}
				}
			}
		});
		connectServer.setFont(SWTResourceManager.getFont("宋体", 10, SWT.NORMAL));
		connectServer.setBounds(430, 6, 72, 22);
		connectServer.setText("\u8FDE\u63A5\u670D\u52A1\u5668");
		
		talkMessage = new Text(sShell, SWT.BORDER);
		talkMessage.setBackground(SWTResourceManager.getColor(204, 255, 204));
		talkMessage.setBounds(75, 34, 246, 34);
		talkMessage.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.keyCode==13)
				{
					send();
				}
				
			}
		});
		
		
		Button send = new Button(sShell, SWT.NONE);
		send.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				send();
			}
		});
		send.setFont(SWTResourceManager.getFont("宋体", 10, SWT.NORMAL));
		send.setBounds(340, 40, 72, 22);
		send.setText("\u53D1\u9001\u4FE1\u606F");
		
		Button disconnect = new Button(sShell, SWT.NONE);
		disconnect.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String str="QUIT";
				cout.println(str);
				textAreaMessage.append("客户请求断开连接\n");
				
			}
		});
		disconnect.setFont(SWTResourceManager.getFont("宋体", 10, SWT.NORMAL));
		disconnect.setBounds(430, 40, 72, 22);
		disconnect.setText("\u65AD\u5F00\u8FDE\u63A5");
		
		Group group = new Group(sShell, SWT.NONE);
		group.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT));
		group.setText("\u5728\u7EBF\u7528\u6237\u5217\u8868");
		group.setBounds(0, 77, 131, 229);
		
		ClientsList = new List(group, SWT.BORDER);
		ClientsList.setLocation(10, 20);
		ClientsList.setSize(110, 209);
		ClientsList.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = ClientsList.getSelectionIndex();
				InputDialog inputDialog = new InputDialog(sShell,
						"私聊窗口", "请输入：", "", null);
				String text = null;
				if (inputDialog.open() == 0)
					text = inputDialog.getValue();
				talkMessage.setText(text);
				User.setSelection(true);
				BD.setSelection(false);
			}
		});
		ClientsList.setBackground(SWTResourceManager.getColor(204, 255, 204));
		
		Group group_1 = new Group(sShell, SWT.NONE);
		group_1.setForeground(SWTResourceManager.getColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT));
		group_1.setText("\u804A\u5929\u4FE1\u606F");
		group_1.setBounds(140, 74, 362, 232);
		
		textAreaMessage = new Text(group_1, SWT.BORDER | SWT.V_SCROLL);
		textAreaMessage.setLocation(10, 26);
		textAreaMessage.setSize(352, 206);
		textAreaMessage.setBackground(SWTResourceManager.getColor(204, 255, 204));
		
		Composite composite = new Composite(sShell, SWT.NONE);
		composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		composite.setForeground(SWTResourceManager.getColor(SWT.COLOR_CYAN));
		composite.setBounds(10, 30, 55, 40);
		
		
		BD = new Button(composite, SWT.RADIO);
		BD.setLocation(0, 0);
		BD.setSize(55, 20);
		BD.setForeground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_BORDER));
		BD.setText("\u5E7F\u64AD");
		BD.setSelection(true);
		
		User = new Button(composite, SWT.RADIO);
		User.setLocation(0, 20);
		User.setSize(55, 20);
		User.setText("\u79C1\u804A");
	}
	public void send(){
		if(User.getSelection()){
			if(ClientsList.getSelectionIndex()==-1){
				MessageDialog.openInformation(sShell, "信息提示", "请选择用户");
				return;
			}else
				if(ClientsList.getItem(ClientsList.getSelectionIndex()).equals(ClientName.getText().trim())){
					MessageDialog.openInformation(sShell, "信息提示", "选择用户为自己");
					return;
			}
		}
		String str =talkMessage.getText();
		if(BD.getSelection())
			str="MSG:"+clientName+":"+str;
		else if(User.getSelection())
			str = "MSGToUser:"+clientName+":"+ClientsList.getItem(ClientsList.getSelectionIndex())+":"+str;
		cout.println(str);
		talkMessage.setText("");
	}
	class ReadMessageThread extends Thread{
		public void list(final Vector imessage){
			final String str1=imessage.toString();
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					String[] s=str1.substring(1,str1.length()-1).split(",");
					for(String str:s)
					{
						ClientsList.add(str.trim());
					}
				}
			});
		}
		public void appendTextArea(String str){
			final String str1=str;
			Display.getDefault().syncExec(new Runnable() {
				
				@Override
				public void run() {
					textAreaMessage.append(str1);
				}
			});
		}
		public void run(){
			String line="";
			while(true){
				try{
					line=cin.readLine();
				}catch(IOException e){
					e.printStackTrace();
					this.appendTextArea("输入/输出异常\n");
					return;
				}
				StringTokenizer st=new StringTokenizer(line,":");
				String keyword=st.nextToken();
				if(keyword.equalsIgnoreCase("QUIT")){
					try{
						socket.close();
							this.appendTextArea("接收到服务器同意断开信息，socket关闭\n");
							Display.getDefault().syncExec(new Runnable() {
								
								@Override
								public void run() {
									ClientsList.removeAll();
								}
							});
							this.stop();
					}catch(IOException e){
						this.appendTextArea("socket关闭异常\n");
					}
					this.stop();
				}else if(keyword.equalsIgnoreCase("PEOPLE")){
					Display.getDefault().syncExec(new Runnable() {
						
						@Override
						public void run() {
							ClientsList.removeAll();
						}
					});
					while(st.hasMoreTokens())
					{
						Vector imessage=new Vector();
						imessage.addElement(st.nextToken());
						this.list(imessage);
					}
				}else{
					String message=st.nextToken("\0");
					message=message.substring(1);
					this.appendTextArea(message+"\n");
				}
			}
		}
	}
}
