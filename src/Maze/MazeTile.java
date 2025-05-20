package Maze;

import Agent.*;

public class MazeTile {
    private int x, y;
    private char type;
    private Agent agent; //<<--->>
    private MazeManager maze;

    public MazeTile(int x, int y, char type, MazeManager maze) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.agent = null;
        this.maze = maze;
    }

    public void applyEffect(Agent a) {
        System.out.println("Applying effect for the agent " + a.getId() + " at (" + x + "," + y + ") on tile '" + type + "'");
        switch (type) {
            case 'T':
                System.out.println("Agent " + a.getId() + " triggered a trap at (" + x + "," + y + ")!");
                a.incrementTrapsTriggered();
                a.backtrack(2); // ::Backtrack 2 steps
                break;
            case 'P':
                a.setHasPowerUp(true);
                System.out.println("Agent " + a.getId() + " collected a teleport power-up at (" + x + "," + y + ")");
                type = 'E'; // ::Consume power-up
                maze.updateCorridorTile(x, y, this);
                break;
            case 'G':
                a.setHasReachedGoal(true);
                System.out.println("Agent " + a.getId() + " reached the goal at (" + x + "," + y + ")!");
                agent = null; // ::Agent leaves the tile
                break;
            default:
                break;
        }
    }

    public boolean isTraversable() {
        return (agent == null && type != 'W');
    }
    public char getType() {
        return type;
    }
    public Agent getAgent() { return agent;}
    public void setAgent(Agent agent) {
        this.agent = agent;
    }
    public boolean hasAgent() {
        return agent != null ;
    }

    @Override
    public String toString() {
        if (type == 'G') {
            return "G";
        }
        if (agent != null) {
            return "A" + agent.getId();
        }
        return String.valueOf(type);
    }
}