package DAO;

import java.util.ArrayList;
import java.util.List;

import oracle.kv.FaultException;
import oracle.kv.Version;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.Table;

import constants.tables.Message;
import constants.tables.User;
import constants.tables.Folder;
import constants.tables.UserFolderMessage;
import exceptions.DAOException;
import email.to.MessageTO;
import email.util.StringUtil;

public class UserFolderMessageDAO extends BaseDAO {

	private Table messageTable = null;

	public UserFolderMessageDAO() {
		messageTable = getTable(UserFolderMessage.TABLE_NAME);

	}

	@Override
	/**
	 *  This method would create USER.FOLDER.MESSAGE table in the database.
	 */
	public void createTable() {
		String tableStr = "CREATE TABLE user.folder.message ("
								+ "messageId STRING, "
								+ "read BOOLEAN DEFAULT FALSE, "
								+ "PRIMARY KEY (messageId)"
							+ ")";
		
		
		try {
			//create table
			getTableAPI().executeSync(tableStr);
			
			System.out.println("USER.FOLDER.MESSAGE table created...");
		} catch (IllegalArgumentException e) {
			System.out.println("The statement is invalid: " + e);
		} catch (FaultException e) {
			System.out.println("There is a transient problem, retry the "
					+ "operation: " + e);
		}// try
		
	}//createTable
	protected boolean deleteUserMessage(String userId, String folderId,
			String messageId) throws DAOException {

		boolean status = false;
		PrimaryKey key = null;

		if (StringUtil.isNotEmpty(userId) && StringUtil.isNotEmpty(folderId)
				&& StringUtil.isNotEmpty(messageId)) {

			// Get unique row
			key = messageTable.createPrimaryKey();
			key.put(Message.MESSAGEID, messageId);
			key.put(User.USERID, userId);
			key.put(Folder.FOLDERID, folderId);

			status = getTableAPI().delete(key, null, null);
		}

		return status;
	}// deleteUserMessage

	protected String addUserMessage(String userId, String folderId,
			String messageId) throws DAOException {

		Version version = null;
		Row messageRow = null;
                //System.out.println("userId: " + userId + " folderId: " + folderId + " messageId: " + messageId);

		if (StringUtil.isNotEmpty(userId) && StringUtil.isNotEmpty(folderId)
				&& StringUtil.isNotEmpty(messageId)) {

			//System.out.println("userId: " + userId + " folderId: " + folderId + " messageId: " + messageId);
			
			messageRow = this.toUserMessageRow(userId, folderId, messageId);

			// check if conversion was successful
			if (messageRow != null) {

				// System.out.println("messageJSON: " +
				// messageRow.toJsonString(true));

				// Insert message profile into the database.
				version = getTableAPI().putIfAbsent(messageRow, null, null);

				if (version == null) {
					throw new DAOException("Message with messageId: "
							+ messageId + " in folder: " + folderId
							+ " already exist for the user: " + userId);
				}
			}// if(messageRow != null)
		}// if (version == null)// if(row !=null){

		return messageId;
	}// addUserMessage

	protected String moveUserMessage(String userId, String messageId,
			String fromFolder, String toFolder) throws DAOException {

		return null;
	}

	/**
	 * Method returns the list of messageIds when userId and folderId is passed as an input argument.
	 * In business sense when you need all the messages in a folder 'folderId' for user 'userId' you 
	 * would use this method.
	 * 
	 * @param userId
	 *            - unique key of User table
	 * @param folderName
	 *            - unique key of folder table
	 * @return List of MessageTO for a user by userId and in the folder by folderId
	 */
	public List<MessageTO> getMessageIds(String userId, String folderId) {

		List<MessageTO> messageTOs = new ArrayList<MessageTO>();

		List<Row> messages = null;
		String messageId = null;
		boolean read = false;
		MessageTO messageTO = null;

		// Get User by userId
		PrimaryKey key = messageTable.createPrimaryKey();
		key.put(User.USERID, userId);
		key.put(Folder.FOLDERID, folderId);

		messages = getTableAPI().multiGet(key, null, null);

		// convert row into categoryTO
		for (Row row : messages) {
			// System.out.println(row.toJsonString(false));
			messageId = row.get(Message.MESSAGEID).asString().get();
			read = row.get(UserFolderMessage.READ).asBoolean().get();

			// create a new MessageTO
			messageTO = new MessageTO();
			messageTO.setMessageId(messageId);
			messageTO.setRead(read);

			// now set messageTO to the list
			messageTOs.add(messageTO);
		}// for

		return messageTOs;

	}// getMessageIds

	private Row toUserMessageRow(String userId, String folderId,
			String messageId) {

		Row row = null;

		// Create User Row
		row = messageTable.createRow();

		row.put(Message.MESSAGEID, messageId);
		row.put(User.USERID, userId);
		row.put(Folder.FOLDERID, folderId);
		row.put(UserFolderMessage.READ, false);

		return row;

	} // toUserFolderMessageRow(messageTO)

}
