package com.smart.sort;

import java.util.Comparator;

import com.smart.entities.Contact;

public class NameComparator implements Comparator<Contact> {

	@Override
	public int compare(Contact o1, Contact o2) {
		String s1 = o1.getName().toUpperCase();
		String s2 = o2.getName().toUpperCase();
		return s1.compareTo(s2);
	}

}
