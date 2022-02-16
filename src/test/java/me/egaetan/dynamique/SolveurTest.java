package me.egaetan.dynamique;

import java.util.Arrays;
import java.util.Random;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import me.egaetan.dynamique.Solveur.Data;

class SolveurTest {

	@Test // long test ~ 500ms / step
	void should_solve_first_then_quit() {
		// GIVEN
		Solveur solveur = new Solveur(24);
		double a0[] = new double[] {10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.};
		double a1[] = new double[] {1. ,20.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.};
		double a2[] = new double[] {20.,10.,10.,20.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.};
		double a3[] = new double[] {20.,10.,10.,20.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.};
		double a4[] = new double[] {20.,10.,10.,20.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.,10.};
		

		// WHEN
		int[] result = solveur.solve(new Solveur.Data(a0), new Solveur.Data(a1), new Solveur.Data(a2), new Solveur.Data(a3), new Solveur.Data(a4));
		
		// THEN
		Assertions.assertThat(result).isEqualTo(new int[] {1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0});
	}
	
	//@Test // long test ~ 100 minutes
	void should_solve_real_datas() {
		// GIVEN
		Random r = new Random();
		r.setSeed("REAL".hashCode());
		Solveur solveur = new Solveur(24);
		Solveur.Data[] datas = new Solveur.Data[10];
		for (int i = 0; i < 10; i++) {
			double[] a = new double[364*24];
			for (int h = 0; h < 364*24; h++) {
				a[h] = r.nextDouble() * 100;
			}
			datas[i] = new Data(a);
		}
		
		// WHEN
		int[] result = solveur.solve(datas);
		
		// THEN
		Assertions.assertThat(result).isNotEmpty();
	}
	
	@Test // long test ~ 100s
	void should_solve_real_datas_small_window() {
		// GIVEN
		Random r = new Random();
		r.setSeed("REAL".hashCode());
		Solveur solveur = new Solveur(12);
		Solveur.Data[] datas = new Solveur.Data[10];
		for (int i = 0; i < 10; i++) {
			double[] a = new double[364*12];
			for (int h = 0; h < 364*12; h++) {
				a[h] = r.nextDouble() * 100;
			}
			datas[i] = new Data(a);
		}
		
		// WHEN
		int[] result = solveur.solve(datas);
		
		// THEN
		int sum = Arrays.stream(result).sum();
		int sumAlt = altSum(result);
		int sumAlt2 = altSum2(result);
		
		System.out.println(sum + " " + sumAlt + " " + sumAlt2);
		Assertions.assertThat(result).isNotEmpty();
		Assertions.assertThat(sum).isEqualTo(19510);
		Assertions.assertThat(sumAlt).isEqualTo(-96);
		Assertions.assertThat(sumAlt2).isEqualTo(77948);
	}

	private int altSum(int[] result) {
		int r = 0;
		for (int i = 0; i < result.length; i++) {
			r+=(i%2==0 ? result[i] : -result[i]);
		}
		return r;
	}

	private int altSum2(int[] result) {
		int r = 0;
		for (int i = 0; i < result.length; i++) {
			r+=(1+(i%7)) * result[i];
		}
		return r;
	}

	@Test
	void should_solve_back_and_forth() {
		// GIVEN
		Solveur solveur = new Solveur(3);
		double a0[] = new double[] { 10., 10., 10., 1.};
		double a1[] = new double[] {  1., 20.,  1., 10.};
		double a2[] = new double[] { 20., 10., 10., 20.};
		double a3[] = new double[] { 20., 10., 10., 20.};
		double a4[] = new double[] { 20., 10., 10., 20.};
		
		// WHEN
		int[] result = solveur.solve(new Solveur.Data(a0), new Solveur.Data(a1), new Solveur.Data(a2), new Solveur.Data(a3), new Solveur.Data(a4));
		
		// THEN
		Assertions.assertThat(result).isEqualTo(new int[] {1, 0, 1, 0});
	}
	@Test
	void should_solve_back_and_forth_prefer_stay_at_end() {
		// GIVEN
		Solveur solveur = new Solveur(3);
		double a0[] = new double[] { 10., 10., 10., 10.};
		double a1[] = new double[] {  1., 20.,  1., 10.};
		double a2[] = new double[] { 20., 10., 10., 20.};
		double a3[] = new double[] { 20., 10., 10., 20.};
		double a4[] = new double[] { 20., 10., 10., 20.};
		
		// WHEN
		int[] result = solveur.solve(new Solveur.Data(a0), new Solveur.Data(a1), new Solveur.Data(a2), new Solveur.Data(a3), new Solveur.Data(a4));
		
		// THEN
		Assertions.assertThat(result).isEqualTo(new int[] {1, 0, 1, 1});
	}

	@Test
	void should_solve_with_constraint() {
		// GIVEN
		Solveur solveur = new Solveur(3);
		double a0[] = new double[] { 10., 10., 10., 10., 10., 10., 10., 10.};
		double a1[] = new double[] {  1., 20.,  1., 20.,  1., 20.,  1., 20.};
		double a2[] = new double[] { 20., 10., 10., 20., 10., 20., 10., 20.};
		double a3[] = new double[] { 20., 10., 10., 20., 10., 20., 10., 20.};
		double a4[] = new double[] { 20., 10., 10., 20., 10., 20., 10., 20.};
		
		// WHEN
		int[] result = solveur.solve(new Solveur.Data(a0), new Solveur.Data(a1), new Solveur.Data(a2), new Solveur.Data(a3), new Solveur.Data(a4));
		
		// THEN
		Assertions.assertThat(result).isEqualTo(new int[] {1, 0, 1, 0, 0, 0, 1, 0});
	}
	
	@Test
	void should_solve_with_constraint_until_4_changes() {
		// GIVEN
		Solveur solveur = new Solveur(7);
		double a0[] = new double[] { 10., 10., 10., 10., 10., 10., 10., 10., 10.};
		double a1[] = new double[] {  1., 20.,  1., 20.,  1., 20.,  1., 20.,  1.};
		double a2[] = new double[] { 20., 10., 10., 20., 10., 20., 10., 20., 10.};
		double a3[] = new double[] { 20., 10., 10., 20., 10., 20., 10., 20., 20.};
		double a4[] = new double[] { 20., 10., 10., 20., 10., 20., 10., 20., 20.};
		
		// WHEN
		int[] result = solveur.solve(new Solveur.Data(a0), new Solveur.Data(a1), new Solveur.Data(a2), new Solveur.Data(a3), new Solveur.Data(a4));
		
		// THEN
		Assertions.assertThat(result).isEqualTo(new int[] {1, 0, 1, 0, 0, 0, 0, 0, 1});
	}
	@Test
	void should_solve_with_constraint_until_4_changes_with_window() {
		// GIVEN
		Solveur solveur = new Solveur(3);
		double a0[] = new double[] { 10., 10., 10., 10., 10., 10., 10., 10., 10.};
		double a1[] = new double[] {  1., 20.,  1., 20.,  1., 20.,  1., 20.,  1.};
		double a2[] = new double[] { 20., 10., 10., 20., 10., 20., 10., 20., 10.};
		double a3[] = new double[] { 20., 10., 10., 20., 10., 20., 10., 20., 20.};
		double a4[] = new double[] { 20., 10., 10., 20., 10., 20., 10., 20., 20.};
		
		// WHEN
		int[] result = solveur.solve(new Solveur.Data(a0), new Solveur.Data(a1), new Solveur.Data(a2), new Solveur.Data(a3), new Solveur.Data(a4));
		
		// THEN
		Assertions.assertThat(result).isEqualTo(new int[] {1, 0, 1, 0, 0, 0, 1, 0, 1});
	}
	@Test
	void should_solve_with_constraint_until_4_changes_with_window_at_4_and_7() {
		// GIVEN
		double a0[] = new double[] { 10., 10., 50., 70., 10., 10., 10., 50., 10., 80.};
		double a1[] = new double[] {  1., 20.,  1., 50.,  1., 20.,  1., 20., 90.,  1.};
		double a2[] = new double[] { 20., 10., 10., 80., 50., 20., 10., 20.,  1., 80.};
		double a3[] = new double[] { 20., 10., 80., 20., 10., 20., 10., 10., 20., 80.};
		double a4[] = new double[] { 20., 10., 10., 20., 10., 20., 10., 20., 20., 80.};
		
		// WHEN
		Solveur solveur4 = new Solveur(4);
		int[] result4 = solveur4.solve(new Solveur.Data(a0), new Solveur.Data(a1), new Solveur.Data(a2), new Solveur.Data(a3), new Solveur.Data(a4));
		
		// THEN
		Assertions.assertThat(result4).isEqualTo(new int[] {1, 4, 4, 4, 1, 1, 1, 3, 2, 1});
		
		
		// WHEN
		Solveur solveur5 = new Solveur(7);
		int[] result5 = solveur5.solve(new Solveur.Data(a0), new Solveur.Data(a1), new Solveur.Data(a2), new Solveur.Data(a3), new Solveur.Data(a4));
		
		// THEN
		Assertions.assertThat(result5).isEqualTo(new int[] {1, 4, 4, 4, 1, 1, 1, 2, 2, 1});
		
		
	}
	
	@Test
	void should_solve_with_constraint_until_4_changes_with_window_at_12() {
		// GIVEN
		double a0[] = new double[] { 10., 10., 50., 70., 10., 10., 10., 50., 10., 80.};
		double a1[] = new double[] {  1., 20.,  1., 50.,  1., 20.,  1., 20., 90.,  1.};
		double a2[] = new double[] { 20., 10., 10., 80., 50., 20., 10., 20.,  1., 80.};
		double a3[] = new double[] { 20., 10., 80., 20., 10., 20., 10., 10., 20., 80.};
		double a4[] = new double[] { 20., 10., 10., 20., 10., 20., 10., 20., 20., 80.};

		// WHEN
		Solveur solveur4 = new Solveur(12);
		int[] result4 = solveur4.solve(new Solveur.Data(a0), new Solveur.Data(a1), new Solveur.Data(a2), new Solveur.Data(a3), new Solveur.Data(a4));

		// THEN
		Assertions.assertThat(result4).isEqualTo(new int[] {1, 4, 4, 4, 1, 1, 1, 2, 2, 1});
	}
	
	@Test
	void should_solve_with_constraint_until_4_changes_with_window_at_12_big_constraint_at_the_end() {
		// GIVEN
		double a0[] = new double[] { 10., 10., 50., 70., 10., 10., 200., 50., 110., 180., 180.};
		double a1[] = new double[] {  1., 20., 19., 50.,  9., 20.,  1., 200., 190.,  1., 190.};
		double a2[] = new double[] { 20., 10., 10., 80., 50., 20., 200., 20.,  1., 180., 180.};
		double a3[] = new double[] { 20., 10., 80., 20., 10., 20., 200.,  1., 120., 180., 180.};
		double a4[] = new double[] { 20., 10., 40., 20., 10., 20., 200., 20., 120., 180.,  1.};

		// WHEN
		Solveur solveur4 = new Solveur(12);
		int[] result4 = solveur4.solve(new Solveur.Data(a0), new Solveur.Data(a1), new Solveur.Data(a2), new Solveur.Data(a3), new Solveur.Data(a4));

		// THEN
		Assertions.assertThat(result4).isEqualTo(new int[] {1, 1, 1, 1, 1, 1, 1, 3, 2, 1, 4});
	}
	

}
