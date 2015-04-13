package email.util;

import java.util.Hashtable;

import DAO.SequenceDAO;
import email.to.SequenceTO;

public class SequenceUtil {
	private static int count = 0;
	private static int cache = 20;
	private static SequenceDAO sequenceDAO = new SequenceDAO();
	private static Hashtable<String, SequenceTO> sequenceHash = new Hashtable<String, SequenceTO>();

	public synchronized static String getNext(String sequenceName) {

		SequenceTO sequenceTO = null;
		String next = null;

		if (StringUtil.isNotEmpty(sequenceName)) {
			sequenceTO = getSequence(sequenceName, true);
			count = sequenceTO.getCount();
			cache = sequenceTO.getCache();

			// make sure count is less than cache
			if (count < cache) {
				next = sequenceTO.incrementCurrent();
			} else {
				// Cache more sequences
				sequenceTO = getSequence(sequenceName, false);
				// get next value now
				next = sequenceTO.incrementCurrent();

			}// EOf if (count++)
		}// if(StringUtil.isNotEmpty(name))

		return next;
	}// getNext

	private synchronized static SequenceTO getSequence(String sequenceName,
			boolean fromMemory) {
		SequenceTO sequenceTO = null;
		long newCurrent = 0;

		if (fromMemory && sequenceHash.containsKey(sequenceName)) {
			sequenceTO = sequenceHash.get(sequenceName);
		} else {

			// get it from database
			sequenceTO = sequenceDAO.getSequence(sequenceName);

			// if sequence don't exist in database yet then initialize it and
			// persist it to database
			if (sequenceTO == null) {

				sequenceTO = new SequenceTO();
				sequenceTO.setName(sequenceName);
				sequenceTO.setCount(0);
				sequenceTO.setIncrement(1);
				sequenceTO.setCache(cache);
				sequenceTO.setCurrent(cache);

				// write it into database
				sequenceDAO.updateSequence(sequenceTO);
			} else {
				newCurrent = EncodeUtil.decode(sequenceTO.getCurrent())
						+ sequenceTO.getCache() * sequenceTO.getIncrement();
				sequenceTO.setCurrent(EncodeUtil.encode(newCurrent));

				// write newCurrent for the sequence into database
				sequenceDAO.updateSequence(sequenceTO);
			}

			// set it to sequenceHash
			sequenceHash.put(sequenceName, sequenceTO);
		}// if(sequenceHash.containsKey(name)){

		return sequenceTO;
	}// getSequence

	public static void main(String[] args) {

		long current = 0;

		for (int i = 0; i < 20; i++) {
			current = EncodeUtil.decode(SequenceUtil.getNext("Test"));
			System.out.println(current);
			try {
				Thread.sleep(10);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}

	}// main
}
