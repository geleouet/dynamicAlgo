package me.egaetan.dynamique;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SolveurLimit3 implements Solveur {


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
	
	public SolveurLimit3(int resetPeriode) {
		super();
		this.resetPeriode = resetPeriode;
	}

	static class State {
		
		private final int resetPeriode;

		public State(int resetPeriode) {
			this.resetPeriode = resetPeriode;
			score = new double[resetPeriode][resetPeriode][resetPeriode];
			from = new Prev[resetPeriode][resetPeriode][resetPeriode];
		}
		// 4 change max by PERIODh
		// each array represent a change and the remaining time before reset to 0
		final double[][][] score;
		final Prev[][][] from;

		public void init() {
			for (int i = 0; i < resetPeriode; i++) {
				for (int j = 0; j < resetPeriode; j++) {
					for (int k = 0; k < resetPeriode; k++) {
							score[i][j][k] = Double.MAX_VALUE;
							from[i][j][k] = new Prev(-1, null);
						}
					}
				}
			}
		public void reset() {
			for (int i = 0; i < resetPeriode; i++) {
				for (int j = 0; j < resetPeriode; j++) {
					for (int k = 0; k < resetPeriode; k++) {
							score[i][j][k] = 0;
							from[i][j][k] = new Prev(-1, null);
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
					double d0 = Double.MAX_VALUE;
					int origin0 = -1;
					double d1 = Double.MAX_VALUE;
					int origin1 = -1;
					double d2 = Double.MAX_VALUE;
					int origin2 = -1;
					for (int m = 0; m < width; m++) {
						double current0 = current[m].score[0][i][j];
						if (current0 < d0) {
							d0 = current0;
							origin0 = m;
						}
						double current1 = current[m].score[i][0][j];
						if (current1 < d1) {
							d1 = current1;
							origin1 = m;
						}
						double current2 = current[m].score[i][j][0];
						if (current2 < d2) {
							d2 = current2;
							origin2 = m;
						}
					}
					
					for (int m = 0; m < width; m++) {

						double m_score = w[m].data[z];

						next[m].score[resetPeriode - 1][i][j] = d0 + m_score;
						next[m].from[resetPeriode - 1][i][j] = current[origin0].from[0][i][j].append(m);
						next[m].score[i][resetPeriode - 1][j] = d1 + m_score;
						next[m].from[i][resetPeriode - 1][j] = current[origin1].from[i][0][j].append(m);
						next[m].score[i][j][resetPeriode - 1] = d2 + m_score;
						next[m].from[i][j][resetPeriode - 1] = current[origin2].from[i][j][0].append(m);
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
							if (current[m].score[i][j][k] < score) {
								score = current[m].score[i][j][k];
								from = current[m].from[i][j][k];
								changes = countChanges(from);
							}
							else if (current[m].score[i][j][k] == score) {
								// prefer less changes
								int currentChanges = countChanges(current[m].from[i][j][k]);
								if (currentChanges < changes) {
									changes = currentChanges;
									score = current[m].score[i][j][k];
									from = current[m].from[i][j][k];
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
					mergeReset(current, next, m, m_score, i, j);
			}
		}
	}

	private void mergeReset(State[] current, State[] next, int m, double m_score, int i, int j) {
		{
			double stay = current[m].score[0][i][j] + m_score;
			double reset   = next[m].score[0][i][j];
			if (stay <= reset) { 
				next[m].score[0][i][j] = stay;
				next[m].from[0][i][j] = current[m].from[0][i][j].append(m);
			}
			else {
				next[m].score[0][i][j] = reset;
			}
		}
		{
			double stay = current[m].score[i][0][j] + m_score;
			double reset   = next[m].score[i][0][j];
			if (stay <= reset) { 
				next[m].score[i][0][j] = stay;
				next[m].from[i][0][j] = current[m].from[i][0][j].append(m);
			}
			else {
				next[m].score[i][0][j] = reset;
			}
		}
		{
			double stay = current[m].score[i][j][0] + m_score;
			double reset   = next[m].score[i][j][0];
			if (stay <= reset) { 
				next[m].score[i][j][0] = stay;
				next[m].from[i][j][0] = current[m].from[i][j][0].append(m);
			}
			else {
				next[m].score[i][j][0] = reset;
			}
		}
	}

	private void moveToReset(State[] current, State[] next, int m, double m_score) {
		for (int i = 0; i < resetPeriode - 1; i++) {
			for (int j = 0; j < resetPeriode - 1; j++) {
				for (int k = 0; k < resetPeriode - 1; k++) {
						moveReset(current, next, m, m_score, i, j, k);
				}
			}
		}
	}

	private void moveReset(State[] current, State[] next, int m, double m_score, int i, int j, int k) {
		next[m].score[i][j][k] = current[m].score[i + 1][j + 1][k + 1] + m_score;
		next[m].from[i][j][k] = current[m].from[i + 1][j + 1][k + 1].append(m);
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
