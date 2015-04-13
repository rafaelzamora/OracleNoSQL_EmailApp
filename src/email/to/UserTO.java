package email.to;

import oracle.kv.table.RecordValue;
import oracle.kv.table.Row;

import constants.tables.User;
import email.util.StringUtil;

public class UserTO {

	private String userId = null;
	private String email = null;
	private String first = null;
	private String middle = null;
	private String last = null;
	private String password = null;
	private int age = 0;
	private String createdOn = null;
	private String modifiedOn = null;
	private boolean active = true;
	private String gender = null;

	public UserTO() {
		
	}
	
	public UserTO(Row row) {

		// Extract user information
		if (row != null) {

			// Now add field values
			this.setEmail(row.get(User.EMAIL).asString().get());
			this.setPassword(row.get(User.PASSWORD).asString().get());
			this.setActive(row.get(User.ACTIVE).asBoolean().get());
			this.setCreatedOn(row.get(User.CREATEDON).asString().get());
			this.setUserId(row.get(User.USERID).asString().get());
			//this.setGender(row.get(User.GENDER).asEnum().get());
			//this.setAge(row.get(User.AGE).asInteger().get());

			RecordValue name = row.get(User.NAME).asRecord();
			this.setFirst(name.get(User.FIRST).asString().get());
			this.setMiddle(name.get(User.MIDDLE).asString().get());
			this.setLast(name.get(User.LAST).asString().get());

		} // if (row != null)

	} // UserTO

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFirst() {
		return first;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public String getMiddle() {
		return middle;
	}

	public void setMiddle(String middle) {
		this.middle = middle;
	}

	public String getLast() {
		return last;
	}

	public void setLast(String last) {
		this.last = last;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(String createdOn) {
		this.createdOn = createdOn;
	}

	public String getModifiedOn() {
		return modifiedOn;
	}

	public void setModifiedOn(String modifiedOn) {
		this.modifiedOn = modifiedOn;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public int getAge() {
		return age;
	}

	// TODO - helper method.
	public String getAgeStr() {
		return String.valueOf(age);
	}

	public void setAge(int age) {
		this.age = age;
	}

	public void setAge(String age) {
		if(StringUtil.isNotEmpty(age))
			this.age = Integer.parseInt(age);
	}

	public String toString() {
		String str = null;
		str = this.getUserId() + "," + this.getEmail() + "," + this.age + "," + this.getFirst() + ","
				+ this.getMiddle() + "," + this.getLast() + ","
				+ this.getGender() + "," + this.getPassword();
		return str;

	}

}
