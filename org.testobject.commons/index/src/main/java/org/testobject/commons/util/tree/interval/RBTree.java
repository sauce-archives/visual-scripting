package org.testobject.commons.util.tree.interval;

import java.util.Comparator;

/*
 * rbtree - Implements a red-black tree with parent pointers.
 *
 * Copyright (C) 2010 Franck Bui-Huu <fbuihuu@gmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2 of the
 * License.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 */

/*
 * For recall a red-black tree has the following properties:
 *
 *     1. All nodes are either BLACK or RED
 *     2. Leafs are BLACK
 *     3. A RED node has BLACK children only
 *     4. Path from a node to any leafs has the same number of BLACK nodes.
 *
 */

/**
 * Red-Black tree.
 * 
 * This implementation does not accept duplicate keys.
 * 
 * Implementation is based on an excellent C code from:
 * 
 * https://github.com/fbuihuu/libtree
 *
 * @author mike
 */
public class RBTree<K,V> {
	
	private static final boolean DEBUG = false;
	
	private enum ColorEnum { RED, BLACK };
	
	/**
	 * Tree node.
	 */
	protected static class Node<K,V> {
		public K key;
		public V value;
		public ColorEnum color;
		
		public Node<K,V> parent;
		public Node<K,V> left;
		public Node<K,V> right;
		
		public Node(K key, V value) {
			this.key = key;
			this.value = value;
			this.color = ColorEnum.RED;
		}
		
		public boolean isRoot() {
			return parent == null;
		}
		
		public boolean isBlack() {
			return color == ColorEnum.BLACK;
		}

		public boolean isRed() {
			return color == ColorEnum.RED;
		}

		/*
		 * Iterators
		 */
		Node<K,V> getFirst()	{
			Node<K,V> node = this;
			
			while (node.left != null)
				node = node.left;
			return node;
		}

		private Node<K,V> getLast() {
			Node<K,V> node = this;
			while (node.right != null)
				node = node.right;
			return node;
		}

		private Node<K,V> getNext() {
			Node<K,V> node = this;

			if (node.right != null)
				return node.right.getFirst();

			Node<K,V> parent;
			while ((parent = node.parent) != null && parent.right == node) {
				node = parent;
			}
			
			return parent;
		}

		private Node<K,V> getPrev() {
			Node<K,V> node = this;

			if (node.left != null)
				return node.left.getLast();

			Node<K,V> parent;
			while ((parent = node.parent) != null && parent.left == node) {
				node = parent;
			}
			
			return parent;
		}

	}

	private final Comparator<K> comparator;
	protected Node<K,V> root  = null;
	private Node<K,V> first = null;
	private Node<K,V> last  = null;
	private int size = 0;

	/**
	 * Creates new tree.
	 * 
	 * @param comparator key comparator.
	 */
	public RBTree(Comparator<K> comparator) {
		this.comparator = comparator;
	}

	protected static class LookupResult<K,V> {
		public final Node<K,V> node;
		public final Node<K,V> parent;
		public final boolean isLeft;
		
		LookupResult(Node<K,V> node, Node<K,V> parent, boolean isLeft) {
			this.node = node;
			this.parent = parent;
			this.isLeft = isLeft;
		}
	}

	protected LookupResult<K,V> doLookup(K key) {
		Node<K,V> parent = null;
		boolean isLeft = false;
		Node<K,V> node = root;

		while (node != null) {
			int res = comparator.compare(node.key, key);
			if (res == 0)
				return new LookupResult<K,V>(node, parent, isLeft);
			parent = node;
			if ((isLeft = res > 0))
				node = node.left;
			else
				node = node.right;
		}
		
		return new LookupResult<K,V>(node, parent, isLeft);
	}

	/*
	 * Rotate operations (They preserve the binary search tree property,
	 * assuming that the keys are unique).
	 */
	protected void rotateLeft(Node<K,V> node)	{
		Node<K,V> p = node;
		Node<K,V> q = node.right; /* can't be NULL */
		Node<K,V> parent = node.parent;

		if (!p.isRoot()) {
			if (parent.left == p)
				parent.left = q;
			else
				parent.right = q;
		} else {
			root = q;
		}
		
		q.parent = parent;
		p.parent = q;

		p.right = q.left;
		if (p.right != null) {
			p.right.parent = p;
		}
		q.left = p;
	}

	protected void rotateRight(Node<K,V> node) {
		Node<K,V> p = node;
		Node<K,V> q = node.left; /* can't be NULL */
		Node<K,V> parent = node.parent;

		if (!p.isRoot()) {
			if (parent.left == p)
				parent.left = q;
			else
				parent.right = q;
		} else {
			root = q;
		}
		
		q.parent = parent;
		p.parent = q;

		p.left = q.right;
		if (p.left != null) {
			p.left.parent = p;
		}
		q.right = p;
	}

	public V lookup(K key) {
		Node<K,V> node = doLookup(key).node;
		if(node == null) return null;
		return node.value;
	}
	
	public V put(K key, V value) {
		LookupResult<K,V> res = doLookup(key);
		
		if(res.node != null) {
			V oldValue = res.node.value;
			res.node.value = value;
			
			if(DEBUG) validate();
			
			return oldValue;
		}
		
		internalInsert(res, new Node<K,V>(key, value));
		return null;
	}

	protected void internalInsert(LookupResult<K,V> res, Node<K,V> node) {

		node.parent = res.parent;

		if (node.parent != null) {
			if (res.isLeft) {
				if (node.parent == first) {
					first = node;
				}
				node.parent.left = node;
			} else {
				if (node.parent == last) {
					last = node;
				}
				node.parent.right = node;
			}
		} else {
			root = node;
			first = node;
			last = node;
		}

		/*
		 * Fixup the modified tree by recoloring nodes and performing
		 * rotations (2 at most) hence the red-black tree properties are
		 * preserved.
		 */
		Node<K,V> parent;
		while ((parent = node.parent) != null && parent.isRed()) {
			Node<K,V> grandpa = parent.parent;

			if (parent == grandpa.left) {
				Node<K,V> uncle = grandpa.right;

				if (uncle != null && uncle.isRed()) {
					parent.color = ColorEnum.BLACK;
					uncle.color = ColorEnum.BLACK;
					grandpa.color = ColorEnum.RED;
					node = grandpa;
				} else {
					if (node == parent.right) {
						rotateLeft(parent);
						node = parent;
						parent = node.parent;
					}
					parent.color = ColorEnum.BLACK;
					grandpa.color = ColorEnum.RED;
					rotateRight(grandpa);
				}
			} else {
				Node<K,V> uncle = grandpa.left;

				if (uncle != null && uncle.isRed()) {
					parent.color = ColorEnum.BLACK;
					uncle.color = ColorEnum.BLACK;
					grandpa.color = ColorEnum.RED;
					node = grandpa;
				} else {
					if (node == parent.left) {
						rotateRight(parent);
						node = parent;
						parent = node.parent;
					}
					parent.color = ColorEnum.BLACK;
					grandpa.color = ColorEnum.RED;
					rotateLeft(grandpa);
				}
			}
		}

		root.color = ColorEnum.BLACK;
		
		size++;
		
		if(DEBUG) validate();
	}
	
	public V remove(K key) {
		LookupResult<K,V> res = doLookup(key);
		
		if(res.node == null) {
			return null;
		}
		
		Node<K,V> node = res.node;
		V value = node.value;
		
		if(node.left != null && node.right != null) {
			Node<K,V> next = node.right.getFirst();
			
			K nodeKey = node.key;
			V nodeValue = node.value;
			
			node.key = next.key;
			node.value = next.value;
			
			next.key = nodeKey;
			next.value = nodeValue;
			
			node = next;
		} 

		internalRemove(node);

		return value;
	}

	protected void internalRemove(Node<K,V> node)	{
		
		Node<K,V> parent = node.parent;
		Node<K,V> left = node.left;
		Node<K,V> right = node.right;
		Node<K,V> next;

		if (node == first)
			first = node.getNext();
		if (node == last)
			last = node.getPrev();

		if (left == null) {
			next = right;
		} else if (right == null) {
			next = left;
		} else {
			throw new IllegalStateException();
		}

		if (parent != null) {
			if(parent.left == node) {
				parent.left = next;
			} else {
				parent.right = next;
			}
		} else {
			root = next;
		}

		ColorEnum color = node.color;
		node = next;
		
		/*
		 * 'node' is now the sole successor's child and 'parent' its
		 * new parent (since the successor can have been moved).
		 */
		if (node != null) {
			node.parent = parent;
		}

		/*
		 * The 'easy' cases.
		 */
		if (color == ColorEnum.RED) {
			--size;
			
			if(DEBUG) validate();
			return;
		}
		
		if (node != null && node.isRed()) {
			node.color = ColorEnum.BLACK;
			--size;
			
			if(DEBUG) validate();
			return;
		}

		do {
			if (node == root)
				break;

			if (node == parent.left) {
				Node<K,V> sibling = parent.right;

				if (sibling.isRed()) {
					sibling.color = ColorEnum.BLACK;
					parent.color = ColorEnum.RED;
					rotateLeft(parent);
					sibling = parent.right;
				}
				
				if ((sibling.left == null || sibling.left.isBlack()) &&
				    (sibling.right == null || sibling.right.isBlack())) {
					sibling.color = ColorEnum.RED;
					node = parent;
					parent = parent.parent;
					continue;
				}
				
				if (sibling.right == null || sibling.right.isBlack()) {
					sibling.left.color = ColorEnum.BLACK;
					sibling.color = ColorEnum.RED;
					rotateRight(sibling);
					sibling = parent.right;
				}
				sibling.color = parent.color;
				parent.color = ColorEnum.BLACK;
				sibling.right.color = ColorEnum.BLACK;
				rotateLeft(parent);
				node = root;
				break;
			} else {
				Node<K,V> sibling = parent.left;

				if (sibling.isRed()) {
					sibling.color = ColorEnum.BLACK;
					parent.color = ColorEnum.RED;
					rotateRight(parent);
					sibling = parent.left;
				}
				
				if ((sibling.left == null || sibling.left.isBlack()) &&
				    (sibling.right== null || sibling.right.isBlack())) {
					sibling.color = ColorEnum.RED;
					node = parent;
					parent = parent.parent;
					continue;
				}
				
				if (sibling.left == null || sibling.left.isBlack()) {
					sibling.right.color = ColorEnum.BLACK;
					sibling.color = ColorEnum.RED;
					rotateLeft(sibling);
					sibling = parent.left;
				}
				sibling.color = parent.color;
				parent.color = ColorEnum.BLACK;
				sibling.left.color = ColorEnum.BLACK;
				rotateRight(parent);
				node = root;
				break;
			}
		} while (node.isBlack());

		if (node != null) {
			node.color = ColorEnum.BLACK;
		}
		--size;
		
		if(DEBUG) validate();
	}
	
	public int getSize() {
		return size;
	}
	
	void validate() {
		if(root == null) return; // OK
		
		if(root.color != ColorEnum.BLACK) {
			throw new AssertionError("root must be black");
		}
		
		int countLeft  = 0;
		int countRight = 0;
		if(root.left != null) {
			countLeft = validateSubTree(root.left);
		}
		if(root.right != null) {
			countRight = validateSubTree(root.right);
		}
		
		if(countLeft != countRight) {
			throw new AssertionError("tree is not black-balanced!");
		}
	}
	
	private static <K,V> int validateSubTree(Node<K,V> node) {
		int height = 0;
		
		if(node.color == ColorEnum.BLACK) {
			height++;
		} else {
			// check that both children are black
			if(node.left != null && node.left.color != ColorEnum.BLACK) {
				throw new AssertionError("left child of RED node is not BLACK");
			}
			if(node.right != null && node.right.color != ColorEnum.BLACK) {
				throw new AssertionError("right child of RED node is not BLACK");
			}
		}

		int countLeft  = 0;
		int countRight = 0;
		if(node.left != null) {
			countLeft = validateSubTree(node.left);
		}
		if(node.right != null) {
			countRight = validateSubTree(node.right);
		}
		
		if(countLeft != countRight) {
			throw new AssertionError("tree is not black-balanced!");
		}
		
		return height + countLeft;
	}
	
	public interface IVisitor<K,V> {
		void visit(K key, V value);
	}
	
	public void visit(IVisitor<K,V> visitor) {
		Node<K,V> node = first;
		
		while(node != null) {
			visitor.visit(node.key, node.value);
			node = node.getNext();
		}
	}
}