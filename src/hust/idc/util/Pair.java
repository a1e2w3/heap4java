package hust.idc.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Pair�࣬������equals��hashCode��������������Ϊӳ���Key��
 * Ӧȷ������A��B��������ӳ���Key����������equals��hashCode����
 * Pair֧��������ķ�ʽclone��ǰ��������A��B���ǿ����л��ģ�����ֻ��ǳ����
 * @author WangCong
 *
 * @param <T> ��һ������������
 * @param <S> �ڶ�������������
 */
public class Pair<T, S> implements Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6086892824234304856L;
	protected T first;
	protected S second;
	
	public Pair(){
		this.first = null;
		this.second = null;
	}
	
	public Pair(T first, S second){
		this.first = first;
		this.second = second;
	}
	
	// getter
	public final T getFirst() {
		return first;
	}
	public final S getSecond() {
		return second;
	}
	
	public boolean isEmpty(){
		return this.first == null || this.second == null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString(){
		return String.format("Pair[%s,%s]", 
							this.first == null ? "null" : this.first.toString(), 
							this.second == null ? "null" : this.second.toString());
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((first == null) ? 0 : first.hashCode());
		result = prime * result + ((second == null) ? 0 : second.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Pair<?, ?>)) {
			return false;
		}
		Pair<?, ?> other = (Pair<?, ?>) obj;
		if (first == null) {
			if (other.first != null) {
				return false;
			}
		} else if (!first.equals(other.first)) {
			return false;
		}
		if (second == null) {
			if (other.second != null) {
				return false;
			}
		} else if (!second.equals(other.second)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Pair<T, S> clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		if(this.first instanceof Serializable && 
				this.second instanceof Serializable){
			try{
				//������д������
				ByteArrayOutputStream bo = new ByteArrayOutputStream();
				ObjectOutputStream oo = new ObjectOutputStream(bo);
				oo.writeObject(this);
				//�����������
				ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
				ObjectInputStream oi = new ObjectInputStream(bi);
				return (Pair<T, S>) (oi.readObject());
			} catch(Exception e){
				// try to clone directly
			} 
		}
		
		return (Pair<T, S>) super.clone();
	}

}
