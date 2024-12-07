package it.polimi.tiw.progettoTIW.beans;

import java.util.ArrayList;
import java.util.List;

public class ReviewsData {
	
	private List<Review> reviews;
	private double avgVote;
	
	public ReviewsData() {
		this.reviews = new ArrayList<Review>();
	}
	
	public List<Review> getReviews() {
		return reviews;
	}
	
	public void setReviews(List<Review> reviews) {
		this.reviews.addAll(reviews);
	}
	
	public double getAvgVote() {
		return avgVote;
	}
	
	public void setAvgVote(double avgVote) {
		this.avgVote = avgVote;
	}

}
