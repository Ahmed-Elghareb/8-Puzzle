package ps3;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class Search {

	public static Heuristic Heu;
	public static Comparator<Node> compr = new Comparator<Node>() {
		@Override
		public int compare(Node n1, Node n2) {
			return Double.compare(Search.h(n1) + n1.get_cost(), Search.h(n2) + n2.get_cost());
		}
	};

	public static Node BF_Search(Node root) {
		if (!is_solvable(root)) {
			System.out.println("Puzzle is not Solvable!");
			return null;
		}
		Node.num = 1;
		List<Node> fringe = new ArrayList<Node>();
		List<Node> closed = new ArrayList<Node>();

		fringe.add(root);
		try (PrintWriter writer = new PrintWriter(new FileWriter("BFS_output.txt", false))) {
			writer.println("------------------BFS------------------");
			while (!fringe.isEmpty()) {
				Node node = fringe.get(0);
				writer.println(node);
				closed.add(node);
				fringe.remove(0);
				List<Action> actions = node.get_actions();
				for (Action act : actions) {
					Node child = node.get_child(act);
					if (closed.contains(child)) { // closed.contains(child) containsNode (closed, child)
						Node.num--;
						writer.printf("%s \nChild of node #%d is visited ", act.name(), node.get_num());
						writer.printf("\n--\n");
						continue;
					} else if (child.is_goal()) {
						writer.println(child);
						return child;
					} else {
						fringe.add(child);
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Error writing to file: " + e.getMessage());
		}

		return null;
	}

	public static Node DFLimited_Search(Node root, int limit) {
		if (!is_solvable(root)) {
			System.out.println("Puzzle is not Solvable!");
			return null;
		}

		List<Node> fringe = new ArrayList<Node>();
		List<Node> closed = new ArrayList<Node>();

		fringe.add(root);
		try (PrintWriter writer = new PrintWriter(new FileWriter("DFL_output.txt", false))) {
			writer.println("------------------DFS------------------");
			while (!fringe.isEmpty()) {

				Node node = fringe.get(fringe.size() - 1);
				fringe.remove(fringe.size() - 1);

				if (node.get_cost() > limit) {
					continue;
				}
				writer.println(node);
				closed.add(node);

				if (node.is_goal()) {
					writer.println(node);
					return node;
				}
				List<Action> actions = node.get_actions();
				for (Action act : actions) {
					Node child = node.get_child(act);
					if (closed.contains(child)) { // closed.contains(child) containsNode (closed, child)
						Node.num--;
						writer.printf("%s \nChild of node #%d is visited ", act.name(), node.get_num());
						writer.printf("\n--\n");
						continue;
					} else if (child.is_goal()) {
						writer.println(child);
						return child;
					} else {
						fringe.add(child);
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Error writing to file: " + e.getMessage());
		}

		return null;
	}

	public static Node IDP_Search(Node root) {
		if (!is_solvable(root)) {
			System.out.println("Puzzle is not Solvable!");
			return null;
		}
		Node.num = 1;
		Node goal = null;
		for (int limit = 0; limit < Double.POSITIVE_INFINITY; limit++) {
			goal = DFLimited_Search(root, limit);
			if (goal != null) {
				return goal;
			}
		}
		return null; // failed
	}

	public static Node AStar_Search(Node root, Heuristic heu) {
		if (!is_solvable(root)) {
			System.out.println("Puzzle is not Solvable!");
			return null;
		}
		Node.num = 1;
		List<Node> fringe = new ArrayList<Node>();
		List<Node> closed = new ArrayList<Node>();
		fringe.add(root);
		Heu = heu;
		try (PrintWriter writer = new PrintWriter(new FileWriter("AStar_output.txt", false))) {
			writer.println("------------------A*------------------");
			while (!fringe.isEmpty()) {

				Collections.sort(fringe, compr); // Sorting based on Heuristics

				Node node = fringe.get(0);
				writer.println(node);
				closed.add(node);
				fringe.remove(0);
				if (node.is_goal()) {
					writer.println(node);
					return node;
				}
				List<Action> actions = node.get_actions();
				for (Action act : actions) {
					Node child = node.get_child(act);
					if (closed.contains(child)) { // closed.contains(child) containsNode (closed, child)
						Node.num--;
						writer.printf("%s \nChild of node #%d is visited ", act.name(), node.get_num());
						writer.printf("\n--\n");
						continue;
					} else if (fringe.contains(child)) {

						int index = is_closer(child, closed);
						if (index != -1) { // path to goal exists but smaller in cost, replace with what in the fringe
							fringe.remove(index);
							fringe.add(child);
							writer.println("THE CHILD IS CLOSER: ADDED TO FRINGE");
						}
						continue;
					} else {
						fringe.add(child);
					}
				}
			}
		} catch (IOException e) {
			System.err.println("Error writing to file: " + e.getMessage());
		}

		return null;
	}

	public static int is_closer(Node n, List<Node> list) {
		for (Node node : list) {
			if (n.equals(node)) {
				if (n.get_cost() < node.get_cost()) {
					return list.indexOf(node);
				}
			}
		}
		return -1;
	}

	public static double h(Node node) {
		switch (Heu) {
		case h1:
			return misplacedTiles(node);
		case h2:
			return manhtnDistance(node);
		case h3:
			return Math.round(euclidDistance(node));
		default:
			break;
		}

		return 0;
	}

	public static int misplacedTiles(Node n) {
		int h = 0;

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if ((n.get_state()[i][j] != 0) && (n.get_state()[i][j] != Node.GOAL_STATE[i][j])) {
					h++;
				}
			}
		}
		return h;
	}

	public static int manhtnDistance(Node n) {
		int h = 0;

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {

				int tile = n.get_state()[i][j];

				for (int l = 0; l < 3; l++) {
					for (int k = 0; k < 3; k++) {

						int goalTile = Node.GOAL_STATE[l][k];

						if ((tile == goalTile) && (tile != 0)) {
							h += Math.abs(l - i) + Math.abs(k - j);
							break;
						}

					}

				}
			}
		}
		return h;
	}

	public static double euclidDistance(Node n) {
		double euclideanDistance = 0;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int tile = n.get_state()[i][j];
				for (int l = 0; l < 3; l++) {
					for (int k = 0; k < 3; k++) {
						int goalTile = Node.GOAL_STATE[l][k];
						if ((tile == goalTile) && (tile != 0)) {
							euclideanDistance += Math.sqrt(Math.pow(l - i, 2) + Math.pow(k - j, 2));
							break;
						}
					}
				}
			}
		}
		return euclideanDistance;
	}

	public static boolean is_solvable(Node node) {
//		int[] goal = new int[] {0,1,2,3,4,5,6,7,8};
		int[] nodeState = new int[9];
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				int tile = node.get_state()[i][j];
				nodeState[(i * 3) + j] = tile;
			}
		}
		int inversions = 0;
		for (int i = 0; i < 9; i++) {
			int tile = nodeState[i];
			for (int k = i + 1; k < 9; k++) {
				int otherTile = nodeState[k];
				if ((otherTile < tile) && (otherTile != 0)) {
					inversions++;
				}
			}

		}
		return inversions % 2 == 0;
	}

	public static List<Action> goalPath(Node goal) {

		List<Node> path = new ArrayList<Node>();
		List<Action> actions = new ArrayList<Action>();

		try (PrintWriter writer = new PrintWriter(new FileWriter("Path_output.txt", false))) {

			System.out.println("\n/////Goal Path/////\n");
			System.out.println("Total nodes generated: " + Node.num);
			System.out.println("Path length: " + goal.get_cost());

			writer.println("\n\n/////Goal Path/////\n");
			writer.println("Total nodes generated: " + Node.num);
			writer.println("Path length: " + goal.get_cost());

//			writer.println("Goal found after " + goal.get_cost() + " moves and " + Node.num + " nodes!\n");

			Node parent = goal.get_parent();
			path.add(goal);
			while (!parent.is_root()) {
				path.add(parent);
				parent = parent.get_parent();
			}
			path.add(parent);

			for (int i = path.size() - 1; i >= 0; i--) {
				Node current = path.get(i);
				actions.add(current.get_action());
				if (Heu != null) {
//					System.out.println(Heu + "(n): " + Search.h(current));
					writer.println(Search.h(current));
				}
//				System.out.println(current);
				writer.println(current);
			}
			// Printing the actions after the nodes
			System.out.print("Path: ");
			writer.print("Path: ");

			for (Action act : actions) {
				if (act != null) {
					System.out.print(act + ", ");
					writer.print(act + ", ");
				}

			}
			System.out.println("GOAL!!✅");
			writer.println("GOAL!!✅");

		} catch (IOException e) {
			System.err.println("Error writing to file: " + e.getMessage());
		}

		return actions;
	}

}
