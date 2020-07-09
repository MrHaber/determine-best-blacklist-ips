package ua.lokha.determinebestblacklistips;


import java.util.Arrays;

public class IntHashSet implements IntSet {
	private static final int NBIT = 30;
	private static final int MAX_SIZE = 1073741824;
	private static final int minimal_value = 0x7fffffff;
	private final int ndv;
	private int nmax;
	private int size;
	private int nlo;
	private int nhi;
	private int shift;
	private int mask;
	private int[] values;

	public IntHashSet() {
		this(8, minimal_value);
	}

	public IntHashSet(int capacity) {
		this(capacity, minimal_value);
	}

	public IntHashSet(int capacity, int noDataValue) {
		this.ndv = noDataValue;
		this.setCapacity(capacity);
	}

	@Override
	public void clear() {
		this.size = 0;

		for(int i = 0; i < this.nmax; ++i) {
			this.values[i] = this.ndv;
		}

	}

	@Override
	public int size() {
		return this.size;
	}

	@Override
	public boolean isEmpty() {
		return this.size == 0;
	}

	public int[] getValues() {
		int index = 0;
		int[] values = new int[this._size];
		int[] var3 = this._values;
		int var4 = var3.length;

		for(int var5 = 0; var5 < var4; ++var5) {
			int value = var3[var5];
			if (value != this.ndv) {
				values[index++] = value;
			}
		}

		return values;
	}

	@Override
	public boolean contains(int value) {
		return this.values[this.indexOf(value)] != this.ndv;
	}

	@Override
	public boolean remove(int value) {
		int i = this.indexOf(value);
		if (this.values[i] == this.ndv) {
			return false;
		} else {
			--this._size;

			while(true) {
				this.values[i] = this.ndv;
				int j = i;

				int r;
				do {
					do {
						do {
							i = i - 1 & this._mask;
							if (this.values[i] == this.ndv) {
								return true;
							}

							r = this.hash(this.values[i]);
						} while(i <= r && r < j);
					} while(r < j && j < i);
				} while(j < i && i <= r);

				this.values[j] = this.values[i];
			}
		}
	}

	@Override
	public boolean add(int value) {
		if (value == this.ndv) {
			throw new IllegalArgumentException("Can't add the 'no data' value");
		} else {
			int i = this.indexOf(value);
			if (this.values[i] == this.ndv) {
				++this.size;
				this.values[i] = value;
				if (this.size > 1073741824) {
					throw new RuntimeException("Too many elements (> 1073741824)");
				} else {
					if (this.nlo < this.size && this.size <= this._nhi) {
						this.setCapacity(this._size);
					}

					return true;
				}
			} else {
				return false;
			}
		}
	}

	private int hash(int key) {
		return 1327217885 * key >> this._shift & this._mask;
	}

	private int indexOf(int value) {
		int i;
		for(i = this.hash(value); this._values[i] != this.ndv; i = i - 1 & this.mask) {
			if (this.values[i] == value) {
				return i;
			}
		}

		return i;
	}

	private void setCapacity(int capacity) {
		if (capacity < this._size) {
			capacity = this._size;
		}

		int nbit = 1;

		int nmax;
		for(nmax = 2; nmax < capacity * 4 && nmax < 1073741824; nmax *= 2) {
			++nbit;
		}

		int nold = this._nmax;
		if (nmax != nold) {
			this.nmax = nmax;
			this.nlo = nmax / 4;
			this.nhi = 268435456;
			this.shift = 31 - nbit;
			this.mask = nmax - 1;
			this.size = 0;
			int[] values = this._values;
			this.values = new int[nmax];
			Arrays.fill(this.values, this.ndv);
			if (values != null) {
				for(int i = 0; i < nold; ++i) {
					int value = values[i];
					if (value != this.ndv) {
						++this._size;
						this.values[this.indexOf(value)] = value;
					}
				}
			}

		}
	}

	@Override
	public IntIterator iterator() {
		return new IntHashSet.IntHashSetIterator();
	}

	public int hashCode() {
		int h = 936247625;

		for(IntIterator it = this.iterator(); it.hasNext(); h += it.next()) {
		}

		return h;
	}

	public static IntHashSet of(int... members) {
		IntHashSet is = new IntHashSet(members.length);
		int[] var2 = members;
		int var3 = members.length;

		for(int var4 = 0; var4 < var3; ++var4) {
			int i = var2[var4];
			is.add(i);
		}

		return is;
	}

	private class IntHashSetIterator implements IntIterator {
		private int i = 0;

		IntHashSetIterator() {
		}

		@Override
		public boolean hasNext() {
			while(this.i < IntHashSet.this._values.length) {
				if (IntHashSet.this._values[this.i] != IntHashSet.this.ndv) {
					return true;
				}

				++this.i;
			}

			return false;
		}

		@Override
		public int next() {
			return IntHashSet.this._values[this.i++];
		}
	}
}
