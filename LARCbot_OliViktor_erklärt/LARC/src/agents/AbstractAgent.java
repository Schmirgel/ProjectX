package agents;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import utils.Config;

public abstract class AbstractAgent {
	private static final double LEARN_RATE = Config
			.getIntValue("Agent_LearnRate") / 100.0;
	private static final double DISCOUNT_RATE = Config
			.getIntValue("Agent_DiscountRate") / 100.0;
	private static final double LAMBDA = Config.getIntValue("Agent_Lambda") / 100.0;
	private static final int QUEUE_SIZE = Config.getIntValue("Agent_QueueSize");

	private static final String LOAD_FILENAME = Config
			.getStringValue("Agent_LoadFile");
	private static final String SAVE_FILENAME = Config
			.getStringValue("Agent_SaveFile");
	protected static final int SAVE_TIMES = Config.getIntValue("Rounds") / 10;
	protected static final String TIMESTAMP = new SimpleDateFormat(
			"yyyy_MM_dd_HH-mm").format(new Date()), FOLDER_NAME;
	protected static final boolean LOAD_ON_START = Config
			.getBoolValue("Agent_LoadOnStart");
	protected static final boolean SAVE = Config
			.getBoolValue("Agent_SaveAgents");

	protected static AgentMode mode = AgentMode.values()[Config.getIntValue("Agent_Mode")];

	private ActionQueue lastActionQueue;

	private HashMap<Integer, Double> eValues;
	// ausgeführt beim laden 
	// welcher "mode" und ordner erstellung
	static {
		if (mode == AgentMode.Q_LEARNING || mode == AgentMode.SARSA_LAMBDA) {
			String enemy = "", algo = "";
			if (Config.getBoolValue("StartBattle"))
				enemy = "_" + Config.getStringValue("EnemyRobot");
		
			algo = "_" + mode.toString().charAt(0);
		
			FOLDER_NAME = "LARCAgents/" + TIMESTAMP + enemy + algo;
		
			new File(FOLDER_NAME).mkdirs();
		} else {
			FOLDER_NAME = null;
		}
	}

	protected AbstractAgent() {
		eValues = new HashMap<Integer, Double>();
		lastActionQueue = new ActionQueue(QUEUE_SIZE);
	}

	protected abstract Double[] getActionList(); // array der gelernten werte

	protected abstract int getStateFromId(int id); // gibt den state anhand einer id zurück

	protected abstract double getMaxQForState(int stateID); // max Q wert in diesem state für beliebige aktion
	
	/**
	 * Speichert gelernte Informationen des Bots in einer Datei ab.
	 * 
	 * @param filename
	 *            Der Dateiname
	 * @throws IOException
	 *             Wenn die angebene Datei nicht vorhanden ist oder nicht
	 *             zugreifbar
	 */
	public void save(final String entryFilename, final String zipFileName) {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				File zipFile = new File(zipFileName == null ? SAVE_FILENAME : zipFileName);
				FileHandler.saveZipFile(zipFile, entryFilename, getActionList());
			}
		});

		t.setName("LARCbot Save Thread");
		t.start();
	}

	// lädt den letzen stand zum weiterlernen
	//
	@SuppressWarnings("unchecked")
	protected static Double[] load(String entryFilename) throws IOException,
			ParseException {
		File f = new File(LOAD_FILENAME);
		Double[] ary = null;
		if (f.exists() && f.isFile() && LOAD_FILENAME.endsWith(".zip")) {
			ZipFile zf = new ZipFile(LOAD_FILENAME);
			ZipEntry ze = zf.getEntry(entryFilename + ".json");
			
			if (ze != null) {
				InputStreamReader reader = new InputStreamReader(
						zf.getInputStream(ze));
				JSONParser parser = new JSONParser();

				List<Double> obj = (List<Double>) ((JSONArray) parser
						.parse(reader));
				ary = obj.toArray(new Double[0]);

				reader.close();
			}
			
			zf.close();
			
		}
		return ary;
	}
	// speichert die letzten aktionen (sarsa lamda) 
	// id von der aktion zur actionqueue (feste größe)
	// bei mehreren gleichen aktionen hintereinander "richtig"
	// verarbeiten 
	protected void addToLastActionQueue(int id) {
		if (!lastActionQueue.offer(id)) {
			int polledID = lastActionQueue.poll();
			if (!lastActionQueue.contains(polledID))
				eValues.remove(polledID); // evalues = liste für sarsa

			lastActionQueue.offer(id);
		}

		if (!eValues.containsKey(id))
			eValues.put(id, 0.0);
	}

	private double e(int sa) {
		return eValues.get(sa);
	}

	private void setE(int sa, double value) {
		if (eValues.containsKey(sa))
			eValues.put(sa, value);
	}
	
	private void q_learning(int sa, int s_, double reward, double alpha,
			double gamma) {
		Double[] Q = getActionList();
		double maxQsa_ = getMaxQForState(s_);

		Q[sa] += alpha * (reward + gamma * maxQsa_ - Q[sa]);
	}

	private void sarsa_lambda(double reward, double alpha, double gamma,
			double lambda) {
		double delta /* , q_alt */;
		int sa, sa_;
		boolean end = false;

		if (lastActionQueue.size() < 1)
			return;

		Iterator<Integer> it = lastActionQueue.reverseIterator();
		Double[] Q = getActionList();

		sa_ = lastActionQueue.getNewestElement();
		sa = it.next();

		delta = reward + gamma * Q[sa_] - Q[sa];
		setE(sa, 1);

		// Debug Ausgaben
		/*
		 * System.out.println(this.getClass().toString());
		 * System.out.printf("Q-Wert von %d ist %f\n", sa_, Q[sa_]);
		 * System.out.println(lastActionQueue.toString());
		 * System.out.println(eValues.toString());
		 * System.out.printf("Reward: %f, Delta: %f\n", reward, delta);
		 */

		while (!end) {
			// q_alt = Q[sa]; // DEBUG

			Q[sa] = Q[sa] + alpha * delta * e(sa);

			// replace traces
			if (getStateFromId(sa) == getStateFromId(sa_))
				setE(sa, 1);
			else
				setE(sa, gamma * lambda * e(sa));

			// Debug Ausgaben
			/*
			 * System.out.printf("sa: %d, Q_alt: %f, Q_neu: %f, e: %f\n", sa,
			 * q_alt, Q[sa], e(sa));
			 */

			if (it.hasNext())
				sa = it.next();
			else
				end = true;
		}

//		System.out.println();
	}

	protected void addRewardToLastActions(double reward) {
		switch (mode) {
		case SARSA_LAMBDA:
			sarsa_lambda(reward, LEARN_RATE, DISCOUNT_RATE, LAMBDA);
			break;

		case Q_LEARNING:
			if (lastActionQueue.size() > 1) {
				Iterator<Integer> it = lastActionQueue.reverseIterator();
				int s_ = getStateFromId(it.next());
				int sa = it.next();

				q_learning(sa, s_, reward, LEARN_RATE, DISCOUNT_RATE);
			}
			break;
			
		default:
			// should never happen
			break;
		}
	}

	/**
	 * Gibt die nächste Aktion an
	 * 
	 * @param stateID
	 *            Die ID des aktuellen Zustands der Umwelt
	 * @return Die ID der nächsten Aktion
	 */
	public abstract int getNextAction(int stateID);

	public abstract void addReward(double reward);

	public abstract void saveOnBattleEnd();

	public abstract void onRoundEnded();
}
