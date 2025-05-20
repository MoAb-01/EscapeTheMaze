package Turns;

import Agent.*;

public class TurnManager {
    private Queue<Agent> agentQueue;
    private int currentRound;

    public TurnManager() {
        this.agentQueue = new Queue<>();
        this.currentRound = 0;
    }

    public void addAgent(Agent a) {
        if (a == null) {System.out.println("Agent cannot be null"); return;};
        agentQueue.enqueue(a);
    }

    public Agent getNextAgent() {
        int size = agentQueue.size();
        while (size > 0) {
            Agent agent = agentQueue.dequeue();
            if (!agent.hasReachedGoal() && !agent.isStuck()) {
                agentQueue.enqueue(agent);
                currentRound++;
                logTurnSummary(agent);
                return agent;
            }
            size--;
        }
        return null;
    }
    public boolean allAgentsFinished() {
        Queue<Agent>.CustomIterator iterator = agentQueue.iterator();
        while (iterator.hasNext()) {
            Agent a = iterator.next();
            if (!a.hasReachedGoal() && !a.isStuck()) return false;
        }
        return true;
    }
    public Queue<Agent> getTurnOrder() {
        return agentQueue;
    }

    public boolean isQueueEmpty() {
        return agentQueue.isEmpty();
    }

    public void logTurnSummary(Agent a) {
        System.out.printf(
                "Round %d – Agent %d at (%d,%d), moves=%d, backtracks=%d%n",
                currentRound,
                a.getId(),
                a.getCurrentX(), a.getCurrentY(),
                a.getTotalMoves(),
                a.getBacktracks()
        );
    }
}