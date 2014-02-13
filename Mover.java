///////////////////////////////////////////////////////////////////////////////
// Main Class File:  MoverWorldSimulation.java
// File:             Mover.java
// Semester:         Spring 2011
//
// Author:           Saul Laufer, slaufer@wisc.edu
// CS Login:         saul
// Lecturer's Name:  Prof. Skrentny
// Lab Section:      322
//////////////////////////// 80 columns wide //////////////////////////////////

/**
  * This class creates a "Mover" object for the MoverWorldSimulation. A Mover object
  * contains a gene sequence, the Mover's coordinates, the amount of food eaten, and
  * it's GUI representation.
  *
  * <p>Bugs: (No known bugs.)
  *
  * @author Saul Laufer
  */

@SuppressWarnings("rawtypes")
public class Mover implements Comparable {

	private String geneticSequence; //Mover's gene sequence
	private int xCoordinate; //Mover's row in world
	private int yCoordinate; //Mover's column in world
	private int foodEaten; //amount of food Mover has eaten
	private char visual; //GUI of Mover

	public Mover(String geneticSequence) {
		this.geneticSequence = geneticSequence;
		this.foodEaten = 0;
		this.visual = MoverWorldGUI.MOVER_UP;
	}
	/**
	  * Gets the current row of a Mover
	  *
	  * @return current row of Mover
	  */

	public int getXCoordinate() {
		return this.xCoordinate;
	}
	
	/**
	  * Gets the current column of a Mover
	  *
	  * @return current column of Mover
	  */

	public int getYCoordinate() {
		return this.yCoordinate;
	}
	
	/**
	  * Gets the amount of food a Mover has eaten
	  *
	  * @return amount of food eaten
	  */

	public int getFoodEaten() {
		return this.foodEaten;
	}
	
	/**
	  * Gets the current Mover's GUI
	  *
	  * @return Mover's GUI
	  */

	public char getVisual() {
		return this.visual;
	}
	
	/**
	  * Gets the gene sequence of a Mover
	  *
	  * @return Mover's gene sequence
	  */

	public String getGeneticSequence() {
		return this.geneticSequence;
	}
	
	/**
	  * Sets the current row of a Mover
	  *
	  * @param xCoordinate Current row of Mover
	  */

	public void setXCoordinate(int xCoordinate) {
		this.xCoordinate = xCoordinate;
	}
	
	/**
	  * Sets the current column of a Mover
	  *
	  * @param yCoordinate Current column of Mover
	  */

	public void setYCoordinate(int yCoordinate) {
		this.yCoordinate = yCoordinate;
	}
	
	/**
	  * Sets the Mover's GUI
	  *
	  * @param visual Mover's new GUI
	  */

	public void setVisual(char visual) {
		this.visual = visual;
	}
	
	/**
	  * Adds amount of food to Mover's amount of eaten food
	  *
	  * @param food Food to add
	  */

	public void addFood(int food) {
		this.foodEaten += food;
	}
	
	/**
	  * Sets the gene sequence of a Mover.
	  *
	  * @param geneticSequence The new gene sequence
	  */

	public void setGeneticSequence(String geneticSequence) {
		this.geneticSequence = geneticSequence;
	}
	
	/**
	  * Compares an object to a Mover object. If object is a Mover object
	  * compares the amount of food eaten by the two Movers.
	  *
	  *@param o An object for comparison
	  * @return an integer value determining which Mover has eaten more food
	  */

	public int compareTo(Object o) {
		if (o instanceof Mover) {
			Mover m1 = (Mover) o;
			if (this.foodEaten > m1.foodEaten) {
				return -1;
			}
			else if (this.foodEaten == m1.foodEaten) {
				return 0;
			}
			else {
				return 1;
			}

		}
		else {
			throw new ClassCastException();
		}
	}
}
