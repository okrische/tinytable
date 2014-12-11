package il.technion.TinyTable;

import junit.framework.Assert;
import il.technion.TinyTable.TinyTableWithCounters;

import org.junit.Test;

public class TinyTableWithCountersUnitTest {
	
	@Test
	public void testStoreBasic()
	{
		int itemSize =6; 
		int bucketCapacity =40;
		int nrBuckets=1;
		TinyTableWithCounters tt = new TinyTableWithCounters(itemSize, bucketCapacity, nrBuckets);
		// requires 2 counter items. 
		tt.StoreValue("Magic", 100);
		Assert.assertTrue(tt.GetValue("Magic") ==100);
		// require 3 counter items. 
		tt.StoreValue("Magic", 8000);
		Assert.assertTrue(tt.GetValue("Magic") ==8000);
		// require 1 counter item. 
		tt.StoreValue("Magic", 20);
		Assert.assertTrue(tt.GetValue("Magic") ==20);


	}
	@Test
	public void testStoreAdvanced()
	{
		int itemSize =6; 
		int bucketCapacity =40;
		int nrBuckets=5;
		TinyTableWithCounters tt = new TinyTableWithCounters(itemSize, bucketCapacity, nrBuckets);
		
		int nrItems = 40;
		for (int i =0; i<nrItems; i++)
		{
			for(int j=0; j<i;j++)
			{

				Assert.assertTrue(tt.GetValue("Magic" + j)==j);
			}
			tt.StoreValue("Magic" +i, i);
			
		}



	}

}
