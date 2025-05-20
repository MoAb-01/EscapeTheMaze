package Agent;

import Maze.MazeManager;
import java.util.Scanner;

public class Agent {
    private int id;
    private int currentX, currentY;
    private StringStack moveHistory;
    private int totalMoves;
    private int backtracks;
    private int trapsTriggered;
    private int powerUpsUsed;
    private int maxStackDepth;
    private boolean hasPowerUp;
    private boolean hasReachedGoal;
    private boolean isStuck;
    private MazeManager maze;

    public Agent(int id, int startX, int startY, MazeManager maze) {
        this.id = id;
        this.currentX = startX;
        this.currentY = startY;
        this.moveHistory = new StringStack();
        this.hasReachedGoal = false;
        this.totalMoves = 0;
        this.backtracks = 0;
        this.trapsTriggered = 0;
        this.powerUpsUsed = 0;
        this.maxStackDepth = 0;
        this.hasPowerUp = false;
        this.isStuck = false;
        this.maze = maze;
        recordMove(startX, startY);
    }

    public int getId() { return id; }
    public int getCurrentX() { return currentX; }
    public int getCurrentY() { return currentY; }
    public boolean hasReachedGoal() { return hasReachedGoal; }
    public int getTotalMoves() { return totalMoves; }
    public int getBacktracks() { return backtracks; }
    public int getTrapsTriggered() { return trapsTriggered; }
    public void incrementTrapsTriggered() { trapsTriggered++; }
    public int getPowerUpsUsed() { return powerUpsUsed; }
    public int getMaxStackDepth() { return maxStackDepth; }
    public boolean hasPowerUp() { return hasPowerUp; }
    public void setHasPowerUp(boolean hasPowerUp) { this.hasPowerUp = hasPowerUp; }
    public void setHasReachedGoal(boolean hasReachedGoal) { this.hasReachedGoal = hasReachedGoal; }
    public boolean isFinished() { return hasReachedGoal || isStuck; }
    public void setX(int x) { this.currentX = x; }
    public void setY(int y) { this.currentY = y; }
    public boolean isStuck() { return isStuck; }
    public MazeManager getMazeManager() {
        return maze;
    }
    public StringStack getMoveHistory() {
        return moveHistory;
    }
    public String getMoveHistoryAsString() {
        return getLastFiveMoves();
    }
    public int getMoveHistorySize() {
        return moveHistory.size();
    }

    public void move(String direction) {
        int newX = currentX;
        int newY = currentY;
        switch (direction.toUpperCase()) {
            case "UP": newX--; break;
            case "DOWN": newX++; break;
            case "LEFT": newY--; break;
            case "RIGHT": newY++; break;
            default:
                System.out.println("Invalid direction!");
                return;
        }
        if (isMoveValid(newX, newY)) {
            maze.moveAgent(this, direction);
            recordMove(newX, newY);
            totalMoves++;
        } else {
            System.out.println("Move to (" + newX + "," + newY + ") is invalid");
        }
    }
    public boolean isMoveValid(int x, int y) {
        return maze.getTile(x, y) != null && maze.getTile(x, y).isTraversable();
    }
    public void backtrack(int steps) {
        System.out.println("Agent " + id + " backtracking " + steps + " step(s) from (" + currentX + "," + currentY + ")");
        System.out.println("Move history before backtrack: " + getMoveHistoryAsString());
        if (moveHistory.size() < steps) {
            steps = moveHistory.size();
        }
        for (int i = 0; i < steps; i++) {
            if (!moveHistory.isEmpty()) {
                String popped = moveHistory.pop();
                System.out.println("Popped position " + i + ": " + popped);
            }
        }
        while (!moveHistory.isEmpty()) {
            String targetPosition = moveHistory.peek();
            String[] position = targetPosition.split(",");
            int newX = Integer.parseInt(position[0]);
            int newY = Integer.parseInt(position[1]);
            if (isMoveValid(newX, newY)) {
                int oldX = currentX;
                int oldY = currentY;
                currentX = newX;
                currentY = newY;
                maze.updateAgentLocation(this, oldX, oldY);
                backtracks++;
                System.out.println("Agent " + id + " backtracked to (" + newX + "," + newY + ")");
                isStuck = false;
                return;
            } else {
                System.out.println("Target position (" + newX + "," + newY + ") is invalid, trying previous");
                moveHistory.pop();
            }
        }
        System.out.println("Agent " + id + " staying at (" + currentX + "," + currentY + "): no valid history to backtrack to");
        backtracks++;
        //if stuck
        boolean hasValidMove = isMoveValid(currentX - 1, currentY) || isMoveValid(currentX + 1, currentY) ||
                isMoveValid(currentX, currentY - 1) || isMoveValid(currentX, currentY + 1);
        isStuck = !hasValidMove;
        if (isStuck) {
            System.out.println("Agent " + id + " is stuck at (" + currentX + "," + currentY + "): no valid moves");
        }
    }

    public void applyPowerUp() {
        if (hasPowerUp) {
            System.out.println("Power-up applied by Agent " + id);
            powerUpsUsed++;
            hasPowerUp = false;
            int[] newPos = maze.teleportAgent(this);
            if (newPos != null) {
                int oldX = currentX;
                int oldY = currentY;
                currentX = newPos[0];
                currentY = newPos[1];
                maze.updateAgentLocation(this, oldX, oldY);
                recordMove(currentX, currentY);
                System.out.println("Agent " + id + " teleported to (" + currentX + "," + currentY + ")");
            } else {
                System.out.println("No valid position found for teleportation"); //Throw problem
            }
        }
    }

    public void recordMove(int x, int y) { //add the move to the record
        moveHistory.push(x + "," + y);
        maxStackDepth = Math.max(maxStackDepth, moveHistory.size());
    }

    public String getLastFiveMoves() {
        StringBuilder sb = new StringBuilder("[");
        StringStack temp = new StringStack();
        int count = 0;
        while (!moveHistory.isEmpty() && count < 5) {
            String move = moveHistory.pop();
            temp.push(move);
            sb.append(move);
            if (!moveHistory.isEmpty() && count < 4) {
                sb.append(", ");
            }
            count++;
        }
        while (!temp.isEmpty()) {
            moveHistory.push(temp.pop());
        }
        sb.append("]");
        return sb.toString();
    }


    public void takeAction(MazeManager maze) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Agent " + id + " -> move (UP/DOWN/LEFT/RIGHT), BACKTRACK, or USEPOWERUP: ");
        String input = "";
        try {
            input = scanner.nextLine().trim().toUpperCase();
        } catch (Exception e) {
            System.out.println("Input error. Skipping turn.");
            return;
        }
        if (input.isEmpty()) {
            System.out.println("Empty input. Skipping turn.");
            return;
        }
        if ("BACKTRACK".equals(input)) {
            backtrack(1);
        } else if ("USEPOWERUP".equals(input)) {
            applyPowerUp();
        } else if (maze.isValidMove(currentX, currentY, input)) {
            move(input);
        } else {
            System.out.println("Invalid move or command. Skipping turn.");
        }
    }
    @Override
    public String toString() {
        return "Agent " + id;
    }
}