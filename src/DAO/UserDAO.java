package DAO;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import oracle.kv.FaultException;
import oracle.kv.Version;
import oracle.kv.table.Index;
import oracle.kv.table.IndexKey;
import oracle.kv.table.RecordValue;
import oracle.kv.table.Row;
import oracle.kv.table.Table;

import constants.Constant;
import constants.tables.User;
import exceptions.DAOException;
import email.to.UserTO;
import email.util.EncodeUtil;
import email.util.SequenceUtil;
import email.util.StringUtil;
import java.text.SimpleDateFormat;

public class UserDAO extends BaseDAO {

    private Table userTable = null;
    private UserFolderDAO folderDAO = null;

    @Override
    /**
     * This method would create USER table in the database.
     */
    public void createTable() {
        String tableStr = "CREATE TABLE user ("
                + "userId STRING, "
                + "name RECORD (first STRING, middle STRING, last STRING), "
                + "age INTEGER CHECK(age >=0 AND age<=200), "
                + "gender ENUM(M,F), "
                + "email STRING, "
                + "password STRING, "
                + "active BOOLEAN DEFAULT TRUE, "
                + "createdOn LONG, "
                + "modifiedOn LONG, "
                + "PRIMARY KEY (userId)"
                + ")";

        String emailIndex = "CREATE INDEX emailIndex ON user(email, password)";

        try {
            //create table
            getTableAPI().executeSync(tableStr);
            //create index
            getTableAPI().executeSync(emailIndex);

        } catch (IllegalArgumentException e) {
            System.out.println("The statement is invalid: " + e);
        } catch (FaultException e) {
            System.out.println("There is a transient problem, retry the "
                    + "operation: " + e);
        }// try

    }//createTable

    public UserDAO() {
        super();
        userTable = super.getTable(User.TABLE_NAME);
        folderDAO = new UserFolderDAO();
    }

    /*
     * Only one thread would be able to add user at any given time. This is to
     * make sure that no two threads create user profile with same email
     * address.
     * 
     * @param userTO this object contains all the information about user profile
     * 
     * @return email of the account created
     */
    public synchronized String addUser(UserTO userTO) throws DAOException {

        Long timestamp = new Date().getTime();
        Version version = null;
        Row userRow = null;
        String email = null;
        String userId = null;

        if (userTO != null) {

            // Create User Row from TO object
            userRow = this.toUserRow(userTO);

            // check if conversion was successful
            if (userRow != null) {

                email = this.getUniqueEmail(userTO.getFirst(),
                        userTO.getMiddle(), userTO.getLast());

                // get a unique userId
                userId = SequenceUtil.getNext(User.TABLE_NAME);

                // hopefully email is never returned null
                if (StringUtil.isNotEmpty(email)) {

                    Date now = new Date();
                    userTO.setEmail(email);
                    userRow.put(User.USERID, userId);
                    System.out.println(now.toString());
                    userRow.put(User.CREATEDON, now.toString());
                    userRow.put(User.MODIFIEDON, now.toString());
                    userRow.put(User.EMAIL, email);

					// Insert user profile into the database. Check no one has
                    // inserted the record with same email since we checked the
                    // last time
                    version = getTableAPI().putIfAbsent(userRow, null, null);

                    if (version == null) {
                        throw new DAOException("User with same : Key /'User/"
                                + userId + "' already exist in database. ");
                    } else {
						// For each new user create three default folders as
                        // well
                        folderDAO.addDefaultFolders(userId);
                    }// if(email != null)
                }
            }// if (version == null)// if(row !=null){
        } // EOF if

        return email;
    } // addUser

    /**
     * This method returns the user account details if email and password are
     * authenticated.
     *
     * @param email - User's email used while creating the account
     * @param password - User's account password
     * @return UserTO with all the details about the user
     *
     */
    public UserTO getUser(String email, String password) {

        UserTO userTO = null;
        // System.out.println("Search user by email: " + email);

        if (StringUtil.isNotEmpty(email) && StringUtil.isNotEmpty(password)) {

            userTO = this.getUserByEmail(email);

            // match the password and if don't match then null the userTO
            if (userTO != null && !password.equals(userTO.getPassword())) {
                userTO = null;
            }

        } else {
            System.out.println("Email and Password can not be empty");
        } // if(StringUtil.isNotEmpty(email))
        return userTO;
    } // getUser

    /**
     * This method returns User's profile for email argument passed
     *
     * @param email - email of the user
     * @return UserTO with everything about the user
     */
    protected UserTO getUserByEmail(String email) {

        Row row = null;
        UserTO userTO = null;
        Index index = null;
        IndexKey indexKey = null;

        if (StringUtil.isNotEmpty(email)) {

            // lower case the email
            email = email.trim().toLowerCase();

            /* get the index */
            index = userTable.getIndex(User.EMAIL_PSWD_INDEX);

            /* index is on email & password field */
            indexKey = index.createIndexKey();
            // Add only Email
            indexKey.put(User.EMAIL, email);

            // get all the rows that have same email and password
            Iterator<Row> results = getTableAPI().tableIterator(indexKey, null,
                    null);

            if (results != null && results.hasNext()) {

                // there should be only one row with email/password combination
                row = results.next();
                if (row != null) {
                    // System.out.println(row.toJsonString(false));
                    userTO = new UserTO(row);
                }
            } // if(results.hasNext())
        } // if(StringUtil.isNotEmpty(email))
        return userTO;
    } // getUserByEmail

    /**
     * Display all the users ordered by email address using email secondary
     * index.
     *
     * @return List of user TOs ordered by email IDs
     */
    public List<UserTO> getUsers() {

        List<UserTO> userList = new ArrayList<UserTO>();

        UserTO userTO = null;
        Row row = null;
        Index index = null;
        IndexKey indexKey = null;

        /* get the index */
        index = userTable.getIndex(User.EMAIL_PSWD_INDEX);


        /*
         * index is on "email" & "password" field but we are not going to set
         * the values to any of these fields which would mean display all the
         * users in ascending order of email ID.
         */
        indexKey = index.createIndexKey();

        // get all the rows that have same email and password
        Iterator<Row> results = getTableAPI().tableIterator(indexKey, null,
                null);

        while (results.hasNext()) {
            row = results.next();
            // System.out.println(row.toJsonString(true));
            userTO = new UserTO(row);
            // System.out.println(row.toJsonString(false));
            if (userTO != null) {
                userList.add(userTO);
            }
        } // for (Row row : categories)

        return userList;
    } // getSubCategories

    /**
     * This method is going to try to create unique email address using
     * first,middle & last name of the user. It will try 5 different combination
     * and will give up after that. In reality you will continue to try until a
     * unique address is created.
     *
     * @param first First name of the User
     * @param middle Middle name of the User
     * @param last Last Name of the User
     * @return email address that can be used
     */
    private String getUniqueEmail(String first, String middle, String last) {

        String email = null;
        int len = 0;
        UserTO userTO = null;

        if (StringUtil.isNotEmpty(first) && StringUtil.isNotEmpty(last)) {
            email = first.toLowerCase() + "." + last.toLowerCase()
                    + User.DOMAIN;

            userTO = this.getUserByEmail(email);

            // Make sure email doesn't exist already
            if (userTO != null) {
				// first attempt failed now lets try to use middle name in the
                // email
                middle = StringUtil.isEmpty(middle) ? User.ALTERNATE_MIDDLE
                        : middle + User.ALTERNATE_MIDDLE;
                len = middle.length();

                for (int i = 0; i < len; i++) {
                    // now try with middle name as part of the email
                    email = first.toLowerCase() + "."
                            + middle.substring(i, i + 1).toLowerCase() + "."
                            + last.toLowerCase() + User.DOMAIN;

					// System.out.println("source email: " + email);
                    userTO = this.getUserByEmail(email);

                    if (userTO == null) // email is unique
                    {
                        break;
                    } else {
                        email = null;
                    }// if(row==null)

                }// while
            }// EOF if(row!=null)
        }// EOF if

        return email;
    }// getUniqueEmail

    private Row toUserRow(UserTO userTO) {

        Row row = null;

        if (userTO != null) { //&& StringUtil.isNotEmpty(userTO.getGender())) {

            // Create User Row
            row = userTable.createRow();

            row.put(User.PASSWORD, userTO.getPassword());
			//row.putEnum(User.GENDER, userTO.getGender().toUpperCase());
            //row.put(User.AGE, userTO.getAge());
            //row.put(User.CREATEDON, userTO.getCreatedOn());
            //row.put(User.MODIFIEDON, userTO.getModifiedOn());

            // User name
            RecordValue name = row.putRecord(User.NAME);
            name.put(User.FIRST, userTO.getFirst());
            name.put(User.MIDDLE, userTO.getMiddle());
            name.put(User.LAST, userTO.getLast());

            // if email is not set then it is a brand new account
            if (StringUtil.isNotEmpty(userTO.getEmail())) {
                // lower case the email as that is case insensitive
                row.put(User.EMAIL, userTO.getEmail());
                row.put(User.ACTIVE, userTO.isActive());
            }

        } // if (userTO != null) {

        return row;

    } // toUserRow(userTO)

    /**
     * This method takes JSON string representing User table and writes it into
     * the database
     *
     * @param userJSON - User can pass a JSON string representing User row. You
     * can pass partial JSON string ie. not setting all the fields present in
     * the user table.
     * @return UserTO - Returns UsertTO with everything that was being passed
     * plus system created values like userId, createdOn dates etc.
     * @throws DAOException is thrown if user already exist with same userId
     */
    public UserTO addUser(String userJSON) throws DAOException {
        Row userRow = null;
        Version version = null;
        UserTO userTO = null;

        if (StringUtil.isNotEmpty(userJSON)) {
            userRow = userTable.createRowFromJson(userJSON, false);

			// Insert user profile into the database. Check no one has
            // inserted the record with same email since we checked the
            // last time
            version = getTableAPI().putIfAbsent(userRow, null, null);

            if (version == null) {
                throw new DAOException(
                        "User with same : Key already exist in database. ");
            } else {
				// System.out.println("Successfully inserted user with email: "+ email);
                // For each new user create three default folders as well
                userTO = new UserTO(userRow);
                folderDAO.addDefaultFolders(userTO.getUserId());
                System.out
                        .println("Email account: '"
                                + userTO.getEmail()
                                + "' has been created successfully. Please use this email/password to login to your account.");
            }// if(email != null)
        }
        return userTO;
    }

    public static void main(String[] args) {
        // System.out.println("Adding User..");

        UserTO userTO = new UserTO();

        UserDAO userDAO = new UserDAO();
        userTO.setPassword("welcome1");
        userTO.setActive(true);

        // add name details
        userTO.setFirst("Bob");
        userTO.setMiddle("");
        userTO.setLast("Smith");
        userTO.setGender(Constant.MALE);
        userTO.setAge(25);

        try {

			//create table first
            //userDAO.createTable();
			//System.exit(1);
			// insert user JSON
            //userDAO.addUser(userJSON);
            // insert user
            userDAO.addUser(userTO);

			// get user by email and password
            //userTO = userDAO.getUser("bob.smith@email.com", "welcome1");
            System.out.println("User JSON: \n:" + userTO.toString());

			// get all users
            //userDAO.getUsers();
        } catch (DAOException e) {
            e.printStackTrace();
        }
    }// main

}
