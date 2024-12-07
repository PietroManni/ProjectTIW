package it.polimi.tiw.progettoTIW.beans;

public class Evaluation {
	private Integer mark;
	private boolean laud;
	private MarkStatus markStatus;
	private EvaluationStatus evaluationStatus;
	
	public Integer getMark() {
		return mark;
	}
	
	public void setMark(Integer mark) {
		this.mark = mark;
	}
	
	public boolean getLaud() {
		return laud;
	}
	
	public void setLaud(boolean laud) {
		this.laud = laud;
	}
	
	public MarkStatus getMarkStatus() {
		return markStatus;
	}
	
	public void setMarkStatus(MarkStatus markStatus) {
		this.markStatus = markStatus;
	}
	
	public void setMarkStatus(int value) {
		this.markStatus = MarkStatus.getMarkStatusFromInt(value);
	}
	
	public EvaluationStatus getEvaluationStatus() {
		return evaluationStatus;
	}
	
	public void setEvaluationStatus(EvaluationStatus evaluationStatus) {
		this.evaluationStatus = evaluationStatus;
	}
	
	public void setEvaluationStatus(int value) {
		this.evaluationStatus = EvaluationStatus.getEvaluationStatusFromInt(value);
	}
}
