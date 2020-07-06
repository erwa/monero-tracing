public class Pair {
	int tx;
	int idx;
	Pair(int tx, int idx) {
		this.tx = tx;
		this.idx = idx;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		if (!(o instanceof Pair)) {
			return false;
		}

		Pair p = (Pair) o;
		return p.tx == this.tx && p.idx == this.idx;
	}

	@Override
	public int hashCode() {
		return 53 * Integer.hashCode(tx) + Integer.hashCode(idx);
	}
}

