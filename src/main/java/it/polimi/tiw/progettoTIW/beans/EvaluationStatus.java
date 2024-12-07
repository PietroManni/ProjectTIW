package it.polimi.tiw.progettoTIW.beans;

public enum EvaluationStatus {
	NOT_INSERTED(0), INSERTED(1), PUBLISHED(2), REFUSED(3), VERBALIZED(4);
	
	private final int value;
	
	EvaluationStatus(int value) {
		this.value = value;
	}
	
	public static EvaluationStatus getEvaluationStatusFromInt(int value) {
		switch (value) {
		case 0:
			return EvaluationStatus.NOT_INSERTED;
		case 1:
			return EvaluationStatus.INSERTED;
		case 2:
			return EvaluationStatus.PUBLISHED;
		case 3:
			return EvaluationStatus.REFUSED;
		case 4:
			return EvaluationStatus.VERBALIZED;
		default:
			return null;
		}
	}
	
	public int getValue() {
		return value;
	}
}
