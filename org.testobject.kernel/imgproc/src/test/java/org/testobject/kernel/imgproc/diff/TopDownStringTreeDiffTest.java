package org.testobject.kernel.imgproc.diff;

import static org.testobject.kernel.imgproc.diff.TopDownStringTreeDiffTest.Util.toList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.testobject.kernel.imgproc.diff.TopDownStringTreeDiff;
import org.testobject.kernel.imgproc.diff.TopDownStringTreeDiff.Mutation;

/**
 * 
 * @author nijkamp
 *
 */
public class TopDownStringTreeDiffTest
{
    public interface Node {}
    
    public interface Container extends Node
    {
        List<Node> getChilds();
    }
    
    public class Leaf implements Node
    {
        public final String label;
        
        public Leaf(String label)
        {
            this.label = label;
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if(obj instanceof Leaf == false)
            {
                return false;
            }
            Leaf leaf = (Leaf) obj;
            return label.equals(leaf.label);
        }
    }
    
    public class Branch implements Container
    {
        public final String label;        
        public final List<Node> childs;
        
        public Branch(String label, List<Node> childs)
        {
            this.label = label;
            this.childs = childs;
        }

        @Override
        public List<Node> getChilds()
        {
            return childs;
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if(obj instanceof Branch == false)
            {
                return false;
            }
            Branch branch = (Branch) obj;
            for(Node child : childs)
            {
                if(branch.childs.contains(child))
                {
                    return false;
                }
            }            
            return true;
        }
    }
    
    public static class Util
    {    
        public static <T> List<T> toList(T t1)
        {
            List<T> list = new ArrayList<T>();
            list.add(t1);
            return list;
        }
        
        public static <T> List<T> toList(T t1, T t2)
        {
            List<T> list = toList(t1);
            list.add(t2);
            return list;
        }
    }
    
    private static TopDownStringTreeDiff<Node> newDiff()
    {
        TopDownStringTreeDiff.Adapter<Node> adapter = new TopDownStringTreeDiff.Adapter<Node>()
        {
            @Override
            public boolean isContainer(Node node)
            {
                return node instanceof Container;
            }

            @Override
            public Node[] getChilds(Node node)
            {
                Container container = (Container) node;
                return container.getChilds().toArray(new Node[] {});
            }

            @Override
            public boolean equalsType(Node left, Node right)
            {
                return left.getClass() == right.getClass();
            }
        };
        return new TopDownStringTreeDiff<Node>(adapter);
    }
    
    private static List<Node> filter(List<Mutation<Node>> mutations, Mutation.Type type)
    {
        List<Node> filtered = new LinkedList<Node>();
        for(Mutation<Node> mutation : mutations)
        {
            if(mutation.type == type)
            {
                filtered.add(mutation.after);
            }
        }
        return filtered;
    }
    
    @Test
    public void testInsert()
    {
        /*

         before  =>  after
         
            a          a
          A          A   B
          
         */
        Node before;
        {
            Node A = new Leaf("A");
            before = new Branch("a", toList(A));
        }
        
        Node after;
        {
            Node A = new Leaf("A");
            Node B = new Leaf("B");
            after = new Branch("a", toList(B, A));
        }
        
        List<Mutation<Node>> mutations = newDiff().mutations(before, after);
        
        List<Node> inserts = filter(mutations, Mutation.Type.INSERT);
        {
            Assert.assertEquals(1, inserts.size());
            Node node = inserts.get(0);
            Assert.assertTrue(node instanceof Leaf);
            Leaf leaf = (Leaf) node;
            Assert.assertEquals("B", leaf.label);
        }
    }
    
    @Test
    public void testInsertLong()
    {
        /*

         before  =>  after
         
            a          a
          A          A    b
                            c
                              B
          
         */
        Node before;
        {
            Node A = new Leaf("A");
            before = new Branch("a", toList(A));
        }
        
        Node after;
        {
            Node A = new Leaf("A");
            Node B = new Leaf("B");
            Branch c = new Branch("c", toList(B));
            Branch b = new Branch("b", Util.<Node>toList(c));
            after = new Branch("a", toList(A, b));
        }
        
        List<Mutation<Node>> mutations = newDiff().mutations(before, after);
        
        List<Node> inserts = filter(mutations, Mutation.Type.INSERT);
        {
            Assert.assertEquals(1, inserts.size());
            Node node = inserts.get(0);
            Assert.assertTrue(node instanceof Branch);
            Branch branch = (Branch) node;
            Assert.assertEquals("b", branch.label);
        }
    }
    
    @Test
    public void testInsertBranch()
    {
        /*

         before  =>  after
         
            a          a
          A          A     b
                         B   C
          
         */
        Node before;
        {
            Node A = new Leaf("A");
            before = new Branch("a", toList(A));
        }
        
        Node after;
        {
            Node A = new Leaf("A");
            Node B = new Leaf("B");
            Node C = new Leaf("C");
            Branch b = new Branch("b", toList(C, B));
            after = new Branch("a", toList(A, b));
        }
        
        List<Mutation<Node>> mutations = newDiff().mutations(before, after);
        
        List<Node> inserts = filter(mutations, Mutation.Type.INSERT);
        {
            Assert.assertEquals(1, inserts.size());
            Node node = inserts.get(0);
            Assert.assertTrue(node instanceof Branch);
            Branch branch = (Branch) node;
            Assert.assertEquals("b", branch.label);
        }
    }
    
    @Test
    public void testRemoveInsert()
    {
        /*

         before  =>  after
         
            a          a
          A   B     A     b
                        D   C
          
         */
        Node before;
        {
            Node A = new Leaf("A");
            Node B = new Leaf("B");
            before = new Branch("a", toList(A, B));
        }
        
        Node after;
        {
            Node A = new Leaf("A");
            Node C = new Leaf("C");
            Node D = new Leaf("D");
            Branch b = new Branch("b", toList(C, D));
            after = new Branch("a", toList(A, b));
        }
        
        List<Mutation<Node>> mutations = newDiff().mutations(before, after);
        {
            Assert.assertEquals(2, mutations.size());
            
            Mutation<Node> remove = mutations.get(0);
            Assert.assertEquals(Mutation.Type.DELETE, remove.type);            
            Assert.assertTrue(remove.after instanceof Leaf);
            Leaf B = (Leaf) remove.after;
            Assert.assertEquals("B", B.label);
            
            Mutation<Node> insert = mutations.get(1);
            Assert.assertEquals(Mutation.Type.INSERT, insert.type);            
            Assert.assertTrue(insert.after instanceof Branch);
            Branch b = (Branch) insert.after;
            Assert.assertEquals("b", b.label);
        }
    }
}
