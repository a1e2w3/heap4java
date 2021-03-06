package hust.idc.util.matrix;

import hust.idc.util.pair.UnorderedPair;

import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class LinkedSymmetricMatrix<K, V> extends AbstractSymmetricMatrix<K, V>
		implements SymmetricMatrix<K, V>, Matrix<K, K, V>, Cloneable, java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6082087458154465268L;
	transient HeadNode headsEntry;
	transient int size, demension;

	transient volatile int modCount = 0;

	public LinkedSymmetricMatrix() {
		super();
		headsEntry = null;
		size = 0;
		demension = 0;
	}

	public LinkedSymmetricMatrix(
			SymmetricMatrix<? extends K, ? extends V> otherMatrix) {
		this();
		this.putAll(otherMatrix);
		// modCount = 0;
	}

	public LinkedSymmetricMatrix(
			Map<? extends UnorderedPair<? extends K>, ? extends V> otherMatrix) {
		this();
		this.putAll(otherMatrix);
		// modCount = 0;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return size;
	}

	@Override
	public int dimension() {
		// TODO Auto-generated method stub
		return demension;
	}

	HeadNode getHead(Object key) {
		HeadNode head = headsEntry;
		if (key == null) {
			while (head != null) {
				if (head.getKey() == null)
					return head;
				head = head.next;
			}
		} else {
			while (head != null) {
				if (key.equals(head.getKey()))
					return head;
				head = head.next;
			}
		}
		return null;
	}

	@Override
	public boolean containsRowOrColumn(Object row) {
		// TODO Auto-generated method stub
		return getHead(row) != null;
	}

	@Override
	public boolean containsKey(Object row, Object column) {
		// TODO Auto-generated method stub
		return getEntry(row, column) != null;
	}

	EntryNode getEntry(Object row, Object column) {
		return getEntry(getHead(row), column);
	}
	
	EntryNode getEntry(HeadNode rowHead, Object column) {
		if(rowHead == null)
			return null;

		EntryNode node = rowHead.rowEntry;
		if (column == null) {
			while (node != null) {
				if (node.getColumnKey() == null)
					return node;
				node = node.right;
			}
		} else {
			while (node != null) {
				if (column.equals(node.getColumnKey()))
					return node;
				node = node.right;
			}
		}

		node = rowHead.columnEntry;
		if (column == null) {
			while (node != null) {
				if (node.getRowKey() == null)
					return node;
				node = node.lower;
			}
		} else {
			while (node != null) {
				if (column.equals(node.getRowKey()))
					return node;
				node = node.lower;
			}
		}
		return null;
	}

	@Override
	public V get(Object row, Object column) {
		// TODO Auto-generated method stub
		EntryNode node = getEntry(row, column);
		return node == null ? null : node.getValue();
	}

	@Override
	public V put(K row, K column, V value) {
		// TODO Auto-generated method stub
		return setValue(addHeadIfNotExists(row), addHeadIfNotExists(column),
				value);
	}

	private V setValue(HeadNode rowHead, HeadNode columnHead, V value) {
		if (rowHead.index == columnHead.index) {
			// value will be set on the diagonal
			EntryNode cur = rowHead.rowEntry;
			if (cur == null) {
				++modCount;
				rowHead.rowEntry = new EntryNode(value, rowHead, columnHead);
				++size;
				rowHead.addSize(1);
				return null;
			}
			while (cur.right != null) {
				cur = cur.right;
			}
			if (cur.columnHead == columnHead) {
				return cur.setValue(value);
			} else {
				++modCount;
				cur.right = new EntryNode(value, rowHead, columnHead);
				++size;
				rowHead.addSize(1);
				return null;
			}

		} else if (rowHead.index > columnHead.index) {
			// value will be set at (rowHead.index, columnHead.index)
			EntryNode rowCur = rowHead.rowEntry, left = null;
			while (rowCur != null && rowCur.columnHead.index < columnHead.index) {
				left = rowCur;
				rowCur = rowCur.right;
			}
			EntryNode columnCur = columnHead.columnEntry, up = null;
			while (columnCur != null && columnCur.rowHead.index < rowHead.index) {
				up = columnCur;
				columnCur = columnCur.lower;
			}

			if (rowCur == null || rowCur.columnHead.index > columnHead.index) {
				if (columnCur == null
						|| columnCur.rowHead.index > rowHead.index) {
					++modCount;
					EntryNode newNode = new EntryNode(value, rowHead,
							columnHead);
					newNode.left = left;
					newNode.right = rowCur;
					newNode.upper = up;
					newNode.lower = columnCur;

					// link left and right
					if (left == null)
						rowHead.rowEntry = newNode;
					else
						left.right = newNode;
					if (rowCur != null)
						rowCur.left = newNode;

					// link up and down
					if (up == null)
						columnHead.columnEntry = newNode;
					else
						up.lower = newNode;
					if (columnCur != null)
						columnCur.upper = newNode;

					rowHead.addSize(1);
					columnHead.addSize(1);
					++size;
					return null;
				} else {
					if (left == null)
						rowHead.rowEntry = columnCur;
					else
						left.right = columnCur;
					if (rowCur != null)
						rowCur.left = columnCur;

					columnCur.rowHead = rowHead;
					columnCur.left = left;
					columnCur.right = rowCur;
					return columnCur.setValue(value);
				}
			} else {
				if (columnCur == rowCur) {
					return rowCur.setValue(value);
				} else {
					if (up == null)
						columnHead.columnEntry = rowCur;
					else
						up.lower = rowCur;
					if (columnCur != null)
						columnCur.upper = rowCur;

					rowCur.columnHead = columnHead;
					rowCur.lower = columnCur;
					rowCur.upper = up;
					return rowCur.setValue(value);
				}
			}
		} else {
			// value will be set at (columnHead.index, rowHead.index)
			return setValue(columnHead, rowHead, value);
		}
	}

	private HeadNode addHeadIfNotExists(K key) {
		if (headsEntry == null) {
			headsEntry = new HeadNode(key, 0);
			demension = 1;
			return headsEntry;
		}

		HeadNode head = headsEntry;
		if (key == null) {
			if (head.getKey() == null)
				return head;
			while (head.next != null) {
				head = head.next;
				if (head.getKey() == null)
					return head;
			}
		} else {
			if (key.equals(head.getKey()))
				return head;
			while (head.next != null) {
				head = head.next;
				if (key.equals(head.getKey()))
					return head;
			}
		}

		head.next = new HeadNode(key, head.index + 1);
		head.next.prev = head;
		++demension;
		return head.next;
	}

	@Override
	public V remove(Object row, Object column) {
		// TODO Auto-generated method stub
		return this.removeNode(getEntry(row, column));
	}

	private V removeNode(EntryNode node) {
		if (node == null)
			return null;

		if (node.isDiagonal()) {
			if (node.left == null)
				node.rowHead.rowEntry = null;
			else
				node.left.right = null;
		} else {
			if (node.left == null)
				node.rowHead.rowEntry = node.right;
			else
				node.left.right = node.right;
			if (node.right != null)
				node.right.left = node.left;

			if (node.upper == null)
				node.columnHead.columnEntry = node.lower;
			else
				node.upper.lower = node.lower;
			if (node.lower != null)
				node.lower.upper = node.upper;
		}

		if (node.rowHead.addSize(-1) == 0) {
			this.removeHead(node.rowHead);
		}
		if (!node.isDiagonal() && node.columnHead.addSize(-1) == 0) {
			this.removeHead(node.columnHead);
		}
		V value = node.value;
		node.dispose();
		++modCount;
		--size;
		return value;
	}

	@Override
	public void removeRowAndColumn(K key) {
		// TODO Auto-generated method stub
		this.removeKey(getHead(key));
	}

	private void removeKey(HeadNode head) {
		if (head == null)
			return;
		EntryNode node = head.rowEntry, next = null;
		while (node != null) {
			next = node.right;
			this.removeNode(node);
			node = next;
		}
		node = head.columnEntry;
		next = null;
		while (node != null) {
			next = node.lower;
			this.removeNode(node);
			node = next;
		}
		++modCount;
		if(!head.disposed())
			this.removeHead(head);
	}

	private void removeHead(HeadNode head) {
		if (head == null)
			return;

		if (head.prev == null)
			headsEntry = head.next;
		else
			head.prev.next = head.next;
		if (head.next != null)
			head.next.prev = head.prev;
		head.dispose();
		--demension;
	}
	
	/**
	 * Returns a shallow copy of this <tt>LinkedSymmetricMatrix</tt> instance: the keys and
	 * values themselves are not cloned.
	 * 
	 * @return a shallow copy of this matrix
	 */
	@Override
	public LinkedSymmetricMatrix<K, V> clone() {
		// TODO Auto-generated method stub
		try {
			LinkedSymmetricMatrix<K, V> v = (LinkedSymmetricMatrix<K, V>) super.clone();
			v.size = v.demension = 0;
			v.headsEntry = null;
		    v.putAll(this);
		    v.modCount = 0;
		    return v;
		} catch (CloneNotSupportedException e) {
		    // this shouldn't happen, since we are Cloneable
			e.printStackTrace();
		    throw new InternalError();
		}
	}

	@Override
	protected int valueCount(Object key) {
		// TODO Auto-generated method stub
		HeadNode head = getHead(key);
		return head == null ? 0 : head.size;
	}

	@Override
	public Map<K, V> keyMap(final K key) {
		// TODO Auto-generated method stub
		HeadNode head = this.getHead(key);
		if (null == head)
			return new KeyMapView(key, head);

		if (null == head.viewMap) {
			head.viewMap = new KeyMapView(key, head);
		}
		return head.viewMap;
	}

	private class KeyMapView extends AbstractKeyMapView {
		private transient volatile HeadNode head;

		private KeyMapView(K key, HeadNode head) {
			super(key);
			this.head = head;
		}
		
		private final int modCount() {
			return headNotExists() ? -1 : head.modCount;
		}
		
		private final boolean headNotExists() {
			if (head != null && head.disposed())
				head = null;
			return head == null;
		}
		
		HeadNode validateHead() {
			if (headNotExists()) {
				head = LinkedSymmetricMatrix.this.getHead(viewKey);
				if (head != null && head.viewMap == null)
					head.viewMap = this;
			}
			return head;
		}

		@Override
		public V put(K key, V value) {
			// TODO Auto-generated method stub
			if (validateHead() == null) {
				head = LinkedSymmetricMatrix.this.addHeadIfNotExists(viewKey);
				head.viewMap = this;
			}
			return LinkedSymmetricMatrix.this.setValue(head,
					addHeadIfNotExists(key), value);
		}

		@Override
		public boolean containsKey(Object key) {
			// TODO Auto-generated method stub
			if (validateHead() == null)
				return false;
			return getEntry(head, key) != null;
		}

		@Override
		public V get(Object key) {
			// TODO Auto-generated method stub
			if (validateHead() == null)
				return null;
			return getEntry(head, key).getValue();
		}

		@Override
		public V remove(Object key) {
			// TODO Auto-generated method stub
			if (validateHead() == null)
				return null;
			V oldValue = removeNode(getEntry(head, key));
			if(head.disposed())
				head = null;
			return oldValue;
		}

		@Override
		public void clear() {
			// TODO Auto-generated method stub
			removeKey(validateHead());
			head = null;
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return validateHead() == null ? 0 : head.size;
		}

		@Override
		public Set<Map.Entry<K, V>> entrySet() {
			// TODO Auto-generated method stub
			validateHead();
			return super.entrySet();
		}
		
		@Override
		Iterator<Map.Entry<K, V>> entryIterator() {
			// TODO Auto-generated method stub
			validateHead();
			return new Iterator<Map.Entry<K, V>>() {
				private EntryNode current = null, next = null;
				private boolean inRow = true;
				private boolean currentRemoved = false;

				private int expectedModCount = KeyMapView.this.modCount();

				@Override
				public boolean hasNext() {
					// TODO Auto-generated method stub
					return next == null ? getNext() : true;
				}

				private boolean getNext() {
					checkModCount();
					if (current == null) {
						if (head == null)
							return false;
						if (head.rowEntry != null) {
							inRow = true;
							next = head.rowEntry;
						} else {
							next = head.columnEntry;
							inRow = false;
						}
						return next != null;
					} else {
						if (inRow) {
							if ((next = current.right) == null) {
								inRow = false;
								return (next = head.columnEntry) != null;
							} else {
								return true;
							}
						} else {
							return (next = current.lower) != null;
						}
					}
				}

				private void checkModCount() {
					if (expectedModCount != KeyMapView.this.modCount())
						throw new ConcurrentModificationException();
				}

				@Override
				public Map.Entry<K, V> next() {
					// TODO Auto-generated method stub
					if (next == null && !getNext())
						throw new NoSuchElementException();
					currentRemoved = false;
					current = next;
					next = null;
					return inRow ? current.rowMapEntry() : current
							.columnMapEntry();
				}

				@Override
				public void remove() {
					// TODO Auto-generated method stub
					checkModCount();
					if (currentRemoved || current == null)
						throw new IllegalStateException();
					if (next == null)
						this.getNext();
					LinkedSymmetricMatrix.this.removeNode(current);
					if (head.disposed()) {
						current = null;
						next = null;
						head = null;
					}
					currentRemoved = true;
					expectedModCount = KeyMapView.this.modCount();
				}

			};
		}

	}
	
	@Override
	void clearViews() {
		super.clearViews();
		keySet = null;
		entrySet = null;
	}

	// View
	protected transient volatile Set<K> keySet = null;
	protected transient volatile Set<Entry<K, K, V>> entrySet = null;

	@Override
	public Set<K> keySet() {
		// TODO Auto-generated method stub
		if (keySet == null) {
			keySet = new AbstractSet<K>() {

				@Override
				public Iterator<K> iterator() {
					// TODO Auto-generated method stub
					return new Iterator<K>() {
						private HeadNode curHead = null;
						private boolean currentRemoved = false;

						private int expectedModCount = LinkedSymmetricMatrix.this.modCount;

						private void checkModCount() {
							if (this.expectedModCount != LinkedSymmetricMatrix.this.modCount)
								throw new ConcurrentModificationException();
						}

						@Override
						public boolean hasNext() {
							// TODO Auto-generated method stub
							checkModCount();
							if (curHead == null)
								return LinkedSymmetricMatrix.this.headsEntry != null;
							else
								return curHead.next != null;
						}

						private boolean getNext() {
							checkModCount();
							if (curHead == null) {
								return (curHead = LinkedSymmetricMatrix.this.headsEntry) != null;
							} else {
								if (curHead.next == null)
									return false;
								curHead = curHead.next;
								return true;
							}
						}

						@Override
						public K next() {
							// TODO Auto-generated method stub
							if (!getNext())
								throw new NoSuchElementException();
							currentRemoved = false;
							return curHead.getKey();
						}

						@Override
						public void remove() {
							// TODO Auto-generated method stub
							checkModCount();
							if (currentRemoved || curHead == null)
								throw new IllegalStateException();

							HeadNode prev = curHead.prev;
							LinkedSymmetricMatrix.this.removeKey(curHead);
							curHead = prev;
							currentRemoved = true;
							expectedModCount = LinkedSymmetricMatrix.this.modCount;
						}

					};
				}

				@Override
				public int size() {
					// TODO Auto-generated method stub
					return LinkedSymmetricMatrix.this.demension;
				}

			};
		}
		return keySet;
	}

	@Override
	public Set<Matrix.Entry<K, K, V>> entrySet() {
		// TODO Auto-generated method stub
		if (entrySet == null) {
			entrySet = new AbstractSet<Entry<K, K, V>>() {

				@Override
				public Iterator<hust.idc.util.matrix.Matrix.Entry<K, K, V>> iterator() {
					// TODO Auto-generated method stub
					return LinkedSymmetricMatrix.this.nodeIterator();
				}

				@Override
				public int size() {
					// TODO Auto-generated method stub
					return LinkedSymmetricMatrix.this.size;
				}

			};
		}
		return entrySet;
	}

	Iterator<Entry<K, K, V>> nodeIterator() {
		return new Iterator<Entry<K, K, V>>() {
			private HeadNode nextHead = LinkedSymmetricMatrix.this.headsEntry;
			private EntryNode currentNode = null;
			private EntryNode nextNode = null;
			private int expectedModCount = LinkedSymmetricMatrix.this.modCount;

			private boolean getNextNode() {
				checkModCount();
				if (currentNode == null) {
					return getNextInNextHead();
				} else {
					if ((nextNode = currentNode.right) != null) {
						return true;
					} else {
						return getNextInNextHead();
					}
				}
			}

			private boolean getNextInNextHead() {
				while (nextHead != null
						&& (nextNode = nextHead.rowEntry) == null) {
					nextHead = nextHead.next;
				}

				if (nextHead == null)
					return false;
				nextHead = nextHead.next;
				return nextNode != null;
			}

			private void checkModCount() {
				if (this.expectedModCount != LinkedSymmetricMatrix.this.modCount)
					throw new ConcurrentModificationException();
			}

			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				return nextNode == null ? this.getNextNode() : true;
			}

			@Override
			public Entry<K, K, V> next() {
				// TODO Auto-generated method stub
				if (null == nextNode && !this.getNextNode())
					throw new NoSuchElementException();
				currentNode = nextNode;
				nextNode = null;
				return currentNode;
			}

			@Override
			public void remove() {
				// TODO Auto-generated method stub
				checkModCount();
				if (currentNode == null)
					throw new IllegalStateException();
				if (nextNode == null)
					this.getNextNode();
				LinkedSymmetricMatrix.this.removeNode(currentNode);
				currentNode = null;
				this.expectedModCount = LinkedSymmetricMatrix.this.modCount;
			}

		};
	}

	private class EntryNode extends AbstractSymmetricMatrixEntry<K, V> {
		private V value;
		private EntryNode right, left, upper, lower;
		private HeadNode rowHead, columnHead;

		private EntryNode(V value, HeadNode rowHead, HeadNode columnHead) {
			this.value = value;
			this.rowHead = rowHead;
			this.columnHead = columnHead;
			this.left = this.right = null;
			this.upper = this.lower = null;
		}

		@Override
		public K getRowKey() {
			// TODO Auto-generated method stub
			return rowHead.getKey();
		}

		@Override
		public K getColumnKey() {
			// TODO Auto-generated method stub
			return columnHead.getKey();
		}

		@Override
		public V getValue() {
			// TODO Auto-generated method stub
			return value;
		}

		@Override
		public V setValue(V value) {
			// TODO Auto-generated method stub
			V oldValue = this.value;
			this.value = value;
			return oldValue;
		}

		private boolean isDiagonal() {
			return rowHead == columnHead;
		}

		void dispose() {
			super.dispose();
			this.value = null;
			this.rowHead = null;
			this.columnHead = null;
			this.left = this.right = null;
			this.upper = this.lower = null;
		}

	}

	private class HeadNode {
		private K key;
		private int index;
		private int size;
		
		transient volatile int modCount;

		/*
		 * To prevent the diagonal element to be access twice, column entry
		 * should not point to the diagonal element. If the diagonal element
		 * exists, column entry should point to its lower element or null if
		 * there isn't any.
		 */
		private EntryNode rowEntry, columnEntry;
		private HeadNode prev, next;

		// View
		protected transient volatile Map<K, V> viewMap = null;

		private HeadNode(K key, int index) {
			this.key = key;
			this.index = index;
			size = 0;
			rowEntry = columnEntry = null;
			prev = next = null;
		}

		private K getKey() {
			return key;
		}

		private int addSize(int incr) {
			if(incr == 0)
				return size;
			++modCount;
			size = Math.max(0, size + incr);
			return size;
		}

		private void dispose() {
			key = null;
			index = -1;
			size = 0;
			modCount = -1;
			rowEntry = columnEntry = null;
			prev = next = null;
			viewMap = null;
		}

		private boolean disposed() {
			return index < 0;
		}
	}
	
	/**
	 * Save the state of the <tt>LinkedMatrix</tt> instance to a stream (that is,
	 * serialize it).
	 * 
	 * @serialData The length of the array backing the <tt>ArrayMatrix</tt>
	 *             instance is emitted (int), followed by all of its keys and
	 *             values (each an <tt>Object</tt>) in the proper order.
	 */
	private void writeObject(java.io.ObjectOutputStream s)
			throws java.io.IOException {
		// Write out element count, and any hidden stuff
		int expectedModCount = modCount;
		s.defaultWriteObject();

		s.writeInt(this.size());
		Iterator<Matrix.Entry<K, K, V>> i = isEmpty() ? null : entrySet().iterator();
		if(i != null){
			while(i.hasNext()){
				Matrix.Entry<K, K, V> e = i.next();
				s.writeObject(e.getRowKey());
				s.writeObject(e.getColumnKey());
				s.writeObject(e.getValue());
			}
		}

		if (modCount != expectedModCount) {
			throw new ConcurrentModificationException();
		}

	}

	/**
	 * Reconstitute the <tt>LinkedMatrix</tt> instance from a stream (that is,
	 * deserialize it).
	 */
	@SuppressWarnings("unchecked")
	private void readObject(java.io.ObjectInputStream s)
			throws java.io.IOException, ClassNotFoundException {
		// Read in size, and any hidden stuff
		s.defaultReadObject();

		// Read in array length and allocate array
		int size = s.readInt();

		// Read in all elements in the proper order.
		for (int i = 0; i < size; i++) {
			K row = (K) s.readObject();
			K column = (K) s.readObject();
			V value = (V) s.readObject();
			put(row, column, value);
		}
	}

}
