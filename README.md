# 8-Puzzle
This project is to solve the 8-Puzzle problem using different algorithms and heuristics functions. The project was tested out on numerous problems with varying depth, and I made a comparison between the different results from different algorithms and heuristics. 

# Project Files
The _.txt_ files in part 2 and 3 are Puzzle samples, while the files with algorithms names and "output" refer to the result from the running the algorithm and saving the steps to solve the problems - if the solution is approachable.  

# Sample Output 
The algorithms print results sarting from root, and step by step, till the solution is found.

A sample: \n
------------------BFS------------------
root
Lvl: 0, #1
+———+———+———+
| 3 | 5 | 2 |
+———+———+———+
| 6 | 1 | 7 |
+———+———+———+
| _ | 8 | 4 |
+———+———+———+  🪵

DOWN,  
Child of 1
Lvl: 1, #1
+———+———+———+
| 3 | 5 | 2 |
+———+———+———+
| _ | 1 | 7 |
+———+———+———+
| 6 | 8 | 4 |
+———+———+———+

UP 
Child of node #1 is visited 
--
LEFT,  
Child of 1
Lvl: 1, #2
+———+———+———+
| 3 | 5 | 2 |
+———+———+———+
| 6 | 1 | 7 |
+———+———+———+
| 8 | _ | 4 |
+———+———+———+

.
.
.

LEFT,  
Child of 4639
Lvl: 20, #4641
+———+———+———+
| 1 | 2 | 3 |
+———+———+———+
| 4 | 5 | 6 |
+———+———+———+
| 7 | 8 | _ |
+———+———+———+  ✅
