package email.to;

import email.util.EncodeUtil;


public class SequenceTO {
	private String name = null;
	private String current = null;
	private int increment = 0;
	private int cache = 0;
	private int count = 0;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public void setCurrent(String current) {
		this.current = current;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCurrent() {
		return current;
	}

	public void setCurrent(long current) {
		this.current = EncodeUtil.encode(current);
	}

	//helper method that increment the current value by the argument
	public String incrementCurrent(){
		this.setCount(++count);
		long newCurrent = EncodeUtil.decode(current) - this.cache + this.getIncrement()*this.count;		
		return EncodeUtil.encode(newCurrent);
	}
	
	public int getIncrement() {
		return increment;
	}

	public void setIncrement(int increment) {
		this.increment = increment;
	}

	public int getCache() {
		return cache;
	}

	public void setCache(int cache) {
		this.cache = cache;
	}

}
