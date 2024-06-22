package ps3;

import ps3.Action;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Node {
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_RESET = "\u001B[0m";
	public static final int[][] GOAL_STATE = new int[][] { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 0 } };
	public static int num = 0;
	private int[][] state;
	private Node previous;
	private Action action;
	private int cost;
//	private int numChildrn;
	private int number;
	private boolean isRoot;

	public Node(int[][] st, Node prev, Action act, int cst, boolean root) {
		state = st;
		previous = prev;
		action = act;
		cost = cst;
		isRoot = root;
//		numChildrn = 0;
		number = num;
		num++;
	}

	public int get_num() {
		return number;
	}

	public Node get_parent() {
		return previous;
	}

	public int get_cost() {
		return cost;
	}

	public Action get_action() {
		return action;
	}

	public int[][] get_state() {
		int[][] copy = new int[state.length][];
		for (int i = 0; i < state.length; i++) {
			copy[i] = state[i].clone();
		}
		return copy;
	}

	public boolean is_goal() {
		return Arrays.deepEquals(get_state(), GOAL_STATE);
	}

	public boolean is_root() {
		return isRoot;
	}

	public List<Action> get_actions() {

		List<Action> actions = new ArrayList<>();

		int zeroRow;
		int zeroCol;
		int[] zeroLoc = get_spaceLocation();
		zeroRow = zeroLoc[0];
		zeroCol = zeroLoc[1];

		if (zeroRow == 1) // middle row
		{
			actions.add(Action.UP);
			actions.add(Action.DOWN);

		} else if (zeroRow == 2) {
			actions.add(Action.DOWN);
		}

		else {
			if (zeroRow != -1)
				actions.add(Action.UP);
		}
		// ----
		if (zeroCol == 1) {
			actions.add(Action.RIGHT);
			actions.add(Action.LEFT);

		} else if (zeroCol == 2) {
			actions.add(Action.RIGHT);
		}

		else {
			if (zeroCol != -1)
				actions.add(Action.LEFT);
		}

		return actions;

	}

	private int[] get_spaceLocation() {
		int[][] currentState = get_state();
		int[] loc = { -1, -1 };
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (currentState[i][j] == 0) {
					loc[0] = i;
					loc[1] = j;
					break;
				}
			}
		}
		return loc;
	}

	public boolean valid_action(Action act) {
		int zeroRow;
		int zeroCol;
		int[] zeroLoc = get_spaceLocation();
		zeroRow = zeroLoc[0];
		zeroCol = zeroLoc[1];

		switch (act) {
		case UP:
			if (zeroRow == 2)
				return false;
			break;
		case DOWN:
			if (zeroRow == 0)
				return false;
			break;
		case LEFT:
			if (zeroCol == 2)
				return false;
			break;
		case RIGHT:
			if (zeroCol == 0)
				return false;
			break;
		default:
			break;
		}
		return true;
	}

	public Node get_child(Action act) {
		int[][] child_state = get_state();
		int[] zeroLoc = get_spaceLocation();
		int zeroRow = zeroLoc[0];
		int zeroCol = zeroLoc[1];
		if (valid_action(act)) {
			switch (act) {
			case UP:
				child_state[zeroRow][zeroCol] = child_state[zeroRow + 1][zeroCol];
				child_state[zeroRow + 1][zeroCol] = 0;
				break;
			case DOWN:
				child_state[zeroRow][zeroCol] = child_state[zeroRow - 1][zeroCol];
				child_state[zeroRow - 1][zeroCol] = 0;
				break;
			case LEFT:
				child_state[zeroRow][zeroCol] = child_state[zeroRow][zeroCol + 1];
				child_state[zeroRow][zeroCol + 1] = 0;
				break;
			case RIGHT:
				child_state[zeroRow][zeroCol] = child_state[zeroRow][zeroCol - 1];
				child_state[zeroRow][zeroCol - 1] = 0;
				break;
			default:
				break;
			}
			return new Node(child_state, this, act, cost + 1, false);

		}

		System.out.println(ANSI_RED
				+ String.format("Space is at (%d,%d) and Action is %s", zeroRow, zeroCol, act.name()) + ANSI_RESET);
		return null;

	}

	public static boolean isSolvable(int[][] puzzle) {
		int[] flattened = new int[8];
		int index = 0;

		// Flatten the puzzle into a 1D array, excluding the blank tile
		for (int i = 0; i < puzzle.length; i++) {
			for (int j = 0; j < puzzle[i].length; j++) {
				if (puzzle[i][j] != 0) { // Exclude the blank tile
					flattened[index++] = puzzle[i][j];
				}
			}
		}

		// Count the number of inversions
		int inversions = 0;
		for (int i = 0; i < flattened.length - 1; i++) {
			for (int j = i + 1; j < flattened.length; j++) {
				if (flattened[i] > flattened[j]) {
					inversions++;
				}
			}
		}

		// The puzzle is solvable if the number of inversions is even
		return inversions % 2 == 0;
	}

	@Override
	public String toString() {
		int[][] current_state = get_state();
		StringBuilder builder = new StringBuilder();

		// Top border
		if (!is_root()) {
			builder.append(get_action().name() + ",  ");
			builder.append("\nChild of " + this.get_parent().get_num());

		} else {
			builder.append("root");

		}

		builder.append("\nLvl: " + cost + ", #" + get_num() + "\n");
		builder.append("+———+———+———+\n");

		for (int i = 0; i < current_state.length; i++) {
			for (int j = 0; j < current_state[i].length; j++) {
				builder.append("| ").append(current_state[i][j] == 0 ? "_" : current_state[i][j]).append(" ");
			}
			builder.append("|\n");

			// Row separator or bottom border
			String str = "+———+———+———+";
			if (i == current_state.length - 1) {
				if (this.is_goal()) {
					str += "  ✅";
				}
				if (this.is_root()) {
					str += "  🪵";
				}
			}
			builder.append(str + "\n");
		}

		return builder.toString();
	}

	// Override equals method
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Node other = (Node) obj;
		return (Arrays.deepEquals(this.get_state(), other.get_state()));
	}

}