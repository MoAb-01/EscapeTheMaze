//yalın gedik 220401053
//Mohamed Abdou 230401011
//Emirhan aydın 220401031

import Maze.*;
import Controlls.*;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("######## Maze Esacpe Game ########");

        final int MIN_DIMENSION = 4;
        final int MAX_DIMENSION = 100;
        final int MIN_AGENTS = 1;
        final int MIN_TURNS = 10;
        final int MAX_TURNS = 10000;

        int width = 0, height = 0, numAgents = 0, maxTurns = 0;

        while (true) {
            System.out.print("Enter maze width: ");
            try {
                    width = Integer.parseInt(scanner.nextLine().trim());
                    //validating the range
                        if (width < MIN_DIMENSION || width > MAX_DIMENSION) {
                            System.out.println("Width must be between " + MIN_DIMENSION + " and " + MAX_DIMENSION);
                            continue;
                        }
                    break;
            }
            catch(NumberFormatException e) //catching invalid d type
            {
                System.out.println("Error:: please ensure you entered integer data type");
                continue;
            }

        }

        while (true) {
            System.out.print("Enter maze height: ");
            try {
                height = Integer.parseInt(scanner.nextLine().trim());
                if (height < MIN_DIMENSION || height > MAX_DIMENSION) {
                    System.out.println("Height must be between " + MIN_DIMENSION + " and " + MAX_DIMENSION);
                    continue;
                }
                break;
            }
            catch (NumberFormatException e) {
                System.out.println("Error:: please ensure you entered integer data type");
                continue;
            }
        }

        int maxAgents = (width * height) / 4;

        while (true) {
            System.out.print("Enter number of agents: ");
            try {
                numAgents = Integer.parseInt(scanner.nextLine().trim());
                if (numAgents < MIN_AGENTS) {
                    System.out.println("Number of agents must be at least " + MIN_AGENTS);
                    continue;
                }
                if (numAgents > maxAgents) {
                    System.out.println("Number of agents cannot exceed " + maxAgents + " for a " + width + "x" + height + " maze");
                    continue;
                }
                break;
            } catch(NumberFormatException e)
            {
                System.out.println("Error:: please ensure you entered integer data type");
            }
        }

        while (true) {
            System.out.print("Enter maximum turns: ");
            try {
                maxTurns = Integer.parseInt(scanner.nextLine().trim());
                if (maxTurns < MIN_TURNS || maxTurns > MAX_TURNS) {
                    System.out.println("Maximum turns must be between " + MIN_TURNS + " and " + MAX_TURNS);
                    continue;
                }
                break;
            }catch(NumberFormatException e) {
                System.out.println("Error:: please ensure you entered integer data type");
                continue;
            }
        }

        MazeManager maze = new MazeManager(width, height);
        maze.generateMaze();
        GameController game = new GameController(maze, numAgents, maxTurns);
        game.runSimulation();
        scanner.close();
    }
}