package me.egaetan.dynamique;

import java.util.function.IntFunction;

public interface Solveur {

	static class Data {
		final double[] data;

		public Data(double[] data) {
			super();
			this.data = data;
		}
	}

	public int[] solve(Data... w);

	public static SolveurBuilder limit(int limite) {
		switch (limite) {
		case 3:
			return new SolveurBuilder(periode -> new SolveurLimit3(periode));
		case 4:
			return new SolveurBuilder(periode -> new SolveurLimit4(periode));

		default:
			throw new RuntimeException("Invalide limit 3 and 4 are supported");
		}
	}

	public class SolveurBuilder {

		private IntFunction<Solveur> constructor;

		public SolveurBuilder(IntFunction<Solveur> constructor) {
			this.constructor = constructor;
		}
		
		public Solveur periode(int p) {
			return constructor.apply(p);
		}
		
	}
}
