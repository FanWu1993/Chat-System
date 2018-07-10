import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import java.net.*;
import java.io.*;
import java.util.Vector;
import java.util.StringTokenizer;

public class ServerApp {

	private Shell sShell = null;
	private Text TextPort;
	private Text connectionArea;
	private Text NotificationArea;
	ServerSocket server = null;
	ConnectSocket connect = null;
	Button btnNewButton;
	static Vector clients = new Vector();

	/**
	 * This method initializes sShell
	 * 
	 * @wbp.parser.entryPoint
	 */
	private void createSShell() {
		sShell = new Shell();
		sShell.setText("\u591A\u5BA2\u6237\u4FE1\u606F\u5E7F\u64AD\u7CFB\u7EDF-\u670D\u52A1\u5668\u7AEF");
		sShell.setSize(new Point(400, 350));
		sShell.setLayout(null);

		Label lblNewLabel = new Label(sShell, SWT.NONE);
		lblNewLabel.setFont(SWTResourceManager.getFont("宋体", 12, SWT.NORMAL));
		lblNewLabel.setBounds(30, 10, 64, 16);
		lblNewLabel.setText("\u76D1\u542C\u7AEF\u53E3");

		TextPort = new Text(sShell, SWT.BORDER);
		TextPort.setText("8080");
		TextPort.setBounds(120, 8, 122, 22);
		TextPort.addKeyListener(new KeyListener() {
			
			@Override
			public void keyReleased(KeyEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				if(arg0.keyCode==13)
					start();
			}
		});

		btnNewButton = new Button(sShell, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				start();
				}
		});
		btnNewButton.setFont(SWTResourceManager.getFont("宋体", 12, SWT.NORMAL));
		btnNewButton.setBounds(256, 7, 90, 22);
		btnNewButton.setText("\u5F00\u59CB\u76D1\u542C");

		connectionArea = new Text(sShell, SWT.BORDER | SWT.V_SCROLL);
		connectionArea
				.setBackground(SWTResourceManager.getColor(204, 255, 204));
		connectionArea.setBounds(10, 32, 372, 138);

		NotificationArea = new Text(sShell, SWT.BORDER | SWT.V_SCROLL);
		NotificationArea.setBackground(SWTResourceManager.getColor(204, 255,
				204));
		NotificationArea.setBounds(10, 176, 372, 137);
	}

	public class ConnectSocket extends Thread {

		Socket socket;
		public void appendInformation() {
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					Client c = new Client(socket);
					clients.addElement(c);
					if (checkName(c)) {
						c.start();
						notifyRoom();
					} else {
						disconnect(c);
					}
				}
			});
		}


		public void run() {
			while (true) {
				while (true) {
					try {
						socket = server.accept();
					} catch (IOException e2) {
						e2.printStackTrace();
						connectionArea.append("客户连接失败\n");
					}
					this.appendInformation();
				}
			}
		}
		public void close(){
			this.stop();
			for(int i=0;i<clients.size();i++){
				Client c=(Client) clients.elementAt(i);
				try{
					c.socket.close();
				}catch(IOException e){
					e.printStackTrace();
				}
			}
			try{
				server.close();
			}catch(IOException e){
				
			}
				
		}
	}
	
	public void start(){
		int temp=-1;
		try {
			temp=Integer.parseInt(TextPort.getText());
			if(temp<0||temp>65535)
				throw new Exception();
		} catch (Exception e2) {
			MessageDialog.openInformation(sShell, "信息提示", "请输入正确的端口号！");
			TextPort.setText("");
			return;
		}
			try {
				server = new ServerSocket(temp);
				NotificationArea.append("系统提示：聊天服务器系统开始启动......\n");
			} catch (IOException e1) {
				e1.printStackTrace();
				NotificationArea.append("服务器端口打开出错\n");
			}
			if (server != null) {
				ConnectSocket connect = new ConnectSocket();
				connect.start();
			}
			TextPort.setEditable(false);
			btnNewButton.setText("停止监听");
	}
	
	class Client extends Thread {
		String name;
		BufferedReader dis;
		PrintStream ps;
		Socket socket;

		public void appendConnectionArea(String str) {
			final String str1 = str;
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {
					connectionArea.append(str1);
				}
			});
		}

		public Client(Socket e) {
			socket = e;
			try {
				dis = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				ps = new PrintStream(socket.getOutputStream());
				String info = dis.readLine();
				StringTokenizer stinfo = new StringTokenizer(info, ":");
				String head = stinfo.nextToken();
				name = stinfo.nextToken();
				connectionArea.append("系统消息：" + name + "已经连接\n");
			} catch (IOException e1) {
				e1.printStackTrace();
				NotificationArea.append("系统消息：用户连接出错\n");
			}
		}

		public void send(String msg) {
			ps.println(msg);
			ps.flush();
		}
		public void sendtoUser(String fromUser,String toUser,String msg){
			Client c=getClient(fromUser);
			c.send("MSG:<私聊>"+fromUser+":"+msg);
			Client c1=getClient(toUser);
			c1.send("MSG:<私聊>"+fromUser+":"+msg);
		}
		
		public void run() {
			while (true) {
				String line = null;
				try {
					line = dis.readLine();
				} catch (IOException e) {
					e.printStackTrace();
					appendConnectionArea("系统消息：读客户信息出错");
					final Client c = this;
					Display.getDefault().syncExec(new Runnable() {
						
						@Override
						public void run() {
							disconnect(c);
						}
					});
					notifyRoom();
					return;
				}
				StringTokenizer st = new StringTokenizer(line, ":");
				String keyword = st.nextToken();
				if (keyword.equalsIgnoreCase("MSG")) {
					sendClients(line);
				}else if(keyword.equalsIgnoreCase("MSGToUser")){

					String fromUser = st.nextToken();
					String toUser = st.nextToken();
					String msg = st.nextToken();

					sendtoUser(fromUser, toUser, msg);
				}else if (keyword.equalsIgnoreCase("QUIT")) {
					send("QUIT");
					disconnect(this);
					notifyRoom();
					this.stop();
				}
			}
		}
	}

	public boolean checkName(Client newClient) {
		for (int i = 0; i < clients.size(); i++) {
			Client c = (Client) clients.elementAt(i);
			if ((c != newClient) && (c.name).equals(newClient.name))
				return false;
		}
		return true;
	}

	public Client getClient(String name){
		for(int i = 0; i < clients.size();i++){
			Client c = (Client) clients.elementAt(i);
			if(c.name.equals(name))
				return c;
		}
		return null;
	}
	public void notifyRoom() {
		String people = "PEOPLE";
		for (int i = 0; i < clients.size(); i++) {
			Client c = (Client) clients.elementAt(i);
			people += ":" + c.name;
		}
		sendClients(people);
	}

	public void sendClients(String msg) {
		for (int i = 0; i < clients.size(); i++) {
			Client c = (Client) clients.elementAt(i);
			c.send(msg);
		}
	}

	public void disconnect(final Client c) {
		try {
			Display.getDefault().syncExec(new Runnable() {

				@Override
				public void run() {

					connectionArea.append(c.name + "断开连接\n");

				}
			});
			c.send("QUIT");
			clients.removeElement(c);
			c.socket.close();
		} catch (IOException e) {
			e.printStackTrace();
			NotificationArea.append("客户端开错误\n");
		}
	}
}
