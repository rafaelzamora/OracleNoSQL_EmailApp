package DAO;

import java.util.ArrayList;
import java.util.List;

import oracle.kv.FaultException;
import oracle.kv.Version;
import oracle.kv.table.PrimaryKey;
import oracle.kv.table.Row;
import oracle.kv.table.Table;

import constants.tables.Folder;
import constants.tables.User;
import static DAO.BaseDAO.getTable;
import static DAO.BaseDAO.getTableAPI;
import exceptions.DAOException;
import email.to.FolderTO;
import email.util.SequenceUtil;
import email.util.StringUtil;
import java.util.Iterator;
import oracle.kv.table.Index;
import oracle.kv.table.IndexKey;

public class UserFolderDAO extends BaseDAO {
	private Table folderTable = null;

	public UserFolderDAO() {
		super();
		folderTable = getTable(Folder.TABLE_NAME);
	}

	@Override
	/**
	 *  This method would create USER table in the database.
	 */
	public void createTable() {
		String tableStr = "CREATE TABLE user.folder ("								
								+ "name STRING, "
								+ "PRIMARY KEY (name)"
							+ ")";
		
		
		try {
			//create table
			getTableAPI().executeSync(tableStr);
			
			System.out.println("USER.FOLDER table created...");
		} catch (IllegalArgumentException e) {
			System.out.println("The statement is invalid: " + e);
		} catch (FaultException e) {
			System.out.println("There is a transient problem, retry the "
					+ "operation: " + e);
		}// try
		
	}//createTable
	
	/**
	 * This method add folders for a userId. 
	 * @param userId - Unique Id of the user
	 * @param folderNames - List of folder names (like inbox, sent, trash etc)
	 * @return true if operation is successful otherwise false.
	 * @throws DAOException when folder by same name already exist.
	 */
	public boolean addFolders(String userId, List<String> folderNames)
			throws DAOException {

		Row folderRow = null;
		boolean status = false;
		FolderTO folderTO = null;
		Version version = null;
		//String folderId = null;

		if (StringUtil.isNotEmpty(userId) && folderNames != null) {
			
			//Create folders for user by iterating through the folderNames list
			for (String folderName : folderNames) {
				//System.out.println(folderName);
				if (StringUtil.isNotEmpty(folderName)) {

                                    String folderId = SequenceUtil.getNext(Folder.TABLE_NAME);

					// create new folderTO
					folderTO = new FolderTO();
					folderTO.setUserId(userId);
                                        folderTO.setFolderId(folderId);
					folderTO.setName(folderName);
					
					folderRow = this.toFolderRow(folderTO);

					System.out.println(folderRow.toJsonString(false));
                                        
                                        System.out.println("hola");
					// Insert Folders for a user.
					version = getTableAPI().putIfAbsent(folderRow, null, null);
                                        

					if (version == null) {
						throw new DAOException(
								"Failed to add folder into User.Folder table");
					} else {
						System.out.println("Successfully inserted row in User.Folder Table with name: " + folderName);
						status = true;
					}// if(version == null)

				}// if (StringUtil.isNotEmpty(folderName)

			}// for

		} // if()

		return status;
	} // addFolders

	protected boolean addDefaultFolders(String userId) throws DAOException {
		List<String> folderNames = new ArrayList<String>();
		boolean status = false;

		folderNames.add(Folder.INBOX);
		folderNames.add(Folder.SENT);
		folderNames.add(Folder.TRASH);

		status = addFolders(userId, folderNames);
		return status;
	}// getDefaultFolders

	
	
	/**
	 * Get all the folders for a UserId
	 * @param userId - unique id of User table
	 * @return List of FolderTO
	 */
	public List<FolderTO> getFolders(String userId){
	
	        FolderTO folderTO = null;
	        List<FolderTO> folderList = new ArrayList<FolderTO>();
	        		
	        List<Row> folders = null;

	        //Get User by userId
	        PrimaryKey key = folderTable.createPrimaryKey();
	        key.put(User.USERID, userId);
	        folders = getTableAPI().multiGet(key, null, null);

	        //convert row into folderTO
	        for(Row row: folders){
	        	//System.out.println(row.toJsonString(false));
	        	folderTO = this.toFolderTO(row);
	        	folderList.add(folderTO);	
	        }//for

	        return folderList;
	    
	}//getFolders

	private Row toFolderRow(FolderTO folderTO) {

		Row row = null;

		if (folderTO != null) {

			// Create User Row
			row = folderTable.createRow();

			// lower case the email as that is case insensitive
			row.put(User.USERID, folderTO.getUserId());
			row.put(Folder.NAME, folderTO.getName());
                        row.put(Folder.FOLDERID, folderTO.getFolderId());
			
		} // if (folderTO != null) {

		return row;

	} // toFolderRow(folderTO)
	
	/**
	 * Converts Folder Row object in FolderTO object
	 * @param row - Folder Row
	 * @return FolderTO
	 */
	private FolderTO toFolderTO(Row row) {

		FolderTO folderTO = null;

		// Extract user information
		if (row != null) {

			folderTO = new FolderTO();
			// Now add field values			
			folderTO.setName(row.get(Folder.NAME).asString().get());
			folderTO.setUserId(row.get(User.USERID).asString().get());
			

		} // if (row != null)

		return folderTO;
	} // toFolderTO

	public static void main(String[] args) {

		UserFolderDAO folderDAO = new UserFolderDAO();
		
		//folderDAO.createTable();
		
		//folderDAO.getFolders("l");
		//End main method
		System.exit(1);
		
		try {
			folderDAO.addDefaultFolders("1");
			
		} catch (DAOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// try
	}// main

    String getFolderId(String userId, String folderName) {
            FolderTO folderTO = null;
	        Row row;		

            //Get folder by userId and foldername
            Index index = folderTable.getIndex(Folder.FOLDER_INDEX);

            /* index is on email & password field */
            IndexKey key = index.createIndexKey();
	        key.put(User.USERID, userId);
                key.put(Folder.NAME, folderName);
                
	        // get all the rows that have same email and password
                Iterator<Row> results = getTableAPI().tableIterator(key, null,
					null);
                if (results != null && results.hasNext()) {
                    System.out.println("hola");
				row = results.next();
				if (row != null) {
					// System.out.println(row.toJsonString(false));
					folderTO = this.toFolderTO(row);
				}
			} // if(results.hasNext())

	        return folderTO.getFolderId();      
        
    }

	

}
