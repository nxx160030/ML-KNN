
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import javax.imageio.ImageIO;
 

public class KMeans {
	
	private static int[] rgb;
	
    public static void main(String [] args){
    	
	if (args.length < 3){
	    System.out.println("Usage: Kmeans <input-image> <k> <output-image>");
	    return;
	}
	try{

		File read = new File(args[0]);
	    BufferedImage originalImage = ImageIO.read(read);
	    
	    int k=Integer.parseInt(args[1]);
	    
	    String output = args[2]+"_" + k + ".jpg";
	    File write = new File(output);
	    BufferedImage kmeansJpg = null;
	    
	    // re-do the compression for 20 times and calculate the average compression ratio and variance
	    double[] comp_ratio = new double[20];
	    double average = 0;
	    double var = 0;
	    for(int i=0;i<20;i++)
	    {
		    kmeansJpg = kmeans_helper(originalImage,k);
		    ImageIO.write(kmeansJpg, "jpg", write); 
		    System.out.println("the " + (i+1) +"th output is: " + write.getPath());
		    double ratio = (double)write.length()/read.length()*100;
		    System.out.println("the  " + (i+1) +"th compression ratio is: " + ratio + "%");
		    comp_ratio[i] = ratio;
		    average += ratio;
	    }
	    // average and variance
	    average = average/20;
	    System.out.println("the average compression ratio is: " + average + "%");
	    for(int i=0;i<20;i++)
	    {
	    	var += (average - comp_ratio[i])*(average - comp_ratio[i]);
	    }
	    System.out.println("the variance is: " + var);
	    	    
	}catch(IOException e){
	    System.out.println(e.getMessage());
	}	
    }
    
    private static BufferedImage kmeans_helper(BufferedImage originalImage, int k){
	int w=originalImage.getWidth();
	int h=originalImage.getHeight();
	BufferedImage kmeansImage = new BufferedImage(w,h,originalImage.getType());
	Graphics2D g = kmeansImage.createGraphics();
	g.drawImage(originalImage, 0, 0, w,h , null);
	// Read rgb values from the image
	rgb=new int[w*h];
	int count=0;
	for(int i=0;i<w;i++){
	    for(int j=0;j<h;j++){
	    	rgb[count++]=kmeansImage.getRGB(i,j);
	    }
	}
	// Call kmeans algorithm: update the rgb values
	kmeans(k);

	// Write the new rgb values to the image
	count=0;
	for(int i=0;i<w;i++){
	    for(int j=0;j<h;j++){
		kmeansImage.setRGB(i,j,rgb[count++]);
	    }
	}
	return kmeansImage;
    }

    // Your k-means code goes here
    // Update the array rgb by assigning each entry in the rgb array to its cluster center
    private static void kmeans(int k){
    	  	
    	// assign randomly k clustering means
    	List<Cluster> cluster_list = new ArrayList<Cluster>();
    	
    	// randomly create k clusters
    	int[] randoms = new Random().ints(0,rgb.length).distinct().limit(k).toArray();
    	for(int i=0;i<randoms.length;i++)
    		cluster_list.add(new Cluster(randoms[i],rgb[randoms[i]]));
    	

        // update until converge
        update(cluster_list);

    	
    	// assign rgb values to means
    	ListIterator<Cluster> itr1 = cluster_list.listIterator();
		while(itr1.hasNext())
		{
			Cluster cluster = itr1.next();
			int color = cluster.getMeanValue();
			HashMap<Integer,Integer> map = cluster.getMap();
			Set<Entry<Integer,Integer>> set = map.entrySet();
			Iterator<Entry<Integer,Integer>> itr2 = set.iterator();
			while(itr2.hasNext())
			{
				Entry<Integer,Integer> entry = itr2.next();
				rgb[entry.getKey()] = color;
			}
		}
  	
    }
    
    // update k clusters until converge
    private static void update(List<Cluster> cluster_list)
    {
    	
    	// clear assignments every time before update
    	ListIterator<Cluster> itr1 = cluster_list.listIterator();
		while(itr1.hasNext())
		{
			Cluster cluster = itr1.next();
			cluster.getMap().clear();
		}		
		
		// assign according to means
    	for(int i=0;i<rgb.length;i++)
    	{
    		double distance = Double.MAX_VALUE;
    		int index = 0;
    		int rgb_alpha = (rgb[i] >> 24) & 0xFF;
    		int rgb_red = (rgb[i] >> 16) & 0xFF;
    		int rgb_green = (rgb[i] >> 8) & 0xFF;
    		int rgb_blue = (rgb[i] & 0xFF);
    		
    		
    		ListIterator<Cluster> itr2 = cluster_list.listIterator();
    		while(itr2.hasNext())
    		{
    			Cluster cluster = itr2.next();
    			int mean = cluster.getMeanValue();
    			int mean_alpha = (mean >> 24) & 0xFF;
    			int mean_red = (mean >> 16) & 0xFF;
    			int mean_green = (mean  >> 8) & 0xFF;
    			int mean_blue = mean & 0xFF;
    			
    			double dist_alpha = rgb_alpha - mean_alpha;
    			double dist_red = rgb_red - mean_red;
    			double dist_green = rgb_green - mean_green;
    			double dist_blue = rgb_blue - mean_blue;
    			
    			double point_distance = Math.sqrt(dist_alpha*dist_alpha+dist_red*dist_red
    					+dist_green*dist_green+dist_blue*dist_blue);
    			
    			if(distance>point_distance)
    			{
    				index = cluster_list.indexOf(cluster);
    				distance = point_distance;
    			}
    		}
    		
    		cluster_list.get(index).getMap().put(i, rgb[i]);
    	}   	
    	
    	// calculate total distances before and after reassign means
    	double old_distance = 0;
    	ListIterator<Cluster> itr3 = cluster_list.listIterator();
		while(itr3.hasNext())
		{
			Cluster cluster = itr3.next();
			old_distance += cluster.getTotalDist();
			cluster.setMean();
		}
		
	   	double new_distance = 0;
    	ListIterator<Cluster> itr4 = cluster_list.listIterator();
		while(itr4.hasNext())
		{
			Cluster cluster = itr4.next();
			new_distance += cluster.getTotalDist();
		}

		// recursively update until converge
		if(old_distance<=new_distance)
		{
			return;
		}
		else
		{
			update(cluster_list);
		}   	   	
    }
}
