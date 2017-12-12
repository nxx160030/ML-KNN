import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

public class Cluster {
	
	// assignments including order and value of rgb array
	private HashMap<Integer,Integer> map = new HashMap<Integer,Integer>();
	// mean order and value of rgb array
	private int mean_order;
	private int mean_value;
	
	public Cluster(int order, int value)
	{
		mean_order = order;
		mean_value = value;
	}
	
	public HashMap<Integer,Integer> getMap()
	{
		return map;
	}
	
	public int getMeanValue()
	{
		return mean_value;
	}
	
	public int getMeanOrder()
	{
		return mean_order;
	}
	
	// calculate new means according to assignments
	public void setMean()
	{
		double mean = 0;
		mean_value = 0;
		int mean_alpha = 0;
		int mean_red = 0;
		int mean_green = 0;
		int mean_blue = 0;
		int count = 0;
		
		Set<Entry<Integer,Integer>> set = map.entrySet();
		Iterator<Entry<Integer,Integer>> itr = set.iterator();
		while(itr.hasNext())
		{
			Entry<Integer,Integer> entry = itr.next();
			int value = entry.getValue();
			mean_alpha += ((value >> 24) & 0xFF);
			mean_red += ((value >> 16) & 0xFF);
			mean_green += ((value >> 8) & 0xFF);
			mean_blue += (value & 0xFF);
			count++;
		}
		
		if(count>0)
		{
			mean_alpha = (int)(mean_alpha/count);
			mean_red = (int)(mean_red/count);
			mean_green = (int)(mean_green/count);
			mean_blue = (int)(mean_blue/count);
			mean = ((mean_alpha & 0x000000FF) << 24)|((mean_red & 0x000000FF) << 16)|
					((mean_green & 0x000000FF) << 8)|((mean_blue & 0x000000FF) << 0);
		}
		
		double min = Double.MAX_VALUE;
		Iterator<Entry<Integer,Integer>> itr2 = set.iterator();
		while(itr2.hasNext())
		{
			Entry<Integer,Integer> entry = itr2.next();
			int value = entry.getValue();
			
			if(min>Math.abs(value - mean))
			{
				min = Math.abs(value - mean);
				mean_value = value;
				mean_order = entry.getKey();
			}
		}	

	}
	
	public double getTotalDist()
	{
		double total_distance = 0;
		int mean_alpha = mean_value >> 24 & 0xFF;
		int mean_red = mean_value >> 16 & 0xFF;
		int mean_green = mean_value >> 8 & 0xFF;
		int mean_blue = mean_value & 0xFF;
		
		Set<Entry<Integer,Integer>> set = map.entrySet();
		Iterator<Entry<Integer,Integer>> itr = set.iterator();
		while(itr.hasNext())
		{
			Entry<Integer,Integer> entry = itr.next();
    		int rgb_alpha = (entry.getValue() >> 24 & 0xFF);
    		int rgb_red = (entry.getValue() >> 16 & 0xFF);
    		int rgb_green = (entry.getValue() >> 8 & 0xFF);
    		int rgb_blue = (entry.getValue() & 0xFF);
    		
			double dist_alpha = rgb_alpha - mean_alpha;
			double dist_red = rgb_red - mean_red;
			double dist_green = rgb_green - mean_green;
			double dist_blue = rgb_blue - mean_blue;
			
			double point_distance = Math.sqrt(dist_alpha*dist_alpha+dist_red*dist_red+
					dist_green*dist_green+dist_blue*dist_blue);
			total_distance += point_distance;
		}	
		return total_distance;
	}
}
