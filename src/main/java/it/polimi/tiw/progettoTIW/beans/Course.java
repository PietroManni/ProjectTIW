package it.polimi.tiw.progettoTIW.beans;

public class Course {
	private int id;
	private String name;
	private String description;
	private int hours;
	private int cfu;
	private int teacherId;
	private Teacher teacher;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public int getHours() {
		return hours;
	}
	
	public void setHours(int hours) {
		this.hours = hours;
	}
	
	public int getCfu() {
		return cfu;
	}
	
	public void setCfu(int cfu) {
		this.cfu = cfu;
	}
	
	public int getTeacherId() {
		return teacherId;
	}
	
	public void setTeacherId(int teacherId) {
		this.teacherId = teacherId;
	}
	
	public Teacher getTeacher() {
		return teacher;
	}
	
	public void setTeacher(Teacher teacher) {
		this.teacher = teacher;
	}
}
