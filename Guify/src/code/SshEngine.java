package code;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import org.apache.commons.io.IOUtils;
import com.jcraft.jsch.*;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import controllers.LoginController;
import java.nio.file.*;

/**
 * 
 * Underlying SSH engine for
 * the application
 *
 */
public class SshEngine {
	
	private static Session session = null;
	
	/*
	 * ========== BEGIN SSH Utilities ==========
	 */
	
	public static boolean connetion() {
		return createSession();
	}
	
	private static boolean createSession() {
		Properties config = new Properties();
		config.put("StrictHostKeyChecking", "no");
		JSch jsch = new JSch();

		try {
			session = jsch.getSession(LoginController.LoginCredentials.username, LoginController.LoginCredentials.host,
					LoginController.LoginCredentials.port);
			session.setPassword(LoginController.LoginCredentials.password);
			session.setConfig(config);
			session.setTimeout(5000); // 5 seconds
			session.connect();
			System.out.println("Connected to remote server");
		} catch (JSchException e) {
			session = null;
			return false;
		}
		return true;
	}

	private static void checkValidityOrCreateSession() throws JSchException {		
		if(session == null || !session.isConnected()) {
			if(!createSession()) {
				throw new JSchException("Failed to create a session in Jsch");
			}
		}
	}

	public static void disconnectSession() {
		if(session != null) {
			session.disconnect();
		}
	}
	
	/*
	 * ========== END SSH Utilities ==========
	 */
	

	/*
	 * ========== BEGIN SFTP get() and put() methods ==========
	 */

	/**
	 * Downloads a file from remote host to local machine.
	 * Executed asynchronously.
	 */
	public static void downloadFile(String source, String dest) {
		// We execute the lengthy and time-consuming operation on a different
		// thread instead of the Event Dispatch Thread.
		// We use SwingWorker so any GUI changes requested by this thread will
		// be correctly addressed to the Event Dispatch Thread
		new Thread(new Runnable() {
		    public void run() {
		    	ChannelSftp channelSftp = null;
				try {
					checkValidityOrCreateSession();
					channelSftp = (ChannelSftp) session.openChannel("sftp");
					channelSftp.connect();
					channelSftp.get(source, dest, new GuifySftpProgressMonitor());
					System.out.println("File " + source + " downloaded in " + dest);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (channelSftp != null)
						channelSftp.disconnect();
				}
		    }
		}).start();
	}
	
	/**
	 * Downloads a directory recursively.
	 * Executed asynchronously.
	 */
	public static void downloadDirectoryRecursively(String source, String dest) {
		// We execute the lengthy and time-consuming operation on a different
		// thread instead of the Event Dispatch Thread.
		// We use SwingWorker so any GUI changes requested by this thread will
		// be correctly addressed to the Event Dispatch Thread
		new Thread(new Runnable() {
		    public void run() {
		    	ChannelSftp channelSftp = null;
				try {
					channelSftp = (ChannelSftp) session.openChannel("sftp");
					channelSftp.connect();
					downloadDirectoryRecursively_aux(channelSftp, source, dest);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (channelSftp != null)
						channelSftp.disconnect();
				}
		    }
		}).start();
	}
	
	/**
	 * Private utility
	 * @param channel_aux An auxiliary SFTP channel
	 * @param remoteDirectory
	 * @param localDirectory
	 * @throws SftpException
	 */
    private static void downloadDirectoryRecursively_aux(ChannelSftp channel_aux, String remoteDirectory, String localDirectory) throws SftpException {
    	channel_aux.cd(remoteDirectory);
        String newLocalDir = Helper.combinePath(localDirectory, Paths.get(remoteDirectory).getFileName().toString());
        new java.io.File(newLocalDir).mkdirs();
        @SuppressWarnings("unchecked")
		Vector<LsEntry> entries = channel_aux.ls("*");
        
        for (ChannelSftp.LsEntry entry : entries) {
            if (!entry.getAttrs().isDir()) {
                // File - download it
            	// Creates a thread for each file. If there are a lot of files
            	// it may be resource-draining. Consider using a ThreadPool
		    	downloadFile(Helper.combinePath(remoteDirectory,  entry.getFilename()), Helper.combinePath(newLocalDir, entry.getFilename()));
            } else if (!".".equals(entry.getFilename()) && !"..".equals(entry.getFilename())) {
            	// Directory - download recursively
                String newRemoteDir = Helper.combinePath(remoteDirectory, entry.getFilename());
                downloadDirectoryRecursively_aux(channel_aux, newRemoteDir, newLocalDir);
            }
        }
    }
	
	/**
	 * Uploads a file from the local machine to the remote host.
	 * Executed asynchronously.
	 */
	public static void uploadFile(File fileToUpload, String remoteDirectory) throws SftpException {
		// We execute the lengthy and time-consuming operation on a different
		// thread instead of the Event Dispatch Thread.
		// We use SwingWorker so any GUI changes requested by this thread will
		// be correctly addressed to the Event Dispatch Thread
		new Thread(new Runnable() {
		    public void run() {
		    	ChannelSftp channelSftp = null;
				String remotePath = null;
				try {
					checkValidityOrCreateSession();
					channelSftp = (ChannelSftp) session.openChannel("sftp");
					channelSftp.connect();
					remotePath = Helper.combinePath(remoteDirectory, fileToUpload.getName());
					channelSftp.put(fileToUpload.getAbsolutePath(), remotePath, new GuifySftpProgressMonitor());
					System.out.println("File: " + fileToUpload.getAbsolutePath() + " uploaded to remote path: " + remotePath);
				}
				catch(SftpException sftpex) {
					// TODO maybe no permissions
				}
				catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (channelSftp != null)
						channelSftp.disconnect();
				}
		    }
		}).start();
	}
	
	/**
	 * Uploads directory recursively.
	 * Executed asynchronously.
	 * @param directory Full path of the local directory to upload
	 * @param remoteDirectory Full path of the remote directory which the local
	 * directory will be uploaded in
	 */
	public static void uploadDirectoriesRecursively(File directory, String remoteDirectory) throws SftpException {
		// We execute the lengthy and time-consuming operation on a different
		// thread instead of the Event Dispatch Thread.
		// We use SwingWorker so any GUI changes requested by this thread will
		// be correctly addressed to the Event Dispatch Thread
		new Thread(new Runnable() {
		    public void run() {
				ChannelSftp channelSftp = null;
				try {
					checkValidityOrCreateSession();
					channelSftp = (ChannelSftp) session.openChannel("sftp");
					channelSftp.connect();
					uploadDirectoriesRecursively_aux(channelSftp, directory, remoteDirectory);
				} 
				catch(SftpException sftpex) {
					//TODO maybe no permissions
				}
				catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (channelSftp != null)
						channelSftp.disconnect();
				}
		    }
		}).start();
	}
	
	/**
	 * Private utility
	 * @param channel_aux
	 * @param localPath
	 * @param remoteDirectory
	 * @throws SftpException
	 */
    private static void uploadDirectoriesRecursively_aux(ChannelSftp channel_aux, File localPath, String remoteDirectory) throws SftpException {
    	if(localPath != null) {
    		String subDirectoryPath = Helper.combinePath(remoteDirectory, localPath.getName());
    		channel_aux.mkdir(subDirectoryPath);
            
            File[] files = localPath.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                    	// Creates a thread for each file. If there are a lot of files
                    	// it may be resource-draining. Consider using a ThreadPool
                    	channel_aux.put(file.getAbsolutePath(), Helper.combinePath(subDirectoryPath, file.getName()), new GuifySftpProgressMonitor());
                        System.out.println("File: " + file.getAbsolutePath() + " uploaded to remote path: " +  Helper.combinePath(subDirectoryPath, file.getName()));
                    } else if (file.isDirectory()) {
                    	uploadDirectoriesRecursively_aux(channel_aux, file, subDirectoryPath);
                    }
                }
            }
    	}
    }
    
	/*
	 * ========== END SFTP get() and put() methods ==========
	 */
    
    
	/*
	 * ========== BEGIN File System operations ==========
	 */
    
	public static void mkdir(String path) throws SftpException {
		ChannelSftp channelSftp = null;
		try {
			checkValidityOrCreateSession();
			channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();
			channelSftp.mkdir(path);
		}
		catch(SftpException sftpex) {
				throw sftpex;
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (channelSftp != null)
				channelSftp.disconnect();
		}
	}
	
	public static String readFile(String filePath) {
		ChannelSftp channel = null;
		try {
			checkValidityOrCreateSession();
			channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect();
			InputStream in = channel.get(filePath);
			return IOUtils.toString(in, StandardCharsets.UTF_8);
		} catch (Exception e) {
			return null;
		} finally {
			if (channel != null)
				channel.disconnect();
		}
	}
	
	public static void writeFile(String content, String pathToFile) {
		ChannelSftp channelSftp = null;
		try {
			checkValidityOrCreateSession();
			channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();
			channelSftp.put(new ByteArrayInputStream(content.getBytes()), pathToFile);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (channelSftp != null)
				channelSftp.disconnect();
		}
	}
	
	public static void rename(String oldPath, String newPath) throws SftpException {
		ChannelSftp channelSftp = null;
		try {
			checkValidityOrCreateSession();
			channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();
			channelSftp.rename(oldPath, newPath);
		} 
		catch(SftpException sftpex) {
			throw sftpex;
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (channelSftp != null)
				channelSftp.disconnect();
		}
	}
	
	/**
	 * Creates an empty file in the specified remote file path
	 * @throws SftpException 
	 */
	public static void touch(String remoteFilePath) throws SftpException {
		ChannelSftp channelSftp = null;
		try {
			checkValidityOrCreateSession();
			channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();
			channelSftp.put(new ByteArrayInputStream(new byte[0]), remoteFilePath);
		} 
		catch(SftpException sftpex) {
			throw sftpex;
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (channelSftp != null)
				channelSftp.disconnect();
		}
	}
	
	public static void rm(String remoteFilePath) throws SftpException {
		ChannelSftp channelSftp = null;
		try {
			checkValidityOrCreateSession();
			channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();
			channelSftp.rm(remoteFilePath);
		} 
		catch(SftpException sftpex) {
			throw sftpex;
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (channelSftp != null)
				channelSftp.disconnect();
		}
	}
	
	public static void rm(List<String> remoteFilePaths) throws SftpException {
		ChannelSftp channelSftp = null;
		try {
			checkValidityOrCreateSession();
			channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();
			for(String remoteFilePath : remoteFilePaths) {
				rm(remoteFilePath, channelSftp);
			}
		} 
		catch(SftpException sftpex) {
			throw sftpex;
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (channelSftp != null)
				channelSftp.disconnect();
		}
	}
	
	private static void rm(String remoteFilePath, ChannelSftp channelSftp) throws SftpException, JSchException {
		channelSftp.rm(remoteFilePath);
	}
	
	public static void rmdir(String remoteFilePath) throws SftpException {
		ChannelSftp channelSftp = null;
		try {
			checkValidityOrCreateSession();
			channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();
			channelSftp.rmdir(remoteFilePath);
		} 
		catch(SftpException sftpex) {
			throw sftpex;
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (channelSftp != null)
				channelSftp.disconnect();
		}
	}
	
	public static void rmdir(List<String> remotePaths) throws SftpException {
		ChannelSftp channelSftp = null;
		try {
			checkValidityOrCreateSession();
			channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();
			for(String remotePath : remotePaths) {
				rmdir(remotePath, channelSftp);
			}
		} 
		catch(SftpException sftpex) {
			throw sftpex;
		}
		catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (channelSftp != null)
				channelSftp.disconnect();
		}
	}
	
	private static void rmdir(String remotePath, ChannelSftp channelSftp) throws SftpException, JSchException {
		channelSftp.rmdir(remotePath);
	}
	
	@SuppressWarnings("unchecked")
	public static Vector<ChannelSftp.LsEntry> ls(String path) throws SftpException{
		ChannelSftp channelSftp = null;
		try {
			checkValidityOrCreateSession();
			channelSftp = (ChannelSftp) session.openChannel("sftp");
			channelSftp.connect();
			Vector<ChannelSftp.LsEntry> entries = channelSftp.ls(path);
			// remove hidden directories (TODO create a setting for that)
			entries.removeIf(entry -> entry.getFilename().equals(".") || entry.getFilename().equals("..") || entry.getFilename().startsWith("."));
			return entries;
		}
		catch(SftpException sftpex) {
			throw sftpex;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (channelSftp != null)
				channelSftp.disconnect();
		}
	}
	
	/*
	 * ========== END File System operations ==========
	 */
	
	
	/*
	 * ========== BEGIN Other ==========
	 */
	
	public static String executeCommand(String command) {
		System.out.println("> " + command);
		Channel channel = null;
		InputStream in = null;
		try {
			checkValidityOrCreateSession();
			channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);
			channel.connect();
			in = channel.getInputStream();
			String returnText = new String(in.readAllBytes(), StandardCharsets.UTF_8);
			in.close();
			// Remove possible \r\n at the end of the string
			returnText = returnText.replaceAll("[\r\n]+$", "").trim();
			System.out.println("< " + returnText);
			return returnText;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSchException e) {
			e.printStackTrace();
		} finally {
			if (channel != null) {
				channel.disconnect();
			}
		}
		return null;
	}
	
	/*
	 * ========== END Other ==========
	 */
	
	
}
/*
 * When executing a command on a remote server using SSH with Jsch in Java, we
 * typically want to capture the output of the command as it is executed on the
 * remote server. Therefore, we need to obtain an input stream from the remote
 * server that will allow us to read the output of the command.
 * 
 * The channel.getInputStream() method returns an input stream that is connected
 * to the standard output of the remote command being executed. This means that
 * any output produced by the command will be sent to the input stream, which we
 * can then read in our Java program to obtain the output.
 * 
 * On the other hand, the channel.getOutputStream() method returns an output
 * stream that is connected to the standard input of the remote command being
 * executed. This means that any input provided to the output stream will be
 * sent to the remote command as its standard input. While this may be useful in
 * some cases, it is not typically what we want when executing a command on a
 * remote server and capturing its output.
 * 
 * So to sum up, we use channel.getInputStream() to obtain an input stream that
 * we can use to read the output of the command being executed on the remote
 * server.
 */