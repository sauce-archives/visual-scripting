package org.testobject.kernel.imgproc.diff;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.testobject.kernel.imgproc.blob.BoundingBox;
import org.testobject.kernel.imgproc.diff.WidgetOrders;
import org.testobject.kernel.imgproc.diff.WidgetOrders.Order;

/**
 * 
 * @author nijkamp
 *
 */
public class WidgetOrdersTest
{
    private static class Box implements BoundingBox
    {
        public final Rectangle rect;
        
        public Box(Rectangle rect)
        {
            this.rect = rect;
        }
        
        @Override
        public Rectangle getBoundingBox()
        {
            return rect;
        }        
    }
    
    @Test
    public void testTwoRows()
    {
        /*         
         1     2   
         1     2   
            3     4
            3     4         
         */
        int[][] offsets =
            {
                { 0, 0, 10, 20 },
                { 30, 0, 10, 20 },
                { 15, 25, 10, 20 },           
                { 45, 25, 10, 20 },
            };
        Order<Box> order = new WidgetOrders.XY<Box>();

        testOrder(offsets, order);
    }
    
    @Test
    public void testTwoRowsContained()
    {
        /*         
        1  1 
        1  1  2  2
        
              3  3
              3  3
        */
        int[][] offsets =
            {
                { 0, 0, 10, 10 },               
                { 20, 15, 10, 5 },
                { 20, 20, 10, 10 },
            };
        Order<Box> order = new WidgetOrders.XY<Box>();

        testOrder(offsets, order);
    }
    
    @Test
    public void testRowSpan()
    {
        /*         
         1  2  4
         1     4
         1  3  4
         */
        int[][] offsets =
            {
                { 0, 0, 10, 30 },               
                { 15, 0, 10, 5 },
                { 15, 20, 10, 5 },
                { 30, 0, 10, 30 },
            };
        Order<Box> order = new WidgetOrders.XY<Box>();

        testOrder(offsets, order);
    }
    
    @Test
    public void testStairs()
    {
        /*         
         1
         1  2
            2  3
               3            
         */
        int[][] offsets =
            {
                { 0, 0, 10, 20 },               
                { 15, 10, 10, 20 },
                { 30, 20, 10, 20 },
            };
        Order<Box> order = new WidgetOrders.XY<Box>();

        testOrder(offsets, order);
    }
    
    @Test
    public void testStairsRowSpan()
    {
        /*         
         1     3   5
         1  2      5
            2  4   5
               4   5         
         */
        int[][] offsets =
            {
                { 0, 0, 10, 20 },               
                { 15, 10, 10, 20 },
                { 30, 0, 10, 10 },
                { 30, 15, 10, 20 },
                { 40, 0, 10, 35 },
            };
        Order<Box> order = new WidgetOrders.XY<Box>();

        testOrder(offsets, order);
    }
    
    @Test
    public void testDomino()
    {
        /*         
         1     3   
         1  2  3  4
            2     4
                           
         */
        int[][] offsets =
            {
                { 0, 0, 10, 20 },               
                { 15, 10, 10, 20 },
                { 30, 0, 10, 20 },               
                { 45, 10, 10, 20 },
            };
        Order<Box> order = new WidgetOrders.XY<Box>();

        testOrder(offsets, order);
    }
    
    private void testOrder(int[][] offsets, Order<Box> order)
    {
        List<Box> boxes = toBoxes(offsets);
        
        List<Box> reversed = new ArrayList<Box>(boxes);
        Collections.reverse(reversed);
        
        List<Box> ordered = order.order(reversed);
                
        for(int i = 0; i < boxes.size(); i++)
        {
            Assert.assertEquals(boxes.get(i).rect, ordered.get(i).rect);
        }
    }
    
    private static List<Box> toBoxes(int[][] offsets)
    {
        Box[] boxes = new Box[offsets.length];
        for(int i = 0; i < offsets.length; i++)
        {
            boxes[i] = new Box(new Rectangle(offsets[i][0], offsets[i][1], offsets[i][2], offsets[i][3]));
        }
        return Arrays.asList(boxes);
    }
    
}
