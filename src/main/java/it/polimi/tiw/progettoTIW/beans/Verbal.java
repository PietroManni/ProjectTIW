package it.polimi.tiw.progettoTIW.beans;

import java.util.Date;

public class Verbal {

	private int id;
	private Date date;
	private int appealId;
	
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
	
	public int getAppealId() {
		return appealId;
	}
	
	public void setAppealId(int appealId) {
		this.appealId = appealId;
	}
}
