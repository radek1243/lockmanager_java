package window;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class MainWindow {

	private Display display;
	private Shell shell;
	private Button refreshBtn;
	private Button killButton;
	private Table table;
	private Connection con;
	private MessageBox mb;
	
	public MainWindow() {
		this.display = new Display();
		this.shell = new Shell(this.display, SWT.CLOSE | SWT.MIN);
		this.initialize();
		try{
			Class.forName("oracle.jdbc.driver.OracleDriver");
			this.con = null;
			this.con = DriverManager.getConnection("jdbc:oracle:thin:@:1521:","","");
		}
		catch (ClassNotFoundException e) {
			this.mb.setMessage(e.getMessage());
			this.mb.open();
		}
		catch(SQLException e){
			this.mb.setMessage(e.getMessage());
			this.mb.open();
		}
	}
	
	private void initialize(){
		//this.shell.setSize(600, 400);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = 15;
		gridLayout.marginHeight = 15;
		this.shell.setLayout(gridLayout);
		this.shell.setText("LockManager");
		
		this.refreshBtn = new Button(this.shell, SWT.PUSH);
		this.refreshBtn.setText("Odœwie¿");
		this.refreshBtn.pack();
		
		this.killButton = new Button(this.shell, SWT.PUSH);
		this.killButton.setText("Kill");
		this.killButton.pack();
		
		this.table = new Table(this.shell, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE | SWT.FULL_SELECTION);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = SWT.FILL;
		data.widthHint=550;
		data.heightHint=350;
		this.table.setLayoutData(data);
		
		TableColumn col1 = new TableColumn(this.table, SWT.CENTER);
		col1.setText("Status");
		col1.setWidth(100);
		TableColumn col2 = new TableColumn(this.table, SWT.CENTER);
		col2.setText("Username");
		col2.setWidth(200);
		TableColumn col3 = new TableColumn(this.table, SWT.CENTER);
		col3.setText("Sid");
		col3.setWidth(100);
		TableColumn col4 = new TableColumn(this.table, SWT.CENTER);
		col4.setText("Serial");
		col4.setWidth(100);
		TableColumn col5 = new TableColumn(this.table, SWT.CENTER);
		col5.setText("Block");
		col5.setWidth(65);
		this.table.setHeaderVisible(true);
		this.table.pack();
		
		this.mb = new MessageBox(this.shell, SWT.ICON_ERROR | SWT.OK);
		this.mb.setText("B³¹d");
		
		this.shell.pack();
		this.centerWindow(); //ostatnia linijka jak chodzi o wygl¹d
		
		//poni¿ej listenery
		
		this.shell.addShellListener(new ShellListener() {
			
			@Override
			public void shellIconified(ShellEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void shellDeiconified(ShellEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void shellDeactivated(ShellEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void shellClosed(ShellEvent arg0) {
				try{
					con.close();
				}
				catch(SQLException e){
					mb.setMessage(e.getMessage());
					mb.open();
				}
			}
			
			@Override
			public void shellActivated(ShellEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		this.refreshBtn.addSelectionListener(new SelectionListener() {			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					//table.clearAll();
					table.removeAll();
					PreparedStatement st = con.prepareStatement("select v$session.USERNAME, l1.SID, v$session.SERIAL#, l1.BLOCK "
									+ "from v$lock l1 join v$session on l1.sid=v$session.sid "
									+ "where l1.id1 in (select l1.id1 from v$lock l1 where l1.block > 0) order by l1.id1, l1.block desc");
					ResultSet rs = st.executeQuery();
					while(rs.next()){
						TableItem item = new TableItem(table, SWT.NONE);
						item.setText(0,"");
						item.setText(1, rs.getString(1));
						item.setText(2, rs.getString(2));
						item.setText(3, rs.getString(3));
						item.setText(4, rs.getString(4));
					}		
					rs.close();
					st.close();
				} 
				catch (SQLException e) {
					mb.setMessage(e.getMessage());
					mb.open();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		this.killButton.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					PreparedStatement st = con.prepareStatement("alter system kill session '"+
					table.getItem(table.getSelectionIndex()).getText(2)+","+table.getItem(table.getSelectionIndex()).getText(3)+"'");
					int result = st.executeUpdate();
					if(result==0){
						table.getItem(table.getSelectionIndex()).setText(0, "KILLED");
					}
					st.close();
				} 
				catch (SQLException e) {
					mb.setMessage(e.getMessage());
					mb.open();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	public void open(){
		this.shell.open();
		while(!this.shell.isDisposed()){
			if(!this.display.readAndDispatch()){
				this.display.sleep();
			}
		}
		this.display.dispose();
	}
	
	private void centerWindow(){
		Monitor primary = display.getPrimaryMonitor();
	    Rectangle bounds = primary.getBounds();
	    Rectangle rect = shell.getBounds();
	    
	    int x = bounds.x + (bounds.width - rect.width) / 2;
	    int y = bounds.y + (bounds.height - rect.height) / 2;
	    
	    this.shell.setLocation(x, y);
	    primary= null; bounds=null; rect=null;
	}
}
