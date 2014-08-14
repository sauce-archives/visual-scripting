package org.testobject.commons.util.distances;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * @author nijkamp
 *
 */
public class Levenshtein<T>
{
    public interface Costs<T>
    {
        float substitution(T s, T t);
    }
    
    public enum Op
    {
        Match, Insert, Delete, Substitute
    }

    private final Costs<T> costs;
    private final List<T> s, t;

    // for all i and j, d[i,j] will hold the Levenshtein distance between
    // the first i characters of s1 and the first j characters of s2
    private final float[][] d;
    private final int m, n;
    private final float levenshtein;

    public Levenshtein(Costs<T> costs, List<T> s, List<T> t)
    {
        this.costs = costs;
        this.s = s;
        this.t = t;
        this.m = s.size();
        this.n = t.size();
        this.d = new float[m+1][n+1];
        this.levenshtein = compute();
    }

    private float compute()
    {
        for (int i = 0; i <= m; i++)
        {
            // the distance of any first string to an empty second string
            d[i][0] = i;
        }

        for (int j = 0; j <= n; j++)
        {
            // the distance of any second string to an empty first string
            d[0][j] = j;
        }

        for (int j = 1; j <= n; j++)
        {
            for (int i = 1; i <= m; i++)
            {
                T sc = s.get(i-1);
                T tc = t.get(j-1);
                if (sc.equals(tc))
                {
                    d[i][j] = d[i - 1][j - 1];
                }
                else
                {
                    float del = d[i - 1][j] + 1;
                    float ins = d[i][j - 1] + 1;
                    float sub = d[i - 1][j - 1] + costs.substitution(sc, tc);
                    d[i][j] = min(del, ins, sub);
                }
            }
        }

        return d[m][n];
    }
    
    private float min(float f1, float f2, float f3)
    {
        return Math.min(f1, Math.min(f2, f3));
    }
    
    public float getDistance()
    {
        return levenshtein;
    }
    
    public float[][] getMatrix()
    {
        return d;
    }
    
    /**
     * compute alignment by backtracking through the matrix
     * 
     * @return
     */
    public List<Op> getAlignment()
    {
        List<Op> ops = new LinkedList<Op>();
        int i = m, j = n;
        while(i > 0 || j > 0)
        {
            float me = d[i][j];
            float n = i > 0 ? d[i-1][j] : me+1;
            float w = j > 0 ? d[i][j-1] : me+1;
            float nw = i > 0 && j > 0 ? d[i-1][j-1] : me+1;
            
            float min = min(n, w, nw);
            if(min == me)
            {
                i--;
                j--;
                ops.add(Op.Match);
            }
            else if(min == n)
            {
                i--;
                ops.add(Op.Delete);
            }
            else if(min == w)
            {
                j--;
                ops.add(Op.Insert);
            }
            else
            {
                i--;
                j--;
                ops.add(Op.Substitute);
            }
        }
        Collections.reverse(ops);
        return ops;
    }
    
    public static List<Character> toChars(String x)
    {
        List<Character> result = new ArrayList<Character>(x.length());
        for (int i = 0; i < x.length(); i++)
        {
            result.add(x.charAt(i));
        }
        return result;
    }
    
    public static String print(float[][] matrix)
    {
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < matrix.length; i++)
        {
            for(float entry : matrix[i])
            {
                str.append((int) entry).append(' ');
            }
            if(i != matrix.length-1)
            {
                str.append('\n');
            }
        }
        return str.toString();
    }
    
    public static void main(String[] args)
    {
        Costs<Character> costs = new Costs<Character>()
        {
            @Override
            public float substitution(Character s, Character t)
            {
                return 1f;
            }
        };
        {
            System.out.println("kitten -> sitting");
            Levenshtein<Character> foo = new Levenshtein<Character>(costs, toChars("kitten"), toChars("sitting"));
            System.out.println("Levenshtein distance = " + foo.getDistance());
            System.out.println("Matrix:");
            System.out.println(print(foo.getMatrix()));
            System.out.println("Alignment: " + foo.getAlignment());
            System.out.println();
        }
        {
            System.out.println("abc -> acd");
            Levenshtein<Character> foo = new Levenshtein<Character>(costs, toChars("abc"), toChars("acd"));
            System.out.println("Levenshtein distance = " + foo.getDistance());
            System.out.println("Matrix:");
            System.out.println(print(foo.getMatrix()));
            System.out.println("Alignment: " + foo.getAlignment());
            System.out.println();
        }
        {
            System.out.println("a -> ba");
            Levenshtein<Character> foo = new Levenshtein<Character>(costs, toChars("a"), toChars("ba"));
            System.out.println("Levenshtein distance = " + foo.getDistance());
            System.out.println("Matrix:");
            System.out.println(print(foo.getMatrix()));
            System.out.println("Alignment: " + foo.getAlignment());
            System.out.println();
        }
    }
}
