package com.pitchedapps.primenumbercalculator;

import android.util.Log;

import java.io.IOException;

// 2015/12/24

public class CalculatorPrimeNumber {

	@SuppressWarnings({ "unchecked", "resource" })
	public static String primeNumberCalculator(Long number) throws IOException, ClassNotFoundException {
		
		Long min = (long) 1;

		if (Calculator.list.size() > 0 && min != 2) {
			min = Calculator.list.get(Calculator.list.size() - 1);
		}


		boolean range = false; //TODO change
		String output = new String();
		Log.d("Prime", "Number in testing is: " + number);
		if (range) {
			if (number < 3) { //1 and 2 are not prime numbers
				output = "No prime numbers found";
			} else if (min == 1) { //there is no existing arraylist; get entire prime list number
				getPrimeInRange(min, number);
				output = printPrimeInRange(Calculator.list.size(), number);
			} else if (min == number) {
				output = printPrimeInRange(Calculator.list.size(), number);
			} else if (min > number) { //if true, simply show section of arraylist below (number)
				Long max = (long) 3;
				int index = 1;
				while (max < number) {
					index++;
					max = Calculator.list.get(index);
				}
				output = printPrimeInRange(index, number);
			} else {
				getPrimeInRange(min, number);
				output = printPrimeInRange(Calculator.list.size(), number);
			}
		} else {
			if (isPrime(number, min)) {
				output = number + " is prime!";
			} else {
				output = number + " is not prime.";
			}
		}
        
//		try {
//		    FileOutputStream fos = new FileOutputStream("data/data/com.pitchedapps.primenumbercalculator/prime.txt");
//		    ObjectOutputStream oos = new ObjectOutputStream(fos);
//		    oos.writeObject(list); // write MenuArray to ObjectOutputStream
//		    oos.close();
//		} catch(Exception ex) {
//		    ex.printStackTrace();
//		}
//
//		FileInputStream fis = new FileInputStream("data/data/com.pitchedapps.primenumbercalculator/prime.txt");
//		ObjectInputStream ois = new ObjectInputStream(fis);
//		ArrayList<Long> list2 = (ArrayList<Long>)ois.readObject();

//		CalculatorSharedPreferences.saveList("prime", list);

		return output;
	}
	
	public static boolean isPrime (Long number, Long min) {
		if (number == 2) {
			Log.d("Prime", number + " is 2; it's a prime");
			return true;
		}
		if (number % 2 == 0) {
			Log.d("Prime", number + " is even; it's not a prime");
			return false;
		} else if (inList(number, min)) {
			Log.d("Prime", number + " is a prime in the list");
			return true;
		} else if (min > number) {
			Log.d("Prime", number + " is within the range of the list and is not prime");
			return false;
		} else {
			Log.d("Prime", number + " needs to be tested with basicIsPrime");
			if (min == 1) {
				min = (long) 3;
			}
			return basicIsPrime(min, number);
		}
	}
	
	public static boolean inList (Long number, Long min) {
		if (min > number) { //first checks if number is smaller than biggest arraylist number
			if (Calculator.list.contains(number)) { //checks if number is in arraylist
				return true;
			} 
		}
		return false;
	}
	
	public static void getPrimeInRange (Long min, Long number) {
		min += 2;
		while (min <= number) {
			 if (basicIsPrime((long) 3, min)) {
				 Calculator.list.add(min);
			 }
			 min += 2;
		}
	}
	
	public static boolean basicIsPrime (Long min, Long number) {
		if (number < 16) { //added just to avoid issue with the square root below
			if (number == 2 || number == 3 || number == 5 || number == 7 || number == 11 || number == 13) {
				Log.d("Prime", number + " is a prime under 16");
				return true;
			} else {
				Log.d("Prime", number + " is not a prime and is below 16");
				return false;
			}
		} else {
			Long max = (long) Math.sqrt((double) number);
			while (min <= max) { //no need to check mod after the square root of the number
				if (number % min == 0) {
					Log.d("Prime", "Divisible by " + min);
					return false;
				} else {
					min += 2;
				}
			}
		}
		return true;
	}
	
	public static String printPrimeInRange(int index, Long number) {
		String output = "Prime numbers until\n" + number + ": ";
		//note that index should be the number of items to be displayed, or 1 bigger than the final array size
		int trueIndex = index;
		while (index > 1) {
			output += Calculator.list.get(trueIndex - index) + ", ";
			index--;
		}
		output += Calculator.list.get(trueIndex - 1); //prints last value without extra comma afterwards
		return output;
	}


}
