package de.nitri.aptimob;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author helfrich
 */
public class Message implements Serializable {
    
	private static final long serialVersionUID = 1L;
	
	private int id;
    private int refId;
    private int domain;
    private List<Integer> userIds;
    private int senderId;
    private String to;
    private String from;
    private String subject;
    private String body;
    private long timestamp;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the refId
     */
    public int getRefId() {
        return refId;
    }

    /**
     * @param refId the refId to set
     */
    public void setRefId(int refId) {
        this.refId = refId;
    }

    /**
     * @return the userIds
     */
    public List<Integer> getUserIds() {
        return userIds;
    }

    /**
     * @param userIds the userIds to set
     */
    public void setUserIds(List<Integer> userIds) {
        this.userIds = userIds;
    }

    /**
     * @return the senderId
     */
    public int getSenderId() {
        return senderId;
    }

    /**
     * @param senderId the senderId to set
     */
    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    /**
     * @return the to
     */
    public String getTo() {
        return to;
    }

    /**
     * @param to the to to set
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * @return the from
     */
    public String getFrom() {
        return from;
    }

    /**
     * @param from the from to set
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * @param subject the subject to set
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * @param body the body to set
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the domain
     */
    public int getDomain() {
        return domain;
    }

    /**
     * @param domain the domain to set
     */
    public void setDomain(int domain) {
        this.domain = domain;
    }
    
}
