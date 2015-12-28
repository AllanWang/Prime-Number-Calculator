package com.pitchedapps.primenumbercalculator;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

// 2015/12/24

public class CalculatorPrimeNumber {

	@SuppressWarnings({ "unchecked", "resource" })
	public static String primeNumberCalculator(Long number) throws IOException, ClassNotFoundException {
		
		ArrayList<Long> list = new ArrayList<Long>();
		Long min = (long) 1;

		//creates text file where arraylist will be stored
		if (!new File("data/data/com.pitchedapps.primenumbercalculator/prime.txt").isFile()) {
			Log.d("Prime", "File not found, creating new one");
			File dir = new File("data/data/com.pitchedapps.primenumbercalculator");
			dir.mkdirs();
			File prime = new File(dir, "prime.txt");
			prime.createNewFile();
		} else { //if file found, load existing arraylist
			Log.d("Prime", "File found");
			FileInputStream fis = new FileInputStream("data/data/com.pitchedapps.primenumbercalculator/prime.txt");
			ObjectInputStream ois = new ObjectInputStream(fis);
			list = (ArrayList<Long>)ois.readObject();
			if (list.size() > 0) {
				min = list.get(list.size()-1);
			}
		}

		boolean range = false; //TODO change
		String output = new String();
		Log.d("Prime", "Number in testing is: " + number);
		if (range) {
			if (number < 3) { //1 and 2 are not prime numbers
				output = "No prime numbers found";
			} else if (min == 1) { //there is no existing arraylist; get entire prime list number
				getPrimeInRange(min, number, list);
				output = printPrimeInRange(list.size(), number, list, output);
			} else if (min == number) {
				output = printPrimeInRange(list.size(), number, list, output);
			} else if (min > number) { //if true, simply show section of arraylist below (number)
				Long max = (long) 3;
				int index = 1;
				while (max < number) {
					index++;
					max = list.get(index);
				}
				output = printPrimeInRange(list.size(), number, list, output);
			} else {
				getPrimeInRange(min, number, list);
				output = printPrimeInRange(list.size(), number, list, output);
			}
		} else {
			if (isPrime(number, min, list)) {
				Log.d("Prime", number + " is a prime in the list");
				output = "It\'s a prime number!";
			} else {
				Log.d("Prime", number + " is within the range of the list but is not prime.");
				output = "It\'s not a prime number.";
			}
		}
        
		try {
		    FileOutputStream fos = new FileOutputStream("data/data/com.pitchedapps.primenumbercalculator/prime.txt");
		    ObjectOutputStream oos = new ObjectOutputStream(fos);   
		    oos.writeObject(list); // write MenuArray to ObjectOutputStream
		    oos.close(); 
		} catch(Exception ex) {
		    ex.printStackTrace();
		}
		
		FileInputStream fis = new FileInputStream("data/data/com.pitchedapps.primenumbercalculator/prime.txt");
		ObjectInputStream ois = new ObjectInputStream(fis);
		ArrayList<Long> list2 = (ArrayList<Long>)ois.readObject();

		return output;
	}
	
	public static boolean isPrime (Long number, Long min, ArrayList<Long> list) {
		if (number == 2) {
			return true;
		}
		if (number % 2 == 0) {
			return false;
		} else if (inList(number, min, list)) {
			return true;
		} else if (min > number) {
			return false;
		} else {
			Long max = (long) Math.sqrt((double) number);
			basicIsPrime(min, max);
		}
		return true;
	}
	
	public static boolean inList (Long number, Long min, ArrayList<Long> list) {
		if (min > number) { //first checks if number is smaller than biggest arraylist number
			if (list.contains(number)) { //checks if number is in arraylist
				return true;
			} 
		}
		return false;
	}
	
	public static void getPrimeInRange (Long min, Long number, ArrayList<Long> list) {
		min += 2;
		while (min <= number) {
			 if (basicIsPrime((long) 3, min)) {
				 list.add(min);
			 }
			 min += 2;
		}
	}
	
	public static boolean basicIsPrime (Long min, Long number) {
		if (number < 16) { //added just to avoid issue with the square root below
			if (number == 2 || number == 3 || number == 5 || number == 7 || number == 11 || number == 13) {
				return true;
			} else {
				return false;
			}
		} else {
			Long max = (long) Math.sqrt((double) number);
			while (min <= max) { //no need to check mod after the square root of the number
				if (number % min == 0) {
					return false;
				} else {
					min += 2;
				}
			}
		}
		return true;
	}
	
	public static String printPrimeInRange (int index, Long number, ArrayList<Long> list, String output) {
		output = "Prime numbers until\n" + number + ": ";
		//note that index should be the number of items to be displayed, or 1 bigger than the final array size
		int trueIndex = index;
		while (index > 1) {
			output += list.get(trueIndex - index) + ", ";
			index--;
		}
		output += list.get(trueIndex - 1); //prints last value without extra comma afterwards
		return output;
	}


}
