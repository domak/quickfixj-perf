/*******************************************************************************
 * Copyright (c) quickfixengine.org  All rights reserved. 
 * 
 * This file is part of the QuickFIX FIX Engine 
 * 
 * This file may be distributed under the terms of the quickfixengine.org 
 * license as defined by quickfixengine.org and appearing in the file 
 * LICENSE included in the packaging of this file. 
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING 
 * THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 * 
 * See http://www.quickfixengine.org/LICENSE for licensing information. 
 * 
 * Contact ask@quickfixengine.org if any conditions of this licensing 
 * are not clear to you.
 ******************************************************************************/

package org.dmk.quickfixj.perf.server;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import quickfix.InvalidMessage;
import quickfix.field.ClOrdID;
import quickfix.field.OrdType;
import quickfix.field.OrderQty;
import quickfix.field.Side;
import quickfix.field.Symbol;
import quickfix.field.TransactTime;
import quickfix.fix44.NewOrderSingle;

public class MessagePerf {

	private static final long FLAG = (((long) Integer.MAX_VALUE) << 32) + Integer.MAX_VALUE;

	private static final long FLAG1 = Integer.MAX_VALUE << 31 + Integer.MAX_VALUE;
	private static final long FLAG11 = Integer.MAX_VALUE << 33 + Integer.MAX_VALUE;
	private static final long FLAG2 = Integer.MAX_VALUE << 31;
	private static final long FLAG3 = Integer.MAX_VALUE >> 32;

	private static long build(int index1, int index2) {
		return (((long) index1) << 32) + index2;
	}

	public static int compare(int index1, int index2) {

		System.out.print("index1: ");
		print(index1);
		System.out.print("index2: ");
		print(index2);

		long flag = build(index1, index2);
		System.out.print("build: ");
		print(flag);

		long result = FLAG & flag;
		System.out.print("res: ");
		print(result);
		System.out.println("eq: " + (result == FLAG));
		System.out.println("inf: " + (result < Integer.MAX_VALUE));
		System.out.println("sups: " + (result > Integer.MAX_VALUE));

		System.out.println("------------------------------------------------");

		// long value = Integer.MAX_VALUE;
		// System.out.println(Integer.valueOf("F", 16));
		// System.out.println(Integer.toBinaryString(Integer.valueOf("F", 16)));
		// System.out.println(Integer.toBinaryString(Integer.valueOf("4", 16)));
		//
		// print(2);
		// print(2 << 1);
		// print((2 << 1) + 1);
		//
		// String t = "0111111111111111111111111111111101111111111111111111111111111111";
		// Long valueOf = Long.valueOf(t, 2);
		// print(valueOf);
		// // print(Long.MAX_VALUE);
		// // print(FLAG);
		// // print(FLAG1);
		// // print(FLAG11);
		// // print(FLAG2);
		// // print(FLAG3);
		//
		// print((long) Integer.MAX_VALUE);
		// print((long) Integer.MAX_VALUE << 1);
		// print(((long) Integer.MAX_VALUE) << 2);
		// print((((long) Integer.MAX_VALUE) << 2) + 1);
		// print((long) Integer.MAX_VALUE << 31);
		// print((long) Integer.MAX_VALUE << 32);
		// print((long) (Integer.MAX_VALUE << 32) + 1);
		// print((((long) Integer.MAX_VALUE) << 32) + Integer.MAX_VALUE);

		return 0;
	}

	private static void print(long value) {
		System.out.println(value + " - " + Long.toBinaryString(value) + " - " + Long.toBinaryString(value).length()
				+ " - " + Long.toHexString(value) + " - " + Long.toHexString(value).length());
	}

	/**
	 * @param args
	 * @throws InvalidMessage
	 */
	public static void main(String[] args) throws InvalidMessage {
		// compare(Integer.MAX_VALUE, Integer.MAX_VALUE);
		// compare(Integer.MAX_VALUE, 10);
		// compare(10, Integer.MAX_VALUE);
		// compare(10, 10);
		// compare(12, 10);
		// compare(10, 12);
		// System.exit(0);

		final int ITERATIONS = 10 * 1000 * 1000;
		Random random = new Random();
		int counter = 0;
		long toStringTotalTime = 0;
		long buildMessageTotalTime = 0;

		for (int i = 0; i < ITERATIONS; i++) {

			long start = System.nanoTime();

			NewOrderSingle message = new NewOrderSingle(new ClOrdID(Integer.toString(counter++)), new Side(Side.BUY),
					new TransactTime(new Date()), new OrdType(OrdType.MARKET));

			message.setField(new Symbol(Integer.toString(random.nextInt(1000000))));
			message.setField(new OrderQty(random.nextDouble()));
			buildMessageTotalTime += System.nanoTime() - start;

			start = System.nanoTime();
			// System.out.println(message.toString());
			message.toString();
			toStringTotalTime += System.nanoTime() - start;
		}

		System.out.println("build message: " + TimeUnit.NANOSECONDS.toMillis(buildMessageTotalTime) + " ms - "
				+ (buildMessageTotalTime / ITERATIONS) + " ns/message");
		System.out.println("to string: " + TimeUnit.NANOSECONDS.toMillis(toStringTotalTime) + " ms - "
				+ (toStringTotalTime / ITERATIONS) + " ns/message");
	}
}
