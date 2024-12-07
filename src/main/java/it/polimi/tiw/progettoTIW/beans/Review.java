package it.polimi.tiw.progettoTIW.beans;

public class Review {
	
	private int id;
	private String review;
	private Integer vote;
	private int courseId;
	private Student student;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getReview() {
		return review;
	}
	
	public void setReview(String review) {
		this.review = review;
	}
	
	public Integer getVote() {
		return vote;
	}
	
	public void setVote(Integer vote) {
		this.vote = vote;
	}
	
	public int getCourseId() {
		return courseId;
	}
	
	public void setCourseId(int courseId) {
		this.courseId = courseId;
	}
	
	public Student getStudent() {
		return student;
	}
	
	public void setStudent(Student student) {
		this.student = student;
	}

}
