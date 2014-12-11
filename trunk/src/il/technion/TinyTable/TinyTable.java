package il.technion.TinyTable;



import il.technion.TinyTable.BitManipulation.ChainHelper;
import il.technion.TinyTable.BitManipulation.SimpleBitwiseArray;
import il.technion.TinyTable.HashFunctions.FingerPrintAux;
import il.technion.TinyTable.HashFunctions.GreenHashMaker;
import il.technion.TinyTable.RankIndexingTechnique.RankIndexingTechnique;

import java.util.List;

public class TinyTable extends SimpleBitwiseArray
{

	// used for debug - counts how many items in the table. 
	protected int nrItems;
	// base index array. 
	public long I0[];
	// IStar array. 
	public long[] IStar;
	// anchor distance array.
	public short[] A;
	
	// used as an object pool for the rank indexing technique. In order to prevent dynamic memory allocation. 
	private final byte[] offsets;
	private final byte[] chain;
	//Hash function with an object pool... recycle! 
	GreenHashMaker hashFunc;

	public TinyTable(int itemsize, int bucketcapacity,int nrBuckets)
	{
		super(bucketcapacity*nrBuckets, itemsize,bucketcapacity);
		this.maxAdditionalSize = 0;
		this.nrItems = 0;
		I0 = new long[nrBuckets];
		IStar = new long[nrBuckets];
		A = new short[nrBuckets];
		hashFunc = new GreenHashMaker(itemsize+maxAdditionalSize, nrBuckets, 64);
		offsets = new byte[64];
		chain = new byte[64];

		this.BucketCapacity = bucketcapacity;
	}
	public void addItem(long item)
	{

		//FingerPrintAux fpaux = ;
		this.addItem(hashFunc.createHash(item));	
	}
	public void addItem(String item)
	{

		//FingerPrintAux fpaux = ;
		this.addItem(hashFunc.createHash(item));	
	}

	public void RemoveItem(long i)
	{

		FingerPrintAux fpaux = hashFunc.createHash(i);
		this.removeItem(fpaux);	
	}
	public void RemoveItem(String i)
	{

		FingerPrintAux fpaux = hashFunc.createHash(i);
		this.removeItem(fpaux);	
	}
	public boolean containItem(String item)
	{
//		FingerPrintAux fpaux = ;
		//System.out.println(fpaux.toString());
		return this.containsItem(hashFunc.createHash(item));
	}
	public boolean containItem(long item)
	{
		return this.containsItem(hashFunc.createHash(item));
	}

	public long howmany(int bucketId, int chainId,long fingerprint)
	{

		long[] chain = this.getChain(bucketId, chainId);
		return ChainHelper.howmany(chain, fingerprint, this.itemSize-1);

		
	}
	@Override
	public int getBucketStart(int bucketId)
	{
		return this.bucketBitSize*bucketId + this.A[bucketId]*this.itemSize;
	}
	@Override
	public int getNrItems(int bucketId)
	{
		return Long.bitCount(this.I0[bucketId]) + Long.bitCount(this.IStar[bucketId]);
	}





	/**
	 * Adds a new fingerPrint to the following bucketNumber and chainNumber, the maximal size 
	 * of supported fingerprint is 64 bits, and it is assumed that the actual data sits on the LSB bits of
	 * long. 
	 * 
	 * According to our protocol, addition of a fingerprint may result in expending the bucket on account of neighboring buckets, 
	 * or down sizing the stored fingerprints to make room for the new one. 
	 * 
	 * In order to support deletions, deleted items are first logically deleted, and are fully 
	 * deleted only upon addition. 
	 * 
	 * @param bucketNumber
	 * @param chainNumber
	 * @param fingerPrint
	 */
	protected void addItem(FingerPrintAux fpAux)
	{
		int nextBucket = this.findFreeBucket(fpAux.bucketId);
		upscaleBuckets(fpAux.bucketId,nextBucket);

		int idxToAdd = RankIndexingTechnique.addItem(fpAux, I0, IStar,offsets,chain);
		// if we need to, we steal items from other buckets. 
		this.PutAndPush(fpAux.bucketId, idxToAdd, fpAux.fingerprint);
		return;
	}

	

	
	protected void removeItem(FingerPrintAux fpaux)
	{
		moveToEnd(fpaux);
		int bucket =0;
		this.RemoveAndShrink(fpaux.bucketId);
		removeItemFromIndex(fpaux);

		for(int i =fpaux.bucketId+1; i<fpaux.bucketId+this.I0.length;i++)
		{
			bucket = (i)%this.I0.length;
			if(A[bucket]>0)
			{
			
				RemoveAndShrink(bucket);
				A[bucket]--;
				continue;
			}
			else
			{

				break;
			}
		}
		

	}

	private int FindItem(FingerPrintAux fpaux)
	{	
//		List<Integer> chain = RankIndexHashing.getChain(chainNumber, L[bucketNumber], IStar[bucketNumber]);
		for (int i=0; i<this.chain.length;i++ ) {
			if(chain[i]<0)
				break;
			long fpToCompare = this.Get(fpaux.bucketId, chain[i]);
			if(fpToCompare == fpaux.fingerprint)
				return chain[i];
		}
		return -1;

	}
	private int moveToEnd(FingerPrintAux fpaux)
	{	
		
		int chainoffset= RankIndexingTechnique.getChainAndUpdateOffsets(fpaux,I0,IStar,offsets,chain)-1;
		int removedOffset = 0;
		//		for (Integer itemOffset : chain) {
		//			
		//			if(itemOffset<0){
		//				throw new RuntimeException("Item is not there!");
		//			}

		int itemOffset = this.FindItem(fpaux);
		if(itemOffset<0)
			throw new RuntimeException("Not found!");

		removedOffset = itemOffset;
		int lastOffset = chain[chainoffset];
		long lastItem = this.Get(fpaux.bucketId, lastOffset);
//		Assert.assertTrue(chain.containsitemOffset));
		this.Put(fpaux.bucketId, removedOffset, lastItem);
		this.Put(fpaux.bucketId, lastOffset, 0l);
		return lastOffset;



}

	

	protected void removeItemFromIndex(FingerPrintAux fpaux) {
		int chainSize = RankIndexingTechnique.getChainAndUpdateOffsets(fpaux, I0, IStar,this.offsets,this.chain,fpaux.chainId)-1;
		RankIndexingTechnique.RemoveItem(fpaux.chainId, I0, IStar,fpaux.bucketId,offsets,chain,chainSize);
	}

	/**
	 * finds a the closest bucket that can accept the new item. 
	 * if the current bucket is under maximal capacity it is the current bucket, otherwise we steal fingerprints from buckets until we reach
	 * a free bucket. 
	 * @param bucketId
	 * @return
	 */
	private int findFreeBucket(int bucketId) {
		bucketId = bucketId%this.A.length;
		while(this.getNrItems(bucketId)+this.A[bucketId] >=this.BucketCapacity)
		{

			bucketId++;
			bucketId = bucketId%this.A.length;

		}
		return bucketId;
	}

	private void resizeBuckets(int bucketId,boolean IncrementAnchor) {
		if(!IncrementAnchor)
			return;
		this.replaceMany(bucketId, 0, 0l,this.getBucketStart(bucketId));
		this.A[bucketId]++;
		return;
	}


	public long[] getChain(int bucketId, int chainId)
	{
		List<Integer> chain = RankIndexingTechnique.getChain(chainId, I0[bucketId], IStar[bucketId]);
		long[] result = new long[chain.size()];
		int i =0;
		for (Integer itemOffset : chain) {
			if(itemOffset<0)
				return null;

			long item = this.Get(bucketId, itemOffset);
			result[i++] = item;
		}
		return result;
	}
	
	

	







	private void upscaleBuckets(int bucketNumber, int lastBucket)
	{
		//Bucket may be wrapped around too! 
		while(lastBucket!=bucketNumber)
		{

			resizeBuckets(lastBucket,true);


			if(--lastBucket<0)
			{
				lastBucket = A.length-1;
			}
		}
		return;

	}
	boolean containsItem(FingerPrintAux fpaux)
	{	
		RankIndexingTechnique.getChainAndUpdateOffsets(fpaux, I0, IStar, offsets, chain, fpaux.chainId);

		return (this.FindItem(fpaux)>=0);


	}

	





/**
 * Put a value at location idx, if the location is taken shift the items to
 * be left until an open space is discovered.
 * 
 * @param idx
 *            - index to put in
 * @param value
 *            - value to put in
 * @param mod 
 * 				- bucket mod, (in order to decode bucket)
 * @param size 
 * 				- bucket item size. (in order to decode bucket)
 * @param chainNumber 
 */
protected void PutAndPush(int bucketId, int idx, final long value) {
	 this.replaceMany(bucketId, idx, value,this.getBucketStart(bucketId));
	this.nrItems++;
	return;
}

protected void RemoveAndShrink(int bucketId) {
	this.replaceBackwards(bucketId,this.getBucketStart(bucketId));
	return;
}





public int getNrItems() {

	return this.nrItems;
}












}
