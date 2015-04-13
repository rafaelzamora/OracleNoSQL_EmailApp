package email.init;

import DAO.MessageDAO;
import DAO.SequenceDAO;
import DAO.UserDAO;
import DAO.UserFolderDAO;
import DAO.UserFolderMessageDAO;

/**
 *  This class need to be run at the very beginning so that all the tables required to run this applications
 *  are created.
 * 
 *
 */
public class CreateSchema {

	private static UserDAO userDAO = new UserDAO();
	private static UserFolderDAO folderDAO = new UserFolderDAO();
	private static UserFolderMessageDAO folderMessageDAO = new UserFolderMessageDAO();
	private static SequenceDAO sequenceDAO = new SequenceDAO();
	private static MessageDAO messageDAO = new MessageDAO();
	
	public static void main(String[] args){
		
		//create all the tables and indexes first
		userDAO.createTable();
		folderDAO.createTable();
		folderMessageDAO.createTable();
		sequenceDAO.createTable();
		messageDAO.createTable();
	}//main
}
