package il.technion.TinyTable;

import junit.framework.Assert;

import org.junit.Test;

import il.technion.TinyTable.TinyTable;

public class TinyTableUnitTest {
	
	@Test
	public void TestBasicAdd()
	{
		int fingerprintSize = 6;
		int bucketCapacity = 40; 
		int nrBuckets = 1; 
		
		TinyTable tt = new TinyTable(fingerprintSize, bucketCapacity, nrBuckets);
		
		tt.addItem("TinyTable1");
		tt.addItem("TinyTable2");
		tt.addItem("TinyTable3");

		Assert.assertTrue(tt.containItem("TinyTable1"));
		Assert.assertTrue(tt.containItem("TinyTable2"));
		Assert.assertTrue(tt.containItem("TinyTable3"));

		
	}
	@Test
	public void TestBasicRemove()
	{
		int fingerprintSize = 6;
		int bucketCapacity = 40; 
		int nrBuckets = 1; 
		
		TinyTable tt = new TinyTable(fingerprintSize, bucketCapacity, nrBuckets);
		
		tt.addItem("TinyTable1");
		tt.addItem("TinyTable2");
		tt.addItem("TinyTable3");

		Assert.assertTrue(tt.containItem("TinyTable1"));
		Assert.assertTrue(tt.containItem("TinyTable2"));
		Assert.assertTrue(tt.containItem("TinyTable3"));
		tt.RemoveItem("TinyTable1");
		tt.RemoveItem("TinyTable2");
		tt.RemoveItem("TinyTable3");

		Assert.assertFalse(tt.containItem("TinyTable1"));
		Assert.assertFalse(tt.containItem("TinyTable2"));
		Assert.assertFalse(tt.containItem("TinyTable3"));
	}
	
	/**
	 *  Fills TinyTable until capacity, verify that all items remain in table. 
	 */
	@Test
	public void AdvancedTestAdd()
	{
		int fingerprintSize = 6;
		int bucketCapacity = 40; 
		int nrBuckets = 100; 
		
		TinyTable tt = new TinyTable(fingerprintSize, bucketCapacity, nrBuckets);
		int totalItems = bucketCapacity*nrBuckets -1;
		for(int i =0; i<totalItems; i++)
		{
			tt.addItem(i);
			for(int j=0; j<i; j++)
			{
				Assert.assertTrue(tt.containItem(i));
			}
		}

	}
	@Test
	public void AdvancedTestRemove()
	{
		int fingerprintSize = 6;
		int bucketCapacity = 40; 
		int nrBuckets = 100; 
		
		TinyTable tt = new TinyTable(fingerprintSize, bucketCapacity, nrBuckets);
		int totalItems = bucketCapacity*nrBuckets -1;
		for(int i =0; i<totalItems; i++)
		{
			tt.addItem(i);
			for(int j=0; j<i; j++)
			{
				Assert.assertTrue(tt.containItem(i));
			}
		}
		// remove all the items...
		for(int i =totalItems-1; i>=0; i--)
		{
			
			// check that all the stuff we did not remove is still there!. 
			for(int j =0; j<=i;j++){
				Assert.assertTrue(tt.containItem(j));
			}
			
			// remove another one! - false positives may cause this one to be contained... 
			tt.RemoveItem(i);
		}
		// we removed all items now lets check that they are all gone. 
		for(int i =0; i<totalItems; i++)
		{
			Assert.assertFalse(tt.containItem(i));
		}


	}
	

}
