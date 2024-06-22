package ps3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main implements Runnable {

	public static Node GOAL_NODE = new Node(Node.GOAL_STATE, null, null, 0, true);
	public static String[] algos = new String[] { "BFS", "IDS", "h1", "h2", "h3" };

	static String path = "";
	static String algorithm = "";
	static List<Node> roots = new ArrayList<Node>();
	static Node root;

	static int i = 0;
	static int alg_index = 0;
	static boolean all_algs = false;
	static Thread watcherThrd;
	static Thread taskThrd;

	static int num_files = 0;
	static double total_time = 0;
	static int total_nodes = 0;

	/**
	 * Read 8 puzzle state from string within a file
	 * 
	 * @param file
	 * @return
	 */
	public static int[][] readPuzzleFromFile(File file) {
		int[][] puzzle = new int[3][3];

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String line;
			int row = 0;

			while ((line = br.readLine()) != null && row < 3) {
				String[] tokens = line.split(" ");
				for (int col = 0; col < tokens.length && col < 3; col++) {
					if (tokens[col].equals("_")) {
						puzzle[row][col] = 0;
					} else {
						puzzle[row][col] = Integer.parseInt(tokens[col]);
					}
				}
				row++;
			}
		} catch (IOException e) {
			System.err.println("Error reading the puzzle from file: " + file.getName());
			e.printStackTrace();
		}

		return puzzle;
	}

	/**
	 * load all files from path, read them as 8 puzzle states, add them to the
	 * root<Node> array
	 */
	public static void loadPuzzles(String filePath) {
		File dir = new File(path);
		FilenameFilter textFilter = (file, name) -> name.toLowerCase().endsWith(".txt");
		File[] files = dir.listFiles(textFilter);
		num_files = files.length;
		for (File f : files) {
			if (f.isFile()) {
//				System.out.println("File: " + f.getName());
				Node node = new Node(readPuzzleFromFile(f), null, null, 0, true);
//				System.out.println(node);
				roots.add(node);
			}
		}
	}

	public static void loadPuzzle(String filePath) {
		File file = new File(filePath);
		if (file.isFile() && file.getName().toLowerCase().endsWith(".txt")) {
			System.out.println("File: " + file.getName());
			Node node = new Node(readPuzzleFromFile(file), null, null, 0, true);
			System.out.println(node);
			roots.add(node);
			root = node;

		} else {
			System.out.println("The specified path does not point to a valid text file: " + filePath);
		}
	}

	// Make sure to adjust the readPuzzleFromFile method to read the puzzle from the
	// given file.

	@Override
	public void run() {
		System.out.println("Task started.");
		Node goal = null;
		String[] path_dir = path.split("/");
		if (path_dir[path_dir.length - 1].contains(".txt")) {
			loadPuzzle(path);
		} else {
			loadPuzzles(path);
		}

		System.out.println("---------------------------------");
		System.out.println(Node.ANSI_YELLOW + "Node number " + (i + 1) + Node.ANSI_RESET);
		System.out.println(roots.get(i));

		long startTime = System.nanoTime();
		// BFS/IDS/h1/h2/h3
		/*
		 * The method runs only the first loadable puzzle
		 */
		if (algorithm.equalsIgnoreCase("BFS")) {
			System.out.println(Node.ANSI_YELLOW + "Algorithm: BFS" + Node.ANSI_RESET);
			goal = Search.BF_Search(roots.get(i));

		} else if (algorithm.equalsIgnoreCase("IDS")) {
			System.out.println(Node.ANSI_YELLOW + "Algorithm: IDS" + Node.ANSI_RESET);
			goal = Search.IDP_Search(roots.get(i));

		} else if (algorithm.equalsIgnoreCase("h1")) {
			System.out.println(Node.ANSI_YELLOW + "Algorithm: A*, h1(n)" + Node.ANSI_RESET);
			goal = Search.AStar_Search(roots.get(i), Heuristic.h1);

		} else if (algorithm.equalsIgnoreCase("h2")) {
			System.out.println(Node.ANSI_YELLOW + "Algorithm: A*, h2(n)" + Node.ANSI_RESET);
			goal = Search.AStar_Search(roots.get(i), Heuristic.h2);

		} else if (algorithm.equalsIgnoreCase("h3")) {
			System.out.println(Node.ANSI_YELLOW + "Algorithm: A*, h3(n)" + Node.ANSI_RESET);
			goal = Search.AStar_Search(roots.get(i), Heuristic.h3);

		} else {
			System.out.println("Not valid algorithm ");
			return;
		}

		long endTime = System.nanoTime();
		long durationInNanos = endTime - startTime;
		double durationInMillis = (durationInNanos / 1_000_000.0);

		total_nodes += Node.num;
		System.out.println("Total Nodes Generated : " + total_nodes);

		if (goal != null) {

			total_time += durationInMillis;

			System.out.printf("\nExecution time in seconds: %.6f \n", durationInMillis / 1000);
//			List<Action> actions = new ArrayList<>();
//			actions = Search.goalPath(goal);
			System.out.println("****");
		}

		watcherThrd.interrupt();

	}

	public static void reset_stats() {
		num_files = 0;
		total_time = 0;
		total_nodes = 0;
	}

	public static void mainAll() {

		all_algs = true;
		if (alg_index == 6) {
			return;
		}
		algorithm = algos[alg_index];
		Main task = new Main(); // Assuming Main implements Runnable
		Thread taskThread = new Thread(task);

		Thread watchdog = new Thread(() -> {
			taskThread.start();
			System.out.println(Thread.activeCount());

			System.out.println(Node.ANSI_RED + "in watchdog" + Node.ANSI_RESET);
			try {
				watcherThrd = Thread.currentThread();
				Thread.sleep(15 * 60 * 1000);
				if (taskThread.isAlive()) {
					System.out.println("Timeout reached. Stopping the task...");
					System.out.println("Total nodes generated: " + Node.num);
					System.out.println("Total time taken: >15 min");
					System.out.println("Path length: Timed out.");
					System.out.println("Path: Timed out.");
					taskThread.interrupt();
				}

			} catch (InterruptedException e) {
				System.out.println("Watchdog thread was interrupted by sleep.");

			}
			i++;
			if (i == num_files) {
				i = 0;
				alg_index++;
				System.out.println(Node.ANSI_GREEN + "-------------------------------------------------");
				System.out.println("Algorithm : " + algorithm);
				System.out.println("Number of files : " + num_files);
				System.out.println("Total Nodes Generated : " + total_nodes);
				System.out.println("AVG Node number : " + total_nodes / num_files);
				System.out.println("Total Time in millis : " + total_time);
				System.out.printf("Average Time in seconds %.6f \n", (total_time / num_files) / 1000);
				System.out.println("-------------------------------------------------" + Node.ANSI_RESET);
				reset_stats();
			}
			mainAll();
//				watcherThrd.stop();		
			System.out.println(Node.ANSI_RED + "Out watchdog" + Node.ANSI_RESET);
			Thread.currentThread().interrupt();
			return;
		});

		watchdog.start();

	}

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("Usage: java PuzzleSolver <file path> <algorithm>");
			System.exit(1);
		}
		path = args[0];
		algorithm = args[1];
		if (algorithm.equalsIgnoreCase("all")) {
			mainAll();
		} else {
			Main task = new Main(); // Assuming Main implements Runnable
			Thread taskThread = new Thread(task);
			taskThrd = taskThread;

			Thread watchdog = new Thread(() -> {
				taskThread.start();

				try {
					watcherThrd = Thread.currentThread();
					Thread.sleep(7 * 60 * 1000);
					if (taskThread.isAlive()) {
						System.out.println("Timeout reached. Stopping the task...");
						System.out.println("Total nodes generated: <<??>>");
						System.out.println("Total time taken: >15 min");
						System.out.println("Path length: Timed out.");
						System.out.println("Path: Timed out.");
						taskThread.interrupt();
					}

				} catch (InterruptedException e) {
					System.out.println("Watchdog thread was interrupted by sleep.");

				}

			});

			watchdog.start();
		}

	}
}
