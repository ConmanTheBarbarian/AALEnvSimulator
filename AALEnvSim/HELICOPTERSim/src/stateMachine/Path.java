package stateMachine;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class Path implements Iterable<Path.Part> {
	private static final String instanceInfix="___instance_";
	private Vector<Part> partVector=new Vector<Part>();
	public static final Part rootPart=new Part();
	public static class Part {
		private String part;
		private int instance;
		
		private Part() {
			this.part="/";
			this.instance=0;
		}
		/**
		 * @param part
		 * @param instance
		 */
		public Part(String part, int instance) {
			if (part.indexOf("/")>0) {
				throw new IllegalArgumentException("Part \""+part+"\" contains a \"/\"");
			}
			this.part = part;
			this.instance = instance;
		}
		/**
		 * @return the part
		 */
		public synchronized final String getPart() {
			return part;
		}
		/**
		 * @return the instance
		 */
		public synchronized final int getInstance() {
			return instance;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("%s%s%d", part, instanceInfix, instance);
		}
		
	}

	public Path() {
		// TODO Auto-generated constructor stub
	}
	public final synchronized Path addPart(final Part part) {
		if ((part.equals(rootPart) && this.partVector.size()==0 ) || (!part.equals(rootPart) && this.partVector.size()>0)) {
			this.partVector.add(part);
		} else { 
			throw new IllegalArgumentException("Cannot add root part here");
		}
		return this;
	}
	public final synchronized Path removePart(final Part part) {
		this.partVector.remove(part);
		return this;
	}
	public final synchronized Path insertPartBefore(final Part partToInsert, final Part part ) {
		final int index=partVector.indexOf(part);
		if (index<0) {
			throw new IllegalArgumentException("Part \""+part+"\" is not found");
		}
		if (part.equals(rootPart) && index>0) {
			throw new IllegalArgumentException("Root part can only be inserted at the beginning");
		}
		if (part.equals(rootPart) && index==0 && part.equals(partVector.get(0))) {
			throw new IllegalArgumentException("Root part already inserted at the beginning");			
		}
		partVector.insertElementAt(partToInsert, index);
		return this;
	}
	public final synchronized Path insertPartAfter(final Part partToInsert, final Part part ) {
		int index=partVector.indexOf(part);
		if (index<0) {
			throw new IllegalArgumentException("Part \""+part+"\" is not found");
		}
		if (part.equals(rootPart)) {
			throw new IllegalArgumentException("Cannot insert root part after anything");
		}
		++index;
		partVector.insertElementAt(partToInsert, index);
		return this;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb=new StringBuffer();
		for (int i=0; i<partVector.size(); ++i) {
			final Part p=partVector.get(i);
			if (i==0 && p.equals(rootPart)) {
				sb.append("/");
			} else if (i>0 && !p.equals(rootPart)) {
				sb.append("/"+p);
			} else {
				throw new IllegalStateException("Incorrect path");
			}
		}
		return sb.toString();
	}

	@Override
	public Iterator<Part> iterator() {
		return partVector.iterator();
	}
	 List<Part> getPartVector() {
		return partVector;
	}

}
