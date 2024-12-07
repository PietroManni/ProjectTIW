package it.polimi.tiw.progettoTIW.utils;

import it.polimi.tiw.progettoTIW.exceptions.OrderParameterException;

public class OrderParser {

	public static int[] parseOrderString(String order) {
		if (order == null || order.isEmpty()) {
			return new int[7];
		}
		if (order.length() != 7) {
			return null;
		}
		int[] orderInt = new int[7]; 
		try {
			for (int i = 0; i < orderInt.length; i++) {
				orderInt[i] = Integer.parseInt(order.substring(i, i + 1));
				if (orderInt[i] != 0 && orderInt[i] != 1) {
					return null;
				}
			}
		} catch (NumberFormatException | NullPointerException e) {
			return null;
		}
		return orderInt;
	}
	
	public static String produceOrderString(int[] orderInt, Integer columnIndex) throws OrderParameterException {
		String order = "";
		for (int i = 0; i < orderInt.length; i++) {
			if (i == columnIndex - 1) {
				if (orderInt[i] == 0) {
					order += "0";
				} else if (orderInt[i] == 1) {
					order += "1";
				} else {
					throw new OrderParameterException("Parameter error");
				}
			} else {
				order += "0";
			}
		}
		return order;
	}
}