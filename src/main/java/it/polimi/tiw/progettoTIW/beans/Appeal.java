package it.polimi.tiw.progettoTIW.beans;

import java.util.Date;

public class Appeal {
	private int id;
	private Date date;
	private int courseId;
	private int verbalId;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public int getCourseId() {
		return courseId;
	}
	
	public void setCourseId(int courseId) {
		this.courseId = courseId;
	}
	
	public int getVerbalId() {
		return verbalId;
	}
	
	public void setVerbalId(int verbalId) {
		this.verbalId = verbalId;
	}
}
