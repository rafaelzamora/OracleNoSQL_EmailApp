package email.to;

import email.util.StringUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javax.print.attribute.Size2DSyntax.MM;

public class MessageTO implements Comparable<MessageTO>{

	private String messageId = "";
	private String userId = "";
	private String folderName = "";
	private String to = "";
	private String from = "";
	private String cc = "";
	private String bcc = "";
	private String subject = "";
	private boolean read = false;
	
	private byte[] body = null;
	private byte[] attachment = null;
	private String createdOn = null;
	private String modifiedOn = null;

	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	
	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	
	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getCc() {
		return StringUtil.isNotEmpty(cc)?cc:"";
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getBcc() {
		return StringUtil.isNotEmpty(bcc)?bcc:"";
	}

	public void setBcc(String bcc) {
		this.bcc = bcc;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}

	public byte[] getAttachment() {
		return attachment;
	}

	public void setAttachment(byte[] attachment) {
		this.attachment = attachment;
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

	public String getDateString(){
		return this.getCreatedOn();
	}
	
	@Override
	public  int compareTo(MessageTO messageTO) {
            SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

	Date createDate; 
        Date createOn;
        Long createDateMs = null;
        Long createOnMs = null ;
            try {
                createDate = dateFormatter.parse(messageTO.getCreatedOn());
                createDateMs = createDate.getTime();
                createOn = dateFormatter.parse(this.createdOn);
                createOnMs = createOn.getTime();
            } catch (ParseException ex) {
                Logger.getLogger(MessageTO.class.getName()).log(Level.SEVERE, null, ex);
            }
		
		//descending order
		return (int)(createDateMs - createOnMs);
		
	}

}
