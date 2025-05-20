package Maze;

import Agent.*;
import java.util.Random;

public class MazeManager {
    private MazeTile[][] grid;
    private CircularLinkedList[] corridors;
    private AgentList agents;
    private int height, width;
    private Random random;
    private int goalX, goalY;

    public MazeManager(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new MazeTile[height][width];
        this.corridors = new CircularLinkedList[height];
        for (int i = 0; i < height; i++) {
            corridors[i] = new CircularLinkedList();
        }
        this.agents = new AgentList();
        this.random = new Random();
    }

    public AgentList getAgentList() {
        return agents;
    }

    public int getHeight() {
        return height;
    }
    public int getWidth() {
        return width;
    }

    public void generateMaze() {
        // Step 1: Initialize grid with empty tiles
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                grid[i][j] = new MazeTile(i, j, 'E', this);
                corridors[i].add(grid[i][j]);
            }
        }
        // Step 2:: Place the goal
        do {
            goalX = random.nextInt(height);
            goalY = random.nextInt(width);
        } while (goalX == 0 && goalY == 0); // Avoid (0,0) for goal
        grid[goalX][goalY] = new MazeTile(goalX, goalY, 'G', this);
        updateCorridorTile(goalX, goalY, grid[goalX][goalY]);
        // Step 3:: Ensure (0,0) is empty
        grid[0][0] = new MazeTile(0, 0, 'E', this);
        updateCorridorTile(0, 0, grid[0][0]);
        // Step 4:: Carve paths from each agent's starting position to goal
        for (int i = 0; i < agents.size(); i++) {
            Agent agent = agents.get(i);
            carvePathToGoal(agent.getCurrentX(), agent.getCurrentY(), goalX, goalY);
        }
        // Step 5: Add random walls, traps, and power-ups with values
        int totalTiles = width * height;
        int numWalls = (int) (totalTiles * 0.20);
        int numTraps = (int) (totalTiles * 0.15);
        int numPowerUps = (int) (totalTiles * 0.10);

        for (int i = 0; i < numWalls; i++) {
            int x, y;
            do {
                x = random.nextInt(height);
                y = random.nextInt(width);
            } while (grid[x][y].getType() != 'E' || (x == 0 && y == 0) || (x == goalX && y == goalY));
            grid[x][y] = new MazeTile(x, y, 'W', this);
            updateCorridorTile(x, y, grid[x][y]);
        }

        for (int i = 0; i < numTraps; i++) {
            int x, y;
            do {
                x = random.nextInt(height);
                y = random.nextInt(width);
            } while (grid[x][y].getType() != 'E' || (x == 0 && y == 0) || (x == goalX && y == goalY));
            grid[x][y] = new MazeTile(x, y, 'T', this);
            updateCorridorTile(x, y, grid[x][y]);
        }

        for (int i = 0; i < numPowerUps; i++) {
            int x, y;
            do {
                x = random.nextInt(height);
                y = random.nextInt(width);
            } while (grid[x][y].getType() != 'E' || (x == 0 && y == 0) || (x == goalX && y == goalY));
            grid[x][y] = new MazeTile(x, y, 'P', this);
            updateCorridorTile(x, y, grid[x][y]);
        }
        // Step 6: Validate maze solvability for all agents
        for (int i = 0; i < agents.size(); i++) {
            Agent agent = agents.get(i);
            if (findPathToGoal(agent.getCurrentX(), agent.getCurrentY()).length == 0) {
                carvePathToGoal(agent.getCurrentX(), agent.getCurrentY(), goalX, goalY);
            }
        }
        // Step 7: Ensure goal tile is not overwritten
        if (grid[goalX][goalY].getType() != 'G') {
            System.out.println("Warning: Goal tile overwritten at (" + goalX + "," + goalY + "). Restoring.");
            grid[goalX][goalY] = new MazeTile(goalX, goalY, 'G', this);
            updateCorridorTile(goalX, goalY, grid[goalX][goalY]);
        }
        System.out.println("Generated maze size: " + grid.length + "x" + grid[0].length);
    }

    private void carvePathToGoal(int startX, int startY, int goalX, int goalY) {
        int currentX = startX;
        int currentY = startY;
        int maxPathSize = height * width;
        int[][] path = new int[maxPathSize][2];
        int pathSize = 0;
        path[pathSize][0] = currentX;
        path[pathSize][1] = currentY;
        pathSize++;

        while (currentX != goalX || currentY != goalY) {
            int[][] possibleMoves = new int[4][2];
            int moveCount = 0;
            int currentDist = Math.abs(currentX - goalX) + Math.abs(currentY - goalY);
            // :: up, down, left, right definitons
            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] dir : directions) {
                int newX = currentX + dir[0];
                int newY = currentY + dir[1];
                if (newX >= 0 && newX < height && newY >= 0 && newY < width) {
                    int newDist = Math.abs(newX - goalX) + Math.abs(newY - goalY);
                    // Prefer moves closer to or same distance from goal
                    if (newDist <= currentDist) {
                        possibleMoves[moveCount][0] = newX;
                        possibleMoves[moveCount][1] = newY;
                        moveCount++;
                    }
                }
            }

            // ::If no preferred moves, take any valid move
            if (moveCount == 0) {
                for (int[] dir : directions) {
                    int newX = currentX + dir[0];
                    int newY = currentY + dir[1];
                    if (newX >= 0 && newX < height && newY >= 0 && newY < width) {
                        possibleMoves[moveCount][0] = newX;
                        possibleMoves[moveCount][1] = newY;
                        moveCount++;
                    }
                }
            }

            if (moveCount == 0) break; // Stuck, exit loop

            // ::Randomly choose from possible moves
            int moveIndex = random.nextInt(moveCount);
            currentX = possibleMoves[moveIndex][0];
            currentY = possibleMoves[moveIndex][1];

            // ::Carve wall to empty unless it’s the goal
            if (grid[currentX][currentY].getType() == 'W' && !(currentX == goalX && currentY == goalY)) {
                grid[currentX][currentY] = new MazeTile(currentX, currentY, 'E', this);
                updateCorridorTile(currentX, currentY, grid[currentX][currentY]);
            }
            path[pathSize][0] = currentX;
            path[pathSize][1] = currentY;
            pathSize++;
        }
        // ::Ensure path is traversable
        for (int i = 0; i < pathSize; i++) {
            int x = path[i][0], y = path[i][1];
            if (x == goalX && y == goalY) continue;
            if (grid[x][y].getType() == 'W') {
                grid[x][y] = new MazeTile(x, y, 'E', this);
                updateCorridorTile(x, y, grid[x][y]);
            }
        }
        // ::Force connection if goal wasn’t reached
        if (currentX != goalX || currentY != goalY) {
            System.out.println("Random walk failed, forcing path to goal from (" + startX + "," + startY + ").");
            int dx = goalX - currentX;
            int dy = goalY - currentY;
            while (dx != 0 || dy != 0) {
                if (dx > 0) {
                    currentX++;
                    dx--;
                } else if (dx < 0) {
                    currentX--;
                    dx++;
                } else if (dy > 0) {
                    currentY++;
                    dy++;
                } else if (dy < 0) {
                    currentY--;
                    dy++;
                }
                if (grid[currentX][currentY].getType() == 'W' && !(currentX == goalX && currentY == goalY)) {
                    grid[currentX][currentY] = new MazeTile(currentX, currentY, 'E', this);
                    updateCorridorTile(currentX, currentY, grid[currentX][currentY]);
                }
            }
        }
    }

    private String[] findPathToGoal(int startX, int startY) {
        boolean[][] visited = new boolean[height][width];
        int maxQueueSize = height * width;
        int[][] queue = new int[maxQueueSize][2];
        int front = 0, rear = 0;
        queue[rear][0] = startX;
        queue[rear][1] = startY;
        rear++;
        visited[startX][startY] = true;

        int[][] parent = new int[height][width]; // Store parent as index in queue
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                parent[i][j] = -1; // -1 means no parent
            }
        }
        while (front < rear) {
            int x = queue[front][0], y = queue[front][1];
            front++;
            if (x == goalX && y == goalY) {
                String[] path = new String[maxQueueSize];
                int pathSize = 0;
                int cx = x, cy = y;
                while (cx != -1) {
                    path[pathSize] = cx + "," + cy;
                    pathSize++;
                    int pIndex = parent[cx][cy];
                    if (pIndex == -1) break;
                    cx = queue[pIndex][0];
                    cy = queue[pIndex][1];
                }
                String[] reversedPath = new String[pathSize];
                for (int i = 0; i < pathSize; i++) {
                    reversedPath[i] = path[pathSize - 1 - i];
                }
                return reversedPath;
            }
            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
            for (int[] dir : directions) {
                int newX = x + dir[0], newY = y + dir[1];
                if (newX >= 0 && newX < height && newY >= 0 && newY < width && !visited[newX][newY] && grid[newX][newY].isTraversable()) {
                    queue[rear][0] = newX;
                    queue[rear][1] = newY;
                    parent[newX][newY] = front - 1;
                    rear++;
                    visited[newX][newY] = true;
                }
            }
        }
        return new String[0];
    }

    public void updateCorridorTile(int x, int y, MazeTile newTile) {
        CircularLinkedList.Node current = corridors[x].getHead();
        int pos = 0;
        while (pos < y) {
            current = current.next;
            pos++;
        }
        if (current != null) {
            current.tile = newTile;
        }
    }

    public void addAgent(Agent a) {
        agents.add(a);
        grid[a.getCurrentX()][a.getCurrentY()].setAgent(a);
        ensureValidStart(a.getCurrentX(), a.getCurrentY());
    }

    public boolean moveAgent(Agent agent, String action) {
        int currentX = agent.getCurrentX();
        int currentY = agent.getCurrentY();
        int newX = currentX;
        int newY = currentY;
        switch (action.toUpperCase()) {
            case "UP": newX--; break;
            case "DOWN": newX++; break;
            case "LEFT": newY--; break;
            case "RIGHT": newY++; break;
            case "BACKTRACK":
                agent.backtrack(2);
                return true;
            case "USEPOWERUP":
                // Handle power-up (e.g., teleport)
                int[] newPos = findRandomValidPosition();
                if (newPos != null) {
                    getTile(currentX, currentY).setAgent(null);
                    agent.setX(newPos[0]);
                    agent.setY(newPos[1]);
                    getTile(newPos[0], newPos[1]).setAgent(agent);
                    agent.recordMove(newPos[0], newPos[1]);
                    System.out.println("Agent " + agent.getId() + " teleported to (" + newPos[0] + "," + newPos[1] + ")");
                    return true;
                }
                return false;
            default:
                System.out.println("Invalid action: " + action);
                return false;
        }
        if (agent.isMoveValid(newX, newY)) {
            getTile(currentX, currentY).setAgent(null);
            agent.setX(newX);
            agent.setY(newY);
            getTile(newX, newY).setAgent(agent);
            agent.recordMove(newX, newY);
            return true;
        } else {
            if (newX >= 0 && newX < height && newY >= 0 && newY < width) {
                MazeTile tile = getTile(newX, newY);
                if (tile.getType() == 'T') {
                    System.out.println("Agent " + agent.getId() + " attempted to move to trap at (" + newX + "," + newY + ")!");
                    agent.incrementTrapsTriggered();
                    agent.backtrack(2);
                    return false;
                }
            }
            System.out.println("Invalid move to (" + newX + "," + newY + ")");
            return false;
        }
    }

    public void updateAgentLocation(Agent a, int oldX, int oldY) {
        boolean otherAgentsAtOldPos = false;
        for (int i = 0; i < agents.size(); i++) {
            Agent other = agents.get(i);
            if (other != a && other.getCurrentX() == oldX && other.getCurrentY() == oldY) {
                otherAgentsAtOldPos = true;
                break;
            }
        }
        if (!otherAgentsAtOldPos) {
            grid[oldX][oldY].setAgent(null);
        }
        grid[a.getCurrentX()][a.getCurrentY()].setAgent(a);
    }
    public void rotateCorridor(int rowId) {
        if (rowId < 0 || rowId >= height) {
            System.out.println("Invalid rowId: " + rowId);
            return;
        }
        if (corridors[rowId] == null || corridors[rowId].getHead() == null) {
            System.out.println("Error: corridors[" + rowId + "] is empty or null");
            return;
        }
        System.out.println("Rotating corridor row " + rowId);
        corridors[rowId].rotate();
        CircularLinkedList.Node current = corridors[rowId].getHead();
        for (int j = 0; j < width; j++) {
            current.tile.setAgent(null); // Clear agent from the tile object in the list
            current = current.next;
        }
        current = corridors[rowId].getHead();
        for (int j = 0; j < width; j++) {
            grid[rowId][j] = current.tile;
            current = current.next;
        }
        if (rowId == goalX) {
            goalY = (goalY - 1 + width) % width; // Shift left
            System.out.println("Goal moved to (" + goalX + "," + goalY + ") due to corridor rotation");
        }
        for (int i = 0; i < agents.size(); i++) {
            Agent a = agents.get(i);
            if (a.getCurrentX() == rowId && !a.hasReachedGoal()) { // Skip finished agents
                int oldY = a.getCurrentY();
                int newY = (oldY + 1) % width; // Shift right
                a.setY(newY);
                grid[rowId][newY].setAgent(a); // Set agent in the new tile position
                System.out.println("Agent " + a.getId() + " moved from (" + rowId + "," + oldY + ") to (" + rowId + "," + newY + ") due to corridor rotation");
            }
        }

        System.out.println("After corridor rotation:");
        printMazeSnapshot();
    }

    public void applyEffects(Agent a) {
        MazeTile tile = grid[a.getCurrentX()][a.getCurrentY()];
        tile.applyEffect(a);
    }

    public MazeTile getTile(int x, int y) {
        if (x >= 0 && x < height && y >= 0 && y < width) {
            return grid[x][y];
        }
        return null;
    }

    public boolean isValidMove(int x, int y, String direction) {
            int newX = x, newY = y;
            switch (direction) {
                case "UP":    newX = x - 1; break;
                case "DOWN":  newX = x + 1; break;
                case "LEFT":  newY = y - 1; break;
                case "RIGHT": newY = y + 1; break;
                default:
                    return false;
            }
            // bounds check
            if (newX < 0 || newX >= height || newY < 0 || newY >= width) {
                return false;
            }

            char target = grid[newX][newY].getType();
            // disallow walls
            if (target == 'W') {
                return false;
            }
            // allow goal
            if (target == 'G') {
                return true;
            }
            return (target == 'E' || target == 'T' || target == 'P' || target == 'C');

    }

    public int[] teleportAgent(Agent agent) {
        int maxPositions = height * width;
        int[][] validPositions = new int[maxPositions][2];
        int count = 0;
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                MazeTile tile = grid[x][y];
                if (tile.isTraversable() && (!tile.hasAgent() || tile.getType() == 'G')) {
                    validPositions[count][0] = x;
                    validPositions[count][1] = y;
                    count++;
                }
            }
        }
        if (count == 0) {
            return null; //:: edge case:No valid positions
        }
        // ::Choose random position
        int index = random.nextInt(count);
        int newX = validPositions[index][0];
        int newY = validPositions[index][1];
        // ::Update agent position
        int oldX = agent.getCurrentX();
        int oldY = agent.getCurrentY();
        agent.setX(newX);
        agent.setY(newY);
        updateAgentLocation(agent, oldX, oldY);
        return new int[]{newX, newY};
    }

    public int[] findRandomValidPosition() {
        int x, y;
        int attempts = 0;
        do {
            x = random.nextInt(height);
            y = random.nextInt(width);
            attempts++;
            if (attempts > 100) return null;
        } while (grid[x][y] == null || grid[x][y].getType() != 'E' || grid[x][y].hasAgent() || (x == goalX && y == goalY));
        int[] pos = new int[]{x, y};
        ensureValidStart(x, y);
        return pos;
    }

    private void ensureValidStart(int x, int y) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        boolean hasValidMove = false;
        for (int[] dir : directions) {
            int newX = x + dir[0], newY = y + dir[1];
            MazeTile tile = getTile(newX, newY);
            if (tile != null && tile.isTraversable() && (tile.getType() == 'E' || tile.getType() == 'P' || tile.getType() == 'G')) {
                hasValidMove = true;
                break;
            }
        }
        if (!hasValidMove) {
            int[][] possibleDirs = new int[4][2];
            int dirCount = 0;
            for (int[] dir : directions) {
                int newX = x + dir[0], newY = y + dir[1];
                if (newX >= 0 && newX < height && newY >= 0 && newY < width && grid[newX][newY].getType() == 'W') {
                    possibleDirs[dirCount][0] = newX;
                    possibleDirs[dirCount][1] = newY;
                    dirCount++;
                }
            }
            if (dirCount > 0) {
                int chosenIndex = random.nextInt(dirCount);
                int chosenX = possibleDirs[chosenIndex][0];
                int chosenY = possibleDirs[chosenIndex][1];
                grid[chosenX][chosenY] = new MazeTile(chosenX, chosenY, 'E', this);
                updateCorridorTile(chosenX, chosenY, grid[chosenX][chosenY]);
            } else {
                for (int[] dir : directions) {
                    int newX = x + dir[0], newY = y + dir[1];
                    if (newX >= 0 && newX < height && newY >= 0 && newY < width && grid[newX][newY].getType() != 'G') {
                        grid[newX][newY] = new MazeTile(newX, newY, 'E', this);
                        updateCorridorTile(newX, newY, grid[newX][newY]);
                        break;
                    }
                }
            }
        }
    }

    public void printMazeSnapshot() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(grid[i][j] + " ");
            }
            System.out.println();
        }
    }

}