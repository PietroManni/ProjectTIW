package it.polimi.tiw.progettoTIW.beans;

public class Exam {

	private int id;
	private int appealId;
	private Student student;
	private Evaluation evaluation;
	
	public Student getStudent() {
		return student;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getAppealId() {
		return appealId;
	}
	
	public void setAppealId(int appealId) {
		this.appealId = appealId;
	}
	
	public void setStudent(Student student) {
		this.student = student;
	}
	
	public Evaluation getEvaluation() {
		return evaluation;
	}
	
	public void setEvaluation(Evaluation evaluation) {
		this.evaluation = evaluation;
	}
}
