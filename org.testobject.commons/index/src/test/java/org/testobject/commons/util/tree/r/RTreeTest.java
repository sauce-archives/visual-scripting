package org.testobject.commons.util.tree.r;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Random;

import org.junit.Test;
import org.testobject.commons.math.algebra.Rectangle;
import org.testobject.commons.util.collections.Lists;

/**
 * 
 * @author enijkamp
 * 
 */
public class RTreeTest {

    private static class Payload {
        public final Rectangle.Double rect;

        private Payload(Rectangle.Double rect) {
            this.rect = rect;
        }
    }

    private static class Adapter implements SpatialIndex.Adapter<Payload> {
        @Override
        public Rectangle.Double getBoundingBox(Payload payload) {
            return payload.rect;
        }
    }

    @Test
    public void testRemove() {
        SpatialIndex<Payload> index = new RTree.Factory<Payload>().create(new Adapter());
        index.put(payload(0, 0, 10, 10));
        final List<Payload> result = Lists.newLinkedList();
        index.remove(index.entries().iterator().next());
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    public void testContains() {
        SpatialIndex<Payload> index = new RTree.Factory<Payload>().create(new Adapter());
        index.put(payload(0, 0, 10, 10));
        final List<Payload> result = Lists.newLinkedList();
        index.contains(rect(0, 0, 10, 10), collect(result));
        assertThat(result.isEmpty(), is(false));
    }

    @Test
    public void testContainsSmaller() {
        SpatialIndex<Payload> index = new RTree.Factory<Payload>().create(new Adapter());
        index.put(payload(2, 2, 6, 6));
        final List<Payload> result = Lists.newLinkedList();
        index.contains(rect(0, 0, 10, 10), collect(result));
        assertThat(result.isEmpty(), is(false));
    }

    @Test
    public void testNotContains() {
        SpatialIndex<Payload> index = new RTree.Factory<Payload>().create(new Adapter());
        index.put(payload(5, 5, 10, 10));
        final List<Payload> result = Lists.newLinkedList();
        index.contains(rect(0, 0, 10, 10), collect(result));
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    public void testContainsMany() {
        SpatialIndex<Payload> index = new RTree.Factory<Payload>().create(new Adapter());
        Random rnd = new Random(0l);
        for(int i = 0; i < 10000; i++) {
            int x = Math.abs(rnd.nextInt()) % 10;
            int y = Math.abs(rnd.nextInt()) % 10;
            int w = (Math.abs(rnd.nextInt()) % 100) + 1;
            int h = (Math.abs(rnd.nextInt()) % 100) + 1;
            index.put(payload(x, y, h, w));
        }
        final List<Payload> result = Lists.newLinkedList();
        index.contains(rect(0, 0, 200, 200), collect(result));
        assertThat(result.size(), is(10000));
    }

    @Test
    public void testIntersects() {
        SpatialIndex<Payload> index = new RTree.Factory<Payload>().create(new Adapter());
        index.put(payload(0, 0, 10, 10));
        final List<Payload> result = Lists.newLinkedList();
        index.intersects(rect(5, 5, 10, 10), collect(result));
        assertThat(result.isEmpty(), is(false));
    }

    @Test
    public void testNotIntersects() {
        SpatialIndex<Payload> index = new RTree.Factory<Payload>().create(new Adapter());
        index.put(payload(0, 0, 10, 10));
        final List<Payload> result = Lists.newLinkedList();
        index.intersects(rect(15, 15, 10, 10), collect(result));
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    public void testIntersectsNotAll() {
        SpatialIndex<Payload> index = new RTree.Factory<Payload>().create(new Adapter());
        index.put(payload(0, 0, 10, 10));
        index.put(payload(0, 0, 20, 20));
        index.put(payload(5, 5, 10, 10));
        index.put(payload(25, 25, 10, 10));
        final List<Payload> result = Lists.newLinkedList();
        index.intersects(rect(0, 0, 20, 20), collect(result));
        assertThat(result.size(), is(3));
    }

    @Test
    public void testIntersectsMany() {
        SpatialIndex<Payload> index = new RTree.Factory<Payload>().create(new Adapter());
        Random rnd = new Random(0l);
        for(int i = 0; i < 10000; i++) {
            int x = Math.abs(rnd.nextInt()) % 10;
            int y = Math.abs(rnd.nextInt()) % 10;
            int w = (Math.abs(rnd.nextInt()) % 100) + 1;
            int h = (Math.abs(rnd.nextInt()) % 100) + 1;
            index.put(payload(x, y, h, w));
        }
        final List<Payload> result = Lists.newLinkedList();
        index.intersects(rect(0, 0, 200, 200), collect(result));
        assertThat(result.size(), is(10000));
    }

    @Test
    public void testNotIntersectsMany() {
        SpatialIndex<Payload> index = new RTree.Factory<Payload>().create(new Adapter());
        Random rnd = new Random(0l);
        for(int i = 0; i < 10000; i++) {
            int x = Math.abs(rnd.nextInt()) % 10;
            int y = Math.abs(rnd.nextInt()) % 10;
            int w = (Math.abs(rnd.nextInt()) % 100) + 1;
            int h = (Math.abs(rnd.nextInt()) % 100) + 1;
            index.put(payload(x, y, h, w));
        }
        final List<Payload> result = Lists.newLinkedList();
        index.intersects(rect(200, 200, 10, 10), collect(result));
        assertThat(result.isEmpty(), is(true));
    }

    @Test
    public void testIntersectsFullyContains() {
        SpatialIndex<Payload> index = new RTree.Factory<Payload>().create(new Adapter());
        index.put(payload(0, 0, 10, 10));
        index.put(payload(4, 4, 2, 2));
        final List<Payload> result = Lists.newLinkedList();
        index.intersects(rect(0, 0, 10, 10), collect(result));
        assertThat(result.size(), is(2));
    }

    private static SpatialIndex.Visitor<Payload> collect(final List<Payload> result) {
        return new SpatialIndex.Visitor<Payload>() {
            @Override
            public boolean visit(Payload payload) {
                result.add(payload);
                return true;
            }
        };
    }

    private static Payload payload(double x, double y, double w, double h) {
        return new Payload(rect(x, y, w, h));
    }

    private static Rectangle.Double rect(double x, double y, double w, double h) {
        return new Rectangle.Double(x, y, w, h);
    }

}
