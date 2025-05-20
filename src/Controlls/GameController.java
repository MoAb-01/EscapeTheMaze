package Controlls;

import Agent.*;
import Maze.*;
import Turns.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

public class GameController {
    private MazeManager maze;
    private AgentList agents;
    private TurnManager turns;
    private AuxLL goalOrder;
    private int maxTurns;
    private int turnCount;
    private StringBuilder log;
    private Agent winner;

    public GameController(MazeManager maze, int numAgents, int maxTurns) {
        this.maze = maze;
        this.maxTurns = maxTurns;
        this.turnCount = 0;
        this.agents = new AgentList();
        this.goalOrder = new AuxLL();
        this.log = new StringBuilder();
        for (int i = 0; i < numAgents; i++) {
            int[] pos = maze.findRandomValidPosition();
            if (pos != null) {
                Agent agent = new Agent(i, pos[0], pos[1], maze);
                agents.add(agent);
                maze.addAgent(agent);
            }
        }
        turns = new TurnManager();
        for (int i = 0; i < agents.size(); i++) {
            turns.addAgent(agents.get(i));
        }
    }

    public void runSimulation() {
        log.append("Simulation started!\n");
        System.out.println("Initial maze state:");
        maze.printMazeSnapshot();
        log.append("Initial maze state:\n").append(getMazeSnapshot()).append("\n");
        Random rand = new Random(); // For random row selection
        Scanner scanner = new Scanner(System.in); // Single scanner instance
        while (!allAgentsReachedGoal() && turnCount < maxTurns) {
            Agent currentAgent = turns.getNextAgent();
            if (currentAgent == null) {
                System.out.println("No active agents available. Ending simulation.");
                log.append("No active agents available. Ending simulation.\n");
                break;
            }
            if (currentAgent.hasReachedGoal() || currentAgent.isStuck()) {
                continue;
            }
            turnCount++;
            String turnInfo = String.format("Turn %d: Agent %d's turn\n", turnCount, currentAgent.getId());
            System.out.print(turnInfo);
            log.append(turnInfo);
            String turnOrder = "Current turn order: " + turns.getTurnOrder().toString() + "\n";
            System.out.print(turnOrder);
            log.append(turnOrder);
            String moveHistory = "Agent " + currentAgent.getId() + " move history (last 5): " + currentAgent.getLastFiveMoves() + "\n";
            System.out.print(moveHistory);
            log.append(moveHistory);
            System.out.print("Agent " + currentAgent.getId() + " -> move (UP/DOWN/LEFT/RIGHT), BACKTRACK, or USEPOWERUP: ");
            String action;
            try {
                action = scanner.nextLine().trim().toUpperCase();
            } catch (Exception e) {
                System.out.println("Error reading input: " + e.getMessage());
                action = "";
            }
            log.append("Agent " + currentAgent.getId() + " action: " + action + "\n");
            if (action.isEmpty()) {
                System.out.println("Empty input, skipping turn.");
                log.append("Action result: Empty input, skipped turn\n");
                continue;
            }
            String actionResult = handleAgentAction(currentAgent, action);
            log.append("Action result: " + actionResult + "\n");
            maze.applyEffects(currentAgent);
            if (currentAgent.hasReachedGoal() && !goalOrder.contains(String.valueOf(currentAgent.getId()))) {
                goalOrder.add(String.valueOf(currentAgent.getId()));
                if (winner == null) {
                    winner = currentAgent;
                }
                System.out.println("Agent " + currentAgent.getId() + " is finished!");
                log.append("Agent " + currentAgent.getId() + " reached the goal!\n");
            }
            System.out.println("Maze after Agent " + currentAgent.getId() + "'s turn:");
            maze.printMazeSnapshot();
            log.append("Maze after Agent " + currentAgent.getId() + "'s turn:\n").append(getMazeSnapshot()).append("\n");
            int rowId = rand.nextInt(maze.getHeight());
            System.out.println("Attempting to rotate corridor row " + rowId);
            log.append("Attempting to rotate corridor row " + rowId + "\n");
            maze.rotateCorridor(rowId);
        }
        if (allAgentsReachedGoal()) {
            System.out.println("All agents have reached the goal! Game over.");
            log.append("All agents have reached the goal! Game over.\n");
        } else if (turnCount >= maxTurns) {
            System.out.println("Maximum turns (" + maxTurns + ") reached. Game over.");
            log.append("Maximum turns (" + maxTurns + ") reached. Game over.\n");
        }
        System.out.println("Final maze state:");
        maze.printMazeSnapshot();
        log.append("Final maze state:\n").append(getMazeSnapshot()).append("\n");
        System.out.println("Goal reach order: " + goalOrder.toString());
        log.append("Goal reach order: " + goalOrder.toString() + "\n");
        printSummaryStatistics();
        writeLogToFile();
    }

    private boolean allAgentsReachedGoal() {
        for (int i = 0; i < agents.size(); i++) {
            Agent agent = agents.get(i);
            if (!agent.hasReachedGoal() && !agent.isStuck()) {
                return false;
            }
        }
        return true;
    }

    private String handleAgentAction(Agent agent, String action) {
        if (agent.hasReachedGoal()) {
            String result = "Agent " + agent.getId() + " has already reached the goal, skipping turn.";
            System.out.println(result);
            log.append(result + "\n");
            return result;
        }
        switch (action) {
            case "UP":
            case "DOWN":
            case "LEFT":
            case "RIGHT":
                if (maze.isValidMove(agent.getCurrentX(), agent.getCurrentY(), action)) {
                    agent.move(action);
                    String result = "Agent " + agent.getId() + " moved to (" + agent.getCurrentX() + "," + agent.getCurrentY() + ")";
                    System.out.println(result);
                    return result;
                } else {
                    String result = "Invalid move to " + action + ".";
                    System.out.println(result);
                    return result;
                }
            case "BACKTRACK":
                if (!agent.getMoveHistory().isEmpty()) {
                    int oldX = agent.getCurrentX();
                    int oldY = agent.getCurrentY();
                    agent.backtrack(1);
                    String result = "Agent " + agent.getId() + " backtracked to (" + agent.getCurrentX() + "," + agent.getCurrentY() + ")";
                    System.out.println(result);
                    return result;
                } else {
                    String result = "No moves to backtrack.";
                    System.out.println(result);
                    log.append(result + "\n");
                    return result;
                }
            case "USEPOWERUP":
                if (agent.hasPowerUp()) {
                    int oldX = agent.getCurrentX();
                    int oldY = agent.getCurrentY();
                    agent.applyPowerUp();
                    String result = "Agent " + agent.getId() + " teleported to (" + agent.getCurrentX() + "," + agent.getCurrentY() + ")";
                    System.out.println(result);
                    return result;
                } else {
                    String result = "No power-up available.";
                    System.out.println(result);
                    log.append(result + "\n");
                    return result;
                }
            default:
                String result = "Invalid move or command: " + action + ". Skipping turn.";
                System.out.println(result);
                log.append(result + "\n");
                return result;
        }
    }

    private String getMazeSnapshot() {
        StringBuilder snapshot = new StringBuilder();
        for (int i = 0; i < maze.getHeight(); i++) {
            for (int j = 0; j < maze.getWidth(); j++) {
                snapshot.append(maze.getTile(i, j).toString()).append(" ");
            }
            snapshot.append("\n");
        }
        return snapshot.toString();
    }

    private void printSummaryStatistics() {
        System.out.println("\n=== Simulation Summary ===");
        log.append("\n=== Simulation Summary ===\n");
        System.out.println("Total number of turns executed: " + turnCount);
        log.append("Total number of turns executed: " + turnCount + "\n");
        double avgMoves = 0;
        for (int i = 0; i < agents.size(); i++) {
            avgMoves += agents.get(i).getTotalMoves();
        }
        avgMoves = agents.size() > 0 ? avgMoves / agents.size() : 0;
        System.out.printf("Average number of moves per agent: %.2f\n", avgMoves);
        log.append(String.format("Average number of moves per agent: %.2f\n", avgMoves));
        System.out.println("Winner (first to reach goal): " + (winner != null ? "Agent " + winner.getId() : "None"));
        log.append("Winner (first to reach goal): " + (winner != null ? "Agent " + winner.getId() : "None") + "\n");
        System.out.println("\nPer-Agent Statistics:");
        log.append("\nPer-Agent Statistics:\n");
        System.out.println("Agent  | Moves | Backs | Traps | Power | Stack");
        log.append("Agent   | Moves | Backs | Traps | Power | Stack\n");
        System.out.println("-------|-------|-------|-------|-------|-------");
        log.append("-------|-------|-------|-------|-------|-------\n");
        for (int i = 0; i < agents.size(); i++) {
            Agent agent = agents.get(i);
            String stats = String.format("Agent%d |   %d   |   %d   |   %d   |   %d   |   %d",
                    agent.getId(),
                    agent.getTotalMoves(),
                    agent.getBacktracks(),
                    agent.getTrapsTriggered(),
                    agent.getPowerUpsUsed(),
                    agent.getMaxStackDepth());
            System.out.println(stats);
            log.append(stats + "\n");
        }
    }

    private void writeLogToFile() {
        try (FileWriter writer = new FileWriter("simulation_log.txt")) {
            writer.write(log.toString());
            System.out.println("Log written to simulation_log.txt");
        } catch (IOException e) {
            System.err.println("Error writing log file: " + e.getMessage());
        }
    }
}