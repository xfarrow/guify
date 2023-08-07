package views;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import code.Constants.GuifyColors;
import controllers.LoginController;
import views.interfaces.ILoginFrame;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;

public class Login extends JFrame implements ILoginFrame {

	private static final long serialVersionUID = 1;
	private LoginController controller;
	private JPanel contentPane;
	private JPasswordField passwordField;
	private JTextField usernameField;
	private JTextField hostField;
	private JTextField portField;
	
	public Login(Object controller) {
		this.controller = (LoginController) controller;
		
		setTitle(this.controller.getTitle());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setBounds(100, 100, 300, 400);
		contentPane = new JPanel();
		contentPane.setForeground(Color.WHITE);
		contentPane.setBackground(Color.WHITE);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		passwordField = new JPasswordField();
		passwordField.setToolTipText("SSH Password");
		passwordField.setBounds(10, 159, 139, 20);
		contentPane.add(passwordField);
		
		JLabel lblNewLabel = new JLabel("Username");
		lblNewLabel.setBounds(10, 78, 139, 14);
		contentPane.add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("Password");
		lblNewLabel_1.setBounds(10, 134, 139, 14);
		contentPane.add(lblNewLabel_1);
		
		usernameField = new JTextField();
		usernameField.setToolTipText("SSH Username");
		usernameField.setBounds(10, 103, 139, 20);
		contentPane.add(usernameField);
		usernameField.setColumns(10);
		
		JButton btnConnect = new JButton("Connect");
		btnConnect.setForeground(Color.WHITE);
		btnConnect.setBackground(GuifyColors.BLUE);
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnConnect_OnClick();
			}
		});
		btnConnect.setBounds(75, 297, 139, 30);
		contentPane.add(btnConnect);
		
		JLabel lblNewLabel_2 = new JLabel("Host");
		lblNewLabel_2.setBounds(10, 22, 139, 14);
		contentPane.add(lblNewLabel_2);
		
		hostField = new JTextField();
		hostField.setToolTipText("SSH Host");
		hostField.setBounds(10, 47, 139, 20);
		contentPane.add(hostField);
		hostField.setColumns(10);
		
		JLabel lblNewLabel_3 = new JLabel("Port");
		lblNewLabel_3.setBounds(10, 190, 139, 14);
		contentPane.add(lblNewLabel_3);
		
		portField = new JTextField();
		portField.setText("22");
		portField.setToolTipText("SSH Port");
		portField.setBounds(10, 215, 86, 20);
		contentPane.add(portField);
		portField.setColumns(10);
		
		
	}

	/**
	 * Events
	 */
	private void btnConnect_OnClick() {

		String host = hostField.getText();
		String username = usernameField.getText();
		String password = String.valueOf(passwordField.getPassword());
		String port = portField.getText();
		
		// Perform validation
		try {
			controller.ValidateInput(host, username, password, port);
		}
		catch(IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(new JFrame(), ex.getMessage(), "Attention required", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// Perform login
		if(!controller.Login(host, username, password, port)) {
			JOptionPane.showMessageDialog(new JFrame(), "SSH Login failed", "SSH Login failed", JOptionPane.ERROR_MESSAGE);
		}
	}
	
}
