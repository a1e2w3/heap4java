package hust.idc.util.matrix;

import hust.idc.util.pair.Pair;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class LinkedMatrix<RK, CK, V> extends AbstractMatrix<RK, CK, V>
		implements Matrix<RK, CK, V> {
	private HeadNode<RK> rowHeadsEntry;
	private HeadNode<CK> columnHeadsEntry;
	private int size, rows, columns;

	private int modCount = 0;

	public LinkedMatrix() {
		super();
		rowHeadsEntry = null;
		columnHeadsEntry = null;
		size = 0;
		rows = columns = 0;
	}

	public LinkedMatrix(
			Matrix<? extends RK, ? extends CK, ? extends V> otherMatrix) {
		this();
		this.setAll(otherMatrix);
	}

	public LinkedMatrix(
			Map<? extends Pair<? extends RK, ? extends CK>, ? extends V> otherMatrix) {
		this();
		this.setAll(otherMatrix);
	}

	private HeadNode<RK> rowHead(Object row) {
		HeadNode<RK> head = rowHeadsEntry;
		if (row == null) {
			while (head != null) {
				if (head.getKey() == null)
					return head;
				head = head.next;
			}
		} else {
			while (head != null) {
				if (row.equals(head.getKey()))
					return head;
				head = head.next;
			}
		}
		return null;
	}

	private HeadNode<CK> columnHead(Object column) {
		HeadNode<CK> head = columnHeadsEntry;
		if (column == null) {
			while (head != null) {
				if (head.getKey() == null)
					return head;
				head = head.next;
			}
		} else {
			while (head != null) {
				if (column.equals(head.getKey()))
					return head;
				head = head.next;
			}
		}
		return null;
	}

	@Override
	public boolean containsRow(Object row) {
		// TODO Auto-generated method stub
		return rowHead(row) != null;
	}

	@Override
	public boolean containsColumn(Object column) {
		// TODO Auto-generated method stub
		return columnHead(column) != null;
	}

	@Override
	public boolean containsKey(Object row, Object column) {
		// TODO Auto-generated method stub
		return getEntry(row, column) != null;
	}

	private EntryNode getEntry(Object row, Object column) {
		HeadNode<RK> rowHead = rowHead(row);
		if (rowHead == null)
			return null;

		EntryNode node = rowHead.entry();
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
		return null;
	}

	@Override
	public V get(Object row, Object column) {
		// TODO Auto-generated method stub
		EntryNode node = getEntry(row, column);
		return node == null ? null : node.getValue();
	}

	@Override
	public V set(RK row, CK column, V value) {
		// TODO Auto-generated method stub
		++modCount;
		HeadNode<RK> rowHead = addRowIfNotExists(row);
		HeadNode<CK> columnHead = addColumnIfNotExists(column);

		EntryNode rowCur = rowHead.entry(), left = null;
		while (rowCur != null && rowCur.columnHead().index < columnHead.index) {
			left = rowCur;
			rowCur = rowCur.right;
		}

		EntryNode columnCur = columnHead.entry(), up = null;
		while (columnCur != null && columnCur.rowHead().index < rowHead.index) {
			up = columnCur;
			columnCur = columnCur.down;
		}

		if (rowCur == null || rowCur.columnHead().index > columnHead.index) {
			if (columnCur == null || columnCur.rowHead().index > rowHead.index) {
				EntryNode newNode = new EntryNode(value, rowHead, columnHead);
				newNode.right = rowCur;
				newNode.down = columnCur;

				if (left == null)
					rowHead.entry = newNode;
				else
					left.right = newNode;
				if (up == null)
					columnHead.entry = newNode;
				else
					up.down = newNode;
				rowHead.addSize(1);
				columnHead.addSize(1);
				++size;
				return null;
			} else {
				if (left == null)
					rowHead.entry = columnCur;
				else
					left.right = columnCur;
				columnCur.rowHead = rowHead;
				columnCur.right = rowCur;
				return columnCur.setValue(value);
			}
		} else {
			if (columnCur == rowCur) {
				return rowCur.setValue(value);
			} else {
				if (up == null)
					columnHead.entry = rowCur;
				else
					up.down = rowCur;
				rowCur.columnHead = columnHead;
				rowCur.down = columnCur;
				return rowCur.setValue(value);
			}
		}
	}

	private HeadNode<RK> addRowIfNotExists(RK row) {
		if (rowHeadsEntry == null) {
			rowHeadsEntry = new HeadNode<RK>(row, 0);
			return rowHeadsEntry;
		}

		HeadNode<RK> head = rowHeadsEntry;
		if (row == null) {
			if (head.getKey() == null)
				return head;
			while (head.next != null) {
				head = head.next;
				if (head.getKey() == null)
					return head;
			}
		} else {
			if (row.equals(head.getKey()))
				return head;
			while (head.next != null) {
				head = head.next;
				if (row.equals(head.getKey()))
					return head;
			}
		}

		head.next = new HeadNode<RK>(row, head.index + 1);
		++rows;
		return head.next;
	}

	private HeadNode<CK> addColumnIfNotExists(CK column) {
		if (columnHeadsEntry == null) {
			columnHeadsEntry = new HeadNode<CK>(column, 0);
			return columnHeadsEntry;
		}

		HeadNode<CK> head = columnHeadsEntry;
		if (column == null) {
			if (head.getKey() == null)
				return head;
			while (head.next != null) {
				head = head.next;
				if (head.getKey() == null)
					return head;
			}
		} else {
			if (column.equals(head.getKey()))
				return head;
			while (head.next != null) {
				head = head.next;
				if (column.equals(head.getKey()))
					return head;
			}
		}

		head.next = new HeadNode<CK>(column, head.index + 1);
		++columns;
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

		if (node.left == null)
			node.rowHead.entry = node.right;
		else
			node.left.right = node.right;
		if (node.right != null)
			node.right.left = node.left;

		if (node.up == null)
			node.columnHead.entry = node.down;
		else
			node.up.down = node.down;
		if (node.down != null)
			node.down.up = node.up;

		if (node.rowHead.addSize(-1) == 0) {
			this.removeRowHead(node.rowHead);
		}
		if (node.columnHead.addSize(-1) == 0) {
			this.removeColumn(node.columnHead);
		}
		V value = node.value;
		node.dispose();
		++modCount;
		return value;
	}

	@Override
	public void removeRow(RK row) {
		// TODO Auto-generated method stub
		this.removeRow(rowHead(row));
	}

	@Override
	public void removeColumn(CK column) {
		// TODO Auto-generated method stub
		this.removeColumn(columnHead(column));
	}

	private void removeRow(HeadNode<RK> rowHead) {
		if (rowHead == null)
			return;
		EntryNode node = rowHead.entry(), next = null;
		while (node != null) {
			next = node.right;
			this.removeNode(node);
			node = next;
		}
		++modCount;
		this.removeRowHead(rowHead);
	}

	private void removeColumn(HeadNode<CK> columnHead) {
		if (columnHead == null)
			return;
		EntryNode node = columnHead.entry(), next = null;
		while (node != null) {
			next = node.down;
			this.removeNode(node);
			node = next;
		}
		++modCount;
		this.removeColumnHead(columnHead);
	}

	private void removeRowHead(HeadNode<RK> rowHead) {
		if (rowHead == null)
			return;

		if (rowHead.prev == null)
			rowHeadsEntry = rowHead.next;
		else
			rowHead.prev.next = rowHead.next;
		if (rowHead.next != null)
			rowHead.next.prev = rowHead.prev;
		rowHead.dispose();
	}

	private void removeColumnHead(HeadNode<CK> columnHead) {
		if (columnHead == null)
			return;

		if (columnHead.prev == null)
			columnHeadsEntry = columnHead.next;
		else
			columnHead.prev.next = columnHead.next;
		if (columnHead.next != null)
			columnHead.next.prev = columnHead.prev;
		columnHead.dispose();
	}

	@Override
	public Map<CK, V> rowMap(final RK row) {
		// TODO Auto-generated method stub
		return new AbstractMap<CK, V>() {

			@Override
			public Set<Map.Entry<CK, V>> entrySet() {
				// TODO Auto-generated method stub
				return new AbstractSet<Map.Entry<CK, V>>() {
					private HeadNode<RK> head = LinkedMatrix.this.rowHead(row);

					@Override
					public Iterator<Map.Entry<CK, V>> iterator() {
						// TODO Auto-generated method stub
						return new Iterator<Map.Entry<CK, V>>() {
							private EntryNode current = null;
							private boolean currentRemoved = false;

							private int expectedModCount = LinkedMatrix.this.modCount;

							@Override
							public boolean hasNext() {
								// TODO Auto-generated method stub
								checkModCount();
								if (current == null)
									return head != null && head.entry != null;
								else
									return current.right != null;
							}

							private boolean getNext() {
								checkModCount();
								if (current == null) {
									if (head == null)
										return false;
									return (current = head.entry) != null;
								} else {
									if (current.right == null)
										return false;
									current = current.right;
									return true;
								}
							}

							private void checkModCount() {
								if (expectedModCount != LinkedMatrix.this.modCount)
									throw new ConcurrentModificationException();
							}

							@Override
							public Map.Entry<CK, V> next() {
								// TODO Auto-generated method stub
								if (!getNext())
									throw new NoSuchElementException();
								currentRemoved = false;
								return current.rowMapEntry();
							}

							@Override
							public void remove() {
								// TODO Auto-generated method stub
								checkModCount();
								if (currentRemoved || current == null)
									throw new IllegalStateException();
								EntryNode left = current.left;
								LinkedMatrix.this.removeNode(current);
								current = left;
								currentRemoved = true;
								expectedModCount = LinkedMatrix.this.modCount;
							}

						};
					}

					@Override
					public int size() {
						// TODO Auto-generated method stub
						return head == null ? 0 : head.listSize();
					}

				};
			}

		};
	}

	@Override
	public Map<RK, V> columnMap(final CK column) {
		// TODO Auto-generated method stub
		return new AbstractMap<RK, V>() {

			@Override
			public Set<Map.Entry<RK, V>> entrySet() {
				// TODO Auto-generated method stub
				return new AbstractSet<Map.Entry<RK, V>>() {
					private HeadNode<CK> head = LinkedMatrix.this
							.columnHead(column);

					@Override
					public Iterator<Map.Entry<RK, V>> iterator() {
						// TODO Auto-generated method stub
						return new Iterator<Map.Entry<RK, V>>() {
							private EntryNode current = null;
							private boolean currentRemoved = false;

							private int expectedModCount = LinkedMatrix.this.modCount;

							@Override
							public boolean hasNext() {
								// TODO Auto-generated method stub
								checkModCount();
								if (current == null)
									return head != null && head.entry != null;
								else
									return current.down != null;
							}

							private boolean getNext() {
								checkModCount();
								if (current == null) {
									if (head == null)
										return false;
									return (current = head.entry) != null;
								} else {
									if (current.down == null)
										return false;
									current = current.down;
									return true;
								}
							}

							private void checkModCount() {
								if (expectedModCount != LinkedMatrix.this.modCount)
									throw new ConcurrentModificationException();
							}

							@Override
							public Map.Entry<RK, V> next() {
								// TODO Auto-generated method stub
								if (!getNext())
									throw new NoSuchElementException();
								currentRemoved = false;
								return current.columnMapEntry();
							}

							@Override
							public void remove() {
								// TODO Auto-generated method stub
								checkModCount();
								if (currentRemoved || current == null)
									throw new IllegalStateException();
								EntryNode up = current.up;
								LinkedMatrix.this.removeNode(current);
								current = up;
								currentRemoved = true;
								expectedModCount = LinkedMatrix.this.modCount;
							}

						};
					}

					@Override
					public int size() {
						// TODO Auto-generated method stub
						return head == null ? 0 : head.listSize();
					}

				};
			}

		};
	}

	@Override
	protected int rowValueCount(Object row) {
		// TODO Auto-generated method stub
		HeadNode<RK> head = rowHead(row);
		return head == null ? 0 : head.listSize();
	}

	@Override
	protected int columnValueCount(Object column) {
		// TODO Auto-generated method stub
		HeadNode<CK> head = columnHead(column);
		return head == null ? 0 : head.listSize();
	}

	// View
	protected transient volatile Set<RK> rowKeySet = null;
	protected transient volatile Set<CK> columnKeySet = null;
	protected transient volatile Set<Entry<RK, CK, V>> entrySet = null;

	@Override
	public Set<RK> rowKeySet() {
		// TODO Auto-generated method stub
		if (rowKeySet == null) {
			rowKeySet = new AbstractSet<RK>() {

				@Override
				public Iterator<RK> iterator() {
					// TODO Auto-generated method stub
					return new Iterator<RK>() {
						private HeadNode<RK> curHead = null;
						private boolean currentRemoved = false;

						private int expectedModCount = LinkedMatrix.this.modCount;

						private void checkModCount() {
							if (expectedModCount != LinkedMatrix.this.modCount)
								throw new ConcurrentModificationException();
						}

						@Override
						public boolean hasNext() {
							// TODO Auto-generated method stub
							checkModCount();
							if (curHead == null)
								return LinkedMatrix.this.rowHeadsEntry != null;
							else
								return curHead.next != null;
						}

						private boolean getNext() {
							checkModCount();
							if (curHead == null) {
								return (curHead = LinkedMatrix.this.rowHeadsEntry) != null;
							} else {
								if (curHead.next == null)
									return false;
								curHead = curHead.next;
								return true;
							}
						}

						@Override
						public RK next() {
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

							HeadNode<RK> prev = curHead.prev;
							LinkedMatrix.this.removeRow(curHead);
							curHead = prev;
							currentRemoved = true;
							expectedModCount = LinkedMatrix.this.modCount;
						}

					};
				}

				@Override
				public int size() {
					// TODO Auto-generated method stub
					return LinkedMatrix.this.rows;
				}

			};
		}
		return rowKeySet;
	}

	@Override
	public Set<CK> columnKeySet() {
		// TODO Auto-generated method stub
		if (columnKeySet == null) {
			columnKeySet = new AbstractSet<CK>() {

				@Override
				public Iterator<CK> iterator() {
					// TODO Auto-generated method stub
					return new Iterator<CK>() {
						private HeadNode<CK> curHead = null;
						private boolean currentRemoved = false;

						private int expectedModCount = LinkedMatrix.this.modCount;

						private void checkModCount() {
							if (expectedModCount != LinkedMatrix.this.modCount)
								throw new ConcurrentModificationException();
						}

						@Override
						public boolean hasNext() {
							// TODO Auto-generated method stub
							checkModCount();
							if (curHead == null)
								return LinkedMatrix.this.columnHeadsEntry != null;
							else
								return curHead.next != null;
						}

						private boolean getNext() {
							checkModCount();
							if (curHead == null) {
								return (curHead = LinkedMatrix.this.columnHeadsEntry) != null;
							} else {
								if (curHead.next == null)
									return false;
								curHead = curHead.next;
								return true;
							}
						}

						@Override
						public CK next() {
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

							HeadNode<CK> prev = curHead.prev;
							LinkedMatrix.this.removeColumn(curHead);
							curHead = prev;
							currentRemoved = true;
							expectedModCount = LinkedMatrix.this.modCount;
						}

					};
				}

				@Override
				public int size() {
					// TODO Auto-generated method stub
					return LinkedMatrix.this.columns;
				}

			};
		}
		return columnKeySet;
	}

	@Override
	public Set<Entry<RK, CK, V>> entrySet() {
		// TODO Auto-generated method stub
		if (entrySet == null) {
			entrySet = new AbstractSet<Entry<RK, CK, V>>() {

				@Override
				public Iterator<Entry<RK, CK, V>> iterator() {
					// TODO Auto-generated method stub
					return LinkedMatrix.this.nodeIterator(true);
				}

				@Override
				public int size() {
					// TODO Auto-generated method stub
					return LinkedMatrix.this.size;
				}

			};
		}
		return entrySet;
	}

	private Iterator<Entry<RK, CK, V>> nodeIterator(final boolean rowOrder) {
		return new Iterator<Entry<RK, CK, V>>() {
			@SuppressWarnings("unchecked")
			private HeadNode<?> currentHead = (HeadNode<?>) (rowOrder ? LinkedMatrix.this.rowHeadsEntry
					: LinkedMatrix.this.columnHeadsEntry);
			private EntryNode currentNode = null;
			private EntryNode nextNode = null;
			private int expectedModCount = LinkedMatrix.this.modCount;

			private boolean getNextNode() {
				checkModCount();
				if (currentNode == null
						|| (rowOrder && (nextNode = currentNode.right) == null)
						|| (!rowOrder && (nextNode = currentNode.down) == null)) {
					while (currentHead != null && currentHead.entry() == null) {
						currentHead = currentHead.next;
					}
					if (currentHead == null)
						return false;
					nextNode = currentHead.entry();
				}
				return true;
			}

			private void checkModCount() {
				if (this.expectedModCount != LinkedMatrix.this.modCount)
					throw new ConcurrentModificationException();
			}

			@Override
			public boolean hasNext() {
				// TODO Auto-generated method stub
				if (nextNode != null)
					return true;
				return this.getNextNode();
			}

			@Override
			public Entry<RK, CK, V> next() {
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
				LinkedMatrix.this.removeNode(currentNode);
				currentNode = null;
				this.expectedModCount = LinkedMatrix.this.modCount;
			}

		};
	}

	private class EntryNode extends AbstractEntry<RK, CK, V> implements
			Entry<RK, CK, V> {
		private V value;
		private EntryNode right, left, up, down;
		private HeadNode<RK> rowHead;
		private HeadNode<CK> columnHead;

		private EntryNode(V value, HeadNode<RK> rowHead, HeadNode<CK> columnHead) {
			this.value = value;
			this.rowHead = rowHead;
			this.columnHead = columnHead;
			this.left = this.right = null;
			this.up = this.down = null;
		}

		private void dispose() {
			this.value = null;
			this.rowHead = null;
			this.columnHead = null;
			this.left = this.right = null;
			this.up = this.down = null;
		}

		private HeadNode<RK> rowHead() {
			return rowHead;
		}

		private HeadNode<CK> columnHead() {
			return columnHead;
		}

		// private EntryNode left() {
		// EntryNode node = rowHead().entry();
		// if (this == node)
		// return null;
		// while (node != null) {
		// if (this == node.right)
		// return node;
		// node = node.right;
		// }
		// return null;
		// }
		//
		// private EntryNode up() {
		// EntryNode node = columnHead().entry();
		// if (this == node)
		// return null;
		// while (node != null) {
		// if (this == node.down)
		// return node;
		// node = node.down;
		// }
		// return null;
		// }

		@Override
		public RK getRowKey() {
			// TODO Auto-generated method stub
			return rowHead().getKey();
		}

		@Override
		public CK getColumnKey() {
			// TODO Auto-generated method stub
			return columnHead().getKey();
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
	}

	private class HeadNode<K> {
		private K key;
		private int index;
		private int size;
		private EntryNode entry;
		private HeadNode<K> prev, next;

		private HeadNode(K key, int index) {
			this.key = key;
			this.index = index;
			this.size = 0;
			this.entry = null;
			this.prev = null;
			this.next = null;
		}

		private K getKey() {
			return key;
		}

		private int listSize() {
			return size;
		}

		private int addSize(int incr) {
			size = Math.max(0, size + incr);
			return size;
		}

		private EntryNode entry() {
			return entry;
		}

		private void dispose() {
			key = null;
			index = -1;
			size = 0;
			entry = null;
			prev = next = null;
		}
	}

}