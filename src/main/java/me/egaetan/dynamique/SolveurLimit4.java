package me.egaetan.dynamique;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SolveurLimit4 implements Solveur {

	static class Prev {
		final byte current;
		final Prev previous;
		
		public Prev(int current, Prev previous) {
			super();
			this.current = (byte) current;
			this.previous = previous;
		}


		Prev append(int next) {
			return new Prev(next, this);
		}
	}
	
	private final int resetPeriode;
	
	public SolveurLimit4(int resetPeriode) {
		super();
		this.resetPeriode = resetPeriode;
	}

	static class State {
		
		private final int resetPeriode;

		public State(int resetPeriode) {
			this.resetPeriode = resetPeriode;
			score = new double[resetPeriode][resetPeriode][resetPeriode][resetPeriode];
			from = new Prev[resetPeriode][resetPeriode][resetPeriode][resetPeriode];
		}
		// 4 change max by PERIODh
		// each array represent a change and the remaining time before reset to 0
		final double[][][][] score;
		final Prev[][][][] from;

		public void init() {
			for (int i = 0; i < resetPeriode; i++) {
				for (int j = 0; j < resetPeriode; j++) {
					for (int k = 0; k < resetPeriode; k++) {
						for (int l = 0; l < resetPeriode; l++) {
							score[i][j][k][l] = Double.MAX_VALUE;
							from[i][j][k][l] = new Prev(-1, null);
						}
					}
				}
			}
		}
		public void reset() {
			for (int i = 0; i < resetPeriode; i++) {
				for (int j = 0; j < resetPeriode; j++) {
					for (int k = 0; k < resetPeriode; k++) {
						for (int l = 0; l < resetPeriode; l++) {
							score[i][j][k][l] = 0;
							from[i][j][k][l] = new Prev(-1, null);
						}
					}
				}
			}
		}
	}
	
	public int[] solve(Data... w) {
		int length = w[0].data.length;
		int width = w.length;

		State current[] = new State[width];
		for (int i = 0; i < width; i++) {
			current[i] = new State(resetPeriode);
			current[i].reset();
			
		}
		State next[] = new State[width];
		for (int i = 0; i < width; i++) {
			next[i] = new State(resetPeriode);
			next[i].init();
		}

		for (int z = 0; z < length; z++) {
			for (int m = 0; m < width; m++) {
				double m_score = w[m].data[z];

				// decalage de tous les etats vers le reset
				moveToReset(current, next, m, m_score);

				// conserve le meilleur sur le reset de chaque branche
				mergeResetPosition(current, next, m, m_score);
			}
			
			// on pose le change
			comeFromOtherBranch(width, current, next, z, w);
			
			var old = current;
			current = next;
			next = old;
		}

		return findBestPath(width, current).stream().mapToInt(i -> i).toArray();
	}

	private void comeFromOtherBranch(int width, State[] current, State[] next, int z, Data... w) {
		for (int i = 0; i < resetPeriode; i++) {
			for (int j = 0; j < resetPeriode; j++) {
				for (int k = 0; k < resetPeriode; k++) {
					double d0 = Double.MAX_VALUE;
					int origin0 = -1;
					double d1 = Double.MAX_VALUE;
					int origin1 = -1;
					double d2 = Double.MAX_VALUE;
					int origin2 = -1;
					double d3 = Double.MAX_VALUE;
					int origin3 = -1;
					for (int m = 0; m < width; m++) {
						double current0 = current[m].score[0][i][j][k];
						if (current0 < d0) {
							d0 = current0;
							origin0 = m;
						}
						double current1 = current[m].score[i][0][j][k];
						if (current1 < d1) {
							d1 = current1;
							origin1 = m;
						}
						double current2 = current[m].score[i][j][0][k];
						if (current2 < d2) {
							d2 = current2;
							origin2 = m;
						}
						double current3 = current[m].score[i][j][k][0];
						if (current3 < d3) {
							d3 = current3;
							origin3 = m;
						}
					}
					
					for (int m = 0; m < width; m++) {

						double m_score = w[m].data[z];

						next[m].score[resetPeriode - 1][i][j][k] = d0 + m_score;
						next[m].from[resetPeriode - 1][i][j][k] = current[origin0].from[0][i][j][k].append(m);
						next[m].score[i][resetPeriode - 1][j][k] = d1 + m_score;
						next[m].from[i][resetPeriode - 1][j][k] = current[origin1].from[i][0][j][k].append(m);
						next[m].score[i][j][resetPeriode - 1][k] = d2 + m_score;
						next[m].from[i][j][resetPeriode - 1][k] = current[origin2].from[i][j][0][k].append(m);
						next[m].score[i][j][k][resetPeriode - 1] = d3 + m_score;
						next[m].from[i][j][k][resetPeriode - 1] = current[origin3].from[i][j][k][0].append(m);
					}
				}
			}
		}
	}

	private List<Integer> findBestPath(int width, State[] current) {
		double score = Double.MAX_VALUE;
		int changes = Integer.MAX_VALUE;
		Prev from = null;
		for (int m = 0; m < width; m++) {
			for (int i = 0; i < resetPeriode; i++) {
				for (int j = 0; j < resetPeriode; j++) {
					for (int k = 0; k < resetPeriode; k++) {
						for (int l = 0; l < resetPeriode; l++) {
							if (current[m].score[i][j][k][l] < score) {
								score = current[m].score[i][j][k][l];
								from = current[m].from[i][j][k][l];
								changes = countChanges(from);
							}
							else if (current[m].score[i][j][k][l] == score) {
								// prefer less changes
								int currentChanges = countChanges(current[m].from[i][j][k][l]);
								if (currentChanges < changes) {
									changes = currentChanges;
									score = current[m].score[i][j][k][l];
									from = current[m].from[i][j][k][l];
								}
							}
						}
					}
				}
			}
		}
		Prev c = from;
		List<Integer> path = new ArrayList<>();
		while (c.current != -1) {
			path.add((int) c.current);
			c = c.previous;
		}
		Collections.reverse(path);
		return path;
	}

	private void mergeResetPosition(State[] current, State[] next, int m, double m_score) {
		for (int i = 0; i < resetPeriode - 1; i++) {
			for (int j = 0; j < resetPeriode - 1; j++) {
				for (int k = 0; k < resetPeriode - 1; k++) {
					mergeReset(current, next, m, m_score, i, j, k);
				}
			}
		}
	}

	private void mergeReset(State[] current, State[] next, int m, double m_score, int i, int j, int k) {
		{
			double stay = current[m].score[0][i][j][k] + m_score;
			double reset   = next[m].score[0][i][j][k];
			if (stay <= reset) { 
				next[m].score[0][i][j][k] = stay;
				next[m].from[0][i][j][k] = current[m].from[0][i][j][k].append(m);
			}
			else {
				next[m].score[0][i][j][k] = reset;
			}
		}
		{
			double stay = current[m].score[i][0][j][k] + m_score;
			double reset   = next[m].score[i][0][j][k];
			if (stay <= reset) { 
				next[m].score[i][0][j][k] = stay;
				next[m].from[i][0][j][k] = current[m].from[i][0][j][k].append(m);
			}
			else {
				next[m].score[i][0][j][k] = reset;
			}
		}
		{
			double stay = current[m].score[i][j][0][k] + m_score;
			double reset   = next[m].score[i][j][0][k];
			if (stay <= reset) { 
				next[m].score[i][j][0][k] = stay;
				next[m].from[i][j][0][k] = current[m].from[i][j][0][k].append(m);
			}
			else {
				next[m].score[i][j][0][k] = reset;
			}
		}
		{
			double stay = current[m].score[i][j][k][0] + m_score;
			double reset   = next[m].score[i][j][k][0];
			if (stay <= reset) { 
				next[m].score[i][j][k][0] = stay;
				next[m].from[i][j][k][0] = current[m].from[i][j][k][0].append(m);
			}
			else {
				next[m].score[i][j][k][0] = reset;
			}
		}
	}

	private void moveToReset(State[] current, State[] next, int m, double m_score) {
		for (int i = 0; i < resetPeriode - 1; i++) {
			for (int j = 0; j < resetPeriode - 1; j++) {
				for (int k = 0; k < resetPeriode - 1; k++) {
					for (int l = 0; l < resetPeriode - 1; l++) {
						moveReset(current, next, m, m_score, i, j, k, l);
					}
				}
			}
		}
	}

	private void moveReset(State[] current, State[] next, int m, double m_score, int i, int j, int k, int l) {
		next[m].score[i][j][k][l] = current[m].score[i + 1][j + 1][k + 1][l + 1] + m_score;
		next[m].from[i][j][k][l] = current[m].from[i + 1][j + 1][k + 1][l + 1].append(m);
	}

	private int countChanges(Prev from) {
		Prev c = from;
		int or = from.current;
		int currentChanges = 0;
		while (c.current != -1) {
			if(c.current != or) {
				or = c.current;
				currentChanges ++;
			}
			c = c.previous;
		}
		return currentChanges;
	}

}
