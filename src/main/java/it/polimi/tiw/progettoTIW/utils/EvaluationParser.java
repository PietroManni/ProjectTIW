package it.polimi.tiw.progettoTIW.utils;

import it.polimi.tiw.progettoTIW.beans.Evaluation;
import it.polimi.tiw.progettoTIW.beans.EvaluationStatus;
import it.polimi.tiw.progettoTIW.beans.MarkStatus;
import it.polimi.tiw.progettoTIW.exceptions.InvalidMarkFormatException;

public class EvaluationParser {

	public static Evaluation parseMark(int mark) throws InvalidMarkFormatException {
		Evaluation evaluation = new Evaluation();
		if (mark > 31 || mark < 0 || (17 >= mark && mark >= 4)) {
			throw new InvalidMarkFormatException("Mark format invalid");
		}
		if (18 <= mark && mark <= 31) {
			evaluation.setLaud(mark - 30 > 0);
			evaluation.setMarkStatus(MarkStatus.EVALUATED);
			evaluation.setMark((evaluation.getLaud()) ? (mark - 1) : mark);
		} else if (0 <= mark && mark <= 3) {
			evaluation.setMarkStatus(mark);
		} else {
			throw new InvalidMarkFormatException("Mark format invalid");
		}
		
		evaluation.setEvaluationStatus(EvaluationStatus.INSERTED);
		return evaluation;
	}
}