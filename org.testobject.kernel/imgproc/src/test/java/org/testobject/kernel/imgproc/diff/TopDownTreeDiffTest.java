package org.testobject.kernel.imgproc.diff;

import static org.testobject.kernel.imgproc.diff.TopDownTreeDiffTest.Util.toList;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.testobject.commons.util.distances.StringDistances;
import org.testobject.kernel.imgproc.diff.TopDownTreeDiff;

/**
 * 
 * @author nijkamp
 *
 * TODO more test-cases which reflect tab issue etc.
 *
 */
public class TopDownTreeDiffTest
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
    
    private static TopDownTreeDiff<Node> newDiff()
    {
        TopDownTreeDiff.Adapter<Node> adapter = new TopDownTreeDiff.Adapter<Node>()
        {
            @Override
            public boolean isContainer(Node node)
            {
                return node instanceof Container;
            }

            @Override
            public List<Node> getChilds(Node node)
            {
                Container container = (Container) node;
                return container.getChilds();
            }

            @Override
            public float similarity(Node node1, Node node2)
            {
                if(node1.getClass() != node2.getClass())
                {
                    return 0;
                }
                if(node1 instanceof Leaf)
                {
                    Leaf leaf1 = (Leaf) node1;
                    Leaf leaf2 = (Leaf) node2;
                    float length = leaf1.label.length() > leaf2.label.length() ? leaf1.label.length() : leaf2.label.length();
                    float distance = StringDistances.getLevenshteinDistance(leaf1.label, leaf2.label) / length;
                    return 1f - distance;
                }
                if(node1 instanceof Branch)
                {
                    Branch branch1 = (Branch) node1;
                    Branch branch2 = (Branch) node2;
                    float max = branch1.childs.size() > branch2.childs.size() ? branch1.childs.size() : branch2.childs.size();
                    return 1f - (Math.abs(branch1.childs.size() - branch2.childs.size()) / max);
                }
                return 0f;
            }
        };
        return new TopDownTreeDiff<Node>(adapter);
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
        
        List<Node> inserts = newDiff().inserts(before, after);
        
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
        
        List<Node> inserts = newDiff().inserts(before, after);
        
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
        
        List<Node> inserts = newDiff().inserts(before, after);
        
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
        
        List<Node> inserts = newDiff().inserts(before, after);
        
        {
            Assert.assertEquals(1, inserts.size());
            Node node = inserts.get(0);
            Assert.assertTrue(node instanceof Branch);
            Branch branch = (Branch) node;
            Assert.assertEquals("b", branch.label);
        }
    }
}
