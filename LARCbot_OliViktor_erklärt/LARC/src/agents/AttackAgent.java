package agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import org.json.simple.parser.ParseException;

import utils.Config;

public class AttackAgent extends AbstractAgent {
	private static int SUCCESS_CHANCE = Config
			.getIntValue("Agent_SuccesChance"); // Erfolgswahrscheinlich, dass
												// die bevorzugte Action
												// ausgeführt wird, in Prozent

	private static Double[] actionList = null;
	private static int roundCounter = 0, fileCounter = 0;

	// private static final String FILENAME = "attack_agent" + FOLDER_NAME;

	static {
		if (LOAD_ON_START) {
			try {
				actionList = load("attack_agent");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
	}

	private Random rnd;
	private int numberOfActions;

	/**
	 * @param environmentStateCount
	 *            Anzahl der Zustände, die die Umwelt annehmen kann
	 * @param actionCount
	 *            Anzahl der möglichen Aktionen
	 */
	public AttackAgent(int stateCount, int actionCount) {
		super();
		rnd = new Random();

		numberOfActions = actionCount;

		if (actionList == null) {
			actionList = new Double[stateCount * numberOfActions];
			Arrays.fill(actionList, new Double(0.0));
		}
	}

	@Override
	protected Double[] getActionList() {
		return actionList;
	}

	@Override
	protected int getStateFromId(int id) {
		return id / numberOfActions;
	}
	
	private int getActionWithMaxValue(int startID) {
		// Action mit dem höchsten Wert suchen
		double max = Double.MIN_VALUE;
		ArrayList<Integer> maxIDs = new ArrayList<Integer>();

		for (int i = 0; i < numberOfActions; i++) {
			if (actionList[startID + i] >= max) {
				if (actionList[startID + i] > max) {
					max = actionList[startID + i];
					maxIDs.clear();
				}

				maxIDs.add(i);
			}
		}

		int size = maxIDs.size();
		if (size <= 0) {
			// System.out.println("MoveAgent Fehler>> " + size +
			// " bei StartIndex " + startID);
			return rnd.nextInt(numberOfActions);
		}

		return maxIDs.get(rnd.nextInt(size));
	}

	@Override
	public int getNextAction(int stateID) {
		int actionID = -1;

		switch (mode) {
		case RANDOM:
			actionID = rnd.nextInt(numberOfActions);
			break;

		case Q_LEARNING:
		case SARSA_LAMBDA:
			int chance = rnd.nextInt(100);

			if (chance < SUCCESS_CHANCE) {
				actionID = getActionWithMaxValue(stateID * numberOfActions);
			} else {
				actionID = rnd.nextInt(numberOfActions);
			}

			addToLastActionQueue(stateID * numberOfActions + actionID);
			break;

		case FIGHTING: // gelernte werte werden nicht geändert
			actionID = getActionWithMaxValue(stateID * numberOfActions);
			break;
		}


		// System.out.println("AttackAgent asked for next action and returns #"
		// + (actionID % 36));

		return actionID;
	}

	@Override
	public void addReward(double reward) {
		// System.out.println("AttackAgent gets reward: " + reward);
		if (mode == AgentMode.Q_LEARNING || mode == AgentMode.SARSA_LAMBDA) {
			// System.out.print("AttackAgent > ");
			addRewardToLastActions(reward);
		}
	}

	@Override
	public void saveOnBattleEnd() {
		if (SAVE)
			save("attack_agent", null);
	}

	@Override
	protected double getMaxQForState(int stateID) {
		int actionID = getActionWithMaxValue(stateID * numberOfActions);
		return actionList[stateID * numberOfActions + actionID];
	}

	@Override
	public void onRoundEnded() {
		if (++roundCounter >= SAVE_TIMES) {
			save("attack_agent", FOLDER_NAME + "/" + fileCounter++ + ".zip");
			roundCounter = 0;
		}
	}
}
