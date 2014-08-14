package org.testobject.kernel.imaging.fingerprint;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.testobject.commons.util.image.Image;
import org.testobject.commons.util.image.ImageUtil;
import org.testobject.commons.util.io.FileUtil;

public class TestImageDistance
{    
    public static void main(String ... av) throws Exception
    {
        final int NUM_ICONS = 41;
        
        ImageFingerprint [] ff = new ImageFingerprint[NUM_ICONS];
        
        for(int i = 0; i < NUM_ICONS; i++)
        {
            File file = new File("samples/icon" + (i + 1) + ".png");
            
            Image.Int im = ImageUtil.Convert.toImageInt(ImageIO.read(FileUtil.readFileFromClassPath(file)));
            
            ff[i] = new ImageFingerprint(im, 0xf2, 0xf1, 0xf0);
        }
        
        double lumaDistance[][] = new double[NUM_ICONS][NUM_ICONS];
        double chromaDistance[][] = new double[NUM_ICONS][NUM_ICONS];
        
        for(int i = 0; i < NUM_ICONS; i++)
        {
            for(int j = 0; j <= i; j++)
            {
                lumaDistance[i][j] = ImageFingerprint.lumaDistanceL1(ff[i], ff[j]); 
                chromaDistance[i][j] = ImageFingerprint.chromaDistanceL1(ff[i], ff[j]); 
            }
        }
        
        TreeMap<Double,int[]> proximityLuma = new TreeMap<Double,int[]>();
        TreeMap<Double,int[]> proximityLumaChroma = new TreeMap<Double,int[]>();

        for(int i = 0; i < NUM_ICONS; i++)
        {
            for(int j = 0; j < i; j++)
            {
                proximityLuma.put(lumaDistance[i][j], new int[] {i, j});
                proximityLumaChroma.put(lumaDistance[i][j] + chromaDistance[i][j], new int[] {i, j});
            }
        }
        
        System.out.println("\nClosest pairs by luma:");
        int count = 0;
        for(Map.Entry<Double,int[]> ent : proximityLuma.entrySet())
        {
            System.out.println("" + ent.getKey() + ": icon" + (ent.getValue()[0] + 1) + " <-> icon" + (ent.getValue()[1] + 1));
            if(++count > 10) break;
        }

        System.out.println("\nClosest pairs by luma+chroma:");
        count = 0;
        for(Map.Entry<Double,int[]> ent : proximityLumaChroma.entrySet())
        {
            System.out.println("" + ent.getKey() + ": icon" + (ent.getValue()[0] + 1) + " <-> icon" + (ent.getValue()[1] + 1));
            if(++count > 10) break;
        }
    }

}
