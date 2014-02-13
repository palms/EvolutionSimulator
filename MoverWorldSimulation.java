///////////////////////////////////////////////////////////////////////////////
// Title:            MoverWorldSimulation
// Files:            Mover.java
// Semester:         Spring 2011
//
// Author:           Saul Laufer, slaufer@wisc.edu
// CS Login:         saul
// Lecturer's Name:  Prof. Skrentny
// Lab Section:      322
//////////////////////////// 80 columns wide //////////////////////////////////

import java.util.*;
import java.io.*;

/**
 * This class creates a two dimensional array contain and a corresponding "world"
 * GUI. It adds Movers and Food to the world. Specifications about the movers
 * food and the world are taken from an input file. It adds the Movers and Food to
 * the world. It moves Movers based on their gene sequence (taken from input file) and
 * records the amount of food "eaten". After a number of "timesteps" (from input), 
 * it "mates" the Movers who have eaten the mostfood to create new movers (the remaining
 * Mover's are removed from the world). The offspring are subjected to mutation 
 * (specifications determined by input file). After a determined amount of rounds
 * the program writes all the specifications from the input file, along with the genes
 * of the last round's Movers, to an output file.
 *
 * <p>Bugs: (No known bugs.)
 *
 * @author Saul Laufer
 */

public class MoverWorldSimulation  {
	
	/**
	  * This is the main method of the MoverWorldSimulation. It reads the 
	  * input file, storing it's data, then creates a new "world". For each round
	  * it adds Movers and food to the world. For each "timestep" it moves Movers
	  * and records food eaten. After each round it selects movers for "mating",
	  * creates offspring, removes unwanted movers and mutates offspring genes.
	  * After the total number of rounds it prints data to an output file and
	  * exits the program.
	  *
	  */

	public static void main(String []args){

		if (args.length < 1 || args.length > 2) {
			System.out.println("To use MoverWorldSimulation: " +
			"<input file name> [<output file name>]");
			System.exit(-1);
		}

		Scanner in = null; // new Scanner object accepting input file
		try  {
			in = new Scanner(new File("sampleConfig.txt"));
		}
		catch (FileNotFoundException e) {
			System.out.println("No such file exists.");
			System.exit(-1);
		}

		final int WORLD_ROWS = in.nextInt(); //total number of rows in world
		final int WORLD_COLS = in.nextInt(); //total number of columns in world
		final double MUTATION_PROBABILITY = in.nextDouble();
		//probability of mutation occurring
		
		final double MATING_PROPORTION = in.nextDouble();
		//proportion of Movers selected for mating after each round
		
		final int ROUNDS_PER_SIMULATION = in.nextInt(); //number of rounds in simulation
		
		final int TIMESTEPS_PER_ROUND = in.nextInt(); //number of steps per round
		final int NUMBER_OF_FOOD_GROUPS = in.nextInt(); //number of food groups in world
		final int FOOD_GROUP_RADIUS = in.nextInt(); //radius of each food group
		final double FOOD_PROBABILITY = in.nextDouble();
		//probability of food occurring in a coordinate
		
		final int SEED = in.nextInt(); //seed value for pseudo-random number generation

		int numberOfMovers = 0; //number of Movers in world
		ArrayList<Mover> moverList = new ArrayList<Mover>();
		//list of Movers in world

		in.nextLine();
		do {
			Mover hold = new Mover(in.nextLine()); //holder for Movers added to list
			moverList.add(hold);
			numberOfMovers++;
		} while (in.hasNext());

		in.close();

		Random rng = new Random(SEED); //pseudo-random number generator
		char[][] worldGrid = new char[WORLD_ROWS][WORLD_COLS];
		//representation of world
		
		MoverWorldGUI world = new MoverWorldGUI("World");
		//the GUI of the world


		//run the simulation
		for(int roundCounter = 0; roundCounter < ROUNDS_PER_SIMULATION; roundCounter++) {
			makeWorldNothing(worldGrid, world);
			addMoversToWorld(moverList, rng, WORLD_ROWS, WORLD_COLS, worldGrid);
			addFoodToWorld(NUMBER_OF_FOOD_GROUPS, rng, WORLD_ROWS, WORLD_COLS,
					FOOD_GROUP_RADIUS, worldGrid, FOOD_PROBABILITY);
			world.update(worldGrid);

			for (int timeStepCounter = 0; timeStepCounter < TIMESTEPS_PER_ROUND;
			timeStepCounter++) {
				performTimeStep(worldGrid, moverList, rng, WORLD_ROWS,
						WORLD_COLS, world);
			}

			moverList = selection(moverList, MATING_PROPORTION, numberOfMovers);
			moverList = mating(moverList, numberOfMovers, MATING_PROPORTION, rng);
			moverList = mutation(moverList, MUTATION_PROBABILITY, numberOfMovers,
					MATING_PROPORTION, rng);
		}


		PrintWriter out = null; //PrintWriter object for writing output
		try  {
			out = new PrintWriter(new File("LauferOutput.txt"));
		}
		catch (FileNotFoundException e) {
			System.out.println("No such file exists.");
			System.exit(-1);
		}

		out.println(WORLD_ROWS + " " + WORLD_COLS);
		out.println(MUTATION_PROBABILITY + " " + MATING_PROPORTION);
		out.println(ROUNDS_PER_SIMULATION + " " + TIMESTEPS_PER_ROUND);
		out.println(NUMBER_OF_FOOD_GROUPS + " " + FOOD_GROUP_RADIUS + " " +
				FOOD_PROBABILITY);
		out.println(SEED);
		
		for (int i = 0; i < moverList.size(); i++) {
			out.println(moverList.get(i).getGeneticSequence());
		}

		out.close();
		System.exit(-1);
	}

	/**
	  * Adds a pre-determined number of Mover objects
	  * to a 2D array representing the MoverWorldGUI object.
	  *
	  * @param movers The list of Movers
	  * @param rng A pseudo-random number generator
	  * @param worldRows Total number of rows in the "world"
	  * @param worldCols Total number of columns in the "world"
	  * @param grid A 2D array representing a MoverWorldGUI object
	  */

	public static void addMoversToWorld(ArrayList<Mover> movers, Random rng, 
			int worldRows, int worldCols, char [][] grid) {

		for (int i = 0; i < movers.size(); i++) {
			int randomXCoordinate; //a random row
			int randomYCoordinate; //a random column
			
			do {
				randomXCoordinate = generateRandomCoordinate(rng, worldRows);
				randomYCoordinate = generateRandomCoordinate(rng, worldCols);
			} while (grid[randomXCoordinate][randomYCoordinate] == 
				MoverWorldGUI.MOVER_UP);

			grid[randomXCoordinate][randomYCoordinate] = movers.get(i).getVisual();
			movers.get(i).setXCoordinate(randomXCoordinate);
			movers.get(i).setYCoordinate(randomYCoordinate);
		}
	}
	
	/**
	  * Adds a a pre-determined number of food to the MoverWorldGUI
	  * object. It distributes food around a pre-determined radius using pseudo-
	  * randomly generated coordinates which use a pre-determined food radius.
	  * It avoids placing food on top of movers.
	  *
	  * @param groups The number of food groups
	  * @param rng A pseudo-random number generator
	  * @param worldRows Total number of rows in the "world"
	  * @param worldCols Total number of columns in the "world"
	  * @param foodRadius The area to place food around a central coordinate
	  * @param grid A 2D array representing the MoverWorldGUI object.
	  * @param foodProbability The probability of food occurring in a given space
	  */

	public static void addFoodToWorld(int groups, Random rng, int worldRows,
			int worldCols, int foodRadius, char[][] grid, double foodProbability) {

		for (int i = 0; i < groups; i++) {
			int randomXCoordinate; //a random row
			int randomYCoordinate; //a random column

			do {
				randomXCoordinate = generateRandomCoordinate(rng, worldRows);
				randomYCoordinate = generateRandomCoordinate(rng, worldCols);
			} while (grid[randomXCoordinate][randomYCoordinate] == 
				MoverWorldGUI.NOTHING);

			int centerRow = randomXCoordinate; //the center row of a food group
			int centerCol = randomYCoordinate; //the center column of a food group

			int row = 0;
			if (centerRow - foodRadius < 0) {
				row = 0;
			}
			else {
				row = centerRow - foodRadius;
			}

			int col = 0;
			if (centerCol - foodRadius < 0) {
				col = 0;
			}
			else {
				col = centerCol - foodRadius;
			}

			for (int j = row; j <= (centerRow + foodRadius) && (j < worldRows); j++) {
				for (int k = col; k <= (centerCol + foodRadius) && (k < worldCols); k++) {
					boolean foodPossibility = isActionPerformed(foodProbability, rng);
					
					if (foodPossibility == true && grid[j][k] != MoverWorldGUI.MOVER_UP) {
						grid[j][k] = MoverWorldGUI.FOOD;

					}
				}
			}
		}
	}
	
	/**
	  * Removes food and movers from a world.
	  *
	  * @param grid The 2D array representing the MoverWorldGUI object.
	  */

	public static void makeWorldNothing(char [][] grid, MoverWorldGUI world) {

		for (int row = 0; row < grid.length; row++) {
			for (int col = 0; col < grid[row].length; col++) {
				grid[row][col] = MoverWorldGUI.NOTHING;
			}
		}
	}
	
	/**
	  * Determines whether or not a probability will happen.
	  *
	  * @param probability The probability of something happening
	  * @param rng A pseudo-random number generator
	  * @return a boolean value; true if action will happen,
	  * false otherwise
	  */

	public static boolean isActionPerformed(double probability, Random rng) {

		double value = rng.nextDouble(); //a random value between 0.0 a 1.0
		
		if (value <= probability) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	  * Generates a random coordinate.
	  *
	  * @param rng A pseudo-random number generator
	  * @param maxVal The maximum value of random coordinate
	  * @return a random integer
	  */

	public static int generateRandomCoordinate(Random rng, int maxVal) {
		return (rng.nextInt(maxVal));
	}
	
	/**
	  * Performs all actions that occur during a single "timestep". For each
	  * Mover, it determines whether they are near food and uses this to determine
	  * how their gene sequence is used to move. After it has processed all Movers
	  * it updates the MoverWorldGUI object.
	  *
	  * @param grid The 2D array representation of the MoverWorldGUI object
	  * @param moverList The list of Mover objects
	  * @param rng A pseudo-random number generator
	  * @param worldRows The total number of rows in the "world"
	  * @param worldCols The total number of columns in the "world"
	  * @param world A MoverWorldGUI object displaying the "world"
	  */

	public static void performTimeStep(char[][] grid, ArrayList<Mover> moverList,
			Random rng, int worldRows, int worldCols, MoverWorldGUI world) {

		for (int i = 0; i < moverList.size(); i++) {

			Mover currentMover = moverList.get(i);
			int xCoordinate = currentMover.getXCoordinate(); //row of Mover
			int yCoordinate = currentMover.getYCoordinate(); //column of Mover
			char direction = currentMover.getVisual(); //GUI of Mover
			String genes = currentMover.getGeneticSequence(); //genes of Mover

			// if mover facing up
			if (direction == MoverWorldGUI.MOVER_UP) {

				if (isCoordinateValid(grid, xCoordinate-1, yCoordinate,
						worldRows, worldCols)
						&& grid[xCoordinate - 1][yCoordinate] == 
							MoverWorldGUI.FOOD) { //food in front

					int segment = 1; //segment of genes used for movement
					moverAction(currentMover, genes, segment, grid, rng,
							worldRows, worldCols);
				}
				else if(isCoordinateValid(grid, xCoordinate, yCoordinate -1,
						worldRows, worldCols)
						&& grid[xCoordinate][yCoordinate - 1] == 
							MoverWorldGUI.FOOD) {  // food to left

					int segment = 2; //segment of genes used for movement
					moverAction(currentMover, genes, segment, grid, rng,
							worldRows, worldCols);

				}
				else if (isCoordinateValid(grid, xCoordinate, yCoordinate +1,
						worldRows, worldCols)
						&& grid[xCoordinate][yCoordinate + 1] == 
							MoverWorldGUI.FOOD) { // food to right

					int segment = 2; //segment of genes used for movement
					moverAction(currentMover, genes, segment, grid, rng, 
							worldRows, worldCols);

				}
				else {

					int segment = 3; //segment of genes used for movement
					moverAction(currentMover, genes, segment, grid, rng, 
							worldRows, worldCols);
				}
			}

			//if mover facing left
			else if (direction == MoverWorldGUI.MOVER_LEFT) {

				if (isCoordinateValid(grid, xCoordinate +1, yCoordinate,
						worldRows, worldCols)
						&& grid[xCoordinate + 1][yCoordinate] == 
							MoverWorldGUI.FOOD) {  //food to left

					int segment = 2; //segment of genes used for movement
					moverAction(currentMover, genes, segment, grid, rng,
							worldRows, worldCols);

				}
				else if (isCoordinateValid(grid, xCoordinate, yCoordinate -1,
						worldRows, worldCols)
						&& grid[xCoordinate][yCoordinate-1] == 
							MoverWorldGUI.FOOD) { // food to front

					int segment = 1; //segment of genes used for movement
					moverAction(currentMover, genes, segment, grid, rng,
							worldRows, worldCols);

				}
				else if (isCoordinateValid(grid, xCoordinate -1, yCoordinate,
						worldRows, worldCols)
						&& grid[xCoordinate - 1][yCoordinate] == 
							MoverWorldGUI.FOOD) { // food to right

					int segment = 2; //segment of genes used for movement
					moverAction(currentMover, genes, segment, grid, rng,
							worldRows, worldCols);

				}
				else {

					int segment = 3; //segment of genes used for movement
					moverAction(currentMover, genes, segment, grid, rng, 
							worldRows, worldCols);
				}
			}

			//if mover facing right
			else if (direction == MoverWorldGUI.MOVER_RIGHT) {

				if (isCoordinateValid(grid, xCoordinate, yCoordinate+1,
						worldRows, worldCols) 
						&& grid[xCoordinate][yCoordinate + 1] ==
							MoverWorldGUI.FOOD) { //food to front

					int segment = 1; //segment of genes used for movement
					moverAction(currentMover, genes, segment, grid, rng,
							worldRows, worldCols);

				}
				else if (isCoordinateValid(grid, xCoordinate-1, yCoordinate,
						worldRows, worldCols)
						&& grid[xCoordinate-1][yCoordinate] == 
							MoverWorldGUI.FOOD) { // food to left

					int segment = 2; //segment of genes used for movement
					moverAction(currentMover, genes, segment, grid, rng,
							worldRows, worldCols);

				}
				else if (isCoordinateValid(grid, xCoordinate+1, yCoordinate+1,
						worldRows, worldCols)
						&& grid[xCoordinate+1][yCoordinate]
						                       == MoverWorldGUI.FOOD) { // food to right

					int segment = 2; //segment of genes used for movement
					moverAction(currentMover, genes, segment, grid, rng,
							worldRows, worldCols);

				}
				else {

					int segment = 3; //segment of genes used for movement
					moverAction(currentMover, genes, segment, grid, rng,
							worldRows, worldCols);
				}
			}

			//if mover facing down
			else {

				if (isCoordinateValid(grid, xCoordinate +1, yCoordinate,
						worldRows, worldCols)
						&& grid[xCoordinate + 1][yCoordinate]
						                         == MoverWorldGUI.FOOD) { //food in front

					int segment = 1; //segment of genes used for movement
					moverAction(currentMover, genes, segment, grid, rng,
							worldRows, worldCols);

				}
				else if (isCoordinateValid(grid, xCoordinate, yCoordinate-1,
						worldRows, worldCols)
						&& grid[xCoordinate][yCoordinate - 1]
						                     == MoverWorldGUI.FOOD) { //food to right

					int segment = 2; //segment of genes used for movement
					moverAction(currentMover, genes, segment, grid, rng,
							worldRows, worldCols);

				}
				else if (isCoordinateValid(grid, xCoordinate, yCoordinate+1,
						worldRows, worldCols)
						&& grid[xCoordinate][yCoordinate + 1]
						                     == MoverWorldGUI.FOOD) { //food to left

					int segment = 2; //segment of genes used for movement
					moverAction(currentMover, genes, segment, grid,
							rng, worldRows, worldCols);

				}
				else {

					int segment = 3; //segment of genes used for movement
					moverAction(currentMover, genes, segment, grid, rng,
							worldRows, worldCols);
				}

			}
		}
		world.update(grid);
	}
	
	/**
	  * Uses gene sequence to determine the probability of movement. Updates
	  * the worldGrid accordingly.
	  *
	  * @param currentMover The Mover for whom to determine movement
	  * @param genes The gene sequence of currentMover
	  * @param segment The gene segment is used
	  * @param grid The 2D array representing the MoverWorldGUI object
	  * @param rng A pseudo-random number generator
	  * @param worldRows The total rows in the "world"
	  * @param worldCols The total cols in the "world"
	  */

	public static void moverAction(Mover currentMover, String genes, int segment,
			char[][] grid, Random rng, int worldRows, int worldCols) {

		String gene1 = ""; //to hold first gene of triplet
		String gene2 = ""; //to hold second gene of triplet
		String gene3 = ""; //to hold thrid gene of triplet
		
		if (segment == 1) {
			gene1 = genes.substring(0, 1);
			gene2 = genes.substring(1, 2);
			gene3 = genes.substring(2, 3);
		}
		else if (segment == 2) {
			gene1 = genes.substring(3, 4);
			gene2 = genes.substring(4, 5);
			gene3 = genes.substring(5, 6);
		}
		else {
			gene1 = genes.substring(6, 7);
			gene2 = genes.substring(7, 8);
			gene3 = genes.substring(8);
		}

		double moveGene = Double.parseDouble(gene1); //numerical value of gene
		moveGene = moveGene / 10;
		double leftGene = Double.parseDouble(gene2); //numerical value of gene
		leftGene = leftGene / 10;
		double rightGene = Double.parseDouble(gene3); //numerical value of gene
		rightGene = rightGene / 10;

		char currentDirection = currentMover.getVisual(); //GUI of Mover
		int xCoordinate = currentMover.getXCoordinate(); //row of Mover
		int yCoordinate = currentMover.getYCoordinate(); //column of Mover


		if (isActionPerformed(moveGene, rng)) {
			
			if (currentDirection == MoverWorldGUI.MOVER_UP) {
				int newX = xCoordinate-1; //new coordinate
				
				if (noMoversInCoordinate(grid, newX, yCoordinate, worldRows, worldCols) &&
						isCoordinateValid(grid, newX, yCoordinate, worldRows, worldCols)) {
					moveMover(currentMover, newX, yCoordinate, grid);
				}
			}
			else if (currentDirection == MoverWorldGUI.MOVER_LEFT) {
				int newY = yCoordinate-1; //new coordinate
				
				if (noMoversInCoordinate(grid, xCoordinate, newY, worldRows, worldCols) &&
						isCoordinateValid(grid, xCoordinate, newY, worldRows, worldCols)) {
					moveMover(currentMover, xCoordinate, newY, grid);
				}
			}
			else if (currentDirection == MoverWorldGUI.MOVER_RIGHT) {
				int newY = yCoordinate+1; //new coordinate
				
				if (noMoversInCoordinate(grid, xCoordinate, newY, worldRows, worldCols) &&
						isCoordinateValid(grid, xCoordinate, newY, worldRows, worldCols)) {
					moveMover(currentMover, xCoordinate, newY, grid);
				}
			}
			else {
				int newX = xCoordinate+1; //new coordinate
				
				if (noMoversInCoordinate(grid, newX, yCoordinate, worldRows, worldCols) &&
						isCoordinateValid(grid, newX, yCoordinate, worldRows, worldCols)) {
					moveMover(currentMover, newX, yCoordinate, grid);
				}

			}
		}

		else if (isActionPerformed(leftGene, rng)) {
			turnMoverLeft(currentMover, grid);
		}

		else if (isActionPerformed(rightGene, rng)) {
			turnMoverLeft(currentMover, grid);
		}
	}
	
	/**
	  * Moves a Mover object in the worldGrid.
	  *
	  * @param currentMover The Mover to move
	  * @param xCoordinate The new row position
	  * @param yCoordinate The new column position
	  * @param grid A 2D array representation of the MoverWorldGUI object
	  */

	public static void moveMover(Mover currentMover, int xCoordinate, int yCoordinate,
			char[][] grid) {

		int currentX = currentMover.getXCoordinate(); //row of Mover
		int currentY = currentMover.getYCoordinate(); //column of Mover

		if (grid[xCoordinate][yCoordinate] == MoverWorldGUI.FOOD) {
			currentMover.addFood(1);
		}
		else {

		}
		currentMover.setXCoordinate(xCoordinate);
		currentMover.setYCoordinate(yCoordinate);
		grid[xCoordinate][yCoordinate] = currentMover.getVisual();
		grid[currentX][currentY] = MoverWorldGUI.NOTHING;
	}
	
	/**
	  * Turns a mover left and updates the worldGrid accordingly.
	  *
	  * @param currentMover The Mover object to turn
	  * @param grid A 2D array representing the MoverWorldGUI object
	  */

	public static void turnMoverLeft(Mover currentMover, char [][] grid) {
		
		if (currentMover.getVisual() == MoverWorldGUI.MOVER_UP) {
			currentMover.setVisual(MoverWorldGUI.MOVER_LEFT);
			grid[currentMover.getXCoordinate()][currentMover.getYCoordinate()] 
			                                    = MoverWorldGUI.MOVER_LEFT;
		}
		else if (currentMover.getVisual() == MoverWorldGUI.MOVER_LEFT) {
			currentMover.setVisual(MoverWorldGUI.MOVER_DOWN);
			grid[currentMover.getXCoordinate()][currentMover.getYCoordinate()] 
			                                    = MoverWorldGUI.MOVER_DOWN;
		}
		else if (currentMover.getVisual() == MoverWorldGUI.MOVER_DOWN) {
			currentMover.setVisual(MoverWorldGUI.MOVER_RIGHT);
			grid[currentMover.getXCoordinate()][currentMover.getYCoordinate()] 
			                                    = MoverWorldGUI.MOVER_RIGHT;
		}
		else {
			currentMover.setVisual(MoverWorldGUI.MOVER_UP);
			grid[currentMover.getXCoordinate()][currentMover.getYCoordinate()] 
			                                    = MoverWorldGUI.MOVER_UP;
		}
	}
	
	/**
	  * Turns a Mover object right. Updates the worldGrid accordingly.
	  *
	  * @param currentMover The Mover object to turn
	  * @param grid A 2D array representation of the MoverWorldGUI object
	  */

	public static void turnMoverRight(Mover currentMover, char[][] grid) {
		
		if (currentMover.getVisual() == MoverWorldGUI.MOVER_UP) {
			currentMover.setVisual(MoverWorldGUI.MOVER_RIGHT);
			grid[currentMover.getXCoordinate()][currentMover.getYCoordinate()] 
			                                    = MoverWorldGUI.MOVER_RIGHT;
		}
		else if (currentMover.getVisual() == MoverWorldGUI.MOVER_RIGHT) {
			currentMover.setVisual(MoverWorldGUI.MOVER_DOWN);
			grid[currentMover.getXCoordinate()][currentMover.getYCoordinate()] 
			                                    = MoverWorldGUI.MOVER_DOWN;
		}
		else if (currentMover.getVisual() == MoverWorldGUI.MOVER_DOWN) {
			currentMover.setVisual(MoverWorldGUI.MOVER_LEFT);
			grid[currentMover.getXCoordinate()][currentMover.getYCoordinate()] 
			                                    = MoverWorldGUI.MOVER_LEFT;
		}
		else {
			currentMover.setVisual(MoverWorldGUI.MOVER_UP);
			grid[currentMover.getXCoordinate()][currentMover.getYCoordinate()] 
			                                    = MoverWorldGUI.MOVER_UP;
		}
	}
	
	/**
	  * Determines whether or not there is a mover in a coordinate.
	  *
	  * @param grid A 2D array representation of the MoverWorldGUI
	  * @param xCoordinate The row to check
	  * @param yCoordinate The column to check
	  * @param worldRows Total number of rows in "world"
	  * @param worldCols Total number of columns in "world"
	  * @return a boolean value; true if coordinate has no mover, false otherwise
	  */

	public static boolean noMoversInCoordinate(char [][] grid, int xCoordinate, 
			int yCoordinate, int worldRows, int worldCols) {

		if (isCoordinateValid(grid, xCoordinate, yCoordinate, worldRows, worldCols)) {
			
			if (grid[xCoordinate][yCoordinate] == MoverWorldGUI.FOOD || 
					grid[xCoordinate][yCoordinate] == MoverWorldGUI.NOTHING) {
				return true;
			}
			return false;
		}
		else {

			return false;
		}
	}
	
	/**
	  * Determines if a coordinate exists in the worldGrid.
	  *
	  * @param grid A 2D array representation of the MoverWorldGUI object
	  * @param xCoordinate The row to check
	  * @param yCoordinate The column to check
	  * @param worldRows The total number of rows in "world"
	  * @param worldCols The total number of columns in "world"
	  * @return a boolean value; true if coordinate exists, false otherwise
	  */

	public static boolean isCoordinateValid(char [][] grid, int xCoordinate,
			int yCoordinate, int worldRows, int worldCols ) {

		if (xCoordinate < worldRows && yCoordinate < worldCols && xCoordinate >= 0 
				&& yCoordinate >= 0) {
			return true;
		}
		else {
			return false;
		}
	}
	/**
	  * Selects a number of Movers for "mating" based on the amount of food
	  * the Mover has eaten. Removes all other Movers.
	  *
	  * @param moverList List of Mover objects
	  * @param matingProportion Proportion of population that will "mate"
	  * @param numberOfMovers The number of Mover objects in the world
	  * @return an ArrayList of the Movers selected for "mating"
	  */

	public static ArrayList<Mover> selection(ArrayList<Mover> moverList,
			double matingProportion, int numberOfMovers) {

		Mover [] selectionList = new Mover [moverList.size()];
		//to hold Movers selected for mating
		
		double numberOfMaters = matingProportion * numberOfMovers;
		//number of Mover selected for mating
		
		int numOfMaters = (int)numberOfMaters; //numberOfMaters converted to int

		for (int i = 0; i < selectionList.length; i++) {
			selectionList[i] = moverList.get(i);
		}

		Arrays.sort(selectionList);

		moverList.clear();

		for (int i = 0; i < numOfMaters; i++) {
			moverList.add(selectionList[i]);
		}

		return moverList;
	}
	
	/**
	  * Creates offspring using the gene sequence's of the Movers selected
	  * for "mating".
	  *
	  * @param moverList List of Mover objects
	  * @param numberOfMovers The number of Movers in the "world"
	  * @param matingProportion The number of Movers selected for "mating"
	  * @param rng A pseudo-random number generator
	  * @return an ArrayList containing the parent and new offspring Movers
	  */

	public static ArrayList<Mover> mating(ArrayList<Mover> moverList,
			int numberOfMovers, double matingProportion, Random rng) {

		double numberOfMaters = matingProportion * numberOfMovers;
		//number of Mover selected for mating
		
		int numOfMaters = (int)numberOfMaters; //numberOfMaters converted to int
		int moverOffspringNeeded = numberOfMovers - numOfMaters;
		//number of new Movers to be created

		for (int i = 0; i < moverOffspringNeeded; i++) {
			int moverParent1 = rng.nextInt(numOfMaters); //first parent place in list
			int moverParent2; //second parent place in list

			do {
				moverParent2 = rng.nextInt(numOfMaters);
			} while(moverParent2 == moverParent1);

			Mover moverParentA = moverList.get(moverParent1); //first parent
			Mover moverParentB = moverList.get(moverParent2); //second parent
			String geneSequenceA = moverParentA.getGeneticSequence(); //genes of first parent
			String geneSequenceB = moverParentB.getGeneticSequence(); //genes of second parent
			String newGenes = ""; //to hold gene sequence of offspring

			if (isActionPerformed(.5, rng)) {
				newGenes += geneSequenceA.substring(0, 3);
			}
			else {
				newGenes += geneSequenceB.substring(0, 3);
			}

			if (isActionPerformed(.5,rng)) {
				newGenes += geneSequenceA.substring(3, 6);
			}
			else {
				newGenes += geneSequenceB.substring(3, 6);
			}

			if (isActionPerformed(.5, rng)) {
				newGenes += geneSequenceA.substring(6);
			}
			else {
				newGenes += geneSequenceB.substring(6);
			}


			Mover offspring = new Mover(newGenes);  //the new Mover
			moverList.add(offspring);
		}

		for (int i = moverList.size() -1; i >= numberOfMovers; i--) {
			moverList.remove(i);
		}
		return moverList;
	}
	
	/**
	  * Mutates the gene sequence's of the offspring Movers.
	  *
	  * @param moverList List of Movers in "world"
	  * @param mutationProbability The probability of mutation occurring
	  * @param numberOfMovers The numbjer of Movers in the "world"
	  * @param matingProportion The proportion of Movers selected for "mating"
	  * @param rng A pseudo-random number generator
	  * @return an ArrayList containing the Mover parents and the Mover offspring
	  * with newly-mutated gene sequences
	  */

	public static ArrayList<Mover> mutation(ArrayList<Mover> moverList, double mutationProbability,
			int numberOfMovers, double matingProportion, Random rng) {

		double numberOfMaters = matingProportion * numberOfMovers;
		//number of Movers selected for mating
		
		int numOfMaters = (int)numberOfMaters; //numOfMaters converted to int
		int numberOfOffspring = numberOfMovers - numOfMaters;
		//number of new Movers created
		
		for (int i = 0; i < numberOfOffspring; i ++) {
			for (int j = 0; j < 9; j++) {
				Mover toMutate = moverList.get(i + numOfMaters); //Mover receiving mutation
				String genes = toMutate.getGeneticSequence(); //genes of Mover to mutate
				String genesPart1 = genes.substring(0, j); 
				//section of gene sequence prior to gene to mutate
				
				String genesPart2 = genes.substring(j+1);
				//section of gene sequence after gene to mutate
				
				if (isActionPerformed(mutationProbability, rng)) {
					int mutatedVal = rng.nextInt(10);  //the new gene
					String mutatedGene = Integer.toString(mutatedVal); //new gene converted to String
					String newGenes = genesPart1 + mutatedGene + genesPart2; //Movers new gene sequence
					toMutate.setGeneticSequence(newGenes);
					moverList.set(i+numOfMaters, toMutate);
				}
			}
		}
		return moverList;
	}
}


