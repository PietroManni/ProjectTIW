package it.polimi.tiw.progettoTIW.beans;

public enum MarkStatus {
	EMPTY(0), ABSENT(1), DEFERRED(2), REPROVED(3), EVALUATED(4);
	
	private final int value;
	
	MarkStatus(int value) {
		this.value = value;
	}
	
	public static MarkStatus getMarkStatusFromInt(int value) {
		switch(value) {
		case 0:
			return MarkStatus.EMPTY;
		case 1:
			return MarkStatus.ABSENT;
		case 2:
			return MarkStatus.DEFERRED;
		case 3:
			return MarkStatus.REPROVED;
		case 4:
			return MarkStatus.EVALUATED;
		default:
			return null;
		}
	}
	
	public int getValue() {
		return value;
	}
}
